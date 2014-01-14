package com.TeamAmazing.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.TeamAmazing.Maze.GameOfLife;

public class GameOfLifeBackground extends View {
	private Paint p;

	private byte[][] board;

	// The width and height of maze cells in pixels.
	public static final int CELL_WIDTH = 10;
	public static final int CELL_HEIGHT = 10;

	public GameOfLifeBackground(Context context, AttributeSet aSet) {
		super(context, aSet);
		p = new Paint();
	}

	public void setBoard(byte[][] newBoard) {
		this.board = newBoard;
	}

	public Rect getRectOf(int x, int y) {
		return new Rect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1) * CELL_WIDTH,
				(y + 1) * CELL_HEIGHT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		p.setStyle(Paint.Style.FILL);
		p.setAlpha(255);

		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if ((board[x][y] & GameOfLife.ALIVE_MASK) != 0) {
					// draw black cell
					p.setColor(Color.BLACK);
					canvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
							* CELL_WIDTH, (y + 1) * CELL_HEIGHT, p);
				} else {
					// draw white cell
					p.setColor(Color.WHITE);
					canvas.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, (x + 1)
							* CELL_WIDTH, (y + 1) * CELL_HEIGHT, p);
				}
			}
		}

	}

	/**
	 * 
	 * Invalidates the part of the view specified by the cells described in bounds.
	 * 
	 * @param bounds
	 *            The bounds of the area to invalidate in the order: bottom,
	 *            left, right, top.
	 */
	public void invalidateAreaOf(int[] bounds) {
		Rect r = getRectOf(bounds[1], bounds[3]);
		r.union(getRectOf(bounds[2], bounds[0]));
		invalidate(r);

	}
}
