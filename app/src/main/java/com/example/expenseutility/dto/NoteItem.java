package com.example.expenseutility.dto;

public class NoteItem {
    private int id;
    private int noteId;
    private int itemType; // 0: text, 1: image, 2: link, 3: document, 4: checklist, 5: audio
    private String content;
    private String metaData; // For additional info like image path, URL, file path, etc.
    private int positionX;
    private int positionY;
    private int width;
    private int height;
    private int sequence;
    private boolean checked; // For checklist items

    // Constructors
    public NoteItem() {
        this.positionX = 0;
        this.positionY = 0;
        this.width = 300;
        this.height = 200;
        this.checked = false;
    }

    public NoteItem(int noteId, int itemType, String content) {
        this.noteId = noteId;
        this.itemType = itemType;
        this.content = content;
        this.positionX = 0;
        this.positionY = 0;
        this.width = 300;
        this.height = 200;
        this.checked = false;
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

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}