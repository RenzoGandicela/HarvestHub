package com.sp.harvesthub.models;

public class Blink {
    private String imageURL;
    private String userId;
    private String name;
    private long timestamp;

    public Blink() {
        // Set default timestamp in empty constructor
        this.timestamp = System.currentTimeMillis();
    }

    public Blink(String userId, String imageURL, String name, long timestamp) {
        this.imageURL = imageURL;
        this.userId = userId;
        this.name = name;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    // Getters and setters
    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    // Add getter and setter for name
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
} 