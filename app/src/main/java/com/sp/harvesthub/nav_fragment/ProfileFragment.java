package com.sp.harvesthub.nav_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;

public class ProfileFragment extends Fragment {
    
    private TextView usernameText, emailText, roleText;
    private EditText descriptionEdit;
    private Button saveButton;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        }
        
        // Initialize views
        initializeViews(view);
        
        // Load user data
        loadUserProfile();
        
        // Set up save button
        saveButton.setOnClickListener(v -> saveDescription());
        
        return view;
    }

    private void initializeViews(View view) {
        usernameText = view.findViewById(R.id.profileUsername);
        emailText = view.findViewById(R.id.profileEmail);
        roleText = view.findViewById(R.id.profileRole);
        descriptionEdit = view.findViewById(R.id.profileDescription);
        saveButton = view.findViewById(R.id.saveDescriptionButton);
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);

                    usernameText.setText(username);
                    emailText.setText(email);
                    roleText.setText(role);
                    if (description != null) {
                        descriptionEdit.setText(description);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading profile: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDescription() {
        String description = descriptionEdit.getText().toString().trim();
        
        if (currentUser == null) return;

        userRef.child("description").setValue(description)
            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), 
                "Description updated successfully", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(getContext(), 
                "Failed to update description: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
} 