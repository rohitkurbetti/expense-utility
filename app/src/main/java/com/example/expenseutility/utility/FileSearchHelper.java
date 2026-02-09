package com.example.expenseutility.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileSearchHelper {

    private static final String TAG = "FileSearchHelper";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Search for a file by name in common public directories
     */
    public static void searchAndOpenFile(Context context, String fileName, FileSearchListener listener) {
        executor.execute(() -> {
            try {
                Uri fileUri = findFileInStorage(context, fileName);

                if (fileUri != null) {
                    // Post result on main thread
                    new android.os.Handler(context.getMainLooper()).post(() -> {
                        listener.onFileFound(fileUri);
                        openFileWithProvider(context, fileUri);
                    });
                } else {
                    new android.os.Handler(context.getMainLooper()).post(() -> {
                        listener.onFileNotFound();
                        Toast.makeText(context, "File not found: " + fileName, Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching file: " + e.getMessage(), e);
                new android.os.Handler(context.getMainLooper()).post(() -> {
                    listener.onError(e.getMessage());
                    Toast.makeText(context, "Error searching file", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Main method to find file in storage
     */
    public static Uri findFileInStorage(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        // First try MediaStore (for Android 10+)
        Uri mediaStoreUri = findFileViaMediaStore(context, fileName);
        if (mediaStoreUri != null) {
            Log.d(TAG, "Found via MediaStore: " + fileName);
            return mediaStoreUri;
        }

        // Then try direct file access (for older versions or if MediaStore doesn't find it)
        Uri directUri = findFileViaDirectAccess(context, fileName);
        if (directUri != null) {
            Log.d(TAG, "Found via direct access: " + fileName);
            return directUri;
        }

        Log.d(TAG, "File not found: " + fileName);
        return null;
    }

    /**
     * Search using MediaStore (works better on Android 10+)
     */
    private static Uri findFileViaMediaStore(Context context, String fileName) {
        ContentResolver resolver = context.getContentResolver();

        // Clean the filename - remove special characters
        String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "");

        // Search in different MediaStore collections
        String[] searchQueries = {
                // Search in Downloads
                MediaStore.Files.getContentUri("external").toString(),
                // Search in Images
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString(),
                // Search in Documents
                MediaStore.Files.getContentUri("external").toString(),
                // Search in Videos
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString(),
                // Search in Audio
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()
        };

        for (String uriString : searchQueries) {
            try {
                Uri uri = Uri.parse(uriString);
                String[] projection = new String[]{
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATA
                };

                // Try exact match first
                String selection = MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?";
                String[] selectionArgs = new String[]{fileName};

                Cursor cursor = resolver.query(
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
                );

                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            int idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                            int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);

                            if (idIndex != -1 && nameIndex != -1) {
                                String foundName = cursor.getString(nameIndex);
                                if (foundName != null && foundName.equalsIgnoreCase(fileName)) {
                                    long id = cursor.getLong(idIndex);
                                    return Uri.withAppendedPath(uri, String.valueOf(id));
                                }
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }

                // If exact match fails, try partial match
                selection = MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + cleanFileName + "%"};

                cursor = resolver.query(
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
                );

                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            int idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                            int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);

                            if (idIndex != -1 && nameIndex != -1) {
                                String foundName = cursor.getString(nameIndex);
                                if (foundName != null && foundName.toLowerCase().contains(cleanFileName.toLowerCase())) {
                                    long id = cursor.getLong(idIndex);
                                    return Uri.withAppendedPath(uri, String.valueOf(id));
                                }
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error querying MediaStore: " + e.getMessage(), e);
                // Continue to next query
            }
        }

        return null;
    }

    /**
     * Search using direct file access (for older Android versions)
     */
    private static Uri findFileViaDirectAccess(Context context, String fileName) {
        // Clean the filename
        String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "");

        // Common public directories to search
        String[] searchPaths = {
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_DOCUMENTS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC
        };

        // Also search in specific common subdirectories
        String[] additionalPaths = {
                "DCIM/Camera",
                "Pictures",
                "Documents",
                "Download",
                "WhatsApp/Media/WhatsApp Images",
                "WhatsApp/Media/WhatsApp Documents",
                "Telegram/Telegram Documents",
                "Telegram/Telegram Images"
        };

        // Try standard public directories
        for (String directory : searchPaths) {
            try {
                File publicDir = Environment.getExternalStoragePublicDirectory(directory);
                if (publicDir != null && publicDir.exists()) {
                    File foundFile = searchFileRecursively(publicDir, fileName, cleanFileName);
                    if (foundFile != null && foundFile.exists()) {
                        return getUriForFile(context, foundFile);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching in " + directory + ": " + e.getMessage());
            }
        }

        // Try additional paths
        File externalStorage = Environment.getExternalStorageDirectory();
        for (String path : additionalPaths) {
            try {
                File dir = new File(externalStorage, path);
                if (dir.exists()) {
                    File foundFile = searchFileRecursively(dir, fileName, cleanFileName);
                    if (foundFile != null && foundFile.exists()) {
                        return getUriForFile(context, foundFile);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching in " + path + ": " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Recursively search for file in directory and subdirectories
     */
    private static File searchFileRecursively(File directory, String exactFileName, String cleanFileName) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return null;
        }

        // Limit recursion depth to avoid too much searching
        return searchFileRecursively(directory, exactFileName, cleanFileName, 0, 5);
    }

    private static File searchFileRecursively(File directory, String exactFileName, String cleanFileName,
                                              int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth) {
            return null;
        }

        try {
            File[] files = directory.listFiles();
            if (files == null) {
                return null;
            }

            // First, check files in current directory (exact match)
            for (File file : files) {
                if (file.isFile()) {
                    String currentFileName = file.getName();
                    // Exact match
                    if (currentFileName.equalsIgnoreCase(exactFileName)) {
                        return file;
                    }
                    // Partial match with cleaned filename
                    if (currentFileName.toLowerCase().contains(cleanFileName.toLowerCase())) {
                        return file;
                    }
                }
            }

            // Then, recursively search subdirectories
            for (File file : files) {
                if (file.isDirectory()) {
                    File found = searchFileRecursively(file, exactFileName, cleanFileName,
                            currentDepth + 1, maxDepth);
                    if (found != null) {
                        return found;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in recursive search: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get proper URI for file (uses FileProvider for Android 7+)
     */
    private static Uri getUriForFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use FileProvider for Android 7.0+
            try {
                return FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        file
                );
            } catch (Exception e) {
                Log.e(TAG, "Error getting URI with FileProvider: " + e.getMessage());
                // Fallback to file URI
                return Uri.fromFile(file);
            }
        } else {
            // For older versions, use file URI
            return Uri.fromFile(file);
        }
    }

    /**
     * Open file with appropriate app using Intent
     */
    public static void openFileWithProvider(Context context, Uri fileUri) {
        try {
            Intent openIntent = new Intent(Intent.ACTION_VIEW);

            // Get MIME type
            String mimeType = getMimeType(context, fileUri);
            if (mimeType == null) {
                mimeType = "*/*";
            }

            // For Android 7.0+ (API 24+), grant permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // Additional flags
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            openIntent.setDataAndType(fileUri, mimeType);

            // Check if there's an app that can handle this intent
            if (openIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(openIntent);
                Toast.makeText(context, "Opening file...", Toast.LENGTH_SHORT).show();
            } else {
                // Try with generic MIME type
                openIntent.setDataAndType(fileUri, "*/*");
                if (openIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(openIntent);
                    Toast.makeText(context, "Opening file with default app...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "No app found to open this file", Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error opening file: " + e.getMessage(), e);
            Toast.makeText(context, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open file (legacy method - uses openFileWithProvider)
     */
    public static void openFile(Context context, Uri fileUri) {
//        openFileWithProvider(context, fileUri);
        openDocument(context, fileUri);

    }

    public static void openDocument(Context context, Uri documentUri) {
        try {
            // Use ACTION_VIEW instead of ACTION_OPEN_DOCUMENT to open the file
            Intent openIntent = new Intent(Intent.ACTION_VIEW);

            // Set the URI and MIME type
            String mimeType = getMimeTypeFromUri(context, documentUri);
            openIntent.setDataAndType(documentUri, mimeType);

            // Grant read permission
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Check if there's an app that can handle this intent
            if (openIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(openIntent);
                Toast.makeText(context, "Opening document...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error opening document: " + e.getMessage(), e);
            Toast.makeText(context, "Error opening document", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openWithViewIntent(Context context, Uri documentUri) {
        try {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);

            String mimeType = getMimeTypeFromUri(context, documentUri);
            viewIntent.setDataAndType(documentUri, mimeType);
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (viewIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(viewIntent);
                Toast.makeText(context, "Opening file...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error with VIEW intent: " + e.getMessage());
            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    private static String getMimeTypeFromUri(Context context, Uri uri) {
        String mimeType = null;

        try {
            if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                mimeType = context.getContentResolver().getType(uri);
            }

            if (mimeType == null) {
                String path = uri.getPath();
                if (path != null) {
                    int dotIndex = path.lastIndexOf('.');
                    if (dotIndex > 0) {
                        String extension = path.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting MIME type: " + e.getMessage());
        }

        // Default MIME type
        if (mimeType == null) {
            mimeType = "*/*";
        }

        return mimeType;
    }

    /**
     * Get MIME type from file URI
     */
    private static String getMimeType(Context context, Uri uri) {
        String mimeType = null;

        try {
            if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                ContentResolver resolver = context.getContentResolver();
                mimeType = resolver.getType(uri);
            }

            if (mimeType == null) {
                String path = uri.getPath();
                if (path != null) {
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting MIME type: " + e.getMessage());
        }

        if (mimeType == null) {
            mimeType = "*/*";
        }

        return mimeType;
    }

    /**
     * Get file name from URI
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        try {
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        // Try different column names
                        String[] columnNames = {
                                OpenableColumns.DISPLAY_NAME,
                                "_display_name",
                                "display_name"
                        };

                        for (String columnName : columnNames) {
                            int nameIndex = cursor.getColumnIndex(columnName);
                            if (nameIndex != -1) {
                                result = cursor.getString(nameIndex);
                                if (result != null) {
                                    break;
                                }
                            }
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name: " + e.getMessage());
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    /**
     * Search for multiple files by names
     */
    public static void searchMultipleFiles(Context context, List<String> fileNames, MultipleFilesSearchListener listener) {
        executor.execute(() -> {
            try {
                List<Uri> foundFiles = new ArrayList<>();
                int searchedCount = 0;

                for (String fileName : fileNames) {
                    Uri fileUri = findFileInStorage(context, fileName);
                    if (fileUri != null) {
                        foundFiles.add(fileUri);
                    }
                    searchedCount++;

                    // Report partial progress
                    final int currentCount = searchedCount;
                    final List<Uri> currentFound = new ArrayList<>(foundFiles);
                    new android.os.Handler(context.getMainLooper()).post(() ->
                            listener.onPartialResults(currentFound, currentCount));
                }

                final List<Uri> finalFound = new ArrayList<>(foundFiles);
                new android.os.Handler(context.getMainLooper()).post(() ->
                        listener.onFilesFound(finalFound));

            } catch (Exception e) {
                Log.e(TAG, "Error searching multiple files: " + e.getMessage(), e);
                new android.os.Handler(context.getMainLooper()).post(() ->
                        listener.onError(e.getMessage()));
            }
        });
    }

    public interface FileSearchListener {
        void onFileFound(Uri fileUri);

        void onFileNotFound();

        void onError(String errorMessage);
    }

    public interface MultipleFilesSearchListener {
        void onFilesFound(List<Uri> fileUris);

        void onPartialResults(List<Uri> foundFiles, int totalSearched);

        void onError(String errorMessage);
    }
}