package com.sp.harvesthub.nav_fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodAPI.ApiCallback;
import com.sp.harvesthub.foodAPI.FileUtil;
import com.sp.harvesthub.foodAPI.LogMealService;
import com.sp.harvesthub.foodListings.FoodItem;
import com.sp.harvesthub.foodListings.FoodItemExtended;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LogMealFragment extends Fragment {
    private static final String TAG = "LogMealFragment";
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;
    private static final int EXTENDED_ACTIVITY_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private LogMealService logMealService;
    private ImageView imageView;
    private TextView foodNameText, ingredientsText, halalText, spicyText;
    private EditText expirationDateEdit, locationEdit, quantityEdit;
    private Button uploadButton, analyzeButton, camButton, saveButton, deleteButton;
    private File selectedImageFile;
    private String currentPhotoPath;
    private Uri photoURI;
    private DatabaseReference foodsRef;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private FoodItemExtended editFoodItem;
    private boolean isEditMode = false;
    private String currentListingId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        logMealService = new LogMealService();
        foodsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/").getReference("foods");
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        if (getArguments() != null) {
            editFoodItem = (FoodItemExtended) getArguments().getSerializable("editFoodItem");
            isEditMode = (editFoodItem != null);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_logmeal, container, false);
        
        // Initialize views and services
        logMealService = new LogMealService();
        imageView = view.findViewById(R.id.imageView);
        foodNameText = view.findViewById(R.id.foodNameText);
        ingredientsText = view.findViewById(R.id.ingredientsText);
        halalText = view.findViewById(R.id.halalText);
        spicyText = view.findViewById(R.id.spicyText);
        Button uploadButton = view.findViewById(R.id.uploadButton);
        Button analyzeButton = view.findViewById(R.id.analyzeButton);
        Button camButton = view.findViewById(R.id.camButton);
        
        expirationDateEdit = view.findViewById(R.id.expirationDateEdit);
        locationEdit = view.findViewById(R.id.locationEdit);
        quantityEdit = view.findViewById(R.id.quantityEdit);

        // Initialize switches once
        Switch spicySwitch = view.findViewById(R.id.switchSpicy);
        Switch halalSwitch = view.findViewById(R.id.switchHalal);

        spicySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spicySwitch.setText(isChecked ? " True" : " False");
        });

        halalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            halalSwitch.setText(isChecked ? " True" : " False");
        });

        // Set click listeners
        uploadButton.setOnClickListener(v -> openFilePicker());
        analyzeButton.setOnClickListener(v -> {
            if (selectedImageFile != null) {
                Log.d(TAG, "Analyzing image...");
                analyzeImage(selectedImageFile);
            } else {
                Toast.makeText(requireContext(), "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
            }
        });

        camButton.setOnClickListener(v -> {
            String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            if (hasPermissions(permissions)) {
                dispatchTakePictureIntent();
            } else {
                requestPermissions(permissions, CAMERA_REQUEST_CODE);
            }
        });

        saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveListingData());

        deleteButton = view.findViewById(R.id.deleteButton);
        
        if (isEditMode && editFoodItem != null) {
            Log.d(TAG, "Edit mode activated for item: " + editFoodItem.getItemId());
            
            // Show delete button
            deleteButton.setVisibility(View.VISIBLE);
            currentListingId = editFoodItem.getItemId();
            
            // Set up delete button click listener
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
            
            // Fetch and populate data from Firebase
            String userId = editFoodItem.getOriginalSellerId(); // Use the original seller ID
            DatabaseReference listingRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                    .getReference("listings")
                    .child(userId)
                    .child("items")
                    .child(currentListingId);

            Log.d(TAG, "Fetching data from: " + listingRef.toString());

            listingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d(TAG, "Snapshot data: " + snapshot.getValue());
                        
                        // Get values with proper type handling
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

                        // Get ingredients
                        List<String> ingredients = new ArrayList<>();
                        DataSnapshot ingredientsSnapshot = snapshot.child("ingredients");
                        if (ingredientsSnapshot.exists()) {
                            for (DataSnapshot ingredient : ingredientsSnapshot.getChildren()) {
                                String value = ingredient.getValue(String.class);
                                if (value != null) {
                                    ingredients.add(value);
                                }
                            }
                        }

                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            foodNameText.setText("Detected Food: " + title);
                            EditText descEdit = view.findViewById(R.id.descEdit);
                            EditText ingredientsEdit = view.findViewById(R.id.ingredientsEdit);
                            
                            descEdit.setText(description);
                            locationEdit.setText(location);
                            quantityEdit.setText(quantity);
                            
                            if (expiryDate != null) {
                                expirationDateEdit.setText(expiryDate.replace("T", " "));
                            }
                            
                            if (!ingredients.isEmpty()) {
                                ingredientsEdit.setText(String.join(", ", ingredients));
                            }
                            
                            Switch spicySwitch = view.findViewById(R.id.switchSpicy);
                            Switch halalSwitch = view.findViewById(R.id.switchHalal);
                            spicySwitch.setChecked(isSpicy != null && isSpicy);
                            halalSwitch.setChecked(isHalal != null && isHalal);
                            
                            // Load image
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(requireContext())
                                    .load(imageUrl)
                                    .into(imageView);
                            }
                        });
                    } else {
                        Log.e(TAG, "No data found for listing ID: " + currentListingId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching listing data: " + error.getMessage());
                }
            });
        } else {
            // Hide delete button in create mode
            deleteButton.setVisibility(View.GONE);
        }

        return view;
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            return;
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(requireContext(), 
                "com.sp.harvesthub.fileprovider", photoFile);
            currentPhotoPath = photoFile.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                imageView.setImageURI(imageUri);
                try {
                    selectedImageFile = FileUtil.from(requireContext(), imageUri);
                    Log.d(TAG, "Image selected: " + selectedImageFile.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load image: " + e.getMessage(), e);
                    foodNameText.setText("Failed to load image.");
                    Toast.makeText(requireContext(), "Error loading image.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    File file = new File(currentPhotoPath);
                    if (file.exists()) {
                        selectedImageFile = file;
                        // Scale down the image to prevent OutOfMemoryError
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath, options));
                        Log.d(TAG, "Camera image loaded successfully");
                    } else {
                        Log.e(TAG, "Image file does not exist: " + currentPhotoPath);
                        Toast.makeText(requireContext(), "Error loading captured image", 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing camera result: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error processing captured image", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
    );

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
        Log.d(TAG, "Opened file picker.");
    }

    private void analyzeImage(File imageFile) {
        if (logMealService != null) {
            logMealService.analyzeImage(imageFile, new ApiCallback() {
                @Override
                public void onSuccess(String result) {
                    requireActivity().runOnUiThread(() -> {
                        foodNameText.setText("Detected Food: " + extractDishName(result));
                    });
                }

                @Override
                public void onFailure(String error) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error analyzing image: " + error, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }

    private String extractDishName(String result) {
        try {
            // The result contains "Detected Food:" followed by the first food item
            String[] lines = result.split("\n");
            if (lines.length >= 2) {
                // Get the first food item line and extract just the name (before the probability)
                String foodLine = lines[1].substring(3); // Skip the "1. " prefix
                int probIndex = foodLine.indexOf(" (");
                if (probIndex > 0) {
                    return foodLine.substring(0, probIndex).trim();
                }
                return foodLine.trim();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting dish name: " + e.getMessage());
        }
        return "Unknown Food";
    }

    private void saveFoodData(FoodItem foodItem) {
        // Only save if the food doesn't exist
        foodsRef.child(foodItem.getDishName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create map with only required fields
                    Map<String, Object> foodData = new HashMap<>();
                    foodData.put("dishName", foodItem.getDishName());
                    foodData.put("ingredients", foodItem.getIngredients());
                    foodData.put("halal", foodItem.isHalal());
                    foodData.put("spicy", foodItem.isSpicy());

                    foodsRef.child(foodItem.getDishName()).setValue(foodData)
                        .addOnSuccessListener(aVoid -> {
                            if (selectedImageFile != null) {
                                Uri fileUri = Uri.fromFile(selectedImageFile);
                                uploadImageToFirebase(fileUri, foodItem.getDishName());
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), 
                            "Failed to save basic food data", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error checking food existence: " + error.getMessage());
            }
        });
    }

    private void uploadImageToFirebase(Uri fileUri, String dishName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = dishName.replaceAll("\\s+", "_") + "_" + timeStamp + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("food_images/" + imageName);

        imageRef.putFile(fileUri)
            .addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updateFoodRecordWithImage(dishName, imageUrl);
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                Toast.makeText(getContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show();
            });
    }

    private void updateFoodRecordWithImage(String dishName, String imageUrl) {
        foodsRef.child(dishName).child("imageURL").setValue(imageUrl)
            .addOnSuccessListener(aVoid ->
                Log.d(TAG, "Image URL added to foods database for " + dishName + ": " + imageUrl))
            .addOnFailureListener(e ->
                Log.e(TAG, "Failed to add image URL to foods database: " + e.getMessage()));
        // Now update the listings node
        updateListingRecordWithImage(dishName, imageUrl);
    }

    private void updateListingRecordWithImage(String dishName, String imageUrl) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings")
                .child(userId)
                .child("items");
        // Query for records where 'title' matches the dish name
        listingsRef.orderByChild("title").equalTo(dishName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            childSnapshot.getRef().child("imageUrl").setValue(imageUrl)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Image URL added to listings database for " + dishName + ": " + imageUrl))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Failed to add image URL to listings database: " + e.getMessage(), e));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error querying listings for image update: " + error.getMessage());
                    }
                });
    }

    private void saveListingData() {
        // Get the food name without the "Detected Food: " prefix
        String foodNameText = this.foodNameText.getText().toString();
        String dishName = foodNameText.equals("Detected Food: -") ? 
            "" : foodNameText.replace("Detected Food: ", "").trim();

        // Validate required fields
        if (dishName.isEmpty()) {
            Toast.makeText(getContext(), "Please analyze an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        String expiryDate = expirationDateEdit.getText().toString();
        String location = locationEdit.getText().toString();
        String quantity = quantityEdit.getText().toString();
        EditText descEdit = requireView().findViewById(R.id.descEdit);
        String description = descEdit.getText().toString();
        EditText ingredientsEdit = requireView().findViewById(R.id.ingredientsEdit);
        String ingredientsText = ingredientsEdit.getText().toString();

        if (expiryDate.isEmpty() || location.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDateFormat(expiryDate)) {
            Toast.makeText(getContext(), "Please use the format YYYY-MM-DD HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create FoodItemExtended object
        FoodItemExtended foodItem = new FoodItemExtended();
        foodItem.setDishName(dishName);
        foodItem.setExpirationDate(expiryDate);
        foodItem.setLocation(location);
        foodItem.setQuantity(quantity);
        foodItem.setDescription(description);

        // Handle ingredients
        List<String> ingredients = new ArrayList<>();
        if (!ingredientsText.isEmpty()) {
            ingredients = Arrays.asList(ingredientsText.split(",\\s*"));
        }
        foodItem.setIngredients(ingredients);

        // Get switch states
        Switch spicySwitch = requireView().findViewById(R.id.switchSpicy);
        Switch halalSwitch = requireView().findViewById(R.id.switchHalal);
        foodItem.setSpicy(spicySwitch.isChecked());
        foodItem.setHalal(halalSwitch.isChecked());

        // If in edit mode, preserve the original item ID
        if (isEditMode && editFoodItem != null) {
            foodItem.setItemId(editFoodItem.getItemId());
        }

        // If there's an image, upload it first
        if (selectedImageFile != null) {
            Uri fileUri = Uri.fromFile(selectedImageFile);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = foodItem.getDishName().replaceAll("\\s+", "_") + "_" + timeStamp + ".jpg";
            StorageReference imageRef = storageRef.child("food_images/" + imageName);

            imageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        foodItem.setImageUrl(uri.toString());
                        saveToExtendedDatabase(foodItem);
                        
                        // Add this after successful save
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                            clearForm();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        } else if (isEditMode && editFoodItem != null) {
            // If no new image is selected in edit mode, keep the existing image URL
            foodItem.setImageUrl(editFoodItem.getImageUrl());
            saveToExtendedDatabase(foodItem);
        } else {
            // Save without image
            saveToExtendedDatabase(foodItem);
        }
    }

    private void saveToExtendedDatabase(FoodItemExtended foodItem) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings")
                .child(userId)
                .child("items");

        // Format the date to match website format
        String formattedDate = expirationDateEdit.getText().toString().replace(" ", "T");

        // Create a map with all the required fields
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("title", foodItem.getDishName());
        foodData.put("description", foodItem.getDescription());
        foodData.put("imageUrl", foodItem.getImageUrl());
        foodData.put("ingredients", foodItem.getIngredients());
        foodData.put("expiryDate", formattedDate);
        foodData.put("location", foodItem.getLocation());
        foodData.put("quantity", foodItem.getQuantity());
        foodData.put("halal", foodItem.isHalal());
        foodData.put("spicy", foodItem.isSpicy());
        foodData.put("status", "available");  // Always set initial status as available
        
        if (isEditMode && editFoodItem != null) {
            // Update existing item
            listingsRef.child(editFoodItem.getItemId()).updateChildren(foodData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Food listing updated successfully!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update food listing: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        } else {
            // Create new item with timestamp
            foodData.put("createdAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(new Date()));
            foodData.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(new Date()));
            
            String itemKey = listingsRef.push().getKey();
            if (itemKey != null) {
                listingsRef.child(itemKey).setValue(foodData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Food listing saved successfully!", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                        clearForm();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save food listing: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == requireActivity().RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                try {
                    File file = new File(currentPhotoPath);
                    if (file.exists()) {
                        selectedImageFile = file;
                        // Scale down the image to prevent OutOfMemoryError
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4; // Adjust this value as needed
                        imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath, options));
                        Log.d(TAG, "Camera image loaded successfully");
                    } else {
                        Log.e(TAG, "Image file does not exist: " + currentPhotoPath);
                        Toast.makeText(getContext(), "Error loading captured image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing camera result: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error processing captured image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imageView.setImageURI(selectedImageUri);
                    try {
                        selectedImageFile = FileUtil.from(requireContext(), selectedImageUri);
                    } catch (IOException e) {
                        Log.e(TAG, "Error creating file from URI", e);
                    }
                }
            } else if (requestCode == EXTENDED_ACTIVITY_REQUEST_CODE) {
                Toast.makeText(getContext(), "Food saved successfully!", Toast.LENGTH_SHORT).show();
                clearForm();
            }
        } else {
            Log.e(TAG, "Camera result not OK. ResultCode: " + resultCode);
        }
    }

    private void clearForm() {
        expirationDateEdit.setText("");
        locationEdit.setText("");
        quantityEdit.setText("");
        foodNameText.setText("Detected Food: -");
        ingredientsText.setText("Ingredients: -");
        halalText.setText("Halal: -");
        spicyText.setText("Spicy: -");
        imageView.setImageResource(android.R.color.darker_gray);
        selectedImageFile = null;
    }

    // Add this method to validate date format
    private boolean isValidDateFormat(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this listing?")
            .setPositiveButton("Delete", (dialog, which) -> deleteListing())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteListing() {
        if (auth.getCurrentUser() == null || currentListingId == null) {
            Toast.makeText(requireContext(), "Unable to delete listing", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference listingRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings")
                .child(userId)
                .child("items")
                .child(currentListingId);

        listingRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Listing deleted successfully", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to delete listing: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
} 