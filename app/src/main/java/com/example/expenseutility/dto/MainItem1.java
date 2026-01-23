package com.example.expenseutility.dto;

import java.util.List;

public class MainItem1 {
    private String title;
    private List<SubItem1> subItems;

    public MainItem1(String title, List<SubItem1> subItems) {
        this.title = title;
        this.subItems = subItems;
    }

    public String getTitle() {
        return title;
    }

    public List<SubItem1> getSubItems() {
        return subItems;
    }
}