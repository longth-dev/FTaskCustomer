package com.example.ftask.models;

import java.util.List;

public class ConversationResponse {
    private List<Conversation> conversations;
    private int totalUnread;

    public ConversationResponse() {
    }

    public ConversationResponse(List<Conversation> conversations, int totalUnread) {
        this.conversations = conversations;
        this.totalUnread = totalUnread;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public int getTotalUnread() {
        return totalUnread;
    }

    public void setTotalUnread(int totalUnread) {
        this.totalUnread = totalUnread;
    }
}