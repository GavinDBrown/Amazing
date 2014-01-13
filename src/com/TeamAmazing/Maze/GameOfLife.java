package com.TeamAmazing.Maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

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

	public GameOfLife() {
		setRules();
	}
	private void setRules(){
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

	public void initializeCells(int width, int height) {
		Random rand = new Random();
		board = new byte[width][height];
		changeList.clear();
		// Create 20 to 30 random starting cells.
		int numOfStartingCells = rand.nextInt(11) + 20;
		int randHorzOffset, randVertOffset;
		while (numOfStartingCells > 0) {
			randHorzOffset = (rand.nextInt(10) + (width / 2 - 5)) % width;
			randVertOffset = (rand.nextInt(10) + (height / 2 - 5)) % height;
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
	 * Computes the next generation in the game of life. Uses a list of changes
	 * so not every cell needs to be checked. Also computes the bounds of the
	 * area changed.
	 * 
	 * @return An array containing the bounds of the area changed in the order:
	 *         bottom, left, right, top.
	 */
	public int[] nextGeneration() {
		int bottom = 0;
		int left = board.length;
		int right = 0;
		int top = board[0].length;
		// make changes in the changeList
		for (Entry<Long, Boolean> entry : changeList.entrySet()) {
			int x = (int) (entry.getKey().longValue() >> 32);
			int y = (int) entry.getKey().longValue();
			if (x < left)
				left = x;
			if (x > right)
				right = x;
			if (y < top)
				top = y;
			if (y > bottom)
				bottom = y;
			boolean state = entry.getValue();
			if (state) {
				makeAlive(x, y);
			} else {
				kill(x, y);
			}
		}

		// compute next changes
		// check each cell and their neighbors.
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

		// return the bounding size of the changes made
		return new int[] { bottom, left, right, top };
	}

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
//		out.writeArray(board);
		out.writeInt(changeList.size());
		for (Long key : changeList.keySet()) {
			out.writeLong(key);
			out.writeByte((byte) (changeList.get(key) ? 1 : 0));
		}
	}

	private void readFromParcel(Parcel in) {
		//TODO Do not use serializable because it's slow.
		board = (byte[][]) in.readSerializable();
//		board = (byte[][]) in.readArray(Byte[].class.getClassLoader());
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			Long key = in.readLong();
			Boolean value = in.readByte() != 0;
			changeList.put(key, value);
		}
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
	
	public static final Parcelable.Creator<GameOfLife> CREATOR =
			new Parcelable.Creator<GameOfLife>() {
				public GameOfLife createFromParcel(Parcel in) {
					return new GameOfLife(in);
				}

				public GameOfLife[] newArray(int size) {
					return new GameOfLife[size];
				}
	};
	
	
}
