package com.bonrita.mediaplayerdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;


public class MediaRecyclerViewAdapter extends RecyclerView.Adapter<MediaRecyclerViewAdapter.MediaViewHolder> {

    List<Audio> audioList = Collections.emptyList();
    Context context;

    public MediaRecyclerViewAdapter(List<Audio> audioList, Context context) {
        this.audioList = audioList;
        this.context = context;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        MediaViewHolder holder = new MediaViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        holder.title.setText(audioList.get(position).getTitle());
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            if (payloads.get(0) instanceof AudioTrackingEvent) {
                AudioTrackingEvent audioTrackingEvent = (AudioTrackingEvent) payloads.get(0);
                if (audioTrackingEvent.isStopped()) {
                    holder.play_pause.setImageResource(R.drawable.ic_play_black_48dp);
                    holder.title.setText(audioList.get(position).getTitle() + " (stopped)");
                } else if(audioTrackingEvent.isPlaying()) {
                    holder.title.setText(audioList.get(position).getTitle()+ " (playing)");
                    holder.play_pause.setImageResource(R.drawable.ic_play_black_48dp);
                } else if(audioTrackingEvent.isPaused()) {
                    holder.title.setText(audioList.get(position).getTitle()+ " (paused)");
                    holder.play_pause.setImageResource(R.drawable.ic_pause);
                }
            }

        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView play_pause;

        public MediaViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            play_pause = (ImageView) itemView.findViewById(R.id.play_pause);
        }

    }
}
