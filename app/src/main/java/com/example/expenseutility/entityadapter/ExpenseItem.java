package com.example.expenseutility.entityadapter;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

public class ExpenseItem implements Serializable {

    private Integer id;
    private String expenseParticulars;
    private Long expenseAmount;
    private String expenseDate;
    private String expenseDateTime;
    private String expenseCategory;
    private Boolean isChecked = false;
    private String fileName;
    private byte[] fileBytes;
    private String partDetails;
    private boolean isHomeExpense;
    private Integer imageDrawableId;

    public ExpenseItem() {

    }

    public ExpenseItem(String expenseCategory, String particulars, String amount, String dateTimeVal, String dateVal, String fileName, byte[] pdfBytes) {
        this.expenseCategory = expenseCategory;
        this.expenseParticulars = particulars;
        this.expenseAmount = Long.valueOf(amount);
        this.expenseDateTime = dateTimeVal;
        this.expenseDate = dateVal;
        this.fileName = fileName;
        this.fileBytes = pdfBytes;
    }

    public ExpenseItem(String expenseCategory, String particulars, String amount, String dateTimeVal, String dateVal, String fileName, byte[] pdfBytes, String encodedPartDetails) {
        this.expenseCategory = expenseCategory;
        this.expenseParticulars = particulars;
        this.expenseAmount = Long.valueOf(amount);
        this.expenseDateTime = dateTimeVal;
        this.expenseDate = dateVal;
        this.fileName = fileName;
        this.fileBytes = pdfBytes;
        this.partDetails = encodedPartDetails;
    }

    public ExpenseItem(Integer id, String expenseParticulars, Long expenseAmount, String expenseDate, String expenseCategory, String fileName, byte[] fileBytes) {
        this.id = id;
        this.expenseParticulars = expenseParticulars;
        this.expenseAmount = expenseAmount;
        this.expenseDate = expenseDate;
        this.expenseCategory = expenseCategory;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
    }

    public ExpenseItem(Integer id, String expenseParticulars, Long expenseAmount, String expenseDate, String expenseCategory, String fileName, byte[] fileBytes, String encodedPartDetails) {
        this.id = id;
        this.expenseParticulars = expenseParticulars;
        this.expenseAmount = expenseAmount;
        this.expenseDate = expenseDate;
        this.expenseCategory = expenseCategory;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.partDetails = encodedPartDetails;
    }

    public ExpenseItem(String expenseParticulars, Long expenseAmount, String expenseDate, String expenseCategory) {
        this.expenseParticulars = expenseParticulars;
        this.expenseAmount = expenseAmount;
        this.expenseDate = expenseDate;
        this.expenseCategory = expenseCategory;
    }

    public ExpenseItem(int id, String expenseParticulars, long amt, String dtm, String expCat, String fileName, byte[] fileBytes, String encodedPartDetails, boolean isHomeExpense) {
        this.id = id;
        this.expenseParticulars = expenseParticulars;
        this.expenseAmount = amt;
        this.expenseDate = dtm;
        this.expenseCategory = expCat;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.partDetails = encodedPartDetails;
        this.isHomeExpense = isHomeExpense;
    }

    public ExpenseItem(String expenseCategory, String particulars, String amount, String dateTimeVal, String dateVal, String fileName, byte[] fileBytes, String encodedPartDetails, boolean isHomeExpense) {
        this.expenseParticulars = particulars;
        this.expenseAmount = Long.valueOf(amount);
        this.expenseDate = dateVal;
        this.expenseDateTime = dateTimeVal;
        this.expenseCategory = expenseCategory;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.partDetails = encodedPartDetails;
        this.isHomeExpense = isHomeExpense;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExpenseParticulars() {
        return expenseParticulars;
    }

    public void setExpenseParticulars(String expenseParticulars) {
        this.expenseParticulars = expenseParticulars;
    }

    public Long getExpenseAmount() {
        return expenseAmount;
    }

    public void setExpenseAmount(Long expenseAmount) {
        this.expenseAmount = expenseAmount;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public String getExpenseDateTime() {
        return expenseDateTime;
    }

    public void setExpenseDateTime(String expenseDateTime) {
        this.expenseDateTime = expenseDateTime;
    }

    public String getPartDetails() {
        return partDetails;
    }

    public void setPartDetails(String partDetails) {
        this.partDetails = partDetails;
    }

    public boolean isHomeExpense() {
        return isHomeExpense;
    }

    public void setHomeExpense(boolean homeExpense) {
        isHomeExpense = homeExpense;
    }

    public Integer getImageDrawableId() {
        return imageDrawableId;
    }

    public void setImageDrawableId(Integer imageDrawableId) {
        this.imageDrawableId = imageDrawableId;
    }

    @Override
    public String toString() {
        return "ExpenseItem{" +
                "id=" + id +
                ", expenseParticulars='" + expenseParticulars + '\'' +
                ", expenseAmount=" + expenseAmount +
                ", expenseDate='" + expenseDate + '\'' +
                ", expenseDateTime='" + expenseDateTime + '\'' +
                ", expenseCategory='" + expenseCategory + '\'' +
                ", isChecked=" + isChecked +
                ", fileName='" + fileName + '\'' +
                ", fileBytes=" + Arrays.toString(fileBytes) +
                '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
