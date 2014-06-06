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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GolSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    /** The GolThread is created and destroyed with the StartMenu Activity. */
    private GolThread mGolThread;
    SurfaceHolder surfaceHolder;

    public GolSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Add our callback to the SurfaceHolder
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

    }

    public GolSurfaceView(Context context) {
        super(context);
        // Add our callback to the SurfaceHolder
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

    }

    /**
     * @param golThread The thread to draw into this GolSurfaceView.
     */
    public void setThread(GolThread golThread) {
        mGolThread = golThread;
    }

    /**
     * @return The GolThread that draws into this GolSurfaceView.
     */
    public GolThread getThread() {
        return mGolThread;
    }

    /**
     * Callback invoked when the surface dimensions change.
     * 
     * @see GolThread.setSurfaceSize
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mGolThread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mGolThread.pause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mGolThread.unpause();
    }

}
