package com.TeamAmazing.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.TextView;

import com.TeamAmazing.drawing.GameBoard;
import com.TeamAmazing.game.Maze.Cell;
import com.TeamAmazing.game.Maze.Wall;

public class Main extends Activity implements OnClickListener {
	private static final float PREVIOUS_VELOCITY_FAC = .49f;
	private static final float TOUCH_FACTOR = .10f;
	private static final float FRICTION = .05f;
	private float sprite2XVelocity = 0;
	private float sprite2YVelocity = 0;
	private float xFriction = 0;
	private float yFriction = 0;
	private static final int MAX_SPEED = 10;
	private static final float REBOUND_FAC = .5f;

	private Handler frame = new Handler();
	// The delay in milliseconds between frame updates
	private static final int FRAME_DELAY = 17; // 17 => about 59 frames per
												// second

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		((Button) findViewById(R.id.the_button)).setOnClickListener(this);
		initGfx();
	}

	synchronized public void initGfx() {
		final GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		ViewTreeObserver vto = gb.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {
				// Initialize stuff that is dependent on the view already having
				// been measured.
				gb.maze = initializeMaze(gb.getWidth(), gb.getHeight());
				Rect startCellRect = calculateCellRect(gb.maze.getCell(Maze.START_CELL));
				// startPos.x = startPos.x * (GameBoard.CELL_WIDTH +
				// GameBoard.WALL_WIDTH)
				// + GameBoard.BOUNDARY_WIDTH + GameBoard.CELL_WIDTH / 2;
				// startPos.y = startPos.y * (GameBoard.CELL_HEIGHT +
				// GameBoard.WALL_WIDTH)
				// + GameBoard.BOUNDARY_WIDTH + GameBoard.CELL_HEIGHT / 2;
				gb.setSprite2(startCellRect.centerX(), startCellRect.centerY());

				ViewTreeObserver obs = gb.getViewTreeObserver();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}

		});
		gb.resetStarField();
		resetSprite2Velocity();
		((Button) findViewById(R.id.the_button)).setEnabled(true);
		frame.removeCallbacksAndMessages(frameUpdate);
		((GameBoard) findViewById(R.id.the_canvas)).invalidate();
		frame.postDelayed(frameUpdate, FRAME_DELAY);
	}

	public static Rect calculateCellRect(Cell cell) {
		return new Rect(cell.getCoords().x * (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH, cell.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH) + GameBoard.WALL_WIDTH
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH,
				(cell.getCoords().y + 1) * (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
						+ GameBoard.BOUNDARY_WIDTH);
	}

	@Override
	// Runs when the reset button is clicked.
	synchronized public void onClick(View v) {
		resetGame();
	}

	private void resetGame() {
		frame.removeCallbacksAndMessages(null);
		initGfx();
	}

	/**
	 * Set sprite2 Velocity's and Friction to 0.
	 */
	private void resetSprite2Velocity() {
		sprite2XVelocity = 0;
		sprite2YVelocity = 0;
		xFriction = 0;
		yFriction = 0;
	}

	/**
	 * Update the velocity of objects on the gameboard.
	 */
	public void updateVelocity() {
		GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		if (gb.isAccelerating()) {
			float xTouch = gb.getXTouch();
			float yTouch = gb.getYTouch();
			Point sprite2 = gb.getSpite2();
			sprite2XVelocity = TOUCH_FACTOR
					* (xTouch - sprite2.x + Math.round(PREVIOUS_VELOCITY_FAC * sprite2XVelocity));
			sprite2YVelocity = TOUCH_FACTOR
					* (yTouch - sprite2.y + Math.round(PREVIOUS_VELOCITY_FAC * sprite2YVelocity));
			// Enforce max speed;
			int accSpeed = (int) Math.round(Math.sqrt(Math.pow(sprite2XVelocity, 2)
					+ Math.pow(sprite2YVelocity, 2)));
			if (accSpeed > MAX_SPEED + 1) {
				sprite2XVelocity = sprite2XVelocity * MAX_SPEED / accSpeed;
				sprite2YVelocity = sprite2YVelocity * MAX_SPEED / accSpeed;
			}
		} else {
			// Decrease speed with friction.
			float speed = (float) Math.sqrt(Math.pow(sprite2XVelocity, 2)
					+ Math.pow(sprite2YVelocity, 2));
			if ((Math.abs(sprite2XVelocity) + Math.abs(sprite2YVelocity)) > 0) {
				xFriction = speed * FRICTION * -1 * sprite2XVelocity
						/ (Math.abs(sprite2XVelocity) + Math.abs(sprite2YVelocity));
				yFriction = speed * FRICTION * -1 * sprite2YVelocity
						/ (Math.abs(sprite2XVelocity) + Math.abs(sprite2YVelocity));
			}
			sprite2XVelocity = sprite2XVelocity + xFriction;
			sprite2YVelocity = sprite2YVelocity + yFriction;
		}
	}

	// TODO add boundary checking for the walls.
	/**
	 * Update the position of the objects on the gameboard.
	 */
	public void updatePosition() {
		final GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		Point vel = new Point(Math.round(sprite2XVelocity), Math.round(sprite2YVelocity));
		while (Math.abs(vel.x) > 0 || Math.abs(vel.y) > 0) {
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

		// Check if we are in the end cell.
		// TODO NOTE, having the check here means that if we bounce into and out
		// of the cell in one frame that it won't count.
		if (calculateCellRect(gb.maze.getCell(Maze.END_CELL)).contains(gb.getSpite2().x,
				gb.getSpite2().y)) {
			// Sprite is inside the end cell.
			resetGame();
		}
	}
	// TODO update to use pixel perfect collision detection
	private boolean wallsIntersects(int left, int top, int right, int bottom) {
		for (Wall w : ((GameBoard) findViewById(R.id.the_canvas)).maze.getWalls()) {
			if (w.getBounds().intersects(left, top, right, bottom))
				return true;
		}
		return false;
	}

	private void takeNStepsInYDirection(Point vel, int n) {
		final GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		Point sprite2 = gb.getSpite2();
		while (n > 0) {
			// Take a steps along the yVel vector, making decisions as we go.
			if (vel.y > 0) {
				if (sprite2.y + 1 > gb.getHeight() - gb.getSprite2Height() / 2
						|| wallsIntersects(
								sprite2.x - gb.getSprite2Width() / 2,
								sprite2.y + 1 - gb.getSprite2Height() / 2,
								sprite2.x + gb.getSprite2Width() / 2, 
								sprite2.y + 1 + gb.getSprite2Height() / 2)) {
					// Rebound
					sprite2.y -= 1;
					vel.y *= -1 * REBOUND_FAC;
					sprite2YVelocity *= -1 * REBOUND_FAC;
					yFriction *= -1 * REBOUND_FAC;
				} else {
					sprite2.y += 1;
				}
				vel.y--;
			} else {
				if (sprite2.y - 1 < gb.getSprite2Height() / 2
						|| wallsIntersects(
								sprite2.x - gb.getSprite2Width() / 2,
								sprite2.y - 1 - gb.getSprite2Height() / 2,
								sprite2.x + gb.getSprite2Width() / 2, 
								sprite2.y - 1 + gb.getSprite2Height() / 2)) {
					// Rebound
					sprite2.y += 1;
					vel.y *= -1 * REBOUND_FAC;
					sprite2YVelocity *= -1 * REBOUND_FAC;
					yFriction *= -1 * REBOUND_FAC;
				} else {
					sprite2.y -= 1;
				}
				vel.y++;
			}
			n--;
		}
	}

	private void takeNStepsInXDirection(Point vel, int n) {
		final GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		Point sprite2 = gb.getSpite2();
		while (n > 0) {
			// Take a steps along the xVel vector, making decisions as we go.
			if (vel.x > 0) {
				if (sprite2.x + 1 > gb.getWidth() - gb.getSprite2Width() / 2
						|| wallsIntersects(
								sprite2.x +1- gb.getSprite2Width() / 2,
								sprite2.y  - gb.getSprite2Height() / 2,
								sprite2.x + 1+gb.getSprite2Width() / 2, 
								sprite2.y + gb.getSprite2Height() / 2)) {
					// Rebound
					sprite2.x -= 1;
					vel.x *= -1 * REBOUND_FAC;
					sprite2XVelocity *= -1 * REBOUND_FAC;
					xFriction *= -1 * REBOUND_FAC;
				} else {
					sprite2.x += 1;
				}
				vel.x--;
			} else {
				if (sprite2.x - 1 < gb.getSprite2Width() / 2
						|| wallsIntersects(
								sprite2.x -1- gb.getSprite2Width() / 2,
								sprite2.y  - gb.getSprite2Height() / 2,
								sprite2.x -1+ gb.getSprite2Width() / 2, 
								sprite2.y + gb.getSprite2Height() / 2)) {
					// Rebound
					sprite2.x += 1;
					vel.x *= -1 * REBOUND_FAC;
					sprite2XVelocity *= -1 * REBOUND_FAC;
					xFriction *= -1 * REBOUND_FAC;
				} else {
					sprite2.x -= 1;
				}
				vel.x++;
			}
			n--;
		}
	}

	/**
	 * Update the gameboard every FRAME_DELAY milliseconds. Updates the velocity
	 * and position and then redraws the canvas.
	 */
	private Runnable frameUpdate = new Runnable() {
		@Override
		synchronized public void run() {
			frame.removeCallbacksAndMessages(frameUpdate);

			// Increase or decrease velocity based on touch information.
			updateVelocity();

			// Update position with boundary checking
			updatePosition();

			// Display Velocity and Friction information
			float speed = (float) Math.sqrt(Math.pow(sprite2XVelocity, 2)
					+ Math.pow(sprite2YVelocity, 2));
			((TextView) findViewById(R.id.the_label)).setText("Velocity ("
					+ String.format("%.2f", sprite2XVelocity) + ","
					+ String.format("%.2f", sprite2YVelocity) + ")");
			((TextView) findViewById(R.id.the_other_label)).setText("Friction ("
					+ String.format("%.2f", xFriction) + "," + String.format("%.2f", yFriction)
					+ ")");
			((TextView) findViewById(R.id.the_third_label)).setText("Speed ("
					+ String.format("%.2f", speed) + ")");

			// Redraw the canvas
			((GameBoard) findViewById(R.id.the_canvas)).invalidate();

			// Loop, after FRAME_DELAY milliseconds.
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		}

	};

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
		wall.setBounds(new Rect((cell.getCoords().x)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH, (cell
				.getCoords().y)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH
				+ GameBoard.WALL_WIDTH, cell.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH) + GameBoard.WALL_WIDTH
				+ GameBoard.BOUNDARY_WIDTH));
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
		wall.setBounds(new Rect((cell.getCoords().x)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH, (cell
				.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH
				+ GameBoard.WALL_WIDTH, (cell.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH) + GameBoard.WALL_WIDTH
				+ GameBoard.BOUNDARY_WIDTH));
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
		wall.setBounds(new Rect(cell.getCoords().x * (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, cell.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH, cell
				.getCoords().x
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH, (cell.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH
				+ GameBoard.WALL_WIDTH));
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
		wall.setBounds(new Rect((cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH, cell
				.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH) + GameBoard.WALL_WIDTH
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH) + GameBoard.BOUNDARY_WIDTH
				+ GameBoard.WALL_WIDTH));
	}

	/**
	 * Set the bounds for a wall next to a cell on the boundary of the maze.
	 * 
	 * @param wall
	 *            　The wall to have it's bounds set.
	 * @param cell
	 *            　The cell that the wall is next to.
	 * @param corners
	 *            　Boolean flags that determine the behavior of how corner cell
	 *            walls are drawn.
	 * @param mazeWidth
	 *            　How many cells are in one row of the maze.
	 * @param mazeHeight
	 *            How many cells are in one column of the maze.
	 */
	private void setWallBoundsBoundaryCell(Wall wall, Cell cell, boolean[] corners, int mazeWidth,
			int mazeHeight) {
		if (cell.getCoords().x == 0) {
			// TopLeft, TopRight, BottomLeft, BottomRight
			if (cell.getCoords().y == 0 && !corners[0]) {
				// Cell is on the top left => horizontal wall above
				// it.
				setWallBoundsAboveCell(wall, cell);
				// Set the flag topLeft so the second wall with
				// cells at (0,0) and null will be drawn on the
				// left.
				corners[0] = true;
			} else if (cell.getCoords().y == mazeHeight - 1 && !corners[2]) {
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
		} else if (cell.getCoords().x == mazeWidth - 1) {
			if (cell.getCoords().y == 0 && !corners[1]) {
				// Cell is on the top right => horizontal wall above
				// it.
				setWallBoundsAboveCell(wall, cell);
				// Set the flag topRight so the second wall with
				// cells at (0, maze.getHeight() - 1) and null will
				// be drawn on the right.
				corners[1] = true;
			} else if (cell.getCoords().y == mazeHeight - 1 && !corners[3]) {
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
		} else if (cell.getCoords().y == 0) {
			// Cell is on the top => horizontal wall above
			// it.
			setWallBoundsAboveCell(wall, cell);
		} else if (cell.getCoords().y == mazeHeight - 1) {
			// Cell is on the bottom => horizontal wall below
			// it.
			setWallBoundsBelowCell(wall, cell);
		}
	}

	/**
	 * Creates a Maze object and initializes it with a random perfect maze.
	 * 
	 * @param canvasWidth
	 *            The width of the canvas the maze is drawn on.
	 * @param canvasHeight
	 *            The height of the canvas the maze is drawn on.
	 * @return The initialized Maze object.
	 */
	private Maze initializeMaze(int canvasWidth, int canvasHeight) {
		Maze maze = new Maze((canvasWidth - 2 * GameBoard.BOUNDARY_WIDTH)
				/ (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH),
				(canvasHeight - 2 * GameBoard.BOUNDARY_WIDTH)
						/ (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH));
		maze.makePerfectMaze();
		// Set the start and finish cells.
		maze.getCells()[0][0].setType(Maze.START_CELL);
		maze.getCells()[maze.getWidth() - 1][maze.getHeight() - 1].setType(Maze.END_CELL);
		// Each corner will have two indistinguishable walls that need have
		// different orientations so set a boolean flag for each corner.
		// topLeft, topRight, bottomLeft, bottomRight
		boolean[] corners = { false, false, false, false };
		for (Wall w : maze.getWalls()) {
			Cell cell1 = w.getV1();
			Cell cell2 = w.getV2();
			if (cell1 != null && cell2 != null) {
				// Cells on the inside.
				if (cell1.getCoords().x == cell2.getCoords().x) {
					// Vertical inside cells => horizontal wall below cell1.
					setWallBoundsBelowCell(w, cell1);
				} else {
					// Horizontal inside cells => vertical wall to the right of
					// cell1.
					setWallBoundsRightCell(w, cell1);
				}
			} else {
				// Cells on the boundary
				if (cell1 != null) {
					setWallBoundsBoundaryCell(w, cell1, corners, maze.getWidth(), maze.getHeight());
				} else {
					// The first cell of a wall should never be null, but
					// this is included for future usability.
					setWallBoundsBoundaryCell(w, cell2, corners, maze.getWidth(), maze.getHeight());
				}

			}
		}
		return maze;
	}

}
