package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sp.harvesthub.R;
import com.sp.harvesthub.activities.DashboardActivity;
import com.sp.harvesthub.models.FeaturedHelperClass;
import java.util.ArrayList;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder> {

    private final ArrayList<FeaturedHelperClass> featuredLocations;
    private final Context context;

    public FeaturedAdapter(Context context, ArrayList<FeaturedHelperClass> featuredLocations) {
        this.context = context;
        this.featuredLocations = featuredLocations;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.featured_card_design, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        FeaturedHelperClass helperClass = featuredLocations.get(position);

        try {
            // Load image using Glide
            Glide.with(context)
                .load(helperClass.getImage())
                .apply(new RequestOptions()
                    .override(800, 600) // Resize image
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop())
                .into(holder.image);

            holder.title.setText(helperClass.getTitle());
            holder.description.setText(helperClass.getDescription());
            holder.location.setText(helperClass.getLocation());
            holder.details.setText(helperClass.getDetails());

            holder.remindBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, DashboardActivity.class);
                intent.putExtra("eventDetails", String.format("%s\n\n%s\n\n%s\n%s",
                    helperClass.getTitle(),
                    helperClass.getDescription(),
                    helperClass.getLocation(),
                    helperClass.getDetails()
                ));
                context.startActivity(intent);
                Toast.makeText(context, "Event added to calendar", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error loading event details", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return featuredLocations.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description, location, details;
        Button remindBtn;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            description = itemView.findViewById(R.id.featured_description);
            location = itemView.findViewById(R.id.featured_location);
            details = itemView.findViewById(R.id.featured_details);
            remindBtn = itemView.findViewById(R.id.remindButton);
        }
    }
} 