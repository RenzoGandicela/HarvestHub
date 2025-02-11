package com.sp.harvesthub.foodListings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.ImageButton;

public class FoodDetailsFragment extends Fragment {
    private static final String ARG_FOOD_ITEM = "food_item";
    private static final String TAG = "FoodDetailsFragment";
    private FoodItemExtended foodItem;
    private ImageButton likeButton;
    private TextView likeCountText;
    private FirebaseAuth auth;
    private DatabaseReference listingsRef;
    private boolean isLiked = false;

    public static FoodDetailsFragment newInstance(FoodItemExtended foodItem) {
        FoodDetailsFragment fragment = new FoodDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FOOD_ITEM, foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            foodItem = (FoodItemExtended) getArguments().getSerializable(ARG_FOOD_ITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_details, container, false);
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings");

        // Initialize views
        likeButton = view.findViewById(R.id.likeButton);
        likeCountText = view.findViewById(R.id.likeCountText);
        
        Button undoButton = view.findViewById(R.id.undoButton);
        undoButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        LinearLayout halalTag = view.findViewById(R.id.halalTag);
        LinearLayout spicyTag = view.findViewById(R.id.spicyTag);
        
        if (foodItem != null) {
            ImageView foodImage = view.findViewById(R.id.foodImage);
            TextView foodName = view.findViewById(R.id.foodName);
            TextView quantityText = view.findViewById(R.id.quantityText);
            TextView sellerIdText = view.findViewById(R.id.sellerIdText);
            TextView locationText = view.findViewById(R.id.locationText);
            TextView expiryDateText = view.findViewById(R.id.expiryDateText);
            TextView descriptionText = view.findViewById(R.id.descriptionText);
            TextView uploadTimeText = view.findViewById(R.id.uploadTimeText);

            // Load image
            Glide.with(this)
                    .load(foodItem.getImageUrl())
                    .into(foodImage);

            // Set texts with capitalized dish name
            foodName.setText(capitalizeDishName(foodItem.getDishName()));
            quantityText.setText("Available Quantity: " + foodItem.getQuantity());
            sellerIdText.setText("Supplied by: @" + foodItem.getSellerId());

            // Capitalize location
            String location = foodItem.getLocation();
            if (location != null && !location.isEmpty()) {
                location = location.substring(0, 1).toUpperCase() + location.substring(1);
                locationText.setText("Location: " + location);
            }

            // Format expiry date
            String expiryDate = foodItem.getExpirationDate();
            if (expiryDate != null && !expiryDate.isEmpty()) {
                try {
                    // Parse the original date format
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                    // Create the desired output format
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    
                    Date date = inputFormat.parse(expiryDate);
                    String formattedDate = outputFormat.format(date);
                    expiryDateText.setText("Expiry Date: " + formattedDate);
                } catch (Exception e) {
                    Log.e(TAG, "Error formatting date: " + e.getMessage());
                    expiryDateText.setText("Expiry Date: " + expiryDate);
                }
            }

            descriptionText.setText(foodItem.getDescription());
            
            // Format and set upload time
            if (foodItem.getCreatedAt() != null) {
                uploadTimeText.setText("Uploaded " + getTimeAgo(foodItem.getCreatedAt()));
            }

            // Handle tag visibility
            if (foodItem.isHalal() || foodItem.isSpicy()) {
                halalTag.setVisibility(foodItem.isHalal() ? View.VISIBLE : View.GONE);
                spicyTag.setVisibility(foodItem.isSpicy() ? View.VISIBLE : View.GONE);
            } else {
                halalTag.setVisibility(View.INVISIBLE);
                spicyTag.setVisibility(View.INVISIBLE);
            }

            // Check if user has liked this item
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                String itemId = foodItem.getItemId();
                String originalSellerId = foodItem.getOriginalSellerId(); // Use original seller ID
                
                // Get total likes count and check if user liked
                listingsRef.child(originalSellerId) // Use original seller ID here
                        .child("items")
                        .child(itemId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // Check if likedBy node exists
                                DataSnapshot likedBySnapshot = snapshot.child("likedBy");
                                if (likedBySnapshot.exists()) {
                                    // Update total likes count
                                    long likeCount = likedBySnapshot.getChildrenCount();
                                    likeCountText.setText(String.valueOf(likeCount));
                                    
                                    // Check if current user has liked
                                    isLiked = likedBySnapshot.hasChild(userId);
                                    updateLikeButton();
                                } else {
                                    likeCountText.setText("0");
                                    isLiked = false;
                                    updateLikeButton();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error fetching likes: " + error.getMessage());
                            }
                        });

                // Update like button click listener
                likeButton.setOnClickListener(v -> {
                    if (auth.getCurrentUser() != null) {
                        DatabaseReference itemRef = listingsRef
                                .child(originalSellerId)
                                .child("items")
                                .child(itemId);

                        if (isLiked) {
                            // Unlike - update both likedBy and likes count
                            itemRef.child("likedBy").child(userId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        // Update likes count
                                        itemRef.child("likedBy").get().addOnSuccessListener(snapshot -> {
                                            int newLikeCount = (int) snapshot.getChildrenCount();
                                            itemRef.child("likes").setValue(newLikeCount)
                                                    .addOnSuccessListener(unused -> {
                                                        isLiked = false;
                                                        updateLikeButton();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to update likes count: " + e.getMessage());
                                                        Toast.makeText(getContext(), "Failed to update likes count", 
                                                            Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Unlike failed: " + e.getMessage());
                                        Toast.makeText(getContext(), "Failed to unlike: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Like - update both likedBy and likes count
                            itemRef.child("likedBy").child(userId).setValue(true)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update likes count
                                        itemRef.child("likedBy").get().addOnSuccessListener(snapshot -> {
                                            int newLikeCount = (int) snapshot.getChildrenCount();
                                            itemRef.child("likes").setValue(newLikeCount)
                                                    .addOnSuccessListener(unused -> {
                                                        isLiked = true;
                                                        updateLikeButton();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to update likes count: " + e.getMessage());
                                                        Toast.makeText(getContext(), "Failed to update likes count", 
                                                            Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Like failed: " + e.getMessage());
                                        Toast.makeText(getContext(), "Failed to like: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Please login to like items", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // User not logged in
                likeCountText.setText("0");
                likeButton.setOnClickListener(v -> {
                    // Prompt user to login
                    Toast.makeText(getContext(), "Please login to like items", Toast.LENGTH_SHORT).show();
                    // Optionally redirect to login
                    // startActivity(new Intent(requireContext(), LoginActivity.class));
                });
            }
        }

        return view;
    }

    private String getTimeAgo(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            long time = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            long hours = diff / (60 * 60 * 1000);
            if (hours < 24) {
                return hours + " hours ago";
            } else {
                long days = hours / 24;
                return days + " days ago";
            }
        } catch (Exception e) {
            return "recently";
        }
    }

    private void updateLikeButton() {
        if (isLiked) {
            likeButton.setImageDrawable(requireContext().getDrawable(R.drawable.ic_heart_filled));
        } else {
            likeButton.setImageDrawable(requireContext().getDrawable(R.drawable.ic_heart_outline));
        }
    }

    private String capitalizeDishName(String dishName) {
        if (dishName == null || dishName.isEmpty()) {
            return "";
        }
        String[] words = dishName.toLowerCase().split("\\s+");
        StringBuilder capitalizedName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalizedName.toString().trim();
    }
} 