package com.taimurlukas.metric2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.example.games.basegameutils.BaseGameActivity;

public class MainActivity extends BaseGameActivity
	implements MenuFragment.Listener, GamePlayFragment.Listener, WinFragment.Listener,
				LevelSelectorFragment.Listener {

	public enum State {
		MENU, GAME, WIN, LEVEL
	};
	State mState = State.MENU;
	
	// Fragments
    private MenuFragment mMenuFragment;
    private GamePlayFragment mGamePlayFragment;
    private WinFragment mWinFragment;
    private LevelSelectorFragment mLevelSelectorFragment;    
    
    // ads
    private AdView mAdView = null;
    private boolean mIsShowingAds;
    
    // sound
    private MediaPlayer mPlayer = null;
    private int mPlayingPos = -1;
    private boolean mIsSoundOn;
    private int mCurrentSongId;
    
    private final String STORAGE_KEY = "PREFERENCES";

    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;
    final String TAG = "Metric";

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    private AccomplishmentsOutbox mOutbox;
    
    private final int LEADER_BOARDS[] = new int[] {
    		R.string.leaderboard_level_1,
    		R.string.leaderboard_level_2,
    		R.string.leaderboard_level_3,
    		R.string.leaderboard_level_4,
    		R.string.leaderboard_level_5,
    		R.string.leaderboard_level_6,
    		R.string.leaderboard_level_7,
    		R.string.leaderboard_level_8,
    		R.string.leaderboard_level_9,
    		R.string.leaderboard_level_10,
    		R.string.leaderboard_level_11,
    		R.string.leaderboard_level_12,
    		R.string.leaderboard_level_13,
    		R.string.leaderboard_level_14,
    		R.string.leaderboard_level_15,
    		R.string.leaderboard_level_16,
    		R.string.leaderboard_level_17,
    		R.string.leaderboard_level_18,
    		R.string.leaderboard_level_19,
    		R.string.leaderboard_level_20,
    		R.string.leaderboard_level_21,
    		R.string.leaderboard_level_22,
    		R.string.leaderboard_level_23,
    		R.string.leaderboard_level_24,
    		R.string.leaderboard_level_25
    };
    /*private final int ACHIEVEMENTS[] = new int[] {
    		R.string.achievement_1_id,
    		R.string.achievement_2_id,
    		R.string.achievement_3_id,
    		R.string.achievement_4_id,
    		R.string.achievement_5_id,
    };*/
    
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
    
    public static final int NUMBER_OF_LEVELS = 25;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // create fragments
        mMenuFragment = new MenuFragment();
        mGamePlayFragment = new GamePlayFragment();
        mWinFragment = new WinFragment();
        mLevelSelectorFragment = new LevelSelectorFragment();

        // listen to fragment events
        mMenuFragment.setListener(this);
        mGamePlayFragment.setListener(this);
        mWinFragment.setListener(this);
        mLevelSelectorFragment.setListener(this);
        
        // add initial fragment (welcome fragment)
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
        		mMenuFragment).commit();
        
        // load data
        mOutbox = new AccomplishmentsOutbox();
        mOutbox.loadLocal(this);

        updateLevelSelectorState();
        
        // load assets
        Assets.font = Typeface.createFromAsset(getAssets(), "AvenirNext.ttf");
        Assets.font2 = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        
        // keep screen on / avoid dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // sound
        SharedPreferences settings = getSharedPreferences(STORAGE_KEY, 0);
        mIsSoundOn = settings.getBoolean("audio", true);
        mGamePlayFragment.setIsSoundOn(mIsSoundOn);
        mMenuFragment.setIsSoundOn(mIsSoundOn);
        mCurrentSongId = R.raw.background;
        
        // ads
        mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(getString(R.string.ad_unit_id));
        
        final LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
        
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
        	layout.addView(mAdView, 1);
        	mIsShowingAds = true;
        }
        else {
        	mIsShowingAds = false;
        }  
        
        AdRequest adRequest = new AdRequest.Builder()
        /*.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("BCC3C650192BB4F5E2271F655942935F") // change to your id */
        .build();

        mAdView.loadAd(adRequest);
        
        BroadcastReceiver wifiConnectionListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                	if (!mIsShowingAds) {
                		layout.addView(mAdView, 1);
                		mIsShowingAds = true;
                	}
                }
                else if (mIsShowingAds) {
                	layout.removeViewAt(1);
                	mIsShowingAds = false;
                }
            }
        };
        registerReceiver(wifiConnectionListener, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        
        // first play?
        if (settings.getBoolean("first", true)) {
        	mMenuFragment.openHelpLayer();

        	SharedPreferences.Editor editor = settings.edit();
        	editor.putBoolean("first", false);
        	editor.apply();
        }
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        
    	// google analytics
        EasyTracker.getInstance(this).activityStart(this);
    }
    private void createMediaPlayer() {
    	mPlayer = MediaPlayer.create(this, mCurrentSongId);
        mPlayer.setLooping(true);
        if (mPlayingPos != -1)
        	mPlayer.seekTo(mPlayingPos);
        mPlayer.start();
    }
    private void destroyMediaPlayer() {
    	if (mPlayer != null) {
			mPlayingPos = mPlayer.getCurrentPosition();
    		mPlayer.stop();
    		mPlayer.reset();
        	mPlayer.release();
        	mPlayer = null;
		}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        
    	// sound
    	if (mIsSoundOn && mPlayer == null) {
    		createMediaPlayer();
    	}
    	
    	// ads
    	if (mAdView != null) {
    		mAdView.resume();
    	}
    }
    @Override
    public void onPause() {
    	// ads
    	if (mAdView != null) {
    		mAdView.pause();
    	}
    	
    	super.onPause();
    }
    @Override
    public void onDestroy() {
    	// ads
    	if (mAdView != null) {
    		mAdView.destroy();
    	}
    	
    	super.onDestroy();
    }
    @Override
    public void onStop() {
    	super.onStop();
    	
    	// google analytics
    	EasyTracker.getInstance(this).activityStop(this);
    	
    	// sound
    	destroyMediaPlayer();
    	
        SharedPreferences settings = getSharedPreferences(STORAGE_KEY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("audio", mIsSoundOn);
        editor.apply();
    }  
    @Override
	public void onToggleSoundClicked() {
    	if (mIsSoundOn) {
    		mIsSoundOn = false;
    		mGamePlayFragment.setIsSoundOn(mIsSoundOn);
    		mMenuFragment.setIsSoundOn(mIsSoundOn);
    		destroyMediaPlayer();
    	}
    	else {
    		mIsSoundOn = true;
    		
    		mGamePlayFragment.setIsSoundOn(mIsSoundOn);
    		mMenuFragment.setIsSoundOn(mIsSoundOn);
    		
    		createMediaPlayer();
    	}
	}
    
    
    @Override
    public void onBackPressed() {
    	if (mState == State.MENU && mMenuFragment.isHelpLayerVisible()) {
			mMenuFragment.cancelHelpLayer();
			return;
		}
    	if (mState == State.WIN) {
    		onWinScreenDismissed();
    		return;
    	}
    	if (mState == State.GAME) {
    		if (mGamePlayFragment.isQuitLayerVisible())
    			mGamePlayFragment.dismissQuitLayer();
    		else
    			mGamePlayFragment.openQuitLayer();
    		return;
    	}
    	if (mState == State.LEVEL) {
    		mState = State.MENU;
    		getSupportFragmentManager().popBackStackImmediate();
    		return;
    	}
    	
    	super.onBackPressed();
    }

    // Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
    	if (newFrag instanceof GamePlayFragment)
    		mState = State.GAME;
    	else if (newFrag instanceof LevelSelectorFragment)
    		mState = State.LEVEL;
    	else if (newFrag instanceof WinFragment)
    		mState = State.WIN;
    	else if (newFrag instanceof MenuFragment)
    		mState = State.MENU;
    	else
    		assert(false);

        getSupportFragmentManager().beginTransaction().addToBackStack(null)
        .replace(R.id.fragment_container, newFrag).commit();
    }
    
    @Override
	public void onQuitClicked() {
    	getSupportFragmentManager().popBackStackImmediate();
    	mState = State.LEVEL;
	}

    @Override
    public void onStartGameRequested() {
    	switchToFragment(mLevelSelectorFragment);
    }
    
    @Override
	public void onLevelSelected(int level) {
    	Log.e("level", ""+level);
    	assert(1 <= level && level <= NUMBER_OF_LEVELS);
		
    	mGamePlayFragment.setState(level, mOutbox.mScores[level]);
		switchToFragment(mGamePlayFragment);
	}
    
    @SuppressLint("InlinedApi")
	@Override
    public void onShowLeaderboardsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),
                    RC_UNUSED);
        } else {
        	ContextThemeWrapper themedContext;
        	if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
        	    themedContext = new ContextThemeWrapper( this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar );
        	}
        	else {
        	    themedContext = new ContextThemeWrapper( this, android.R.style.Theme_Light_NoTitleBar );
        	}
        	new AlertDialog.Builder(themedContext)
            .setTitle("Leaderboard")
            .setMessage(getString(R.string.leaderboards_not_available))
            .setPositiveButton(android.R.string.ok, null)
            .setIcon(R.drawable.launcher)
             .show();
        }
    }

    private boolean isWin(int level, int score) {
    	return score <= getResources().getInteger(mMinScoreIds[level]);
    }
    
    @Override
    public void onEnteredScore(int level, int score) {
        mWinFragment.setFinalScore(level, score);

        destroyMediaPlayer();
        mPlayingPos = -1;
        
        if (isWin(level, score)) { // win
        	mCurrentSongId = R.raw.youwin;
        }
        else { // loose
        	mCurrentSongId = R.raw.youlose;
        }
        if (mIsSoundOn)
        	createMediaPlayer();

        // update leaderboards
        updateLeaderboards(level, score);

        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();

        // switch to the exciting "you won" screen
        switchToFragment(mWinFragment);
    }
    
    /*void unlockAchievement(int achievementId, String fallbackString) {
        if (isSignedIn()) {
            Games.Achievements.unlock(getApiClient(), getString(achievementId));
        } else {
            Toast.makeText(this, getString(R.string.achievement) + ": " + fallbackString,
                    Toast.LENGTH_LONG).show();
        }
    }*/

    /*void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }*/

    void pushAccomplishments() {
    	mOutbox.saveLocal(this);
    	
        if (!isSignedIn()) {
            return;
        }
        
        for (int i = 0; i < NUMBER_OF_LEVELS; i++) {
        	if (mOutbox.mScores[i] >= 0) {
                Games.Leaderboards.submitScore(getApiClient(), getString(LEADER_BOARDS[i]),
                        mOutbox.mScores[i]);
                
                // Games.Achievements.unlock(getApiClient(), getString(ACHIEVEMENTS[i]));
            }
        }
    }

    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
    void updateLeaderboards(int level, int score) {
    	if (mOutbox.mScores[level] == -1 || mOutbox.mScores[level] > score)
    		mOutbox.mScores[level] = score;
    	updateLevelSelectorState();
    }

    @Override
    public void onWinScreenDismissed() {
    	int n = getSupportFragmentManager().getBackStackEntryCount();
    	for (int i = 0; i < n-1; i++)
    		getSupportFragmentManager().popBackStackImmediate();
    	
    	mState = State.LEVEL;

    	destroyMediaPlayer();
    	mCurrentSongId = R.raw.background;
    	mPlayingPos = -1;
        if (mIsSoundOn)
        	createMediaPlayer();
    }
    @Override
	public void onTryAgainClicked(int level) {
    	mGamePlayFragment.setState(level, mOutbox.mScores[level]);
    	getSupportFragmentManager().popBackStackImmediate();
    	
    	mState = State.GAME;

    	destroyMediaPlayer();
    	mCurrentSongId = R.raw.background;
    	mPlayingPos = -1;
        if (mIsSoundOn)
        	createMediaPlayer();
        
	}

    @Override
    public void onSignInFailed() {
        // Sign-in failed, so show sign-in button on main menu
        mMenuFragment.setShowSignInButton(true);
        mWinFragment.setShowSignInButton(true);
    }

    @Override
    public void onSignInSucceeded() {
    	
    	
        // Show sign-out button on main menu
        mMenuFragment.setShowSignInButton(false);
        
        mWinFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(getApiClient());
        String displayName = "";
        if (p != null) {
            displayName = p.getDisplayName();
        }
        
        mMenuFragment.setDisplayName(displayName);

        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
        }
        
        // get scores from server
        for (int i = 0; i < NUMBER_OF_LEVELS; i++)
        	Games.Leaderboards.loadCurrentPlayerLeaderboardScore(getApiClient(), getString(LEADER_BOARDS[i]),
        			LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
        			.setResultCallback(new LeaderBoardResultListener(i));

    }
    
    private class LeaderBoardResultListener implements ResultCallback<LoadPlayerScoreResult> {
    	int mLeaderBoardNumber;
    	
    	public LeaderBoardResultListener(int num) {
    		mLeaderBoardNumber = num;
    	}
    	
    	@Override
		public void onResult(LoadPlayerScoreResult res) {
    		if (res != null && res.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK && res.getScore() != null) {
    			mOutbox.mScores[mLeaderBoardNumber] = (int) res.getScore().getRawScore();
    			updateLevelSelectorState();
    		}
		}
    }

    @Override
    public void onSignInButtonClicked() {
        beginUserInitiatedSignIn();
    }

    @Override
    public void onSignOutButtonClicked() {
        signOut();
        mMenuFragment.setShowSignInButton(true);
        mWinFragment.setShowSignInButton(true);
        mMenuFragment.setDisplayName("");
    }
    
    private void updateLevelSelectorState() {
    	int currentLevel = mOutbox.getCurrentLevel();
    	    	
    	mLevelSelectorFragment.setState(currentLevel, mOutbox.mScores);
    }
    
    class AccomplishmentsOutbox {
        int mScores[] = new int[NUMBER_OF_LEVELS];
                
        private final String KEY = "xy6d3;3";
        
        public AccomplishmentsOutbox() {
        	resetScores();
        }
        
        private void resetScores() {
        	for (int i = 0; i < NUMBER_OF_LEVELS; i++)
        		mScores[i] = -1;
        }

        public boolean isEmpty() {
        	for (int i = 0; i < NUMBER_OF_LEVELS; i++)
        		if (mScores[i] >= 0)
        			return false;
        	return true;
        }
        
        public int getCurrentLevel() {
        	int currentLevel = 0;
        	for (int i = 0; i < NUMBER_OF_LEVELS; i++) {
        		if (mScores[i] != -1 && mScores[i] < getResources().getInteger(mMinScoreIds[i]))
        			currentLevel++;
        		else
        			break;
        	}
        	
        	return currentLevel;
		}

		@SuppressWarnings("unused")
		private String encode(String s) {
        	String out = "";
        	for (int i = 0; i < s.length(); i++)
        		out += (char) (s.charAt(i) ^ KEY.charAt(i % KEY.length()));
        	return out;
        }

        public void saveLocal(Context ctx) {
        	String data = "";
        	for (int i = 0; i < NUMBER_OF_LEVELS; i++)
        		data += mScores[i] + ";";
        	        	
        	//data = encode(data);
        	        	
        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());// getSharedPreferences(STORAGE_KEY, 0);
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putString("data", data);
        	editor.apply();
        }

        public void loadLocal(Context ctx) {
        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getSharedPreferences(STORAGE_KEY, 0);
        	String data = settings.getString("data", "");
        	
        	//data = encode(data);
        	        	
        	try {
        		String[] s = data.split(";");
        		for (int i = 0; i < NUMBER_OF_LEVELS; i++)
        			mScores[i] = Integer.parseInt(s[i]);
        	}
        	catch(Exception e) {
        		//e.printStackTrace();
        		resetScores();
           	}
        }
    }

    @Override
    public void onWinScreenSignInClicked() {
        beginUserInitiatedSignIn();
    }
}
