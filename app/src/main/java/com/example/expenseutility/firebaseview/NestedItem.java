package com.example.expenseutility.firebaseview;

import java.util.List;

// Model for the ListView
public class NestedItem {
    private String subItemName;
    private String subItemExpense;
    private String subItemExpensePer;
    private Long subItemExpenseTotalAmount;

    private Boolean isChecked = false;




    public NestedItem(String subItemName, String subItemExpense,String subItemExpensePer, Long subItemExpenseTotalAmount) {
        this.subItemName = subItemName;
        this.subItemExpense = subItemExpense;
        this.subItemExpensePer = subItemExpensePer;
        this.subItemExpenseTotalAmount = subItemExpenseTotalAmount;

    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public String getSubItemName() {
        return subItemName;
    }

    public String getSubItemExpense() {
        return subItemExpense;
    }

    public String getSubItemExpensePer() {
        return subItemExpensePer;
    }

    public Long getSubItemExpenseTotalAmount() {
        return subItemExpenseTotalAmount;
    }

    public void setSubItemExpenseTotalAmount(Long subItemExpenseTotalAmount) {
        this.subItemExpenseTotalAmount = subItemExpenseTotalAmount;
    }

    @Override
    public String toString() {
        return "NestedItem{" +
                "subItemName='" + subItemName + '\'' +
                ", subItemExpense='" + subItemExpense + '\'' +
                ", subItemExpensePer='" + subItemExpensePer + '\'' +
                '}';
    }
}

