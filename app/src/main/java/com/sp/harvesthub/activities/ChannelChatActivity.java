package com.sp.harvesthub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sp.harvesthub.R;
import com.sp.harvesthub.adapters.MessageAdapter;
import com.sp.harvesthub.models.Message;
import com.sp.harvesthub.utils.FirebaseHelper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.sp.harvesthub.utils.ImgurService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.sp.harvesthub.utils.ImageResponse;
import android.view.MenuItem;
import com.sp.harvesthub.models.ChatMessage;
import com.sp.harvesthub.utils.FirebaseImageUploader;
import android.view.Menu;
import android.view.MenuInflater;
import androidx.appcompat.app.AlertDialog;

public class ChannelChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messagesList;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private EditText messageInput;
    private String channelId;
    private String serverId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGES_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_chat);

        serverId = getIntent().getStringExtra("serverId");
        channelId = getIntent().getStringExtra("channelId");
        String channelName = getIntent().getStringExtra("channelName");

        // Check if we have the required data
        if (serverId == null || channelId == null) {
            Toast.makeText(this, "Error: Missing channel information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(channelName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton attachButton = findViewById(R.id.attachButton);

        // Set up RecyclerView
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesList = new ArrayList<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageAdapter = new MessageAdapter(this, messagesList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Initialize Firebase references
        databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Servers").child(serverId)
                .child("channels").child(channelId)
                .child("messages");
        storageRef = FirebaseStorage.getInstance().getReference()
                .child("chat_images");

        // Set up click listeners
        sendButton.setOnClickListener(v -> sendMessage(null));
        attachButton.setOnClickListener(v -> selectImages());

        // Load existing messages
        loadMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_clear_chat) {
            showClearChatDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(List<String> imageUrls) {
        String content = messageInput.getText().toString().trim();
        if ((content.isEmpty() && (imageUrls == null || imageUrls.isEmpty()))) {
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        userRef.get().addOnSuccessListener(snapshot -> {
            String username = snapshot.child("username").getValue(String.class);
            if (username == null) {
                username = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }

            // Create message data with all required fields
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("senderId", currentUserId);
            messageData.put("timestamp", System.currentTimeMillis());
            messageData.put("username", username);

            // If there are images, set the message type and imageUrl
            if (imageUrls != null && !imageUrls.isEmpty()) {
                messageData.put("messageType", "image");
                messageData.put("imageUrl", imageUrls.get(0)); // Store single image URL directly
                messageData.put("content", ""); // Empty content for image messages
            } else {
                messageData.put("messageType", "text");
                messageData.put("content", content);
            }

            // Push the message
            DatabaseReference newMessageRef = databaseRef.push();
            newMessageRef.setValue(messageData)
                    .addOnSuccessListener(aVoid -> {
                        messageInput.setText("");
                        messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ChannelChatActivity.this,
                                    "Failed to send message: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }

    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }

    private void loadMessages() {
        databaseRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot messageSnap : snapshot.getChildren()) {
                    try {
                        String senderId = messageSnap.child("senderId").getValue(String.class);
                        String username = messageSnap.child("username").getValue(String.class);
                        Long timestamp = messageSnap.child("timestamp").getValue(Long.class);
                        String messageType = messageSnap.child("messageType").getValue(String.class);

                        Message message;
                        if ("image".equals(messageType)) {
                            String imageUrl = messageSnap.child("imageUrl").getValue(String.class);
                            message = new Message("", imageUrl, senderId, username, null, timestamp);
                        } else {
                            String content = messageSnap.child("content").getValue(String.class);
                            message = new Message(content, null, senderId, username, null, timestamp);
                        }

                        messagesList.add(message);
                    } catch (Exception e) {
                        Log.e("ChatActivity", "Error parsing message: " + e.getMessage());
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messagesList.isEmpty()) {
                    messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChannelChatActivity.this,
                        "Failed to load messages: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
        } else if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            List<Uri> imageUris = new ArrayList<>();

            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                // Single image selected
                imageUris.add(data.getData());
            }

            if (!imageUris.isEmpty()) {
                uploadImages(imageUris);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

            // Create a reference to chat_images folder
            StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("chat_images")
                    .child(System.currentTimeMillis() + "-" + imageUri.getLastPathSegment());

            fileRef.putFile(imageUri)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        // You could add a progress indicator here if desired
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            // Send message with the image URL
                            sendMessage(List.of(downloadUri.toString()));
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChannelChatActivity.this,
                                "Failed to upload image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadImages(List<Uri> imageUris) {
        // Show upload started toast
        Toast.makeText(this, "Uploading images...", Toast.LENGTH_SHORT).show();

        FirebaseImageUploader.uploadImages(imageUris, new FirebaseImageUploader.OnUploadCompleteListener() {
            @Override
            public void onComplete(List<String> imageUrls) {
                runOnUiThread(() -> {
                    // Show success toast
                    Toast.makeText(ChannelChatActivity.this,
                            "Images uploaded successfully!", Toast.LENGTH_SHORT).show();
                    // Send message with images
                    sendMessage(imageUrls);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    // Show error toast
                    Toast.makeText(ChannelChatActivity.this,
                            "Failed to upload images: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void copyInputStreamToFile(InputStream in, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    private void showClearChatDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to delete all messages? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> clearChat())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearChat() {
        databaseRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "All messages deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to delete messages: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}