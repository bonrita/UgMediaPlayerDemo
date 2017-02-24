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

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    // The binder to be given to clients.
    private final IBinder iBinder = new MediaPlayerBinder();
    private int currentAudioPosition = -1;
    private ArrayList<Audio> audioList;
    private Audio currentAudio;
    private MediaPlayer mediaPlayer;
    private MediaSessionManager mediaSessionManager;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityBroadcastPlayNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // This method is called by android when an activity requests this service to be started.
        //Get audio list data from shared preferences.
        MediaStorageUtility mediaStorage = new MediaStorageUtility(getApplicationContext());
        audioList = mediaStorage.loadAudioList();
        currentAudioPosition = mediaStorage.getCurrentAudioPosition();

        if (currentAudioPosition != -1 && currentAudioPosition < audioList.size()) {
            currentAudio = audioList.get(currentAudioPosition);
        } else {
            stopSelf();
        }

        if (mediaSessionManager == null) {
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


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            currentAudioPosition = new MediaStorageUtility(getApplicationContext()).getCurrentAudioPosition();
            if (currentAudioPosition != -1 && currentAudioPosition < audioList.size()) {
                currentAudio = audioList.get(currentAudioPosition);
            } else {
                stopSelf();
            }

            // Reset the media player to play the new audio.
            stopCurrentAudioPlay();
            mediaPlayer.reset();
            initMediaPlayer();
        }
    };

    /**
     * Grab the broadcast message that was broadcasted by the client (activity).
     * And then register the broadcast receiver that will act up on this broadcast.
     */
    private void registerActivityBroadcastPlayNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Make sure the current audio that is playing is completely stopped form playing.
        stopCurrentAudioPlay();

        // Stop this service.
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // This is invoked when the media source is ready for playback
        playAudio();
    }

    private void playAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
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
