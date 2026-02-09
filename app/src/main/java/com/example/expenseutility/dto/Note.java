package com.example.expenseutility.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Note {
    private int id;
    private String title;
    private Date createdDate;
    private Date modifiedDate;
    private String color;
    private boolean isPinned;
    private boolean isArchived;
    private List<NoteContent> contents;
    private List<String> labels;

    public Note() {
        this.createdDate = new Date();
        this.modifiedDate = new Date();
        this.contents = new ArrayList<>();
        this.labels = new ArrayList<>();
        this.color = "#FFFFFF";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public List<NoteContent> getContents() {
        return contents;
    }

    public void setContents(List<NoteContent> contents) {
        this.contents = contents;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void addContent(NoteContent content) {
        this.contents.add(content);
    }

    public void removeContent(NoteContent content) {
        this.contents.remove(content);
    }
}