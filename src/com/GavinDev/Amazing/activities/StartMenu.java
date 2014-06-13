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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.activities.LeaderboardPickerDialog.LeaderboardPickerDialogCallback;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

public class StartMenu extends BaseGameActivity implements LeaderboardPickerDialogCallback,
        StartMenuFragmentGolEnabled.Listener, StartMenuFragmentGolDisabled.Listener {

    // Fragments
    StartMenuFragmentGolEnabled mStartMenuFragmentGolEnabled;
    StartMenuFragmentGolDisabled mStartMenuFragmentGolDisabled;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;
    final String TAG = "com.GavinDev.Amazing.activites.StartMenu";

    // Maze types
    public static final int PERFECT_MAZE = 0;
    public static final int DFS_MAZE = 1;
    public static final int GROWING_TREE_MAZE = 2;
    public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    /** Whether to show the leaderboard on successful sign-in */
    private boolean mShowLeaderboard = false;
    private boolean mShowAchievements = false;

    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);

        // create fragments
        mStartMenuFragmentGolEnabled = new StartMenuFragmentGolEnabled();
        mStartMenuFragmentGolDisabled = new StartMenuFragmentGolDisabled();

        // listen to fragment events
        mStartMenuFragmentGolEnabled.setListener(this);
        mStartMenuFragmentGolDisabled.setListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.

        // TODO re-enable orientation choice

        // Set the desired orientation
        // if (prefs.getBoolean("pref_orientation", true)) {
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        // } else {
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        // }

        // Check if the game of life background is enabled and add initial
        // fragment
        if (prefs.getBoolean("pref_start_background", true)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mStartMenuFragmentGolEnabled).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mStartMenuFragmentGolDisabled).commit();
        }

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if ("pref_start_background".equals(key)) {
                    if (prefs.getBoolean("pref_start_background", true)) {
                        // background is going from disabled -> enabled
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, mStartMenuFragmentGolEnabled)
                                .commitAllowingStateLoss();

                    } else {
                        // background is going from enabled -> disabled
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, mStartMenuFragmentGolDisabled)
                                .commitAllowingStateLoss();
                    }
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

    }

    // Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(prefsListener);
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

    @Override
    public void onStartMazeRequested(int mazeType) {
        Intent intent;
        switch (mazeType) {
            case PERFECT_MAZE:
                intent = new Intent(this, MazeGame.class);
                intent.putExtra(MAZE_TYPE, PERFECT_MAZE);
                break;
            case GROWING_TREE_MAZE:
                intent = new Intent(this, MazeGame.class);
                intent.putExtra(MAZE_TYPE, GROWING_TREE_MAZE);
                break;
            case DFS_MAZE:
                intent = new Intent(this, MazeGame.class);
                intent.putExtra(MAZE_TYPE, DFS_MAZE);
                break;
            default:
                intent = new Intent(this, MazeGame.class);
                intent.putExtra(MAZE_TYPE, PERFECT_MAZE);
                break;
        }
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
                getString(R.string.leaderboard_easy)), RC_UNUSED);
    }

    @Override
    public void displayMediumLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                getString(R.string.leaderboard_medium)), RC_UNUSED);
    }

    @Override
    public void displayHardLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                getString(R.string.leaderboard_hard)), RC_UNUSED);
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
