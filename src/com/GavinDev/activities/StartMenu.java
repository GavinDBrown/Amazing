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

package com.GavinDev.activities;

import android.app.Activity;
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
import com.GavinDev.drawing.GolSurfaceView;
import com.GavinDev.drawing.GolThread;

public class StartMenu extends Activity {

    public static final int PERFECT_MAZE = 0;
    public static final int DFS_MAZE = 1;
    public static final int GROWING_TREE_MAZE = 2;
    public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

    /** A handle to the thread that's running the Game Of Life animation. */
    private GolThread mGOLThread;

    /** A handle to the View in which the background is running. */
    private GolSurfaceView mGOLView;

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

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

    public void startMediumMaze(View v) {
        Intent intent = new Intent(this, MazeGame.class);
        intent.putExtra(MAZE_TYPE, GROWING_TREE_MAZE);
        startActivity(intent);
    }

}
