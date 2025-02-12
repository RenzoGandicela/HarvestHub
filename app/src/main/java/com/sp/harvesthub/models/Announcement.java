package com.sp.harvesthub.models;

public class Announcement {
    private String image;
    private String title;
    private String description;
    private String location;
    private String details;
    private long timestamp;
    private int eventType;

    public Announcement() {
        // Required empty constructor for Firebase
    }

    public Announcement(String image, String title, String description, String location, String details, int eventType) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
        this.details = details;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    // Convert to FeaturedHelperClass for adapter
    public FeaturedHelperClass toFeaturedHelper() {
        return new FeaturedHelperClass(image, title, description, location, details, eventType);
    }

    // Getters and setters
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getEventType() { return eventType; }
    public void setEventType(int eventType) { this.eventType = eventType; }
} 