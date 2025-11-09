package com.example.ftask.ui.activity;

public class Booking {
    private int id;
    private String startAt;
    private double totalPrice;
    private String customerNote;
    private String status;

    public Booking(int id, String startAt, double totalPrice, String customerNote, String status) {
        this.id = id;
        this.startAt = startAt;
        this.totalPrice = totalPrice;
        this.customerNote = customerNote;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getStartAt() {
        return startAt;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public String getStatus() {
        return status;
    }
}