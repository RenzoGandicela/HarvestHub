package com.sp.harvesthub.foodAPI;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.harvesthub.foodListings.FoodItemExtended;

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
            saveToListingsDatabase(foodItem);
        } else {
            Log.e(TAG, "No food item received");
            Toast.makeText(this, "Error: No food data received", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveToListingsDatabase(FoodItemExtended foodItem) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is signed in");
            Toast.makeText(this, "Error: No user is signed in", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        String sellerId = currentUser.getUid();
        
        // Create reference to user's items node
        DatabaseReference userItemsRef = listingsRef.child(sellerId).child("items");
        String key = userItemsRef.push().getKey();
        
        if (key != null) {
            // Get current timestamp in ISO 8601 format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String timestamp = sdf.format(new Date());

            // Create a Map to include all the food item data
            Map<String, Object> foodData = new HashMap<>();
            foodData.put("title", foodItem.getDishName());
            foodData.put("halal", foodItem.isHalal());
            foodData.put("spicy", foodItem.isSpicy());
            foodData.put("ingredients", foodItem.getIngredients());
            foodData.put("expiryDate", foodItem.getExpirationDate());
            foodData.put("location", foodItem.getLocation());
            foodData.put("quantity", foodItem.getQuantity());
            foodData.put("sellerId", sellerId);
            foodData.put("status", foodItem.isAvailable() ? "available" : "unavailable");
            foodData.put("createdAt", timestamp);
            foodData.put("updatedAt", timestamp);
            
            userItemsRef.child(key).setValue(foodData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Successfully saved to listings database");
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save to listings database", e);
                        setResult(RESULT_CANCELED);
                        finish();
                    });
        } else {
            Log.e(TAG, "Failed to generate key for new record");
            Toast.makeText(this, "Error: Could not create database entry", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}