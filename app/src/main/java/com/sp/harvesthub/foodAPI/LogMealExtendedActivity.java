package com.sp.harvesthub.foodAPI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodListings.FoodItemExtended;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class LogMealExtendedActivity extends AppCompatActivity {

    private static final String TAG = "LogMealExtendedActivity";
    private DatabaseReference listingsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings");

        FoodItemExtended foodItem = (FoodItemExtended) getIntent().getSerializableExtra("foodItem");
        if (foodItem != null) {
            saveToDatabase(foodItem);
        } else {
            Log.e(TAG, "No food item received");
            Toast.makeText(this, "Error: No food data received", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveToDatabase(FoodItemExtended foodItem) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String sellerId = user.getUid();
            DatabaseReference userItemsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                    .getReference("listings")
                    .child(sellerId)
                    .child("items");

            String key = userItemsRef.push().getKey();
            if (key != null) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        .format(new Date());

                Map<String, Object> foodData = new HashMap<>();
                foodData.put("title", foodItem.getDishName());
                foodData.put("description", foodItem.getDescription());
                foodData.put("ingredients", foodItem.getIngredients());
                foodData.put("location", foodItem.getLocation());
                foodData.put("quantity", foodItem.getQuantity());
                foodData.put("expiryDate", foodItem.getExpirationDate());
                foodData.put("imageUrl", foodItem.getImageUrl());
                foodData.put("halal", foodItem.isHalal());
                foodData.put("spicy", foodItem.isSpicy());
                foodData.put("likes", 0); // Initialize as integer
                foodData.put("sellerId", sellerId);
                foodData.put("status", foodItem.isAvailable() ? "available" : "unavailable");
                foodData.put("createdAt", timestamp);
                foodData.put("updatedAt", timestamp);

                userItemsRef.child(key).setValue(foodData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Successfully saved to listings database");
                            setResult(RESULT_OK);

                            // Send broadcast with explicit intent
                            Intent intent = new Intent("com.sp.harvesthub.SELECT_HOME");
                            intent.setPackage(getPackageName());
                            sendBroadcast(intent);

                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save to listings database", e);
                            setResult(RESULT_CANCELED);
                            finish();
                        });
            }
        }
    }
}