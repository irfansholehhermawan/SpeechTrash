package com.d3iftelu.gooddayteam.speechtrash.model;

/**
 * Created by Sholeh Hermawan on 22/04/2018.
 */

public class User {
    private String user_id;
    private String name;
    private String photoUrl;
    private double latitude;
    private double longitude;
    private String token;
    private boolean validasi;
    private String idUSer;

    public User() {
    }

    public User(String user_id, String name, String photoUrl, double latitude, double longitude, String token, boolean validasi) {
        this.user_id = user_id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.token = token;
        this.validasi = validasi;
    }

    public User(User user, String idUser) {
        this.user_id = user.getUser_id();
        this.name = user.getName();
        this.photoUrl = user.getPhotoUrl();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
        this.token = user.getToken();
        this.idUSer = idUser;
    }

    public String getIdUSer() {
        return idUSer;
    }

    public String getUser_id() {
        return user_id;
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

    public boolean isValidasi() {
        return validasi;
    }
}
