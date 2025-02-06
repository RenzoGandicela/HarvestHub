package com.sp.harvesthub.foodListings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodAPI.LogMealActivity;

import java.util.ArrayList;
import java.util.List;

public class FoodActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_recyclerview_layout);

        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);

        foodAdapter.setOnItemClickListener(new FoodAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FoodItem foodItem) {
                // Handle item click if needed
            }
        });

        fetchFoodData();
    }

    private void fetchFoodData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("foods");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    FoodItem food = foodSnapshot.getValue(FoodItem.class);
                    if (food != null) {
                        food.setDishName(foodSnapshot.getKey());
                        food.setImageURL(foodSnapshot.child("imageURL").getValue(String.class));
                        foodList.add(food);
                    }
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logMeal) {
            Intent intent = new Intent(this, LogMealActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.food) {
            Intent intent = new Intent(this, FoodActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}