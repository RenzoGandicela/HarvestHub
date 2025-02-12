package com.sp.harvesthub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sp.harvesthub.R;
import com.sp.harvesthub.foodListings.FoodItemExtended;
import com.sp.harvesthub.nav_fragment.BookmarkDetailsFragment;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {
    private Context context;
    private List<FoodItemExtended> bookmarkList;

    public BookmarkAdapter(Context context, List<FoodItemExtended> bookmarkList) {
        this.context = context;
        this.bookmarkList = bookmarkList;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_card, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        FoodItemExtended foodItem = bookmarkList.get(position);
        holder.foodName.setText(capitalizeDishName(foodItem.getDishName()));
        
        // Handle tag visibility
        holder.halalTag.setVisibility(foodItem.isHalal() ? View.VISIBLE : View.GONE);
        holder.spicyTag.setVisibility(foodItem.isSpicy() ? View.VISIBLE : View.GONE);
        
        // Set location
        if (foodItem.getLocation() != null && !foodItem.getLocation().isEmpty()) {
            String location = foodItem.getLocation();
            location = location.substring(0, 1).toUpperCase() + location.substring(1);
            holder.locationText.setText(location);
            holder.locationText.setVisibility(View.VISIBLE);
        } else {
            holder.locationText.setVisibility(View.GONE);
        }

        // Load image
        if (foodItem.getImageUrl() != null && !foodItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(foodItem.getImageUrl())
                    .into(holder.foodImage);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                BookmarkDetailsFragment detailsFragment = BookmarkDetailsFragment.newInstance(foodItem);
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    public void updateItems(List<FoodItemExtended> newItems) {
        this.bookmarkList = newItems;
        notifyDataSetChanged();
    }

    static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName;
        LinearLayout halalTag, spicyTag;
        TextView locationText;

        BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            halalTag = itemView.findViewById(R.id.halalTag);
            spicyTag = itemView.findViewById(R.id.spicyTag);
            locationText = itemView.findViewById(R.id.locationText);
        }
    }

    private String capitalizeDishName(String dishName) {
        if (dishName == null || dishName.isEmpty()) return "";
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