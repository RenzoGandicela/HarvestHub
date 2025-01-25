package com.sp.harvesthubmap;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Location1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location1);

        WebView liveCameraView = findViewById(R.id.liveCameraView);
        WebSettings webSettings = liveCameraView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript

// Optimize WebView
        webSettings.setBuiltInZoomControls(false); // Disable zoom controls
        webSettings.setDisplayZoomControls(false); // Don't show zoom buttons
        webSettings.setLoadWithOverviewMode(true); // Load content to fit the screen
        webSettings.setUseWideViewPort(true); // Enable wide viewport
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // Avoid caching
        webSettings.setDomStorageEnabled(true); // Enable DOM storage

        liveCameraView.setOnTouchListener((v, event) -> true); // Consume all touch events


// Load the live stream URL with autoplay and hidden controls
        liveCameraView.setWebViewClient(new WebViewClient());
        liveCameraView.loadUrl("https://www.youtube.com/embed/ptc942KENIM?autoplay=1&controls=0&mute=1&modestbranding=1&disablekb=1 ");



        // Chat Button
        Button chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(Location1Activity.this, chat.class);
            startActivity(intent);
        });
    }
}