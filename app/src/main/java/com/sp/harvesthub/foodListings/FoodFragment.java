package com.sp.harvesthub.foodListings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import java.util.ArrayList;
import java.util.List;

public class FoodFragment extends Fragment {
    private static final String TAG = "FoodFragment";
    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_recyclerview_layout, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(requireContext(), foodList);
        recyclerView.setAdapter(foodAdapter);

        fetchListingsData();

        return view;
    }

    private void fetchListingsData() {
        DatabaseReference listingsRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("listings");
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://splashcreen2-default-rtdb.firebaseio.com/")
                .getReference("Users");

        listingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
} 