package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sp.harvesthub.R;
import com.sp.harvesthub.activities.ServerActivity;
import com.sp.harvesthub.models.Server;
import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {
    private Context context;
    private List<Server> serversList;

    public ServerAdapter(Context context, List<Server> serversList) {
        this.context = context;
        this.serversList = serversList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_server, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Server server = serversList.get(position);
        
        // Set server name
        holder.serverNameTxt.setText(server.getName());
        
        // Set server description
        if (server.getDescription() != null && !server.getDescription().isEmpty()) {
            holder.serverDescriptionTxt.setVisibility(View.VISIBLE);
            holder.serverDescriptionTxt.setText(server.getDescription());
        } else {
            holder.serverDescriptionTxt.setVisibility(View.GONE);
        }
        
        // Load server icon
        if (server.getIconURL() != null && !server.getIconURL().isEmpty()) {
            Glide.with(context)
                .load(server.getIconURL())
                .placeholder(R.drawable.ic_server_placeholder)
                .error(R.drawable.ic_server_placeholder)
                .into(holder.serverIconImg);
        } else {
            holder.serverIconImg.setImageResource(R.drawable.ic_server_placeholder);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ServerActivity.class);
            intent.putExtra("serverId", server.getId());
            intent.putExtra("serverName", server.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return serversList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serverIconImg;
        TextView serverNameTxt;
        TextView serverDescriptionTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serverIconImg = itemView.findViewById(R.id.serverIconImg);
            serverNameTxt = itemView.findViewById(R.id.serverNameTxt);
            serverDescriptionTxt = itemView.findViewById(R.id.serverDescriptionTxt);
        }
    }
} 