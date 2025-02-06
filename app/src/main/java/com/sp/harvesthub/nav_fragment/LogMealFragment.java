package com.sp.harvesthub.nav_fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodAPI.ApiCallback;
import com.sp.harvesthub.foodAPI.FileUtil;
import com.sp.harvesthub.foodAPI.LogMealService;
import com.sp.harvesthub.foodAPI.LogMealExtendedActivity;
import com.sp.harvesthub.foodListings.FoodActivity;
import com.sp.harvesthub.foodListings.FoodItem;
import com.sp.harvesthub.foodListings.FoodItemExtended;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
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
    private Button uploadButton, analyzeButton, camButton;
    private File selectedImageFile;
    private String currentPhotoPath;
    private Uri photoURI;
    private DatabaseReference foodsRef;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        logMealService = new LogMealService();
        foodsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/").getReference("foods");
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_logmeal, container, false);
        initializeViews(view);
        setupButtonListeners();
        return view;
    }

    private void initializeViews(View view) {
        imageView = view.findViewById(R.id.imageView);
        foodNameText = view.findViewById(R.id.foodNameText);
        ingredientsText = view.findViewById(R.id.ingredientsText);
        halalText = view.findViewById(R.id.halalText);
        spicyText = view.findViewById(R.id.spicyText);
        expirationDateEdit = view.findViewById(R.id.expirationDateEdit);
        locationEdit = view.findViewById(R.id.locationEdit);
        quantityEdit = view.findViewById(R.id.quantityEdit);
        uploadButton = view.findViewById(R.id.uploadButton);
        analyzeButton = view.findViewById(R.id.analyzeButton);
        camButton = view.findViewById(R.id.camButton);
    }

    private void setupButtonListeners() {
        uploadButton.setOnClickListener(v -> openFilePicker());
        
        analyzeButton.setOnClickListener(v -> {
            // First check if image is selected
            if (selectedImageFile == null) {
                Toast.makeText(getContext(), "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate all required fields first
            String expirationDate = expirationDateEdit.getText().toString().trim();
            String location = locationEdit.getText().toString().trim();
            String quantity = quantityEdit.getText().toString().trim();

            if (expirationDate.isEmpty()) {
                expirationDateEdit.setError("Please enter expiration date");
                expirationDateEdit.requestFocus();
                return;
            }

            if (location.isEmpty()) {
                locationEdit.setError("Please enter location");
                locationEdit.requestFocus();
                return;
            }

            if (quantity.isEmpty()) {
                quantityEdit.setError("Please enter quantity");
                quantityEdit.requestFocus();
                return;
            }
            
            // Check if file exists and is not empty
            if (!selectedImageFile.exists() || selectedImageFile.length() == 0) {
                Toast.makeText(getContext(), "Invalid image file. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check file size (e.g., max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB in bytes
            if (selectedImageFile.length() > maxSize) {
                Toast.makeText(getContext(), "Image file is too large. Please choose a smaller image.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // If all validations pass, analyze the image
            analyzeImage(selectedImageFile);
        });
        
        camButton.setOnClickListener(v -> {
            if (hasPermissions(REQUIRED_PERMISSIONS)) {
                dispatchTakePictureIntent();
            } else {
                requestPermissions(REQUIRED_PERMISSIONS, CAMERA_REQUEST_CODE);
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // Ensure the camera app exists
        if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(requireContext(),
                            "com.sp.harvesthub.fileprovider",  // Make sure this matches your manifest
                            photoFile);
                    
                    // Add permissions
                    List<ResolveInfo> resInfoList = requireActivity().getPackageManager()
                            .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        requireActivity().grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                    Log.d(TAG, "Camera intent dispatched with URI: " + photoURI);
                }
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file: " + ex.getMessage(), ex);
                Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No camera available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        // Create the storage directory if it does not exist
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                throw new IOException("Failed to create directory");
            }
        }
        
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",        /* suffix */
                storageDir     /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void analyzeImage(File imageFile) {
        logMealService.analyzeFoodImage(imageFile, new ApiCallback() {
            @Override
            public void onSuccess(String result) {
                requireActivity().runOnUiThread(() -> {
                    String detectedDishName = extractTopDishName(result);
                    if (detectedDishName.isEmpty()) {
                        foodNameText.setText("No food detected.");
                        Toast.makeText(getContext(), "No food detected.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    foodNameText.setText("Detected Food: " + detectedDishName);
                    saveOrRetrieveFoodFromFirebase(detectedDishName);
                });
            }

            @Override
            public void onFailure(String error) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String extractTopDishName(String result) {
        try {
            String[] sections = result.split("\n\n");
            if (sections.length == 0) return "";

            String detectedDishText = sections[0];
            if (detectedDishText.contains("\n")) {
                String[] lines = detectedDishText.split("\n");
                if (lines.length > 1) {
                    String topDishLine = lines[1].trim();
                    int openParenIndex = topDishLine.indexOf("(");
                    if (openParenIndex != -1) {
                        return topDishLine.substring(3, openParenIndex).trim();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting top dish name: " + e.getMessage());
        }
        return "";
    }

    private void saveOrRetrieveFoodFromFirebase(String dishName) {
        foodsRef.child(dishName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Food exists in foods database - load its data
                    FoodItem existingFood = snapshot.getValue(FoodItem.class);
                    if (existingFood != null) {
                        // Update UI with existing data
                        foodNameText.setText("Detected Food: " + existingFood.getDishName());
                        ingredientsText.setText("Ingredients: " + String.join(", ", existingFood.getIngredients()));
                        halalText.setText("Halal: " + (existingFood.isHalal() ? "Yes" : "No"));
                        spicyText.setText("Spicy: " + (existingFood.isSpicy() ? "Yes" : "No"));
                        
                        // Still prompt for new details for testing database
                        promptUserForFoodDetails(dishName);
                    }
                } else {
                    // Food doesn't exist, proceed with new data collection
                    promptUserForFoodDetails(dishName);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Database error occurred.", Toast.LENGTH_SHORT).show();
                promptUserForFoodDetails(dishName);
            }
        });
    }

    private void promptUserForFoodDetails(String dishName) {
        showHalalDialog(dishName);
    }

    private void showHalalDialog(String dishName) {
        try {
            final boolean[] isHalal = {false};
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Is " + dishName + " Halal?")
                   .setCancelable(false)
                   .setSingleChoiceItems(new String[]{"Yes", "No"}, -1, (dialog, which) -> {
                       isHalal[0] = (which == 0);
                   })
                   .setPositiveButton("Next", (dialog, which) -> {
                       dialog.dismiss();
                       showSpicyDialog(dishName, isHalal[0]);
                   })
                   .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            
            AlertDialog dialog = builder.create();
            dialog.show();
            Log.d(TAG, "Showing halal dialog for dish: " + dishName);
        } catch (Exception e) {
            Log.e(TAG, "Error showing halal dialog: " + e.getMessage(), e);
        }
    }

    private void showSpicyDialog(String dishName, boolean isHalal) {
        final boolean[] isSpicy = {false};
        new AlertDialog.Builder(requireContext())
            .setTitle("Is " + dishName + " Spicy?")
            .setSingleChoiceItems(new String[]{"Yes", "No"}, -1, (dialog, which) -> {
                isSpicy[0] = (which == 0);
            })
            .setPositiveButton("Next", (dialog, which) -> showIngredientsDialog(dishName, isHalal, isSpicy[0]))
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void showIngredientsDialog(String dishName, boolean isHalal, boolean isSpicy) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setTitle("Enter Ingredients for " + dishName);

        final EditText ingredientsInput = new EditText(requireContext());
        ingredientsInput.setHint("Enter ingredients separated by commas");
        dialogBuilder.setView(ingredientsInput);

        dialogBuilder.setPositiveButton("Save", (dialog, which) -> {
            String ingredientsText = ingredientsInput.getText().toString().trim();
            
            // Validate ingredients
            if (ingredientsText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter ingredients", Toast.LENGTH_SHORT).show();
                return;
            }

            // Double check required fields again before saving
            String expirationDate = expirationDateEdit.getText().toString().trim();
            String location = locationEdit.getText().toString().trim();
            String quantity = quantityEdit.getText().toString().trim();

            if (expirationDate.isEmpty() || location.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> ingredientsList = Arrays.asList(ingredientsText.split(",\\s*"));

            // Create FoodItem for foods database (with limited fields)
            FoodItem basicFoodItem = new FoodItem();
            basicFoodItem.setDishName(dishName);
            basicFoodItem.setHalal(isHalal);
            basicFoodItem.setSpicy(isSpicy);
            basicFoodItem.setIngredients(ingredientsList);

            // Save basic food data
            saveFoodData(basicFoodItem);

            // Create extended food item for testing database
            FoodItemExtended extendedFoodItem = new FoodItemExtended(
                dishName, isHalal, isSpicy, ingredientsList,
                expirationDate, location, quantity, true);
            saveToExtendedDatabase(extendedFoodItem);
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
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

    private void saveToExtendedDatabase(FoodItemExtended foodItem) {
        try {
            // Get current user
            if (auth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "Please sign in to save food data", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                    .getReference("listings")
                    .child(userId)
                    .child("items");

            // Generate a unique key for the item
            String itemKey = listingsRef.push().getKey();
            if (itemKey == null) {
                Toast.makeText(getContext(), "Error generating database key", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    .format(new Date());

            // Create the data map without a description field
            Map<String, Object> foodData = new HashMap<>();
            foodData.put("title", foodItem.getDishName());
            foodData.put("halal", foodItem.isHalal());
            foodData.put("spicy", foodItem.isSpicy());
            foodData.put("ingredients", foodItem.getIngredients());
            foodData.put("expiryDate", foodItem.getExpirationDate());
            foodData.put("location", foodItem.getLocation());
            foodData.put("quantity", foodItem.getQuantity());
            foodData.put("sellerId", userId);
            foodData.put("status", foodItem.isAvailable() ? "available" : "unavailable");
            foodData.put("createdAt", timestamp);
            foodData.put("updatedAt", timestamp);

            // Save to listings database
            listingsRef.child(itemKey).setValue(foodData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Successfully saved to listings database");
                        Toast.makeText(getContext(), "Food data saved successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving to listings database: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Error saving food data: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in saveToExtendedDatabase: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
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
} 