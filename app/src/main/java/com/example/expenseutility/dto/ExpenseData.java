package com.example.expenseutility.dto;

import java.util.List;

// ExpenseData.java
public class ExpenseData {
    private String year;
    private double yearTotal;
    private List<MonthData> months;

    public ExpenseData(String year, double yearTotal, List<MonthData> months) {
        this.year = year;
        this.yearTotal = yearTotal;
        this.months = months;
    }

    // Getters and Setters
    public String getYear() {
        return year;
    }

    public double getYearTotal() {
        return yearTotal;
    }

    public List<MonthData> getMonths() {
        return months;
    }
}

