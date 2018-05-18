package com.d3iftelu.gooddayteam.speechtrash.model;

public class Message {
    private String name;
    private String message;
    private long timestamp;

    public Message() {
    }

    public Message(String message, String name, long timestamp) {
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
