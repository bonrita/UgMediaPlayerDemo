package com.bonrita.mediaplayerdemo;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bonrita.mediaplayerdemo.PlayNewAudio";

    boolean serviceBound = false;

    private static final String SOURCE_URL = "http://10.0.2.2:8080/drupal8/api/v1/song_list?_format=json";

    ArrayList<Audio> audioList;

    // Service player that houses all functionality to play music.
    private MediaPlayerService servicePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Override the default actionbar so as i use the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Load mock data.
        loadMockData();

        initRecyclerView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabOnclickListener);


    }

    View.OnClickListener fabOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Fab btn clicked", Toast.LENGTH_LONG).show();
        }
    };

    private void initRecyclerView() {
        if (audioList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            MediaRecyclerViewAdapter adapter = new MediaRecyclerViewAdapter(audioList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new AudioTouchListener(this, new AudioTouchListener.onItemClickListener() {

                @Override
                public void onClick(View view, int position) {
                    Toast.makeText(getApplicationContext(), "Clicked me " + audioList.get(position).getTitle(), Toast.LENGTH_LONG).show();
                    playAudio(position);
                }
            }));
//            Log.d("BONRI", "TEST TEST");
        }
    }

    // Bind this activity (client) to the MediaPlayer service.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
            servicePlayer = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    /**
     * Play an audio at a given index in the audio list.
     *
     * @param audioIndex The index of the audio to play.
     */
    private void playAudio(int audioIndex) {

        // Check if the service is active.
        if (!serviceBound) {
            // Store a serializable audioList to shared preferences.
            MediaStorageUtility storage = new MediaStorageUtility(getApplicationContext());
            storage.storeAudioList(audioList);
            storage.storeAudioPosition(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
            // Store the new audioIndex to SharedPreferences
            MediaStorageUtility storage = new MediaStorageUtility(getApplicationContext());
            storage.storeAudioPosition(audioIndex);

            // If service is active send a broadcast "PLAY_NEW_AUDIO" to the MediaPlayerService
            Intent broadCastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadCastIntent);
        }

    }

    private void loadRemoteData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data ....");
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, SOURCE_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();

                try {
                    String statusCode = response.getString("statusCode");
                    Log.d("statusCode", statusCode);

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("X-Force-Status-Code-200", "1");
                return params;
            }
        };

        // Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Adding our request to the que
        requestQueue.add(jsonObjectRequest);

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

                    Audio audio = new Audio(genre, album, author, title, url);

                    // Save to audioList.
                    audioList.add(audio);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
