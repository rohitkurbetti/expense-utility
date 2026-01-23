package com.example.expenseutility.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expenseutility.dto.Budget;

import java.util.ArrayList;
import java.util.List;

public class BudgetHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_BUDGET = "monthly_budget";
    // Column Names
    public static final String KEY_ID = "id";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MONTH = "month";
    public static final String KEY_BUDGET = "budget";
    // Database Information
    private static final String DATABASE_NAME = "BudgetDB";
    private static final int DATABASE_VERSION = 1;
    // Create Table Query
    private static final String CREATE_TABLE_BUDGET =
            "CREATE TABLE " + TABLE_BUDGET + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_YEAR + " INTEGER NOT NULL,"
                    + KEY_MONTH + " INTEGER NOT NULL,"
                    + KEY_BUDGET + " INTEGER NOT NULL,"
                    + "UNIQUE(" + KEY_YEAR + ", " + KEY_MONTH + ")"
                    + ")";
    private static BudgetHelper instance;

    public BudgetHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized BudgetHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BudgetHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BUDGET);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }

    // Add or Update Budget
    public long addOrUpdateBudget(int year, int month, int budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_YEAR, year);
        values.put(KEY_MONTH, month);
        values.put(KEY_BUDGET, budget);

        try {
            // Check if budget already exists for this year and month
            String whereClause = KEY_YEAR + "=? AND " + KEY_MONTH + "=?";
            String[] whereArgs = {String.valueOf(year), String.valueOf(month)};

            // Try to update first
            int rowsAffected = db.update(TABLE_BUDGET, values, whereClause, whereArgs);

            if (rowsAffected == 0) {
                // No existing record, insert new one
                result = db.insert(TABLE_BUDGET, null, values);
            } else {
                result = rowsAffected;
            }
        } finally {
            db.close();
        }

        return result;
    }

    // Get Budget for specific year and month
    public int getBudget(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        int budget = 0;

        String[] columns = {KEY_BUDGET};
        String selection = KEY_YEAR + "=? AND " + KEY_MONTH + "=?";
        String[] selectionArgs = {String.valueOf(year), String.valueOf(month)};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BUDGET, columns, selection, selectionArgs,
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                budget = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BUDGET));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return budget;
    }

    // Check if budget exists for year and month
    public boolean budgetExists(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;

        String[] columns = {KEY_ID};
        String selection = KEY_YEAR + "=? AND " + KEY_MONTH + "=?";
        String[] selectionArgs = {String.valueOf(year), String.valueOf(month)};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BUDGET, columns, selection, selectionArgs,
                    null, null, null);

            exists = cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

    // Get all budgets as list
    public List<Budget> getAllBudgets() {
        List<Budget> budgetList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {KEY_ID, KEY_YEAR, KEY_MONTH, KEY_BUDGET};
        String orderBy = KEY_YEAR + " DESC, " + KEY_MONTH + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BUDGET, columns, null, null, null, null, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Budget budget = new Budget();
                    budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                    budget.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_YEAR)));
                    budget.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MONTH)));
                    budget.setBudget(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BUDGET)));
                    budgetList.add(budget);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return budgetList;
    }

    // Delete budget for specific year and month
    public int deleteBudget(int year, int month) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;

        try {
            String whereClause = KEY_YEAR + "=? AND " + KEY_MONTH + "=?";
            String[] whereArgs = {String.valueOf(year), String.valueOf(month)};

            result = db.delete(TABLE_BUDGET, whereClause, whereArgs);
        } finally {
            db.close();
        }

        return result;
    }

    // Delete budget by ID
    public int deleteBudgetById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;

        try {
            String whereClause = KEY_ID + "=?";
            String[] whereArgs = {String.valueOf(id)};

            result = db.delete(TABLE_BUDGET, whereClause, whereArgs);
        } finally {
            db.close();
        }

        return result;
    }

    // Get total budget for a year
    public int getYearlyTotal(int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        int total = 0;

        Cursor cursor = null;
        try {
            String query = "SELECT SUM(" + KEY_BUDGET + ") FROM " + TABLE_BUDGET
                    + " WHERE " + KEY_YEAR + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(year)});

            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return total;
    }
}
