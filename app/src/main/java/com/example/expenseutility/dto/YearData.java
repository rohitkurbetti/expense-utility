package com.example.expenseutility.dto;

import java.util.List;

public class YearData {
    private String year;
    private double totalAmount;
    private List<MonthData> months;

    public YearData(String year, double totalAmount, List<MonthData> months) {
        this.year = year;
        this.totalAmount = totalAmount;
        this.months = months;
    }

    public String getYear() {
        return year;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<MonthData> getMonths() {
        return months;
    }
}
