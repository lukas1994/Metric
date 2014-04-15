package com.taimurlukas.metric2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FontTextView extends TextView {
	public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTypeface(Assets.font);
    }
}
