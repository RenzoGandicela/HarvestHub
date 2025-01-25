package com.sp.harvesthubmap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Location3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location3);

        // Chat Button
        Button chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(Location3Activity.this, chat.class);
            startActivity(intent);
        });
    }
}