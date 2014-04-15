package com.taimurlukas.metric2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

public class GuessButtonBackground extends LinearLayout {
	Paint mPaint1, mPaint2;
	Context mContext;
	Bitmap mBitmap;
	boolean mBitmapFinished;
	
	public GuessButtonBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
		
		setWillNotDraw(false);
		
		mPaint1 = new Paint();
		mPaint1.setStyle(Paint.Style.FILL);
		mPaint1.setAntiAlias(true);
		
		mPaint2 = new Paint();
		mPaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		//mPaint2.setAlpha(0xFF);
		mPaint2.setAntiAlias(true);
	    
		
		mBitmapFinished = false;		
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (!mBitmapFinished) {
			mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
			Canvas c = new Canvas(mBitmap);
			
			float cornerRadius = mContext.getResources().getDimensionPixelSize(R.dimen.game_bg_corner_radius);
			
			mPaint1.setColor(Color.WHITE);
			c.drawRect(0, 0, getWidth(), getHeight()-cornerRadius, mPaint1);
			c.drawRect(cornerRadius, getHeight()-cornerRadius, getWidth()-cornerRadius, getHeight(), mPaint1);
			c.drawCircle(cornerRadius, getHeight()-cornerRadius, cornerRadius, mPaint1);
			c.drawCircle(getWidth()-cornerRadius, getHeight()-cornerRadius, cornerRadius, mPaint1);
						
			FontTextView tv = (FontTextView) findViewById(R.id.guessButton);
			if (tv != null) {
				int delta = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mContext.getResources().getDisplayMetrics());
				c.drawCircle(getWidth()/2, getHeight()+delta, getHeight()+delta, mPaint2);
			}
			
			
			
			mBitmapFinished = true;
		}
		
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}

}
