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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.drawing.GolSurfaceView;
import com.GavinDev.Amazing.drawing.GolThread;

/**
 * Fragment with the main menu for the game. The main menu allows the player to
 * choose a gameplay mode (Easy, Medium or Hard), and displays the Game of Life
 * animation in the background.
 */
public class StartMenuFragmentGolEnabled extends Fragment implements OnClickListener {
    public interface Listener {
        public void onStartMazeRequested(int mazeType);
    }

    GolSurfaceView mGolSurfaceView;
    GolThread mGolThread;

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.start_menu_gol_enabled, container, false);

        mGolSurfaceView = (GolSurfaceView) v.findViewById(R.id.start_menu_gol_enabled);
        final int[] CLICKABLES = new int[] {
                R.id.start_kruskals_maze, R.id.start_medium_maze,
                R.id.start_recursive_backtracker_maze
        };
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        mGolThread = new GolThread(mGolSurfaceView.getHolder());
        mGolSurfaceView.setThread(mGolThread);
        mGolThread.start();
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_kruskals_maze:
                mListener.onStartMazeRequested(StartMenu.PERFECT_MAZE);
                break;
            case R.id.start_medium_maze:
                mListener.onStartMazeRequested(StartMenu.GROWING_TREE_MAZE);
                break;
            case R.id.start_recursive_backtracker_maze:
                mListener.onStartMazeRequested(StartMenu.DFS_MAZE);
                break;

        }
    }

    @Override
    public void onDestroyView() {
        if (mGolThread != null) {
            mGolThread.halt(); // stop the animation if it's valid
            boolean retry = true;
            while (retry) {
                try {
                    mGolThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
            mGolThread = null;
        }
        super.onDestroyView();
    }
}
