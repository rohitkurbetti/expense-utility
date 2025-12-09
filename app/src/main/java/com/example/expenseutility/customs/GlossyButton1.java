package com.example.expenseutility.customs;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.example.expenseutility.R;

public class GlossyButton1 extends AppCompatButton {

    private Paint outlinePaint;
    private float strokeWidth = 4f;
    private int outlineColor = Color.BLACK;

    public GlossyButton1(Context context) {
        super(context);
        init(null);
    }

    public GlossyButton1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GlossyButton1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // Outline paint for text
        outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(strokeWidth);
        outlinePaint.setAntiAlias(true);
        outlinePaint.setColor(outlineColor);
        outlinePaint.setTextAlign(Paint.Align.LEFT);

        // Apply glossy background from drawable
        setBackgroundResource(R.drawable.btn_green_glossy);

        // Read XML attributes if present
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    new int[]{android.R.attr.textSize, android.R.attr.textColor});

            float textSize = a.getDimensionPixelSize(0, -1);
            if (textSize > 0) {
                setTextSize(pxToSp(getContext(), textSize));
            }

            a.recycle();
        }

        // Default padding for button look
        setPadding(20, 12, 20, 12);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw outline first
        String text = getText().toString();
        Rect bounds = new Rect();
        getPaint().getTextBounds(text, 0, text.length(), bounds);

        // Copy current text paint into outlinePaint
        outlinePaint.setTextSize(getTextSize());
        outlinePaint.setTypeface(getTypeface());

        float x = (getWidth() - getPaint().measureText(text)) / 2f;
        float y = (getHeight() / 2f) - ((getPaint().descent() + getPaint().ascent()) / 2f);

        canvas.drawText(text, x, y, outlinePaint);

        // Draw main text (normal)
        super.onDraw(canvas);
    }

    private float pxToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

    // Allow changing outline color dynamically
    public void setOutlineColor(int color) {
        outlineColor = color;
        outlinePaint.setColor(color);
        invalidate();
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        outlinePaint.setStrokeWidth(width);
        invalidate();
    }
}
