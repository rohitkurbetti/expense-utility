package com.example.expenseutility.entityadapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.expenseutility.ImagePreviewFragment;

public class ImagePreviewPagerAdapter extends FragmentStateAdapter {

    private long[] imageIds;
    private boolean[] selectedStates;
    private OnSelectionChangedListener listener;

    public ImagePreviewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                    long[] imageIds,
                                    boolean[] selectedStates,
                                    OnSelectionChangedListener listener) {
        super(fragmentActivity);
        this.imageIds = imageIds;
        this.selectedStates = selectedStates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ImagePreviewFragment.newInstance(
                imageIds[position],
                selectedStates[position],
                (isSelected) -> {
                    selectedStates[position] = isSelected;
                    if (listener != null) {
                        listener.onSelectionChanged(position, isSelected);
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return imageIds.length;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int position, boolean isSelected);
    }
}