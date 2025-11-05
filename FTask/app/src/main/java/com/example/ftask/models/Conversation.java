package com.example.ftask.models;

public class Conversation {
    private String id;
    private String partnerId;
    private String partnerName;
    private String partnerAvatar;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;
    private String partnerRole; // "tasker", "customer"
    private boolean isOnline;

    public Conversation() {
    }

    public Conversation(String id, String partnerId, String partnerName, String partnerAvatar,
                        String lastMessage, long lastMessageTime, int unreadCount,
                        String partnerRole, boolean isOnline) {
        this.id = id;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.partnerAvatar = partnerAvatar;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.partnerRole = partnerRole;
        this.isOnline = isOnline;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerAvatar() {
        return partnerAvatar;
    }

    public void setPartnerAvatar(String partnerAvatar) {
        this.partnerAvatar = partnerAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getPartnerRole() {
        return partnerRole;
    }

    public void setPartnerRole(String partnerRole) {
        this.partnerRole = partnerRole;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}