package com.example.expenseutility.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.expenseutility.R;

public class GlossyButton extends FrameLayout {
    private TextView textMain, textOutline;

    public GlossyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GlossyButton(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_glossy_button, this, true);

        textMain = findViewById(R.id.textMain);
        textOutline = findViewById(R.id.textOutline);

        // Allow text to be set via XML
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.text, android.R.attr.textSize});
            String text = a.getString(0);
            if (text != null) {
                setText(text);
            }
            // Handle text size
            float textSize = a.getDimension(1, -1);
            if (textSize > 0) {
                textMain.setTextSize(pxToSp(context, textSize));
                textOutline.setTextSize(pxToSp(context, textSize));
            }
            a.recycle();
        }
    }

    private float pxToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

    public void setText(String text) {
        textMain.setText(text);
        textOutline.setText(text);
    }

    public String getText() {
        return textMain.getText().toString();
    }
}

