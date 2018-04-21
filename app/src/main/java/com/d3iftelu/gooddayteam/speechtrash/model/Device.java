package com.d3iftelu.gooddayteam.speechtrash.model;

/**
 * Created by Sholeh Hermawan on 21/04/2018.
 */

public class Device {
    private String deviceName;
    private String deviceId;
    private boolean status;

    public Device() {
    }

    public Device(String deviceName, String deviceId) {
        this.deviceName = deviceName;
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
