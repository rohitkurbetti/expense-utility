package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.FileHolder;

import java.util.List;

public class FileListAdapter extends ArrayAdapter<FileHolder> {

    public FileListAdapter(Context context, List<FileHolder> files) {
        super(context, 0, files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        FileHolder file = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_file, parent, false);
        }

        TextView tvFileName = convertView.findViewById(R.id.tvFileName);
        TextView tvFileType = convertView.findViewById(R.id.tvFileType);
        TextView tvSerialNo = convertView.findViewById(R.id.tvSerialNo);
        TextView tvUri = convertView.findViewById(R.id.tvUri);

        if (file != null) {
            tvFileName.setText(file.getFileName());
            tvUri.setText(file.getUri());
            
            // Set Serial Number
            tvSerialNo.setText(String.valueOf(position + 1));
            
            // Determine and set File Type
            String fileName = file.getFileName();
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1).toUpperCase();
            }
            
            String fileType = extension.isEmpty() ? "Unknown File" : extension + " File";
            if (extension.equals("PDF")) fileType = "PDF Document";
            else if (extension.equals("JPG") || extension.equals("JPEG") || extension.equals("PNG")) fileType = "Image";
            else if (extension.equals("DOC") || extension.equals("DOCX")) fileType = "Word Document";
            else if (extension.equals("XLS") || extension.equals("XLSX")) fileType = "Excel Spreadsheet";
            else if (extension.equals("TXT")) fileType = "Text File";
            
            tvFileType.setText(fileType);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
