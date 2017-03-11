package com.bonrita.mediaplayerdemo;


public class AudioTrackingEvent {

    private boolean paused;
    private boolean stopped;
    private boolean playing;

    public AudioTrackingEvent() {

    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStop(boolean stop) {
        this.stopped = stop;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
