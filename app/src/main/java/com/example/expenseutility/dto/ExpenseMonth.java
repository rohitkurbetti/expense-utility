package com.example.expenseutility.dto;

import java.util.List;

// ExpenseMonth.java
public class ExpenseMonth {
    private String monthYear;
    private double totalExpense;
    private List<CategoryItem> categoryItems;
    private boolean isExpanded;

    public ExpenseMonth(String monthYear, double totalExpense, List<CategoryItem> categoryItems) {
        this.monthYear = monthYear;
        this.totalExpense = totalExpense;
        this.categoryItems = categoryItems;
        this.isExpanded = false;
    }

    // Getters and Setters
    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public List<CategoryItem> getCategoryItems() {
        return categoryItems;
    }

    public void setCategoryItems(List<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
