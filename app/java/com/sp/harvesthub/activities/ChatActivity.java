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
        messageAdapter = new MessageAdapter(this, messagesList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        databaseRef = FirebaseDatabase.getInstance().getReference()
            .child("Servers").child(serverId)
            .child("channels").child(channelId)
            .child("messages");
        storageRef = FirebaseStorage.getInstance().getReference()
            .child("chat_images");

        sendButton.setOnClickListener(v -> sendMessage());
        attachButton.setOnClickListener(v -> openImagePicker());

        loadMessages();
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (!text.isEmpty()) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String senderId = auth.getCurrentUser() != null ? 
                auth.getCurrentUser().getUid() : "sample_user";
            
            // Create message data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("text", text);
            messageData.put("imageURL", "");  // Empty string instead of null
            messageData.put("senderId", senderId);
            messageData.put("senderName", "User " + senderId.substring(0, 4));  // Temporary name
            messageData.put("senderProfilePic", "");  // Empty string instead of null
            messageData.put("timestamp", System.currentTimeMillis());
            
            // Get reference to messages in this channel
            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("Servers")
                .child(serverId)
                .child("channels")
                .child(channelId)
                .child("messages");
            
            // Push new message
            DatabaseReference newMessageRef = messagesRef.push();
            newMessageRef.setValue(messageData)
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    Log.d("ChatActivity", "Message sent successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Failed to send message: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, 
                        "Failed to send message: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void loadMessages() {
        DatabaseReference messagesRef = FirebaseHelper.getMessagesRef(serverId, channelId);
        messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    try {
                        String text = messageSnapshot.child("text").getValue(String.class);
                        String imageURL = messageSnapshot.child("imageURL").getValue(String.class);
                        String senderId = messageSnapshot.child("senderId").getValue(String.class);
                        String senderName = messageSnapshot.child("senderName").getValue(String.class);
                        String senderProfilePic = messageSnapshot.child("senderProfilePic").getValue(String.class);
                        Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                        if (timestamp == null) {
                            timestamp = System.currentTimeMillis();
                        }

                        Message message = new Message(text, imageURL, senderId, senderName, senderProfilePic, timestamp);
                        messagesList.add(message);
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
                            sendMessageWithImage(imageUrl);
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

    private void sendMessageWithImage(String imageUrl) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String senderId = auth.getCurrentUser() != null ? 
            auth.getCurrentUser().getUid() : "sample_user";
        
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", "");
        messageData.put("imageURL", imageUrl);
        messageData.put("senderId", senderId);
        messageData.put("senderName", "User " + senderId.substring(0, 4));
        messageData.put("senderProfilePic", "");
        messageData.put("timestamp", System.currentTimeMillis());
        
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
            .child("Servers")
            .child(serverId)
            .child("channels")
            .child(channelId)
            .child("messages");
        
        messagesRef.push().setValue(messageData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(ChatActivity.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, 
                    "Failed to send image: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
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
} 