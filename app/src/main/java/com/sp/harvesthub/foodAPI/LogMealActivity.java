package com.sp.harvesthub.foodAPI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sp.harvesthub.foodListings.FoodActivity;
import com.sp.harvesthub.foodListings.FoodItem;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodListings.FoodItemExtended;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogMealActivity extends AppCompatActivity {
    private static final String TAG = "LogMealActivity";

    private LogMealService logMealService;
    private ImageView imageView;
    private TextView foodNameText, ingredientsText, halalText, spicyText;
    private File selectedImageFile;
    private DatabaseReference foodsRef;
    private String currentPhotoPath;
    private Uri photoURI;
    private static final int CAMERA_REQUEST_CODE = 101;
    private FirebaseAuth auth;
    private EditText expirationDateEdit, locationEdit, quantityEdit;
    private boolean availability;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logmeal);

        foodsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/").getReference("foods");

        logMealService = new LogMealService();
        imageView = findViewById(R.id.imageView);
        foodNameText = findViewById(R.id.foodNameText);
        ingredientsText = findViewById(R.id.ingredientsText);
        halalText = findViewById(R.id.halalText);
        spicyText = findViewById(R.id.spicyText);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button analyzeButton = findViewById(R.id.analyzeButton);
        Button camButton = findViewById(R.id.camButton);

        expirationDateEdit = findViewById(R.id.expirationDateEdit);
        locationEdit = findViewById(R.id.locationEdit);
        quantityEdit = findViewById(R.id.quantityEdit);

        String[] permissions = {
                android.Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        uploadButton.setOnClickListener(v -> openFilePicker());
        analyzeButton.setOnClickListener(v -> {
            if (selectedImageFile != null) {
                Log.d(TAG, "Analyzing image...");
                analyzeImage(selectedImageFile);
            } else {
                Toast.makeText(this, "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
            }
        });
        camButton.setOnClickListener(v -> {
            if (hasPermissions(permissions)) {
                dispatchTakePictureIntent();
            } else {
                ActivityCompat.requestPermissions(this, permissions, CAMERA_REQUEST_CODE);
            }
        });

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> Log.d(TAG, "Anonymous auth successful"))
                    .addOnFailureListener(e -> Log.e(TAG, "Anonymous auth failed", e));
        }
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            photoURI = FileProvider.getUriForFile(this, "com.sp.harvesthub.fileprovider", photoFile);
            currentPhotoPath = photoFile.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Log.d(TAG, "Camera image captured");
                if (currentPhotoPath != null) {
                    Log.d(TAG, "Photo path: " + currentPhotoPath);
                    selectedImageFile = new File(currentPhotoPath);
                    Log.d(TAG, "File exists: " + selectedImageFile.exists() + ", size: " + selectedImageFile.length());

                    imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
                } else {
                    Log.e(TAG, "currentPhotoPath is null");
                }
            } else if (requestCode == 102 && data != null) {
                Log.d(TAG, "Gallery image selected");
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    Log.d(TAG, "Gallery image URI: " + selectedImageUri.toString());
                    imageView.setImageURI(selectedImageUri);
                    try {
                        selectedImageFile = FileUtil.from(this, selectedImageUri);
                    } catch (IOException e) {
                        Log.e(TAG, "Error creating file from URI", e);
                    }
                } else {
                    Log.e(TAG, "Selected image URI is null");
                }
            } else if (requestCode == 1001) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Food saved successfully!", Toast.LENGTH_SHORT).show();
                    // Clear the form or update UI as needed
                    clearForm();
                } else {
                    Toast.makeText(this, "Failed to save extended food data", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.e(TAG, "Activity result not OK - resultCode: " + resultCode);
        }
    }

    private void uploadImageToFirebase(Uri fileUri, String dishName) {
        if (fileUri != null) {

            // Create a unique filename using timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = dishName.replaceAll("\\s+", "_") + "_" + timeStamp + ".jpg";

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("food_images/" + imageName);

            UploadTask uploadTask = imageRef.putFile(fileUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image uploaded successfully");
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Update the food record with the image URL
                    updateFoodRecordWithImage(dishName, imageUrl);
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                Toast.makeText(LogMealActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateFoodRecordWithImage(String dishName, String imageUrl) {
        foodsRef.child(dishName).child("imageURL").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image URL added to database for " + dishName + ": " + imageUrl);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add image URL to database: " + e.getMessage()));
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    imageView.setImageURI(imageUri);
                    try {
                        selectedImageFile = FileUtil.from(this, imageUri);
                        Log.d(TAG, "Image selected: " + selectedImageFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load image: " + e.getMessage(), e);
                        foodNameText.setText("Failed to load image.");
                        Toast.makeText(LogMealActivity.this, "Error loading image.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
        Log.d(TAG, "Opened file picker.");
    }

    private void analyzeImage(File imageFile) {
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
                    FoodItemExtended foodItem = snapshot.getValue(FoodItemExtended.class);
                    updateUI(foodItem);
                } else {
                    promptUserForFoodDetails(dishName);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LogMealActivity.this, "Database error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FoodItemExtended foodItem) {
        foodNameText.setText("Detected Food: " + foodItem.getDishName());
        ingredientsText.setText("Ingredients: " + String.join(", ", foodItem.getIngredients()));
        halalText.setText("Halal: " + (foodItem.isHalal() ? "Yes" : "No"));
        spicyText.setText("Spicy: " + (foodItem.isSpicy() ? "Yes" : "No"));
        expirationDateEdit.setText(foodItem.getExpirationDate());
        locationEdit.setText(foodItem.getLocation());
        quantityEdit.setText(foodItem.getQuantity());
        Toast.makeText(LogMealActivity.this, "Updated", Toast.LENGTH_LONG).show();
    }

    private void promptUserForFoodDetails(String detectedDishName) {
        // Set the detected dish name
        foodNameText.setText("Detected Food: " + detectedDishName);

        // Show halal dialog first, which will chain to other dialogs
        showHalalDialog(detectedDishName);
    }

    private void showHalalDialog(String dishName) {
        final boolean[] isHalal = {false};

        AlertDialog.Builder halalDialog = new AlertDialog.Builder(this);
        halalDialog.setTitle("Is " + dishName + " Halal?");
        halalDialog.setSingleChoiceItems(new String[]{"Yes", "No"}, -1, (dialog, which) -> {
            isHalal[0] = (which == 0);
        });
        halalDialog.setPositiveButton("Next", (dialog, which) -> showSpicyDialog(dishName, isHalal[0]));
        halalDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        halalDialog.show();
    }

    private void showSpicyDialog(String dishName, boolean isHalal) {
        final boolean[] isSpicy = {false};

        AlertDialog.Builder spicyDialog = new AlertDialog.Builder(this);
        spicyDialog.setTitle("Is " + dishName + " Spicy?");
        spicyDialog.setSingleChoiceItems(new String[]{"Yes", "No"}, -1, (dialog, which) -> {
            isSpicy[0] = (which == 0);
        });
        spicyDialog.setPositiveButton("Next", (dialog, which) -> showIngredientsDialog(dishName, isHalal, isSpicy[0]));
        spicyDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        spicyDialog.show();
    }

    private void showIngredientsDialog(String dishName, boolean isHalal, boolean isSpicy) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Enter Ingredients for " + dishName);

        final EditText ingredientsInput = new EditText(this);
        ingredientsInput.setHint("Enter ingredients separated by commas");
        dialogBuilder.setView(ingredientsInput);

        dialogBuilder.setPositiveButton("Save", (dialog, which) -> {
            String ingredientsText = ingredientsInput.getText().toString();
            List<String> ingredientsList = Arrays.asList(ingredientsText.split(",\\s*"));

            // Get values from EditTexts in the main layout
            String expirationDate = expirationDateEdit.getText().toString();
            String location = locationEdit.getText().toString();
            String quantity = quantityEdit.getText().toString();

            if (expirationDate.isEmpty() || location.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show availability dialog
            new AlertDialog.Builder(this)
                    .setTitle("Is the food available?")
                    .setPositiveButton("Yes", (dialog2, which2) -> {
                        // Save basic food data
                        FoodItem basicFood = new FoodItem(dishName, isHalal, isSpicy, ingredientsList);
                        saveFoodData(basicFood);

                        // Save extended food data
                        FoodItemExtended extendedFood = new FoodItemExtended(
                                dishName, isHalal, isSpicy, ingredientsList,
                                expirationDate, location, quantity, true);
                        saveToExtendedDatabase(extendedFood);
                    })
                    .setNegativeButton("No", (dialog2, which2) -> {
                        // Same as above but with availability = false
                        FoodItem basicFood = new FoodItem(dishName, isHalal, isSpicy, ingredientsList);
                        saveFoodData(basicFood);

                        FoodItemExtended extendedFood = new FoodItemExtended(
                                dishName, isHalal, isSpicy, ingredientsList,
                                expirationDate, location, quantity, false);
                        saveToExtendedDatabase(extendedFood);
                    })
                    .show();
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }

    private void saveFoodData(FoodItem foodItem) {
        // Save basic food data to /foods
        foodsRef.child(foodItem.getDishName()).setValue(foodItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Basic food data saved!", Toast.LENGTH_SHORT).show();
                    // Upload image if available
                    if (selectedImageFile != null) {
                        Uri fileUri = Uri.fromFile(selectedImageFile);
                        uploadImageToFirebase(fileUri, foodItem.getDishName());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save basic food data", Toast.LENGTH_SHORT).show());
    }

    private void saveToExtendedDatabase(FoodItemExtended foodItem) {
        try {
            Intent intent = new Intent(this, LogMealExtendedActivity.class);
            intent.putExtra("foodItem", foodItem);
            startActivityForResult(intent, 1001); // Use startActivityForResult instead of startActivity
        } catch (Exception e) {
            Log.e(TAG, "Error starting LogMealExtendedActivity", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        // Clear all input fields
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