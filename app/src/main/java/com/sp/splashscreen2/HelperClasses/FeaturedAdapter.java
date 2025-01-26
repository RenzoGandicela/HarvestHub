package com.sp.splashscreen2.HelperClasses;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.splashscreen2.Dashboard;
import com.sp.splashscreen2.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.FeaturedViewHolder> {  //feature adapter will be text, feature view holder will be design

    ArrayList<FeaturedHelperClass> featuredLocations;
    private Context context;

    public FeaturedAdapter(Context context, ArrayList<FeaturedHelperClass> featuredLocations) {  //get data from here!
        this.featuredLocations = featuredLocations;
        this.context = context;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_card_design, parent, false);  //created view that is passed to the design
        FeaturedViewHolder featuredViewHolder = new FeaturedViewHolder(view);
        return featuredViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        FeaturedHelperClass featuredHelperClass = featuredLocations.get(position);

        holder.image.setImageResource(featuredHelperClass.getImage());
        holder.title.setText(featuredHelperClass.getTitle());
        holder.description.setText(featuredHelperClass.getDescription());
        holder.location.setText(featuredHelperClass.getLocation());
        holder.details.setText(featuredHelperClass.getDetails());
        holder.remindBtn.setOnClickListener(v -> {
            String eventDetails = featuredHelperClass.getDetails();
            Intent intent = new Intent(context, Dashboard.class);
            intent.putExtra("eventDetails", featuredHelperClass.getDetails());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return featuredLocations.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title, description, location, details;
        Button remindBtn;

        public FeaturedViewHolder(@NonNull View itemView) {  //subclass that holds views
            super(itemView);

            //initialise variables
            image = itemView.findViewById(R.id.featured_image);
            title = itemView.findViewById(R.id.featured_title);
            description = itemView.findViewById(R.id.featured_description);
            location = itemView.findViewById(R.id.featured_location);
            details = itemView.findViewById(R.id.featured_details);
            remindBtn = itemView.findViewById(R.id.remindButton);
        }
    }
}
