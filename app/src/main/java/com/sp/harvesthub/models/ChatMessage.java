package com.sp.harvesthub.models;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    private String senderId;
    private String username;
    private String message;
    private List<String> imageUrls;
    private long timestamp;

    public ChatMessage() {
        // Required empty constructor for Firebase
    }

    public ChatMessage(String senderId, String username, String message, List<String> imageUrls) {
        this.senderId = senderId;
        this.username = username;
        this.message = message;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 