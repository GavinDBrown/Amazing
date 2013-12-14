package com.TeamAmazing.drawing;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class StartMenuBackground extends View {
	public boolean restarting = false;
	private Paint p;
	Random rand = new Random();
	private int[][] cells;
	private ArrayDeque<int[][]> previousConfigs;
	// The width and height of the grid of cells. NOT the width and height of
	// the canvas.
	private static int GridWidth = 0;
	private static int GridHeight = 0;
	// The width and height of maze cells in pixels.
	public static final int CELL_WIDTH = 20;
	public static final int CELL_HEIGHT = 20;

	public StartMenuBackground(Context context, AttributeSet aSet) {
		super(context, aSet);
		p = new Paint();
		previousConfigs = new ArrayDeque<int[][]>(4);
	}

	@Override
	public void onDraw(Canvas canvas) {
		p.setStyle(Paint.Style.FILL);
		p.setAlpha(255);
		p.setColor(Color.WHITE);
		canvas.drawRect(0, 0, getWidth(), getHeight(), p);

		// draw the cells.
		p.setColor(Color.BLACK);
		for (int x = 0; x < cells.length; x++) {
			for (int y = 0; y < cells[x].length; y++) {
				if (cells[x][y] == 1) {
					p.setColor(Color.BLACK);
					canvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
							* CELL_WIDTH, (y + 1) * CELL_HEIGHT, p);
				} else {
					p.setColor(Color.WHITE);
					canvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
							* CELL_WIDTH, (y + 1) * CELL_HEIGHT, p);
				}
			}
		}
	}

	// TODO Make the buttons fully opaque. This should be done in the XML files,
	// just not sure how ATM.
	public void initializeCells() {
		GridWidth = this.getWidth() / CELL_WIDTH;
		GridHeight = this.getHeight() / CELL_HEIGHT;
		cells = new int[GridWidth][GridHeight];
		// TODO Respond appropriately when GridWidth or GridHeight is less than
		// 10.
		int horzOffset = GridWidth / 2 - 5;
		int vertOffset = GridHeight / 2 - 5;
		// start with 20 to 30 cells
		int numOfStartingCells = rand.nextInt(10) + 20;
		int randXOffset, randYOffset;
		while (numOfStartingCells > 0) {
			randXOffset = rand.nextInt(10);
			randYOffset = rand.nextInt(10);
			cells[horzOffset + randXOffset][vertOffset + randYOffset] = 1;
			numOfStartingCells--;
		}
	}

	public void nextGeneration() {
		int[][] nextGeneration = new int[GridWidth][GridHeight];
		// The number of alive neighbors a cell must have to live between
		// generations.
		Set<Integer> ruleToLive = new HashSet<Integer>();
		ruleToLive.add(1);
		ruleToLive.add(2);
		ruleToLive.add(3);
		ruleToLive.add(4);
		// ruleToLive.add(5);
		// The number of alive neighbors a cell must have to be born.
		Set<Integer> ruleToBeBorn = new HashSet<Integer>();
		ruleToBeBorn.add(3);
		for (int x = 0; x < cells.length; x++) {
			for (int y = 0; y < cells[x].length; y++) {
				if (cells[x][y] == 1) {
					// cell is alive, check to see if it will remain alive.
					if (!ruleToLive.contains(numOfAliveNeighborsAt(x, y))) {
						// kill the cell
						nextGeneration[x][y] = 0;
					} else {
						// let it live
						nextGeneration[x][y] = 1;
					}
				} else {
					// cell is dead, check it see if it should be born.
					if (ruleToBeBorn.contains(numOfAliveNeighborsAt(x, y))) {
						// Bring the cell to life.
						nextGeneration[x][y] = 1;
					} else {
						// leave it dead
						nextGeneration[x][y] = 0;
					}
				}
			}
		}
		// Restart if the nextGeneration is the same as any of the past 3
		// previous ones.
		while (previousConfigs.size() > 3)
			previousConfigs.removeFirst();
		previousConfigs.addLast(cells);
		for (int[][] config : previousConfigs) {
			if (Arrays.deepEquals(nextGeneration, config)) {
				restarting = true;
				return;
			}
		}
		cells = nextGeneration;
	}

	private int numOfAliveNeighborsAt(int x, int y) {
		int result = 0;
		if (x > 0) {
			// add the cell to the left.
			result += cells[x - 1][y];
			if (y > 0) {
				// add the top left cell.
				result += cells[x - 1][y - 1];
			}
			if (y < cells[x].length - 1) {
				// add the cell to the bottom left.
				result += cells[x - 1][y + 1];
			}
		}
		if (x < cells.length - 1) {
			// add the cell to the right
			result += cells[x + 1][y];
			if (y > 0) {
				// add the top right cell.
				result += cells[x + 1][y - 1];
			}
			if (y < cells[x].length - 1) {
				// add the cell to the bottom right.
				result += cells[x + 1][y + 1];
			}
		}
		if (y > 0) {
			// add the top cell
			result += cells[x][y - 1];
		}
		if (y < cells[x].length - 1) {
			// add the bottom cell
			result += cells[x][y + 1];
		}
		return result;
	}
}
