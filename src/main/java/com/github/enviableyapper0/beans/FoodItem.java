package com.github.enviableyapper0.beans;

import java.io.Serializable;

/**
 * Created by MaxMac on 27-Oct-17.
 */

public class FoodItem implements Serializable {
    private double price;
    private String name;
    private int quantity = 0;
    private boolean isAvailable = true;
    private String id;
    private FoodType foodType;

    public FoodItem() {}

    public FoodItem(String id, String name, double price, FoodType foodType) {
        this.price = price;
        this.name = name;
        this.foodType = foodType;
        this.id = id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public String getID() {
        return id;
    }
}