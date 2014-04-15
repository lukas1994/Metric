package com.taimurlukas.metric2;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.SignInButton;
import com.taimurlukas.metric2.MainActivity.State;

public class MenuFragment extends Fragment
	implements OnTouchListener, OnClickListener {
	
	final State STATE = State.MENU;
		
	public interface Listener {
        public void onStartGameRequested();
        public void onShowLeaderboardsRequested();
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
        public void onToggleSoundClicked();
    }

	private Listener mListener = null;
    private boolean mShowSignIn;
    private boolean mIsSoundOn;
    private String mDisplayName;
    
    private FrameLayout mLayout;
    private View mHelpLayer;
    
    private boolean mIsHelpLayerVisible = false;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayout = new FrameLayout(getActivity());
		View v = inflater.inflate(R.layout.menu_layout, container, false);
		mHelpLayer = inflater.inflate(R.layout.help_layout, container, false);
		
		mHelpLayer.findViewById(R.id.close_button).setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				mHelpLayer.setVisibility(View.GONE);
				mIsHelpLayerVisible = false;
			}
		});
		if (!mIsHelpLayerVisible)
			mHelpLayer.setVisibility(View.GONE);
		
		
		final int[] buttons_touch = {
				R.id.sign_out_button,
				R.id.toggle_sound,
				R.id.help_button
		};
		final int[] buttons_click = {
				R.id.play_button,
				R.id.highscore_button,
				R.id.sign_in_button
		};
		for (int id : buttons_touch)
			v.findViewById(id).setOnTouchListener(this);
		for (int id : buttons_click)
			v.findViewById(id).setOnClickListener(this);
		
		mLayout.addView(v);
		mLayout.addView(mHelpLayer);
        
		return mLayout;
	}
	
	public void cancelHelpLayer() {
		if (mHelpLayer != null)
			mHelpLayer.setVisibility(View.GONE);
		mIsHelpLayerVisible = false;
	}
	public void openHelpLayer() {
		if (mHelpLayer != null)
			mHelpLayer.setVisibility(View.VISIBLE);
		mIsHelpLayerVisible = true;
	}
	public boolean isHelpLayerVisible() {
		if (mHelpLayer == null || mHelpLayer.getVisibility() == View.GONE)
			return false;
		return true;
	}
	
	public void setListener(Listener l) {
        mListener = l;
    }
	public void setShowSignInButton(boolean b) {
		mShowSignIn = b;
		updateUi();
	}
	public void setIsSoundOn(boolean isSoundOn) {
		mIsSoundOn = isSoundOn;
		updateUi();
	}
	public void setDisplayName(String displayName) {
		mDisplayName = displayName;
		updateUi();
	}

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }
    
    private void updateUi() {
        if (getActivity() == null) return;

        SignInButton signInButton = (SignInButton) getActivity().findViewById(R.id.sign_in_button);
        if (signInButton != null) {
        	signInButton.setVisibility(mShowSignIn ? View.VISIBLE : View.GONE);
        	signInButton.setStyle(SignInButton.SIZE_STANDARD, SignInButton.COLOR_LIGHT);
        }
        
        LinearLayout signOutButton = (LinearLayout) getActivity().findViewById(R.id.sign_out_button);
        if (signOutButton != null)
        	signOutButton.setVisibility(mShowSignIn ? View.INVISIBLE : View.VISIBLE);
        
        FontTextView2 greetingBox = (FontTextView2) getActivity().findViewById(R.id.signed_in_view);
        if (greetingBox != null) {
        	if (mShowSignIn) {
        		greetingBox.setVisibility(View.GONE);
        	}
        	else{ 
	        	String greeting = getString(R.string.greeting);
	        	if (mDisplayName != null && mDisplayName != "")
	        		greeting += " " + mDisplayName.trim();        		
	        	greetingBox.setText(greeting);
	        	greetingBox.setVisibility(View.VISIBLE);
        	}
        }
        
     		
        // sound
     	ImageView soundButton = (ImageView) getActivity().findViewById(R.id.toggle_sound);
     	if (soundButton != null) {
	     	if (mIsSoundOn)
	   			soundButton.setImageResource(R.drawable.music);
	     	else
	     		soundButton.setImageResource(R.drawable.nomusic);
     	}
    }

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (mHelpLayer.getVisibility() == View.VISIBLE)
			return true;
		
		switch (view.getId()) {
        case R.id.sign_out_button:
        	switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //((ImageView) view).setColorFilter(0x77000000,PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
                break;
            case MotionEvent.ACTION_UP:
            	mListener.onSignOutButtonClicked();
            case MotionEvent.ACTION_CANCEL:
                //((ImageView) view).clearColorFilter();
                break;
			}
        	view.invalidate();
            break;
        case R.id.toggle_sound:
        	switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ((ImageView) view).setColorFilter(0x77000000,PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
                break;
            case MotionEvent.ACTION_UP:
            	mListener.onToggleSoundClicked();
            case MotionEvent.ACTION_CANCEL:
                ((ImageView) view).clearColorFilter();
                break;
			}
        	view.invalidate();
        	break;
		case R.id.help_button:
			switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ((ImageView) view).setColorFilter(0x77000000,PorterDuff.Mode.SRC_ATOP);
                view.invalidate();
                break;
            case MotionEvent.ACTION_UP:
            	mHelpLayer.setVisibility(View.VISIBLE);
            case MotionEvent.ACTION_CANCEL:
                ((ImageView) view).clearColorFilter();
                break;
			}
        	view.invalidate();
        	break;
		}
		
		return true;
	}

	@Override
	public void onClick(View view) {
		if (mHelpLayer.getVisibility() == View.VISIBLE)
			return;
		switch (view.getId()) {
		case R.id.play_button:
			mListener.onStartGameRequested();
            break;
		case R.id.highscore_button:
			mListener.onShowLeaderboardsRequested();
            break;
		case R.id.sign_in_button:
			mListener.onSignInButtonClicked();
            break;
		}
	}
}

