package com.example.ftask.models;

public class WalletModel {
    private int id;
    private double balance;
    private double totalEarned;
    private double totalWithdrawn;
    private User user;

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public double getTotalWithdrawn() {
        return totalWithdrawn;
    }

    public static class User {
        private int id;
        private String username;
        private String phone;
        private String email;
        private String gender;
        private String idCard;
        private String fullName;
        private String role;

        // Getters
        public String getFullName() { return fullName; }
    }
}