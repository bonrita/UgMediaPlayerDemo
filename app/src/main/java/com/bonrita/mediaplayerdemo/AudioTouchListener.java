package com.bonrita.mediaplayerdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class AudioTouchListener implements RecyclerView.OnItemTouchListener {

    private onItemClickListener onItemClickListener;
    private Context context;
    GestureDetector gestureDetector;

    public AudioTouchListener(Context context, final onItemClickListener onItemClickListener) {
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());

        if (child != null && onItemClickListener != null && gestureDetector.onTouchEvent(e)) {
            onItemClickListener.onClick(child, rv.getChildLayoutPosition(child));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface onItemClickListener {
        public void onClick(View view, int position);
    }
}
