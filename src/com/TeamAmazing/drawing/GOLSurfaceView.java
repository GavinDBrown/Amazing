//    Amazing, the maze game.
//    Copyright (C) 2014  Gavin Brown
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
