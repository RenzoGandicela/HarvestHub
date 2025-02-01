package com.sp.splashscreen2.HelperClasses;

public class FeaturedHelperClass {
    private String image;
    private String title;
    private String description;
    private String location;
    private String details;
    private int eventType;  // New field for event type

    public FeaturedHelperClass(String image, String title, String description, String location, String details, int eventType) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
        this.details = details;
        this.eventType = eventType;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}