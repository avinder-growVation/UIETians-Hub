package com.medha.avinder.uietianshub.models;

import java.io.Serializable;

public class Gallery implements Serializable {
    private int id;
    private String link, timestamp;

    public Gallery() {
    }

    public Gallery(int id, String link, String timestamp) {
        this.id = id;
        this.link = link;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}