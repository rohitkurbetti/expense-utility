package com.example.expenseutility.dto;

public class Transaction {

    private boolean selected;
    private String date;
    private String particulars;
    private String debitAmount;
    private String category;

    public Transaction(String date, String particulars, String debitAmount) {
        this.date = date;
        this.particulars = particulars;
        this.debitAmount = debitAmount;
        this.selected = false;
    }

    public String getDate() { return date; }
    public String getParticulars() { return particulars; }
    public String getDebitAmount() { return debitAmount; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


}
