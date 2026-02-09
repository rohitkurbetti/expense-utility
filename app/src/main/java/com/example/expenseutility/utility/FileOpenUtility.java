package com.example.expenseutility.utility;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FileOpenUtility {

    /**
     * Parse JSON and search for files in storage
     */
    public static void openFilesFromJson(Context context, String filesJson) {
        if (filesJson == null || filesJson.isEmpty()) {
            Toast.makeText(context, "No attached files", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(filesJson);
            JSONArray fileNamesArray = jsonObject.getJSONArray("file_names");

            List<String> fileNames = new ArrayList<>();
            for (int i = 0; i < fileNamesArray.length(); i++) {
                fileNames.add(fileNamesArray.getString(i));
            }

            if (fileNames.isEmpty()) {
                Toast.makeText(context, "No files found in JSON", Toast.LENGTH_SHORT).show();
                return;
            }

            // Search for the first file and open it
            String firstFileName = fileNames.get(0);
            FileSearchHelper.searchAndOpenFile(context, firstFileName, new FileSearchHelper.FileSearchListener() {
                @Override
                public void onFileFound(Uri fileUri) {
                    Toast.makeText(context, "Opened: " + firstFileName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFileNotFound() {
                    Toast.makeText(context, "Could not find: " + firstFileName, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error parsing file data", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get all file names from JSON
     */
    public static List<String> getFileNamesFromJson(String filesJson) {
        List<String> fileNames = new ArrayList<>();

        if (filesJson == null || filesJson.isEmpty()) {
            return fileNames;
        }

        try {
            JSONObject jsonObject = new JSONObject(filesJson);
            JSONArray fileNamesArray = jsonObject.getJSONArray("file_names");

            for (int i = 0; i < fileNamesArray.length(); i++) {
                fileNames.add(fileNamesArray.getString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fileNames;
    }

    /**
     * Search and open all files from JSON
     */
    public static void searchAndOpenAllFiles(Context context, String filesJson) {
        List<String> fileNames = getFileNamesFromJson(filesJson);

        if (fileNames.isEmpty()) {
            Toast.makeText(context, "No files to open", Toast.LENGTH_SHORT).show();
            return;
        }

        FileSearchHelper.searchMultipleFiles(context, fileNames, new FileSearchHelper.MultipleFilesSearchListener() {
            @Override
            public void onFilesFound(List<Uri> fileUris) {
                // Open all found files
                for (Uri fileUri : fileUris) {
                    FileSearchHelper.openFile(context, fileUri);
                }

                if (fileUris.size() < fileNames.size()) {
                    Toast.makeText(context,
                            "Opened " + fileUris.size() + " of " + fileNames.size() + " files",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPartialResults(List<Uri> foundFiles, int totalSearched) {
                // Optional: Show progress
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
