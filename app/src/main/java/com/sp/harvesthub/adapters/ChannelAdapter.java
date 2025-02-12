package com.sp.harvesthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sp.harvesthub.R;
<<<<<<< HEAD
import com.sp.harvesthub.activities.ChatActivity;
=======
import com.sp.harvesthub.activities.ChannelChatActivity;
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
import com.sp.harvesthub.models.Channel;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {
    private Context context;
    private List<Channel> channelsList;
    private String serverId;

    public ChannelAdapter(Context context, List<Channel> channelsList, String serverId) {
        this.context = context;
        this.channelsList = channelsList;
        this.serverId = serverId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Channel channel = channelsList.get(position);
        holder.channelNameTxt.setText(channel.getName());

        holder.itemView.setOnClickListener(v -> {
<<<<<<< HEAD
            Intent intent = new Intent(context, ChatActivity.class);
=======
            Intent intent = new Intent(context, ChannelChatActivity.class);
>>>>>>> 4e5a6c21f9b5bbb4a762c3f15e95787e4d05c8fb
            intent.putExtra("serverId", serverId);
            intent.putExtra("channelId", channel.getId());
            intent.putExtra("channelName", channel.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return channelsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView channelNameTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            channelNameTxt = itemView.findViewById(R.id.channelNameTxt);
        }
    }
} 