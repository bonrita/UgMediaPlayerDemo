package com.bonrita.mediaplayerdemo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MediaStorageUtility {

    private final String STORAGE = "com.bonrita.mediaplayerdemo.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public MediaStorageUtility(Context context) {
        this.context = context;
    }

    public void storeAudioList(ArrayList<Audio> audioList) {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(audioList);
        editor.putString("audioList", json);
        editor.apply();
    }

    public void storeAudioPosition(int audioIndex) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", audioIndex);
        editor.apply();
    }

    public int getCurrentAudioPosition() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);
    }

    public ArrayList<Audio> loadAudioList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}
