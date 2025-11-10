package com.example.ftask.ui.activity;

public class Booking {
    private int id;
    private String startAt;
    private double totalPrice;
    private String customerNote;
    private String status;
    private String serviceName; // ✅ Thêm tên dịch vụ

    public Booking(int id, String startAt, double totalPrice, String customerNote, String status, String serviceName) {
        this.id = id;
        this.startAt = startAt;
        this.totalPrice = totalPrice;
        this.customerNote = customerNote;
        this.status = status;
        this.serviceName = serviceName;
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

    public String getServiceName() {
        return serviceName;
    }
}