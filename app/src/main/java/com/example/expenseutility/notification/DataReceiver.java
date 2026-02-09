package com.example.expenseutility.notification;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.expenseutility.database.TaskDatabaseHelper;
import com.example.expenseutility.dto.Task;
import com.example.expenseutility.utility.NotificationHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Toast 1: Broadcast received
        Toast.makeText(context, "📡 Broadcast Received!", Toast.LENGTH_SHORT).show();

        // Extract task details from intent extras
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "No extras found in intent");
            return;
        }
        String filesJson = extras.getString("attached_files", "");
//        parseFilesJson(filesJson);

        if (extras.getString("operation_type", "").equalsIgnoreCase("DELETE")) {
            Task task = new Task();

            String title = extras.getString("task_title", "");
            String operationType = extras.getString("operation_type", "");
            long timestamp = extras.getLong("timestamp", 0);

            task.setAttachedFiles(filesJson);
            task.setTitle(title);
            task.setTimestamp(timestamp);

            // Store in database - This triggers the main function
            TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(context);
            long taskId = dbHelper.deleteTaskByTimestamp(task.getTimestamp());

            if (taskId != -1) {

                // Show notification to user
                showNotification(context, task, operationType);

                Toast.makeText(context,
                        "Task title '" + task.getTitle() + "' requested and deleted!",
                        Toast.LENGTH_LONG).show();


            } else {
                Toast.makeText(context, "Failed to save task!", Toast.LENGTH_SHORT).show();
            }

        } else if (extras.getString("operation_type", "").equalsIgnoreCase("UPDATE")) {


            String taskTitle = extras.getString("task_title", "");
            String taskDescription = extras.getString("task_description", "");
            String taskPriority = extras.getString("task_priority", "3");
            String taskDueDate = extras.getString("task_due_date", "");
//        String taskAmount = extras.getString("task_amount", "0.0");
            String taskType = extras.getString("task_type", "Expense");
            String sourceApp = extras.getString("source_app", "Unknown");
            long timestamp = extras.getLong("timestamp", 0);

            if (taskTitle.isEmpty()) {
                Log.e(TAG, "Empty task title received");
                Toast.makeText(context, "Received empty task!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create Task object
            Task task = new Task();
            task.setTitle(taskTitle);
            task.setDescription(taskDescription);
            task.setPriority(taskPriority);
            task.setDueDate(taskDueDate);
//        task.setAmount(taskAmount);
            task.setType(taskType);
            task.setSourceApp(sourceApp);
            task.setStatus("Pending");
            task.setTimestamp(timestamp);
            task.setAttachedFiles(filesJson);

            // Store in database - This triggers the main function
            TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(context);
            long taskId = dbHelper.updateTask(task.getTimestamp(), task);

            if (taskId != -1) {

                // Show notification to user
                showNotification(context, task, "UPDATE");

                Toast.makeText(context,
                        "Task title '" + task.getTitle() + "' requested and updated!",
                        Toast.LENGTH_LONG).show();


            } else {
                Toast.makeText(context, "Failed to save task!", Toast.LENGTH_SHORT).show();
            }


        } else {
            String taskTitle = extras.getString("task_title", "");
            String taskDescription = extras.getString("task_description", "");
            String taskPriority = extras.getString("task_priority", "3");
            String taskDueDate = extras.getString("task_due_date", "");
//        String taskAmount = extras.getString("task_amount", "0.0");
            String taskType = extras.getString("task_type", "Expense");
            String sourceApp = extras.getString("source_app", "Unknown");
            long timestamp = extras.getLong("timestamp", 0);

            if (taskTitle.isEmpty()) {
                Log.e(TAG, "Empty task title received");
                Toast.makeText(context, "Received empty task!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create Task object
            Task task = new Task();
            task.setTitle(taskTitle);
            task.setDescription(taskDescription);
            task.setPriority(taskPriority);
            task.setDueDate(taskDueDate);
//        task.setAmount(taskAmount);
            task.setType(taskType);
            task.setSourceApp(sourceApp);
            task.setStatus("Pending");
            task.setTimestamp(timestamp);
            task.setAttachedFiles(filesJson);

            // Store in database - This triggers the main function
            TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(context);
            long taskId = dbHelper.addTask(task);

            if (taskId != -1) {

                // Show notification to user
                showNotification(context, task, "");

                Toast.makeText(context,
                        "Task '" + taskTitle + "' received and saved!",
                        Toast.LENGTH_LONG).show();

//            processReceivedData(context, task.getTitle());

            } else {
                Toast.makeText(context, "Failed to save task!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseFilesJson(String filesJson) {
        try {
            JSONObject jsonObject = new JSONObject(filesJson);
            JSONArray fileNames = jsonObject.getJSONArray("file_names");
            JSONArray fileUris = jsonObject.getJSONArray("file_uris");

            for (int i = 0; i < fileNames.length(); i++) {
                String fileName = fileNames.getString(i);
                String fileUri = fileUris.getString(i);

                // Do something with the file information
                Log.d("FileInfo", "Name: " + fileName + ", URI: " + fileUri);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showNotification(Context context, Task task, String operationType) {
        // Create and show notification
        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (operationType.equalsIgnoreCase("DELETE")) {
            notificationHelper.createNotification(
                    task.getId(),
                    "Task Deleted",
                    task.getTitle() + " in " + context.getPackageName(),
                    "Click to view details"
            );
        } else if (operationType.equalsIgnoreCase("UPDATE")) {
            notificationHelper.createNotification(
                    task.getId(),
                    "Task Updated",
                    task.getTitle() + " in " + context.getPackageName(),
                    "Click to view details"
            );
        } else {


            notificationHelper.createNotification(
                    task.getId(),
                    "New Task Received",
                    task.getTitle() + " from " + task.getSourceApp(),
                    "Click to view details"
            );
        }
    }

    private void processReceivedData(Context context, String message) {
        // This function is triggered when data is received
        String result = "✅ Processed: " + message;
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
    }
}