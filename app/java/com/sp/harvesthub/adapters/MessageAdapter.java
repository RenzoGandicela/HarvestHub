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
import com.google.firebase.auth.FirebaseAuth;
import com.sp.harvesthub.R;
import com.sp.harvesthub.models.Message;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "sample_user";
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        String senderId = message.getSenderId();
        if (senderId != null && senderId.equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
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

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageTxt, timestampTxt, senderNameTxt;
        ImageView messageImage;
        CircleImageView profileImage;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timestampTxt = itemView.findViewById(R.id.timestampTxt);
            senderNameTxt = itemView.findViewById(R.id.senderNameTxt);
            messageImage = itemView.findViewById(R.id.messageImage);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        void bind(Message message) {
            // Set sender name
            senderNameTxt.setText(message.getSenderName() != null ? message.getSenderName() : "Me");
            
            // Set timestamp
            timestampTxt.setText(formatTimestamp(message.getTimestamp()));

            // Load profile picture
            if (message.getSenderProfilePic() != null && !message.getSenderProfilePic().isEmpty()) {
                Glide.with(context)
                    .load(message.getSenderProfilePic())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }

            if (message.getText() != null) {
                messageTxt.setVisibility(View.VISIBLE);
                messageTxt.setText(message.getText());
            } else {
                messageTxt.setVisibility(View.GONE);
            }

            if (message.getImageURL() != null && !message.getImageURL().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(message.getImageURL())
                    .into(messageImage);
            } else {
                messageImage.setVisibility(View.GONE);
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageTxt, timestampTxt, senderNameTxt;
        ImageView messageImage;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timestampTxt = itemView.findViewById(R.id.timestampTxt);
            senderNameTxt = itemView.findViewById(R.id.senderNameTxt);
            messageImage = itemView.findViewById(R.id.messageImage);
            profileImage = itemView.findViewById(R.id.profileImage);
        }

        void bind(Message message) {
            // Set sender name
            senderNameTxt.setText(message.getSenderName() != null ? message.getSenderName() : "Unknown User");
            
            // Set timestamp
            timestampTxt.setText(formatTimestamp(message.getTimestamp()));

            // Load profile picture
            if (message.getSenderProfilePic() != null && !message.getSenderProfilePic().isEmpty()) {
                Glide.with(context)
                    .load(message.getSenderProfilePic())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }

            if (message.getText() != null) {
                messageTxt.setVisibility(View.VISIBLE);
                messageTxt.setText(message.getText());
            } else {
                messageTxt.setVisibility(View.GONE);
            }

            if (message.getImageURL() != null && !message.getImageURL().isEmpty()) {
                messageImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                    .load(message.getImageURL())
                    .into(messageImage);
            } else {
                messageImage.setVisibility(View.GONE);
            }
        }
    }
} 