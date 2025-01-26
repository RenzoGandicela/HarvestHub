package com.sp.splashscreen2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.sp.splashscreen2.HelperClasses.FeaturedAdapter;
import com.sp.splashscreen2.HelperClasses.FeaturedHelperClass;

import java.util.ArrayList;

public class Announcements extends AppCompatActivity {

    private TextView eventDetails;
    private Button remindButton;
    RecyclerView featuredRecycler;
    private FeaturedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        // Initialize UI elements
        featuredRecycler = findViewById(R.id.featured_recycler);

        featuredRecycler();


    }

    private void featuredRecycler(){

        featuredRecycler.setHasFixedSize(true); //loads views only visible to users
        featuredRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ArrayList<FeaturedHelperClass> featuredLocations = new ArrayList<>();

        featuredLocations.add(new FeaturedHelperClass(R.drawable.food_sharing, "Food Sharing Event @Dover", "Join us for a vibrant food-sharing event at Dover, where community members come together to share delicious dishes.", "\uD83D\uDCCD Dover", "\uD83D\uDCC5 Date: 10/10/2025\\n⏰ Time: 6pm\\n\uD83D\uDCCD Location: Dover Community Hall"));
        featuredLocations.add(new FeaturedHelperClass(R.drawable.food_sharing, "Food Sharing Event @Dover", "Join us for a vibrant food-sharing event at Dover, where community members come together to share delicious dishes.", "\uD83D\uDCCD Dover", "\uD83D\uDCC5 Date: 10/10/2025\\n⏰ Time: 6pm\\n\uD83D\uDCCD Location: Dover Community Hall"));
        featuredLocations.add(new FeaturedHelperClass(R.drawable.food_sharing, "Food Sharing Event @Dover", "Join us for a vibrant food-sharing event at Dover, where community members come together to share delicious dishes.", "\uD83D\uDCCD Dover", "\uD83D\uDCC5 Date: 10/10/2025\\n⏰ Time: 6pm\\n\uD83D\uDCCD Location: Dover Community Hall"));

        adapter = new FeaturedAdapter(this, featuredLocations);
        featuredRecycler.setAdapter(adapter);
    }
}