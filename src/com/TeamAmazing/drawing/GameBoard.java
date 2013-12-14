package com.TeamAmazing.drawing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.TeamAmazing.game.MazeGame;
import com.TeamAmazing.game.Maze;
import com.TeamAmazing.game.R;
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
	public static final int CELL_WIDTH = 60;
	// The height of a maze cell in pixels.
	public static final int CELL_HEIGHT = 60;
	public static final int WALL_WIDTH = 5;
	public static final int BOUNDARY_WIDTH = 20;
	private static final int SPRITE_WIDTH = 25;
	private static final int SPRITE_HEIGHT = 13;

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
		bm2 = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(getResources(), R.drawable.ufo), SPRITE_WIDTH,
				SPRITE_HEIGHT, false);
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

		p.setColor(Color.CYAN);
		p.setAlpha(starAlpha += starFade);
		if (starAlpha >= 252 || starAlpha <= 80)
			starFade = starFade * -1;
		p.setStrokeWidth(5);
		for (int i = 0; i < NUM_OF_STARS; i++) {
			canvas.drawPoint(starField.get(i).x, starField.get(i).y, p);
		}
		// Draw the maze
		p.setColor(Color.MAGENTA);
		for (Wall w : maze.getWalls()) {
			canvas.drawRect(w.getBounds(), p);
		}
		
		// Draw the end cell.
		p.setColor(Color.RED);
		// TODO Calculate the end cell's Rect once and save the information.
		canvas.drawRect(MazeGame.calculateCellRect(maze.getCell(Maze.END_CELL)),p);

		// Check if the sprite has been initialized with a start position.
		if (!(sprite2.x < 0)) {
			// Draws the bitmap, with sprite2.x,y as the center
			canvas.drawBitmap(bm2, sprite2.x - sprite2Bounds.width() / 2,
					sprite2.y - sprite2Bounds.height() / 2, null);
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
