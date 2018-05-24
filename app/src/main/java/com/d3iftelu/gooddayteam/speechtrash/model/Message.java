package com.d3iftelu.gooddayteam.speechtrash.model;

public class Message {
    private String messageId;
    private String user_id;
    private String name;
    private String message;
    private String timestamp;

    public Message() {
    }

    public Message(String user_id, String message, String name, String timestamp) {
        this.user_id = user_id;
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Message(Message messages, String messageId) {
        this.user_id = messages.getUser_id();
        this.name = messages.getName();
        this.message = messages.getMessage();
        this.timestamp = messages.getTimestamp();
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
