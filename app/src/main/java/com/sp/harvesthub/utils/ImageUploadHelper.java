package com.sp.harvesthub.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;

public class ImageUploadHelper {
    private static final String STORAGE_PATH = "images/";
    private final FirebaseStorage storage;
    private final Context context;

    public ImageUploadHelper(Context context) {
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    public void uploadImage(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onFailure("No image selected");
            return;
        }

        String imageName = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child(STORAGE_PATH + imageName);

        imageRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        listener.onSuccess(uri.toString());
                        Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> {
                listener.onFailure(e.getMessage());
                Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            })
            .addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                listener.onProgress((int) progress);
            });
    }

    public void loadImage(String imageUrl, ImageView imageView) {
        Glide.with(context)
            .load(imageUrl)
            .centerCrop()
            .into(imageView);
    }

    public interface OnImageUploadListener {
        void onSuccess(String imageUrl);
        void onFailure(String error);
        void onProgress(int progress);
    }
} 