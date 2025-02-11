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
import android.widget.Switch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.sp.harvesthub.foodListings.FoodFragment;

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
    private StorageReference storageRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        logMealService = new LogMealService();
        foodsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/").getReference("foods");
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.food) {
            // Replace current fragment with FoodFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FoodFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        } else if (item.getItemId() == R.id.logMeal) {
            // Replace current fragment with LogMealFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LogMealFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        // Initialize switches
        Switch halalSwitch = view.findViewById(R.id.switchHalal);
        Switch spicySwitch = view.findViewById(R.id.switchSpicy);

        // Set up switch listeners
        halalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            halalSwitch.setText(isChecked ? " True" : " False");
        });

        spicySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            spicySwitch.setText(isChecked ? " True" : " False");
        });
    }

    private void setupButtonListeners() {
        uploadButton.setOnClickListener(v -> openFilePicker());
        
        analyzeButton.setOnClickListener(v -> {
            // First check if image is selected
            if (selectedImageFile == null) {
                Toast.makeText(getContext(), "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get all input values
            String ingredients = ((EditText) requireView().findViewById(R.id.ingredientsEdit))
                    .getText().toString().trim();
            String expirationDate = expirationDateEdit.getText().toString().trim();
            String location = locationEdit.getText().toString().trim();
            String quantity = quantityEdit.getText().toString().trim();
            
            // Get switch states
            Switch halalSwitch = requireView().findViewById(R.id.switchHalal);
            Switch spicySwitch = requireView().findViewById(R.id.switchSpicy);
            boolean isHalal = halalSwitch.isChecked();
            boolean isSpicy = spicySwitch.isChecked();

            // Validate all fields
            if (ingredients.isEmpty()) {
                ((EditText) requireView().findViewById(R.id.ingredientsEdit)).setError("Please enter ingredients");
                ((EditText) requireView().findViewById(R.id.ingredientsEdit)).requestFocus();
                return;
            }

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

            // Store the input values to be used after image analysis
            List<String> ingredientsList = Arrays.asList(ingredients.split(",\\s*"));
            
            String description = ((EditText) requireView().findViewById(R.id.descEdit))
                    .getText().toString().trim();

            // Add description validation
            if (description.isEmpty()) {
                ((EditText) requireView().findViewById(R.id.descEdit)).setError("Please enter a description");
                ((EditText) requireView().findViewById(R.id.descEdit)).requestFocus();
                return;
            }

            // Add this method to validate date format
            if (!isValidDateFormat(expirationDate)) {
                expirationDateEdit.setError("Please use format: YYYY-MM-DD HH:mm");
                expirationDateEdit.requestFocus();
                return;
            }

            // If all validations pass, analyze the image and save data
            analyzeImage(selectedImageFile, new ApiCallback() {
                @Override
                public void onSuccess(String result) {
                    String dishName = extractDishName(result);
                    
                    // Upload image first, then save data
                    if (selectedImageFile != null) {
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(new Date());
                        String imagePath = "listings/" + auth.getCurrentUser().getUid() + "/" + timestamp + ".jpg";
                        StorageReference imageRef = storageRef.child(imagePath);
                        
                        UploadTask uploadTask = imageRef.putFile(Uri.fromFile(selectedImageFile));
                        uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return imageRef.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                // Create and save food item with image URL
                                FoodItemExtended foodItem = new FoodItemExtended(
                                    dishName,
                                    isHalal,
                                    isSpicy,
                                    ingredientsList,
                                    expirationDate,
                                    location,
                                    quantity,
                                    true  // default to available
                                );
                                foodItem.setDescription(description);
                                foodItem.setImageUrl(downloadUri.toString());
                                
                                // Save to both databases
                                saveFoodData(foodItem);
                                saveToExtendedDatabase(foodItem);
                                
                                // Update UI to show success
                                requireActivity().runOnUiThread(() -> {
                                    foodNameText.setText("Detected Food: " + dishName);
                                    Toast.makeText(getContext(), "Food item saved successfully!", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> 
                                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    }
                }

                @Override
                public void onFailure(String error) {
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Error analyzing image: " + error, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
        
        camButton.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                dispatchTakePictureIntent();
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

    private boolean checkAndRequestPermissions() {
        if (hasPermissions(REQUIRED_PERMISSIONS)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, CAMERA_REQUEST_CODE);
            return false;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.sp.harvesthub.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                
                // Grant permissions to all apps that can handle the intent
                List<ResolveInfo> resInfoList = requireActivity().getPackageManager()
                        .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    requireActivity().grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void analyzeImage(File imageFile, ApiCallback callback) {
        if (logMealService != null) {
            logMealService.analyzeImage(imageFile, new ApiCallback() {
                @Override
                public void onSuccess(String result) {
                    requireActivity().runOnUiThread(() -> {
                        foodNameText.setText("Detected Food: " + extractDishName(result));
                    });
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        }
    }

    private String extractDishName(String result) {
        // The result contains "Detected Food:" followed by the first food item
        String[] lines = result.split("\n");
        if (lines.length >= 2) {
            // Get the first food item line and extract just the name (before the probability)
            String foodLine = lines[1].substring(3); // Skip the "1. " prefix
            int probIndex = foodLine.indexOf(" (");
            if (probIndex > 0) {
                return foodLine.substring(0, probIndex);
            }
            return foodLine;
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
        foodData.put("status", "available");
        foodData.put("createdAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(new Date()));
        foodData.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).format(new Date()));

        // Generate a new unique key for this item
        String itemKey = listingsRef.push().getKey();
        if (itemKey != null) {
            listingsRef.child(itemKey).setValue(foodData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Food listing saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save food listing: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
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
} 