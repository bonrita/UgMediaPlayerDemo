package com.bonrita.mediaplayerdemo;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MediaPlayerService extends Service {

    // The binder to be given to clients.
    private final IBinder iBinder = new MediaPlayerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    private class MediaPlayerBinder extends Binder {
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
