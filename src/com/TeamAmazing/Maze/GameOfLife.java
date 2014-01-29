package com.TeamAmazing.Maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

public class GameOfLife implements Parcelable {

	// The number of alive neighbors a cell must have to live between
	// generations.
	private Set<Byte> ruleToLive = new HashSet<Byte>();
	// The number of alive neighbors a cell must have to be born.
	private Set<Byte> ruleToBeBorn = new HashSet<Byte>();
	private Map<Long, Boolean> changeList = new HashMap<Long, Boolean>();
	private Map<Long, Boolean> nextChangeList = new HashMap<Long, Boolean>();
	private byte[][] board;
	public static final byte ALIVE_MASK = 16;
	private static final byte NEIGHBORS_MASK = 15;
	private int maxGenerations;
	private int numCurrentGenerations;

	// The width and height of maze cells in pixels.
	public static final int CELL_WIDTH = 10;
	public static final int CELL_HEIGHT = 10;

	private Paint mPaint;
	
	/** The Canvas drawn upon. This way we only need to draw the changed cells instead of the entire game each iteration.*/
	private Canvas myCanvas = null;
	private Bitmap myCanvasBitmap = null;
	private Matrix identityMatrix;

	public GameOfLife() {
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
		// ruleToLive.add((byte)5);
		ruleToBeBorn.add((byte) 3);
	}

	public byte[][] getBoard() {
		return board;
	}

	public void init(int canvasWidth, int canvasHeight) {
		myCanvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
		myCanvas = new Canvas();
		myCanvas.setBitmap(myCanvasBitmap);
		identityMatrix = new Matrix();

		int width = canvasWidth / GameOfLife.CELL_WIDTH;
		int height = canvasHeight / GameOfLife.CELL_HEIGHT;
		maxGenerations = (int) Math.max(2.5 * width, 2.5 * height);
		numCurrentGenerations = 0;
		Random rand = new Random();
		board = new byte[width][height];
		changeList.clear();
		// Create 20 to 30 random starting cells.
		int numOfStartingCells = rand.nextInt(11) + 20;
		int randHorzOffset, randVertOffset;
		while (numOfStartingCells > 0) {
			randHorzOffset = (width + rand.nextInt(10) + (width / 2 - 5)) % width;
			randVertOffset = (height + rand.nextInt(10) + (height / 2 - 5)) % height;
			if ((board[randHorzOffset][randVertOffset] & ALIVE_MASK) == 0) {
				// cell is dead, make it alive
				// makeAlive(randHorzOffset, randVertOffset);
				long loc = (long) randHorzOffset << 32 | randVertOffset
						& 0xFFFFFFFFL;
				changeList.put(loc, true);
			}
			numOfStartingCells--;
		}
	}

	// a cell should never have more than 8 neighbors so bit 4 should never
	// carry into bit 5, which is the cell state bit.
	private void makeAlive(int x, int y) {
		board[x][y] |= ALIVE_MASK;
		// update the neighbors
		board[(x + 1) % board.length][y] += 1;
		board[(x + 1) % board.length][(y + 1) % board[0].length] += 1;
		board[(x + 1) % board.length][(y - 1 + board[0].length)
				% board[0].length] += 1;
		board[x][(y + 1) % board[0].length] += 1;
		board[x][(y - 1 + board[0].length) % board[0].length] += 1;
		board[(x - 1 + board.length) % board.length][y] += 1;
		board[(x - 1 + board.length) % board.length][(y + 1) % board[0].length] += 1;
		board[(x - 1 + board.length) % board.length][(y - 1 + board[0].length)
				% board[0].length] += 1;
	}

	private void kill(int x, int y) {
		board[x][y] &= ~ALIVE_MASK;
		// update the neighbors
		if ((board[(x + 1) % board.length][y] & NEIGHBORS_MASK) > 0) {
			board[(x + 1) % board.length][y] -= 1;
		}
		if ((board[(x + 1) % board.length][(y + 1) % board[0].length] & NEIGHBORS_MASK) > 0) {
			board[(x + 1) % board.length][(y + 1) % board[0].length] -= 1;
		}
		if ((board[(x + 1) % board.length][(y - 1 + board[0].length)
				% board[0].length] & NEIGHBORS_MASK) > 0) {
			board[(x + 1) % board.length][(y - 1 + board[0].length)
					% board[0].length] -= 1;
		}
		if ((board[x][(y + 1) % board[0].length] & NEIGHBORS_MASK) > 0) {
			board[x][(y + 1) % board[0].length] -= 1;
		}
		if ((board[x][(y - 1 + board[0].length) % board[0].length] & NEIGHBORS_MASK) > 0) {
			board[x][(y - 1 + board[0].length) % board[0].length] -= 1;
		}
		if ((board[(x - 1 + board.length) % board.length][y] & NEIGHBORS_MASK) > 0) {
			board[(x - 1 + board.length) % board.length][y] -= 1;
		}
		if ((board[(x - 1 + board.length) % board.length][(y + 1)
				% board[0].length] & NEIGHBORS_MASK) > 0) {
			board[(x - 1 + board.length) % board.length][(y + 1)
					% board[0].length] -= 1;
		}
		if ((board[(x - 1 + board.length) % board.length][(y - 1 + board[0].length)
				% board[0].length] & NEIGHBORS_MASK) > 0) {
			board[(x - 1 + board.length) % board.length][(y - 1 + board[0].length)
					% board[0].length] -= 1;
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
		if (numCurrentGenerations > maxGenerations) {
			// restart
			init(myCanvas.getWidth(), myCanvas.getHeight());
		}

		// Draw a black background if this is the first generation
		if (numCurrentGenerations == 0) {
			mPaint.setColor(Color.BLACK);
			myCanvas.drawRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight(), mPaint);
		}

		numCurrentGenerations++;

		// make changes in the changeList and draw them
		for (Entry<Long, Boolean> entry : changeList.entrySet()) {
			int x = (int) (entry.getKey().longValue() >> 32);
			int y = (int) entry.getKey().longValue();
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
		// check each cell in the change list and their neighbors.
		for (Entry<Long, Boolean> entry : changeList.entrySet()) {
			int x = (int) (entry.getKey().longValue() >> 32);
			int y = (int) entry.getKey().longValue();
			checkCell(x, y);
			checkCell((x + 1) % board.length, y);
			checkCell((x + 1) % board.length, (y + 1) % board[0].length);
			checkCell((x + 1) % board.length, (y - 1 + board[0].length)
					% board[0].length);
			checkCell(x, (y + 1) % board[0].length);
			checkCell(x, (y - 1 + board[0].length) % board[0].length);
			checkCell((x - 1 + board.length) % board.length, y);
			checkCell((x - 1 + board.length) % board.length, (y + 1)
					% board[0].length);
			checkCell((x - 1 + board.length) % board.length,
					(y - 1 + board[0].length) % board[0].length);

		}
		// swap the changeLists
		Map<Long, Boolean> temp = changeList;
		changeList = nextChangeList;
		nextChangeList = temp;
		nextChangeList.clear();
		
		canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);

//		for (int x = 0; x < board.length; x++) {
//			for (int y = 0; y < board[x].length; y++) {
//				if ((board[x][y] & GameOfLife.ALIVE_MASK) != 0) {
//					// draw alive cell
//					mPaint.setColor(Color.WHITE);
//					canvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
//							* CELL_WIDTH, (y + 1) * CELL_HEIGHT, mPaint);
//				} else {
//					// draw dead cell
//					mPaint.setColor(Color.BLACK);
//					canvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
//							* CELL_WIDTH, (y + 1) * CELL_HEIGHT, mPaint);
//				}
//			}
//		}
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
	private void checkCell(int x, int y) {
		if ((board[x][y] & ALIVE_MASK) != 0) {
			// cell is alive
			// check if it should die.
			if (!ruleToLive.contains((byte) (board[x][y] & NEIGHBORS_MASK))) {
				// kill the cell in the next generation
				nextChangeList.put((long) x << 32 | y & 0xFFFFFFFFL, false);
			}
		} else {
			// cell is dead
			// check if it should be born
			if (ruleToBeBorn.contains((byte) (board[x][y] & NEIGHBORS_MASK))) {
				// make the cell become alive in the next generation
				nextChangeList.put((long) x << 32 | y & 0xFFFFFFFFL, true);
			}
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeSerializable(board);
		out.writeInt(changeList.size());
		for (Long key : changeList.keySet()) {
			out.writeLong(key);
			out.writeByte((byte) (changeList.get(key) ? 1 : 0));
		}
		out.writeInt(maxGenerations);
		out.writeInt(numCurrentGenerations);
	}

	private void readFromParcel(Parcel in) {
		// TODO change board to be a row major order array.
		board = (byte[][]) in.readSerializable();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			Long key = in.readLong();
			Boolean value = in.readByte() != 0;
			changeList.put(key, value);
		}
		maxGenerations = in.readInt();
		numCurrentGenerations = in.readInt();
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
