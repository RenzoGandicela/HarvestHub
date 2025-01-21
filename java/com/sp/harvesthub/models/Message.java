package com.sp.harvesthub.models;

public class Message {
    private String text;
    private String imageURL;
    private String senderId;
    private String senderName;
    private String senderProfilePic;
    private long timestamp;

    public Message() {
        // Required empty constructor for Firebase
    }

    public Message(String text, String imageURL, String senderId, String senderName, String senderProfilePic, long timestamp) {
        this.text = text;
        this.imageURL = imageURL;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePic = senderProfilePic;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderProfilePic() { return senderProfilePic; }
    public void setSenderProfilePic(String senderProfilePic) { this.senderProfilePic = senderProfilePic; }
} 