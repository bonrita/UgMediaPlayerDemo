package com.bonrita.mediaplayerdemo;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
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
    // Sample mp3 to use.
// http://www.stephaniequinn.com/samples.htm
//    http://www.pacdv.com/sounds/ambience_sounds.html

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bonrita.mediaplayerdemo.PlayNewAudio";

    boolean serviceBound = false;

    private static final String SOURCE_URL = "http://10.0.2.2:8080/drupal8/api/v1/song_list?_format=json";

    ArrayList<Audio> audioList;

    // Service player that houses all functionality to play music.
    private MediaPlayerService servicePlayer;

    private int activePosition;

    private int lastPlayedAudioIndex = -1;

    private MediaRecyclerViewAdapter adapter;

    private BottomSheetBehavior bottomSheetBehavior;

    private boolean isPlaying = false;

    // Buttons and properties to forward or reverse the list.
    private boolean continueForwardForever = false;
    private ImageButton forwardForeverBtn;
    private boolean continueBackwardForever = false;
    private ImageButton backwardForeverBtn;

    // Button to repeat song list or a song.
    private ImageButton repeatBtn;
    private boolean repeatOn = false;

    // Button next song.
    private ImageButton nextSongBtn;
    private ImageButton previousSongBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Override the default actionbar so as i use the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activePosition = -1;

        // Bottom sheet behaviour initialize.
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Load mock data.
        loadMockData();

        initRecyclerView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabOnclickListener);

        // Initialize the Continue playing the list forward forever button.
        forwardForeverBtn = (ImageButton) findViewById(R.id.song_list_forward_forever);
        forwardForeverBtn.setOnClickListener(forwardForeverOnclickListener);

        // Initialize the Continue playing the list forward forever button.
        backwardForeverBtn = (ImageButton) findViewById(R.id.song_list_back_forever);
        backwardForeverBtn.setOnClickListener(backwardForeverOnclickListener);

        // Initialize repeat button.
        repeatBtn = (ImageButton) findViewById(R.id.song_list_loop);
        repeatBtn.setOnClickListener(repeatSongOnclickListener);

        // Initialize next and previous song button.
        nextSongBtn = (ImageButton) findViewById(R.id.song_list_next);
        nextSongBtn.setOnClickListener(nextSongOnclickListener);

        previousSongBtn = (ImageButton) findViewById(R.id.song_list_previous);
        previousSongBtn.setOnClickListener(previousSongOnclickListener);

    }

    View.OnClickListener fabOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    View.OnClickListener nextSongOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isPlaying) {
                boolean playNext = false;
                int newIndex = activePosition + 1;


                AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
                audioTrackingEvent.setStop(true);
                adapter.notifyItemChanged(activePosition, audioTrackingEvent);

                if (newIndex < audioList.size()) {
                    activePosition = newIndex;
                    playNext = true;
                } else if (repeatOn) {
                    activePosition = 0;
                    playNext = true;
                }

                if (playNext) {
                      audioTrackingEvent = new AudioTrackingEvent();
                    audioTrackingEvent.setPlaying(true);
                    adapter.notifyItemChanged(activePosition, audioTrackingEvent);
                    playAudio(activePosition);
                }

            }
        }
    };

    View.OnClickListener previousSongOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean playPrevious = false;
            int newIndex = activePosition - 1;

            if (newIndex < 0 && repeatOn) {
                activePosition = audioList.size() - 1;
                playPrevious = true;
            } else if (newIndex > -1) {
                activePosition = newIndex;
                playPrevious = true;
            }

            if (playPrevious) {
                playAudio(activePosition);
            }
        }
    };

    View.OnClickListener repeatSongOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Reverse music if necessary.
            if (repeatOn) {
                // Turn off repeat.
                repeatOn = false;
                repeatBtn.setImageResource(R.drawable.repeat_24_ff4081);
            } else {
                // Let the list or song repeat.
                repeatOn = true;
                repeatBtn.setImageResource(R.drawable.repeat_24);
            }
        }
    };

    View.OnClickListener backwardForeverOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Reset the forward button.
            continueForwardForever = false;
            forwardForeverBtn.setImageResource(R.drawable.double_down_24_ff4081);

            // Reverse music if necessary.
            if (continueBackwardForever) {
                // Turn off forever.
                continueBackwardForever = false;
                backwardForeverBtn.setImageResource(R.drawable.double_up_24_ff4081);
            } else {
                // Let the list go forward till it reaches the end.
                continueBackwardForever = true;
                backwardForeverBtn.setImageResource(R.drawable.double_up_24);
            }
        }
    };

    View.OnClickListener forwardForeverOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Reset the backward button.
            backwardForeverBtn.setImageResource(R.drawable.double_up_24_ff4081);
            continueBackwardForever = false;

            // Forward the music if necessary.
            if (continueForwardForever) {
                // Turn off forever.
                continueForwardForever = false;
                forwardForeverBtn.setImageResource(R.drawable.double_down_24_ff4081);
            } else {
                // Let the list go forward till it reaches the end.
                continueForwardForever = true;
                forwardForeverBtn.setImageResource(R.drawable.double_down_24);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initRecyclerView() {
        if (audioList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            adapter = new MediaRecyclerViewAdapter(audioList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new AudioTouchListener(this, new AudioTouchListener.onItemClickListener() {

                @Override
                public void onClick(View view, int position, RecyclerView rv) {

                    // Store current position.
                    activePosition = position;

                    // When a new audio is playing. Turn the previous paused audio icon to play.
                    if (lastPlayedAudioIndex > -1 && lastPlayedAudioIndex != position) {
                        AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
                        audioTrackingEvent.setStop(true);
                        adapter.notifyItemChanged(lastPlayedAudioIndex, audioTrackingEvent);
//                        Toast.makeText(getApplicationContext(), "Last played audio index: " + Integer.toString(lastPlayedAudioIndex), Toast.LENGTH_SHORT).show();
                    }
                    AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
                    audioTrackingEvent.setPlaying(true);
                    adapter.notifyItemChanged(position, audioTrackingEvent);

                    playAudio(position);

                    // The bottomsheet is called here so as to initialize it.
                    // It is done on purpose otherwise it won't show.
                    updateAndControlBottomsheet();

                }
            }));

        }
    }

    /**
     * Update bottom sheet with data from the current song.
     */
    protected void updateAndControlBottomsheet() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {

//            Toast.makeText(this, "Should load picasso image " + Integer.toString(activePosition), Toast.LENGTH_SHORT).show();
            // And art cover icon.
            ImageView artCover = (ImageView) findViewById(R.id.song_art_cover);
            Picasso.with(this).load(audioList.get(activePosition).getImageUrl()).placeholder(R.drawable.music_world).into(artCover);


            // Add song title.
            TextView songTitle = (TextView) findViewById(R.id.song_title);
            songTitle.setText(audioList.get(activePosition).getTitle());

            // Add song author
            TextView songAuthor = (TextView) findViewById(R.id.song_author);
            songAuthor.setText(audioList.get(activePosition).getAuthor());

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    // This method will receive events from the event bus.
    @Subscribe
    public void onAudioTracking(AudioTrackingEvent event) {

//        Toast.makeText(getApplicationContext(), "Audio tracking event received " + Integer.toString(activePosition), Toast.LENGTH_SHORT).show();
        if (event.isPaused()) {
            isPlaying = false;
            adapter.notifyItemChanged(activePosition, event);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (event.isCompleted()) {
            isPlaying = false;
            adapter.notifyItemChanged(activePosition, event);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Repeat the current song indefinitely.
            if (repeatOn && !continueForwardForever && !continueBackwardForever) {
                AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
                audioTrackingEvent.setPlaying(true);
                adapter.notifyItemChanged(activePosition, audioTrackingEvent);
                playAudio(activePosition);
            }

            // Forward the list till the end of it.
            PlayTheAudioListFoward();

            // Reverse the list backwards till the start of the list.
            PlayTheAudioListBackward();
        }

        if (event.isPlaying()) {

            adapter.notifyItemChanged(activePosition, event);

            // Add song data to the bottom sheet.
            // The bottom sheet behaviour is repeated in this method on purpose.
            // It solves a behaviour of the bottom sheet to show at the right audio events.
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            updateAndControlBottomsheet();
        }
    }

    /**
     * Play the playlist / audio list backward.
     * <p>
     * Reverse the list backwards till the start of the list.
     */
    protected void PlayTheAudioListBackward() {
        if (continueBackwardForever) {
            int newIndex = activePosition - 1;
            if (newIndex < 0) {
                if (repeatOn) {
                    // Last key is total minus one.
                    activePosition = audioList.size() - 1;
                    bottomSheetPlayAudioHelper(activePosition);
                } else {
                    // We have reached the end of the list. Reset the buttons.
                    activePosition = -1;
                    continueBackwardForever = false;
                    backwardForeverBtn.setImageResource(R.drawable.double_up_24_ff4081);
                }

            } else {
                activePosition = newIndex;
                bottomSheetPlayAudioHelper(newIndex);
            }
        }
    }

    /**
     * Play audio.
     *
     * @param index
     */
    private void bottomSheetPlayAudioHelper(int index) {
        AudioTrackingEvent audioTrackingEvent = new AudioTrackingEvent();
        audioTrackingEvent.setPlaying(true);
        adapter.notifyItemChanged(index, audioTrackingEvent);
        playAudio(index);
        // The bottomsheet is called here so as to initialize it.
        // It is done on purpose otherwise it won't show.
        updateAndControlBottomsheet();
    }

    /**
     * Play the playlist / audio list forward.
     * <p>
     * Forward the list till the end of it.
     */
    protected void PlayTheAudioListFoward() {
        if (continueForwardForever) {
            int newIndex = activePosition + 1;

            if (newIndex > audioList.size()) {
                // We have reached the end of the list. Reset the buttons.
                activePosition = -1;
                continueForwardForever = false;
                forwardForeverBtn.setImageResource(R.drawable.double_down_24_ff4081);
            } else {
                // Continue playing to the next index.
                activePosition = newIndex;
                bottomSheetPlayAudioHelper(newIndex);
            }
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
//        Toast.makeText(getApplicationContext(), "Clicked me " + audioList.get(audioIndex).getTitle(), Toast.LENGTH_LONG).show();
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

        lastPlayedAudioIndex = audioIndex;
        isPlaying = true;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);

            // Stop player.
            servicePlayer.stopSelf();
        }
    }
}
