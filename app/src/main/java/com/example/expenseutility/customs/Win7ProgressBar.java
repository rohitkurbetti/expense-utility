package com.example.expenseutility.customs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.expenseutility.R;

public class Win7ProgressBar extends View {

    private Paint basePaint, fillPaint, lightPaint, glossPaint;
    private float progress = 0; // current % progress
    private float animatedProgress = 0; // smooth animated value
    private float lightOffset = 0;
    private boolean indeterminate = true;

    // Theme colors
    private int colorPrimary;
    private int colorPrimaryDark;
    private int colorAccent;

    public Win7ProgressBar(Context context) {
        super(context);
        init();
    }

    public Win7ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Win7ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // Resolve theme colors dynamically
        colorPrimary = resolveThemeColor(getContext(), com.google.android.material.R.attr.colorPrimary);
        colorPrimaryDark = resolveThemeColor(getContext(), com.google.android.material.R.attr.colorPrimaryDark);
        colorAccent = resolveThemeColor(getContext(), R.color.brownLight);


        // Background (Win7 silver track)
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setShader(new LinearGradient(0, 0, 0, 100,
                0xFFDDDDDD, 0xFFFFFFFF, Shader.TileMode.CLAMP));

        // Progress fill (glossy green base)
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Moving highlight stripe
        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Gloss effect (white shine on top half)
        glossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glossPaint.setShader(new LinearGradient(0, 0, 0, 60,
                new int[]{0xBBFFFFFF, 0x00FFFFFF},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP));
    }

    private int resolveThemeColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Rounded rect background
        RectF bgRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(bgRect, 6, 6, basePaint);

        // Gradient glossy fill using theme colors
        fillPaint.setShader(new LinearGradient(0, 0, 0, height,
                new int[]{colorPrimaryDark, colorPrimary},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP));

        float progressWidth = (animatedProgress / 100f) * width;
        RectF progRect = new RectF(0, 0, progressWidth, height);
        canvas.drawRoundRect(progRect, 6, 6, fillPaint);

        // Gloss effect (only over progress area)
        RectF glossRect = new RectF(0, 0, progressWidth, height / 3f);
        canvas.drawRoundRect(glossRect, 6, 6, glossPaint);

        // Moving glossy stripe (Windows 7 animation)
        int stripeWidth = width / 3;
        LinearGradient stripeGradient = new LinearGradient(
                lightOffset, 0, lightOffset + stripeWidth, 0,
                new int[]{0x00FFFFFF, 0x80F5F5F5, 0x00FFFFFF},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );
        lightPaint.setShader(stripeGradient);

        if (indeterminate) {
            canvas.drawRoundRect(bgRect, 6, 6, lightPaint);
        } else {
            canvas.drawRoundRect(progRect, 6, 6, lightPaint);
        }

        // Animate offset for stripe
        lightOffset += 6;
        if (lightOffset > width) {
            lightOffset = -stripeWidth;
        }

        postInvalidateOnAnimation();
    }

    // Smooth progress setter
    public void setProgress(float value) {
        this.progress = Math.max(0, Math.min(100, value));
        indeterminate = false;

        ValueAnimator animator = ValueAnimator.ofFloat(animatedProgress, progress);
        animator.setDuration(400); // smooth duration
        animator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setIndeterminate(boolean value) {
        indeterminate = value;
        invalidate();
    }
}
