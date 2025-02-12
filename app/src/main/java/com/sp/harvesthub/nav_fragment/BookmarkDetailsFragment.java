package com.sp.harvesthub.nav_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.sp.harvesthub.R;
import com.sp.harvesthub.database.BookmarkDbHelper;
import com.sp.harvesthub.foodListings.FoodItemExtended;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.MenuItem;
import com.sp.harvesthub.MainActivity;
import com.google.android.material.navigation.NavigationView;

public class BookmarkDetailsFragment extends Fragment {
    private static final String ARG_FOOD_ITEM = "food_item";
    private FoodItemExtended foodItem;
    private BookmarkDbHelper bookmarkDbHelper;
    private ImageButton bookmarkButton;

    public static BookmarkDetailsFragment newInstance(FoodItemExtended foodItem) {
        BookmarkDetailsFragment fragment = new BookmarkDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FOOD_ITEM, foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            foodItem = (FoodItemExtended) getArguments().getSerializable(ARG_FOOD_ITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_details, container, false);

        bookmarkDbHelper = new BookmarkDbHelper(requireContext());

        // Initialize views
        ImageView foodImage = view.findViewById(R.id.foodImage);
        TextView foodName = view.findViewById(R.id.foodName);
        TextView quantityText = view.findViewById(R.id.quantityText);
        TextView sellerIdText = view.findViewById(R.id.sellerIdText);
        TextView locationText = view.findViewById(R.id.locationText);
        TextView expiryDateText = view.findViewById(R.id.expiryDateText);
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        TextView uploadTimeText = view.findViewById(R.id.uploadTimeText);
        LinearLayout halalTag = view.findViewById(R.id.halalTag);
        LinearLayout spicyTag = view.findViewById(R.id.spicyTag);
        bookmarkButton = view.findViewById(R.id.bookmarkButton);
        Button undoButton = view.findViewById(R.id.undoButton);

        if (foodItem != null) {
            // Load image
            Glide.with(this)
                    .load(foodItem.getImageUrl())
                    .into(foodImage);

            // Set texts
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
                }
            }

            sellerIdText.setText("Supplied by: @" + foodItem.getSellerId());
            locationText.setText("Location: " + foodItem.getLocation());
            expiryDateText.setText("Expiry Date: " + foodItem.getExpirationDate());
            descriptionText.setText(foodItem.getDescription());

            if (foodItem.getCreatedAt() != null) {
                uploadTimeText.setText("Uploaded " + getTimeAgo(foodItem.getCreatedAt()));
            }

            // Handle tags visibility
            halalTag.setVisibility(foodItem.isHalal() ? View.VISIBLE : View.GONE);
            spicyTag.setVisibility(foodItem.isSpicy() ? View.VISIBLE : View.GONE);

            // Set bookmark button click listener
            bookmarkButton.setOnClickListener(v -> {
                bookmarkDbHelper.removeBookmark(foodItem.getItemId());
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        undoButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
                // Ensure BookmarkFragment is visible
                if (getActivity() instanceof MainActivity) {
                    NavigationView navigationView = ((MainActivity) getActivity())
                        .findViewById(R.id.nav_view);
                    if (navigationView != null) {
                        MenuItem bookmarkItem = navigationView.getMenu()
                            .findItem(R.id.nav_bookmark);
                        if (bookmarkItem != null) {
                            bookmarkItem.setChecked(true);
                        }
                    }
                }
            }
        });

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