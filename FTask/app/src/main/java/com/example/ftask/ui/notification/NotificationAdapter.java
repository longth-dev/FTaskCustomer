package com.example.ftask.ui.notification;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.example.ftask.R;
import java.util.*;

public class NotificationAdapter extends ArrayAdapter<NotificationItem> {

    public NotificationAdapter(Context context, List<NotificationItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NotificationItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvMessage = convertView.findViewById(R.id.tvMessage);
        TextView tvDate = convertView.findViewById(R.id.tvDate);

        tvTitle.setText(item.title);
        tvMessage.setText(item.message);
        tvDate.setText(item.createdAt.replace("T", " ").substring(0, 16));

        if (!item.isRead) {
            convertView.setBackgroundColor(0xFFE3F2FD); // màu xanh nhạt nếu chưa đọc
        } else {
            convertView.setBackgroundColor(0xFFFFFFFF);
        }

        return convertView;
    }
}
