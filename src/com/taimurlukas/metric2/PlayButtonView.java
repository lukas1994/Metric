package com.taimurlukas.metric2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class PlayButtonView extends View {
	private final int FRAME_RATE = 12;
	
	private Paint mPaintRed, mPaintWhite;
	private Path mPath;
	
	private int mRadius;
	
	private double mAngle;
	
	private Handler mHandler;
	private Runnable mUpdateView;
	
	public PlayButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
				
		mPaintRed = new Paint();
		mPaintRed.setStyle(Paint.Style.FILL);
		mPaintRed.setColor(getResources().getColor(R.color.bg_red));
		mPaintRed.setAntiAlias(true);
		
		mPaintWhite = new Paint();
		mPaintWhite.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintWhite.setColor(Color.WHITE);
		mPaintWhite.setStrokeJoin(Paint.Join.ROUND);
		mPaintWhite.setStrokeCap(Paint.Cap.ROUND);
		mPaintWhite.setPathEffect(new CornerPathEffect(17));
		mPaintWhite.setAntiAlias(true);
		
		mPath = new Path();
		
		mAngle = 0.0;
		
		mHandler = new Handler();
		mUpdateView = new Runnable() {
		  @Override
		  public void run() {
			 mAngle += 2*Math.PI/50;
			 mAngle %= 2*Math.PI;
		     invalidate();
		  }
		};
	}
	
	@Override
	public void onDraw(Canvas c) {
		mRadius = Math.min(getWidth(), getHeight())/2 - 20;
		c.drawCircle((int) (getWidth()/2),
				(int) (getHeight()/2),
				(float) (mRadius + 10*Math.sin(mAngle)), mPaintRed);
		
		mPath.reset();
		mPath.moveTo((int) (getWidth()/2 + 0.66*mRadius*Math.cos(2*Math.PI/3)), 
				(int) (getHeight()/2 + 0.66*mRadius*Math.sin(2*Math.PI/3)));
		mPath.lineTo((int) (getWidth()/2 + 0.66*mRadius*Math.cos(0*Math.PI/3)), 
				(int) (getHeight()/2 + 0.66*mRadius*Math.sin(0*Math.PI/3)));
		mPath.lineTo((int) (getWidth()/2 + 0.66*mRadius*Math.cos(4*Math.PI/3)), 
				(int) (getHeight()/2 + 0.66*mRadius*Math.sin(4*Math.PI/3)));
		mPath.close();
		c.drawPath(mPath, mPaintWhite);
		
		mHandler.postDelayed(mUpdateView, FRAME_RATE);
	}
}
