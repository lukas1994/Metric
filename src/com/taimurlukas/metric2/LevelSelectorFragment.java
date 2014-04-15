package com.taimurlukas.metric2;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.taimurlukas.metric2.MainActivity.State;

public class LevelSelectorFragment extends Fragment
	implements OnClickListener {
	
	final State STATE = State.LEVEL;
		
	public interface Listener {
        public void onLevelSelected(int level);
    }

    Listener mListener = null;
    
    private int mCurrentLevel;
    private int mScores[];
    
    private LinearLayout mLevelContainer;
    
    private View mLevelButtons[];
    private ImageView mIconViews[];
    private TextView mScoreViews[];
	private TextView mDescriptionViews[];
	private ImageView mLockViews[];
    

	private final int[] mLevelIcons = {
			R.drawable.level1,
			R.drawable.level2,
			R.drawable.level3,
			R.drawable.level4,
			R.drawable.level5,
			R.drawable.level6,
			R.drawable.level7,
			R.drawable.level8,
			R.drawable.level9,
			R.drawable.level10,
			R.drawable.level11,
			R.drawable.level12,
			R.drawable.level13,
			R.drawable.level14,
			R.drawable.level15,
			R.drawable.level16,
			R.drawable.level17,
			R.drawable.level18,
			R.drawable.level19,
			R.drawable.level20,
			R.drawable.level21,
			R.drawable.level22,
			R.drawable.level23,
			R.drawable.level24,
			R.drawable.level25
    };    
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.level_selector_layout, container, false);
		
		mLevelContainer = (LinearLayout) v.findViewById(R.id.level_container);
		
		mLevelButtons = new View[25];
		mIconViews = new ImageView[25];
		mScoreViews = new TextView[25];
		mDescriptionViews = new TextView[25];
		mLockViews = new ImageView[25];
		
		for (int guess = 0; guess < 5; guess++) {
			for (int shape = 0; shape < 5; shape++) {
				mLevelButtons[guess*5+shape] = inflater.inflate(R.layout.level_button_layout, mLevelContainer, false);
				
				((FontTextView) mLevelButtons[guess*5+shape].findViewById(R.id.level_number_text)).setText("Level " + (guess*5+shape+1));
				mIconViews[guess*5+shape] = (ImageView) mLevelButtons[guess*5+shape].findViewById(R.id.level_icon);
				mScoreViews[guess*5+shape] = (TextView) mLevelButtons[guess*5+shape].findViewById(R.id.level_score_value);
				mDescriptionViews[guess*5+shape] = (TextView) mLevelButtons[guess*5+shape].findViewById(R.id.level_score_description);
				mLockViews[guess*5+shape] = (ImageView) mLevelButtons[guess*5+shape].findViewById(R.id.level_lock);
				
				mLevelButtons[guess*5+shape].setTag(Integer.valueOf(guess*5+shape));
				mLevelButtons[guess*5+shape].setOnClickListener(this);
				
				mIconViews[guess*5+shape].setImageResource(mLevelIcons[guess*5+shape]);
								
				mLevelContainer.addView(mLevelButtons[guess*5+shape]);
			}
		}
		
		updateUi();
		
		return v;
	}
	
	@Override
	public void onStart() {
		updateUi();
		super.onStart();
	}
	
	public void setListener(Listener l) {
        mListener = l;
    }

	public void setState(int currentLevel, int scores[]) {
		mCurrentLevel = currentLevel;
		mScores = scores.clone();
		updateUi();
	}
	
	public void updateUi() {
		if (getActivity() == null) return;
		
		assert(1 <= mCurrentLevel);
				
		int decimalPlaces = getResources().getInteger(R.integer.score_decimal_places);
				
		if (mLevelContainer != null) {
			for (int guess = 0; guess < 5; guess++) {
				for (int shape = 0; shape < 5; shape++) {
					if (guess*5+shape <= mCurrentLevel) {
						mLevelButtons[guess*5+shape].setEnabled(true);
						mIconViews[guess*5+shape].clearColorFilter();
						if (mScores[guess*5+shape] != -1)
							mScoreViews[guess*5+shape].setText(Utils.googleScoreToScore(mScores[guess*5+shape], decimalPlaces));
						else
							mScoreViews[guess*5+shape].setText(getString(R.string.no_heighscore_yet));
						mDescriptionViews[guess*5+shape].setVisibility(View.VISIBLE);
						mLockViews[guess*5+shape].setVisibility(View.GONE);
					}
					else {
						mLevelButtons[guess*5+shape].setEnabled(false);
						mIconViews[guess*5+shape].setColorFilter(0x66000000,PorterDuff.Mode.SRC_ATOP);
						mScoreViews[guess*5+shape].setText(getString(R.string.no_heighscore_yet));
						mDescriptionViews[guess*5+shape].setVisibility(View.GONE);
						mLockViews[guess*5+shape].setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		mListener.onLevelSelected((Integer) view.getTag());
	}
}

