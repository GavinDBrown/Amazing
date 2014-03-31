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

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.TeamAmazing.drawing.MazeCompletedDialogFragment;
import com.TeamAmazing.drawing.MazeCompletedDialogFragment.OnDialogClosedListener;
import com.TeamAmazing.drawing.MazeSurfaceView;
import com.TeamAmazing.drawing.MazeThread;
import com.TeamAmazing.game.R;

public class MazeGame extends Activity implements OnDialogClosedListener {
	/** A handle to the thread that's running the maze. */
	private MazeThread mMazeThread;

	/** A handle to the View displaying the maze. */
	private MazeSurfaceView mMazeView;

	private MyActivityHandler activityHandler;

	private Menu mOptionsMenu;

	private String timerText = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		activityHandler = new MyActivityHandler(this);

		setContentView(R.layout.maze_game);
		// get handles to the View and start the Thread.
		mMazeView = (MazeSurfaceView) findViewById(R.id.maze_view);
		mMazeThread = new MazeThread(mMazeView.getHolder(), this,
				activityHandler);
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
	protected void onDestroy() {
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
	public void onRestoreInstanceState(Bundle inState) {
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
		mOptionsMenu = menu;
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maze_game_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem menuItem = menu.findItem(R.id.maze_timer);
		final TextView textView = (TextView) menuItem.getActionView();
		textView.setText(timerText);
		return super.onPrepareOptionsMenu(menu);
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

	@Override
	public void onDialogClosed() {
		mMazeThread.onDialogClosed();
	}

	static class MyActivityHandler extends Handler {
		private final WeakReference<MazeGame> mActivity;

		MyActivityHandler(MazeGame act) {
			mActivity = new WeakReference<MazeGame>(act);
		}

		@Override
		public void handleMessage(Message msg) {
			MazeGame act = mActivity.get();
			if (act != null) {
				act.handleMessage(msg);
			}
		}
	}

	public void handleMessage(Message msg) {
		switch ((int) msg.what) {
		case MazeThread.MESSAGE_MAZE_COMPLETED:
			// display congratulatory dialog
			MazeCompletedDialogFragment congratulationsFragment = new MazeCompletedDialogFragment();
			Bundle args = new Bundle();
			args.putInt("time", msg.arg1);
			congratulationsFragment.setArguments(args);
			congratulationsFragment.show(getFragmentManager(),
					"TAG_MAZE_COMPLETED");
			break;
		case MazeThread.MESSAGE_UPDATE_TIMER:
			// update the timer with the supplied string
			timerText = millisToString(msg.arg1);
			if (mOptionsMenu != null) {
				// TODO somehow was not running on the UI thread without the
				// runOnUiThread method, why?
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						onPrepareOptionsMenu(mOptionsMenu);

					}
				});

			}
		}
	}

	private String millisToString(int time) {
		int millis = (time % 1000) / 100;
		int second = (time / 1000) % 60;
		int minute = (time / (1000 * 60)) % 60;
		int hour = (time / (1000 * 60 * 60)) % 24;
		String string;

		if (hour > 0) {
			string = String.format("%d:%02d:%02d.%d", hour, minute, second,
					millis);
		} else if (minute > 0) {
			string = String.format("%d:%02d.%d", minute, second, millis);
		} else {
			string = String.format("%d.%d", second, millis);
		}

		return string;
	}

}
