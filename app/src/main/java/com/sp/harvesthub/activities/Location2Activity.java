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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Location2Activity extends AppCompatActivity {

    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private DatabaseReference chatDatabase;
    private ValueEventListener chatListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location1);

        // Initialize Firebase Auth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Initialize Firebase Database Reference for chat room 2
        chatDatabase = FirebaseDatabase.getInstance()
                .getReference("chats").child("chat_room_2").child("messages");

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

        liveCameraView.setOnTouchListener((v, event) -> true);

        liveCameraView.setWebViewClient(new WebViewClient());
        liveCameraView.loadUrl("https://www.youtube.com/embed/another_stream_id?autoplay=1&controls=0&mute=1&modestbranding=1&disablekb=1&fs=0&showinfo=0&rel=0");

        // Chat ListView with custom layout for better visibility
        ListView chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, R.layout.chat_message_item, R.id.messageText, chatMessages);
        chatListView.setAdapter(chatAdapter);

        attachDatabaseListener();

        // Message Input and Send Button
        EditText messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty() && currentUser != null) {
                // Get current timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                
                // Create message object
                DatabaseReference newMessageRef = chatDatabase.push();
                newMessageRef.child("message").setValue(message);
                newMessageRef.child("senderId").setValue(currentUser.getUid());
                newMessageRef.child("username").setValue(currentUser.getEmail());
                newMessageRef.child("timestamp").setValue(System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> {
                        messageInput.setText("");
                        chatListView.smoothScrollToPosition(chatAdapter.getCount());
                    })
                    .addOnFailureListener(e -> Toast.makeText(Location2Activity.this, 
                        "Failed to send message", Toast.LENGTH_SHORT).show());
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
                        // Add messages to beginning of list to show newest first
                        chatMessages.add(formattedMessage);
                    }
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Location2Activity.this, 
                    "Failed to load messages: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };
        // Order messages by timestamp
        chatDatabase.orderByChild("timestamp").addValueEventListener(chatListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatDatabase != null && chatListener != null) {
            chatDatabase.removeEventListener(chatListener);
        }
    }
} 