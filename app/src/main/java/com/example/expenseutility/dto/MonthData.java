package com.example.expenseutility.dto;

import java.util.List;

// MonthData.java
public class MonthData {
    private String monthName;
    private double totalAmount;
    private List<CategoryData> categories;

    public MonthData(String monthName, double totalAmount, List<CategoryData> categories) {
        this.monthName = monthName;
        this.totalAmount = totalAmount;
        this.categories = categories;
    }

    public String getMonthName() {
        return monthName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<CategoryData> getCategories() {
        return categories;
    }
}
