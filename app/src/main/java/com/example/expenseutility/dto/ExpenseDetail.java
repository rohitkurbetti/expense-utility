package com.example.expenseutility.dto;

// ExpenseDetail.java
public class ExpenseDetail {
    private String itemName;
    private double amount;
    private String date;
    private String description;

    public ExpenseDetail(String itemName, double amount, String date, String description) {
        this.itemName = itemName;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
