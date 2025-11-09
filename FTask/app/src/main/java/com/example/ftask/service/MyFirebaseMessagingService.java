package com.example.ftask.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.ftask.MainActivity;
import com.example.ftask.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i(TAG, "============================================");
        Log.i(TAG, "NOTIFICATION RECEIVED!");
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        Log.i(TAG, "Message ID: " + remoteMessage.getMessageId());

        String title;
        String body;

        // Ưu tiên notification payload nếu có
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Has notification payload");
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
        } else {
            // Nếu không có, lấy từ data payload
            Log.d(TAG, "No notification payload, using data payload");
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        // Kiểm tra data payload (luôn log để debug)
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload size: " + remoteMessage.getData().size());
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                Log.d(TAG, "Data - " + entry.getKey() + ": " + entry.getValue());
            }
        } else {
            Log.d(TAG, "No data payload");
        }

        // Fallback nếu không có title/body
        if (title == null || title.isEmpty()) {
            title = "FTask Notification";
        }
        if (body == null || body.isEmpty()) {
            body = "You have a new message";
        }

        Log.d(TAG, "Final Title: " + title + ", Final Body: " + body);
        sendNotification(title, body, remoteMessage.getData());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(TAG, "============================================");
        Log.i(TAG, "Refreshed token: " + token);
        Log.i(TAG, "============================================");
        // Gửi token lên server nếu cần
        // sendRegistrationToServer(token);
    }

    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        try {
            Log.i(TAG, "Creating notification...");

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            createNotificationChannel();

            // Đây là dòng icon
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle(title)
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());
                Log.i(TAG, "Notification displayed successfully! ID: " + notificationId);
            } else {
                Log.e(TAG, "NotificationManager is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying notification", e);
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Default Channel",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Default notification channel for FTask");
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
            }
        }
    }
}
