//TODO Add collision detection for the maze.
package com.TeamAmazing.drawing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.TeamAmazing.game.Maze;
import com.TeamAmazing.game.R;
import com.TeamAmazing.game.Maze.Cell;
import com.TeamAmazing.game.Maze.Wall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameBoard extends View {

	private Paint p;
	private List<Point> starField = null;
	public Maze maze = null;
	private int starAlpha = 80;
	private int starFade = 2;
	private Rect sprite2Bounds = new Rect(0, 0, 0, 0);
	private Point sprite2;
	private Bitmap bm2 = null;
	private boolean isAccelerating = false;
	private float xTouch;
	private float yTouch;

	private static final int NUM_OF_STARS = 25;
	// The width of a maze cell in pixels.
	private static final int CELL_WIDTH = 30;
	// The height of a maze cell in pixels.
	private static final int CELL_HEIGHT = 30;
	private static final int WALL_WIDTH = 5;
	private static final int BOUNDARY_WIDTH = 20;

	// Getters and setters.

	synchronized public void setSprite2(int x, int y) {
		sprite2 = new Point(x, y);
	}

	synchronized public void setSprite2(Point p) {
		sprite2 = p;
	}

	synchronized public int getSprite2X() {
		return sprite2.x;
	}

	synchronized public int getSprite2Y() {
		return sprite2.y;
	}

	synchronized public int getSprite2Width() {
		return sprite2Bounds.width();
	}

	synchronized public int getSprite2Height() {
		return sprite2Bounds.height();
	}

	synchronized public Point getSpite2() {
		return sprite2;
	}

	synchronized public void resetStarField() {
		starField = null;
	}

	synchronized public boolean isAccelerating() {
		return isAccelerating;
	}

	synchronized public void resetMaze() {
		maze = null;
	}

	synchronized public float getYTouch() {
		return yTouch;
	}

	synchronized public float getXTouch() {
		return xTouch;
	}

	public GameBoard(Context context, AttributeSet aSet) {
		super(context, aSet);
		p = new Paint();
		// load our bitmap and set the bounds for the controller
		sprite2 = new Point(-1, -1);
		// Define a matrix so we can rotate the asteroid
		bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.ufo);
		sprite2Bounds = new Rect(0, 0, bm2.getWidth(), bm2.getHeight());
	}

	synchronized private void initializeStars(int maxX, int maxY) {
		starField = new ArrayList<Point>();
		for (int i = 0; i < NUM_OF_STARS; i++) {
			Random r = new Random();
			int x = r.nextInt(maxX - 5 + 1) + 5;
			int y = r.nextInt(maxY - 5 + 1) + 5;
			starField.add(new Point(x, y));
		}
	}

	private void drawBoundaryCell(Cell cell, Canvas canvas) {
		if (cell.getCoords().x == 0) {
			// Cell is on the left so draw a vertical wall to
			// the left of it.
			canvas.drawRect(BOUNDARY_WIDTH, cell.getCoords().y * (CELL_HEIGHT + WALL_WIDTH)
					+ BOUNDARY_WIDTH, BOUNDARY_WIDTH + WALL_WIDTH, (cell.getCoords().y + 1)
					* (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH + WALL_WIDTH, p);
		}
		if (cell.getCoords().x == maze.getWidth() - 1) {
			// Cell is on the right so draw a vertical wall to the
			// right of it.
			canvas.drawRect(maze.getWidth() * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
					cell.getCoords().y * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH,
					maze.getWidth() * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH + WALL_WIDTH,
					(cell.getCoords().y + 1) * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH
							+ WALL_WIDTH, p);
		}
		if (cell.getCoords().y == 0) {
			// Cell is on the top so draw a horizontal wall above
			// it.
			canvas.drawRect((cell.getCoords().x) * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
					BOUNDARY_WIDTH, (cell.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH)
							+ BOUNDARY_WIDTH + WALL_WIDTH, WALL_WIDTH + BOUNDARY_WIDTH, p);

		}
		if (cell.getCoords().y == maze.getHeight() - 1) {
			// Cell is on the bottom so draw a horizontal wall below
			// it.
			// canvas.drawRect(left, top, right, bottom, paint)
			canvas.drawRect((cell.getCoords().x) * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
					maze.getHeight() * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
					(cell.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH
							+ WALL_WIDTH, maze.getHeight() * (CELL_WIDTH + WALL_WIDTH) + WALL_WIDTH
							+ BOUNDARY_WIDTH, p);

		}
	}

	// TODO calculate a cell's Rect once, and save that information.

	@Override
	synchronized public void onDraw(Canvas canvas) {
		// Draw a border around the maze
		// p.setStyle(Paint.Style.STROKE);
		// p.setStrokeWidth(BOUNDARY_WIDTH);
		// p.setColor(Color.GREEN);
		// canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);

		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.BLACK);
		p.setAlpha(255);
		p.setStrokeWidth(1);
		canvas.drawRect(0, 0, getWidth(), getHeight(), p);

		if (starField == null) {
			initializeStars(canvas.getWidth(), canvas.getHeight());
		}
		if (maze == null) {
			initializeMaze(canvas.getWidth(), canvas.getHeight());
		}

		p.setColor(Color.CYAN);
		p.setAlpha(starAlpha += starFade);
		if (starAlpha >= 252 || starAlpha <= 80)
			starFade = starFade * -1;
		p.setStrokeWidth(5);
		for (int i = 0; i < NUM_OF_STARS; i++) {
			canvas.drawPoint(starField.get(i).x, starField.get(i).y, p);
		}
		// Draws the maze
		p.setColor(Color.MAGENTA);
		for (Wall w : maze.getWalls()) {
			Cell cell1 = w.getV1();
			Cell cell2 = w.getV2();
			if (cell1 != null && cell2 != null) {
				// Draw the cells on the inside.
				if (cell1.getCoords().x == cell2.getCoords().x) {
					// Vertical cells, so I want to draw a horizontal wall
					canvas.drawRect((cell1.getCoords().x) * (CELL_WIDTH + WALL_WIDTH)
							+ BOUNDARY_WIDTH, (cell1.getCoords().y + 1)
							* (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH,
							(cell1.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH
									+ WALL_WIDTH, (cell1.getCoords().y + 1)
									* (CELL_HEIGHT + WALL_WIDTH) + WALL_WIDTH + BOUNDARY_WIDTH, p);

				} else {
					// Horizontal cells, so I want to draw a vertical wall.
					canvas.drawRect((cell1.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH)
							+ BOUNDARY_WIDTH, cell1.getCoords().y * (CELL_HEIGHT + WALL_WIDTH)
							+ BOUNDARY_WIDTH, (cell1.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH)
							+ WALL_WIDTH + BOUNDARY_WIDTH, (cell1.getCoords().y + 1)
							* (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH + WALL_WIDTH, p);
				}

			} else {
				// Draw the cells on the boundary
				if (cell1 != null) {
					drawBoundaryCell(cell1, canvas);
				} else {
					drawBoundaryCell(cell2, canvas);
				}

			}
		}

		// Check if the sprite has been initialized with a start position.
		if (!(sprite2.x < 0)) {
			// Draws the bitmap, with sprite2.x,y as the center
			canvas.drawBitmap(bm2, sprite2.x - sprite2Bounds.width() / 2,
					sprite2.y - sprite2Bounds.height() / 2, null);
		}
	}

	/**
	 * 
	 * @param width
	 *            The width of the canvas the maze is drawn on.
	 * @param height
	 *            The height of the canvas the maze is drawn on.
	 */
	private void initializeMaze(int width, int height) {
		maze = new Maze((width - 2 * BOUNDARY_WIDTH) / (CELL_WIDTH + WALL_WIDTH),
				(height - 2 * BOUNDARY_WIDTH) / (CELL_HEIGHT + WALL_WIDTH));
		maze.makePerfectMaze();
	}

	@Override
	synchronized public boolean onTouchEvent(MotionEvent ev) {
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
