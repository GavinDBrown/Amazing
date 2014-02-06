package com.TeamAmazing.drawing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GOLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	/** The thread that actually draws the animation */
	private GOLThread thread;
	SurfaceHolder surfaceHolder;

	// class constructor
	public GOLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// initialize our screen holder
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

	}
	public void setThread(GOLThread t){
		thread = t;
	}

	/**
	 * Fetches the animation thread corresponding to this GOLView.
	 * 
	 * @return the animation thread
	 */
	public GOLThread getThread() {
		return thread;
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (hasWindowFocus){
			thread.unpause();
		} else {
			thread.pause();
		}
		
	}

	/** Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.pause();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.unpause();
	}
	
}
