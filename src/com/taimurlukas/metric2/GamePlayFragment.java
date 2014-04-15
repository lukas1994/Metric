package com.taimurlukas.metric2;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.taimurlukas.metric2.MainActivity.State;

public class GamePlayFragment extends Fragment
	implements OnClickListener, GuessView.Listener {
	
	final State STATE = State.GAME;
	
	private int GAMES_PER_LEVEL;
	
	ShowView mShowView;
	GuessView mGuessView;
	FontTextView mGuessButton;
	
	private int mLevel;
	private int mGame;
	private int mScore;
	private int mHighscore;
	
	private boolean mIsGuessing;
	
	private boolean mIsSoundOn;
	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundPoolMap;
	
	private final int GUESS_SOUND_ID = 1;
	
	private FrameLayout mLayout;
    private View mQuitLayer;
    private View mResultLayer;
		
	public interface Listener {
        public void onEnteredScore(int level, int score);
        public void onQuitClicked();
    }

    Listener mListener = null;
    
    public void setListener(Listener l) {
    	mListener = l;
    }
    public void setIsSoundOn(boolean isSoundOn) {
    	mIsSoundOn = isSoundOn;
    }
    
    public void setState(int level, int highscore) {
    	assert(0 <= level && level < MainActivity.NUMBER_OF_LEVELS);
    	
    	mLevel = level;  	
    	mGame = 1;
    	mHighscore = highscore;
    	mScore = 0;
    }

    @SuppressLint("UseSparseArrays")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mLayout = new FrameLayout(getActivity());
        View v = inflater.inflate(R.layout.game_layout, container, false);
        
        // quit layer
        mQuitLayer = inflater.inflate(R.layout.quit_layout, container, false);        
        mQuitLayer.findViewById(R.id.yes_button).setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				mQuitLayer.setVisibility(View.GONE);
				mListener.onQuitClicked();
			}
		});
        mQuitLayer.findViewById(R.id.no_button).setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				dismissQuitLayer();
			}
		});
        mQuitLayer.setVisibility(View.GONE);
        
        // result layer
        mResultLayer = inflater.inflate(R.layout.temp_result_layout, container, false);
        mResultLayer.findViewById(R.id.next_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mResultLayer.setVisibility(View.GONE);
				
				if (mGame == GAMES_PER_LEVEL) {
					mListener.onEnteredScore(mLevel, mScore / GAMES_PER_LEVEL);
				}
				else
					startNextGame();
			}
		});
        mResultLayer.setVisibility(View.GONE);
        
        
        mShowView = (ShowView) v.findViewById(R.id.showView);
	    mGuessView = (GuessView) v.findViewById(R.id.guessView);
	    mGuessButton = (FontTextView) v.findViewById(R.id.guessButton);
	    
	    mGuessView.setListener(this);
	    mGuessView.setLevel(mLevel / 5);
	    
	    v.findViewById(R.id.guessButton).setOnClickListener(this);
	    	    
	    GAMES_PER_LEVEL = getResources().getInteger(R.integer.games_per_level);
	    
	    mShowView.setLevel(mLevel % 5);
	    mShowView.setAreaBounds(mGuessView.getMinArea(), mGuessView.getMaxArea());
    	mShowView.update();
    	
    	// sound
    	if (mIsSoundOn) {
	    	mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	    	mSoundPoolMap = new HashMap<Integer, Integer>();
	    	mSoundPoolMap.put(GUESS_SOUND_ID, mSoundPool.load(getActivity(), R.raw.guess, 1));
    	}
    	
    	mGame = 1;
    	mScore = 0;
    	
    	mIsGuessing = true;
    	
    	mLayout.addView(v);
    	mLayout.addView(mResultLayer);
    	mLayout.addView(mQuitLayer);
    		    
        return mLayout;
    }
    
    private void startNextGame() {
		mGame++;
		
		updateUi();
		
		mShowView.update();
		
		mGuessView.reset();
		
		mIsGuessing = true;
    }
    
    public boolean isQuitLayerVisible() {
    	return mQuitLayer.getVisibility() == View.VISIBLE;
    }
    public void dismissQuitLayer() {
    	mQuitLayer.setVisibility(View.GONE);
    	mGuessView.setIsEditable(true);
    }
    public void openQuitLayer() {
    	mQuitLayer.setVisibility(View.VISIBLE);
    	mGuessView.setIsEditable(false);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }
    
    private void updateUi() {
    	if (getActivity() == null)
    		return;
    	
        int decimalPlaces = getResources().getInteger(R.integer.score_decimal_places);

    	TextView highscore = (TextView) getActivity().findViewById(R.id.highscore_text_view);
		if (highscore != null) {
			if (mHighscore == -1)
				highscore.setText(getString(R.string.no_heighscore_yet));
			else
				highscore.setText(Utils.googleScoreToScore(mHighscore, decimalPlaces));
		}
		
		TextView score = (TextView) getActivity().findViewById(R.id.score_text_view);
		if (score != null) {
			if (mGame == 1)
				score.setText(getString(R.string.no_score_yet));
			else
				score.setText(Utils.googleScoreToScore(mScore / (mGame-1), decimalPlaces));
		}
		
		TextView round = (TextView) getActivity().findViewById(R.id.round_text_view);
		if (round != null) round.setText(""+mGame);
    }

	@Override
	public void onClick(View view) {
		if (isQuitLayerVisible())
			return;
		
		if (mIsGuessing) {
			if (mIsSoundOn) {
				AudioManager audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
				float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			    float leftVolume = curVolume/maxVolume;
				float rightVolume = curVolume/maxVolume;
				int priority = 1;
				int noLoop = 0;
				float normalPlaybackRate = 1f;
				mSoundPool.play(mSoundPoolMap.get(GUESS_SOUND_ID), leftVolume, rightVolume, priority, noLoop, normalPlaybackRate);
			}
			
			double a0 = mShowView.getArea(), a1 = mGuessView.getArea();
			mScore += Math.abs(a0-a1)*100*100/a0;
			
			int decimalPlaces = getResources().getInteger(R.integer.score_decimal_places);
			
			TextView tv = (TextView) mResultLayer.findViewById(R.id.temp_result_display);
			if (tv != null) {
				String score = Utils.googleScoreToScore((int) (Math.abs(a0-a1)*100*100/a0), decimalPlaces);
				if (a0 < a1)
					tv.setText(score + " too large");
				else
					tv.setText(score + " too small");
			}
			
			mGuessView.animate(mShowView.getArea());
			mIsGuessing = false;
		}
	}
	
	@Override
	public void onAreaBoundsChanged() {
		mShowView.setAreaBounds(mGuessView.getMinArea(), mGuessView.getMaxArea());
	}
	@Override
	public void onAnimationFinished() {		
		new Thread() {
		    @Override
		    public void run() {
		        try {
		            Thread.sleep(1000);
		        } catch (InterruptedException e) {
		        }

		        getActivity().runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		            	mResultLayer.setVisibility(View.VISIBLE);
		            }
		        });
		    }
		}.run();
	}

}
