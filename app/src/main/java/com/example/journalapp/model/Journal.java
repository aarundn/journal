package com.example.journalapp.model;

import com.google.firebase.Timestamp;

public class Journal {
    private String title;
    private String description;
    private String imageUrl;
    private String userId;
    private Timestamp timesAdd;
    private String userName;

    public Journal() {
    }

    public Journal(String title, String description, String imageUrl,
                   String userId, Timestamp timesAdd, String userName) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timesAdd = timesAdd;
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimesAdd() {
        return timesAdd;
    }

    public void setTimesAdd(Timestamp timesAdd) {
        this.timesAdd = timesAdd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
