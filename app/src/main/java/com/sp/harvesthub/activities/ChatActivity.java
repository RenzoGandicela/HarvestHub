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

public class ChatActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_chat);

        serverId = getIntent().getStringExtra("serverId");
        channelId = getIntent().getStringExtra("channelId");
        String channelName = getIntent().getStringExtra("channelName");

        // Check if we have the required data
        if (serverId == null || channelId == null) {
            Toast.makeText(this, "Error: Missing channel information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(channelName != null ? channelName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton attachButton = findViewById(R.id.attachButton);

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesList = new ArrayList<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageAdapter = new MessageAdapter(this, messagesList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        databaseRef = FirebaseDatabase.getInstance().getReference()
            .child("Servers").child(serverId)
            .child("channels").child(channelId)
            .child("messages");
        storageRef = FirebaseStorage.getInstance().getReference()
            .child("chat_images");

        sendButton.setOnClickListener(v -> sendMessage(null));
        attachButton.setOnClickListener(v -> selectImages());

        loadMessages();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_clear_chat) {
            showClearChatDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(List<String> imageUrls) {
        String messageText = messageInput.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Debug log
        Log.d("ChatActivity", "Sending message with user ID: " + userId);
        
        // Get the actual username from Firebase Database
        FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    Log.d("ChatActivity", "Username from Firebase: " + username); // Debug log
                    
                    if (username == null || username.isEmpty()) {
                        username = "User " + userId.substring(0, 4);
                    }
                    
                    if (!messageText.isEmpty() || (imageUrls != null && !imageUrls.isEmpty())) {
                        ChatMessage chatMessage = new ChatMessage(userId, username, messageText, imageUrls);
                        databaseRef.push().setValue(chatMessage)
                            .addOnSuccessListener(aVoid -> {
                                messageInput.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChatActivity.this, 
                                    "Failed to send message: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error...
                }
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
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    try {
                        ChatMessage chatMessage = messageSnapshot.getValue(ChatMessage.class);
                        if (chatMessage != null) {
                            // Debug log
                            Log.d("ChatActivity", "Loading message from: " + chatMessage.getSenderId());
                            
                            String imageUrl = chatMessage.getImageUrls() != null && !chatMessage.getImageUrls().isEmpty() 
                                ? chatMessage.getImageUrls().get(0)
                                : null;
                                
                            Message message = new Message(
                                chatMessage.getMessage(),
                                imageUrl,
                                chatMessage.getSenderId().trim(), // Ensure no whitespace
                                chatMessage.getUsername(),
                                null,
                                chatMessage.getTimestamp()
                            );
                            messagesList.add(message);
                        }
                    } catch (Exception e) {
                        Log.e("ChatActivity", "Error loading message: " + e.getMessage());
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messagesList.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatActivity", "Error loading messages: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
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
            // Show progress to user
            Toast.makeText(this, "Preparing image for upload...", Toast.LENGTH_SHORT).show();

            try {
                // Convert Uri to File
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                File file = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
                copyInputStreamToFile(inputStream, file);

                // Create Retrofit instance
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.imgur.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

                ImgurService service = retrofit.create(ImgurService.class);

                // Prepare the image file for upload
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

                // Show upload started
                Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

                // Make API call
                service.uploadImage(body).enqueue(new Callback<ImageResponse>() {
                    @Override
                    public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                            String imageUrl = response.body().data.link;
                            // Send message with image URL
                            sendMessage(List.of(imageUrl));
                            // Clean up the temp file
                            file.delete();
                        } else {
                            Toast.makeText(ChatActivity.this, 
                                "Upload failed: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageResponse> call, Throwable t) {
                        Toast.makeText(ChatActivity.this, 
                            "Upload failed: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                        // Clean up the temp file
                        file.delete();
                    }
                });

            } catch (IOException e) {
                Toast.makeText(this, 
                    "Error preparing image: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
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
                    Toast.makeText(ChatActivity.this, 
                        "Images uploaded successfully!", Toast.LENGTH_SHORT).show();
                    // Send message with images
                    sendMessage(imageUrls);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    // Show error toast
                    Toast.makeText(ChatActivity.this, 
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    private void showClearChatDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Chat")
            .setMessage("Are you sure you want to clear all messages? This action cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> clearChat())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void clearChat() {
        databaseRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Chat cleared successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to clear chat: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
} 