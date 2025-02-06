package com.sp.harvesthub.foodAPI;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.harvesthub.foodListings.FoodItemExtended;

public class LogMealExtendedActivity extends AppCompatActivity {

    private DatabaseReference extendedFoodsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove setContentView if you don't have a specific layout for this activity

        extendedFoodsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("testing");

        FoodItemExtended foodItem = (FoodItemExtended) getIntent().getSerializableExtra("foodItem");
        if (foodItem != null) {
            saveToExtendedDatabase(foodItem);
        } else {
            Log.e("LogMealExtendedActivity", "No food item received");
            Toast.makeText(this, "Error: No food data received", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveToExtendedDatabase(FoodItemExtended foodItem) {
        String key = extendedFoodsRef.push().getKey();
        if (key != null) {
            extendedFoodsRef.child(key).setValue(foodItem)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("LogMealExtendedActivity", "Successfully saved to testing database");
                        Toast.makeText(this, "Food saved to extended database!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LogMealExtendedActivity", "Failed to save to testing database", e);
                        Toast.makeText(this, "Error saving to extended database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    });
        } else {
            Log.e("LogMealExtendedActivity", "Failed to generate key for new record");
            Toast.makeText(this, "Error: Could not create database entry", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}