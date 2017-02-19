package com.bonrita.mediaplayerdemo;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;

public class MediaPlayerService extends Service {

    // The binder to be given to clients.
    private final IBinder iBinder = new MediaPlayerBinder();
    private int currentAudioPosition = -1;
    private ArrayList<Audio> audioList;
    private Audio currentAudio;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityBroadcastPlayNewAudio();
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {

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
        }
    };

    /**
     * Stop the current audio that is playing from playing.
     */
    private void stopCurrentAudioPlay() {
    }

    /**
     * Grab the broadcast message that was broadcasted by the client (activity).
     * And then register the broadcast receiver that will act up on this broadcast.
     */
    private void registerActivityBroadcastPlayNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
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
