package com.sp.splashscreen2.HelperClasses;



public class FeaturedHelperClass {

    int image;
    String title, description, location, details;


    public FeaturedHelperClass(int image, String title, String description, String location, String details) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.location = location;
        this.details = details;

    }

    public int getImage() {
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
