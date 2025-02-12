package com.sp.harvesthub.models;

public class FeaturedHelperClass {
    private String image;
    private String title;
    private String description;
    private String location;
<<<<<<< HEAD
    private String details;
    private int eventType;  // 1 for regular events, 2 for donation drives
    private long timestamp;

    public FeaturedHelperClass(String image, String title, String description, String location, String details) {
=======
    private String date;
    private String time;
    private int eventType;  // 1 for regular events, 2 for donation drives
    private long timestamp;

    public FeaturedHelperClass(String image, String title, String description, String location, String date, String time) {
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
<<<<<<< HEAD
        this.details = details;
=======
        this.date = date;
        this.time = time;
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
        this.eventType = 1;
        this.timestamp = System.currentTimeMillis();
    }

<<<<<<< HEAD
    public FeaturedHelperClass(String image, String title, String description, String location, String details, int eventType) {
=======
    public FeaturedHelperClass(String image, String title, String description, String location, String date, String time, int eventType) {
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
<<<<<<< HEAD
        this.details = details;
=======
        this.date = date;
        this.time = time;
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
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

<<<<<<< HEAD
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
=======
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
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
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