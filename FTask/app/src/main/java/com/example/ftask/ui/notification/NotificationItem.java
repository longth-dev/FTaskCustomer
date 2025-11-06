package com.example.ftask.ui.notification;

public class NotificationItem {
    public int id;
    public String title;
    public String message;
    public String createdAt;
    public boolean isRead;

    public NotificationItem(int id, String title, String message, String createdAt, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }
}