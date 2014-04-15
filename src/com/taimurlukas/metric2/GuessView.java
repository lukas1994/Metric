package com.taimurlukas.metric2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GuessView extends View {
	private int mColor;
	private int mColor2;
	private Paint mPaint;
	private Paint mPaint2;
		
	private int mStartDistToCenter = getResources().getInteger(R.integer.start_guess_dist_to_center);
	private int mDistToCenter = mStartDistToCenter;
	private int mMinDistToCenter = getResources().getInteger(R.integer.start_guess_dist_to_center);
	private int mMaxDistToCenter;
	private int mCenterX;
	private int mCenterY;
	
	private boolean mIsMoving = false;
	private int mMovePointX;
	private int mMovePointY;
	
	private int mLevel;
	
	// animation
	private final int FRAME_RATE = getResources().getInteger(R.integer.frame_rate);
	private final int ANIMATION_TIME = getResources().getInteger(R.integer.animation_time);
	private Handler mHandler = null;
	private Runnable mUpdateView;
	private boolean mIsGuessing;
	private double mState;
	private final double EPSILON = 1e-6;
	private double mRealDistToCenter;
	
	// shapes
	private Path mShapes[];
	private double mMaxAngles[] = {
			Math.PI/4,
			Math.PI/2,
			Math.PI/3,
			Math.PI/2,
			Math.PI/2
	};
	private float mMovePoints[][] = {
			{(float) Math.sqrt(2)/2, (float) Math.sqrt(2)/2},
			{(float) Math.cos(Math.PI/6), (float) Math.sin(Math.PI/6)},
			{(float) Math.cos(Math.PI/3), (float) Math.sin(Math.PI/3)},
			{(float) Math.cos(3*Math.PI/10), (float) Math.sin(3*Math.PI/10)},
			{(float) Math.cos(3*Math.PI/10), (float) Math.sin(3*Math.PI/10)}
	};
	private float mTmpMovePoint[];
	
	private Path mTmpPath;
	private Matrix mScaleMatrix;
	
	interface Listener {
		public void onAreaBoundsChanged();
		public void onAnimationFinished();
	}
	
	private Listener mListener;
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	
	private boolean mIsEditable = true;
	public void setIsEditable(boolean isEditable) {
		mIsEditable = isEditable;
	}
	
	public void setLevel(int level) {
		mLevel = level;
	}
	
	public GuessView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		
		mColor = context.getResources().getColor(R.color.guess);
		mColor2 = context.getResources().getColor(R.color.guess2);
		
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
		
		mPaint2 = new Paint();
		mPaint2.setStyle(Paint.Style.FILL);
		mPaint2.setColor(mColor2);
		mPaint2.setAntiAlias(true);
		
		// animation
		mIsGuessing = true;
		mState = 0.0;
		mHandler = new Handler();
		mUpdateView = new Runnable() {
		  @Override
		  public void run() {
			  mState += FRAME_RATE*1.0 / ANIMATION_TIME;
			  mState = Math.min(mState, 1.0);
		      invalidate();
		  }
		};
		
		mTmpPath = new Path();
		mScaleMatrix = new Matrix();
		mTmpMovePoint = new float[2];
		
		// create shapes with distToCenter 1
		mShapes = new Path[5];
		
		mShapes[0] = new Path(); // square
		mShapes[0].moveTo((float) -Math.sqrt(2)/2, (float) Math.sqrt(2)/2);
		mShapes[0].lineTo((float) Math.sqrt(2)/2, (float) Math.sqrt(2)/2);
		mShapes[0].lineTo((float) Math.sqrt(2)/2, (float) -Math.sqrt(2)/2);
		mShapes[0].lineTo((float) -Math.sqrt(2)/2, (float) -Math.sqrt(2)/2);
		mShapes[0].close();
		
		mShapes[1] = new Path(); // triangle
		mShapes[1].moveTo(0, -1);
		mShapes[1].lineTo((float) Math.cos(1*Math.PI/6), (float) Math.sin(1*Math.PI/6));
		mShapes[1].lineTo((float) Math.cos(5*Math.PI/6), (float) Math.sin(5*Math.PI/6));
		mShapes[1].close();
		
		mShapes[2] = new Path(); // hexagon
		mShapes[2].moveTo(1, 0);
		for (int k = 1; k <= 5; k++)
			mShapes[2].lineTo((float) Math.cos(2*k*Math.PI/6), (float) Math.sin(2*k*Math.PI/6));
		mShapes[2].close();
		
		mShapes[3] = new Path(); // star
		mShapes[3].moveTo(0, -1);
		for (int k = 0; k <= 4; k++) {
			mShapes[3].lineTo((float) (0.5*Math.cos((-3+4*k)*Math.PI/10)), (float) (0.5*Math.sin((-3+4*k)*Math.PI/10)));
			mShapes[3].lineTo((float) (Math.cos((-1+4*k)*Math.PI/10)), (float) (Math.sin((-1+4*k)*Math.PI/10)));
		}
		mShapes[3].close();
		
		mShapes[4] = new Path(); // curved
		mShapes[4].moveTo((float) (0.5*Math.cos(-3*Math.PI/10)), (float) (0.5*Math.sin(-3*Math.PI/10)));
		for (int k = 0; k <= 4; k++) {
			mShapes[4].quadTo((float) (1.6*Math.cos((-1+4*k)*Math.PI/10)), (float) (1.6*Math.sin((-1+4*k)*Math.PI/10)), 
					(float) (0.5*Math.cos((1+4*k)*Math.PI/10)), (float) (0.5*Math.sin((1+4*k)*Math.PI/10)));
		}
		mShapes[4].close();
	}
	
	@Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        
        if (mListener != null)
        	mListener.onAreaBoundsChanged();
    }
	
	public void reset() {
		mIsGuessing = true;
		mDistToCenter = mStartDistToCenter;
		invalidate();
	}
	
	private double dist(int x1, int y1, int x2, int y2) {
		int dx = x1-x2;
		int dy = y1-y2;
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsGuessing || !mIsEditable)
			return true;
		
		mCenterX = getWidth()/2;
		mCenterY = getHeight()/2;
		
		calcMaxDistToCenter();
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (dist((int) event.getX(), (int) event.getY(),
					mMovePointX, mMovePointY) < 50) {
				mIsMoving = true;
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (mIsMoving) {
				int x = (int)(event.getX()+event.getY()+mCenterX-mCenterY)/2;
				int y = (int)(event.getX()+event.getY()-mCenterX+mCenterY)/2;
				mDistToCenter = (int) dist(mCenterX, mCenterY, x, y);
				mDistToCenter = Math.max(mDistToCenter, mMinDistToCenter);
				mDistToCenter = Math.min(mDistToCenter, mMaxDistToCenter);
				
				Log.e("test", ""+mDistToCenter);
				invalidate();
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP) {
			mIsMoving = false;
		}
		return true;
	}
	
	@Override
	public void onDraw(Canvas c) {
		mCenterX = getWidth()/2;
		mCenterY = getHeight()/2;
		
		mTmpPath.rewind();
		mTmpPath.addPath(mShapes[mLevel]);
		
		mScaleMatrix.setTranslate(mCenterX, mCenterY);
		mScaleMatrix.preScale((float) mDistToCenter, (float) mDistToCenter);
		
		mTmpPath.transform(mScaleMatrix);
		
		c.drawPath(mTmpPath, mPaint);
		
		if (mIsGuessing) {
			mTmpMovePoint = mMovePoints[mLevel].clone();
			mScaleMatrix.mapPoints(mTmpMovePoint);
			mMovePointX = (int) mTmpMovePoint[0];
			mMovePointY = (int) mTmpMovePoint[1];
			c.drawCircle(mTmpMovePoint[0], mTmpMovePoint[1] , getResources().getInteger(R.integer.guess_circle_radius), mPaint2);
		}
		else { // animating
			int color = mPaint.getColor();
			mPaint.setColor(getResources().getColor(R.color.real_area));
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(5);
			float length = (float) (mDistToCenter + mState * (mRealDistToCenter - mDistToCenter));
			
			mTmpPath.rewind();
			mTmpPath.addPath(mShapes[mLevel]);
			
			mScaleMatrix.setTranslate(mCenterX, mCenterY);
			mScaleMatrix.preScale((float) length, (float) length);
			
			mTmpPath.transform(mScaleMatrix);
			
			c.drawPath(mTmpPath, mPaint);
			
			mPaint.setColor(color);
			mPaint.setStyle(Paint.Style.FILL);
			
			if (Math.abs(mState - 1.0) > EPSILON)
				mHandler.postDelayed(mUpdateView, FRAME_RATE);
			else
				mListener.onAnimationFinished();
		}
	}
	
	public double getArea() {
		return distToCenterToArea(mDistToCenter);
	}
	public int getMinArea() {
		return (int) distToCenterToArea(mMinDistToCenter);
	}
	private void calcMaxDistToCenter() {
		double length = 0;
		if (getHeight() != 0 && getWidth() != 0) {
			length = Math.min(getWidth(), getHeight()) - 2*10;
		}
		else {
			measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			length = Math.min(getMeasuredHeight(), getMeasuredWidth()) - 2*10;
		}
		
		mMaxDistToCenter = (int) (length / Math.sin(mMaxAngles[mLevel]) / 2);
	}
	public int getMaxArea() {
		calcMaxDistToCenter();
		return (int) distToCenterToArea(mMaxDistToCenter);
	}
	private double areaToDistToCenter(double area) {
		switch (mLevel) {
		case 0: return Math.sqrt(area/2);
		case 1: return Math.sqrt(area/(3*Math.sqrt(3)/4));
		case 2: return Math.sqrt(area/(3*Math.sqrt(3)/2));
		case 3: return Math.sqrt(area/(5*Math.sin(Math.PI/5)*Math.sin(Math.PI/5)*Math.tan(3*Math.PI/10)));
		case 4: return Math.sqrt(area/(1.5*5*Math.sin(Math.PI/5)*Math.sin(Math.PI/5)*Math.tan(3*Math.PI/10)));
		}
		return 0;
	}
	private double distToCenterToArea(double distToCenter) {
		switch (mLevel) {
		case 0: return 2*distToCenter*distToCenter;
		case 1: return 3*Math.sqrt(3)/4*distToCenter*distToCenter;
		case 2: return 3*Math.sqrt(3)/2*distToCenter*distToCenter;
		case 3: return 5*Math.sin(Math.PI/5)*Math.sin(Math.PI/5)*Math.tan(3*Math.PI/10)*distToCenter*distToCenter;
		case 4: return 1.5*5*Math.sin(Math.PI/5)*Math.sin(Math.PI/5)*Math.tan(3*Math.PI/10)*distToCenter*distToCenter;
		}
		return 0;
	}
	
	public void animate(double area) {
		if (!mIsGuessing)
			return;
		mRealDistToCenter = areaToDistToCenter(area);
		
		Log.e("real", ""+mRealDistToCenter);
		Log.e("we", ""+mDistToCenter);
		Log.e("max", ""+areaToDistToCenter(getMaxArea()));
		mState = 0.0;
		mIsGuessing = false;
		invalidate();
	}
}
