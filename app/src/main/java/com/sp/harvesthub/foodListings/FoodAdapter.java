package com.sp.harvesthub.foodListings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        FoodItem food = foodList.get(position);
        holder.foodName.setText(capitalizeDishName(food.getDishName()));
        holder.halalTag.setVisibility(food.isHalal() ? View.VISIBLE : View.GONE);
        holder.spicyTag.setVisibility(food.isSpicy() ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(food.getImageURL())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.foodImage);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName;
        LinearLayout halalTag, spicyTag;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            halalTag = itemView.findViewById(R.id.halalTag);
            spicyTag = itemView.findViewById(R.id.spicyTag);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(foodList.get(position));
                }
            });
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