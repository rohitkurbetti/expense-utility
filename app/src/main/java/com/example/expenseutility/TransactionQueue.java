package com.example.expenseutility;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class TransactionQueue {
    private static TransactionQueue instance;
    private final List<String> messageQueue = new ArrayList<>();
    private final Context context;

    private TransactionQueue(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized TransactionQueue getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionQueue(context);
        }
        return instance;
    }

    public void addMessage(String message) {
        messageQueue.add(message);
        // Save to DB if needed
    }

    public List<String> getMessages() {
        return new ArrayList<>(messageQueue);
    }

    public int getCount() {
        return messageQueue.size();
    }

    public void clear() {
        messageQueue.clear();
    }
}
