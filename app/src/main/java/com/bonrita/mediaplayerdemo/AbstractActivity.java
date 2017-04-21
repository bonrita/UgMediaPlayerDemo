package com.bonrita.mediaplayerdemo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

abstract public class AbstractActivity extends AppCompatActivity {

    ArrayList<Audio> audioList;
    protected int activePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activePosition = -1;
        // Load mock data.
        loadMockData();
    }

    /**
     * Get data from a text resource File that contains json data.
     */
    protected void loadMockData() {

        int ctr;
        InputStream inputStream = getResources().openRawResource(R.raw.song_list);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse the data.
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());

            String status_code = jObject.getString("statusCode");

            if (status_code.equalsIgnoreCase("200")) {
                JSONArray jArray = jObject.getJSONArray("data");

                audioList = new ArrayList<>();

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject song = jArray.getJSONObject(i);

                    String genre = song.getString("genre");
                    String album = song.getString("album");
                    String author = song.getString("author");
                    String title = song.getString("title");
                    String url = song.getString("url");
                    String imgUrl = song.getString("img");

                    Audio audio = new Audio(genre, album, author, title, url, imgUrl);

                    // Save to audioList.
                    audioList.add(audio);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
