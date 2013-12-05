//TODO Add collision detection for the maze.
package com.TeamAmazing.drawing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.TeamAmazing.drawing.Maze.Wall;
import com.TeamAmazing.drawing.Maze.Cell;
import com.TeamAmazing.game.R;

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
	private Maze maze = null;
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
	private static final int CELL_WIDTH = 15;
	// The height of a maze cell in pixels.
	private static final int CELL_HEIGHT = 15;
	private static final int WALL_WIDTH = 2;

	// Allow our controller to get and set the sprite positions

	// sprite 2 setter
	synchronized public void setSprite2(int x, int y) {
		sprite2 = new Point(x, y);
	}

	synchronized public void setSprite2(Point p) {
		sprite2 = p;
	}

	// sprite 2 getter
	synchronized public int getSprite2X() {
		return sprite2.x;
	}

	synchronized public int getSprite2Y() {
		return sprite2.y;
	}

	public Point getSpite2() {
		return sprite2;
	}

	synchronized public void resetStarField() {
		starField = null;
	}

	// expose sprite bounds to controller
	synchronized public int getSprite2Width() {
		return sprite2Bounds.width();
	}

	synchronized public int getSprite2Height() {
		return sprite2Bounds.height();
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

	// private boolean checkForCollision() {
	// if (sprite1.x < 0 && sprite2.x < 0 && sprite1.y < 0 && sprite2.y < 0)
	// return false;
	// Rect r1 = new Rect(sprite1.x, sprite1.y, sprite1.x
	// + sprite1Bounds.width(), sprite1.y + sprite1Bounds.height());
	// Rect r2 = new Rect(sprite2.x - sprite2Bounds.width() / 2, sprite2.y
	// - sprite2Bounds.height() / 2, sprite2.x + sprite2Bounds.width()
	// / 2, sprite2.y + sprite2Bounds.height() / 2);
	// Rect r3 = new Rect(r1);
	// if (r1.intersect(r2)) {
	// for (int i = r1.left; i < r1.right; i++) {
	// for (int j = r1.top; j < r1.bottom; j++) {
	// if (bm1.getPixel(i - r3.left, j - r3.top) != Color.TRANSPARENT) {
	// if (bm2.getPixel(i - r2.left, j - r2.top) != Color.TRANSPARENT) {
	// lastCollision = new Point(sprite2.x
	// - sprite2Bounds.width() / 2 + i - r2.left,
	// sprite2.y - sprite2Bounds.height() / 2 + j
	// - r2.top);
	// return true;
	// }
	// }
	// }
	// }
	// }
	// lastCollision = new Point(-1, -1);
	// return false;
	// }

	@Override
	synchronized public void onDraw(Canvas canvas) {

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
		// p.setColor(Color.MAGENTA);
		// void drawRect(float left, float top, float right, float bottom, Paint
		// paint)
		for (Wall w : maze.getWalls()) {
			Cell cell1 = w.v1;
			Cell cell2 = w.v2;
			if (cell1.coordinates.x == cell2.coordinates.x) {
				p.setColor(Color.MAGENTA);
				// Vertical cells, so I want to draw a horizontal wall
				canvas.drawRect(
						cell1.coordinates.x * (CELL_WIDTH + WALL_WIDTH),
						cell1.coordinates.y * (CELL_HEIGHT + WALL_WIDTH),
						(cell1.coordinates.x + 1) * (CELL_WIDTH + WALL_WIDTH),
						cell1.coordinates.y * (CELL_HEIGHT + WALL_WIDTH)
								+ WALL_WIDTH, p);

			} else {
				p.setColor(Color.RED);
				// Horizontal cells, so I want to draw a vertical wall.
				canvas.drawRect(
						cell1.coordinates.x * (CELL_WIDTH + WALL_WIDTH),
						cell1.coordinates.y * (CELL_HEIGHT + WALL_WIDTH),
						cell1.coordinates.x * (CELL_WIDTH + WALL_WIDTH)
								+ WALL_WIDTH, (cell1.coordinates.y + 1)
								* (CELL_HEIGHT + WALL_WIDTH), p);
			}

		}

		// Check if the sprite has been initialized with a start position.
		if (!(sprite2.x < 0)) {
			// Draws the bitmap, with sprite2.x,y as the center
			canvas.drawBitmap(bm2, sprite2.x - sprite2Bounds.width() / 2,
					sprite2.y - sprite2Bounds.height() / 2, null);
		}
	}

	// The width and height passed are the canvas width and height.
	private void initializeMaze(int width, int height) {
		maze = new Maze(width / (CELL_WIDTH + WALL_WIDTH), height
				/ (CELL_HEIGHT + WALL_WIDTH));
		maze.makePerfectMaze();
	}

	// Method for getting touch state--requires android 2.1 or greater
	// The touch event only triggers if a down event happens inside this view,
	// however move events and up events can happen outside the view
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

	public boolean isAccelerating() {
		return isAccelerating;
	}

	public void resetMaze() {
		maze = null;
	}

	public float getYTouch() {
		return yTouch;
	}

	public float getXTouch() {
		return xTouch;
	}
}
