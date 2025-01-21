package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sp.harvesthub.R;
import com.sp.harvesthub.activities.BlinkViewerActivity;
import com.sp.harvesthub.models.Blink;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class BlinkAdapter extends RecyclerView.Adapter<BlinkAdapter.ViewHolder> {
    private Context context;
    private List<Blink> blinksList;

    public BlinkAdapter(Context context, List<Blink> blinksList) {
        this.context = context;
        this.blinksList = blinksList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blink, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Blink blink = blinksList.get(position);
        
        // Set name or userId as display name
        final String displayName = blink.getName() != null ? blink.getName() : blink.getUserId();
        final String shortDisplayName = displayName != null && displayName.length() > 10 ? 
            displayName.substring(0, 10) + "..." : displayName;
        
        holder.usernameTxt.setText(shortDisplayName);
        
        // Load blink image with Glide
        if (blink.getImageURL() != null && !blink.getImageURL().isEmpty()) {
            Glide.with(context)
                .load(blink.getImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(holder.blinkImage);
        } else {
            holder.blinkImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BlinkViewerActivity.class);
            intent.putExtra("imageURL", blink.getImageURL());
            intent.putExtra("username", shortDisplayName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return blinksList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView blinkImage;
        TextView usernameTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            blinkImage = itemView.findViewById(R.id.blinkImage);
            usernameTxt = itemView.findViewById(R.id.usernameTxt);
        }
    }
} 