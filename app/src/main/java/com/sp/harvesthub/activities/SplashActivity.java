package com.sp.harvesthub.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.sp.harvesthub.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static int SPLASH_SCREEN = 5000; // 5 seconds
    private Animation topAnim, bottomAnim;
    private ImageView image;
    private TextView logo, logo2, slogan;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            System.out.println("SPLASH_DEBUG: Activity Creation Starting");
            android.util.Log.i("SPLASH_DEBUG", "Activity Creation Starting");

            super.onCreate(savedInstanceState);
            System.out.println("SPLASH_DEBUG: After super.onCreate");

            // Check if resources exist
            try {
                int logoId = R.drawable.baseline_food_bank_24;
                int musicId = R.raw.newaudio;
                System.out.println("SPLASH_DEBUG: Resources found - logo: " + logoId + ", music: " + musicId);
            } catch (Exception e) {
                System.out.println("SPLASH_DEBUG: Resource not found error: " + e.getMessage());
            }

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_splash);
            System.out.println("SPLASH_DEBUG: Layout set");

            // Initialize views with error checking
            image = findViewById(R.id.imageView);
            if (image == null) System.out.println("SPLASH_DEBUG: imageView is null!");

            logo2 = findViewById(R.id.textView2);
            if (logo2 == null) System.out.println("SPLASH_DEBUG: logo TextView is null!");

            logo = findViewById(R.id.textView3);
            if (logo == null) System.out.println("SPLASH_DEBUG: logo TextView is null!");

            slogan = findViewById(R.id.textView4);
            if (slogan == null) System.out.println("SPLASH_DEBUG: slogan TextView is null!");

            System.out.println("SPLASH_DEBUG: Views initialized");

            // Initialize animations and media
            initializeAnimationsAndMedia();
            System.out.println("SPLASH_DEBUG: Animations initialized");

            // Start timer
            startSplashTimer();
            System.out.println("SPLASH_DEBUG: Timer started");

        } catch (Exception e) {
            System.out.println("SPLASH_DEBUG: FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("SPLASH_DEBUG: onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("SPLASH_DEBUG: onResume called");
    }

    private void initializeAnimationsAndMedia() {
        // Initialize animations
        try {
            topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
            bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
            Log.d(TAG, "initializeAnimationsAndMedia: Animations loaded");
        } catch (Exception e) {
            Log.e(TAG, "initializeAnimationsAndMedia: Error loading animations", e);
        }

        // Set animations
        try {
            if (image != null) {
                image.setImageResource(R.drawable.baseline_food_bank_24);
                image.setAnimation(topAnim);
            }
            if (logo != null) logo.setAnimation(bottomAnim);
            if (logo2 != null) logo2.setAnimation(bottomAnim);
            if (slogan != null) slogan.setAnimation(bottomAnim);
            Log.d(TAG, "initializeAnimationsAndMedia: Animations set");
        } catch (Exception e) {
            Log.e(TAG, "initializeAnimationsAndMedia: Error setting animations", e);
        }

        // Initialize media player
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.newaudio);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.start();
                Log.d(TAG, "initializeAnimationsAndMedia: Media player started");
            } else {
                Log.e(TAG, "initializeAnimationsAndMedia: Failed to create media player");
            }
        } catch (Exception e) {
            Log.e(TAG, "initializeAnimationsAndMedia: Error with media player", e);
        }
    }

    private void startSplashTimer() {
        Log.d(TAG, "startSplashTimer: Starting timer for " + SPLASH_SCREEN + "ms");
        new Handler().postDelayed(() -> {
            Log.d(TAG, "startSplashTimer: Timer finished, starting LoginActivity");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            // Stop media player
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    Log.d(TAG, "startSplashTimer: Media player stopped and released");
                } catch (Exception e) {
                    Log.e(TAG, "startSplashTimer: Error stopping media player", e);
                }
            }

            // Create transition animation
            try {
                Pair[] pairs = new Pair[0];
                //pairs[0] = new Pair<View, String>(image, "logo_image");
                //pairs[1] = new Pair<View, String>(logo, "logo_text");
                //pairs[2] = new Pair<View, String>(logo2, "logo_text");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, pairs);
                startActivity(intent, options.toBundle());
                Log.d(TAG, "startSplashTimer: Started LoginActivity with transitions");
            } catch (Exception e) {
                Log.e(TAG, "startSplashTimer: Error starting LoginActivity", e);
                // Fallback to starting activity without transitions
                startActivity(intent);
            }
            finish();
        }, SPLASH_SCREEN);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Cleaning up resources");
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
