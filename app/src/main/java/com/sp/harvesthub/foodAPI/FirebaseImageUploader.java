package com.sp.harvesthub.foodAPI;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.util.Log;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseImageUploader {
    private static final String FOOD_IMAGES_PATH = "food_images/";
    private static final String STORAGE_BUCKET = "splashscreen2.firebasestorage.app";
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface OnUploadCompleteListener {
        void onComplete(List<String> imageUrls);
        void onError(Exception e);
    }

    public interface OnImageUploadListener {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    private static void ensureAuthenticated(OnUploadCompleteListener listener) {
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseImageUploader", "Authentication failed", e);
                        listener.onError(e);
                    });
        }
    }

    public static void uploadImages(List<Uri> imageUris, OnUploadCompleteListener listener) {
        ensureAuthenticated(listener);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        List<String> uploadedUrls = new ArrayList<>();
        final int[] remainingUploads = {imageUris.size()};

        Log.d("FirebaseImageUploader", "Starting upload of " + imageUris.size() + " images");

        for (Uri imageUri : imageUris) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = timestamp + ".jpg";

            // Create the full path including the directory
            String fullPath = FOOD_IMAGES_PATH + fileName;
            Log.d("FirebaseImageUploader", "Attempting to upload to path: " + fullPath);

            StorageReference imageRef = storage.getReference(fullPath);

            // Log the full reference details
            Log.d("FirebaseImageUploader", "Storage Reference - Bucket: " + imageRef.getBucket());
            Log.d("FirebaseImageUploader", "Storage Reference - Path: " + imageRef.getPath());

            // Add metadata to ensure proper content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            imageRef.putFile(imageUri, metadata)
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d("FirebaseImageUploader", "Upload progress: " + progress + "%");
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("FirebaseImageUploader", "Upload successful, getting download URL");
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    Log.d("FirebaseImageUploader", "Got download URL: " + downloadUrl);
                                    uploadedUrls.add(downloadUrl);
                                    remainingUploads[0]--;

                                    if (remainingUploads[0] == 0) {
                                        Log.d("FirebaseImageUploader", "All uploads complete");
                                        listener.onComplete(uploadedUrls);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseImageUploader", "Failed to get download URL", e);
                                    listener.onError(e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseImageUploader", "Upload failed", e);
                        listener.onError(e);
                    });
        }
    }

    public void uploadImage(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            String timestamp = String.valueOf(System.currentTimeMillis());
            StorageReference imageRef = FirebaseStorage.getInstance()
                    .getReferenceFromUrl("gs://" + STORAGE_BUCKET)
                    .child(FOOD_IMAGES_PATH)
                    .child(timestamp + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    listener.onSuccess(imageUrl);
                                })
                                .addOnFailureListener(e -> {
                                    listener.onFailure(e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        listener.onFailure(e.getMessage());
                    });
        } else {
            listener.onFailure("No image selected");
        }
    }
}