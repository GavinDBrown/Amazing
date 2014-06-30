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

package com.GavinDev.Amazing.UI;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.UI.LeaderboardPickerDialog.LeaderboardPickerDialogCallback;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

public class GameActivity extends Activity implements LeaderboardPickerDialogCallback,
        StartMenuFragment.Callback, MazeGameFragment.Callback,
        MazeCompletedDialog.MazeCompletedDialogCallback {

    // Fragments
    private StartMenuFragment mStartMenuFragment;
    private MazeGameFragment mMazeGameFragment;
    private PreferencesFragment mPreferencesFragment;

    private GameHelper mGameServicesHelper;

    public static final String MAZE_COMPLETED_TIME_ID = "mazeCompletedTime";

    final String TAG = "com.GavinDev.Amazing.activites.StartMenu";

    // Maze types
    public static final int PERFECT_MAZE = 0;
    public static final int DFS_MAZE = 1;
    public static final int GROWING_TREE_MAZE = 2;
    public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

    // private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    /** Whether to show the leaderboard on successful sign-in */
    private boolean mShowLeaderboard = false;
    private boolean mShowAchievements = false;

    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    public GameHelper getGameServicesHelper() {
        return mGameServicesHelper;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_activity);

        // create game helper with games API:
        mGameServicesHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);

        GameHelperListener gameHelperListener = new GameHelper.GameHelperListener() {
            @Override
            public void onSignInSucceeded() {
                // show sign-out button, hide the sign-in button
                invalidateOptionsMenu();

                if (mShowLeaderboard) {
                    mShowLeaderboard = false;
                    // Display leaderboard picker dialog
                    new LeaderboardPickerDialog()
                            .show(getFragmentManager(), "TAG_PICK_LEADERBOARD");

                }
                if (mShowAchievements) {
                    mShowAchievements = false;
                    // Display achievements
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(mGameServicesHelper
                                    .getApiClient()), 0);
                }

            }

            @Override
            public void onSignInFailed() {
                // Sign in has failed. So show the user the sign-in button.
                invalidateOptionsMenu();

                if (mShowLeaderboard || mShowAchievements) {
                    // Show tpast telling user they need to sign-in
                    Toast t = Toast.makeText(getApplicationContext(), R.string.sign_in_required,
                            Toast.LENGTH_SHORT);
                    t.show();
                }
                mShowLeaderboard = false;
                mShowAchievements = false;

                // return focus to the main view
                // TODO create and set a view for popups so this isn't needed.
                findViewById(R.id.fragment_container).requestFocus();
            }

        };
        mGameServicesHelper.setup(gameHelperListener);

        if (savedInstanceState == null) {
            // create fragments
            mStartMenuFragment = new StartMenuFragment();
            mPreferencesFragment = new PreferencesFragment();
            mMazeGameFragment = new MazeGameFragment();
            // Add the appropriate StartMenu Fragment
            onStartMenu();
        }

        mStartMenuFragment.setCallback(this);

    }

    // Switch UI to the given fragment
    private void switchToFragment(Fragment newFrag) {
        FragmentManager fm = getFragmentManager();

        if (newFrag instanceof StartMenuFragment) {
            // Switching to StartMenuFragment; don't add this transaction to the
            // backstack
            fm.beginTransaction().replace(R.id.fragment_container, newFrag).commit();
        } else {
            // Switching from StartMenuFragment; add this transaction to
            // the backstack.
            fm.beginTransaction()
                    // .setCustomAnimations(R.animator.slide_in_right,
                    // R.animator.slide_out_left,
                    // R.animator.slide_in_left, R.animator.slide_out_right)
                    .replace(R.id.fragment_container, newFrag).addToBackStack("StartMenuFragment")
                    .commit();
        }
    }

    /**
     * Navigates back to the StartMenu screen or the top fragment on the
     * backstack.
     */
    @Override
    public void onStartMenu() {
        // pop a Fragment off of backstack if it's there
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            // otherwise switch to the StartMenuFragment
            switchToFragment(mStartMenuFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGameServicesHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGameServicesHelper.onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mGameServicesHelper.onActivityResult(request, response, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.start_menu_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Change visibility of buttons based on if mMazeGameFragment is being
        // shown.
        if (mMazeGameFragment.isVisible()) {
            // make some of the action activities invisible
            menu.findItem(R.id.sign_in_button).setVisible(false);
            menu.findItem(R.id.sign_out_button).setVisible(false);
            menu.findItem(R.id.leaderboards).setVisible(false);
            menu.findItem(R.id.achievements).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
        } else {// make them visible again when the fragment is not visible

            if (mGameServicesHelper.isSignedIn()) {// Change visibility of
                                                   // buttons based on sign in
                                                   // status

                menu.findItem(R.id.sign_in_button).setVisible(false);
                menu.findItem(R.id.sign_out_button).setVisible(true);
            } else {
                menu.findItem(R.id.sign_out_button).setVisible(false);
                menu.findItem(R.id.sign_in_button).setVisible(true);
            }
            menu.findItem(R.id.leaderboards).setVisible(true);
            menu.findItem(R.id.achievements).setVisible(true);
            menu.findItem(R.id.settings).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.settings:
                switchToFragment(mPreferencesFragment);
                return true;
            case R.id.sign_in_button:
                // start the asynchronous sign in flow
                mGameServicesHelper.beginUserInitiatedSignIn();
                return true;
            case R.id.sign_out_button:
                mGameServicesHelper.signOut();
                // change the visibility of the sign in buttons.
                invalidateOptionsMenu();
                return true;
            case R.id.leaderboards:
                if (mGameServicesHelper.isSignedIn()) {
                    // display dialog asking for which leaderboard to show
                    new LeaderboardPickerDialog()
                            .show(getFragmentManager(), "TAG_PICK_LEADERBOARD");
                } else {
                    // ask user to sign in and then display the dialog asking
                    // which leaderboard to show.
                    mGameServicesHelper.beginUserInitiatedSignIn();
                    mShowLeaderboard = true;
                }
                return true;
            case R.id.achievements:
                if (mGameServicesHelper.isSignedIn()) {
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(mGameServicesHelper
                                    .getApiClient()), 0);
                } else {
                    // ask user to sign in and then display the dialog asking
                    // which leaderboard to show.
                    mGameServicesHelper.beginUserInitiatedSignIn();
                    mShowAchievements = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStartMazeRequested(int mazeType) {
        switch (mazeType) {
            case PERFECT_MAZE:
                mMazeGameFragment = MazeGameFragment.newInstance(PERFECT_MAZE);
                switchToFragment(mMazeGameFragment);
                break;
            case GROWING_TREE_MAZE:
                mMazeGameFragment = MazeGameFragment.newInstance(GROWING_TREE_MAZE);
                switchToFragment(mMazeGameFragment);
                break;
            case DFS_MAZE:
                mMazeGameFragment = MazeGameFragment.newInstance(DFS_MAZE);
                switchToFragment(mMazeGameFragment);
                break;
        }

    }

    @Override
    public void displayEasyLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                mGameServicesHelper.getApiClient(), getString(R.string.leaderboard_easy)),
                RC_UNUSED);
    }

    @Override
    public void displayMediumLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                mGameServicesHelper.getApiClient(), getString(R.string.leaderboard_medium)),
                RC_UNUSED);
    }

    @Override
    public void displayHardLeaderboard() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                mGameServicesHelper.getApiClient(), getString(R.string.leaderboard_hard)),
                RC_UNUSED);
    }

    @Override
    public void onReset() {
        mMazeGameFragment.reset();
    }

    @Override
    public void onScores(int mazeType) {
        if (!mGameServicesHelper.isSignedIn()) {
            mGameServicesHelper.beginUserInitiatedSignIn();
            mShowLeaderboard = true;
        } else {
            switch (mazeType) {
                case GameActivity.PERFECT_MAZE:
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                            mGameServicesHelper.getApiClient(),
                            getString(R.string.leaderboard_easy)), 0);
                    break;
                case GameActivity.GROWING_TREE_MAZE:
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                            mGameServicesHelper.getApiClient(),
                            getString(R.string.leaderboard_medium)), 0);
                    break;
                case GameActivity.DFS_MAZE:
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                            mGameServicesHelper.getApiClient(),
                            getString(R.string.leaderboard_hard)), 0);
                    break;
            }
        }
    }

}
