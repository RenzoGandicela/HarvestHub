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

        listingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : userSnapshot.child("items").getChildren()) {
                        try {
                            String title = itemSnapshot.child("title").getValue(String.class);
                            Boolean halal = itemSnapshot.child("halal").getValue(Boolean.class);
                            Boolean spicy = itemSnapshot.child("spicy").getValue(Boolean.class);
                            List<String> ingredients = new ArrayList<>();
                            DataSnapshot ingredientsSnapshot = itemSnapshot.child("ingredients");
                            if (ingredientsSnapshot.exists()) {
                                for (DataSnapshot ingredient : ingredientsSnapshot.getChildren()) {
                                    ingredients.add(ingredient.getValue(String.class));
                                }
                            }
                            String imageUrl = itemSnapshot.child("imageUrl").getValue(String.class);

                            FoodItem food = new FoodItem(title, 
                                                       halal != null && halal,
                                                       spicy != null && spicy,
                                                       ingredients);
                            food.setImageURL(imageUrl);
                            foodList.add(food);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing food item: " + e.getMessage());
                        }
                    }
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
} 