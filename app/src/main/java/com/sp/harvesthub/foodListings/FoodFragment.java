package com.sp.harvesthub.foodListings;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.nav_fragment.LogMealFragment;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
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

        fetchListingsData();

        Button startSharingButton = view.findViewById(R.id.startSharingButton);
        startSharingButton.setOnClickListener(v -> {
            // Navigate to LogMealFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LogMealFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

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
            if (available && !"available".equals(extendedItem.getStatus())) {
                matches = false;
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
                allFoodItems.clear(); // Clear both lists
                foodList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    
                    // Fetch username for this user
                    usersRef.child(userId).child("username").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String username = task.getResult().getValue(String.class);
                            
                            // Now process the food items with the username
                            for (DataSnapshot itemSnapshot : userSnapshot.child("items").getChildren()) {
                                try {
                                    FoodItemExtended foodItem = new FoodItemExtended();
                                    foodItem.setItemId(itemSnapshot.getKey());
                                    
                                    // Store both the username for display and original sellerId for database operations
                                    foodItem.setOriginalSellerId(userId); // Store original seller ID
                                    foodItem.setSellerId(username != null ? username : userId); // Use username for display
                                    
                                    // Safely get boolean values with default false
                                    Boolean halal = itemSnapshot.child("halal").getValue(Boolean.class);
                                    Boolean spicy = itemSnapshot.child("spicy").getValue(Boolean.class);
                                    foodItem.setHalal(halal != null ? halal : false);
                                    foodItem.setSpicy(spicy != null ? spicy : false);
                                    
                                    // Map other fields
                                    foodItem.setDishName(itemSnapshot.child("title").getValue(String.class));
                                    foodItem.setLocation(itemSnapshot.child("location").getValue(String.class));
                                    foodItem.setExpirationDate(itemSnapshot.child("expiryDate").getValue(String.class));
                                    foodItem.setQuantity(itemSnapshot.child("quantity").getValue(String.class));
                                    foodItem.setDescription(itemSnapshot.child("description").getValue(String.class));
                                    foodItem.setImageUrl(itemSnapshot.child("imageUrl").getValue(String.class));
                                    foodItem.setCreatedAt(itemSnapshot.child("createdAt").getValue(String.class));
                                    foodItem.setUpdatedAt(itemSnapshot.child("updatedAt").getValue(String.class));
                                    
                                    // Get likes count
                                    DataSnapshot likedBySnapshot = itemSnapshot.child("likedBy");
                                    if (likedBySnapshot.exists()) {
                                        foodItem.setLikesCount((int) likedBySnapshot.getChildrenCount());
                                    }

                                    // Handle ingredients list
                                    List<String> ingredients = new ArrayList<>();
                                    DataSnapshot ingredientsSnapshot = itemSnapshot.child("ingredients");
                                    if (ingredientsSnapshot.exists()) {
                                        for (DataSnapshot ingredient : ingredientsSnapshot.getChildren()) {
                                            ingredients.add(ingredient.getValue(String.class));
                                        }
                                    }
                                    foodItem.setIngredients(ingredients);

                                    allFoodItems.add(foodItem); // Add to both lists
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.food) {
            // Replace current fragment with FoodFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FoodFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        } else if (item.getItemId() == R.id.logMeal) {
            // Replace current fragment with LogMealFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new LogMealFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 