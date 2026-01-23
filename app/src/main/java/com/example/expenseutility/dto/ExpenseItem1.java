package com.example.expenseutility.dto;

// ExpenseItem1.java
public class ExpenseItem1 {
    private String title;
    private double amount;
    private String date;
    private String description;

    public ExpenseItem1(String title, double amount, String date, String description) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public String getTitle() {
        return title;
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
