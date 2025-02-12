package com.sp.harvesthub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
<<<<<<< HEAD
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
=======
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
>>>>>>> renzo
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
<<<<<<< HEAD
import com.sp.harvesthub.adapters.ConversationsAdapter;
import com.sp.harvesthub.adapters.UsersAdapter;
import com.sp.harvesthub.models.ChatConversation;
import com.sp.harvesthub.models.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatsRecyclerView;
    private RecyclerView usersRecyclerView;
    private ConversationsAdapter conversationsAdapter;
    private UsersAdapter usersAdapter;
    private List<ChatConversation> conversations;
    private List<User> users;
    private EditText searchInput;
    private EditText userSearchInput;
    private TextView messagesCountText;
    private LinearLayout searchLayout;
    private LinearLayout mainLayout;
    private DatabaseReference directMessagesRef;
    private String currentUserId;
    private ValueEventListener conversationsListener;
=======
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
>>>>>>> renzo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        
        // Make activity full screen and hide system bars
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        View decorView = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(flags);
        
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        directMessagesRef = FirebaseDatabase.getInstance().getReference("directMessages");

        // Initialize views
        initializeViews();
        
        // Set up RecyclerViews
        setupRecyclerViews();
        
        // Load conversations
        loadConversations();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(flags);
        }
    }

    private void initializeViews() {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        userSearchInput = findViewById(R.id.userSearchInput);
        messagesCountText = findViewById(R.id.messagesCountText);
        searchLayout = findViewById(R.id.searchLayout);
        mainLayout = findViewById(R.id.mainLayout);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton addFriendButton = findViewById(R.id.addFriendButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up conversation search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterConversations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up user search
        userSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up search button
        searchButton.setOnClickListener(v -> {
            if (searchLayout.getVisibility() == View.VISIBLE) {
                searchLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            } else {
                searchLayout.setVisibility(View.VISIBLE);
                mainLayout.setVisibility(View.GONE);
                loadUsers();
            }
        });

        // Set up add friend button
        addFriendButton.setOnClickListener(v -> {
            searchLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
            loadUsers();
        });
    }

    private void setupRecyclerViews() {
        // Set up conversations RecyclerView
        conversations = new ArrayList<>();
        conversationsAdapter = new ConversationsAdapter(this, conversations);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(conversationsAdapter);

        // Set up users RecyclerView
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users, user -> {
            // Create or open conversation when user is clicked
            createOrOpenConversation(user);
        });
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(usersAdapter);
    }

    private void loadConversations() {
        DatabaseReference directMessagesRef = FirebaseDatabase.getInstance().getReference("directMessages");
        conversationsListener = directMessagesRef
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    conversations.clear();
                    int newMessages = 0;

                    for (DataSnapshot conversationSnap : snapshot.getChildren()) {
                        try {
                            // Get last message details
                            DataSnapshot lastMessageSnap = conversationSnap.child("lastMessage");
                            String content = lastMessageSnap.child("content").getValue(String.class);
                            String senderId = lastMessageSnap.child("senderId").getValue(String.class);
                            Long timestamp = lastMessageSnap.child("timestamp").getValue(Long.class);

                            // Get participants
                            DataSnapshot participantsSnap = conversationSnap.child("participants");
                            String otherUserId = null;
                            String otherUsername = null;
                            String otherProfilePic = null;

                            for (DataSnapshot participantSnap : participantsSnap.getChildren()) {
                                String participantId = participantSnap.child("id").getValue(String.class);
                                if (!participantId.equals(currentUserId)) {
                                    otherUserId = participantId;
                                    otherUsername = participantSnap.child("username").getValue(String.class);
                                    otherProfilePic = participantSnap.child("profilePicture").getValue(String.class);
                                    break;
                                }
                            }

                            if (otherUserId != null && timestamp != null) {
                                ChatConversation conversation = new ChatConversation(
                                    conversationSnap.getKey(),
                                    otherUserId,
                                    otherUsername,
                                    otherProfilePic,
                                    content,
                                    timestamp
                                );
                                conversations.add(conversation);
                            }
                        } catch (Exception e) {
                            Log.e("ChatActivity", "Error parsing conversation: " + e.getMessage());
                        }
                    }

                    // Sort conversations by timestamp
                    Collections.sort(conversations, 
                        (a, b) -> Long.compare(b.getLastMessageTimestamp(), a.getLastMessageTimestamp()));
                    conversationsAdapter.notifyDataSetChanged();

                    messagesCountText.setText(String.format("You have %d new messages", newMessages));
=======
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
>>>>>>> renzo
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
<<<<<<< HEAD
                    Toast.makeText(ChatActivity.this, 
                        "Error loading conversations: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
=======
                    // Handle error...
>>>>>>> renzo
                }
            });
    }

<<<<<<< HEAD
    private void loadUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    if (!userId.equals(currentUserId)) {
                        String username = userSnap.child("username").getValue(String.class);
                        String profilePic = userSnap.child("profilePicture").getValue(String.class);
                        User user = new User(userId, username, profilePic);
                        users.add(user);
                    }
                }
                usersAdapter.notifyDataSetChanged();
=======
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
>>>>>>> renzo
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
<<<<<<< HEAD
                Toast.makeText(ChatActivity.this, 
                    "Error loading users: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
=======
                Log.e("ChatActivity", "Error loading messages: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
>>>>>>> renzo
            }
        });
    }

<<<<<<< HEAD
    private void searchUsers(String query) {
        if (query.isEmpty()) {
            loadUsers();
            return;
        }

        List<User> filteredList = new ArrayList<>();
        for (User user : users) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        usersAdapter.updateUsers(filteredList);
    }

    private void filterConversations(String query) {
        if (query.isEmpty()) {
            loadConversations();
            return;
        }

        List<ChatConversation> filteredList = new ArrayList<>();
        for (ChatConversation conversation : conversations) {
            if (conversation.getOtherUsername().toLowerCase()
                    .contains(query.toLowerCase())) {
                filteredList.add(conversation);
            }
        }
        conversationsAdapter.updateConversations(filteredList);
    }

    private void createOrOpenConversation(User user) {
        // Check if conversation already exists
        directMessagesRef.child(currentUserId)
            .orderByChild("otherUserId")
            .equalTo(user.getUserId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Conversation exists, open it
                        for (DataSnapshot convSnap : snapshot.getChildren()) {
                            String conversationId = convSnap.getKey();
                            openDirectChat(conversationId, user.getUserId(), user.getUsername());
                            break;
                        }
                    } else {
                        // Create new conversation
                        createNewConversation(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChatActivity.this, 
                        "Error checking conversation: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void createNewConversation(User otherUser) {
        String conversationId = directMessagesRef.push().getKey();
        if (conversationId == null) return;

        // Create conversation for current user
        DatabaseReference currentUserConvRef = directMessagesRef
            .child(currentUserId)
            .child(conversationId);

        // Create conversation for other user
        DatabaseReference otherUserConvRef = directMessagesRef
            .child(otherUser.getUserId())
            .child(conversationId);

        // Initial conversation data
        long timestamp = System.currentTimeMillis();
        String initialMessage = "Say hi to your new friend!";

        currentUserConvRef.setValue(new ChatConversation(
            conversationId, otherUser.getUserId(), otherUser.getUsername(), 
            otherUser.getProfilePicture(), initialMessage, timestamp
        )).addOnSuccessListener(aVoid -> {
            // Get current user's username
            FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId)
                .child("username")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String currentUsername = snapshot.getValue(String.class);
                    otherUserConvRef.setValue(new ChatConversation(
                        conversationId, currentUserId, currentUsername, null, initialMessage, timestamp
                    ));
                    openDirectChat(conversationId, otherUser.getUserId(), otherUser.getUsername());
                });
        });
    }

    private void openDirectChat(String conversationId, String otherUserId, String otherUsername) {
        Intent intent = new Intent(this, DirectChatActivity.class);
        intent.putExtra("conversationId", conversationId);
        intent.putExtra("otherUserId", otherUserId);
        intent.putExtra("otherUsername", otherUsername);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (directMessagesRef != null && conversationsListener != null) {
            directMessagesRef.removeEventListener(conversationsListener);
        }
    }
=======
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
>>>>>>> renzo
} 