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

package com.TeamAmazing.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private volatile float mXTouch;
    private volatile float mYTouch;
    private volatile boolean mIsAccelerating;
    private static final float TOUCH_TOLERANCE = 4;

    // The size of the maze
    private int mCellsPerRow = 12;
    private int mCellsPerColumn = 15;

    // Pixel sizes of objects
    private int mCellWidth;
    private int mCellHeight;
    private int mWallWidth;
    private int mBoundaryWidth;
    private int mBoundaryHeight;
    private int mUfoWidth;
    private int mUfoHeight;
    private static final double UFO_ASPECT_RATIO = 35.0 / 18.0;

    // ufo variables
    private static final float PREVIOUS_VELOCITY_FAC = .49f;
    private static final float TOUCH_FACTOR = .075f;
    private static final float FRICTION = .05f;
    private static final String UFO_X_VELOCITY_ID = "ufoxvelocity";
    private float mUfoXVelocity = 0;
    private static final String UFO_Y_VELOCITY_ID = "ufoyvelocity";
    private float mUfoYVelocity = 0;
    private static final String X_FRICTION_ID = "xfriction";
    private float mXFriction = 0;
    private static final String Y_FRICTION_ID = "yfriction";
    private float mYFriction = 0;
    private static final int MAX_SPEED = 10;
    private static final float REBOUND_FAC = .5f;
    private static final String UFO_ID = "ufo";
    private Point mUfo;
    private Path mPath;
    private Bitmap mUfoBm;

    // maze variables
    private static final String MAZE_ID = "maze";
    private Maze mMaze;
    private volatile int mMazeType = StartMenu.PERFECT_MAZE;
    private Rect mEndRect;
    private Rect mStartRect;

    /** @see setSurfaceSize */
    private volatile int mCanvasHeight;
    private volatile int mCanvasWidth;

    // References to important objects
    private SurfaceHolder mSurfaceHolder;
    private Handler mHandler;
    private Context mContext;

    public static final int MESSAGE_MAZE_COMPLETED = 1;
    public static final int MESSAGE_UPDATE_TIMER = 2;

    // Thread states
    private static final int STATE_STOPPED = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_WAIT_FOR_DIALOG = 3;
    private static final int STATE_RESET_AFTER_MEASURE = 4;
    private volatile int mState = STATE_STOPPED;

    private Paint mPaint;

    // Timing variables
    private static final String TIME_ELAPSED_ID = "timeElapsed";
    private int mTimeElapsed = 0;
    private long mTimeStart;

    public MazeThread(SurfaceHolder surfaceHolder, Context context, Handler handler, int mazeType) {
        mSurfaceHolder = surfaceHolder;
        mHandler = handler;
        mMazeType = mazeType;
        mContext = context;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(255);
        mPaint.setStrokeWidth(1);

        mUfo = new Point();
        mPath = new Path();

        newMaze();
    }

    /**
     * Stops this MazeThread.
     */
    public void halt() {
        synchronized (mSurfaceHolder) {
            mState = STATE_STOPPED;
            mSurfaceHolder.notify();
        }
    }

    public void pause() {
        synchronized (mSurfaceHolder) {
            if (mState == STATE_RUNNING)
                mState = STATE_PAUSED;
        }
    }

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
                outState.putParcelable(MAZE_ID, mMaze);
                outState.putParcelable(UFO_ID, mUfo);
                outState.putFloat(UFO_X_VELOCITY_ID, mUfoXVelocity);
                outState.putFloat(UFO_Y_VELOCITY_ID, mUfoYVelocity);
                outState.putFloat(X_FRICTION_ID, mXFriction);
                outState.putFloat(Y_FRICTION_ID, mYFriction);
                outState.putInt(TIME_ELAPSED_ID, mTimeElapsed);
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
        synchronized (mSurfaceHolder) {
            mMaze = savedState.getParcelable(MAZE_ID);
            mState = STATE_RUNNING;
            mUfo = savedState.getParcelable(UFO_ID);
            mUfoXVelocity = savedState.getFloat(UFO_X_VELOCITY_ID);
            mUfoYVelocity = savedState.getFloat(UFO_Y_VELOCITY_ID);
            mXFriction = savedState.getFloat(X_FRICTION_ID);
            mYFriction = savedState.getFloat(Y_FRICTION_ID);
            mTimeElapsed = savedState.getInt(TIME_ELAPSED_ID);
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

            mTimeStart = System.currentTimeMillis();

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
                mTimeElapsed = mTimeElapsed + (int) (System.currentTimeMillis() - mTimeStart);
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_TIMER, mTimeElapsed, 0,
                        null));
            }

        }
    }

    private void mDraw(Canvas canvas) {
        // draw the background
        mPaint.setColor(mContext.getResources().getColor(R.color.paper_white));
        canvas.drawRect(0, 0, mCanvasWidth, mCanvasHeight, mPaint);

        // draw the maze
        mPaint.setColor(Color.BLACK);
        for (Wall w : mMaze.getWalls()) {
            canvas.drawRect(w.getBounds(), mPaint);
        }

        // Draw the end cell.
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(150);
        canvas.drawRect(mEndRect, mPaint);
        mPaint.setAlpha(255);

        // Draw the path
        mPaint.setColor(mContext.getResources().getColor(R.color.pencil_grey));
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        // Draw the ufo.
        canvas.drawBitmap(mUfoBm, mUfo.x - mUfoWidth / 2, mUfo.y - mUfoHeight / 2, null);

    }

    /**
     * Update the position of the UFO
     */
    private void updatePosition() {

        Point vel = new Point(Math.round(mUfoXVelocity), Math.round(mUfoYVelocity));
        boolean positionUpdated = false;
        while (Math.abs(vel.x) > 0 || Math.abs(vel.y) > 0) {
            positionUpdated = true;
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

        // Add current position to mPath if the position changed.
        if (positionUpdated)
            mPath.lineTo(mUfo.x, mUfo.y);

        // Check if we are in the end cell.
        if (mEndRect.contains(mUfo.x, mUfo.y)) {
            // Sprite is inside the end cell.
            mazeCompleted();
        }
    }

    private void mazeCompleted() {
        // Send a message to the UI thread
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_MAZE_COMPLETED, mTimeElapsed, 0, null));
        synchronized (mSurfaceHolder) {
            mState = STATE_WAIT_FOR_DIALOG;
        }
    }

    /**
     * Note: Can be called from UI thread or MazeThread. Creates a new maze.
     */
    public void newMaze() {
        synchronized (mSurfaceHolder) {
            mMaze = new Maze(mCellsPerRow, mCellsPerColumn, mMazeType);
            if (mCanvasHeight != 0 && mCanvasWidth != 0) {
                mUfoXVelocity = 0;
                mUfoYVelocity = 0;
                mXFriction = 0;
                mYFriction = 0;
                mTimeElapsed = 0;
                calculateGFXSizes();
                mUfo.x = mStartRect.centerX();
                mUfo.y = mStartRect.centerY();
                mPath.reset();
                mPath.moveTo(mUfo.x, mUfo.y);
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
            mCellWidth = (int) (mCanvasWidth / (mCellsPerRow + (mCellsPerRow + 1) / 12.0));
            mCellHeight = (int) (mCanvasHeight / (mCellsPerColumn + (mCellsPerColumn + 1) / 12.0));
            mWallWidth = mCellWidth > mCellHeight ? mCellHeight / 12 : mCellWidth / 12;
            mBoundaryWidth = (mCanvasWidth - mCellWidth * mCellsPerRow - mWallWidth
                    * (mCellsPerRow + 1)) / 2;
            mBoundaryHeight = (mCanvasHeight - mCellHeight * mCellsPerColumn - mWallWidth
                    * (mCellsPerColumn + 1)) / 2;

            // Calculate ufo size to be approximately 2/3rds of cellWidth or
            // cellHeight, whichever makes a smaller ufo.
            if (2 * mCellWidth / 3 + (int) Math.round((2 * mCellWidth / 3) / UFO_ASPECT_RATIO) < 2
                    * mCellHeight / 3 + (int) Math.round((2 * mCellHeight / 3) * UFO_ASPECT_RATIO)) {
                mUfoWidth = 2 * mCellWidth / 3;
                mUfoHeight = (int) Math.round(mUfoWidth / UFO_ASPECT_RATIO);
            } else {
                mUfoHeight = 2 * mCellHeight / 3;
                mUfoWidth = (int) Math.round(mUfoHeight * UFO_ASPECT_RATIO);
            }

            // resize the ufo bitmap
            mUfoBm = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ufo),
                    mUfoWidth, mUfoHeight, false);

            // get bounds for start and end rectangles
            mEndRect = calculateCellRect(mMaze.getCell(Cell.END_CELL));
            mStartRect = calculateCellRect(mMaze.getCell(Cell.START_CELL));

            // Calculate the bounds for each wall.
            // Each corner will have two indistinguishable walls that need
            // have
            // different orientations so set a boolean flag for each corner.
            // topLeft, topRight, bottomLeft, bottomRight
            boolean[] corners = {
                    false, false, false, false
            };
            for (Wall w : mMaze.getWalls()) {
                Cell cell1 = w.getCell1();
                Cell cell2 = w.getCell2();
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
                        setWallBoundsBoundaryCell(w, cell1, corners, mMaze.getWidth(),
                                mMaze.getHeight());
                    } else {
                        // The first cell of a wall should never be null,
                        // but
                        // this is included for future usability.
                        setWallBoundsBoundaryCell(w, cell2, corners, mMaze.getWidth(),
                                mMaze.getHeight());
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
                if (mCanvasWidth > 0 && mCanvasHeight > 0 && mState == STATE_RESET_AFTER_MEASURE) {
                    newMaze();
                }
            }
        }
    }

    /** Called by the UI thread when there is a touch event. */
    public boolean handleTouchEvent(MotionEvent ev) {
        synchronized (mSurfaceHolder) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mXTouch = ev.getX();
                    mYTouch = ev.getY();
                    mIsAccelerating = true;
                    break;
                case MotionEvent.ACTION_UP:
                    mIsAccelerating = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mXTouch = ev.getX();
                    mYTouch = ev.getY();
                    mIsAccelerating = true;
                    break;
            }
            return true;
        }
    }

    /**
     * Update the velocity of the ufo.
     */
    private void updateVelocity() {
        if (mIsAccelerating
                && (Math.abs(mXTouch - mUfo.x) > TOUCH_TOLERANCE || Math.abs(mYTouch - mUfo.y) > TOUCH_TOLERANCE)) {
            mUfoXVelocity = TOUCH_FACTOR
                    * (mXTouch - mUfo.x + Math.round(PREVIOUS_VELOCITY_FAC * mUfoXVelocity));
            mUfoYVelocity = TOUCH_FACTOR
                    * (mYTouch - mUfo.y + Math.round(PREVIOUS_VELOCITY_FAC * mUfoYVelocity));
            // Enforce max speed;
            int accSpeed = (int) Math.round(Math.sqrt(Math.pow(mUfoXVelocity, 2)
                    + Math.pow(mUfoYVelocity, 2)));
            if (accSpeed > MAX_SPEED + 1) {
                mUfoXVelocity = mUfoXVelocity * MAX_SPEED / accSpeed;
                mUfoYVelocity = mUfoYVelocity * MAX_SPEED / accSpeed;
            }
        } else {
            // Decrease speed with friction.
            float speed = (float) Math
                    .sqrt(Math.pow(mUfoXVelocity, 2) + Math.pow(mUfoYVelocity, 2));
            if ((Math.abs(mUfoXVelocity) + Math.abs(mUfoYVelocity)) > 0) {
                mXFriction = speed * FRICTION * -1 * mUfoXVelocity
                        / (Math.abs(mUfoXVelocity) + Math.abs(mUfoYVelocity));
                mYFriction = speed * FRICTION * -1 * mUfoYVelocity
                        / (Math.abs(mUfoXVelocity) + Math.abs(mUfoYVelocity));
            }
            mUfoXVelocity = mUfoXVelocity + mXFriction;
            mUfoYVelocity = mUfoYVelocity + mYFriction;
        }
    }

    /**
     * Given the bounds for a UFO position checks if it intersects with a wall.
     * 
     * @param left The left bound of the UFO.
     * @param top The top bound of the UFO.
     * @param right The right bound of the UFO.
     * @param bottom The bottom bound of the UFO.
     * @return True iff the given UFO position intersects with a wall.
     */
    private boolean wallsIntersects(int left, int top, int right, int bottom) {
        for (Wall w : mMaze.getWalls()) {
            if (w.getBounds().intersects(left, top, right, bottom)) {
                // get the bounds of the intersection
                Rect intersection = new Rect();
                intersection.setIntersect(w.getBounds(), new Rect(left, top, right, bottom));
                for (int x = intersection.left; x < intersection.right; x++) {
                    for (int y = intersection.top; y < intersection.bottom; y++) {
                        if (mUfoBm.getPixel(x - left, y - top) != Color.TRANSPARENT) {
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    /**
     * Move the UFO N steps in the Y direction, rebounding from walls as needed.
     * 
     * @param n The number of steps to move
     */
    private void takeNStepsInYDirection(Point vel, int n) {
        while (n > 0) {
            // Take a steps along the yVel vector, making decisions as we
            // go.
            if (vel.y > 0) {
                if (mUfo.y + 1 > mCanvasHeight - mUfoHeight / 2
                        || wallsIntersects(mUfo.x - mUfoWidth / 2, mUfo.y + 1 - mUfoHeight / 2,
                                mUfo.x + mUfoWidth / 2, mUfo.y + 1 + mUfoHeight / 2)) {
                    // Rebound
                    mUfo.y -= 1;
                    vel.y *= -1 * REBOUND_FAC;
                    mUfoYVelocity *= -1 * REBOUND_FAC;
                    mYFriction *= -1 * REBOUND_FAC;
                } else {
                    mUfo.y += 1;
                }
                vel.y--;
            } else {
                if (mUfo.y - 1 < mUfoHeight / 2
                        || wallsIntersects(mUfo.x - mUfoWidth / 2, mUfo.y - 1 - mUfoHeight / 2,
                                mUfo.x + mUfoWidth / 2, mUfo.y - 1 + mUfoHeight / 2)) {
                    // Rebound
                    mUfo.y += 1;
                    vel.y *= -1 * REBOUND_FAC;
                    mUfoYVelocity *= -1 * REBOUND_FAC;
                    mYFriction *= -1 * REBOUND_FAC;
                } else {
                    mUfo.y -= 1;
                }
                vel.y++;
            }
            n--;
        }
    }

    /**
     * Move the UFO N steps in the X direction, rebounding from walls as needed.
     * 
     * @param n The number of steps to move
     */
    private void takeNStepsInXDirection(Point vel, int n) {
        while (n > 0) {
            // Take a steps along the xVel vector, making decisions as we
            // go.
            if (vel.x > 0) {
                if (mUfo.x + 1 > mCanvasWidth - mUfoWidth / 2
                        || wallsIntersects(mUfo.x + 1 - mUfoWidth / 2, mUfo.y - mUfoHeight / 2,
                                mUfo.x + 1 + mUfoWidth / 2, mUfo.y + mUfoHeight / 2)) {
                    // Rebound
                    mUfo.x -= 1;
                    vel.x *= -1 * REBOUND_FAC;
                    mUfoXVelocity *= -1 * REBOUND_FAC;
                    mXFriction *= -1 * REBOUND_FAC;
                } else {
                    mUfo.x += 1;
                }
                vel.x--;
            } else {
                if (mUfo.x - 1 < mUfoWidth / 2
                        || wallsIntersects(mUfo.x - 1 - mUfoWidth / 2, mUfo.y - mUfoHeight / 2,
                                mUfo.x - 1 + mUfoWidth / 2, mUfo.y + mUfoHeight / 2)) {
                    // Rebound
                    mUfo.x += 1;
                    vel.x *= -1 * REBOUND_FAC;
                    mUfoXVelocity *= -1 * REBOUND_FAC;
                    mXFriction *= -1 * REBOUND_FAC;
                } else {
                    mUfo.x -= 1;
                }
                vel.x++;
            }
            n--;
        }
    }

    /**
     * Set the bounds for a vertical wall to the left of the cell.
     * 
     * @param wall The wall to have it's bounds set.
     * @param cell The cell that has a wall to the left of it.
     */
    private void setWallBoundsLeftCell(Wall wall, Cell cell) {
        wall.setBounds(new Rect(cell.getCoords().y * (mCellWidth + mWallWidth) + mBoundaryWidth,
                cell.getCoords().x * (mCellHeight + mWallWidth) + mBoundaryHeight,
                cell.getCoords().y * (mCellWidth + mWallWidth) + mWallWidth + mBoundaryWidth, (cell
                        .getCoords().x + 1)
                        * (mCellHeight + mWallWidth)
                        + mBoundaryHeight
                        + mWallWidth));
    }

    /**
     * Set the bounds for a vertical wall to the right of the cell.
     * 
     * @param wall The wall to have it's bounds set.
     * @param cell The cell that has a wall to the right of it.
     */
    private void setWallBoundsRightCell(Wall wall, Cell cell) {
        wall.setBounds(new Rect((cell.getCoords().y + 1) * (mCellWidth + mWallWidth)
                + mBoundaryWidth,
                cell.getCoords().x * (mCellHeight + mWallWidth) + mBoundaryHeight, (cell
                        .getCoords().y + 1)
                        * (mCellWidth + mWallWidth)
                        + mWallWidth
                        + mBoundaryWidth, (cell.getCoords().x + 1) * (mCellHeight + mWallWidth)
                        + mBoundaryHeight + mWallWidth));
    }

    /**
     * Set the bounds for a horizontal wall above the cell.
     * 
     * @param wall The wall to have it's bounds set.
     * @param cell The cell that has a wall above it.
     */
    private void setWallBoundsAboveCell(Wall wall, Cell cell) {
        // Horizontal wall
        wall.setBounds(new Rect((cell.getCoords().y) * (mCellWidth + mWallWidth) + mBoundaryWidth,
                (cell.getCoords().x) * (mCellHeight + mWallWidth) + mBoundaryHeight, (cell
                        .getCoords().y + 1)
                        * (mCellWidth + mWallWidth)
                        + mBoundaryWidth
                        + mWallWidth, cell.getCoords().x * (mCellHeight + mWallWidth) + mWallWidth
                        + mBoundaryHeight));
    }

    /**
     * Set the bounds for a horizontal wall below the cell.
     * 
     * @param wall The wall to have it's bounds set.
     * @param cell The cell that has a wall below it.
     */
    private void setWallBoundsBelowCell(Wall wall, Cell cell) {
        // Horizontal wall
        wall.setBounds(new Rect((cell.getCoords().y) * (mCellWidth + mWallWidth) + mBoundaryWidth,
                (cell.getCoords().x + 1) * (mCellHeight + mWallWidth) + mBoundaryHeight, (cell
                        .getCoords().y + 1)
                        * (mCellWidth + mWallWidth)
                        + mBoundaryWidth
                        + mWallWidth, (cell.getCoords().x + 1) * (mCellHeight + mWallWidth)
                        + mWallWidth + mBoundaryHeight));
    }

    /**
     * Set the bounds for a wall next to a cell on the boundary of the maze.
     * 
     * @param wall The wall to have it's bounds set.
     * @param cell The cell that the wall is next to.
     * @param corners Boolean flags that determine the behavior of how corner
     *            cell walls are drawn.
     * @param mazeWidth How many cells are in one row of the maze.
     * @param mazeHeight How many cells are in one column of the maze.
     */
    private void setWallBoundsBoundaryCell(Wall wall, Cell cell, boolean[] corners, int mazeWidth,
            int mazeHeight) {
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

    /**
     * Returns a Rect with the bounds for the cell.
     */
    private Rect calculateCellRect(Cell cell) {
        return new Rect(cell.getCoords().y * (mCellWidth + mWallWidth) + mWallWidth
                + mBoundaryWidth, cell.getCoords().x * (mCellHeight + mWallWidth) + mWallWidth
                + mBoundaryHeight, (cell.getCoords().y + 1) * (mCellWidth + mWallWidth)
                + mBoundaryWidth, (cell.getCoords().x + 1) * (mCellHeight + mWallWidth)
                + mBoundaryHeight);
    }
}
