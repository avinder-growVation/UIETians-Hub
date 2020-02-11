package com.medha.avinder.uietianshub.models;

/**
 * Created by Avinder on 26-01-2018.
 */

public class Timeline {
    private int id;
    private String title, image, details, timestamp, sender;

    public Timeline() {
    }

    public Timeline(int id, String title, String image, String details, String timestamp, String sender) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.details = details;
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
