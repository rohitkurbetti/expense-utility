package com.example.expenseutility.customs;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatButton;

import com.example.expenseutility.R;

public class GlossyButtonTest1 extends AppCompatButton {

    public GlossyButtonTest1(Context context) {
        super(context);
        init();
    }

    public GlossyButtonTest1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlossyButtonTest1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.glossy_button_test_1);
        setTextColor(0xFFFFFFFF); // White text
        setAllCaps(true);
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(13);
        setPadding(32, 16, 32, 16);
        setShadowLayer(1.5f, 1, 1, 0xAA000000);
    }
}

