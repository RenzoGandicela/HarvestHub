package com.sp.harvesthub.models;

public class DirectMessage {
    private String messageId;
    private String content;
    private String imageUrl;
    private String senderId;
    private String username;
    private String profilePictureUrl;
    private long timestamp;

    public DirectMessage() {
        // Required empty constructor for Firebase
        this.messageId = "";
        this.content = "";
        this.timestamp = System.currentTimeMillis();
    }

    public DirectMessage(String messageId, String content, String imageUrl, String senderId, String username, String profilePictureUrl, long timestamp) {
        this.messageId = messageId != null ? messageId : "";
        this.content = content != null ? content : "";
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
        this.timestamp = timestamp;
    }

    public DirectMessage(String messageId, String content, String imageUrl, String senderId, long timestamp) {
        this.messageId = messageId != null ? messageId : "";
        this.content = content != null ? content : "";
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getMessageId() { return messageId != null ? messageId : ""; }
    public String getContent() { return content != null ? content : ""; }
    public String getImageUrl() { return imageUrl; }
    public String getSenderId() { return senderId; }
    public String getUsername() { return username; }
    public long getTimestamp() { return timestamp; }
    public String getProfilePictureUrl() { return profilePictureUrl; }

    public void setMessageId(String messageId) { this.messageId = messageId != null ? messageId : ""; }
    public void setContent(String content) { this.content = content != null ? content : ""; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setUsername(String username) { this.username = username; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
} 