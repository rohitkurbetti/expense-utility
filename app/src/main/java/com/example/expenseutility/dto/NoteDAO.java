package com.example.expenseutility.dto;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.expenseutility.database.NoteDBHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteDAO {
    private SQLiteDatabase db;
    private NoteDBHelper dbHelper;

    public NoteDAO(Context context) {
        dbHelper = new NoteDBHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Note operations
    public long insertNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteDBHelper.COLUMN_TITLE, note.getTitle());
        values.put(NoteDBHelper.COLUMN_COLOR, note.getColor());
        values.put(NoteDBHelper.COLUMN_IS_PINNED, note.isPinned() ? 1 : 0);
        values.put(NoteDBHelper.COLUMN_IS_ARCHIVED, note.isArchived() ? 1 : 0);
        values.put(NoteDBHelper.COLUMN_CREATED_DATE, note.getCreatedDate().getTime());
        values.put(NoteDBHelper.COLUMN_MODIFIED_DATE, note.getModifiedDate().getTime());

        long noteId = db.insert(NoteDBHelper.TABLE_NOTES, null, values);

        // Insert contents
        if (noteId != -1 && note.getContents() != null) {
            for (NoteContent content : note.getContents()) {
                insertNoteContent(content, (int) noteId);
            }
        }

        return noteId;
    }

    public int updateNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteDBHelper.COLUMN_TITLE, note.getTitle());
        values.put(NoteDBHelper.COLUMN_COLOR, note.getColor());
        values.put(NoteDBHelper.COLUMN_IS_PINNED, note.isPinned() ? 1 : 0);
        values.put(NoteDBHelper.COLUMN_IS_ARCHIVED, note.isArchived() ? 1 : 0);
        values.put(NoteDBHelper.COLUMN_MODIFIED_DATE, new Date().getTime());

        return db.update(NoteDBHelper.TABLE_NOTES, values,
                NoteDBHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(int noteId) {
        // Delete note contents first
        db.delete(NoteDBHelper.TABLE_NOTE_CONTENTS,
                NoteDBHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});

        // Delete note
        db.delete(NoteDBHelper.TABLE_NOTES,
                NoteDBHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(noteId)});
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();

        Cursor cursor = db.query(NoteDBHelper.TABLE_NOTES,
                null, null, null, null, null,
                NoteDBHelper.COLUMN_IS_PINNED + " DESC, " +
                        NoteDBHelper.COLUMN_MODIFIED_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Note note = cursorToNote(cursor);
                note.setContents(getNoteContents(note.getId()));
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return notes;
    }

    private Note cursorToNote(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_ID)));
        note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_TITLE)));
        note.setColor(cursor.getString(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_COLOR)));
        note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_IS_PINNED)) == 1);
        note.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_IS_ARCHIVED)) == 1);
        note.setCreatedDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_CREATED_DATE))));
        note.setModifiedDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_MODIFIED_DATE))));

        return note;
    }

    // NoteContent operations
    public long insertNoteContent(NoteContent content, int noteId) {
        ContentValues values = new ContentValues();
        values.put(NoteDBHelper.COLUMN_NOTE_ID, noteId);
        values.put(NoteDBHelper.COLUMN_TYPE, content.getType().name());
        values.put(NoteDBHelper.COLUMN_CONTENT, content.getContent());
        values.put(NoteDBHelper.COLUMN_FILE_PATH, content.getFilePath());
        values.put(NoteDBHelper.COLUMN_POSITION_X, content.getPositionX());
        values.put(NoteDBHelper.COLUMN_POSITION_Y, content.getPositionY());
        values.put(NoteDBHelper.COLUMN_WIDTH, content.getWidth());
        values.put(NoteDBHelper.COLUMN_HEIGHT, content.getHeight());
        values.put(NoteDBHelper.COLUMN_ORDER, content.getOrder());

        return db.insert(NoteDBHelper.TABLE_NOTE_CONTENTS, null, values);
    }

    public List<NoteContent> getNoteContents(int noteId) {
        List<NoteContent> contents = new ArrayList<>();

        Cursor cursor = db.query(NoteDBHelper.TABLE_NOTE_CONTENTS,
                null,
                NoteDBHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)},
                null, null,
                NoteDBHelper.COLUMN_ORDER + " ASC");

        if (cursor.moveToFirst()) {
            do {
                NoteContent content = new NoteContent();
                content.setId(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_ID)));
                content.setNoteId(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_NOTE_ID)));
                content.setType(NoteType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_TYPE))));
                content.setContent(cursor.getString(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_CONTENT)));
                content.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_FILE_PATH)));
                content.setPositionX(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_POSITION_X)));
                content.setPositionY(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_POSITION_Y)));
                content.setWidth(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_WIDTH)));
                content.setHeight(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_HEIGHT)));
                content.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(NoteDBHelper.COLUMN_ORDER)));

                contents.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return contents;
    }
}
