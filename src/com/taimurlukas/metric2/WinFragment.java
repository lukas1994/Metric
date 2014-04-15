package com.taimurlukas.metric2;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.taimurlukas.metric2.MainActivity.State;

public class WinFragment extends Fragment implements OnClickListener {
	
	final State STATE = State.WIN;
	
	private final int mMinScoreIds[] = new int[] {
    		R.integer.win_level_1,
    		R.integer.win_level_2,
    		R.integer.win_level_3,
    		R.integer.win_level_4,
    		R.integer.win_level_5,
    		R.integer.win_level_6,
    		R.integer.win_level_7,
    		R.integer.win_level_8,
    		R.integer.win_level_9,
    		R.integer.win_level_10,
    		R.integer.win_level_11,
    		R.integer.win_level_12,
    		R.integer.win_level_13,
    		R.integer.win_level_14,
    		R.integer.win_level_15,
    		R.integer.win_level_16,
    		R.integer.win_level_17,
    		R.integer.win_level_18,
    		R.integer.win_level_19,
    		R.integer.win_level_20,
    		R.integer.win_level_21,
    		R.integer.win_level_22,
    		R.integer.win_level_23,
    		R.integer.win_level_24,
    		R.integer.win_level_25
    };
	
	int mLevel;
    int mScore;
    boolean mShowSignIn = false;

    public interface Listener {
        public void onWinScreenDismissed();
        public void onWinScreenSignInClicked();
        public void onTryAgainClicked(int level);
    }

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.win_layout, container, false);
        v.findViewById(R.id.win_ok_button).setOnClickListener(this);
        v.findViewById(R.id.win_screen_sign_in_button).setOnClickListener(this);
        v.findViewById(R.id.share_button).setOnClickListener(this);
        v.findViewById(R.id.try_again_button).setOnClickListener(this);
        v.findViewById(R.id.share_button).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;
				switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.getDrawable().setColorFilter(0x77000000,PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                	share();
                case MotionEvent.ACTION_CANCEL:
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    break;
				}

				return true;
			}
		});
        
        return v;
    }

    public void setFinalScore(int level, int score) {
    	mLevel = level;
        mScore = score;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView scoreTv = (TextView) getActivity().findViewById(R.id.score_display);
        
        int decimalPlaces = getResources().getInteger(R.integer.score_decimal_places);

        if (scoreTv != null) scoreTv.setText(Utils.googleScoreToScore(mScore, decimalPlaces));

        LinearLayout signin = (LinearLayout) getActivity().findViewById(R.id.win_screen_sign_in_bar);
        if (signin != null) signin.setVisibility(mShowSignIn ? View.VISIBLE : View.GONE);
        
        TextView tv1 = (TextView) getActivity().findViewById(R.id.text_view_1);
        TextView tv2 = (TextView) getActivity().findViewById(R.id.text_view_2);
        LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.try_again_button);
        
        if (tv1 != null && tv2 != null && ll != null) {
        	if (mScore <= getResources().getInteger(mMinScoreIds[mLevel])) {
	       		tv1.setText("You needed less than " + Utils.googleScoreToScore(getResources().getInteger(mMinScoreIds[mLevel]), decimalPlaces) + 
	       				" to get to the next level.");
	       		tv2.setText("Well done!");
	       		tv2.setVisibility(View.VISIBLE);
	       		ll.setVisibility(View.GONE);
	       	}
	       	else {
	       		tv1.setText("You need less than " + Utils.googleScoreToScore(getResources().getInteger(R.integer.win_level_1), decimalPlaces) + 
	       				" to get to the next level.");
	       		tv2.setVisibility(View.GONE);
	       		ll.setVisibility(View.VISIBLE);
	        }
        	
        }

    }
    
    private void share() {
    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
	    shareIntent.setType("text/plain");
	    shareIntent.putExtra(Intent.EXTRA_TEXT, "My score was " + Utils.googleScoreToScore(mScore, getResources().getInteger(R.integer.score_decimal_places)) + 
	    		" in level " + (mLevel+1) + "!!!\n\n" +
	    		"https://play.google.com/store/apps/details?id=com.taimurlukas.metric2");
	    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Metric");
	    startActivity(Intent.createChooser(shareIntent, "Share..."));
    }

    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
    	case R.id.win_screen_sign_in_button:
    		mListener.onWinScreenSignInClicked();
    		break;
    	case R.id.win_ok_button:
    		mListener.onWinScreenDismissed();
    		break;
    	case R.id.try_again_button:
    		mListener.onTryAgainClicked(mLevel);
    		break;
    	case R.id.share_button:
    		share();
    		break;
    	}
    }

    public void setShowSignInButton(boolean showSignIn) {
        mShowSignIn = showSignIn;
        updateUi();
    }
}
