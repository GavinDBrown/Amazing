package com.TeamAmazing.Maze;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameOfLife {

	// The number of alive neighbors a cell must have to live between
	// generations.
	private Set<Byte> ruleToLive = new HashSet<Byte>();
	// The number of alive neighbors a cell must have to be born.
	private Set<Byte> ruleToBeBorn = new HashSet<Byte>();
	private List<Integer> changeList = new ArrayList<Integer>();
	private final int NUM_OF_BOARDS = 2;
	private byte[][][] boards;
	private int currentBoard = 0;
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

	public byte[][] getCurrentBoard() {
		return boards[currentBoard];
	}

	public void initializeCells(int width, int height) {
		boards = new byte[NUM_OF_BOARDS][width][height];
		// Create 20 to 30 random starting cells.
		int numOfStartingCells = rand.nextInt(10) + 20;
		int randHorzOffset, randVertOffset;
		while (numOfStartingCells > 0) {
			randHorzOffset = (rand.nextInt(10) + (width / 2 - 5))%width;
			randVertOffset = (rand.nextInt(10) + (height / 2 - 5))%height;
			if ((boards[currentBoard][randHorzOffset][randVertOffset] & ALIVE_MASK) == 0) {
				// cell is dead, make it alive
				makeAlive(randHorzOffset, randVertOffset, currentBoard);
//				changeList.add(randHorzOffset);
//				changeList.add(randVertOffset);
			}
			numOfStartingCells--;
		}
	}

	// a cell should never have more than 8 neighbors so bit 4 should never
	// carry into bit 5, which is the cell state bit.
	private void makeAlive(int x, int y, int boardIndex) {
		boards[boardIndex][x][y] |= ALIVE_MASK;
		// update the neighbors
		boards[boardIndex][(x + 1) % boards[boardIndex].length][y] += 1;
		boards[boardIndex][(x + 1) % boards[boardIndex].length][(y + 1)
				% boards[boardIndex][0].length] += 1;
		boards[boardIndex][(x + 1) % boards[boardIndex].length][(y - 1 + boards[boardIndex][0].length)
				% boards[boardIndex][0].length] += 1;
		boards[boardIndex][x][(y + 1) % boards[boardIndex][0].length] += 1;
		boards[boardIndex][x][(y - 1 + boards[boardIndex][0].length)
				% boards[boardIndex][0].length] += 1;
		boards[boardIndex][(x - 1 + boards[boardIndex].length)
				% boards[boardIndex].length][y] += 1;
		boards[boardIndex][(x - 1 + boards[boardIndex].length)
				% boards[boardIndex].length][(y + 1)
				% boards[boardIndex][0].length] += 1;
		boards[boardIndex][(x - 1 + boards[boardIndex].length)
				% boards[boardIndex].length][(y - 1 + boards[boardIndex][0].length)
				% boards[boardIndex][0].length] += 1;
	}

	private void kill(int x, int y, int boardIndex) {
		boards[boardIndex][x][y] &= ~ALIVE_MASK;
		// update the neighbors
		if ((boards[boardIndex][(x + 1) % boards[boardIndex].length][y] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][(x + 1) % boards[boardIndex].length][y] -= 1;
		}
		if ((boards[boardIndex][(x + 1) % boards[boardIndex].length][(y + 1)
				% boards[boardIndex][0].length] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][(x + 1) % boards[boardIndex].length][(y + 1)
					% boards[boardIndex][0].length] -= 1;
		}
		if ((boards[boardIndex][(x + 1) % boards[boardIndex].length][(y - 1 + boards[boardIndex][0].length)
				% boards[boardIndex][0].length] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][(x + 1) % boards[boardIndex].length][(y - 1 + boards[boardIndex][0].length)
					% boards[boardIndex][0].length] -= 1;
		}
		if ((boards[boardIndex][x][(y + 1) % boards[boardIndex][0].length] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][x][(y + 1) % boards[boardIndex][0].length] -= 1;
		}
		if ((boards[boardIndex][x][(y - 1 + boards[boardIndex][0].length)
				% boards[boardIndex][0].length] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][x][(y - 1 + boards[boardIndex][0].length)
					% boards[boardIndex][0].length] -= 1;
		}
		if ((boards[boardIndex][(x - 1 + boards[boardIndex].length)
				% boards[boardIndex].length][y] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][(x - 1 + boards[boardIndex].length)
					% boards[boardIndex].length][y] -= 1;
		}
		if ((boards[boardIndex][(x - 1 + boards[boardIndex].length)
				% boards[boardIndex].length][(y + 1)
				% boards[boardIndex][0].length] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][(x - 1 + boards[boardIndex].length)
					% boards[boardIndex].length][(y + 1)
					% boards[boardIndex][0].length] -= 1;
		}
		if ((boards[boardIndex][(x - 1 + boards[boardIndex].length)
				% boards[boardIndex].length][(y - 1 + boards[boardIndex][0].length)
				% boards[boardIndex][0].length] & NEIGHBORS_MASK) > 0) {
			boards[boardIndex][(x - 1 + boards[boardIndex].length)
					% boards[boardIndex].length][(y - 1 + boards[boardIndex][0].length)
					% boards[boardIndex][0].length] -= 1;
		}
	}

	public void nextGeneration() {
		changeList.clear();
		int nextBoard = (currentBoard + 1) % NUM_OF_BOARDS;
		// copy over the currentBoard into the next one.
		for (int x = 0; x < boards[currentBoard].length; x++) {
			for (int y = 0; y < boards[currentBoard][x].length; y++) {
				boards[nextBoard][x][y] = boards[currentBoard][x][y];
			}
		}
		// Update the next board based off the state of the current board.
		for (int x = 0; x < boards[currentBoard].length; x++) {
			for (int y = 0; y < boards[currentBoard][x].length; y++) {
				if ((boards[currentBoard][x][y] & ALIVE_MASK) != 0) {
					// cell is alive
					// check if it should die.
					if (!ruleToLive.contains((byte)(boards[currentBoard][x][y]
							& NEIGHBORS_MASK))) {
						// kill the cell in the next generation
						kill(x, y, nextBoard);
						changeList.add(x);
						changeList.add(y);
					}
				} else {
					// cell is dead
					// check if it should be born
					if (ruleToBeBorn.contains((byte)(boards[currentBoard][x][y]
							& NEIGHBORS_MASK))) {
						makeAlive(x, y, nextBoard);
						changeList.add(x);
						changeList.add(y);
					}

				}
			}
		}
		currentBoard = (currentBoard + 1) % NUM_OF_BOARDS;
	}
	
	public List<Integer> getChangeList(){
		return changeList;
	}
}
