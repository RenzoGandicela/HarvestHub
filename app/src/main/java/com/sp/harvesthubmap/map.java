package com.sp.harvesthubmap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class map extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button openMapButton = findViewById(R.id.openMapButton);

        openMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(map.this, googleMaps.class);
            startActivity(intent);
        });

    }
}