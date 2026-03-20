package com.example.expenseutility.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseutility.R;
import com.example.expenseutility.dto.FileHolder;
import com.example.expenseutility.entityadapter.AttachedFilesAdapter;
import com.example.expenseutility.utility.AppConfig;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AttachedFilesBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_FILES_JSON = "files_json";
    private String filesJson;
    private RecyclerView rvFiles;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private AttachedFilesAdapter adapter;
    private List<FileHolder> fileList = new ArrayList<>();

    public static AttachedFilesBottomSheet newInstance(String filesJson) {
        AttachedFilesBottomSheet fragment = new AttachedFilesBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_FILES_JSON, filesJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filesJson = getArguments().getString(ARG_FILES_JSON);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_attached_files_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFiles = view.findViewById(R.id.rvFiles);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        View btnClose = view.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> dismiss());

        setupRecyclerView();
        loadFiles();
    }

    private void setupRecyclerView() {
        rvFiles.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttachedFilesAdapter(getContext(), fileList);
        rvFiles.setAdapter(adapter);
    }

    private void loadFiles() {
        progressBar.setVisibility(View.VISIBLE);
        rvFiles.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        new Thread(() -> {
            // Simulate slight delay for smooth UI transition
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<FileHolder> parsedFiles = new ArrayList<>();
            if (filesJson != null && !filesJson.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(filesJson);
                    JSONArray fileNames = jsonObject.getJSONArray("file_names");
                    JSONArray fileUris = jsonObject.getJSONArray("file_uris");

                    for (int i = 0; i < fileNames.length(); i++) {
                        String fileName = fileNames.getString(i);
                        String fileUri = fileUris.getString(i);
                        parsedFiles.add(new FileHolder(fileName, fileUri));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                progressBar.setVisibility(View.GONE);
                fileList.clear();
                fileList.addAll(parsedFiles);
                adapter.notifyDataSetChanged();

                if (fileList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvFiles.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    rvFiles.setVisibility(View.VISIBLE);
                    
                    if (new AppConfig(getContext()).isAnimationEnabled()) {
                        runLayoutAnimation(rvFiles);
                    }
                }
            });
        }).start();
    }

    private void runLayoutAnimation(RecyclerView recyclerView) {
        final android.content.Context context = recyclerView.getContext();
        final android.view.animation.LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_up);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }
}
