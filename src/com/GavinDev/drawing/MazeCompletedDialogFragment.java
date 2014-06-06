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

package com.GavinDev.drawing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import com.GavinDev.Amazing.R;
import com.GavinDev.activities.MazeGame;

import java.util.Locale;

public class MazeCompletedDialogFragment extends DialogFragment {
    OnDialogButtonPressedCallback mCallback;

    // Interface the container activity must implement
    public interface OnDialogButtonPressedCallback {
        public void onReset();

        public void onStartMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnDialogButtonPressedCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogButtonPressedCallback");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int time = args.getInt(MazeGame.MAZE_COMPLETED_TIME_ID);
        String message = mMessageFormater(time);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                AlertDialog.THEME_HOLO_DARK);

        builder.setMessage(message);

        builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked reset button
                mCallback.onReset();
            }
        });
        builder.setNegativeButton(R.string.menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mCallback.onStartMenu();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String mMessageFormater(int time) {
        int millis = (time % 1000) / 100;
        int second = (time / 1000) % 60;
        int minute = (time / (1000 * 60)) % 60;
        int hour = (time / (1000 * 60 * 60)) % 24;
        String message;
        Resources res = getResources();

        if (hour > 0) {
            message = res.getString(R.string.maze_finished_hour);
        } else if (minute > 1) {
            message = String.format(Locale.US, res.getString(R.string.maze_finished_minutes),
                    minute, second, millis);
        } else if (minute > 0) {
            message = String.format(Locale.US, res.getString(R.string.maze_finished_minute),
                    second, millis);
        } else {
            message = String.format(Locale.US, res.getString(R.string.maze_finished_seconds),
                    second, millis);
        }

        return message;
    }

}
