package com.example.expenseutility.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.expenseutility.entityadapter.ExpenseItem;
import com.example.expenseutility.entityadapter.Suggestion;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "ExpenseTable";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EXPENSE_CATEGORY = "expenseCategory";
    private static final String COLUMN_PARTICULARS = "particulars";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DATE_TIME = "dateTime";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_FILE = "file";
    private static final String COLUMN_FILE_NAME = "fileName";

    private static final String TABLE_NAME_1 = "SuggestionsTable";
    private static final String COLUMN_ID_1 = "id";
    private static final String COLUMN_DESCRIPTION_1 = "suggestionDescription";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EXPENSE_CATEGORY + " TEXT, " +
                COLUMN_PARTICULARS + " TEXT, " +
                COLUMN_AMOUNT + " BIGINT, " +
                COLUMN_DATE_TIME + " TEXT," +
                COLUMN_DATE + " Date, " +
                COLUMN_FILE_NAME + " TEXT, " +
                COLUMN_FILE + " blob " +
                " )";
        db.execSQL(createTable);

        //table2

        String createTable1 = "CREATE TABLE " + TABLE_NAME_1 + " (" +
                COLUMN_ID_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DESCRIPTION_1 + " TEXT " + " )";
        db.execSQL(createTable1);


        //txn ignore table

        db.execSQL("CREATE TABLE txn_ignore (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, dateTime TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_1);
        db.execSQL("DROP TABLE IF EXISTS txn_ignore");
        onCreate(db);
    }


    public void putDummyData() throws IllegalAccessException, NoSuchFieldException {
        insertExpense("Grocery",	"ask",	"150",	"2024-08-30 02:22"	,"2024-08-30",	null	,null, null);
        insertExpense("Healthcare",	"tabletvs",	"140",	"2024-08-23 11:21"	,"2024-08-23",	null	,null, null);
        insertExpense("Food",	"breakfast",	"508",	"2024-08-23 11:21"	,"2024-08-23",	null	,null, null);
        insertExpense("Savings and Investments",	"movie",	"100",	"2024-08-23 11:20"	,"2024-08-23",	null	,null, null);
        insertExpense("Grocery",	"dmart",	"340",	"2024-08-23 11:20"	,"2024-08-23",	null	,null, null);
    }

    public void putDummyData(int n) throws IllegalAccessException, NoSuchFieldException {

        for(int i =0;i<n;i++) {
        insertExpense("Grocery",	"ask",	"150",	"2024-08-30 02:22"	,"2024-08-30",	null	,null, null);
        insertExpense("Healthcare",	"tabletvs",	"140",	"2024-08-23 11:21"	,"2024-08-23",	null	,null, null);
        insertExpense("Food",	"breakfast",	"508",	"2024-08-23 11:21"	,"2024-08-23",	null	,null, null);
        insertExpense("Savings and Investments",	"movie",	"100",	"2024-08-23 11:20"	,"2024-08-23",	null	,null, null);
        insertExpense("Grocery",	"dmart",	"340",	"2024-08-23 11:20"	,"2024-08-23",	null	,null, null);
        }
    }

    public boolean insertExpense(String expenseCategory, String particulars, String amount, String dateTime, String date, String fileName, byte[] fileBytes, Integer id) throws IllegalAccessException, NoSuchFieldException {
        Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
        field.setAccessible(true);
        field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EXPENSE_CATEGORY, expenseCategory);
        contentValues.put(COLUMN_PARTICULARS, particulars);
        contentValues.put(COLUMN_AMOUNT, amount);
        contentValues.put(COLUMN_DATE_TIME, dateTime);
        contentValues.put(COLUMN_DATE, date);
        contentValues.put(COLUMN_FILE_NAME, fileName);
        contentValues.put(COLUMN_FILE, fileBytes);
        long result = 0;
        if(id !=null){
            //updation
            contentValues.put(COLUMN_ID, id);
            result = db.update(TABLE_NAME, contentValues,COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        } else {
            //insertion
            result = db.insert(TABLE_NAME, null, contentValues);
        }

        db.close();
        return result != -1;
    }

    public Cursor getAllExpenseData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " order by " + COLUMN_DATE + " desc ", null);
    }

    public Cursor getExpenseGroupByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT date, sum(\"amount\") \"amount\" FROM 'ExpenseTable' group by date;", null);
    }


    public double getTotalExpenseForToday() {
        double totalExpense = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Get today's date
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") as total FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{todayDate});

        if (cursor.moveToFirst()) {
            totalExpense = cursor.getDouble(0);
        }
        cursor.close();
        db.close();

        return totalExpense;
    }

    public int deleteRow(int anInt) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(anInt)});
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME, null, null);
        db.close();
    }

    public Cursor getAllExpenseDataForChart() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT expenseCategory,sum("+ COLUMN_AMOUNT +") as amount FROM " + TABLE_NAME + " group by expenseCategory";
        Cursor cursor = db.rawQuery(query, null);
        return cursor;

    }

    public Cursor getExpenseById(Integer id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " + TABLE_NAME + " where " + COLUMN_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        return cursor;
    }

    public Cursor getExpenseByIds(int[] items) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<items.length;i++) {
            stringBuilder.append(items[i]);
            if(i != items.length-1) {
                stringBuilder.append(",");
            }

        }
        String query = "SELECT * from " + TABLE_NAME + " where " + COLUMN_ID + " in ( "+ stringBuilder +" )";

        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }



    public int deleteRowSuggestion(int anInt) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(DatabaseHelper.TABLE_NAME_1, DatabaseHelper.COLUMN_ID_1 + " = ?", new String[]{String.valueOf(anInt)});
    }

    // Method to add a new suggestion
    public void addSuggestion(String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION_1, description);
        db.insert(TABLE_NAME_1, null, values);
        db.close();
    }

    // Method to get all suggestions
    public List<Suggestion> getAllSuggestions() {
        List<Suggestion> suggestionList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Suggestion suggestion = new Suggestion();
                suggestion.setId(cursor.getInt(0));
                suggestion.setDescription(cursor.getString(1));
                suggestionList.add(suggestion);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return suggestionList;
    }


    public boolean checkIfExists(String expCategory, String expAmt, String expDateTime, String expDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id from " + TABLE_NAME + " where "+ COLUMN_AMOUNT + "=? and "+COLUMN_EXPENSE_CATEGORY+" =? " +
                " and "+COLUMN_DATE_TIME+" =? and "+ COLUMN_DATE + " =? ";
        Cursor cursor = db.rawQuery(query, new String[]{expAmt,expCategory,expDateTime, expDate});
        boolean isExists = false;
        if(cursor.getCount()>0){
            isExists = true;
        }
        return isExists;
    }

    public boolean checkIfExistsMinPossibleParams(int expAmt, String expDateTime, String expDate) {
        boolean isExists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id from " + TABLE_NAME + " where " + " "+ COLUMN_AMOUNT + "=? and "
                + COLUMN_DATE_TIME + " =? and "+ COLUMN_DATE + " =? ";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(expAmt),expDateTime, expDate});
        if(cursor.getCount()>0){
            isExists = true;
        }
        return isExists;
    }

    public void insertIgnoredTransaction(double amount, String dateTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("dateTime", dateTime);
        db.insert("txn_ignore", null, cv);
    }

    public boolean isTransactionIgnored(double amount, String dateTime) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM txn_ignore WHERE amount = ? AND dateTime = ?", new String[]{
                String.valueOf(amount), dateTime
        });

        while(cursor.moveToNext()) {
            Log.i("TXN", cursor.getString(1)+" " + cursor.getString(2));
        }


        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    public List<ExpenseItem> getMonthData(String month) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor sqlRows = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "+COLUMN_DATE+ " like '"+month+"%' ",null);

        List<ExpenseItem> expenseItemList = new ArrayList<>();

        while(sqlRows.moveToNext()) {
            int id = sqlRows.getInt(0);
            String expCat = sqlRows.getString(1);
            String pert = sqlRows.getString(2);
            long amt = sqlRows.getInt(3);
            String dtm = sqlRows.getString(4);
            String dt = sqlRows.getString(5);
            String flnm = sqlRows.getString(6);

            ExpenseItem item = new ExpenseItem(id, pert, amt,dt,expCat,flnm,null);
            expenseItemList.add(item);
        }

        return expenseItemList;

    }
}

