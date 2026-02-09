package com.example.expenseutility.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDBHelper extends SQLiteOpenHelper {
    // Tables
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_NOTE_CONTENTS = "note_contents";
    public static final String TABLE_LABELS = "labels";
    public static final String TABLE_NOTE_LABELS = "note_labels";
    // Common columns
    public static final String COLUMN_ID = "id";
    // Notes table columns
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CREATED_DATE = "created_date";
    public static final String COLUMN_MODIFIED_DATE = "modified_date";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IS_PINNED = "is_pinned";
    public static final String COLUMN_IS_ARCHIVED = "is_archived";
    // Note contents table columns
    public static final String COLUMN_NOTE_ID = "note_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_FILE_PATH = "file_path";
    public static final String COLUMN_POSITION_X = "position_x";
    public static final String COLUMN_POSITION_Y = "position_y";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_ORDER = "content_order";
    // Labels table columns
    public static final String COLUMN_LABEL_NAME = "label_name";
    public static final String COLUMN_LABEL_COLOR = "label_color";
    private static final String DATABASE_NAME = "keep_notes.db";
    private static final int DATABASE_VERSION = 1;

    public NoteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create notes table
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_MODIFIED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_COLOR + " TEXT DEFAULT '#FFFFFF',"
                + COLUMN_IS_PINNED + " INTEGER DEFAULT 0,"
                + COLUMN_IS_ARCHIVED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);

        // Create note_contents table
        String CREATE_NOTE_CONTENTS_TABLE = "CREATE TABLE " + TABLE_NOTE_CONTENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE_ID + " INTEGER,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_FILE_PATH + " TEXT,"
                + COLUMN_POSITION_X + " INTEGER DEFAULT 0,"
                + COLUMN_POSITION_Y + " INTEGER DEFAULT 0,"
                + COLUMN_WIDTH + " INTEGER DEFAULT 300,"
                + COLUMN_HEIGHT + " INTEGER DEFAULT 200,"
                + COLUMN_ORDER + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_NOTE_ID + ") REFERENCES " + TABLE_NOTES + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_NOTE_CONTENTS_TABLE);

        // Create labels table
        String CREATE_LABELS_TABLE = "CREATE TABLE " + TABLE_LABELS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LABEL_NAME + " TEXT UNIQUE,"
                + COLUMN_LABEL_COLOR + " TEXT DEFAULT '#4CAF50'"
                + ")";
        db.execSQL(CREATE_LABELS_TABLE);

        // Create note_labels junction table
        String CREATE_NOTE_LABELS_TABLE = "CREATE TABLE " + TABLE_NOTE_LABELS + "("
                + COLUMN_NOTE_ID + " INTEGER,"
                + "label_id INTEGER,"
                + "PRIMARY KEY(" + COLUMN_NOTE_ID + ", label_id),"
                + "FOREIGN KEY(" + COLUMN_NOTE_ID + ") REFERENCES " + TABLE_NOTES + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(label_id) REFERENCES " + TABLE_LABELS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_NOTE_LABELS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_LABELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_CONTENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }
}
