package com.sp.harvesthub.nav_fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sp.harvesthub.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;

public class ProfileFragment extends Fragment {
    
    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profileImage;
    private TextView usernameText, emailText, roleText;
    private EditText descriptionEdit;
    private Button saveButton;
    private ImageButton editUsernameButton;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private ValueEventListener profileListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(currentUser.getUid());
        }
        
        // Initialize views
        initializeViews(view);
        
        // Load user data
        loadUserProfile();
        
        // Set up click listeners
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        usernameText = view.findViewById(R.id.profileUsername);
        emailText = view.findViewById(R.id.profileEmail);
        roleText = view.findViewById(R.id.profileRole);
        descriptionEdit = view.findViewById(R.id.profileDescription);
        saveButton = view.findViewById(R.id.saveDescriptionButton);
        editUsernameButton = view.findViewById(R.id.editUsernameButton);
    }

    private void setupClickListeners() {
        profileImage.setOnClickListener(v -> openImageChooser());
        
        editUsernameButton.setOnClickListener(v -> showUsernameDialog());
        
        saveButton.setOnClickListener(v -> saveDescription());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_username, null);
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        usernameInput.setText(usernameText.getText());

        builder.setView(dialogView)
               .setTitle("Edit Username")
               .setPositiveButton("Save", (dialog, which) -> {
                   String newUsername = usernameInput.getText().toString().trim();
                   if (!newUsername.isEmpty()) {
                       updateUsername(newUsername);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void updateUsername(String newUsername) {
        if (currentUser == null) return;

        // Create a HashMap to store all the updates we need to make
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("/Users/" + currentUser.getUid() + "/username", newUsername);

        // Get reference to the root of the database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        
        // First update the user profile
        rootRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                if (isAdded() && getContext() != null) {
                    usernameText.setText(newUsername);
                    Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Failed to update username: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded() && getContext() != null) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String profileImageUrl = snapshot.child("profilePicture").getValue(String.class);

                    usernameText.setText(username);
                    emailText.setText(email);
                    roleText.setText(role);
                    if (description != null) {
                        descriptionEdit.setText(description);
                    }
                    
                    if (profileImageUrl != null && !profileImageUrl.isEmpty() && isAdded()) {
                        try {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error loading profile: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        };
        
        userRef.addValueEventListener(profileListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && profileListener != null) {
            userRef.removeEventListener(profileListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null) return;

        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            })
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && isAdded() && getContext() != null) {
                    Uri downloadUri = task.getResult();
                    // Update both profile picture URLs to maintain consistency
                    userRef.child("profilePicture").setValue(downloadUri.toString())
                        .addOnSuccessListener(aVoid -> {
                            if (isAdded() && getContext() != null) {
                                // Update the ImageView immediately
                                Glide.with(requireContext())
                                    .load(downloadUri)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(profileImage);
                                    
                                Toast.makeText(getContext(), 
                                    "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (isAdded() && getContext() != null) {
                                Toast.makeText(getContext(), 
                                    "Failed to update profile picture: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), 
                        "Failed to upload image: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void saveDescription() {
        String description = descriptionEdit.getText().toString().trim();
        
        if (currentUser == null) return;

        userRef.child("description").setValue(description)
            .addOnSuccessListener(aVoid -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), 
                        "Description updated successfully", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), 
                        "Failed to update description: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}