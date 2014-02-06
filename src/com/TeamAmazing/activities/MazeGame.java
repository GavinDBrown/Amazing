package com.TeamAmazing.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.TeamAmazing.drawing.MazeSurfaceView;
import com.TeamAmazing.drawing.MazeThread;
import com.TeamAmazing.game.R;

public class MazeGame extends Activity {
	/** A handle to the thread that's running the maze. */
	private MazeThread mMazeThread;

	/** A handle to the View displaying the maze. */
	private MazeSurfaceView mMazeView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.maze_game);
		// get handles to the View and start the Thread.
		mMazeView = (MazeSurfaceView) findViewById(R.id.maze_view);
		mMazeThread = new MazeThread(mMazeView.getHolder(), this);
		mMazeThread.setMazeType(getIntent().getIntExtra(StartMenu.MAZE_TYPE,
				StartMenu.PERFECT_MAZE));
		mMazeView.setThread(mMazeThread);
		mMazeThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMazeThread.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMazeThread.unpause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if (mMazeThread != null) {
			mMazeThread.halt(); 
			boolean retry = true;
			while (retry) {
				try {
					mMazeThread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
			mMazeThread = null;
			mMazeView = null;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle inState){
		super.onRestoreInstanceState(inState);
		mMazeThread.restoreState(inState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMazeThread.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maze_game_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.reset_maze:
			mMazeThread.initGFX();
			return true;
		case android.R.id.home:
			// This ensures that the parent activity is recreated with any
			// information it may have saved.
			Intent intent = NavUtils.getParentActivityIntent(this);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			NavUtils.navigateUpTo(this, intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
