package com.TeamAmazing.drawing;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.TeamAmazing.Maze.GameOfLife;

public class GOLThread extends Thread {

	/** The delay in milliseconds between frame updates */
	private static final int FRAME_DELAY = 20;
	private long sleepTime;
	private long beforeTime;

	private GameOfLife gameOfLife;
	private final Object GOLLock = new Object();

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
	private volatile boolean stopped = true;

	private volatile boolean paused = false;

	public GOLThread(SurfaceHolder surfaceHolder) {
		mSurfaceHolder = surfaceHolder;
	}

	@Override
	public void start() {
		synchronized (mSurfaceHolder) {
			stopped = false;
			mSurfaceHolder.notify();
		}
		super.start();
	}

	public void halt() {
		synchronized (mSurfaceHolder) {
			stopped = true;
			mSurfaceHolder.notify();
		}
	}

	/**
	 * Pauses the update & animation.
	 */
	public void pause() {
		synchronized (mSurfaceHolder) {
			paused = true;
		}
	}

	/**
	 * Resumes from a pause.
	 */
	public void unpause() {
		synchronized (mSurfaceHolder) {
			paused = false;
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
		synchronized (GOLLock) {
			if (outState != null) {
				outState.putParcelable(GAME_OF_LIFE_ID, gameOfLife);
			}
		}
		return outState;
	}

	/**
	 * Restores state from the indicated Bundle. Typically called when the
	 * Activity is being restored after having been previously destroyed.
	 * 
	 * @param savedState
	 *            Bundle containing the state
	 */
	public synchronized void restoreState(Bundle savedState) {
		synchronized (GOLLock) {
			gameOfLife = (GameOfLife) savedState.getParcelable(GAME_OF_LIFE_ID);
		}
	}

	@Override
	public void run() {
		AnimationLoop: while (!stopped) {
			while (paused && !stopped) {
				try {
					synchronized (mSurfaceHolder) {
						mSurfaceHolder.wait();
					}

				} catch (InterruptedException ignore) {
				}
			}
			// Check if thread was stopped while it was paused.
			if (stopped)
				break AnimationLoop;

			beforeTime = System.nanoTime();
			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas();
				synchronized (GOLLock) {
					if (gameOfLife != null) {
						gameOfLife.drawAndUpdate(c);
					} else {
						pause();
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
			sleepTime = FRAME_DELAY
					- ((System.nanoTime() - beforeTime) / 1000000L);

			try {
				// actual sleep code
				if (sleepTime > 0 && !stopped && !paused) {
					synchronized (mSurfaceHolder) {
						mSurfaceHolder.wait(sleepTime);
					}
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(GOLThread.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}
	}

	/**
	 * Callback invoked when the surface dimensions change.
	 */
	public void setSurfaceSize(int width, int height) {
		// synchronized to make sure these all change atomically
		synchronized (GOLLock) {
			if (mCanvasWidth != width || mCanvasHeight != height) {
				mCanvasWidth = width;
				mCanvasHeight = height;
				// reset the GOL
				if (mCanvasWidth > 0 && mCanvasHeight > 0) {
					gameOfLife = new GameOfLife();
					gameOfLife.init(mCanvasWidth, mCanvasHeight);
				}
			}
		}
	}
}