
package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        void onEventClick(FeaturedHelperClass event, int position);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
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

        // Load image using Glide
        Glide.with(context)
                .load(featuredHelperClass.getImage())
                .placeholder(R.color.secondary_dark_orange)
                .error(R.color.secondary_dark_orange)
                .into(holder.image);

        holder.title.setText("🎉 " + featuredHelperClass.getTitle());
        holder.description.setText("🍴 " + featuredHelperClass.getDescription());
        holder.date.setText("📅 " + featuredHelperClass.getDate());
        holder.time.setText("⏰ " + featuredHelperClass.getTime());
        holder.location.setText("📍 " + featuredHelperClass.getLocation());


        // Set text colors for better visibility
        holder.description.setTextColor(context.getResources().getColor(R.color.black));
        holder.location.setTextColor(context.getResources().getColor(R.color.gray));
        holder.date.setTextColor(context.getResources().getColor(R.color.black));
        holder.time.setTextColor(context.getResources().getColor(R.color.black));

        // Apply different styles based on event type
        if (featuredHelperClass.getEventType() == 1) {
            // Regular event styling
            holder.title.setTextColor(context.getResources().getColor(R.color.primary_dark_green));
            holder.cardView.setBackgroundResource(R.drawable.white_box);
        } else if (featuredHelperClass.getEventType() == 2) {
            // Donation drive styling
            holder.title.setTextColor(context.getResources().getColor(R.color.secondary_dark_orange));
            holder.cardView.setBackgroundResource(R.drawable.white_box2);
        }

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(featuredHelperClass, holder.getAdapterPosition());
            }
        });

        // Update reminder functionality
        holder.remindBtn.setOnClickListener(v -> {
            String eventDate = featuredHelperClass.getDate();
            String eventTime = featuredHelperClass.getTime();
            String eventTitle = featuredHelperClass.getTitle();
            String eventLocation = featuredHelperClass.getLocation();
            Intent intent = new Intent(context, DashboardActivity.class);
            intent.putExtra("eventTitle", eventTitle);
            intent.putExtra("eventLocation", eventLocation);
            intent.putExtra("eventDate", eventDate);
            intent.putExtra("eventTime", eventTime);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return featuredLocations.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description, location, date, time;
        Button remindBtn;
        CardView cardView;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            description = itemView.findViewById(R.id.featured_description);
            location = itemView.findViewById(R.id.featured_location);
            date = itemView.findViewById(R.id.featured_date);
            time = itemView.findViewById(R.id.featured_time);
            remindBtn = itemView.findViewById(R.id.remindButton);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}