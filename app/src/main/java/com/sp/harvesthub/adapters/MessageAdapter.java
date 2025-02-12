package com.sp.harvesthub.adapters;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.sp.harvesthub.R;
import com.sp.harvesthub.models.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        Log.d("MessageAdapter", "Current User ID: " + currentUserId);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_bubble, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        
        boolean isCurrentUser = false;
        if (message != null && message.getSenderId() != null && currentUserId != null) {
            isCurrentUser = message.getSenderId().trim().equals(currentUserId.trim());
        }

        // Configure layout based on sender
        if (isCurrentUser) {
            // Current user's message (right side)
            holder.leftSpace.setVisibility(View.VISIBLE);
            holder.rightSpace.setVisibility(View.GONE);
            holder.profileImage.setVisibility(View.VISIBLE);
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.primary_green));
            holder.messageText.setTextColor(context.getResources().getColor(R.color.white));
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.white));
            holder.senderName.setGravity(Gravity.END);
        } else {
            // Other user's message (left side)
            holder.leftSpace.setVisibility(View.GONE);
            holder.rightSpace.setVisibility(View.VISIBLE);
            holder.profileImage.setVisibility(View.GONE);
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.beige));
            holder.messageText.setTextColor(context.getResources().getColor(R.color.black));
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.black));
            holder.senderName.setGravity(Gravity.START);
        }

        // Set sender name
        if (message.getSenderName() != null) {
            holder.senderName.setVisibility(View.VISIBLE);
            holder.senderName.setText(message.getSenderName());
        } else {
            holder.senderName.setVisibility(View.GONE);
        }

        // Set message text
        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getMessage());
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Set image
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
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText(formatTimestamp(message.getTimestamp()));
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }

        // Load profile picture if available
        if (message.getSenderProfilePic() != null && !message.getSenderProfilePic().isEmpty()) {
            Glide.with(context)
                .load(message.getSenderProfilePic())
                .placeholder(R.drawable.default_profile)
                .into(holder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView senderName;
        TextView messageText;
        ImageView messageImage;
        TextView timestamp;
        CircleImageView profileImage;
        View leftSpace;
        View rightSpace;
        LinearLayout messageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            senderName = itemView.findViewById(R.id.senderName);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            timestamp = itemView.findViewById(R.id.timestamp);
            profileImage = itemView.findViewById(R.id.profileImage);
            leftSpace = itemView.findViewById(R.id.leftSpace);
            rightSpace = itemView.findViewById(R.id.rightSpace);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
} 