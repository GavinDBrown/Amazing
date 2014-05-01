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

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.TeamAmazing.Maze.Cell;
import com.TeamAmazing.Maze.Maze;
import com.TeamAmazing.Maze.Wall;
import com.TeamAmazing.activities.StartMenu;
import com.TeamAmazing.game.R;

public class MazeThread extends Thread {

	// touch event variables
	private volatile float xTouch;
	private volatile float yTouch;
	private volatile boolean isAccelerating;

	// The size of the maze
	private int cellsPerRow = 12;
	private int cellsPerColumn = 15;

	// Pixel sizes of objects and their default values
	public int cellWidth = 60;
	public int cellHeight = 60;
	public int wallWidth = 5;
	public int boundaryWidth = 20;
	public int boundaryHeight = 20;
	private int ufoWidth = 35;
	private int ufoHeight = 18;
	private static final double UFO_ASPECT_RATIO = 35.0 / 18.0;

	// ufo variables
	private static final float PREVIOUS_VELOCITY_FAC = .49f;
	private static final float TOUCH_FACTOR = .075f;
	private static final float FRICTION = .05f;
	private static final String UFO_X_VELOCITY_ID = "ufoxvelocity";
	private float ufoXVelocity = 0;
	private static final String UFO_Y_VELOCITY_ID = "ufoyvelocity";
	private float ufoYVelocity = 0;
	private static final String X_FRICTION_ID = "xfriction";
	private float xFriction = 0;
	private static final String Y_FRICTION_ID = "yfriction";
	private float yFriction = 0;
	private static final int MAX_SPEED = 10;
	private static final float REBOUND_FAC = .5f;
	private static final String UFO_ID = "ufo";
	private Point ufo;
	private Bitmap ufoBM;

	// maze variables
	private static final String MAZE_ID = "maze";
	private Maze maze;
	private volatile int mazeType = StartMenu.PERFECT_MAZE;
	private Rect endRect;
	private Rect startRect;

	/** @see setSurfaceSize */
	private volatile int mCanvasHeight;
	private volatile int mCanvasWidth;

	// Handles to important objects
	private SurfaceHolder mSurfaceHolder;
	private Handler uiHandler;
	private Context mContext;

	public static final int MESSAGE_MAZE_COMPLETED = 1;
	public static final int MESSAGE_UPDATE_TIMER = 2;

	// Thread states
	private volatile int mState = STATE_STOPPED;
	private static final int STATE_STOPPED = 0;
	private static final int STATE_RUNNING = 1;
	private static final int STATE_PAUSED = 2;
	private static final int STATE_WAIT_FOR_DIALOG = 3;
	private static final int STATE_RESET_AFTER_MEASURE = 4;

	private Paint p;

	// star variables
	private static final int NUM_OF_STARS = 25;
	private Point[] starField;
	private static final String STARFIELD_ID = "starfield";
	private int starAlpha = 80;
	private int starFade = 2;
	private Random rand;

	// Timing variables
	private static final String TIME_ELAPSED_ID = "timeElapsed";
	private int timeElapsed = 0;
	private long timeStart;

	public MazeThread(SurfaceHolder surfaceHolder, Context context,
			Handler uiHandler, int mazeType) {
		mSurfaceHolder = surfaceHolder;
		this.uiHandler = uiHandler;
		this.mazeType = mazeType;
		mContext = context;

		p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setAlpha(255);
		p.setStrokeWidth(1);

		rand = new Random();
		starField = new Point[NUM_OF_STARS];

		ufo = new Point();

		resetMaze();
	}

	public void halt() {
		synchronized (mSurfaceHolder) {
			mState = STATE_STOPPED;
			mSurfaceHolder.notify();
		}
	}

	/**
	 * Pauses the update & animation if running.
	 */
	public void pause() {
		synchronized (mSurfaceHolder) {
			if (mState == STATE_RUNNING)
				mState = STATE_PAUSED;
		}
	}

	/**
	 * Resumes from a pause.
	 */
	public void unpause() {
		synchronized (mSurfaceHolder) {
			if (mState == STATE_PAUSED)
				mState = STATE_RUNNING;
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
		synchronized (mSurfaceHolder) {
			if (outState != null) {
				outState.putParcelable(MAZE_ID, maze);
				outState.putParcelable(UFO_ID, ufo);
				outState.putFloat(UFO_X_VELOCITY_ID, ufoXVelocity);
				outState.putFloat(UFO_Y_VELOCITY_ID, ufoYVelocity);
				outState.putFloat(X_FRICTION_ID, xFriction);
				outState.putFloat(Y_FRICTION_ID, yFriction);
				outState.putInt(TIME_ELAPSED_ID, timeElapsed);
				outState.putParcelableArray(STARFIELD_ID, starField);
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
		synchronized (mSurfaceHolder) {
			maze = savedState.getParcelable(MAZE_ID);
			mState = STATE_RUNNING;
			ufo = savedState.getParcelable(UFO_ID);
			ufoXVelocity = savedState.getFloat(UFO_X_VELOCITY_ID);
			ufoYVelocity = savedState.getFloat(UFO_Y_VELOCITY_ID);
			xFriction = savedState.getFloat(X_FRICTION_ID);
			yFriction = savedState.getFloat(Y_FRICTION_ID);
			timeElapsed = savedState.getInt(TIME_ELAPSED_ID);
			starField = (Point[]) savedState.getParcelableArray(STARFIELD_ID);
		}
	}

	@Override
	public void run() {
		while (mState != STATE_STOPPED) {
			while (mState != STATE_RUNNING && mState != STATE_STOPPED) {
				try {
					synchronized (mSurfaceHolder) {
						mSurfaceHolder.wait();
					}

				} catch (InterruptedException ignore) {
				}
			}
			// Check if thread was stopped while it was paused.
			if (mState == STATE_STOPPED)
				break;

			timeStart = System.currentTimeMillis();

			synchronized (mSurfaceHolder) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas();
					if (c == null) {
						// Pause here so that our calls do not get throttled for
						// calling lockCanvas() too often.
						pause();
					} else {

						// Update velocity based on touch
						// information.
						updateVelocity();

						// Update position with boundary checking
						updatePosition();

						// draw to the canvas
						mDraw(c);
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}

			if (mState != STATE_WAIT_FOR_DIALOG) {
				timeElapsed = timeElapsed
						+ (int) (System.currentTimeMillis() - timeStart);
				uiHandler.dispatchMessage(uiHandler.obtainMessage(
						MESSAGE_UPDATE_TIMER, timeElapsed, 0, null));
			}

		}
	}

	private void mDraw(Canvas canvas) {
		// draw the background
		p.setColor(Color.BLACK);
		canvas.drawRect(0, 0, mCanvasWidth, mCanvasHeight, p);

		// draw the stars
		p.setColor(Color.CYAN);
		p.setAlpha(starAlpha += starFade);
		if (starAlpha >= 252 || starAlpha <= 80)
			starFade = starFade * -1;
		p.setStrokeWidth(5);
		for (int i = 0; i < NUM_OF_STARS; i++) {
			canvas.drawPoint(starField[i].x, starField[i].y, p);
		}

		// draw the maze
		p.setColor(Color.MAGENTA);
		for (Wall w : maze.getWalls()) {
			canvas.drawRect(w.getBounds(), p);
		}

		// Draw the end cell.
		p.setColor(Color.RED);
		p.setAlpha(150);
		canvas.drawRect(endRect, p);

		// Draw the ufo.
		canvas.drawBitmap(ufoBM, ufo.x - ufoWidth / 2, ufo.y - ufoHeight / 2,
				null);

	}

	private void initializeStars() {
		for (int i = 0; i < NUM_OF_STARS; i++) {
			int x = rand.nextInt(mCanvasWidth) + cellWidth / 2;
			int y = rand.nextInt(mCanvasHeight) + cellHeight / 2;
			starField[i] = (new Point(x, y));
		}
	}

	/**
	 * Update the position of the objects
	 */
	private void updatePosition() {
		Point vel = new Point(Math.round(ufoXVelocity),
				Math.round(ufoYVelocity));
		while (Math.abs(vel.x) > 0 || Math.abs(vel.y) > 0) {
			if (Math.abs(vel.x) > Math.abs(vel.y) && vel.y != 0) {
				takeNStepsInXDirection(vel, Math.abs(vel.x / vel.y));
				takeNStepsInYDirection(vel, 1);
			} else if (Math.abs(vel.y) > Math.abs(vel.x) && vel.x != 0) {
				takeNStepsInYDirection(vel, Math.abs(vel.y / vel.x));
				takeNStepsInXDirection(vel, 1);
			} else {
				if (Math.abs(vel.x) > 0) {
					takeNStepsInXDirection(vel, 1);
				}
				if (Math.abs(vel.y) > 0) {
					takeNStepsInYDirection(vel, 1);
				}
			}
		}

		// Check if we are in the end cell.
		if (endRect.contains(ufo.x, ufo.y)) {
			// Sprite is inside the end cell.
			mazeCompleted();
		}
	}

	private void mazeCompleted() {
		// Send a message to the UI thread
		uiHandler.dispatchMessage(uiHandler.obtainMessage(
				MESSAGE_MAZE_COMPLETED, timeElapsed, 0, null));
		synchronized (mSurfaceHolder) {
			mState = STATE_WAIT_FOR_DIALOG;
		}
	}

	/** Called from UI thread */
	public void resetMaze() {
		synchronized (mSurfaceHolder) {
			maze = new Maze(cellsPerRow, cellsPerColumn, mazeType);
			if (mCanvasHeight != 0 && mCanvasWidth != 0) {
				ufoXVelocity = 0;
				ufoYVelocity = 0;
				xFriction = 0;
				yFriction = 0;
				timeElapsed = 0;
				initializeStars();
				calculateGFXSizes();
				ufo.x = startRect.centerX();
				ufo.y = startRect.centerY();
				mState = STATE_RUNNING;
				mSurfaceHolder.notify();
			} else {
				mState = STATE_RESET_AFTER_MEASURE;
			}
		}
	}

	/**
	 * Calculates the pixel sizes of the maze related objects.
	 */
	public void calculateGFXSizes() {
		synchronized (mSurfaceHolder) {
			if (mCanvasWidth == 0 | mCanvasHeight == 0)
				return;

			// Calculate pixel sizes of maze and ufo
			// wallWidth is calculated to be approximately 1/12th of
			// cellWidth/Height, whichever is smaller
			cellWidth = (int) (mCanvasWidth / (cellsPerRow + (cellsPerRow + 1) / 12.0));
			cellHeight = (int) (mCanvasHeight / (cellsPerColumn + (cellsPerColumn + 1) / 12.0));
			wallWidth = cellWidth > cellHeight ? cellHeight / 12
					: cellWidth / 12;
			boundaryWidth = (mCanvasWidth - cellWidth * cellsPerRow - wallWidth
					* (cellsPerRow + 1)) / 2;
			boundaryHeight = (mCanvasHeight - cellHeight * cellsPerColumn - wallWidth
					* (cellsPerColumn + 1)) / 2;

			// Calculate ufo size to be approximately 2/3rds of cellWidth or
			// cellHeight, whichever makes a smaller ufo.
			if (2 * cellWidth / 3
					+ (int) Math.round((2 * cellWidth / 3) / UFO_ASPECT_RATIO) < 2
					* cellHeight
					/ 3
					+ (int) Math.round((2 * cellHeight / 3) * UFO_ASPECT_RATIO)) {
				ufoWidth = 2 * cellWidth / 3;
				ufoHeight = (int) Math.round(ufoWidth / UFO_ASPECT_RATIO);
			} else {
				ufoHeight = 2 * cellHeight / 3;
				ufoWidth = (int) Math.round(ufoHeight * UFO_ASPECT_RATIO);
			}

			// resize the ufo bitmap
			ufoBM = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.ufo), ufoWidth,
					ufoHeight, false);

			// get bounds for start and end rectangles
			endRect = calculateCellRect(maze.getCell(Cell.END_CELL));
			startRect = calculateCellRect(maze.getCell(Cell.START_CELL));

			// Calculate the bounds for each wall.
			// Each corner will have two indistinguishable walls that need
			// have
			// different orientations so set a boolean flag for each corner.
			// topLeft, topRight, bottomLeft, bottomRight
			boolean[] corners = { false, false, false, false };
			for (Wall w : maze.getWalls()) {
				Cell cell1 = w.getV1();
				Cell cell2 = w.getV2();
				if (cell1 != null && cell2 != null) {
					// Cells on the inside.
					if (cell1.getCoords().y == cell2.getCoords().y) {
						// Vertical inside cells => horizontal wall below
						// cell1.
						setWallBoundsBelowCell(w, cell1);
					} else {
						// Horizontal inside cells => vertical wall to the
						// right
						// of
						// cell1.
						setWallBoundsRightCell(w, cell1);
					}
				} else {
					// Cells on the boundary
					if (cell1 != null) {
						setWallBoundsBoundaryCell(w, cell1, corners,
								maze.getWidth(), maze.getHeight());
					} else {
						// The first cell of a wall should never be null,
						// but
						// this is included for future usability.
						setWallBoundsBoundaryCell(w, cell2, corners,
								maze.getWidth(), maze.getHeight());
					}
				}
			}
		}
		return;
	}

	/**
	 * Callback invoked when the surface dimensions change.
	 */
	public void setSurfaceSize(int width, int height) {
		// synchronized to make sure these all change atomically
		synchronized (mSurfaceHolder) {
			if (mCanvasWidth != width || mCanvasHeight != height) {
				mCanvasWidth = width;
				mCanvasHeight = height;
				calculateGFXSizes();
				if (mCanvasWidth > 0 && mCanvasHeight > 0
						&& mState == STATE_RESET_AFTER_MEASURE) {
					resetMaze();
				}
			}
		}
	}

	/** Called by the UI thread when there is a touch event. */
	public boolean handleTouchEvent(MotionEvent ev) {
		synchronized (mSurfaceHolder) {
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				xTouch = ev.getX();
				yTouch = ev.getY();
				isAccelerating = true;
				break;
			case MotionEvent.ACTION_UP:
				isAccelerating = false;
				break;
			case MotionEvent.ACTION_MOVE:
				xTouch = ev.getX();
				yTouch = ev.getY();
				isAccelerating = true;
				break;
			}
			return true;
		}
	}

	/**
	 * Update the velocity of the ufo.
	 */
	private void updateVelocity() {
		if (isAccelerating) {
			ufoXVelocity = TOUCH_FACTOR
					* (xTouch - ufo.x + Math.round(PREVIOUS_VELOCITY_FAC
							* ufoXVelocity));
			ufoYVelocity = TOUCH_FACTOR
					* (yTouch - ufo.y + Math.round(PREVIOUS_VELOCITY_FAC
							* ufoYVelocity));
			// Enforce max speed;
			int accSpeed = (int) Math.round(Math.sqrt(Math.pow(ufoXVelocity, 2)
					+ Math.pow(ufoYVelocity, 2)));
			if (accSpeed > MAX_SPEED + 1) {
				ufoXVelocity = ufoXVelocity * MAX_SPEED / accSpeed;
				ufoYVelocity = ufoYVelocity * MAX_SPEED / accSpeed;
			}
		} else {
			// Decrease speed with friction.
			float speed = (float) Math.sqrt(Math.pow(ufoXVelocity, 2)
					+ Math.pow(ufoYVelocity, 2));
			if ((Math.abs(ufoXVelocity) + Math.abs(ufoYVelocity)) > 0) {
				xFriction = speed * FRICTION * -1 * ufoXVelocity
						/ (Math.abs(ufoXVelocity) + Math.abs(ufoYVelocity));
				yFriction = speed * FRICTION * -1 * ufoYVelocity
						/ (Math.abs(ufoXVelocity) + Math.abs(ufoYVelocity));
			}
			ufoXVelocity = ufoXVelocity + xFriction;
			ufoYVelocity = ufoYVelocity + yFriction;
		}
	}

	private boolean wallsIntersects(int left, int top, int right, int bottom) {
		for (Wall w : maze.getWalls()) {
			if (w.getBounds().intersects(left, top, right, bottom)) {
				// get the bounds of the intersection
				Rect intersection = new Rect();
				intersection.setIntersect(w.getBounds(), new Rect(left, top,
						right, bottom));
				for (int x = intersection.left; x < intersection.right; x++) {
					for (int y = intersection.top; y < intersection.bottom; y++) {
						if (ufoBM.getPixel(x - left, y - top) != Color.TRANSPARENT) {
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	private void takeNStepsInYDirection(Point vel, int n) {
		while (n > 0) {
			// Take a steps along the yVel vector, making decisions as we
			// go.
			if (vel.y > 0) {
				if (ufo.y + 1 > mCanvasHeight - ufoHeight / 2
						|| wallsIntersects(ufo.x - ufoWidth / 2, ufo.y + 1
								- ufoHeight / 2, ufo.x + ufoWidth / 2, ufo.y
								+ 1 + ufoHeight / 2)) {
					// Rebound
					ufo.y -= 1;
					vel.y *= -1 * REBOUND_FAC;
					ufoYVelocity *= -1 * REBOUND_FAC;
					yFriction *= -1 * REBOUND_FAC;
				} else {
					ufo.y += 1;
				}
				vel.y--;
			} else {
				if (ufo.y - 1 < ufoHeight / 2
						|| wallsIntersects(ufo.x - ufoWidth / 2, ufo.y - 1
								- ufoHeight / 2, ufo.x + ufoWidth / 2, ufo.y
								- 1 + ufoHeight / 2)) {
					// Rebound
					ufo.y += 1;
					vel.y *= -1 * REBOUND_FAC;
					ufoYVelocity *= -1 * REBOUND_FAC;
					yFriction *= -1 * REBOUND_FAC;
				} else {
					ufo.y -= 1;
				}
				vel.y++;
			}
			n--;
		}
	}

	private void takeNStepsInXDirection(Point vel, int n) {
		while (n > 0) {
			// Take a steps along the xVel vector, making decisions as we
			// go.
			if (vel.x > 0) {
				if (ufo.x + 1 > mCanvasWidth - ufoWidth / 2
						|| wallsIntersects(ufo.x + 1 - ufoWidth / 2, ufo.y
								- ufoHeight / 2, ufo.x + 1 + ufoWidth / 2,
								ufo.y + ufoHeight / 2)) {
					// Rebound
					ufo.x -= 1;
					vel.x *= -1 * REBOUND_FAC;
					ufoXVelocity *= -1 * REBOUND_FAC;
					xFriction *= -1 * REBOUND_FAC;
				} else {
					ufo.x += 1;
				}
				vel.x--;
			} else {
				if (ufo.x - 1 < ufoWidth / 2
						|| wallsIntersects(ufo.x - 1 - ufoWidth / 2, ufo.y
								- ufoHeight / 2, ufo.x - 1 + ufoWidth / 2,
								ufo.y + ufoHeight / 2)) {
					// Rebound
					ufo.x += 1;
					vel.x *= -1 * REBOUND_FAC;
					ufoXVelocity *= -1 * REBOUND_FAC;
					xFriction *= -1 * REBOUND_FAC;
				} else {
					ufo.x -= 1;
				}
				vel.x++;
			}
			n--;
		}
	}

	/**
	 * Set the bounds for a vertical wall to the left of the cell.
	 * 
	 * @param wall
	 *            The wall to have it's bounds set.
	 * @param cell
	 *            The cell that has a wall to the left of it.
	 */
	private void setWallBoundsLeftCell(Wall wall, Cell cell) {
		wall.setBounds(new Rect(cell.getCoords().y * (cellWidth + wallWidth)
				+ boundaryWidth, cell.getCoords().x * (cellHeight + wallWidth)
				+ boundaryHeight, cell.getCoords().y * (cellWidth + wallWidth)
				+ wallWidth + boundaryWidth, (cell.getCoords().x + 1)
				* (cellHeight + wallWidth) + boundaryHeight + wallWidth));
	}

	/**
	 * Set the bounds for a vertical wall to the right of the cell.
	 * 
	 * @param wall
	 *            The wall to have it's bounds set.
	 * @param cell
	 *            The cell that has a wall to the right of it.
	 */
	private void setWallBoundsRightCell(Wall wall, Cell cell) {
		wall.setBounds(new Rect((cell.getCoords().y + 1)
				* (cellWidth + wallWidth) + boundaryWidth, cell.getCoords().x
				* (cellHeight + wallWidth) + boundaryHeight,
				(cell.getCoords().y + 1) * (cellWidth + wallWidth) + wallWidth
						+ boundaryWidth, (cell.getCoords().x + 1)
						* (cellHeight + wallWidth) + boundaryHeight + wallWidth));
	}

	/**
	 * Set the bounds for a horizontal wall above the cell.
	 * 
	 * @param wall
	 *            The wall to have it's bounds set.
	 * @param cell
	 *            The cell that has a wall above it.
	 */
	private void setWallBoundsAboveCell(Wall wall, Cell cell) {
		// Horizontal wall
		wall.setBounds(new Rect((cell.getCoords().y) * (cellWidth + wallWidth)
				+ boundaryWidth, (cell.getCoords().x)
				* (cellHeight + wallWidth) + boundaryHeight,
				(cell.getCoords().y + 1) * (cellWidth + wallWidth)
						+ boundaryWidth + wallWidth, cell.getCoords().x
						* (cellHeight + wallWidth) + wallWidth + boundaryHeight));
	}

	/**
	 * Set the bounds for a horizontal wall below the cell.
	 * 
	 * @param wall
	 *            The wall to have it's bounds set.
	 * @param cell
	 *            The cell that has a wall below it.
	 */
	private void setWallBoundsBelowCell(Wall wall, Cell cell) {
		// Horizontal wall
		wall.setBounds(new Rect((cell.getCoords().y) * (cellWidth + wallWidth)
				+ boundaryWidth, (cell.getCoords().x + 1)
				* (cellHeight + wallWidth) + boundaryHeight,
				(cell.getCoords().y + 1) * (cellWidth + wallWidth)
						+ boundaryWidth + wallWidth, (cell.getCoords().x + 1)
						* (cellHeight + wallWidth) + wallWidth + boundaryHeight));
	}

	/**
	 * Set the bounds for a wall next to a cell on the boundary of the maze.
	 * 
	 * @param wall
	 *            　The wall to have it's bounds set.
	 * @param cell
	 *            　The cell that the wall is next to.
	 * @param corners
	 *            　Boolean flags that determine the behavior of how corner cell
	 *            walls are drawn.
	 * @param mazeWidth
	 *            　How many cells are in one row of the maze.
	 * @param mazeHeight
	 *            How many cells are in one column of the maze.
	 */
	private void setWallBoundsBoundaryCell(Wall wall, Cell cell,
			boolean[] corners, int mazeWidth, int mazeHeight) {
		if (cell.getCoords().y == 0) {
			// TopLeft, TopRight, BottomLeft, BottomRight
			if (cell.getCoords().x == 0 && !corners[0]) {
				// Cell is on the top left => horizontal wall above
				// it.
				setWallBoundsAboveCell(wall, cell);
				// Set the flag topLeft so the second wall with
				// cells at (0,0) and null will be drawn on the
				// left.
				corners[0] = true;
			} else if (cell.getCoords().x == mazeHeight - 1 && !corners[2]) {
				// Cell is on the bottom left => horizontal wall
				// below it.
				setWallBoundsBelowCell(wall, cell);
				// Set the flag bottomLeft so the second wall
				// with cells at (0, maze.getHeight() - 1) and null
				// will be drawn on the left.
				corners[2] = true;
			} else {
				// Cell is on the left => vertical wall to
				// the left of it.
				setWallBoundsLeftCell(wall, cell);
			}
		} else if (cell.getCoords().y == mazeWidth - 1) {
			if (cell.getCoords().x == 0 && !corners[1]) {
				// Cell is on the top right => horizontal wall above
				// it.
				setWallBoundsAboveCell(wall, cell);
				// Set the flag topRight so the second wall with
				// cells at (0, maze.getHeight() - 1) and null will
				// be drawn on the right.
				corners[1] = true;
			} else if (cell.getCoords().x == mazeHeight - 1 && !corners[3]) {
				// Cell is on the bottom right => horizontal wall
				// below it.
				setWallBoundsBelowCell(wall, cell);
				// Set the flag bottomRight so the second wall
				// with cells at (maze.getWidth()-1,
				// maze.getHeight() - 1) and null will be drawn on
				// the right.
				corners[3] = true;
			} else {
				// Cell is on the right => vertical wall to the
				// right of it.
				setWallBoundsRightCell(wall, cell);
			}
		} else if (cell.getCoords().x == 0) {
			// Cell is on the top => horizontal wall above
			// it.
			setWallBoundsAboveCell(wall, cell);
		} else if (cell.getCoords().x == mazeHeight - 1) {
			// Cell is on the bottom => horizontal wall below
			// it.
			setWallBoundsBelowCell(wall, cell);
		}
	}

	private Rect calculateCellRect(Cell cell) {
		return new Rect(cell.getCoords().y * (cellWidth + wallWidth)
				+ wallWidth + boundaryWidth, cell.getCoords().x
				* (cellHeight + wallWidth) + wallWidth + boundaryHeight,
				(cell.getCoords().y + 1) * (cellWidth + wallWidth)
						+ boundaryWidth, (cell.getCoords().x + 1)
						* (cellHeight + wallWidth) + boundaryHeight);
	}
}
