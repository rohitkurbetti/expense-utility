package com.example.expenseutility;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.dto.ImageItem;
import com.example.expenseutility.entityadapter.ImageFileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CleanActivity extends AppCompatActivity implements ImageFileAdapter.OnSelectionChangedListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PREVIEW_REQUEST_CODE = 200;

    private RecyclerView recyclerView;
    private Button btnDelete, btnSelectAll, btnSelectNone, btnToggleView;
    private TextView tvTotalFiles, tvTotalSize, tvSelectedSize;
    private ImageFileAdapter adapter;
    private List<ImageItem> imageList = new ArrayList<>();
    private boolean isListView = true; // true = list, false = grid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean);

        recyclerView = findViewById(R.id.recyclerView);
        btnDelete = findViewById(R.id.btnDelete);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnSelectNone = findViewById(R.id.btnSelectNone);
        btnToggleView = findViewById(R.id.btnToggleView);
        tvTotalFiles = findViewById(R.id.tvTotalFiles);
        tvTotalSize = findViewById(R.id.tvTotalSize);
        tvSelectedSize = findViewById(R.id.tvSelectedSize);

        // Set initial layout manager (list)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageFileAdapter(this, imageList, this, isListView); // pass initial mode
        recyclerView.setAdapter(adapter);

        btnDelete.setOnClickListener(v -> deleteSelectedFiles());
        btnSelectAll.setOnClickListener(v -> selectAll(true));
        btnSelectNone.setOnClickListener(v -> selectAll(false));
        btnToggleView.setOnClickListener(v -> toggleViewMode());

        if (checkPermissions()) {
            loadOldImages();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(this, "Please grant 'All files access' in settings", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadOldImages();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadOldImages() {
        new Thread(() -> {
            List<ImageItem> items = scanWhatsAppImages();
            runOnUiThread(() -> {
                imageList.clear();
                imageList.addAll(items);
                adapter.notifyDataSetChanged();
                updateStats();
            });
        }).start();
    }

    private List<ImageItem> scanWhatsAppImages() {
        List<ImageItem> result = new ArrayList<>();
        ContentResolver resolver = getContentResolver();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,           // add ID
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE
        };

        String selection = MediaStore.Images.Media.DATA + " like ?";
        String[] selectionArgs = new String[]{"%WhatsApp%"};

        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

            long fourYearsAgo = getFourYearsAgo();

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String path = cursor.getString(pathColumn);
                String name = cursor.getString(nameColumn);
                long date = cursor.getLong(dateColumn) * 1000L;
                long size = cursor.getLong(sizeColumn);

                ImageItem item = new ImageItem(id, path, name, date, size);
                if (date < fourYearsAgo) {
                    item.setSelected(true);
                }
                result.add(item);
            }
            cursor.close();
        }
        return result;
    }

    private long getFourYearsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -4);
        return cal.getTimeInMillis();
    }

    private void deleteSelectedFiles() {
        List<ImageItem> toDelete = new ArrayList<>();
        long totalSelectedSize = 0;
        for (ImageItem item : imageList) {
            if (item.isSelected()) {
                toDelete.add(item);
                totalSelectedSize += item.getSize();
            }
        }

        if (toDelete.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete " + toDelete.size() +
                        " file(s) (" + formatSize(totalSelectedSize) + ")?\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        int successCount = 0;
                        for (ImageItem item : toDelete) {
                            if (deleteImageFile(item.getPath())) {
                                successCount++;
                            }
                        }

                        final int finalSuccessCount = successCount;
                        runOnUiThread(() -> {
                            Toast.makeText(CleanActivity.this,
                                    "Deleted " + finalSuccessCount + " of " + toDelete.size() + " files",
                                    Toast.LENGTH_LONG).show();
                            loadOldImages();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean deleteImageFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return file.delete();
            } else {
                String where = MediaStore.Images.Media.DATA + "=?";
                String[] selectionArgs = new String[]{path};
                int deleted = getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        where, selectionArgs);
                return deleted > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void selectAll(boolean select) {
        for (ImageItem item : imageList) {
            item.setSelected(select);
        }
        adapter.notifyDataSetChanged();
        updateStats();
    }

    private void toggleViewMode() {
        int firstVisiblePosition = -1;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        } else if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }

        if (isListView) {
            // Switch to grid (2 columns)
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            btnToggleView.setText("List");
            isListView = false;
        } else {
            // Switch to list
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            btnToggleView.setText("Grid");
            isListView = true;
        }

        adapter.setListView(isListView); // update adapter

        if (firstVisiblePosition != -1) {
            recyclerView.scrollToPosition(firstVisiblePosition);
        }
    }

    // Implementation of OnSelectionChangedListener
    @Override
    public void onSelectionChanged() {
        updateStats();
    }

    @Override
    public void onItemClick(ImageItem item) {
        int position = imageList.indexOf(item);
        Intent intent = new Intent(this, ImagePreviewActivity.class);

        long[] ids = new long[imageList.size()];
        boolean[] selectedStates = new boolean[imageList.size()];
        for (int i = 0; i < imageList.size(); i++) {
            ids[i] = imageList.get(i).getId();
            selectedStates[i] = imageList.get(i).isSelected();
        }

        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_IDS, ids);
        intent.putExtra(ImagePreviewActivity.EXTRA_SELECTED_STATES, selectedStates);
        intent.putExtra(ImagePreviewActivity.EXTRA_CURRENT_POSITION, position);
        startActivityForResult(intent, PREVIEW_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREVIEW_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            boolean[] updatedStates = data.getBooleanArrayExtra(ImagePreviewActivity.EXTRA_RESULT_SELECTED_STATES);
            if (updatedStates != null && updatedStates.length == imageList.size()) {
                for (int i = 0; i < imageList.size(); i++) {
                    imageList.get(i).setSelected(updatedStates[i]);
                }
                adapter.notifyDataSetChanged();
                updateStats();
            }
        }
    }

    private void updateStats() {
        int totalFiles = imageList.size();
        long totalSize = 0;
        int selectedCount = 0;
        long selectedSize = 0;

        for (ImageItem item : imageList) {
            totalSize += item.getSize();
            if (item.isSelected()) {
                selectedCount++;
                selectedSize += item.getSize();
            }
        }

        tvTotalFiles.setText("Total Images: " + totalFiles);
        tvTotalSize.setText("Total Size: " + formatSize(totalSize));
        tvSelectedSize.setText("Selected: " + selectedCount + " files, " + formatSize(selectedSize));
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.2f %s",
                bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}