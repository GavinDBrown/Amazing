/*  Amazing, the maze game.
 * Copyright (C) 2014  Gavin Brown
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.GavinDev.Amazing.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.activities.ActivityHandler.ActivityHandlerCallback;
import com.GavinDev.Amazing.activities.MazeCompletedDialog.MazeCompletedDialogCallback;
import com.GavinDev.Amazing.drawing.MazeSurfaceView;
import com.GavinDev.Amazing.drawing.MazeThread;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.Locale;

public class MazeGame extends BaseGameActivity implements MazeCompletedDialogCallback,
        ActivityHandlerCallback {
    /** The thread that's running the maze. */
    private MazeThread mMazeThread;

    /** The View displaying the maze. */
    private MazeSurfaceView mMazeView;

    private ActivityHandler mActivityHandler;

    /** The dialog displayed when the maze is completed. */
    private MazeCompletedDialog mCongratulationsFragment;

    private Menu mOptionsMenu;

    public int mMazeType;

    private String mTimerText = "";

    public static final String MAZE_COMPLETED_TIME_ID = "mazeCompletedTime";

    private static final int MINIMUM_TIME_TO_UNLOCK_NOVICE = 30000;
    private static final int MINIMUM_TIME_TO_UNLOCK_MASTER = 10000;

    /**
     * Called when an instance of MazeGame is created. Changes the screen
     * orientation as needed. Enables up navigation in the actionbar. Creates an
     * ActivityHandler with a weak reference to this activity and a reference to
     * the UI thread. Sets the content view. Creates a MazeThread and starts it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Set the desired orientation
        if (prefs.getBoolean("pref_orientation", true)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Avoid automatic sign-in's in this class.
        getGameHelper().setMaxAutoSignInAttempts(0);

        // Defines a Handler object that's attached to the UI thread
        mActivityHandler = new ActivityHandler(this, Looper.getMainLooper());

        mMazeType = getIntent().getIntExtra(StartMenu.MAZE_TYPE, -1);

        setContentView(R.layout.maze_game);
        // get handles to the View and start the MazeThread.
        mMazeView = (MazeSurfaceView) findViewById(R.id.maze_view);
        mMazeThread = new MazeThread(mMazeView.getHolder(), this, mActivityHandler, mMazeType);
        mMazeView.setThread(mMazeThread);
        mMazeThread.start();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        textView.setText(mTimerText);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.reset_maze:
                mMazeThread.newMaze();
                return true;
            case android.R.id.home:
                // Ensure that the parent activity is resumed if it's on the
                // stack
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReset() {
        mMazeThread.newMaze();
    }

    @Override
    public void onScores() {
        if (isSignedIn()) {
            switch (mMazeType) {
                case StartMenu.PERFECT_MAZE:
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                            getString(R.string.leaderboard_easy)), 0);
                    break;
                case StartMenu.GROWING_TREE_MAZE:
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                            getString(R.string.leaderboard_medium)), 0);
                    break;
                case StartMenu.DFS_MAZE:
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                            getString(R.string.leaderboard_hard)), 0);
                    break;
            }
        }
    }

    /**
     * Called when the menu button is pressed in the
     * MazeCompletedDialogFragment. Navigates back to the StartMenu activity.
     */
    @Override
    public void onStartMenu() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
    }

    /**
     * Called on the UI Thread via an ActivityHandler. Displays the
     * MazeCompletedDialogFragment or updates the timer in the actionbar.
     */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MazeThread.MESSAGE_MAZE_COMPLETED:
                mazeCompleted(msg.arg1, msg.arg2);
                break;
            case MazeThread.MESSAGE_UPDATE_TIMER:
                // update the timer with the supplied string
                mTimerText = millisToString(msg.arg1);
                if (mOptionsMenu != null) {
                    onPrepareOptionsMenu(mOptionsMenu);
                }
                break;
            case MazeThread.MESSAGE_DISMISS_DIALOG:
                if (mCongratulationsFragment != null)
                    mCongratulationsFragment.dismiss();
                break;
        }
    }

    private void mazeCompleted(int time, int mazeType) {
        // display congratulatory dialog
        mCongratulationsFragment = new MazeCompletedDialog();
        Bundle args = new Bundle();
        args.putInt(MAZE_COMPLETED_TIME_ID, time);
        mCongratulationsFragment.setArguments(args);
        mCongratulationsFragment.show(getFragmentManager(), "TAG_MAZE_COMPLETED");

        // Submit score to leaderboard
        if (isSignedIn()) {
            switch (mazeType) {
                case StartMenu.PERFECT_MAZE:
                    Games.Leaderboards.submitScore(getApiClient(),
                            getString(R.string.leaderboard_easy), time);
                    if (time < MINIMUM_TIME_TO_UNLOCK_NOVICE) {
                        Games.Achievements.unlock(getApiClient(),
                                getString(R.string.achievement_easy_maze_novice));
                    }
                    if (time < MINIMUM_TIME_TO_UNLOCK_MASTER) {
                        Games.Achievements.unlock(getApiClient(),
                                getString(R.string.achievement_easy_maze_master));
                    }
                    break;
                case StartMenu.GROWING_TREE_MAZE:
                    Games.Leaderboards.submitScore(getApiClient(),
                            getString(R.string.leaderboard_medium), time);
                    if (time < MINIMUM_TIME_TO_UNLOCK_NOVICE) {
                        Games.Achievements.unlock(getApiClient(),
                                getString(R.string.achievement_medium_maze_novice));
                    }
                    if (time < MINIMUM_TIME_TO_UNLOCK_MASTER) {
                        Games.Achievements.unlock(getApiClient(),
                                getString(R.string.achievement_medium_maze_master));
                    }
                    break;
                case StartMenu.DFS_MAZE:
                    Games.Leaderboards.submitScore(getApiClient(),
                            getString(R.string.leaderboard_hard), time);
                    if (time < MINIMUM_TIME_TO_UNLOCK_NOVICE) {
                        Games.Achievements.unlock(getApiClient(),
                                getString(R.string.achievement_hard_maze_novice));
                    }
                    if (time < MINIMUM_TIME_TO_UNLOCK_MASTER) {
                        Games.Achievements.unlock(getApiClient(),
                                getString(R.string.achievement_hard_maze_master));
                    }
                    break;
            }
        }

    }

    /**
     * Convert an integer representing time in milliseconds to a pretty string
     * format.
     */
    private String millisToString(int time) {
        int millis = (time % 1000) / 100;
        int second = (time / 1000) % 60;
        int minute = (time / (1000 * 60)) % 60;
        int hour = (time / (1000 * 60 * 60)) % 24;
        String string;

        if (hour > 0) {
            string = String.format(Locale.US, "%d:%02d:%02d.%d", hour, minute, second, millis);
        } else if (minute > 0) {
            string = String.format(Locale.US, "%d:%02d.%d", minute, second, millis);
        } else {
            string = String.format(Locale.US, "%d.%d", second, millis);
        }

        return string;
    }

    @Override
    public void onSignInFailed() {
        // ignore
    }

    @Override
    public void onSignInSucceeded() {
        // ignore
    }

}
