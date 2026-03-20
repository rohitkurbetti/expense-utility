package com.example.expenseutility.entityadapter;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.FileHolder;
import com.example.expenseutility.dto.Task;
import com.example.expenseutility.utility.FileSearchHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private SimpleDateFormat dateFormat;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Bind data to views
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.taskCategory.setText(task.getType());
        holder.tvPriority.setText("See Files");

        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
            holder.tvDueDate.setText("Due: " + task.getDueDate());
            holder.tvDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDueDate.setVisibility(View.GONE);
        }

        if (task.getAmount() != null && !task.getAmount().equals("0.0")) {
            holder.tvAmount.setText("$" + task.getAmount());
            holder.tvAmount.setVisibility(View.VISIBLE);
        } else {
            holder.tvAmount.setVisibility(View.GONE);
        }

        holder.tvSource.setText("From: " + task.getSourceApp());
        holder.tvStatus.setText("Status: " + task.getStatus());

        // Format timestamp
        String formattedDate = dateFormat.format(new Date(task.getTimestamp()));
        holder.tvTimestamp.setText(formattedDate);


        holder.tvPriority.setOnClickListener(v -> {
            if (context instanceof androidx.fragment.app.FragmentActivity) {
                com.example.expenseutility.fragments.AttachedFilesBottomSheet bottomSheet =
                        com.example.expenseutility.fragments.AttachedFilesBottomSheet.newInstance(task.getAttachedFiles());
                bottomSheet.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "AttachedFilesBottomSheet");
            } else {
                Toast.makeText(context, "Cannot open file list: Context is not an Activity", Toast.LENGTH_SHORT).show();
            }
        });


        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Selected: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    // ViewHolder class
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvPriority, tvDueDate, tvAmount, tvSource, tvStatus, tvTimestamp, taskCategory;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            taskCategory = itemView.findViewById(R.id.task_category);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}