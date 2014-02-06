package com.TeamAmazing.drawing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MazeSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {
	/** The thread that actually draws the animation */
	private MazeThread mThread;
	SurfaceHolder mSurfaceHolder;

	// class constructor
	public MazeSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// initialize our screen holder
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		setFocusable(true);
	}

	public void setThread(MazeThread t) {
		mThread = t;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return mThread.handleTouchEvent(ev);
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (hasWindowFocus) {
			mThread.unpause();
		} else {
			mThread.pause();
		}
	}

	/** Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mThread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mThread.pause();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mThread.unpause();
	}
}
