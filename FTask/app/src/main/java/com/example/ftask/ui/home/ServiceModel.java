package com.example.ftask.ui.home;

public class ServiceModel {
    private int image;
    private String name;

    public ServiceModel(int image, String name) {
        this.image = image;
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public String getName() {
        return name;
    }
}
