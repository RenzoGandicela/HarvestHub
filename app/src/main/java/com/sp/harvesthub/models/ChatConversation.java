package com.sp.harvesthub.models;

public class ChatConversation {
    private String conversationId;
    private String otherUserId;
    private String otherUsername;
    private String otherUserProfilePic;
    private String lastMessage;
    private long lastMessageTimestamp;
    private boolean isOnline;

    public ChatConversation() {
        // Required empty constructor for Firebase
    }

    public ChatConversation(String conversationId, String otherUserId, String otherUsername, 
                          String otherUserProfilePic, String lastMessage, long lastMessageTimestamp) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.otherUserProfilePic = otherUserProfilePic;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // Getters and Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUsername() { return otherUsername; }
    public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }

    public String getOtherUserProfilePic() { return otherUserProfilePic; }
    public void setOtherUserProfilePic(String otherUserProfilePic) { this.otherUserProfilePic = otherUserProfilePic; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
} 