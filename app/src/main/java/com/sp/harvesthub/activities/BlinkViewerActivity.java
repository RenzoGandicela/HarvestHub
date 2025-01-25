package com.sp.harvesthub.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sp.harvesthub.R;

public class BlinkViewerActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blink_viewer);

        ImageView blinkFullImageView = findViewById(R.id.blinkFullImageView);
        ImageView userProfileImage = findViewById(R.id.userProfileImage);
        TextView usernameTxt = findViewById(R.id.usernameTxt);

        String imageUrl = getIntent().getStringExtra("imageURL");
        String username = getIntent().getStringExtra("username");

        Log.d("BlinkViewerActivity", "Loading image URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(blinkFullImageView);
            
            // Also load the small profile image
            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(userProfileImage);
        }

        usernameTxt.setText(username);

        // Close activity when clicked
        blinkFullImageView.setOnClickListener(v -> finish());
    }
} 