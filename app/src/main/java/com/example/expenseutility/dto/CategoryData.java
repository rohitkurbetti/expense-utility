package com.example.expenseutility.dto;

import java.util.List;

// CategoryData.java
public class CategoryData {
    private String categoryName;
    private double totalAmount;
    private Integer categoryIcon;
    private List<ExpenseItem1> expenses;

    public CategoryData(String categoryName, double totalAmount, Integer categoryIcon, List<ExpenseItem1> expenses) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.expenses = expenses;
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ExpenseItem1> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseItem1> expenses) {
        this.expenses = expenses;
    }

    public Integer getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(Integer categoryIcon) {
        this.categoryIcon = categoryIcon;
    }
}
