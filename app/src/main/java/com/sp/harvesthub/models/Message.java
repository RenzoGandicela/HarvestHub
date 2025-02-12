package com.sp.harvesthub.models;

public class Message {
    private String message;
    private String imageUrl;
    private String senderId;
    private String senderName;
    private String senderProfilePic;
    private long timestamp;

    public Message() {
        // Required empty constructor for Firebase
    }

    public Message(String message, String imageUrl, String senderId, String senderName, String senderProfilePic, long timestamp) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePic = senderProfilePic;
        this.timestamp = timestamp;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderProfilePic() {
        return senderProfilePic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderProfilePic(String senderProfilePic) {
        this.senderProfilePic = senderProfilePic;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 