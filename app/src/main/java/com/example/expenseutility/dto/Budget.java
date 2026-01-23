package com.example.expenseutility.dto;

public class Budget {
    private int id;
    private int year;
    private int month;
    private int budget;

    public Budget() {
    }

    public Budget(int id, int year, int month, int budget) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.budget = budget;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    // Helper method to get month name
    public String getMonthName() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return (month >= 1 && month <= 12) ? months[month - 1] : "Invalid Month";
    }
}
