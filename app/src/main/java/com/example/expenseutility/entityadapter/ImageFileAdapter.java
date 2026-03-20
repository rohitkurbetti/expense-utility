package com.example.expenseutility.entityadapter;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.expenseutility.R;
import com.example.expenseutility.dto.ImageItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_GRID = 1;
    private List<ImageItem> imageList;
    private Context context;
    private OnSelectionChangedListener listener;
    private boolean isListView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ImageFileAdapter(Context context, List<ImageItem> imageList, OnSelectionChangedListener listener, boolean isListView) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
        this.isListView = isListView;
    }

    @Override
    public int getItemViewType(int position) {
        return isListView ? VIEW_TYPE_LIST : VIEW_TYPE_GRID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LIST) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new ListViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_grid, parent, false);
            return new GridViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageItem item = imageList.get(position);

        if (holder instanceof ListViewHolder) {
            ListViewHolder listHolder = (ListViewHolder) holder;
            listHolder.tvSrNo.setText(String.valueOf(position + 1));
            listHolder.tvFileName.setText(item.getName());
            listHolder.tvFileDate.setText(dateFormat.format(new Date(item.getDate())));
            listHolder.tvFileSize.setText(formatSize(item.getSize()));

            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.getId());
            Glide.with(context)
                    .load(contentUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .centerCrop()
                    .into(listHolder.ivThumbnail);

            // Checkbox handling
            listHolder.checkBox.setOnCheckedChangeListener(null);
            listHolder.checkBox.setChecked(item.isSelected());
            listHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) listener.onSelectionChanged();
            });

            listHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        } else if (holder instanceof GridViewHolder) {
            GridViewHolder gridHolder = (GridViewHolder) holder;

            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.getId());
            Glide.with(context)
                    .load(contentUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .centerCrop()
                    .into(gridHolder.ivThumbnail);

            // Checkbox handling
            gridHolder.checkBox.setOnCheckedChangeListener(null);
            gridHolder.checkBox.setChecked(item.isSelected());
            gridHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) listener.onSelectionChanged();
            });

            gridHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.2f %s",
                bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public void setListView(boolean isListView) {
        this.isListView = isListView;
        notifyDataSetChanged();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged();

        void onItemClick(ImageItem item);
    }

    // ViewHolder for list mode
    public static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo;
        ImageView ivThumbnail;
        CheckBox checkBox;
        TextView tvFileName, tvFileDate, tvFileSize;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileDate = itemView.findViewById(R.id.tvFileDate);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
        }
    }

    // ViewHolder for grid mode
    public static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        CheckBox checkBox;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}