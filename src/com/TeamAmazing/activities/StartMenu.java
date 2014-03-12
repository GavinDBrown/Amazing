//    Amazing, the simple maze game.
//    Copyright (C) 2014  Gavin Brown
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import com.TeamAmazing.drawing.GOLSurfaceView;
import com.TeamAmazing.game.R;

public class StartMenu extends Activity {

	public static final int PERFECT_MAZE = 0;
	public static final int DFS_MAZE = 1;
	public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

	/** A handle to the thread that's running the Game Of Life animation. */
	private GOLThread mGOLThread;

	/** A handle to the View in which the background is running. */
	private GOLSurfaceView mGOLView;

	private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Check if the game of life background is enabled
		if (sharedPrefs.getBoolean("pref_start_background", true)) {
			setContentView(R.layout.game_of_life_background);
			startGOLBackground();

		} else {
			// The game of life background is disabled
			setContentView(R.layout.start_menu_background);
		}

		prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs,
					String key) {
				if (prefs.getBoolean("pref_start_background", true)) {
					// background is going from disabled -> enabled
					setContentView(R.layout.game_of_life_background);
					startGOLBackground();

				} else {
					// background is going from enabled -> disabled
					setContentView(R.layout.start_menu_background);
					stopGOLBackground();
				}
			}
		};

		sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener);

	}

	private void startGOLBackground() {
		// get handles to the GOLView and it's GOLThread
		mGOLView = (GOLSurfaceView) findViewById(R.id.game_of_life_background);
		mGOLThread = new GOLThread(mGOLView.getHolder());
		mGOLView.setThread(mGOLThread);
		mGOLThread.start();
	}

	private void stopGOLBackground() {
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
			mGOLThread = null;
			mGOLView = null;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Check if the GOLBackground is enabled
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_start_background", true)) {
			mGOLThread.saveState(outState);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Check if the GOLBackground is enabled
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_start_background", true))
			mGOLThread.restoreState(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_start_background", true)) {
			mGOLThread.unpause();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_start_background", true)) {
			// When the screen is locked, the app doesn't lose focus and the
			// surface
			// remains intact, so we need to pause here.
			mGOLThread.pause();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(prefsListener);

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_start_background", true)) {
			stopGOLBackground();
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
