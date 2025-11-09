package com.example.ftask.ui.account;

public class Transaction {
    private int id;
    private String type;
    private double amount;
    private double balanceBefore;
    private double balanceAfter;
    private String description;
    private String status;

    public Transaction(int id, String type, double amount, double balanceBefore, double balanceAfter, String description, String status) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.status = status;
    }

    // Getter
    public int getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceBefore() { return balanceBefore; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
}