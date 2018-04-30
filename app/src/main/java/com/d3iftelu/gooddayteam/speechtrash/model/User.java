package com.d3iftelu.gooddayteam.speechtrash.model;

/**
 * Created by Sholeh Hermawan on 22/04/2018.
 */

public class User {
    private String name;
    private String photoUrl;
    private double latitude;
    private double longitude;
    private String token;

    public User() {
    }

    public User(String name, String photoUrl, double latitude, double longitude, String token) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.token = token;
    }

    public User(String name, String photoUrl, double latitude, double longitude) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getToken() {
        return token;
    }
}
