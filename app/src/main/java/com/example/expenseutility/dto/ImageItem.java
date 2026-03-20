package com.example.expenseutility.dto;

public class ImageItem {
    private long id;
    private String path;
    private String name;
    private long date;
    private long size;
    private boolean selected;

    public ImageItem(long id, String path, String name, long date, long size) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.date = date;
        this.size = size;
        this.selected = false;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}