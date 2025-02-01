package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.splashscreen2.Dashboard;
import com.sp.splashscreen2.R;

import java.util.ArrayList;

public class OtherAdapter extends RecyclerView.Adapter<OtherAdapter.OtherViewHolder> {

    private ArrayList<FeaturedHelperClass> otherLocations;
    private Context context;

    public OtherAdapter(Context context, ArrayList<FeaturedHelperClass> otherLocations) {
        this.context = context;
        if (otherLocations == null) {
            this.otherLocations = new ArrayList<>(); // Empty list initialization
        } else {
            this.otherLocations = otherLocations;
        }
    }

    @NonNull
    @Override
    public OtherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_card_design, parent, false);
        return new OtherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherViewHolder holder, int position) {
        FeaturedHelperClass featuredHelperClass = otherLocations.get(position);

        holder.image.setText(featuredHelperClass.getImage());
        holder.title.setText(featuredHelperClass.getTitle());
        holder.description.setText(featuredHelperClass.getDescription());
        holder.location.setText(featuredHelperClass.getLocation());
        holder.details.setText(featuredHelperClass.getDetails());

        // Handling based on event type
        if (featuredHelperClass.getEventType() == 1) {
            // Type 1: Standard event handling (if necessary, customize as per your design)
            holder.title.setTextColor(context.getResources().getColor(R.color.teal_700));
        } else if (featuredHelperClass.getEventType() == 2) {
            // Type 2: Other event handling (if necessary, customize as per your design)
            holder.title.setTextColor(context.getResources().getColor(R.color.teal_200));
        }

        holder.remindBtn.setOnClickListener(v -> {
            String eventDetails = featuredHelperClass.getDetails();
            Intent intent = new Intent(context, Dashboard.class);
            intent.putExtra("eventDetails", eventDetails);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return otherLocations.size();
    }

    public static class OtherViewHolder extends RecyclerView.ViewHolder {

        TextView image;
        TextView title, description, location, details;
        Button remindBtn;

        public OtherViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize variables
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            description = itemView.findViewById(R.id.featured_description);
            location = itemView.findViewById(R.id.featured_location);
            details = itemView.findViewById(R.id.featured_details);
            remindBtn = itemView.findViewById(R.id.remindButton);
        }
    }
}