package com.sp.harvesthub.foodListings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFoodActivity extends AppCompatActivity {
    private static final String TAG = "EditFoodActivity";

    private ImageView imageView;
    private TextView foodNameText;
    private EditText descEdit, ingredientsEdit, expirationDateEdit, locationEdit, quantityEdit;
    private Switch spicySwitch, halalSwitch, statusSwitch;
    private Button saveButton, deleteButton;
    private String itemId;
    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Food Listing");
        }

        // Initialize views
        initializeViews();

        // Get data from intent
        FoodItemExtended foodItem = (FoodItemExtended) getIntent().getSerializableExtra("foodItem");
        if (foodItem != null) {
            itemId = foodItem.getItemId();
            sellerId = foodItem.getOriginalSellerId();
            loadFoodData();
        }

        // Set up button listeners
        saveButton.setOnClickListener(v -> saveChanges());
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        foodNameText = findViewById(R.id.foodNameText);
        descEdit = findViewById(R.id.descEdit);
        ingredientsEdit = findViewById(R.id.ingredientsEdit);
        expirationDateEdit = findViewById(R.id.expirationDateEdit);
        locationEdit = findViewById(R.id.locationEdit);
        quantityEdit = findViewById(R.id.quantityEdit);
        spicySwitch = findViewById(R.id.switchSpicy);
        halalSwitch = findViewById(R.id.switchHalal);
        statusSwitch = findViewById(R.id.switchStatus);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Set up switch listeners
        spicySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                spicySwitch.setText(isChecked ? "True" : "False"));

        halalSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                halalSwitch.setText(isChecked ? "True" : "False"));

        statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                statusSwitch.setText(isChecked ? "Available" : "Claimed"));
    }

    private void loadFoodData() {
        DatabaseReference listingRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings")
                .child(sellerId)
                .child("items")
                .child(itemId);

        listingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get values
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    Object quantityObj = snapshot.child("quantity").getValue();
                    String quantity = (quantityObj instanceof Long) ?
                            String.valueOf(quantityObj) : (String) quantityObj;
                    String expiryDate = snapshot.child("expiryDate").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    Boolean isHalal = snapshot.child("halal").getValue(Boolean.class);
                    Boolean isSpicy = snapshot.child("spicy").getValue(Boolean.class);
                    String status = snapshot.child("status").getValue(String.class);
                    boolean isAvailable = !"claimed".equalsIgnoreCase(status);

                    // Update UI
                    foodNameText.setText("Food: " + title);
                    descEdit.setText(description);
                    locationEdit.setText(location);
                    quantityEdit.setText(quantity);

                    if (expiryDate != null) {
                        expirationDateEdit.setText(expiryDate.replace("T", " "));
                    }

                    // Handle ingredients
                    List<String> ingredients = new ArrayList<>();
                    DataSnapshot ingredientsSnapshot = snapshot.child("ingredients");
                    if (ingredientsSnapshot.exists()) {
                        for (DataSnapshot ingredient : ingredientsSnapshot.getChildren()) {
                            String value = ingredient.getValue(String.class);
                            if (value != null) {
                                ingredients.add(value);
                            }
                        }
                        ingredientsEdit.setText(String.join(", ", ingredients));
                    }

                    statusSwitch.setChecked(isAvailable);
                    statusSwitch.setText(isAvailable ? "Available" : "Claimed");

                    spicySwitch.setChecked(Boolean.TRUE.equals(isSpicy));
                    halalSwitch.setChecked(Boolean.TRUE.equals(isHalal));

                    // Load image
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(EditFoodActivity.this)
                                .load(imageUrl)
                                .into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditFoodActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", descEdit.getText().toString());
        updates.put("location", locationEdit.getText().toString());
        updates.put("quantity", quantityEdit.getText().toString());
        updates.put("expiryDate", expirationDateEdit.getText().toString().replace(" ", "T"));
        updates.put("halal", halalSwitch.isChecked());
        updates.put("spicy", spicySwitch.isChecked());
        updates.put("status", statusSwitch.isChecked() ? "available" : "claimed");

        String ingredientsText = ingredientsEdit.getText().toString();
        if (!ingredientsText.isEmpty()) {
            List<String> ingredients = new ArrayList<>();
            for (String ingredient : ingredientsText.split(",")) {
                ingredients.add(ingredient.trim());
            }
            updates.put("ingredients", ingredients);
        }

        // Update in Firebase
        DatabaseReference listingRef = FirebaseDatabase.getInstance()
                .getReference("listings")
                .child(sellerId)
                .child("items")
                .child(itemId);

        listingRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this listing?")
                .setPositiveButton("Delete", (dialog, which) -> deleteListing())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteListing() {
        DatabaseReference listingRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings")
                .child(sellerId)
                .child("items")
                .child(itemId);

        listingRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Listing deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete listing", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 