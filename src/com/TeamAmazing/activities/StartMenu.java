package com.TeamAmazing.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.TeamAmazing.drawing.GOLThread;
import com.TeamAmazing.drawing.GOLView;
import com.TeamAmazing.game.R;

public class StartMenu extends Activity {

	public static final int PERFECT_MAZE = 0;
	public static final int DFS_MAZE = 1;
	public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

	/** A handle to the thread that's running the Game Of Life animation. */
	private GOLThread mGOLThread;

	/** A handle to the View in which the background is running. */
	private GOLView mGOLView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Check if the game of life background is enabled
		if (sharedPrefs.getBoolean("pref_start_background", false)) {
			setContentView(R.layout.game_of_life_background);
			// get handles to the GOLView and its GOLThread
			mGOLView = (GOLView) findViewById(R.id.game_of_life_background);
			mGOLThread = new GOLThread(mGOLView.getHolder());
			mGOLView.setThread(mGOLThread);
			mGOLThread.start();
		} else {
			// The game of life background is disabled
			setContentView(R.layout.start_menu_background);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
//		SharedPreferences sharedPrefs = PreferenceManager
//				.getDefaultSharedPreferences(this);
//
//		// Check if the game of life background is enabled
//		if (sharedPrefs.getBoolean("pref_start_background", false)) {
//			setContentView(R.layout.game_of_life_background);
//
//		} else {
//			// The game of life background is disabled
//			setContentView(R.layout.start_menu_background);
//		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Check if the game of life thread is non-null i.e. the background
		// could be disabled
		if (mGOLThread != null) {
			mGOLThread.saveState(outState);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_start_background", false))
			mGOLThread.restoreState(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (mGOLThread == null){
			// TODO maybe I should always do this?
			mGOLThread = mGOLView.getThread();
		}
//		
//		if (mGOLThread != null)
//			
//			mGOLThread.unpause(); // pause animation if it's running
	}

	@Override
	public void onPause() {
		super.onPause();
//
//		if (mGOLThread != null)
//			mGOLThread.pause(); // pause animation if it's running
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mGOLThread != null) {
			mGOLThread.halt(); // stop the animation if it's valid
			boolean retry = true;
			while (retry) {
				try {
					mGOLThread.join();
					retry = false;
				} catch (InterruptedException e) {
				}

			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.start_menu_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Kruskal's algorithm
	public void startKruskalsMaze(View v) {
		Intent intent = new Intent(this, MazeGame.class);
		intent.putExtra(MAZE_TYPE, PERFECT_MAZE);
		startActivity(intent);
	}

	// Recursive backtracker algorithm
	public void startRecursiveBacktrackerMaze(View v) {
		Intent intent = new Intent(this, MazeGame.class);
		intent.putExtra(MAZE_TYPE, DFS_MAZE);
		startActivity(intent);
	}

}
