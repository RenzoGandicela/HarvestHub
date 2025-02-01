package com.sp.harvesthub.models;

public class CalendarNote {
    private String note;
    private long timestamp;

    // Required empty constructor for Firebase
    public CalendarNote() {
        this.timestamp = System.currentTimeMillis();
    }

    public CalendarNote(String note) {
        this.note = note;
        this.timestamp = System.currentTimeMillis();
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 