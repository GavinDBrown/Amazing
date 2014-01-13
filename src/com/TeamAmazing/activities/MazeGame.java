package com.TeamAmazing.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.TeamAmazing.Maze.Cell;
import com.TeamAmazing.Maze.Maze;
import com.TeamAmazing.Maze.Wall;
import com.TeamAmazing.drawing.GameBoard;
import com.TeamAmazing.game.R;

public class MazeGame extends Activity {
	private int mazeType;

	private static final float PREVIOUS_VELOCITY_FAC = .49f;
	private static final float TOUCH_FACTOR = .10f;
	private static final float FRICTION = .05f;
	private static final String SPRITE_2_X_VELOCITY_ID = "sprite2xvelocity";
	private float sprite2XVelocity = 0;
	private static final String SPRITE_2_Y_VELOCITY_ID = "sprite2yvelocity";
	private float sprite2YVelocity = 0;
	private static final String X_FRICTION_ID = "xfriction";
	private float xFriction = 0;
	private static final String Y_FRICTION_ID = "yfriction";
	private float yFriction = 0;
	private static final int MAX_SPEED = 10;
	private static final float REBOUND_FAC = .5f;
	private static final String SPRITE2_ID = "sprite2";
	private static final String MAZE_ID = "maze";

	private Handler frame = new Handler();
	// The delay in milliseconds between frame updates
	private static final int FRAME_DELAY = 17; // 17 => about 59 frames per
												// second

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maze_game);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Get the message from the intent
		Intent intent = getIntent();
		this.mazeType = intent.getIntExtra(StartMenu.MAZE_TYPE,
				StartMenu.PERFECT_MAZE);
		if (savedInstanceState == null) {
			initGfx();
		} else {
			final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
			sprite2XVelocity = savedInstanceState
					.getFloat(SPRITE_2_X_VELOCITY_ID);
			sprite2YVelocity = savedInstanceState
					.getFloat(SPRITE_2_Y_VELOCITY_ID);
			xFriction = savedInstanceState.getFloat(X_FRICTION_ID);
			yFriction = savedInstanceState.getFloat(Y_FRICTION_ID);
			gb.setSprite2((Point) savedInstanceState.getParcelable(SPRITE2_ID));
			gb.setMaze((Maze) savedInstanceState.getParcelable(MAZE_ID));
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		frame.removeCallbacksAndMessages(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		frame.postDelayed(frameUpdate, FRAME_DELAY);
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		outState.putFloat(SPRITE_2_X_VELOCITY_ID, sprite2XVelocity);
		outState.putFloat(SPRITE_2_Y_VELOCITY_ID, sprite2YVelocity);
		outState.putFloat(X_FRICTION_ID, xFriction);
		outState.putFloat(Y_FRICTION_ID, yFriction);
		outState.putParcelable(SPRITE2_ID, gb.getSpite2());
		outState.putParcelable(MAZE_ID, gb.getMaze());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maze_game_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.reset_maze:
			resetGame();
			return true;
		case android.R.id.home:
			// This ensures that the parent activity is recreated with any
			// information it may have saved.
			Intent intent = NavUtils.getParentActivityIntent(this);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			NavUtils.navigateUpTo(this, intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void resetGame() {
		frame.removeCallbacksAndMessages(null);
		initGfx();
	}

	public void initGfx() {
		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		// Check if the View has been measured.
		if (gb.getWidth() == 0 || gb.getHeight() == 0) {
			ViewTreeObserver vto = gb.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				@SuppressLint("NewApi")
				@SuppressWarnings("deprecation")
				public void onGlobalLayout() {
					// Initialize stuff that is dependent on the view already
					// having
					// been measured.
					gb.setMaze(createMaze(gb.getWidth(), gb.getHeight()));
					Rect startCellRect = calculateCellRect(gb.getMaze()
							.getCell(Cell.START_CELL));
					gb.setSprite2(startCellRect.centerX(),
							startCellRect.centerY());
					gb.resetStarField();
					resetSprite2Velocity();
					frame.removeCallbacksAndMessages(frameUpdate);
					gb.invalidate();
					frame.postDelayed(frameUpdate, FRAME_DELAY);

					// Remove this ViewTreeObserver
					ViewTreeObserver obs = gb.getViewTreeObserver();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						obs.removeOnGlobalLayoutListener(this);
					} else {
						obs.removeGlobalOnLayoutListener(this);
					}
				}

			});
		} else {
			gb.setMaze(createMaze(gb.getWidth(), gb.getHeight()));
			Rect startCellRect = calculateCellRect(gb.getMaze().getCell(
					Cell.START_CELL));
			gb.setSprite2(startCellRect.centerX(), startCellRect.centerY());
			gb.resetStarField();
			resetSprite2Velocity();
			frame.removeCallbacksAndMessages(frameUpdate);
			gb.invalidate();
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		}

	}

	public static Rect calculateCellRect(Cell cell) {
		return new Rect(cell.getCoords().x
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH,
				cell.getCoords().y
						* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
						+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH,
				(cell.getCoords().x + 1)
						* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
						+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().y + 1)
						* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
						+ GameBoard.BOUNDARY_WIDTH);
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
		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		if (gb.isAccelerating()) {
			float xTouch = gb.getXTouch();
			float yTouch = gb.getYTouch();
			Point sprite2 = gb.getSpite2();
			sprite2XVelocity = TOUCH_FACTOR
					* (xTouch - sprite2.x + Math.round(PREVIOUS_VELOCITY_FAC
							* sprite2XVelocity));
			sprite2YVelocity = TOUCH_FACTOR
					* (yTouch - sprite2.y + Math.round(PREVIOUS_VELOCITY_FAC
							* sprite2YVelocity));
			// Enforce max speed;
			int accSpeed = (int) Math.round(Math.sqrt(Math.pow(
					sprite2XVelocity, 2) + Math.pow(sprite2YVelocity, 2)));
			if (accSpeed > MAX_SPEED + 1) {
				sprite2XVelocity = sprite2XVelocity * MAX_SPEED / accSpeed;
				sprite2YVelocity = sprite2YVelocity * MAX_SPEED / accSpeed;
			}
		} else {
			// Decrease speed with friction.
			float speed = (float) Math.sqrt(Math.pow(sprite2XVelocity, 2)
					+ Math.pow(sprite2YVelocity, 2));
			if ((Math.abs(sprite2XVelocity) + Math.abs(sprite2YVelocity)) > 0) {
				xFriction = speed
						* FRICTION
						* -1
						* sprite2XVelocity
						/ (Math.abs(sprite2XVelocity) + Math
								.abs(sprite2YVelocity));
				yFriction = speed
						* FRICTION
						* -1
						* sprite2YVelocity
						/ (Math.abs(sprite2XVelocity) + Math
								.abs(sprite2YVelocity));
			}
			sprite2XVelocity = sprite2XVelocity + xFriction;
			sprite2YVelocity = sprite2YVelocity + yFriction;
		}
	}

	/**
	 * Update the position of the objects on the gameboard.
	 */
	public void updatePosition() {
		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		Point vel = new Point(Math.round(sprite2XVelocity),
				Math.round(sprite2YVelocity));
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
		// NOTE, having the check here means that if we bounce into and out
		// of the cell in one frame that it won't count.
		if (calculateCellRect(gb.getMaze().getCell(Cell.END_CELL)).contains(
				gb.getSpite2().x, gb.getSpite2().y)) {
			// Sprite is inside the end cell.
			resetGame();
		}
	}

	// TODO update to use pixel perfect collision detection
	private boolean wallsIntersects(int left, int top, int right, int bottom) {
		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		for (Wall w : gb.getMaze().getWalls()) {
			if (w.getBounds().intersects(left, top, right, bottom))
				return true;
		}
		return false;
	}

	private void takeNStepsInYDirection(Point vel, int n) {
		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		Point sprite2 = gb.getSpite2();
		while (n > 0) {
			// Take a steps along the yVel vector, making decisions as we go.
			if (vel.y > 0) {
				if (sprite2.y + 1 > gb.getHeight() - gb.getSprite2Height() / 2
						|| wallsIntersects(
								sprite2.x - gb.getSprite2Width() / 2, sprite2.y
										+ 1 - gb.getSprite2Height() / 2,
								sprite2.x + gb.getSprite2Width() / 2, sprite2.y
										+ 1 + gb.getSprite2Height() / 2)) {
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
								sprite2.x - gb.getSprite2Width() / 2, sprite2.y
										- 1 - gb.getSprite2Height() / 2,
								sprite2.x + gb.getSprite2Width() / 2, sprite2.y
										- 1 + gb.getSprite2Height() / 2)) {
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
		final GameBoard gb = ((GameBoard) findViewById(R.id.gameboard));
		Point sprite2 = gb.getSpite2();
		while (n > 0) {
			// Take a steps along the xVel vector, making decisions as we go.
			if (vel.x > 0) {
				if (sprite2.x + 1 > gb.getWidth() - gb.getSprite2Width() / 2
						|| wallsIntersects(sprite2.x + 1 - gb.getSprite2Width()
								/ 2, sprite2.y - gb.getSprite2Height() / 2,
								sprite2.x + 1 + gb.getSprite2Width() / 2,
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
						|| wallsIntersects(sprite2.x - 1 - gb.getSprite2Width()
								/ 2, sprite2.y - gb.getSprite2Height() / 2,
								sprite2.x - 1 + gb.getSprite2Width() / 2,
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

			// Redraw the canvas
			((GameBoard) findViewById(R.id.gameboard)).invalidate();

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
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().y)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH + GameBoard.WALL_WIDTH, cell
				.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH));
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
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH + GameBoard.WALL_WIDTH, (cell
				.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH));
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
		wall.setBounds(new Rect(cell.getCoords().x
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, cell.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, cell.getCoords().x
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH, (cell
				.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH + GameBoard.WALL_WIDTH));
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
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, cell.getCoords().y
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH, (cell.getCoords().x + 1)
				* (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH)
				+ GameBoard.WALL_WIDTH + GameBoard.BOUNDARY_WIDTH, (cell
				.getCoords().y + 1)
				* (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH)
				+ GameBoard.BOUNDARY_WIDTH + GameBoard.WALL_WIDTH));
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
	private void setWallBoundsBoundaryCell(Wall wall, Cell cell,
			boolean[] corners, int mazeWidth, int mazeHeight) {
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
	 * Creates a Maze object and initializes it.
	 * 
	 * @param canvasWidth
	 *            The width of the canvas the maze is drawn on.
	 * @param canvasHeight
	 *            The height of the canvas the maze is drawn on.
	 * @return The initialized Maze object.
	 */
	private Maze createMaze(int canvasWidth, int canvasHeight) {
		Maze maze = new Maze((canvasWidth - 2 * GameBoard.BOUNDARY_WIDTH)
				/ (GameBoard.CELL_WIDTH + GameBoard.WALL_WIDTH),
				(canvasHeight - 2 * GameBoard.BOUNDARY_WIDTH)
						/ (GameBoard.CELL_HEIGHT + GameBoard.WALL_WIDTH));
		switch (mazeType) {
		case StartMenu.PERFECT_MAZE:
			maze.makePerfectMaze();
			break;
		case StartMenu.DFS_MAZE:
			maze.makeDFSMaze();
			break;
		}

		// Set the start and finish cells.
		maze.getCells()[0].setType(Cell.START_CELL);
		maze.getCells()[maze.getWidth() * maze.getHeight() - 1]
				.setType(Cell.END_CELL);

		// Calculate the bounds for each wall.
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
					setWallBoundsBoundaryCell(w, cell1, corners,
							maze.getWidth(), maze.getHeight());
				} else {
					// The first cell of a wall should never be null, but
					// this is included for future usability.
					setWallBoundsBoundaryCell(w, cell2, corners,
							maze.getWidth(), maze.getHeight());
				}

			}
		}
		return maze;
	}

}
