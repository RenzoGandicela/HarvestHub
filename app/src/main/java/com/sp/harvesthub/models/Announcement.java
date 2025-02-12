package com.sp.harvesthub.models;

public class Announcement {
    private String image;
    private String title;
    private String description;
    private String location;
<<<<<<< HEAD
    private String details;
=======
    private String date;
    private String time;
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
    private long timestamp;
    private int eventType;

    public Announcement() {
        // Required empty constructor for Firebase
    }

<<<<<<< HEAD
    public Announcement(String image, String title, String description, String location, String details, int eventType) {
=======
    public Announcement(String image, String title, String description, String location, String date,String time , int eventType) {
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

    // Convert to FeaturedHelperClass for adapter
    public FeaturedHelperClass toFeaturedHelper() {
<<<<<<< HEAD
        return new FeaturedHelperClass(image, title, description, location, details, eventType);
=======
        return new FeaturedHelperClass(image, title, description, location, date, time, eventType);
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
    }

    // Getters and setters
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
<<<<<<< HEAD
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
=======

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getEventType() { return eventType; }
    public void setEventType(int eventType) { this.eventType = eventType; }
<<<<<<< HEAD
} 
=======
}
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
