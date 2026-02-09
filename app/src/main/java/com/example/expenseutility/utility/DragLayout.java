package com.example.expenseutility.utility;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DragLayout extends FrameLayout {

    private View draggedView;
    private float lastTouchX, lastTouchY;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private OnDragListener dragListener;

    public DragLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Enable clipping to padding
        setClipToPadding(false);
        setClipChildren(false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't intercept touch events, let children handle them
        return false;
    }

    public void setOnDragListener(OnDragListener listener) {
        this.dragListener = listener;
    }

    public void makeViewDraggable(View view) {
        view.setOnTouchListener(new DragTouchListener());
    }

    private void applyBoundaries(View view, FrameLayout.LayoutParams params) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        int parentWidth = getWidth();
        int parentHeight = getHeight();

        // Keep within parent bounds
        if (params.leftMargin < 0) {
            params.leftMargin = 0;
        }
        if (params.topMargin < 0) {
            params.topMargin = 0;
        }
        if (params.leftMargin + viewWidth > parentWidth) {
            params.leftMargin = parentWidth - viewWidth;
        }
        if (params.topMargin + viewHeight > parentHeight) {
            params.topMargin = parentHeight - viewHeight;
        }

        // Ensure margins are not negative
        if (params.leftMargin < 0) params.leftMargin = 0;
        if (params.topMargin < 0) params.topMargin = 0;
    }

    public void addDraggableChild(View child, int x, int y) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = x;
        params.topMargin = y;

        child.setLayoutParams(params);
        makeViewDraggable(child);
        addView(child);
    }

    public interface OnDragListener {
        void onViewDragged(View view, int x, int y);
    }

    private class DragTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    draggedView = view;
                    view.bringToFront();

                    // Get the initial touch coordinates
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();

                    // Save the initial pointer ID
                    activePointerId = event.getPointerId(0);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (draggedView == view) {
                        // Find the index of the active pointer
                        int pointerIndex = event.findPointerIndex(activePointerId);
                        if (pointerIndex < 0) {
                            return false;
                        }

                        float x = event.getRawX();
                        float y = event.getRawY();

                        float deltaX = x - lastTouchX;
                        float deltaY = y - lastTouchY;

                        // Update the view position
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                        params.leftMargin += deltaX;
                        params.topMargin += deltaY;

                        // Apply boundaries
                        applyBoundaries(view, params);

                        view.setLayoutParams(params);

                        // Update last touch position
                        lastTouchX = x;
                        lastTouchY = y;

                        // Notify listener
                        if (dragListener != null) {
                            dragListener.onViewDragged(view, params.leftMargin, params.topMargin);
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (draggedView == view) {
                        draggedView = null;
                        activePointerId = MotionEvent.INVALID_POINTER_ID;
                    }
                    return true;

                case MotionEvent.ACTION_POINTER_UP:
                    // Handle additional pointer up events if needed
                    break;
            }
            return true;
        }
    }
}