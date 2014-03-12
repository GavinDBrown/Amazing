//    Amazing, the simple maze game.
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
package com.TeamAmazing.Maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

import com.TeamAmazing.drawing.GOLThread;

@SuppressLint("UseSparseArrays")
public class GameOfLife implements Parcelable {

	// The number of alive neighbors a cell must have to live between
	// generations.
	private Set<Byte> ruleToLive = new HashSet<Byte>();
	// The number of alive neighbors a cell must have to be born.
	private Set<Byte> ruleToBeBorn = new HashSet<Byte>();
	private Map<Integer, Boolean> changeList = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> nextChangeList = new HashMap<Integer, Boolean>();
	private Set<Integer> checkSet = new HashSet<Integer>();

	private Random rand;

	/**
	 * A 2D board of game of life bytes stored in row major order. The bottom 4
	 * bits of a byte represent the number of alive neighbors a locations has.
	 * The 5th bit is on iff the cell is alive. The top 3 bits count the number
	 * of times this byte has been put into nextChangeList.
	 */
	private byte[] board;
	private int mWidth, mHeight;
	public static final byte ALIVE_MASK = (byte) 16;
	private static final byte NEIGHBORS_MASK = (byte) 15;
	/** WARNING: shift to the right 5 times before using this mask! */
	private static final byte TIMES_CHANGED_MASK = (byte) 7;
	private boolean firstTime;
	private int emptyTimes;
	private static final int MAX_EMPTY_TIMES = 10;
	private static final int MAX_CELLS_TO_MAKE_ALIVE = 10;

	// The width and height of maze cells in pixels.
	public static final int CELL_WIDTH = 4;
	public static final int CELL_HEIGHT = 4;
	
	private Paint mPaint;
	
	// Handle to the thread running this GOL
	private GOLThread mThread;

	/**
	 * The Canvas drawn upon. This way we only need to draw the changed cells
	 * instead of the entire game each iteration.
	 */
	private Canvas myCanvas = null;
	private Bitmap myCanvasBitmap = null;
	private Matrix identityMatrix;

	public GameOfLife(GOLThread t) {
		mThread = t;
		rand = new Random();
		mPaint = new Paint();
		mPaint.setAlpha(255);
		mPaint.setStyle(Paint.Style.FILL);
		setRules();
	}

	private void setRules() {
		// The rules
		ruleToLive.add((byte) 1);
		ruleToLive.add((byte) 2);
		ruleToLive.add((byte) 3);
		ruleToLive.add((byte) 4);
		ruleToLive.add((byte) 5);
		ruleToBeBorn.add((byte) 3);
	}

	public byte[] getBoard() {
		return board;
	}

	/**
	 * Initialize this GameOfLife. Creates a new empty board and adds random
	 * starting points to the changeList.
	 * 
	 * @param canvasWidth
	 *            The width of the canvas this GameOfLife will be drawn on.
	 * @param canvasHeight
	 *            The height of the canvas this GameOfLife will be drawn on.
	 */
	public void init(int canvasWidth, int canvasHeight) {
		myCanvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight,
				Bitmap.Config.ARGB_8888);
		myCanvas = new Canvas();
		myCanvas.setBitmap(myCanvasBitmap);
		identityMatrix = new Matrix();

		mWidth = canvasWidth / GameOfLife.CELL_WIDTH;
		mHeight = canvasHeight / GameOfLife.CELL_HEIGHT;
		firstTime = true;
		emptyTimes = 0;
		Random rand = new Random();
		board = new byte[mWidth * mHeight];
		changeList.clear();
		// Create 20 to 30 random starting cells.
		int numOfStartingCells = rand.nextInt(11) + 20;
		int randHorzOffset, randVertOffset, randX, randY;
		randX = rand.nextInt(mWidth);
		randY = rand.nextInt(mHeight);
		while (numOfStartingCells > 0) {
			randHorzOffset = (mWidth + rand.nextInt(10) + randX) % mWidth;
			randVertOffset = (mHeight + rand.nextInt(10) + randY) % mHeight;
			if ((board[randVertOffset * mWidth + randHorzOffset] & ALIVE_MASK) == 0) {
				// cell is dead, make it alive
				changeList.put(randVertOffset * mWidth + randHorzOffset, true);
			}
			numOfStartingCells--;
		}
	}

	// a cell should never have more than 8 neighbors so bit 4 should never
	// carry into bit 5, which is the cell state bit.
	private void makeAlive(int x, int y) {
		board[y * mWidth + x] |= ALIVE_MASK;
		// update the neighbors
		board[y * mWidth + ((x + 1) % mWidth)] += 1;
		board[((y + 1) % mHeight) * mWidth + ((x + 1) % mWidth)] += 1;
		board[((y - 1 + mHeight) % mHeight) * mWidth + ((x + 1) % mWidth)] += 1;
		board[((y + 1) % mHeight) * mWidth + x] += 1;
		board[((y - 1 + mHeight) % mHeight) * mWidth + x] += 1;
		board[y * mWidth + ((x - 1 + mWidth) % mWidth)] += 1;
		board[((y + 1) % mHeight) * mWidth + ((x - 1 + mWidth) % mWidth)] += 1;
		board[((y - 1 + mHeight) % mHeight) * mWidth
				+ ((x - 1 + mWidth) % mWidth)] += 1;
	}

	private void kill(int x, int y) {
		board[y * mWidth + x] &= ~ALIVE_MASK;
		// update the neighbors
		if ((board[y * mWidth + ((x + 1) % mWidth)] & NEIGHBORS_MASK) > 0) {
			board[y * mWidth + ((x + 1) % mWidth)] -= 1;
		}
		if ((board[((y + 1) % mHeight) * mWidth + ((x + 1) % mWidth)] & NEIGHBORS_MASK) > 0) {
			board[((y + 1) % mHeight) * mWidth + ((x + 1) % mWidth)] -= 1;
		}
		if ((board[((y - 1 + mHeight) % mHeight) * mWidth + ((x + 1) % mWidth)] & NEIGHBORS_MASK) > 0) {
			board[((y - 1 + mHeight) % mHeight) * mWidth + ((x + 1) % mWidth)] -= 1;
		}
		if ((board[((y + 1) % mHeight) * mWidth + x] & NEIGHBORS_MASK) > 0) {
			board[((y + 1) % mHeight) * mWidth + x] -= 1;
		}
		if ((board[((y - 1 + mHeight) % mHeight) * mWidth + x] & NEIGHBORS_MASK) > 0) {
			board[((y - 1 + mHeight) % mHeight) * mWidth + x] -= 1;
		}
		if ((board[y * mWidth + ((x - 1 + mWidth) % mWidth)] & NEIGHBORS_MASK) > 0) {
			board[y * mWidth + ((x - 1 + mWidth) % mWidth)] -= 1;
		}
		if ((board[((y + 1) % mHeight) * mWidth + ((x - 1 + mWidth) % mWidth)] & NEIGHBORS_MASK) > 0) {
			board[((y + 1) % mHeight) * mWidth + ((x - 1 + mWidth) % mWidth)] -= 1;
		}
		if ((board[((y - 1 + mHeight) % mHeight) * mWidth
				+ ((x - 1 + mWidth) % mWidth)] & NEIGHBORS_MASK) > 0) {
			board[((y - 1 + mHeight) % mHeight) * mWidth
					+ ((x - 1 + mWidth) % mWidth)] -= 1;
		}
	}

	/**
	 * Computes and draws the next generation in the game of life. Uses a list
	 * of changes so not every cell needs to be checked.
	 * 
	 */
	public void drawAndUpdate(Canvas canvas) {

		if (canvas == null)
			return;

		// Check if we have exceeded the maximum number of generations
		if (emptyTimes > MAX_EMPTY_TIMES) {
			// restart after a delay;
			mThread.GOLRestarting();
			init(myCanvas.getWidth(), myCanvas.getHeight());
			return;
		}

		// Draw a black background if this is the first generation
		if (firstTime) {
			mPaint.setColor(Color.BLACK);
			myCanvas.drawRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight(),
					mPaint);
			firstTime = false;
		}

		if (changeList.isEmpty()) {
			emptyTimes++;
			int cellsMadeAlive = 0;
			// Look for unfilled spots and start a couple of cells there.
			int offset = rand.nextInt(board.length);
			int loc;
			for (int i = 0; i < board.length; i++) {
				loc = (i + offset) % board.length;
				if ((board[loc] & ALIVE_MASK) == 0) {
					if (cellsMadeAlive >= MAX_CELLS_TO_MAKE_ALIVE) {
						break;
					}
					int x = loc % mWidth;
					int y = loc / mWidth;
					// cell is dead check to see if all it's neighbors are dead
					if (((board[y * mWidth + x] & ALIVE_MASK) == 0)
							&& ((board[y * mWidth + ((x + 1) % mWidth)] & ALIVE_MASK) == 0)
							&& ((board[((y + 1) % mHeight) * mWidth
									+ ((x + 1) % mWidth)] & ALIVE_MASK) == 0)
							&& ((board[((y - 1 + mHeight) % mHeight) * mWidth
									+ ((x + 1) % mWidth)] & ALIVE_MASK) == 0)
							&& ((board[((y + 1) % mHeight) * mWidth + x] & ALIVE_MASK) == 0)
							&& ((board[((y - 1 + mHeight) % mHeight) * mWidth
									+ x] & ALIVE_MASK) == 0)
							&& ((board[y * mWidth + ((x - 1 + mWidth) % mWidth)] & ALIVE_MASK) == 0)
							&& ((board[((y + 1) % mHeight) * mWidth
									+ ((x - 1 + mWidth) % mWidth)] & ALIVE_MASK) == 0)
							&& ((board[((y - 1 + mHeight) % mHeight) * mWidth
									+ ((x - 1 + mWidth) % mWidth)] & ALIVE_MASK) == 0)) {
						// all of the neighbors are dead
						// give this cell a chance to come alive.
						if (rand.nextInt(2) == 0) {
							changeList.put(loc, true);
							cellsMadeAlive++;
						}
					}

				}

			}
		}

		// make changes in the changeList and draw them
		for (Entry<Integer, Boolean> entry : changeList.entrySet()) {
			int x = entry.getKey() % mWidth;
			int y = entry.getKey() / mWidth;
			boolean state = entry.getValue();
			if (state) {
				// make cell alive in the board
				makeAlive(x, y);
				// draw alive cell
				mPaint.setColor(Color.WHITE);
				myCanvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
						* CELL_WIDTH, (y + 1) * CELL_HEIGHT, mPaint);
			} else {
				// make cell dead in the board
				kill(x, y);
				// draw dead cell
				mPaint.setColor(Color.BLACK);
				myCanvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
						* CELL_WIDTH, (y + 1) * CELL_HEIGHT, mPaint);
			}

		}

		// compute next changes
		/**
		 * check each cell in the change list and their neighbors. Add each
		 * location to a set to avoid checking the same location multiple times.
		 */
		for (Entry<Integer, Boolean> entry : changeList.entrySet()) {
			int x = entry.getKey() % mWidth;
			int y = entry.getKey() / mWidth;
			checkSet.add(y * mWidth + x);
			checkSet.add(y * mWidth + ((x + 1) % mWidth));
			checkSet.add(((y + 1) % mHeight) * mWidth + ((x + 1) % mWidth));
			checkSet.add(((y - 1 + mHeight) % mHeight) * mWidth
					+ ((x + 1) % mWidth));
			checkSet.add(((y + 1) % mHeight) * mWidth + x);
			checkSet.add(((y - 1 + mHeight) % mHeight) * mWidth + x);
			checkSet.add(y * mWidth + ((x - 1 + mWidth) % mWidth));
			checkSet.add(((y + 1) % mHeight) * mWidth
					+ ((x - 1 + mWidth) % mWidth));
			checkSet.add(((y - 1 + mHeight) % mHeight) * mWidth
					+ ((x - 1 + mWidth) % mWidth));
		}

		// check each location in the checkSet
		for (int loc : checkSet)
			checkCell(loc);
		checkSet.clear();

		// swap the changeLists
		Map<Integer, Boolean> temp = changeList;
		changeList = nextChangeList;
		nextChangeList = temp;
		nextChangeList.clear();

		canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);
	}

	/**
	 * Checks if a cell should live or die in the next generation. Adds detected
	 * changes to nextChangeList.
	 * 
	 * @param x
	 *            The x coordinate of the cell to check.
	 * @param y
	 *            The y coordinate of the cell to check.
	 */
	private void checkCell(int loc) {
		if ((board[loc] & ALIVE_MASK) != 0) {
			// cell is alive
			// check if it should die.
			if (!ruleToLive.contains((byte) (board[loc] & NEIGHBORS_MASK))) {
				// kill the cell in the next generation

				// If a cell has been in nextChangeList 7 times it's likely part
				// of an oscillator, so don't add it anymore.

				if (((board[loc] >>> 5) & TIMES_CHANGED_MASK) < 7) {
					board[loc] = (byte) (((((board[loc] >>> 5) & TIMES_CHANGED_MASK) + 1) << 5) | (board[loc] & ~(TIMES_CHANGED_MASK << 5)));
					nextChangeList.put(loc, false);
				}
			}
		} else {
			// cell is dead
			// check if it should be born
			if (ruleToBeBorn.contains((byte) (board[loc] & NEIGHBORS_MASK))) {
				// make the cell become alive in the next generation
				if (((board[loc] >>> 5) & TIMES_CHANGED_MASK) < 7) {
					board[loc] = (byte) (((((board[loc] >>> 5) & TIMES_CHANGED_MASK) + 1) << 5) | (board[loc] & ~(TIMES_CHANGED_MASK << 5)));
					nextChangeList.put(loc, true);
				}
			}
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeByteArray(board);
		out.writeInt(changeList.size());
		for (Integer key : changeList.keySet()) {
			out.writeInt(key);
			out.writeByte((byte) (changeList.get(key) ? 1 : 0));
		}
		out.writeInt(emptyTimes);
		out.writeByte((byte) (firstTime ? 1 : 0));
	}

	private void readFromParcel(Parcel in) {
		in.readByteArray(board);
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			int key = in.readInt();
			Boolean value = in.readByte() != 0;
			changeList.put(key, value);
		}
		emptyTimes = in.readInt();
		firstTime = in.readByte() != 0;
	}

	/**
	 * Constructor to use when re-constructing object from a parcel
	 * 
	 * @param in
	 *            a parcel from which to read this object
	 */
	public GameOfLife(Parcel in) {
		setRules();
		readFromParcel(in);
	}

	public static final Parcelable.Creator<GameOfLife> CREATOR = new Parcelable.Creator<GameOfLife>() {
		public GameOfLife createFromParcel(Parcel in) {
			return new GameOfLife(in);
		}

		public GameOfLife[] newArray(int size) {
			return new GameOfLife[size];
		}
	};
}
