package com.example.expenseutility.dto;

import java.util.Locale;

public class CategoryItem {
    private String categoryName;
    private double amount;
    private double percentage;

    public CategoryItem(String categoryName, double amount, double percentage) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getFormattedAmount() {
        return String.format(Locale.ENGLISH, "₹%.0f", amount);
    }

    public String getFormattedPercentage() {
        return String.format(Locale.ENGLISH, "%.2f%%", percentage);
    }
}
