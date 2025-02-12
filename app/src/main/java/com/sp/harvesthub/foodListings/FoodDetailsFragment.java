package com.sp.harvesthub.foodListings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.List;
import java.util.Locale;

public class FoodDetailsFragment extends Fragment {
    private static final String ARG_FOOD_ITEM = "food_item";
    private static final String TAG = "FoodDetailsFragment";
    private FoodItemExtended foodItem;
    private ImageButton likeButton;
    private TextView likeCountText;
    private FirebaseAuth auth;
    private DatabaseReference listingsRef;
    private boolean isLiked = false;
    private Menu optionsMenu;
    private boolean isUserSeller = false;

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
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            foodItem = (FoodItemExtended) getArguments().getSerializable(ARG_FOOD_ITEM);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        this.optionsMenu = menu;
        
        // Hide menu item by default
        MenuItem editItem = menu.findItem(R.id.editListing);
        if (editItem != null) {
            editItem.setVisible(false);
        }
        
        // Check user role immediately
        checkUserRole();
        super.onCreateOptionsMenu(menu, inflater);
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
            
            // Update quantity/status display
            if ("claimed".equalsIgnoreCase(foodItem.getStatus())) {
                quantityText.setText("Unavailable");
                quantityText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                String quantity = foodItem.getQuantity();
                if (quantity != null && !quantity.isEmpty()) {
                    quantityText.setText("Available Quantity: " + quantity);
                    quantityText.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    quantityText.setText("Available Quantity: 0");
                    quantityText.setTextColor(getResources().getColor(android.R.color.black));
                }
            }

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

            // Set ingredients
            TextView ingredientsListText = view.findViewById(R.id.ingredientsListText);
            List<String> ingredients = foodItem.getIngredients();
            if (ingredients != null && !ingredients.isEmpty()) {
                ingredientsListText.setText("Ingredients: " + String.join(", ", ingredients));
            } else {
                ingredientsListText.setText("Ingredients: Not specified");
            }

            // Initialize likes count from likedBy node
            DatabaseReference itemRef = listingsRef
                .child(foodItem.getOriginalSellerId())
                .child("items")
                .child(foodItem.getItemId());

            itemRef.child("likedBy").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded()) return;
                    
                    if (snapshot.exists()) {
                        long likeCount = snapshot.getChildrenCount();
                        likeCountText.setText(String.valueOf(likeCount));
                    } else {
                        likeCountText.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching likes count: " + error.getMessage());
                }
            });

            // Handle likes for logged-in users
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                DatabaseReference likedByRef = itemRef.child("likedBy").child(userId);

                likedByRef.get().addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    isLiked = snapshot.exists();
                    updateLikeButton();
                });

                likeButton.setOnClickListener(v -> {
                    if (!isAdded()) return;

                    if (isLiked) {
                        // Unlike
                        likedByRef.removeValue().addOnSuccessListener(aVoid -> {
                            if (!isAdded()) return;
                            isLiked = false;
                            updateLikeButton();
                        });
                    } else {
                        // Like
                        likedByRef.setValue(true).addOnSuccessListener(aVoid -> {
                            if (!isAdded()) return;
                            isLiked = true;
                            updateLikeButton();
                        });
                    }
                });
            } else {
                // User not logged in
                likeButton.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Please login to like items", Toast.LENGTH_SHORT).show();
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
        // Check if fragment is attached before proceeding
        if (!isAdded()) {
            return;
        }

        try {
            if (isLiked) {
                likeButton.setImageDrawable(requireContext().getDrawable(R.drawable.ic_heart_filled));
            } else {
                likeButton.setImageDrawable(requireContext().getDrawable(R.drawable.ic_heart_outline));
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to update like button: Fragment not attached", e);
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

    private void checkUserRole() {
        if (auth.getCurrentUser() != null && foodItem != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            
            // Check if current user is the original seller of this item
            boolean isOwner = currentUserId.equals(foodItem.getOriginalSellerId());
            
            // Show edit button if user is the owner
            if (optionsMenu != null) {
                MenuItem editItem = optionsMenu.findItem(R.id.editListing);
                if (editItem != null) {
                    editItem.setVisible(isOwner);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.editListing) {
            editListing();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editListing() {
        Intent intent = new Intent(getActivity(), EditFoodActivity.class);
        intent.putExtra("foodItem", foodItem);
        startActivity(intent);
    }
} 