package com.d3iftelu.gooddayteam.speechtrash.model;

public class MarkerData {
    private String imageUrl;
    private double lat;
    private double lng;
    private String name;

    public MarkerData() {
    }

    public MarkerData(String imageUrl, double lat, double lng, String name) {
        this.imageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }
}
