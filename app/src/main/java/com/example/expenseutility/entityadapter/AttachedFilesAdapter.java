package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.FileHolder;
import com.example.expenseutility.utility.FileSearchHelper;

import java.util.List;

public class AttachedFilesAdapter extends RecyclerView.Adapter<AttachedFilesAdapter.FileViewHolder> {

    private Context context;
    private List<FileHolder> fileList;

    public AttachedFilesAdapter(Context context, List<FileHolder> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file_attachment, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileHolder file = fileList.get(position);

        holder.tvFileName.setText(file.getFileName());
        
        // Try to determine file type for display
        String fileName = file.getFileName();
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toUpperCase();
        }
        
        if (!extension.isEmpty()) {
            holder.tvFileType.setText(extension + " File • Tap to open");
        } else {
            holder.tvFileType.setText("Tap to open");
        }

        holder.itemView.setOnClickListener(v -> {
            Uri fileUri = FileSearchHelper.findFileInStorage(context, file.getFileName());
            FileSearchHelper.openFile(context, fileUri);
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void updateList(List<FileHolder> newList) {
        this.fileList = newList;
        notifyDataSetChanged();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvFileType;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileType = itemView.findViewById(R.id.tvFileType);
        }
    }
}
