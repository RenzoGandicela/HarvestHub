package com.sp.harvesthub.utils;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storage.getReference().child(CHAT_IMAGES_PATH + imageName);

            imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadedUrls.add(task.getResult().toString());
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