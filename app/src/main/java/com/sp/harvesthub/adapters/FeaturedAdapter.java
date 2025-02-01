package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.harvesthub.R;
import com.sp.harvesthub.activities.DashboardActivity;
import com.sp.harvesthub.models.FeaturedHelperClass;

import java.util.ArrayList;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder> {

    private ArrayList<FeaturedHelperClass> featuredLocations;
    private Context context;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onRemindClick(FeaturedHelperClass event);
    }

    public FeaturedAdapter(Context context, ArrayList<FeaturedHelperClass> featuredLocations) {
        this.context = context;
        this.featuredLocations = featuredLocations != null ? featuredLocations : new ArrayList<>();
        
        // Try to cast context to listener if it implements the interface
        if (context instanceof OnEventClickListener) {
            this.listener = (OnEventClickListener) context;
        }
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.featured_card_design, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        FeaturedHelperClass featuredHelperClass = featuredLocations.get(position);
        
        holder.title.setText(featuredHelperClass.getTitle());
        holder.description.setText(featuredHelperClass.getDescription());
        holder.location.setText(featuredHelperClass.getLocation());
        holder.details.setText(featuredHelperClass.getDetails());
        
        // Apply different styles based on event type
        if (featuredHelperClass.getEventType() == 1) {
            // Regular event styling
            holder.title.setTextColor(context.getResources().getColor(R.color.teal_700));
            holder.cardView.setBackgroundResource(R.drawable.white_box);
        } else if (featuredHelperClass.getEventType() == 2) {
            // Donation drive styling
            holder.title.setTextColor(context.getResources().getColor(R.color.teal_200));
            holder.cardView.setBackgroundResource(R.drawable.teal_round);
        }

        // Update reminder functionality
        holder.remindBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemindClick(featuredHelperClass);
            }
        });
    }

    @Override
    public int getItemCount() {
        return featuredLocations.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        TextView image;
        TextView title, description, location, details;
        Button remindBtn;
        CardView cardView;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            description = itemView.findViewById(R.id.featured_description);
            location = itemView.findViewById(R.id.featured_location);
            details = itemView.findViewById(R.id.featured_details);
            remindBtn = itemView.findViewById(R.id.remindButton);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}