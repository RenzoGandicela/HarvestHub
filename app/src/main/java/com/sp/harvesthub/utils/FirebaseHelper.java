package com.sp.harvesthub.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class FirebaseHelper {
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private static final StorageReference storage = FirebaseStorage.getInstance().getReference();

    // Initialize sample data
    public static void initializeSampleData() {
        try {
            // Add sample server
            DatabaseReference serverRef = getServersRef().child("fc3_enjoyers");
            Map<String, Object> serverData = new HashMap<>();
            serverData.put("id", "fc3_enjoyers");
            serverData.put("name", "FC3 Enjoyers");
            serverData.put("description", "Food sharing enthusiasts from SP");
            serverData.put("iconURL", "https://api.dicebear.com/7.x/initials/png?seed=FC3");
            serverRef.setValue(serverData);

            // Add general-chat channel
            DatabaseReference channelRef = getChannelsRef("fc3_enjoyers").child("general_chat");
            Map<String, Object> channelData = new HashMap<>();
            channelData.put("id", "general_chat");
            channelData.put("name", "general-chat");
            channelData.put("description", "General discussion");
            channelRef.setValue(channelData, (error, ref) -> {
                if (error != null) {
                    Log.e("FirebaseHelper", "Error adding channel: " + error.getMessage());
                } else {
                    Log.d("FirebaseHelper", "Channel added successfully");
                }
            });

            // Add sample message
            DatabaseReference messageRef = getMessagesRef("fc3_enjoyers", "general_chat").push();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("text", "Welcome to FC3 Enjoyers!");
            messageData.put("senderId", "sample_user");
            messageData.put("timestamp", Long.valueOf(System.currentTimeMillis()));
            messageData.put("imageURL", null);
            messageRef.setValue(messageData);

            // Add sample blink
            DatabaseReference blinkRef = getStatusesRef().push();
            Map<String, Object> blinkData = new HashMap<>();
            blinkData.put("imageURL", "https://api.dicebear.com/7.x/avatars/png?seed=sample_user");
            blinkData.put("userId", "sample_user");
            blinkData.put("timestamp", System.currentTimeMillis());
            blinkRef.setValue(blinkData, (error, ref) -> {
                if (error != null) {
                    Log.e("FirebaseHelper", "Error adding blink: " + error.getMessage());
                } else {
                    Log.d("FirebaseHelper", "Blink added successfully");
                }
            });
        } catch (Exception e) {
            Log.e("FirebaseHelper", "Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Server methods
    public static DatabaseReference getServersRef() {
        return database.child("Servers");
    }

    public static DatabaseReference getServerRef(String serverId) {
        return getServersRef().child(serverId);
    }

    public static DatabaseReference getChannelsRef(String serverId) {
        return getServerRef(serverId).child("channels");
    }

    public static DatabaseReference getChannelRef(String serverId, String channelId) {
        return getChannelsRef(serverId).child(channelId);
    }

    public static DatabaseReference getMessagesRef(String serverId, String channelId) {
        return getChannelRef(serverId, channelId).child("messages");
    }

    // Status (Blinks) methods
    public static DatabaseReference getStatusesRef() {
        return database.child("Statuses");
    }

    public static DatabaseReference getUserStatusRef(String userId) {
        return getStatusesRef().child(userId);
    }

    // Storage methods
    public static StorageReference getBlinkImagesRef() {
        return storage.child("blink_images");
    }

    public static StorageReference getChatImagesRef() {
        return storage.child("chat_images");
    }
} 