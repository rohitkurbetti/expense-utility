package com.example.expenseutility;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImagePreviewFragment extends Fragment {

    private static final String ARG_IMAGE_ID = "image_id";
    private static final String ARG_IS_SELECTED = "is_selected";

    private long imageId;
    private boolean isSelected;
    private OnSelectionChangedListener callback;

    public static ImagePreviewFragment newInstance(long id, boolean selected, OnSelectionChangedListener listener) {
        Bundle args = new Bundle();
        args.putLong(ARG_IMAGE_ID, id);
        args.putBoolean(ARG_IS_SELECTED, selected);
        ImagePreviewFragment fragment = new ImagePreviewFragment();
        fragment.setArguments(args);
        fragment.callback = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageId = getArguments().getLong(ARG_IMAGE_ID);
            isSelected = getArguments().getBoolean(ARG_IS_SELECTED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview_page, container, false);

        PhotoView photoView = view.findViewById(R.id.photoView);
        CheckBox checkBoxSelect = view.findViewById(R.id.checkBoxSelect);

        if (imageId != 0) {
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
            Glide.with(this)
                    .load(contentUri)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(photoView);

            checkBoxSelect.setChecked(isSelected);
        }

        checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSelected = isChecked;
            if (callback != null) {
                callback.onSelectionChanged(isChecked);
            }
        });

        return view;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean isSelected);
    }
}