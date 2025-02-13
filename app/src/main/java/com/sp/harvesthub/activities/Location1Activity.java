package com.sp.harvesthub.activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodListings.FoodAdapter;
import com.sp.harvesthub.foodListings.FoodItem;
import com.sp.harvesthub.foodListings.FoodItemExtended;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Location1Activity extends AppCompatActivity {

    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private DatabaseReference chatDatabase;
    private ValueEventListener chatListener;
    private FirebaseUser currentUser;
    private EditText messageInput;
    private ListView chatListView;
    
    // Add these for food listings
    private RecyclerView foodRecyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location1);

        // Initialize food listings
        foodList = new ArrayList<>();
        foodRecyclerView = findViewById(R.id.FridgeListingsRecyclerView);
        foodAdapter = new FoodAdapter(this, foodList);
        
        // Set up grid layout like in the main food listings
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        foodRecyclerView.setLayoutManager(layoutManager);
        foodRecyclerView.setAdapter(foodAdapter);

        // Fetch Bedok listings
        fetchBedokListings();

        // Set title for the location
        setTitle("Bedok Block 702 Community Fridge");

        // Initialize Firebase Auth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Initialize Firebase Database Reference for chat room 1 (Bedok Block 702)
        chatDatabase = FirebaseDatabase.getInstance()
                .getReference("chats").child("chat_room_1").child("messages");

        // Livestream WebView setup
        WebView liveCameraView = findViewById(R.id.liveCameraView);
        WebSettings webSettings = liveCameraView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);

        liveCameraView.setOnTouchListener((v, event) -> true);

        liveCameraView.setWebViewClient(new WebViewClient());
        // Update stream URL for Bedok Block 702 location
        liveCameraView.loadUrl("https://www.youtube.com/embed/mRuglupSUgI?autoplay=1&controls=0&mute=1&modestbranding=1&disablekb=1&fs=0&showinfo=0&rel=0");

        // Chat ListView with custom layout for better visibility
        chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, R.layout.chat_message_item, R.id.messageText, chatMessages);
        chatListView.setAdapter(chatAdapter);

        attachDatabaseListener();

        // Message Input and Send Button
        messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });
    }

    private void attachDatabaseListener() {
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String message = messageSnapshot.child("message").getValue(String.class);
                    String username = messageSnapshot.child("username").getValue(String.class);
                    Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                    
                    if (message != null && username != null && timestamp != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String time = sdf.format(new Date(timestamp));
                        String formattedMessage = username + " (" + time + "): " + message;
                        chatMessages.add(formattedMessage);
                    }
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Location1Activity.this, 
                    "Failed to load messages: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        // Order messages by timestamp
        chatDatabase.orderByChild("timestamp").addValueEventListener(chatListener);
    }

    private void sendMessage(String message) {
        if (!message.isEmpty() && currentUser != null) {
            // Get the current user's data to get the username
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(currentUser.getUid());
            
            userRef.get().addOnSuccessListener(snapshot -> {
                String username = snapshot.child("username").getValue(String.class);
                if (username == null) {
                    username = currentUser.getEmail(); // Fallback to email if username not found
                }

                // Create message object
                DatabaseReference newMessageRef = chatDatabase.push();
                newMessageRef.child("message").setValue(message);
                newMessageRef.child("senderId").setValue(currentUser.getUid());
                newMessageRef.child("username").setValue(username);
                newMessageRef.child("timestamp").setValue(System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> {
                        messageInput.setText("");
                        chatListView.smoothScrollToPosition(chatAdapter.getCount());
                    })
                    .addOnFailureListener(e -> Toast.makeText(Location1Activity.this, 
                        "Failed to send message", Toast.LENGTH_SHORT).show());
            });
        }
    }

    private void fetchBedokListings() {
        DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings");
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("Users");

        listingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    // Fetch username for this user
                    usersRef.child(userId).child("username").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String username = task.getResult().getValue(String.class);

                            for (DataSnapshot itemSnapshot : userSnapshot.child("items").getChildren()) {
                                try {
                                    String location = itemSnapshot.child("location").getValue(String.class);
                                    if (location != null && location.toLowerCase().contains("bedok")) {
                                        FoodItemExtended foodItem = new FoodItemExtended();
                                        foodItem.setItemId(itemSnapshot.getKey());
                                        foodItem.setOriginalSellerId(userId);
                                        foodItem.setSellerId(username != null ? username : userId);

                                        // Get all the necessary fields
                                        foodItem.setDishName(itemSnapshot.child("title").getValue(String.class));
                                        foodItem.setLocation(location);
                                        foodItem.setImageUrl(itemSnapshot.child("imageUrl").getValue(String.class));
                                        foodItem.setDescription(itemSnapshot.child("description").getValue(String.class));
                                        
                                        // Get status
                                        String status = itemSnapshot.child("status").getValue(String.class);
                                        foodItem.setStatus(status != null ? status : "available");
                                        
                                        // Get boolean values
                                        Boolean halal = itemSnapshot.child("halal").getValue(Boolean.class);
                                        Boolean spicy = itemSnapshot.child("spicy").getValue(Boolean.class);
                                        foodItem.setHalal(halal != null ? halal : false);
                                        foodItem.setSpicy(spicy != null ? spicy : false);

                                        // Only add if the item is available
                                        if ("available".equalsIgnoreCase(foodItem.getStatus())) {
                                            foodList.add(foodItem);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            foodAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Location1Activity.this, 
                    "Error loading food listings: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatDatabase != null && chatListener != null) {
            chatDatabase.removeEventListener(chatListener);
        }
    }
} 