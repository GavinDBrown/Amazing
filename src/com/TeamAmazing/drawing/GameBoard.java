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

	@Override
	synchronized public void onDraw(Canvas canvas) {
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
			canvas.drawRect(w.getBounds(), p);
		}

		// Check if the sprite has been initialized with a start position.
		if (!(sprite2.x < 0)) {
			// Draws the bitmap, with sprite2.x,y as the center
			canvas.drawBitmap(bm2, sprite2.x - sprite2Bounds.width() / 2,
					sprite2.y - sprite2Bounds.height() / 2, null);
		}
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
		wall.setBounds(new Rect((cell.getCoords().x) * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
				(cell.getCoords().y) * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH, (cell
						.getCoords().x + 1)
						* (CELL_WIDTH + WALL_WIDTH)
						+ BOUNDARY_WIDTH
						+ WALL_WIDTH, (cell.getCoords().y) * (CELL_HEIGHT + WALL_WIDTH)
						+ WALL_WIDTH + BOUNDARY_WIDTH));
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
		wall.setBounds(new Rect((cell.getCoords().x) * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
				(cell.getCoords().y + 1) * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH, (cell
						.getCoords().x + 1)
						* (CELL_WIDTH + WALL_WIDTH)
						+ BOUNDARY_WIDTH
						+ WALL_WIDTH, (cell.getCoords().y + 1) * (CELL_HEIGHT + WALL_WIDTH)
						+ WALL_WIDTH + BOUNDARY_WIDTH));
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
		wall.setBounds(new Rect(cell.getCoords().x * (CELL_WIDTH + WALL_WIDTH) + BOUNDARY_WIDTH,
				cell.getCoords().y * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH,
				cell.getCoords().x * (CELL_WIDTH + WALL_WIDTH) + WALL_WIDTH + BOUNDARY_WIDTH, (cell
						.getCoords().y + 1)
						* (CELL_HEIGHT + WALL_WIDTH)
						+ BOUNDARY_WIDTH
						+ WALL_WIDTH));
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
		wall.setBounds(new Rect((cell.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH)
				+ BOUNDARY_WIDTH, cell.getCoords().y * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH,
				(cell.getCoords().x + 1) * (CELL_WIDTH + WALL_WIDTH) + WALL_WIDTH + BOUNDARY_WIDTH,
				(cell.getCoords().y + 1) * (CELL_HEIGHT + WALL_WIDTH) + BOUNDARY_WIDTH + WALL_WIDTH));
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
		boolean topLeftDone = false;
		boolean topRightDone = false;
		boolean bottomLeftDone = false;
		boolean bottomRightDone = false;
		for (Wall w : maze.getWalls()) {
			Cell cell1 = w.getV1();
			Cell cell2 = w.getV2();
			if (cell1 != null && cell2 != null) {
				// Cells on the inside.
				if (cell1.getCoords().x == cell2.getCoords().x) {
					// Vertical inside cells => horizontal wall below.
					setWallBoundsBelowCell(w, cell1);
				} else {
					// Horizontal inside cells => vertical wall to the right.
					setWallBoundsRightCell(w, cell1);
				}
			} else {
				// Cells on the boundary
				if (cell1 != null) {
					if (cell1.getCoords().x == 0) {
						if (cell1.getCoords().y == 0 && !topLeftDone) {
							// Cell is on the top left => horizontal wall above
							// it.
							setWallBoundsAboveCell(w, cell1);
							// Set the flag topLeftDone so the second wall with
							// cells at (0,0) and null will be drawn on the
							// left.
							topLeftDone = true;
						} else if (cell1.getCoords().y == maze.getHeight() - 1 && !bottomLeftDone) {
							// Cell is on the bottom left => horizontal wall
							// below it.
							setWallBoundsBelowCell(w, cell1);
							// Set the flag bottomLeftDone so the second wall
							// with cells at (0, maze.getHeight() - 1) and null
							// will be drawn on the left.
							bottomLeftDone = true;
						} else {
							// Cell is on the left => vertical wall to
							// the left of it.
							setWallBoundsLeftCell(w, cell1);
						}
					} else if (cell1.getCoords().x == maze.getWidth() - 1) {
						if (cell1.getCoords().y == 0 && !topRightDone) {
							// Cell is on the top right => horizontal wall above
							// it.
							setWallBoundsAboveCell(w, cell1);
							// Set the flag topRightDone so the second wall with
							// cells at (0, maze.getHeight() - 1) and null will
							// be drawn on the right.
							topRightDone = true;
						} else if (cell1.getCoords().y == maze.getHeight() - 1 && !bottomRightDone) {
							// Cell is on the bottom right => horizontal wall
							// below it.
							setWallBoundsBelowCell(w, cell1);
							// Set the flag bottomRightDone so the second wall
							// with cells at (maze.getWidth()-1,
							// maze.getHeight() - 1) and null will be drawn on
							// the right.
							bottomRightDone = true;
						} else {
							// Cell is on the right => vertical wall to the
							// right of it.
							setWallBoundsRightCell(w, cell1);
						}
					} else if (cell1.getCoords().y == 0) {
						// Cell is on the top => horizontal wall above
						// it.
						setWallBoundsAboveCell(w, cell1);
					} else if (cell1.getCoords().y == maze.getHeight() - 1) {
						// Cell is on the bottom => horizontal wall below
						// it.
						setWallBoundsBelowCell(w, cell1);
					}
				} else {
					// Only the second cell of a wall should ever be null, but
					// this is included for future usability.
					if (cell2.getCoords().x == 0) {
						if (cell2.getCoords().y == 0 && !topLeftDone) {
							// Cell is on the top left => horizontal wall above
							// it.
							setWallBoundsAboveCell(w, cell2);
							// Set the flag topLeftDone so the second wall with
							// cells at (0,0) and null will be drawn on the
							// left.
							topLeftDone = true;
						} else if (cell2.getCoords().y == maze.getHeight() - 1 && !bottomLeftDone) {
							// Cell is on the bottom left => horizontal wall
							// below it.
							setWallBoundsBelowCell(w, cell2);
							// Set the flag bottomLeftDone so the second wall
							// with cells at (0, maze.getHeight() - 1) and null
							// will be drawn on the left.
							bottomLeftDone = true;
						} else {
							// Cell is on the left => vertical wall to
							// the left of it.
							setWallBoundsLeftCell(w, cell2);
						}
					} else if (cell2.getCoords().x == maze.getWidth() - 1) {
						if (cell2.getCoords().y == 0 && !topRightDone) {
							// Cell is on the top right => horizontal wall above
							// it.
							setWallBoundsAboveCell(w, cell2);
							// Set the flag topRightDone so the second wall with
							// cells at (0, maze.getHeight() - 1) and null will
							// be drawn on the right.
							topRightDone = true;
						} else if (cell2.getCoords().y == maze.getHeight() - 1 && !bottomRightDone) {
							// Cell is on the bottom right => horizontal wall
							// below it.
							setWallBoundsBelowCell(w, cell2);
							// Set the flag bottomRightDone so the second wall
							// with cells at (maze.getWidth()-1,
							// maze.getHeight() - 1) and null will be drawn on
							// the right.
							bottomRightDone = true;
						} else {
							// Cell is on the right => vertical wall to the
							// right of it.
							setWallBoundsRightCell(w, cell2);
						}
					} else if (cell2.getCoords().y == 0) {
						// Cell is on the top => horizontal wall above
						// it.
						setWallBoundsAboveCell(w, cell2);
					} else if (cell2.getCoords().y == maze.getHeight() - 1) {
						// Cell is on the bottom => horizontal wall below
						// it.
						setWallBoundsBelowCell(w, cell2);
					}
				}

			}
		}
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
