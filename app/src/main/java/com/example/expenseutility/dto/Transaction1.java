package com.example.expenseutility.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction1 {
    private int serialNo;
    private String tranDate;
    private String particulars;
    private String debit;
    private String category;
    private String formattedDate;
    private boolean isChecked;
    private String parsedParticulars;

    public Transaction1(int serialNo, String tranDate, String particulars, String debit, String category, String parsedParticulars) {
        this.serialNo = serialNo;
        this.tranDate = tranDate;
        this.particulars = particulars;
        this.debit = debit;
        this.category = category;
        this.isChecked = true;
        this.formattedDate = formatDate(tranDate);
        this.parsedParticulars = parsedParticulars;
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr;
        }
    }

    // Getters
    public int getSerialNo() {
        return serialNo;
    }

    // Setters
    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    public String getTranDate() {
        return tranDate;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getParticulars() {
        return particulars;
    }

    public String getDebit() {
        return debit;
    }

    public String getCategory() {
        return category;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getParsedParticulars() {
        return parsedParticulars;
    }

    public void setParsedParticulars(String parsedParticulars) {
        this.parsedParticulars = parsedParticulars;
    }

    @Override
    public String toString() {
        return "Transaction1{" +
                "serialNo=" + serialNo +
                ", tranDate='" + tranDate + '\'' +
                ", particulars='" + particulars + '\'' +
                ", debit='" + debit + '\'' +
                ", category='" + category + '\'' +
                ", formattedDate='" + formattedDate + '\'' +
                ", isChecked=" + isChecked +
                ", parsedParticulars='" + parsedParticulars + '\'' +
                '}';
    }
}