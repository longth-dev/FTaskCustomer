package com.example.ftask;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.ftask.api.FcmTokenHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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
        Log.i(TAG, "Message Type: " + remoteMessage.getMessageType());
        Log.i(TAG, "============================================");

        // Kiểm tra nếu message có data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload size: " + remoteMessage.getData().size());
            for (java.util.Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                Log.d(TAG, "Data - " + entry.getKey() + ": " + entry.getValue());
            }
        } else {
            Log.d(TAG, "No data payload");
        }

        // Kiểm tra nếu message có notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Has notification payload");
            Log.d(TAG, "Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            
            // Hiển thị notification
            sendNotification(title, body, remoteMessage.getData());
        } else {
            Log.d(TAG, "No notification payload, using data payload");
            // Nếu không có notification payload, lấy từ data
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String message = remoteMessage.getData().get("message");
            
            if (title == null || title.isEmpty()) {
                title = "New Notification";
            }
            if (body == null || body.isEmpty()) {
                body = message != null ? message : "You have a new message";
            }
            
            Log.d(TAG, "Extracted Title: " + title + ", Body: " + body);
            sendNotification(title, body, remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        Log.i(TAG, "============================================");
        Log.i(TAG, "NEW FCM TOKEN (nếu token thay đổi):");
        Log.i(TAG, token);
        Log.i(TAG, "============================================");
        
        // Gửi token lên server qua FcmTokenHelper
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Sử dụng FcmTokenHelper để gửi token lên server
        FcmTokenHelper.sendToken(getApplicationContext(), token, new FcmTokenHelper.TokenCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "FCM token sent to server successfully");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to send FCM token to server: " + error);
            }
        });
    }

    private void sendNotification(String title, String messageBody, java.util.Map<String, String> data) {
        try {
            Log.i(TAG, "============================================");
            Log.i(TAG, "Creating notification...");
            Log.i(TAG, "Title: " + title);
            Log.i(TAG, "Body: " + messageBody);
            Log.i(TAG, "============================================");
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Thêm data vào intent nếu cần
            if (data != null && !data.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            // Tạo notification channel cho Android Oreo trở lên
            createNotificationChannel();

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle(title != null ? title : "New Notification")
                            .setContentText(messageBody != null ? messageBody : "You have a new message")
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
                Log.i(TAG, "============================================");
                Log.i(TAG, "Notification displayed successfully!");
                Log.i(TAG, "Notification ID: " + notificationId);
                Log.i(TAG, "============================================");
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
            if (notificationManager != null) {
                // Kiểm tra xem channel đã tồn tại chưa
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "Default Channel",
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("Default notification channel");
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    channel.setShowBadge(true);
                    // Sound sẽ tự động được bật khi importance là HIGH
                    // Sử dụng sound mặc định của hệ thống (không cần setSound riêng)

                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
                } else {
                    Log.d(TAG, "Notification channel already exists: " + CHANNEL_ID);
                }
            }
        }
    }
}

