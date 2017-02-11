package com.bonrita.mediaplayerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bonrita.mediaplayerdemo.PlayNewAudio";

    boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
