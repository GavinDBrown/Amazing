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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MazeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    /** The thread that actually draws the animation */
    private MazeThread mMazeThread;
    SurfaceHolder mSurfaceHolder;

    // class constructors
    public MazeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // initialize our screen holder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setFocusable(true);
    }

    public MazeSurfaceView(Context context) {
        super(context);
        // initialize our screen holder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setFocusable(true);
    }

    /**
     * @param mazeThread The thread to draw into this MazeSurfaceView.
     */
    public void setThread(MazeThread mazeThread) {
        mMazeThread = mazeThread;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mMazeThread.handleTouchEvent(ev);
    }

    /** Callback invoked when the surface dimensions change. */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mMazeThread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mMazeThread.pause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMazeThread.unpause();
    }
}
