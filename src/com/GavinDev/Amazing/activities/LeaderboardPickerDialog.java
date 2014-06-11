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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.GavinDev.Amazing.R;

public class LeaderboardPickerDialog extends DialogFragment {
    LeaderboardPickerDialogCallback mCallback;

    // Interface the container activity must implement
    public interface LeaderboardPickerDialogCallback {
        public void displayEasyLeaderboard();

        public void displayMediumLeaderboard();

        public void displayHardLeaderboard();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (LeaderboardPickerDialogCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogButtonPressedCallback");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                AlertDialog.THEME_HOLO_DARK);

        builder.setMessage(getString(R.string.leaderboard_picker_dialog));

        builder.setNegativeButton(R.string.easy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mCallback.displayEasyLeaderboard();
            }
        });
        builder.setNeutralButton(R.string.medium, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mCallback.displayMediumLeaderboard();
            }
        });
        builder.setPositiveButton(R.string.hard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mCallback.displayHardLeaderboard();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
