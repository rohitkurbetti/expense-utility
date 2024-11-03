package com.example.expenseutility.entityadapter;

import java.io.Serializable;

public class FirebaseSubSubItem implements Serializable {

    private ExpenseItem expenseItem;

    public ExpenseItem getExpenseItem() {
        return expenseItem;
    }

    public void setExpenseItem(ExpenseItem expenseItem) {
        this.expenseItem = expenseItem;
    }
}
