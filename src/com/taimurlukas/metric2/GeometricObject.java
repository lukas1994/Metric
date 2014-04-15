package com.taimurlukas.metric2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;

public class GeometricObject {
	private final int MAX_TRIALS = 50;
	
	private Paint mPaint;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	
	private int mWidth;
	private int mHeight;
	
	private int mMinArea;
	private int mMaxArea;
	
	private double mArea;
	
	private Random mRandom = new Random();
	
	GeometricObject(int level, int color, int width, int height, int minArea, int maxArea) {
		mWidth = width;
		mHeight = height;
		
		mMinArea = minArea;
		mMaxArea = maxArea;
		
		mBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(color);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(3);
		
		draw(level);
	}
	
	private void draw(int level) {
		switch (level) {
		case 0: drawRectangle(); break;
		case 1: drawTriangle(); break;
		case 2: drawConvexPolygon(); break;
		case 3: drawAnyPolygon(); break;
		case 4: drawShape(); break;
		}
	}
	
	private void drawRectangle() {
		int width = mRandom.nextInt(mWidth-200) + 100;
		mArea = mRandom.nextInt(mMaxArea-mMinArea) + mMinArea;
		int height = (int) Math.min((float) (mArea / width), mHeight-100);
		mArea = width*height;
		mCanvas.drawRect(mWidth/2 - width/2, mHeight/2 - height/2,
				mWidth/2 + width/2, mHeight/2 + height/2, 
				mPaint);
	}
	private void drawTriangle() {
		Point[] points = new Point[3];
		for (int i = 0; i < 3; i++)
			points[i] = new Point();
		
		
		double centerX = mWidth/2, centerY = mHeight/2;
		
		int count;
		for (count = 0; count < MAX_TRIALS; count++) {
			double minR = Math.sqrt(mMinArea/Math.PI);
			double maxR = Math.min(Math.min(mWidth, mHeight)/2, Math.sqrt(mMaxArea/Math.PI));
			double r = mRandom.nextDouble()*(maxR-minR)+minR;
			for (int i = 0; i < 3; i++) {
				double angle = mRandom.nextDouble()*2*Math.PI;
				
				points[i].x = (int) (centerX + r*Math.cos(angle));
				points[i].y = (int) (centerY + r*Math.sin(angle));
				
			}
			
			mArea = Math.abs(points[0].x*(points[1].y-points[2].y) +
					points[1].x*(points[2].y-points[0].y) +
					points[2].x*(points[0].y-points[1].y))/2;
			
			if (mMinArea <= mArea && mArea <= mMaxArea)
				break;
		}
		assert(count != MAX_TRIALS);
		
		drawPolygon(points, false);
	}
	private void drawConvexPolygon() {
		int count;
		for (count = 0; count < MAX_TRIALS; count++) {
			int n = 2*(3+mRandom.nextInt(5));
			Point[] points = generatePoints(n, true);
			
			drawPolygon(points, false);
			
			if (mMinArea <= mArea && mArea <= mMaxArea)
				break;
		}
		assert(count != MAX_TRIALS);
	}
	private void drawAnyPolygon() {
		int count;
		for (count = 0; count < MAX_TRIALS; count++) {
			int n = 2*(3+mRandom.nextInt(5));
			Point[] points = generatePoints(n, false);
			
			drawPolygon(points, false);
			
			if (mMinArea <= mArea && mArea <= mMaxArea)
				break;
		}
		assert(count != MAX_TRIALS);
	}
	private void drawShape() {
		int count;
		for (count = 0; count < MAX_TRIALS; count++) {
			int n = 2*(3+mRandom.nextInt(5));
			Point[] points = generatePoints(n, false);
			
			drawPolygon(points, true);
			
			if (mMinArea <= mArea && mArea <= mMaxArea)
				break;
		}
		assert(count != MAX_TRIALS);
	}
	
	private Point[] generatePoints(int n, boolean isConvex) {
		Point[] points = new Point[n];
		
		double centerX = mWidth/2, centerY = mHeight/2;
		ArrayList<Double> angles = new ArrayList<Double>();
		for (int i = 0; i < n; i++)
			angles.add(mRandom.nextDouble()*2*Math.PI);
		Collections.sort(angles);
		
		double minR = Math.sqrt(mMinArea/Math.PI);
		double maxR = Math.min(Math.min(mWidth, mHeight)/2, Math.sqrt(mMaxArea/Math.PI));
		double r = mRandom.nextDouble()*(maxR-minR)+minR;
		for (int i = 0; i < n; i++) {
			if (!isConvex)
				r = mRandom.nextDouble()*(maxR-minR)+minR;
			points[i] = new Point();
			points[i].x = (int) (centerX + r*Math.cos(angles.get(i)));
			points[i].y = (int) (centerY + r*Math.sin(angles.get(i)));
		}
		return points;
	}
	
	private void drawPolygon(Point[] points, boolean useBezier) {
	    if (points.length < 2) {
	        return;
	    }
	    
	    // reset
	    mCanvas.drawColor(Color.TRANSPARENT);

	    // path
	    Path polyPath = new Path();
	    polyPath.moveTo(points[0].x, points[0].y);
	    if (useBezier) {
		    for (int i = 1; i < points.length-1; i+=2) {
		    	polyPath.quadTo(points[i].x, points[i].y, points[i+1].x, points[i+1].y);
		    }
		    polyPath.quadTo(points[points.length-1].x, points[points.length-1].y,
		    		points[0].x, points[0].y);
	    }
	    else {
	    	for (int i = 1; i < points.length; i++) {
		        polyPath.lineTo(points[i].x, points[i].y);
		    }
	    }
	    polyPath.close();

	    // draw
	    mCanvas.drawPath(polyPath, mPaint);
	    
	    
	    // calc area
	    Region region = new Region();
	    region.setPath(polyPath, new Region(0,0,mWidth,mHeight));
	    RegionIterator regionIterator = new RegionIterator(region);

	    mArea = 0.0;

        Rect tmpRect = new Rect(); 

        while (regionIterator.next(tmpRect)) {
        	mArea += tmpRect.width() * tmpRect.height();
        }
   	}
	
	public Bitmap getBitmap() {
		return mBitmap;
	}
	public double getArea() {
		return mArea;
	}
}
