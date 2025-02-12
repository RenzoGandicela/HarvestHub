package com.sp.harvesthub.foodListings;

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
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private Context context;
    private List<FoodItem> foodList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodItem foodItem);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FoodAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_card, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItemExtended foodItem = (FoodItemExtended) foodList.get(position);
        holder.foodName.setText(capitalizeDishName(foodItem.getDishName()));
        
        // Handle tag visibility
        if (foodItem.isHalal() || foodItem.isSpicy()) {
            holder.halalTag.setVisibility(foodItem.isHalal() ? View.VISIBLE : View.GONE);
            holder.spicyTag.setVisibility(foodItem.isSpicy() ? View.VISIBLE : View.GONE);
        } else {
            holder.halalTag.setVisibility(View.INVISIBLE);
            holder.spicyTag.setVisibility(View.INVISIBLE);
        }
        
        // Set location text with capitalization
        if (foodItem.getLocation() != null && !foodItem.getLocation().isEmpty()) {
            String location = foodItem.getLocation();
            location = location.substring(0, 1).toUpperCase() + location.substring(1);
            holder.locationText.setText(location);
            holder.locationText.setVisibility(View.VISIBLE);
        } else {
            holder.locationText.setVisibility(View.GONE);
        }

        // Load image
        String imageUrl = foodItem.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.foodImage);
        }

        // Change overlay color based on status
        LinearLayout overlay = holder.itemView.findViewById(R.id.foodOverlay);
        if ("claimed".equalsIgnoreCase(foodItem.getStatus())) {
            overlay.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.claimed_overlay));
        } else {
            overlay.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.available_overlay));
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                FoodDetailsFragment detailsFragment = FoodDetailsFragment.newInstance(foodItem);
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
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName;
        LinearLayout halalTag, spicyTag;
        TextView locationText;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            halalTag = itemView.findViewById(R.id.halalTag);
            spicyTag = itemView.findViewById(R.id.spicyTag);
            locationText = itemView.findViewById(R.id.locationText);
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