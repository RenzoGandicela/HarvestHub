package com.sp.harvesthub.foodListings;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
<<<<<<< HEAD
=======
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
>>>>>>> renzo
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
<<<<<<< HEAD

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
=======
import android.widget.Toast;
import androidx.annotation.NonNull;
>>>>>>> renzo
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
<<<<<<< HEAD

import com.google.android.material.bottomnavigation.BottomNavigationView;
=======
import androidx.core.widget.NestedScrollView;
>>>>>>> renzo
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.nav_fragment.LogMealFragment;
<<<<<<< HEAD
=======
import com.google.android.material.bottomnavigation.BottomNavigationView;
>>>>>>> renzo

import java.util.ArrayList;
import java.util.List;

public class FoodFragment extends Fragment {
    private static final String TAG = "FoodFragment";
    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodList;
    private EditText searchEditText;
    private ImageButton filterButton;
    private List<FoodItem> allFoodItems; // Store all items
    private boolean isHalalChecked = false;
    private boolean isSpicyChecked = false;
    private boolean isAvailableChecked = false;
    private NestedScrollView scrollView;
    private FloatingActionButton scrollToTopButton;
    private BottomNavigationView bottomNavigationView;
    private Button startSharingButton;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_recyclerview_layout, container, false);
        
        // Initialize views
        searchEditText = view.findViewById(R.id.searchEditText);
        filterButton = view.findViewById(R.id.filterButton);
        recyclerView = view.findViewById(R.id.recyclerView);
        
        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        allFoodItems = new ArrayList<>(); // Initialize allFoodItems
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(requireContext(), foodList);
        recyclerView.setAdapter(foodAdapter);

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterFoodItems(s.toString());
            }
        });

        // Setup filter button
        filterButton.setOnClickListener(v -> showFilterDialog());

        // Set welcome text with username
        TextView welcomeText = view.findViewById(R.id.welcomeText);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                    .getReference("Users")
                    .child(auth.getCurrentUser().getUid());
            
            userRef.child("username").get().addOnSuccessListener(snapshot -> {
                String username = snapshot.getValue(String.class);
                if (username != null) {
                    welcomeText.setText("Hello, @" + username);
                }
            });
        }

        // Initialize scroll view and button
        scrollView = view.findViewById(R.id.scrollView);
        scrollToTopButton = view.findViewById(R.id.scrollToTopButton);
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);

        // Set up scroll listener
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) 
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > 500) {
                    scrollToTopButton.show();
                } else {
                    scrollToTopButton.hide();
                }
        });

        // Set up scroll to top button
        scrollToTopButton.setOnClickListener(v -> {
            scrollView.smoothScrollTo(0, 0);
        });

        // Set up start sharing button
        startSharingButton = view.findViewById(R.id.startSharingButton);
        auth = FirebaseAuth.getInstance();

        // Check user role and update button visibility
        checkUserRoleAndUpdateUI();

        fetchListingsData();

        return view;
    }

    private void filterFoodItems(String query) {
        List<FoodItem> filteredList = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            if (item.getDishName().toLowerCase().contains(query.toLowerCase()) ||
                item.getLocation().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        foodList.clear();
        foodList.addAll(filteredList);
        foodAdapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.filter_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CheckBox halalFilter = dialog.findViewById(R.id.filterHalal);
        CheckBox spicyFilter = dialog.findViewById(R.id.filterSpicy);
        CheckBox availableFilter = dialog.findViewById(R.id.filterAvailable);

        // Restore checked states
        halalFilter.setChecked(isHalalChecked);
        spicyFilter.setChecked(isSpicyChecked);
        availableFilter.setChecked(isAvailableChecked);

        // Set checkbox change listeners for immediate filtering
        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> {
            // Update stored states
            isHalalChecked = halalFilter.isChecked();
            isSpicyChecked = spicyFilter.isChecked();
            isAvailableChecked = availableFilter.isChecked();
            
            applyFilters(isHalalChecked, isSpicyChecked, isAvailableChecked);
        };

        halalFilter.setOnCheckedChangeListener(filterListener);
        spicyFilter.setOnCheckedChangeListener(filterListener);
        availableFilter.setOnCheckedChangeListener(filterListener);

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void applyFilters(boolean halal, boolean spicy, boolean available) {
        List<FoodItem> filteredList = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            FoodItemExtended extendedItem = (FoodItemExtended) item;
            boolean matches = true;
            
            if (halal && !extendedItem.isHalal()) {
                matches = false;
            }
            if (spicy && !extendedItem.isSpicy()) {
                matches = false;
            }
            if (available) {
                // Debug log to check status
                Log.d(TAG, "Item: " + extendedItem.getDishName() + " Status: " + extendedItem.getStatus());
                
                // Check if status is "available" (case-insensitive)
                String status = extendedItem.getStatus();
                if (status == null || !status.equalsIgnoreCase("available")) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredList.add(item);
            }
        }
        
        foodList.clear();
        foodList.addAll(filteredList);
        foodAdapter.notifyDataSetChanged();
    }

    private void fetchListingsData() {
        DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings");
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("Users");

        listingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFoodItems.clear();
                foodList.clear();
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    
                    usersRef.child(userId).child("username").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String username = task.getResult().getValue(String.class);
                            
                            DataSnapshot itemsSnapshot = userSnapshot.child("items");
                            if (!itemsSnapshot.exists()) {
                                return;
                            }

                            for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                                try {
                                    // Create new food item
                                    FoodItemExtended foodItem = new FoodItemExtended();
                                    
                                    // Set basic info
                                    foodItem.setItemId(itemSnapshot.getKey());
                                    foodItem.setOriginalSellerId(userId);
                                    foodItem.setSellerId(username != null ? username : userId);
                                    
                                    // Get all values safely
                                    foodItem.setDishName(getStringValue(itemSnapshot, "title"));
                                    foodItem.setDescription(getStringValue(itemSnapshot, "description"));
                                    foodItem.setLocation(getStringValue(itemSnapshot, "location"));
                                    foodItem.setExpirationDate(getStringValue(itemSnapshot, "expiryDate"));
                                    foodItem.setImageUrl(getStringValue(itemSnapshot, "imageUrl"));
                                    foodItem.setCreatedAt(getStringValue(itemSnapshot, "createdAt"));
                                    foodItem.setUpdatedAt(getStringValue(itemSnapshot, "updatedAt"));
                                    foodItem.setStatus(getStringValue(itemSnapshot, "status"));
                                    
                                    // Handle quantity
                                    Object quantityObj = itemSnapshot.child("quantity").getValue();
                                    if (quantityObj != null) {
                                        String quantity = String.valueOf(quantityObj);
                                        foodItem.setQuantity(quantity);
                                    } else {
                                        foodItem.setQuantity("0");
                                    }
                                    
                                    // Handle boolean values
                                    foodItem.setHalal(getBooleanValue(itemSnapshot, "halal"));
                                    foodItem.setSpicy(getBooleanValue(itemSnapshot, "spicy"));
                                    
                                    // Handle ingredients
                                    List<String> ingredients = new ArrayList<>();
                                    DataSnapshot ingredientsSnapshot = itemSnapshot.child("ingredients");
                                    if (ingredientsSnapshot.exists()) {
                                        for (DataSnapshot ingredient : ingredientsSnapshot.getChildren()) {
                                            String value = ingredient.getValue(String.class);
                                            if (value != null) {
                                                ingredients.add(value);
                                            }
                                        }
                                    }
                                    foodItem.setIngredients(ingredients);
                                    
                                    // Handle likes count
                                    DataSnapshot likedBySnapshot = itemSnapshot.child("likedBy");
                                    if (likedBySnapshot.exists()) {
                                        foodItem.setLikesCount((int) likedBySnapshot.getChildrenCount());
                                    } else {
                                        Object likesObj = itemSnapshot.child("likes").getValue();
                                        int likesCount = 0;
                                        if (likesObj instanceof Long) {
                                            likesCount = ((Long) likesObj).intValue();
                                        } else if (likesObj instanceof Integer) {
                                            likesCount = (Integer) likesObj;
                                        }
                                        foodItem.setLikesCount(likesCount);
                                    }

                                    allFoodItems.add(foodItem);
                                    foodList.add(foodItem);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing food item: " + e.getMessage(), e);
                                }
                            }
                            foodAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    // Helper methods for safe value extraction
    private String getStringValue(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value != null ? String.valueOf(value) : "";
    }

    private boolean getBooleanValue(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    private void checkUserRoleAndUpdateUI() {
        if (auth.getCurrentUser() != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                    .getReference("Users")
                    .child(auth.getCurrentUser().getUid());
            
            userRef.child("role").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String role = task.getResult().getValue(String.class);
                    Log.d(TAG, "User role for UI update: " + role); // Debug log
                    
                    // Show start sharing button only for sellers
                    if (startSharingButton != null) {
                        boolean isSeller = "seller".equalsIgnoreCase(role);
                        startSharingButton.setVisibility(isSeller ? View.VISIBLE : View.GONE);

                        // Set click listener only if button is visible
                        if (isSeller) {
                            startSharingButton.setOnClickListener(v -> {
                                FragmentTransaction transaction = requireActivity()
                                    .getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new LogMealFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                                
                                bottomNavigationView.setSelectedItemId(R.id.nav_add);
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only check role if the user is logged in
        if (auth.getCurrentUser() != null) {
            checkUserRoleAndUpdateUI();
        }
    }
} 