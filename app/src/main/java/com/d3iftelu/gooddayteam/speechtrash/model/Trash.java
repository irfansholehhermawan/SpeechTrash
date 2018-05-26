package com.d3iftelu.gooddayteam.speechtrash.model;

public class Trash {
    private int volume;
    private int berat;
    private String time;

    public Trash() {
    }

    public Trash(int volume, int berat) {
        this.volume = volume;
        this.berat = berat;
    }

    public Trash(int volume, int berat, String time) {
        this.volume = volume;
        this.berat = berat;
        this.time = time;
    }

    public int getVolume() {
        return volume;
    }

    public int getBerat() {
        return berat;
    }

    public String getTime() {
        return time;
    }
}
