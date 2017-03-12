package com.bonrita.mediaplayerdemo;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener{

    // The binder to be given to clients.
    private final IBinder iBinder = new MediaPlayerBinder();
    // The audio index from the audio list.
    private int audioIndex = -1;
    // Truck the current audio index that is playing.
    private int currentPlayingAudioIndex = -1;
    // The list holding the songs to play.
    private ArrayList<Audio> audioList;
    // The song to play.
    private Audio currentAudio;
    private MediaPlayer mediaPlayer;
    private MediaSessionManager mediaSessionManager;
    // Store current audio position to resume from when audio is paused.
    private int resumeAudioPosition;
    // False if not paused and true when paused.
    private boolean audioPaused;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityBroadcastPlayNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("====>>> Function called", "onStartCommand");
        // This method is called by android when an activity requests this service to be started.
        //Get audio list data from shared preferences.
        MediaStorageUtility mediaStorage = new MediaStorageUtility(getApplicationContext());
        audioList = mediaStorage.loadAudioList();
        audioIndex = mediaStorage.getCurrentAudioPosition();
        audioPaused = false;

        if (audioIndex != -1 && audioIndex < audioList.size() && currentPlayingAudioIndex == audioIndex) {
            Toast.makeText(getApplicationContext(), "ONSTARTCOMMAND Same audio index already playing, pause audio", Toast.LENGTH_SHORT).show();
        }

        if (audioIndex != -1 && audioIndex < audioList.size()) {
            currentAudio = audioList.get(audioIndex);
            if (audioIndex != currentPlayingAudioIndex) {
                currentPlayingAudioIndex = audioIndex;
            }
        } else {
            stopSelf();
        }

        if (mediaSessionManager == null) {
            Log.d("====>>> Function called", "onStartCommand : initMediaPlayer");
            initMediaPlayer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        // Set up the MediaPlayer event listeners.
        mediaPlayer.setOnCompletionListener(this);

        // Called to update status in buffering a media stream received through progressive HTTP download.
        mediaPlayer.setOnPreparedListener(this);

        // Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set the url of the audio.
            mediaPlayer.setDataSource(currentAudio.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    /**
     * Stop the current audio that is playing from playing.
     */
    private void stopCurrentAudioPlay() {
        if (mediaPlayer == null) {
            return;
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    /**
     * Pause audio when paused.
     */
    private void pauseAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumeAudioPosition = mediaPlayer.getCurrentPosition();
            audioPaused = true;
        }
    }

    /**
     * Resume audio from the position it was paused.
     */
    private void resumeAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumeAudioPosition);
            mediaPlayer.start();
            audioPaused = false;
        }
    }

    private BroadcastReceiver PlayAudioBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("====>>> Function called", "BroadcastReceiver:onReceive");
            audioIndex = new MediaStorageUtility(getApplicationContext()).getCurrentAudioPosition();

            if (audioPaused && currentPlayingAudioIndex == audioIndex) {
                Toast.makeText(getApplicationContext(), "BROADCAST Resume audio", Toast.LENGTH_SHORT).show();
                // Resume playing the audio that was paused.
                resumeAudio();
            } else {

                if (audioIndex != -1 && audioIndex < audioList.size() && currentPlayingAudioIndex == audioIndex && !audioPaused) {
                    Toast.makeText(getApplicationContext(), "BROADCAST Pause audio", Toast.LENGTH_SHORT).show();
                    AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
                    audioTrackingEvent.setPaused(true);
                    EventBus.getDefault().post(audioTrackingEvent);
                    pauseAudio();
                } else {
                    audioPaused = false;
                    if (audioIndex != -1 && audioIndex < audioList.size()) {
                        currentAudio = audioList.get(audioIndex);

                        if (audioIndex != currentPlayingAudioIndex) {
                            currentPlayingAudioIndex = audioIndex;
                        }
                    } else {
                        stopSelf();
                    }


                    // Reset the media player to play the new audio.
                    stopCurrentAudioPlay();
                    mediaPlayer.reset();
                    Toast.makeText(getApplicationContext(), "BROADCAST Play audio", Toast.LENGTH_SHORT).show();
                    AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
                    audioTrackingEvent.setPlaying(true);
                    EventBus.getDefault().post(audioTrackingEvent);
                    initMediaPlayer();

                    Log.d("====>>> Function called", "BroadcastReceiver: onReceiver : initMediaPlayer index:"+Integer.toString(audioIndex));
                }
            }
        }
    };

    /**
     * Grab the broadcast message that was broadcasted by the client (activity).
     * And then register the broadcast receiver that will act up on this broadcast.
     */
    private void registerActivityBroadcastPlayNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(PlayAudioBroadcastReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "On completion in service: "+Integer.toString(audioIndex), Toast.LENGTH_SHORT).show();

        // Broadcast completion.
        AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
        audioTrackingEvent.setCompleted(true);
        EventBus.getDefault().post(audioTrackingEvent);

        // Reset value of the current audio index so that the audio continues playing smoothly.
        currentPlayingAudioIndex = -1;
        // Make sure the current audio that is playing is completely stopped form playing.
        stopCurrentAudioPlay();

        // Stop this service.
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "On prepared in service: "+Integer.toString(audioIndex), Toast.LENGTH_SHORT).show();
        // This is invoked when the media source is ready for playback
        playAudio();
    }

    private void playAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "On seek audio: "+Integer.toString(audioIndex), Toast.LENGTH_SHORT).show();
    }

    public class MediaPlayerBinder extends Binder {
        /**
         * Return the MediaPlayerService so that clients can access it's methods.
         *
         * @return MediaPlayerService.
         */
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
