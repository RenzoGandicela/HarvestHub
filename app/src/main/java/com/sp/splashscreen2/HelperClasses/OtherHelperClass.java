package com.sp.splashscreen2.HelperClasses;

public class OtherHelperClass {

    private String title, details, location, description, image;

    public OtherHelperClass(String image, String title, String description, String location, String details) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
        this.details = details;

    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getDetails() {
        return details;
    }

}
