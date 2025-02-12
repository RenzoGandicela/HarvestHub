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
        conversationId = getIntent().getStringExtra("conversationId");
        if (conversationId == null) {
            // Generate a unique conversation ID using Firebase push
            conversationId = FirebaseDatabase.getInstance()
                .getReference("directMessages")
                .push().getKey();
        }

        if (otherUserId == null) {
            Toast.makeText(this, "Error: Missing user information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if conversation exists, if not create it
        DatabaseReference conversationRef = FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId);

        conversationRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId)
            .child("messages");

        messagesListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot messageSnap : snapshot.getChildren()) {
                    String content = messageSnap.child("content").getValue(String.class);
                    String senderId = messageSnap.child("senderId").getValue(String.class);
                    Long timestamp = messageSnap.child("timestamp").getValue(Long.class);
                    String imageUrl = messageSnap.child("imageUrl").getValue(String.class);

                    if (senderId != null && timestamp != null) {
                        DirectMessage message = new DirectMessage(
                            messageSnap.getKey(),
                            content,
                            imageUrl,
                            senderId,
                            timestamp
                        );
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
        DatabaseReference conversationRef = FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId);

        long timestamp = System.currentTimeMillis();

        // Create conversation data
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("conversationId", conversationId);
        conversationData.put("lastMessage", "Say hi to your new friend!");
        conversationData.put("lastMessageTimestamp", timestamp);
        conversationData.put("online", false);
        conversationData.put("otherUserId", otherUserId);
        
        // Get other user's profile picture
        DatabaseReference otherUserRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(otherUserId);
            
        otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String otherUserProfilePic = snapshot.child("profilePicture").getValue(String.class);
                if (otherUserProfilePic == null) {
                    otherUserProfilePic = "/default-avatar.jpg";
                }
                
                conversationData.put("otherUserProfilePic", otherUserProfilePic);
                conversationData.put("otherUsername", otherUsername);

                // Create the conversation
                conversationRef.setValue(conversationData)
                    .addOnSuccessListener(aVoid -> {
                        // Conversation created successfully
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(DirectChatActivity.this, 
                            "Failed to create conversation: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DirectChatActivity.this, 
                    "Error getting user data: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String content, String imageUrl) {
        if ((content == null || content.trim().isEmpty()) && imageUrl == null) {
            return;
        }

        DatabaseReference conversationRef = FirebaseDatabase.getInstance()
            .getReference("directMessages")
            .child(conversationId);

        long timestamp = System.currentTimeMillis();

        // Create message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", content != null ? content.trim() : "");
        messageData.put("senderId", currentUserId);
        messageData.put("timestamp", timestamp);
        if (imageUrl != null) {
            messageData.put("imageUrl", imageUrl);
        }

        // Get a new push ID for the message
        String messageId = conversationRef.child("messages").push().getKey();
        if (messageId == null) return;

        // Create updates map for atomic update
        Map<String, Object> updates = new HashMap<>();
        updates.put("/messages/" + messageId, messageData);
        updates.put("/lastMessage", content != null ? content : "");
        updates.put("/lastMessageTimestamp", timestamp);

        // Perform atomic update
        conversationRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                if (content != null) {
                    messageInput.setText("");
                }
                messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            })
            .addOnFailureListener(e -> 
                Toast.makeText(DirectChatActivity.this, 
                    "Failed to send message: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;
        sendMessage(text, null);
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
                    sendMessage("", imageUrls.get(0));
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