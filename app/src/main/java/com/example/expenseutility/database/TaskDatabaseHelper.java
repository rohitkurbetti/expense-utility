package com.example.expenseutility.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expenseutility.dto.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    // Table name and columns
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_SOURCE_APP = "source_app";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String KEY_TASK_ATTACHED_FILES = "attached_files"; // New column

    private static final String DATABASE_NAME = "ExpenseUtilityDB";
    private static final int DATABASE_VERSION = 2; // Increased version
    // Create table SQL (updated with new columns)
    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITLE + " TEXT NOT NULL," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_PRIORITY + " TEXT DEFAULT '3'," +
                    COLUMN_DUE_DATE + " TEXT," +
                    COLUMN_AMOUNT + " TEXT DEFAULT '0.0'," +
                    COLUMN_TYPE + " TEXT DEFAULT 'Expense'," +
                    COLUMN_STATUS + " TEXT DEFAULT 'Pending'," +
                    COLUMN_SOURCE_APP + " TEXT," +
                    COLUMN_TIMESTAMP + " INTEGER," +
                    KEY_TASK_ATTACHED_FILES + " TEXT" +
                    ")";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns if upgrading from version 1
            db.execSQL("ALTER TABLE " + TABLE_TASKS +
                    " ADD COLUMN " + COLUMN_AMOUNT + " TEXT DEFAULT '0.0'");
            db.execSQL("ALTER TABLE " + TABLE_TASKS +
                    " ADD COLUMN " + COLUMN_TYPE + " TEXT DEFAULT 'Expense'");
        }
    }

    // Add a new task (This is the main function triggered by BroadcastReceiver)
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_AMOUNT, task.getAmount());
        values.put(COLUMN_TYPE, task.getType());
        values.put(COLUMN_STATUS, task.getStatus());
        values.put(COLUMN_SOURCE_APP, task.getSourceApp());
        values.put(COLUMN_TIMESTAMP, task.getTimestamp());
        values.put(KEY_TASK_ATTACHED_FILES, task.getAttachedFiles()); // New field

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();

        return id;
    }

    // Get all tasks
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_TASKS +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
                task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
                task.setAmount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                task.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                task.setSourceApp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE_APP)));
                task.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                task.setAttachedFiles(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_ATTACHED_FILES)));

                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tasks;
    }

    // Get tasks by source app
    public List<Task> getTasksBySource(String sourceApp) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COLUMN_SOURCE_APP + " = ?",
                new String[]{sourceApp},
                null, null,
                COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
                task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
                task.setAmount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                task.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                task.setSourceApp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE_APP)));
                task.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tasks;
    }

    // Get today's tasks
    public List<Task> getTodaysTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        long today = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;
        long yesterday = today - oneDay;

        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COLUMN_TIMESTAMP + " >= ?",
                new String[]{String.valueOf(yesterday)},
                null, null,
                COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
                task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
                task.setAmount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                task.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                task.setSourceApp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE_APP)));
                task.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tasks;
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, null, null);
        db.close();
    }

    public long deleteTaskByTimestamp(long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_TASKS,
                COLUMN_TIMESTAMP + " = ?",
                new String[]{String.valueOf(timestamp)});
        db.close();
        return rowsDeleted;
    }

    public long updateTask(long timestamp, Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Put all task fields into ContentValues
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_AMOUNT, task.getAmount());
        values.put(COLUMN_TYPE, task.getType());
        values.put(COLUMN_STATUS, task.getStatus());
        values.put(COLUMN_SOURCE_APP, task.getSourceApp());
        values.put(COLUMN_TIMESTAMP, task.getTimestamp());

        // Update the task where timestamp matches (assuming timestamp is unique identifier)
        int rowsAffected = db.update(TABLE_TASKS,
                values,
                COLUMN_TIMESTAMP + " = ?",
                new String[]{String.valueOf(timestamp)});

        db.close();

        // Return the number of rows updated (0 if no task was found with that timestamp)
        return rowsAffected;

    }

    public int getTaskCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_TASKS;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return count;
    }

    public int getPendingTaskCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_TASKS +
                " WHERE " + COLUMN_STATUS + " = 'Pending'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return count;
    }
}