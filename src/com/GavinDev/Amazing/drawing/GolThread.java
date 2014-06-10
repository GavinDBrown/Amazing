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

package com.GavinDev.Amazing.drawing;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.GavinDev.Amazing.Maze.GameOfLife;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GolThread extends Thread {

    /** The minimum delay in milliseconds between frame updates */
    private static final int FRAME_DELAY = 20;

    private long mSleepTime;
    private long mBeforeTime;

    private GameOfLife mGameOfLife;
    private final Object mGolLock = new Object();

    /** String to identify GameOfLife object in bundle. */
    private static final String GAME_OF_LIFE_ID = "gameoflife";

    /**
     * Current height of the surface/canvas.
     * 
     * @see #setSurfaceSize
     */
    private volatile int mCanvasHeight;

    /**
     * Current width of the surface/canvas.
     * 
     * @see #setSurfaceSize
     */
    private volatile int mCanvasWidth;

    /** Handle to the surface manager object we interact with */
    private SurfaceHolder mSurfaceHolder;

    /**
     * Used to signal the thread whether it should be running or not. Passing
     * true allows the thread to run; passing false will shut it down if it's
     * already running.
     */
    private volatile boolean mStopped = true;

    private volatile boolean mPaused = true;

    private volatile boolean mRestarting = true;

    /** Time to wait after a game of life animation finishes. */
    private final long RESTART_DELAY = 5000L;

    public GolThread(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void start() {
        synchronized (mSurfaceHolder) {
            mStopped = false;
        }
        super.start();
    }

    /**
     * Stops this GolThread.
     */
    public void halt() {
        synchronized (mSurfaceHolder) {
            mStopped = true;
            mSurfaceHolder.notify();
        }
    }

    public void pause() {
        synchronized (mSurfaceHolder) {
            mPaused = true;
        }
    }

    public void unpause() {
        synchronized (mSurfaceHolder) {
            mPaused = false;
            mSurfaceHolder.notify();
        }
    }

    /**
     * Dump game state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     * 
     * @return Bundle with this view's state
     */
    public Bundle saveState(Bundle outState) {
        synchronized (mGolLock) {
            if (outState != null) {
                outState.putParcelable(GAME_OF_LIFE_ID, mGameOfLife);
            }
        }
        return outState;
    }

    /**
     * Restores state from the indicated Bundle. Typically called when the
     * Activity is being restored after having been previously destroyed.
     * 
     * @param savedState Bundle containing the state
     */
    public synchronized void restoreState(Bundle savedState) {
        synchronized (mGolLock) {
            mGameOfLife = (GameOfLife) savedState.getParcelable(GAME_OF_LIFE_ID);
        }
    }

    @Override
    public void run() {
        AnimationLoop: while (!mStopped) {
            while (mPaused && !mStopped) {
                try {
                    synchronized (mSurfaceHolder) {
                        mSurfaceHolder.wait();
                    }

                } catch (InterruptedException ignore) {
                }
            }
            while (mRestarting && !mStopped) {
                try {
                    synchronized (mSurfaceHolder) {
                        mSurfaceHolder.wait(RESTART_DELAY);
                        mRestarting = false;
                    }

                } catch (InterruptedException ignore) {
                }

            }

            // Check if thread was stopped while it was paused.
            if (mStopped)
                break AnimationLoop;

            mBeforeTime = System.nanoTime();
            Canvas c = null;
            try {
                c = mSurfaceHolder.lockCanvas();
                if (c == null) {
                    // Pause here so that our calls do not get throttled by the
                    // OS for calling lockCanvas too often.
                    pause();
                } else {
                    synchronized (mGolLock) {
                        if (mGameOfLife != null) {
                            mGameOfLife.drawAndUpdate(c);
                        } else {
                            pause();
                        }
                    }
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }

            // Sleep time. Time required to sleep to keep game consistent
            // This starts with the specified delay time (in milliseconds)
            // then subtracts from that the
            // actual time it took to update and render the game.
            mSleepTime = FRAME_DELAY - ((System.nanoTime() - mBeforeTime) / 1000000L);

            try {
                if (mSleepTime > 0 && !mStopped && !mPaused) {
                    synchronized (mSurfaceHolder) {
                        // Note: Spurious wakeups are okay here. It will just
                        // mean that this animation happens slightly faster.
                        mSurfaceHolder.wait(mSleepTime);
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GolThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Callback invoked when the surface dimensions change.
     */
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (mGolLock) {
            if (mCanvasWidth != width || mCanvasHeight != height) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                // reset the GOL as the dimensions have changed.
                if (mCanvasWidth > 0 && mCanvasHeight > 0) {
                    mGameOfLife = new GameOfLife(this);
                    mGameOfLife.init(mCanvasWidth, mCanvasHeight);
                }
            }
        }
    }

    public void GOLRestarting() {
        synchronized (mSurfaceHolder) {
            mRestarting = true;
        }
    }
}
