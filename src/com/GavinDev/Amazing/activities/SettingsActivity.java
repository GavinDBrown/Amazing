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
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.GavinDev.Amazing.R;

public class SettingsActivity extends PreferenceActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Set the desired orientation
        if (prefs.getBoolean("pref_orientation", true)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        addPreferencesFromResource(R.xml.preferences);

        /**
         * When the preferred orientation is changed this clears the activity
         * stack then pushes the StartMenu onto the stack and then a new
         * instance of this Activity onto the stack. It then calls finish() to
         * exit this activity and pull the new instance of it (with the changed
         * orientation) from the stack.
         */
        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if ("pref_orientation".equals(key)) { // Orientation has changed
                    // Clear the activity stack and add the launch activity
                    Intent startMenuIntent = getApplicationContext().getPackageManager()
                            .getLaunchIntentForPackage(getApplicationContext().getPackageName());
                    startMenuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(startMenuIntent);
                    // add a new instance of this activity to the activity stack
                    // and finish this instance
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(prefsListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

}
