package com.sp.harvesthub.models;

public class User {
    private String userId;
    private String username;
    private String profilePicture;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String userId, String username, String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
} 