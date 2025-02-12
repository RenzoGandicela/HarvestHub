package com.sp.harvesthub.nav_fragment;

<<<<<<< HEAD
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.sp.harvesthub.R;
import com.sp.harvesthub.foodAPI.LogMealActivity;
import com.sp.harvesthub.foodAPI.LogMealService;
import com.sp.harvesthub.foodListings.FoodActivity;

import java.io.File;
import java.io.IOException;

public class HomeFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 101;
    private ImageView imageView;
    private TextView foodNameText, ingredientsText, halalText, spicyText;
    private EditText expirationDateEdit, locationEdit, quantityEdit;
    private Button uploadButton, analyzeButton, camButton;
    private LogMealService logMealService;
    private File selectedImageFile;
    private String currentPhotoPath;
    private Uri photoURI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        logMealService = new LogMealService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_logmeal, container, false);
        
        // Initialize views
        initializeViews(view);
        
        // Set up button click listeners
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
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 102);
        });

        analyzeButton.setOnClickListener(v -> {
            if (selectedImageFile != null) {
                Intent intent = new Intent(getActivity(), LogMealActivity.class);
                intent.putExtra("imageFile", selectedImageFile.getAbsolutePath());
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Please select or capture an image first.", Toast.LENGTH_SHORT).show();
            }
        });

        camButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LogMealActivity.class);
            intent.putExtra("openCamera", true);
            startActivity(intent);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle activity results (camera/gallery)
        // This will be handled by LogMealActivity
=======
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.sp.harvesthub.R;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
    }
} 