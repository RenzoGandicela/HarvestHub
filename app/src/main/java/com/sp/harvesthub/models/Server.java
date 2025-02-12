package com.sp.harvesthub.models;

import java.util.List;
import java.util.Map;

public class Server {
    private String id;
    private String name;
    private String description;
    private String iconURL;
    private Map<String, Channel> channels;

    // Required empty constructor for Firebase
    public Server() {
    }

    // Constructor with all fields
    public Server(String id, String name, String description, String iconURL) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconURL = iconURL;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, Channel> channels) {
        this.channels = channels;
    }
} 