package com.d3iftelu.gooddayteam.speechtrash.model;

public class Trash {
    private int volume;
    private int berat;

    public Trash() {
    }

    public Trash(int volume, int berat) {
        this.volume = volume;
        this.berat = berat;
    }

    public int getVolume() {
        return volume;
    }

    public int getBerat() {
        return berat;
    }
}
