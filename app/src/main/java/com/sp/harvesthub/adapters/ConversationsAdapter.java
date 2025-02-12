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
import com.sp.harvesthub.R;
import com.sp.harvesthub.activities.DirectChatActivity;
import com.sp.harvesthub.models.ChatConversation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {
    private Context context;
    private List<ChatConversation> conversations;

    public ConversationsAdapter(Context context, List<ChatConversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_direct_chat, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ChatConversation conversation = conversations.get(position);

        // Set username
        holder.usernameText.setText(conversation.getOtherUsername());

        // Set last message
        holder.lastMessageText.setText(conversation.getLastMessage());

        // Set timestamp
        if (conversation.getLastMessageTimestamp() > 0) {
            holder.timestampText.setText(formatTimestamp(conversation.getLastMessageTimestamp()));
        }

        // Load profile picture
        if (conversation.getOtherUserProfilePic() != null && !conversation.getOtherUserProfilePic().isEmpty()) {
            Glide.with(context)
                .load(conversation.getOtherUserProfilePic())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(holder.profileImage);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DirectChatActivity.class);
            intent.putExtra("conversationId", conversation.getConversationId());
            intent.putExtra("otherUserId", conversation.getOtherUserId());
            intent.putExtra("otherUsername", conversation.getOtherUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public void updateConversations(List<ChatConversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView usernameText;
        TextView lastMessageText;
        TextView timestampText;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            usernameText = itemView.findViewById(R.id.usernameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }
    }
} 