package com.example.lesorac.model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private int numRatings;
    private double avgRating;
    private String photo;
    private String fcmToken;

    public User() {
    }

    public User(String uid, String name, int numRatings, double avgRating, String photo) {
        this.uid = uid;
        this.name = name;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
        this.photo = photo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getfcmToken() { return fcmToken; }

    public void setfcmToken(String FcmToken) { this.fcmToken = FcmToken; }
}
