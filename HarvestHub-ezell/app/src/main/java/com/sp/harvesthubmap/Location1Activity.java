package com.sp.harvesthubmap;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class Location1Activity extends AppCompatActivity {

    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private DatabaseReference chatDatabase;
    private ValueEventListener chatListener;
    private RecyclerView fridgeListingsRecyclerView;
    private FoodAdapter fridgeListingsAdapter;
    private List<FoodItem> fridgeListings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location1);

        // Initialize RecyclerView for Bedok listings
        fridgeListingsRecyclerView = findViewById(R.id.FridgeListingsRecyclerView);
        fridgeListings = new ArrayList<>();
        fridgeListingsAdapter = new FoodAdapter(this, fridgeListings);
        fridgeListingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fridgeListingsRecyclerView.setAdapter(fridgeListingsAdapter);

        // Initialize Firebase and fetch Bedok listings
        fetchBedokListings();

        // Livestream WebView
        WebView liveCameraView = findViewById(R.id.liveCameraView);
        WebSettings webSettings = liveCameraView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);

        liveCameraView.setOnTouchListener((v, event) -> true); // Consume all touch events

        liveCameraView.setWebViewClient(new WebViewClient());
        liveCameraView.loadUrl("https://www.youtube.com/embed/20sA-bYT5fY?autoplay=1&controls=0&mute=1&modestbranding=1&disablekb=1&fs=0&showinfo=0&rel=0");


        // Chat ListView
        ListView chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        chatListView.setAdapter(chatAdapter);

        // Attach a listener to read messages from Firebase in real time
        attachDatabaseListener();

        // Message Input and Send Button
        EditText messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                // Save the message to Firebase
                chatDatabase.push().setValue("User: " + message);
                messageInput.setText(""); // Clear the input field
            }
        });
    }

    private void fetchBedokListings() {
        DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings");

        listingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fridgeListings.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : userSnapshot.child("items").getChildren()) {
                        String location = itemSnapshot.child("location").getValue(String.class);
                        if (location != null && location.toLowerCase().contains("bedok")) {
                            FoodItemExtended foodItem = new FoodItemExtended();
                            foodItem.setItemId(itemSnapshot.getKey());
                            foodItem.setDishName(itemSnapshot.child("title").getValue(String.class));
                            foodItem.setLocation(location);
                            foodItem.setImageUrl(itemSnapshot.child("imageUrl").getValue(String.class));
                            foodItem.setDescription(itemSnapshot.child("description").getValue(String.class));
                            
                            // Add other necessary fields
                            Boolean halal = itemSnapshot.child("halal").getValue(Boolean.class);
                            Boolean spicy = itemSnapshot.child("spicy").getValue(Boolean.class);
                            foodItem.setHalal(halal != null ? halal : false);
                            foodItem.setSpicy(spicy != null ? spicy : false);
                            
                            fridgeListings.add(foodItem);
                        }
                    }
                }
                fridgeListingsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    // Attach a listener to read messages from Firebase in real time
    private void attachDatabaseListener() {
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                runOnUiThread(() -> {
                    chatMessages.clear(); // Clear the local list before adding updated messages
                    for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                        String message = messageSnapshot.getValue(String.class);
                        if (message != null) {
                            chatMessages.add(0, message); // Add the message at the top
                        }
                    }
                    chatAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the ListView
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log or handle the error
            }
        };
        chatDatabase.addValueEventListener(chatListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener to prevent memory leaks
        if (chatDatabase != null && chatListener != null) {
            chatDatabase.removeEventListener(chatListener);
        }
    }
}
