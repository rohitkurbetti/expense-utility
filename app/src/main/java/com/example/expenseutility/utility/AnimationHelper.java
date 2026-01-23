package com.example.expenseutility.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.HorizontalScrollView;

import java.util.HashMap;
import java.util.Map;

public class AnimationHelper {

    private static Map<View, Integer> originalWidths = new HashMap<>();

    public static void expandColumn(final View column, final int targetWidth,
                                    final HorizontalScrollView scrollView,
                                    final View[] allColumns,
                                    final AnimationListener listener) {
        if (column.getVisibility() == View.VISIBLE) return;

        // Store original widths
        for (View col : allColumns) {
            if (!originalWidths.containsKey(col)) {
                originalWidths.put(col, col.getLayoutParams().width);
            }
        }

        // Set column visible with 0 width
        column.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = column.getLayoutParams();
        final int startWidth = params.width;
        params.width = 0;
        column.setLayoutParams(params);
        column.requestLayout();

        // Animate width expansion
        ValueAnimator animator = ValueAnimator.ofInt(0, targetWidth);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = column.getLayoutParams();
                params.width = width;
                column.setLayoutParams(params);
                column.requestLayout();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Shrink previous columns
                shrinkPreviousColumns(allColumns, column);

                // Scroll to show new column
                scrollToColumn(scrollView, column);

                if (listener != null) listener.onAnimationEnd();
            }
        });

        animator.start();
    }

    public static void collapseColumn(final View column, final HorizontalScrollView scrollView,
                                      final View[] allColumns,
                                      final AnimationListener listener) {
        if (column.getVisibility() != View.VISIBLE) return;

        final ViewGroup.LayoutParams params = column.getLayoutParams();
        final int startWidth = params.width;

        // Animate width collapse
        ValueAnimator animator = ValueAnimator.ofInt(startWidth, 0);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (int) animation.getAnimatedValue();
                params.width = width;
                column.setLayoutParams(params);
                column.requestLayout();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                column.setVisibility(View.GONE);

                // Expand remaining columns
                expandRemainingColumns(allColumns);

                // Scroll to appropriate column
                View lastVisible = getLastVisibleColumn(allColumns);
                if (lastVisible != null) {
                    scrollToColumn(scrollView, lastVisible);
                }

                if (listener != null) listener.onAnimationEnd();
            }
        });

        animator.start();
    }

    private static void shrinkPreviousColumns(View[] allColumns, View expandingColumn) {
        boolean foundExpanding = false;

        for (View column : allColumns) {
            if (column == expandingColumn) {
                foundExpanding = true;
                continue;
            }

            if (foundExpanding && column.getVisibility() == View.VISIBLE) {
                // This column is to the right of expanding column, keep it collapsed
                continue;
            }

            if (column.getVisibility() == View.VISIBLE && column != expandingColumn) {
                // Shrink column by 30%
                ViewGroup.LayoutParams params = column.getLayoutParams();
                int currentWidth = params.width;
                int newWidth = (int) (currentWidth * 0.7);
                if (newWidth < 150) newWidth = 150; // Minimum width

                animateColumnWidth(column, newWidth, 300);
            }
        }
    }

    private static void expandRemainingColumns(View[] allColumns) {
        int visibleCount = 0;
        for (View column : allColumns) {
            if (column.getVisibility() == View.VISIBLE) {
                visibleCount++;
            }
        }

        if (visibleCount == 0) return;

        // Calculate available space distribution
        for (View column : allColumns) {
            if (column.getVisibility() == View.VISIBLE) {
                Integer originalWidth = originalWidths.get(column);
                if (originalWidth != null) {
                    // Restore some width based on how many columns are visible
                    float factor = 1.0f / visibleCount;
                    int newWidth = (int) (originalWidth * factor * 1.5);
                    if (newWidth > originalWidth) newWidth = originalWidth;
                    if (newWidth < 200) newWidth = 200;

                    animateColumnWidth(column, newWidth, 300);
                }
            }
        }
    }

    private static void animateColumnWidth(final View column, final int targetWidth, int duration) {
        final ViewGroup.LayoutParams params = column.getLayoutParams();
        final int startWidth = params.width;

        ValueAnimator animator = ValueAnimator.ofInt(startWidth, targetWidth);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (int) animation.getAnimatedValue();
                params.width = width;
                column.setLayoutParams(params);
                column.requestLayout();
            }
        });
        animator.start();
    }

    private static void scrollToColumn(final HorizontalScrollView scrollView, final View column) {
        if (scrollView == null || column == null) return;

        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int scrollTo = column.getLeft();
                scrollView.smoothScrollTo(scrollTo, 0);
            }
        }, 200);
    }

    private static View getLastVisibleColumn(View[] columns) {
        for (int i = columns.length - 1; i >= 0; i--) {
            if (columns[i].getVisibility() == View.VISIBLE) {
                return columns[i];
            }
        }
        return null;
    }

    public interface AnimationListener {
        void onAnimationStart();

        void onAnimationEnd();
    }
}