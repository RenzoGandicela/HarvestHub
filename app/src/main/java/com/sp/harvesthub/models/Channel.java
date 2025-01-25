package com.sp.harvesthub.models;

import java.util.Map;

public class Channel {
    private String id;
    private String name;
    private String description;
    private Map<String, Message> messages;

    public Channel() {
        // Required empty constructor for Firebase
    }

    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

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

    // Getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, Message> getMessages() { return messages; }
    public void setMessages(Map<String, Message> messages) { this.messages = messages; }
} 