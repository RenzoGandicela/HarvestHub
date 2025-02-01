package com.sp.harvesthub.nav_fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sp.harvesthub.R;
import com.sp.harvesthub.adapters.FeaturedAdapter;
import com.sp.harvesthub.models.FeaturedHelperClass;
import java.util.ArrayList;

public class AnnouncementFragment extends Fragment {
    private static final String TAG = "AnnouncementFragment";
    private RecyclerView featuredRecycler;
    private FeaturedAdapter adapter;
    private ArrayList<FeaturedHelperClass> featuredLocations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        featuredLocations = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);
        
        try {
            initializeRecyclerView(view);
            setupFeaturedItems();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up announcement view", e);
        }
        
        return view;
    }

    private void initializeRecyclerView(View view) {
        featuredRecycler = view.findViewById(R.id.featured_recycler);
        featuredRecycler.setLayoutManager(new LinearLayoutManager(getContext(), 
            LinearLayoutManager.HORIZONTAL, false));
        featuredRecycler.setHasFixedSize(true);
    }

    private void setupFeaturedItems() {
        // Clear existing items to prevent duplicates
        featuredLocations.clear();

        // Add featured items
        featuredLocations.add(new FeaturedHelperClass(
            R.drawable.food_sharing, 
            "Food Sharing Event @Dover",
            "Join us for a vibrant food-sharing event at Dover Community Hub! Share your homemade dishes, learn new recipes, and connect with fellow food enthusiasts.",
            "\uD83D\uDCCD Dover Community Hub",
            "\uD83D\uDCC5 Date: 10/10/2025\n⏰ Time: 6:00 PM - 9:00 PM"
        ));

        featuredLocations.add(new FeaturedHelperClass(
            R.drawable.community_garden, 
            "Urban Farming Workshop",
            "Learn essential urban farming techniques, sustainable practices, and how to grow your own vegetables in limited spaces.",
            "\uD83D\uDCCD Tampines Hub",
            "\uD83D\uDCC5 Date: 15/10/2025\n⏰ Time: 9:00 AM - 12:00 PM"
        ));

        // Initialize adapter if needed
        if (adapter == null) {
            adapter = new FeaturedAdapter(requireContext(), featuredLocations);
            featuredRecycler.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        featuredRecycler.setAdapter(null);
        adapter = null;
    }
}