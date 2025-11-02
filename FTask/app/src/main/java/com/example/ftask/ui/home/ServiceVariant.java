package com.example.ftask.ui.home;

public class ServiceVariant {
    private int id;
    private String name;
    private String description;
    private int durationHours;
    private int pricePerVariant;
    private boolean isMultiPartner;
    private int numberOfPartners;

    // Getters và Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDurationHours() { return durationHours; }
    public int getPricePerVariant() { return pricePerVariant; }
    public boolean isMultiPartner() { return isMultiPartner; }
    public int getNumberOfPartners() { return numberOfPartners; }
}
