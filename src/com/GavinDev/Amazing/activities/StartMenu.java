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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.activities.LeaderboardPickerDialog.LeaderboardPickerDialogCallback;
import com.GavinDev.Amazing.drawing.GolSurfaceView;
import com.GavinDev.Amazing.drawing.GolThread;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

public class StartMenu extends BaseGameActivity implements LeaderboardPickerDialogCallback {

    public static final int PERFECT_MAZE = 0;
    public static final int DFS_MAZE = 1;
    public static final int GROWING_TREE_MAZE = 2;
    public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

    /** A handle to the thread that's running the Game Of Life animation. */
    private GolThread mGOLThread;

    /** A handle to the View in which the background is running. */
    private GolSurfaceView mGOLView;

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    /** Whether to show the leaderboard on successful sign-in */
    private boolean mShowLeaderboard = false;
    private boolean mShowAchievements = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Finish this activity if there is already a task running that starts
        // with this activity.
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Set the desired orientation
        if (prefs.getBoolean("pref_orientation", true)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        // Check if the game of life background is enabled
        if (prefs.getBoolean("pref_start_background", true)) {
            setContentView(R.layout.game_of_life_background);
            startGOLBackground();

        } else {
            // The game of life background is disabled
            setContentView(R.layout.start_menu_background);
        }

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if ("pref_start_background".equals(key)) {
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
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

    }

    private void startGOLBackground() {
        // get handles to the GOLView and it's GOLThread
        mGOLView = (GolSurfaceView) findViewById(R.id.game_of_life_background);
        mGOLThread = new GolThread(mGOLView.getHolder());
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
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_start_background",
                true)) {
            mGOLThread.saveState(outState);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Check if the GOLBackground is enabled
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_start_background",
                true))
            mGOLThread.restoreState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_start_background",
                true)) {
            mGOLThread.unpause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_start_background",
                true)) {
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

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_start_background",
                true)) {
            stopGOLBackground();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.start_menu_activity_actions, menu);

        // Change visibility of buttons based on sign in status
        if (isSignedIn()) {
            menu.findItem(R.id.sign_in_button).setVisible(false);
        } else {
            menu.findItem(R.id.sign_out_button).setVisible(false);
        }
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
            case R.id.sign_in_button:
                // start the asynchronous sign in flow
                beginUserInitiatedSignIn();
                return true;
            case R.id.sign_out_button:
                signOut();
                // change the visibility of the sign in buttons.
                invalidateOptionsMenu();
                return true;
            case R.id.leaderboards:
                if (isSignedIn()) {
                    // display dialog asking for which leaderboard to show
                    new LeaderboardPickerDialog()
                            .show(getFragmentManager(), "TAG_PICK_LEADERBOARD");
                } else {
                    // ask user to sign in and then display the dialog asking
                    // which leaderboard to show.
                    beginUserInitiatedSignIn();
                    mShowLeaderboard = true;
                }
                return true;
            case R.id.achievements:
                if (isSignedIn()) {
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(getApiClient()), 0);
                } else {
                    // ask user to sign in and then display the dialog asking
                    // which leaderboard to show.
                    beginUserInitiatedSignIn();
                    mShowAchievements = true;
                }
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

    public void startMediumMaze(View v) {
        Intent intent = new Intent(this, MazeGame.class);
        intent.putExtra(MAZE_TYPE, GROWING_TREE_MAZE);
        startActivity(intent);
    }

    @Override
    public void onSignInFailed() {
        // Sign in has failed. So show the user the sign-in button.
        invalidateOptionsMenu();

        if (mShowLeaderboard || mShowAchievements) {
            // Show dialog telling user they need to sign-in to see
            // leaderboards
            new SignInRequiredDialog().show(getFragmentManager(), "TAG_SIGN_IN_REQUIRED");

        }
        mShowLeaderboard = false;
        mShowAchievements = false;
    }

    @Override
    public void onSignInSucceeded() {
        // show sign-out button, hide the sign-in button
        invalidateOptionsMenu();

        // TODO update UI, enable functionality that depends on
        // sign in, etc

        if (mShowLeaderboard) {
            mShowLeaderboard = false;
            // Display leaderboard picker dialog
            new LeaderboardPickerDialog().show(getFragmentManager(), "TAG_PICK_LEADERBOARD");

        }
        if (mShowAchievements) {
            mShowAchievements = false;
            // Display achievements
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 0);
        }

    }

    @Override
    public void displayEasyLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                getString(R.string.leaderboard_easy)), 0);
    }

    @Override
    public void displayMediumLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                getString(R.string.leaderboard_medium)), 0);
    }

    @Override
    public void displayHardLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                getString(R.string.leaderboard_hard)), 0);
    }

    public static class SignInRequiredDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.sign_in_required).setNegativeButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
