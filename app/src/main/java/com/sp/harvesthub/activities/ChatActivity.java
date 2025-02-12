package com.sp.harvesthub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChatActivity.this, 
                        "Error loading conversations: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, 
                    "Error loading users: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

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
} 