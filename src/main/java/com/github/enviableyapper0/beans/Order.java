package com.github.enviableyapper0.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private List<FoodItem> foodItems;
    private int id;
    private int tableNum;

    public Order() {
    }

    public Order(int id, int tableNum) {
        this.foodItems = new ArrayList<>();
        this.id = id;
        this.tableNum = tableNum;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }

    public int getTableNum() {
        return tableNum;
    }

    public void setTableNum(int tableNum) {
        this.tableNum = tableNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Order{" +
                "foodItems=" + foodItems +
                ", id=" + id +
                ", tableNum=" + tableNum +
                '}';
    }
}
