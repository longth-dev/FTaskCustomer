package com.example.ftask.ui.message;

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
import com.example.ftask.R;
import com.example.ftask.models.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private Context context;
    private List<Conversation> conversations;

    public ConversationAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.txtPartnerName.setText(conversation.getPartnerName());
        holder.txtLastMessage.setText(conversation.getLastMessage());
        holder.txtTime.setText(formatTime(conversation.getLastMessageTime()));

        // Hiển thị badge unread count
        if (conversation.getUnreadCount() > 0) {
            holder.txtUnreadCount.setVisibility(View.VISIBLE);
            holder.txtUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
        } else {
            holder.txtUnreadCount.setVisibility(View.GONE);
        }

        // Hiển thị online status
        holder.imgOnlineStatus.setVisibility(conversation.isOnline() ? View.VISIBLE : View.GONE);

        // Load avatar
        if (conversation.getPartnerAvatar() != null && !conversation.getPartnerAvatar().isEmpty()) {
            Glide.with(context)
                    .load(conversation.getPartnerAvatar())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // Click to open chat detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("CONVERSATION_ID", conversation.getId());
            intent.putExtra("PARTNER_ID", conversation.getPartnerId());
            intent.putExtra("PARTNER_NAME", conversation.getPartnerName());
            intent.putExtra("PARTNER_AVATAR", conversation.getPartnerAvatar());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // < 1 phút
            return "Vừa xong";
        } else if (diff < 3600000) { // < 1 giờ
            return (diff / 60000) + " phút";
        } else if (diff < 86400000) { // < 1 ngày
            return (diff / 3600000) + " giờ";
        } else if (diff < 604800000) { // < 1 tuần
            return (diff / 86400000) + " ngày";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgOnlineStatus;
        TextView txtPartnerName, txtLastMessage, txtTime, txtUnreadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgOnlineStatus = itemView.findViewById(R.id.imgOnlineStatus);
            txtPartnerName = itemView.findViewById(R.id.txtPartnerName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnreadCount = itemView.findViewById(R.id.txtUnreadCount);
        }
    }
}
