package com.taimurlukas.metric2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class ShowView extends View {
	private int mColor;
	
	private GeometricObject mObj;
	
	private int mLevel;
	
	private int mMinArea;
	private int mMaxArea;
		
	private boolean mIsFirst;
			
	public ShowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mColor = context.getResources().getColor(R.color.show);
		
		mIsFirst = true;
	}
	
	public void setLevel(int level) {
		mLevel = level;
	}
	public void setAreaBounds(int minArea, int maxArea) {
		mMinArea = minArea;
		mMaxArea = maxArea;
	}
	
	public void update() {
		mIsFirst = true;
		invalidate();
	}
	
	public double getArea() {
		if (mObj == null)
			return 0;
		return mObj.getArea();
	}
	
	@Override
	public void onDraw(Canvas c) {
		if (mIsFirst) {
			mObj = new GeometricObject(mLevel, mColor, getWidth(), getHeight(), mMinArea, mMaxArea);
			
			mIsFirst = false;
		}
		if (mObj == null) {
			return;
		}
		
		c.drawBitmap(mObj.getBitmap(), 0, 0, null);
	}
	
	@Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        
        update();
    }
}
