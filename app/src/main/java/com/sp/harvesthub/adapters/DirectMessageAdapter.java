package com.sp.harvesthub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sp.harvesthub.R;
import com.sp.harvesthub.models.DirectMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DirectMessageAdapter extends RecyclerView.Adapter<DirectMessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<DirectMessage> messages;
    private String currentUserId;

    public DirectMessageAdapter(Context context, List<DirectMessage> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        DirectMessage message = messages.get(position);

        // Set message text
        if (message.getContent() != null && !message.getContent().isEmpty()) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getContent());
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Set image if exists
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(message.getImageUrl())
                .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }

        // Set timestamp
        if (message.getTimestamp() > 0) {
            holder.timestamp.setText(formatTimestamp(message.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        DirectMessage message = messages.get(position);
        String senderId = message.getSenderId();
        
        // Default to received message if senderId is null or currentUserId is null
        if (senderId == null || currentUserId == null) {
            return VIEW_TYPE_RECEIVED;
        }
        
        return senderId.equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        TextView timestamp;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
} 