package com.sp.harvesthub.utils;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import android.webkit.MimeTypeMap;

public class FirebaseImageUploader {
    private static final String CHAT_IMAGES_PATH = "chat_images/";
    
    public interface OnUploadCompleteListener {
        void onComplete(List<String> imageUrls);
        void onError(Exception e);
    }

    public static void uploadImages(List<Uri> imageUris, OnUploadCompleteListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        List<String> uploadedUrls = new ArrayList<>();
        final int[] remainingUploads = {imageUris.size()};

        for (Uri imageUri : imageUris) {
            // Get original filename from Uri
            String originalFilename = imageUri.getLastPathSegment();
            if (originalFilename == null) {
                originalFilename = "image";
            }
            
            // Get file extension
            String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(storage.getApp().getApplicationContext()
                .getContentResolver().getType(imageUri));
            if (extension == null) {
                extension = originalFilename.contains(".") ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".") + 1) : "jpg";
            }
            
            // Create filename with timestamp (matching website format)
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = timestamp + "-" + originalFilename;
            
            // Create full path
            String fullPath = CHAT_IMAGES_PATH + filename;
            StorageReference imageRef = storage.getReference().child(fullPath);

            imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String downloadUrl = task.getResult().toString();
                        uploadedUrls.add(downloadUrl);
                        remainingUploads[0]--;
                        
                        if (remainingUploads[0] == 0) {
                            listener.onComplete(uploadedUrls);
                        }
                    } else {
                        listener.onError(task.getException());
                    }
                });
        }
    }
} 