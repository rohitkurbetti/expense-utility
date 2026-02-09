package com.example.expenseutility.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class NoteContent implements Parcelable {
    public static final Creator<NoteContent> CREATOR = new Creator<NoteContent>() {
        @Override
        public NoteContent createFromParcel(Parcel in) {
            return new NoteContent(in);
        }

        @Override
        public NoteContent[] newArray(int size) {
            return new NoteContent[size];
        }
    };
    private int id;
    private int noteId;
    private NoteType type;
    private String content;
    private String filePath;
    private int positionX;
    private int positionY;
    private int width;
    private int height;
    private int order;

    public NoteContent() {
    }

    // Parcelable implementation
    protected NoteContent(Parcel in) {
        id = in.readInt();
        noteId = in.readInt();
        type = NoteType.valueOf(in.readString());
        content = in.readString();
        filePath = in.readString();
        positionX = in.readInt();
        positionY = in.readInt();
        width = in.readInt();
        height = in.readInt();
        order = in.readInt();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public NoteType getType() {
        return type;
    }

    public void setType(NoteType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(noteId);
        dest.writeString(type.name());
        dest.writeString(content);
        dest.writeString(filePath);
        dest.writeInt(positionX);
        dest.writeInt(positionY);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(order);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
