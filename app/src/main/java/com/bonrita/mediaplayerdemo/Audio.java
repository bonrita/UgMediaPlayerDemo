package com.bonrita.mediaplayerdemo;

import java.io.Serializable;


public class Audio implements Serializable {

    private String genre;
    private String album;
    private String author;
    private String title;
    private String url;


    public Audio(String genre, String album, String author, String title, String url) {
        this.genre = genre;
        this.album = album;
        this.author = author;
        this.title = title;
        this.url = url;
    }

    public String getGenre() {
        return genre;
    }

    public String getAlbum() {
        return album;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
