package com.sp.harvesthub.models;

public class FeaturedHelperClass {
    private String image;
    private String title;
    private String description;
    private String location;
    private String date;
    private String time;
    private int eventType;  // 1 for regular events, 2 for donation drives
    private long timestamp;

    public FeaturedHelperClass(String image, String title, String description, String location, String date, String time) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.eventType = 1;
        this.timestamp = System.currentTimeMillis();
    }

    public FeaturedHelperClass(String image, String title, String description, String location, String date, String time, int eventType) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters for the fields
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}