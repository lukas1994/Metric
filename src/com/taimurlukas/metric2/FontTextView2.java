package com.taimurlukas.metric2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FontTextView2 extends TextView {
	public FontTextView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FontTextView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontTextView2(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTypeface(Assets.font2);
    }
}
