package com.TeamAmazing.Maze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class GameOfLife {

	// The number of alive neighbors a cell must have to live between
	// generations.
	private Set<Byte> ruleToLive = new HashSet<Byte>();
	// The number of alive neighbors a cell must have to be born.
	private Set<Byte> ruleToBeBorn = new HashSet<Byte>();
	private Map<Long, Boolean> changeList = new HashMap<Long, Boolean>();
	private Map<Long, Boolean> nextChangeList = new HashMap<Long, Boolean>();
	// private List<Integer> nextChangeList = new ArrayList<Integer>();
	private byte[][] board;
	Random rand = new Random();
	public static final byte ALIVE_MASK = 16;
	private static final byte NEIGHBORS_MASK = 15;

	public GameOfLife() {
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
		board = new byte[width][height];
		changeList.clear();
		// Create 20 to 30 random starting cells.
		int numOfStartingCells = rand.nextInt(10) + 20;
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
}
