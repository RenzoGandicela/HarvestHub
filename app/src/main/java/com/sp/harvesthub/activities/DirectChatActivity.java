package com.sp.harvesthub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.adapters.DirectMessageAdapter;
import com.sp.harvesthub.models.DirectMessage;
import com.sp.harvesthub.utils.FirebaseImageUploader;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectChatActivity extends AppCompatActivity {
    private String conversationId;
    private String otherUserId;
    private String otherUsername;
    private String currentUserId;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton attachButton;
    private ImageButton backButton;
    private TextView titleText;
    private DirectMessageAdapter messageAdapter;
    private List<DirectMessage> messagesList;
    private DatabaseReference messagesRef;
    private ValueEventListener messagesListener;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_chat);

        // Get data from intent
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUsername = getIntent().getStringExtra("otherUsername");
        
        // Create conversation ID by combining user IDs
        conversationId = currentUserId + "-" + otherUserId;

        if (otherUserId == null) {
            Toast.makeText(this, "Error: Missing user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if conversation exists, if not create it
        FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        createConversation();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DirectChatActivity.this, 
                        "Error checking conversation: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(otherUsername);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initializeViews();

        // Set up RecyclerView
        setupRecyclerView();

        // Load messages
        loadMessages();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        attachButton = findViewById(R.id.attachButton);
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);

        // Set title
        titleText.setText(otherUsername);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Set up attach button
        attachButton.setOnClickListener(v -> selectImage());
    }

    private void setupRecyclerView() {
        messagesList = new ArrayList<>();
        messageAdapter = new DirectMessageAdapter(this, messagesList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        messagesRef = FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId)
            .child("messages");

        messagesListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot messageSnap : snapshot.getChildren()) {
                    DirectMessage message = messageSnap.getValue(DirectMessage.class);
                    if (message != null) {
                        // Ensure the message has an ID
                        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
                            message.setMessageId(messageSnap.getKey());
                        }
                        messagesList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messagesList.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DirectChatActivity.this, 
                    "Error loading messages: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createConversation() {
        long timestamp = System.currentTimeMillis();

        // Create conversation data with exact same structure as website
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("createdAt", timestamp);
        
        // Create initial message
        Map<String, Object> initialMessage = new HashMap<>();
        initialMessage.put("content", "Say hi to your new friend!");
        initialMessage.put("senderId", currentUserId);
        initialMessage.put("timestamp", timestamp);

        // Set last message data
        conversationData.put("lastMessage", initialMessage);
        conversationData.put("lastUpdated", timestamp);

        // Create participants data
        Map<String, Object> participants = new HashMap<>();
        
        // Current user data
        Map<String, Object> currentUserData = new HashMap<>();
        currentUserData.put("id", currentUserId);
        currentUserData.put("username", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        currentUserData.put("profilePicture", "/default-avatar.jpg");
        participants.put(currentUserId, currentUserData);

        // Other user data
        Map<String, Object> otherUserData = new HashMap<>();
        otherUserData.put("id", otherUserId);
        otherUserData.put("username", otherUsername);
        otherUserData.put("profilePicture", "/default-avatar.jpg");
        participants.put(otherUserId, otherUserData);

        conversationData.put("participants", participants);

        // Create the conversation in Firebase
        FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId)
            .setValue(conversationData);
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        // Clear input
        messageInput.setText("");

        long timestamp = System.currentTimeMillis();

        // Create message with exact same structure as website
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", text);
        messageData.put("senderId", currentUserId);
        messageData.put("timestamp", timestamp);

        // Get message ID using timestamp
        String messageId = "-" + timestamp;

        // Create updates map
        Map<String, Object> updates = new HashMap<>();
        updates.put("/messages/" + messageId, messageData);
        updates.put("/lastMessage/content", text);
        updates.put("/lastMessage/senderId", currentUserId);
        updates.put("/lastMessage/timestamp", timestamp);
        updates.put("/lastUpdated", timestamp);

        // Update everything at the conversation level
        FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId)
            .updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                // Message sent successfully
            })
            .addOnFailureListener(e -> {
                Toast.makeText(DirectChatActivity.this, 
                    "Failed to send message: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        
        FirebaseImageUploader.uploadImages(List.of(imageUri), new FirebaseImageUploader.OnUploadCompleteListener() {
            @Override
            public void onComplete(List<String> imageUrls) {
                if (!imageUrls.isEmpty()) {
                    String imageUrl = imageUrls.get(0);
                    
                    // Create message data
                    DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                        .getReference("directMessages")
                        .child(conversationId)
                        .child("messages");

                    String messageId = messagesRef.push().getKey();
                    if (messageId == null) return;

                    // Create message with exact same structure as website
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("content", "");
                    messageData.put("imageUrl", imageUrl);
                    messageData.put("senderId", currentUserId);
                    messageData.put("timestamp", System.currentTimeMillis());

                    // Update both messages and lastMessage atomically
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("/messages/" + messageId, messageData);
                    updates.put("/lastMessage/content", "");
                    updates.put("/lastMessage/imageUrl", imageUrl);
                    updates.put("/lastMessage/senderId", currentUserId);
                    updates.put("/lastMessage/timestamp", System.currentTimeMillis());
                    updates.put("/lastUpdated", System.currentTimeMillis());

                    // Update everything at the conversation level
                    FirebaseDatabase.getInstance()
                        .getReference("directMessages")
                        .child(conversationId)
                        .updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            runOnUiThread(() -> Toast.makeText(DirectChatActivity.this, 
                                "Image sent successfully!", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> Toast.makeText(DirectChatActivity.this, 
                                "Failed to send image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(DirectChatActivity.this, 
                        "Failed to upload image: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }
} 