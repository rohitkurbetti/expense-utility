package com.example.expenseutility.entityadapter;

import java.io.Serializable;

public class Suggestion implements Serializable {

    private int id;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Suggestion(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public Suggestion() {

    }

    public Suggestion(String description) {
        this.description = description;
    }
}
