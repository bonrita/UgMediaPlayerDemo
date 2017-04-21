package com.bonrita.mediaplayerdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AudioDetailActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_detail);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            activePosition = (int)extras.get("current-index");
            // And art cover.
            ImageView artCover = (ImageView) findViewById(R.id.song_art_cover);
            Picasso.with(this).load(audioList.get(activePosition).getImageUrl()).placeholder(R.drawable.music_world).into(artCover);

            // Add song title.
            TextView songTitle = (TextView) findViewById(R.id.song_title);
            songTitle.setText(audioList.get(activePosition).getTitle());

            // Add song author
            TextView songAuthor = (TextView) findViewById(R.id.song_author);
            songAuthor.setText(audioList.get(activePosition).getAuthor());

        }
    }

}
