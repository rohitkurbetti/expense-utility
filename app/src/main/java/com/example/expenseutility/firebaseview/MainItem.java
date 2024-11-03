package com.example.expenseutility.firebaseview;

import java.util.List;

// Model for the RecyclerView
public class MainItem {
    private String title;
    private String totalExpense;
    private List<NestedItem> nestedItemList;  // List of items for the nested ListView
    private boolean isExpanded;  // For toggling expansion

    public MainItem(String title, List<NestedItem> nestedItemList,String totalExpense) {
        this.title = title;
        this.nestedItemList = nestedItemList;
        this.totalExpense = totalExpense;
        this.isExpanded = false;  // Initially collapsed
    }

    public String getTitle() {
        return title;
    }

    public String getTotalExpense() {
        return totalExpense;
    }

    public List<NestedItem> getNestedItemList() {
        return nestedItemList;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @Override
    public String toString() {
        return "MainItem{" +
                "title='" + title + '\'' +
                ", totalExpense='" + totalExpense + '\'' +
                ", nestedItemList=" + nestedItemList +
                ", isExpanded=" + isExpanded +
                '}';
    }
}
