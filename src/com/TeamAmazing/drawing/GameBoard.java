//TODO update the game to have a maze background, and some way to detect where walls all for collision detection.
//TODO Change the spaceship (sprite 2) to be something more suitable to a navigate a maze.
//TODO Sprite2's location seems to be the top left of it's bounding rectangle instead of the center.
package com.TeamAmazing.drawing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.TeamAmazing.game.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GameBoard extends View {

	private Paint p;
	private List<Point> starField = null;
	private int starAlpha = 80;
	private int starFade = 2;
	private Rect sprite2Bounds = new Rect(0, 0, 0, 0);
	private Point sprite2;
	// TODO make velocity and friction values private and use getter and setter
	// methods.
	public float sprite2XVelocity = 0;
	public float sprite2YVelocity = 0;
	public float xFriction = 0;
	public float yFriction = 0;
	private static final int MAX_SPEED = 15;
	private Bitmap bm2 = null;
	private boolean collisionDetected = false;
	private Point lastCollision = new Point(-1, -1);
	private boolean isAccelerating = false;
	private int xTouch;
	private int yTouch;

	private static final int NUM_OF_STARS = 25;
	private static final float REBOUND_FAC = .5f;
	private static final float PREVIOUS_VELOCITY_FAC = .49f;
	private static final float TOUCH_FACTOR = .25f;
	private static final float FRICTION = .05f;

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

	// return the point of the last collision
	synchronized public Point getLastCollision() {
		return lastCollision;
	}

	// return the collision flag
	synchronized public boolean wasCollisionDetected() {
		return collisionDetected;
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
		collisionDetected = false;
	}

	private boolean checkForCollision() {
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
		// // TODO why do we have r3? Why not just use r1?
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
		return false;
	}

	@Override
	synchronized public void onDraw(Canvas canvas) {

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

		// Draws the bitmap, with sprite2.x,y as the center
		canvas.drawBitmap(bm2, sprite2.x - sprite2Bounds.width() / 2, sprite2.y
				- sprite2Bounds.height() / 2, null);
		collisionDetected = checkForCollision();
		if (collisionDetected) {
			p.setColor(Color.RED);
			p.setAlpha(255);
			p.setStrokeWidth(5);
			canvas.drawLine(lastCollision.x - 5, lastCollision.y - 5,
					lastCollision.x + 5, lastCollision.y + 5, p);
			canvas.drawLine(lastCollision.x + 5, lastCollision.y - 5,
					lastCollision.x - 5, lastCollision.y + 5, p);
		}
	}

	// Method for getting touch state--requires android 2.1 or greater
	// The touch event only triggers if a down event happens inside this view,
	// however move events and up events can happen outside the view
	@Override
	synchronized public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xTouch = Math.round(ev.getX());
			yTouch = Math.round(ev.getY());
			isAccelerating = true;
			break;
		case MotionEvent.ACTION_UP:
			isAccelerating = false;
			break;
		case MotionEvent.ACTION_MOVE:
			xTouch = Math.round(ev.getX());
			yTouch = Math.round(ev.getY());
			isAccelerating = true;
			break;
		}
		return true;
	}

	public void updateVelocity() {
		if (isAccelerating) {
			xFriction = 0;
			yFriction = 0;
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

	public void resetSprite2Velocity() {
		sprite2XVelocity = 0;
		sprite2YVelocity = 0;
		xFriction = 0;
		yFriction = 0;
	}

	// Tries adding the position and velocity. If that would send the sprite out
	// of bounds then it updates position one step at a time along the velocity
	// vector, reversing when needed.
	public void updatePosition() {
		Point upLoc = new Point(Math.round(sprite2.x + sprite2XVelocity),
				Math.round(sprite2.y + sprite2YVelocity));
		if (upLoc.x > getWidth() - getSprite2Width() / 2
				|| upLoc.x < getSprite2Width() / 2) {
			int xVel = Math.round(sprite2XVelocity);
			upLoc.x = sprite2.x;
			// Take a steps along the xVel vector, making decisions as we go.
			while (Math.abs(xVel) > 0) {
				if (xVel > 0) {
					if (upLoc.x + 1 > getWidth() - getSprite2Width() / 2) {
						// Rebound
						upLoc.x -= 1;
						xVel *= -1 * REBOUND_FAC;
						sprite2XVelocity *= -1 * REBOUND_FAC;
						xFriction *= -1 * REBOUND_FAC;
					} else {
						upLoc.x += 1;
					}
					xVel--;
				} else {
					if (upLoc.x - 1 < getSprite2Width() / 2) {
						// Rebound
						upLoc.x += 1;
						xVel *= -1 * REBOUND_FAC;
						sprite2XVelocity *= -1 * REBOUND_FAC;
						xFriction *= -1 * REBOUND_FAC;
					} else {
						upLoc.x -= 1;
					}
					xVel++;
				}
			}
		}
		if (upLoc.y > getHeight() - getSprite2Height() / 2
				|| upLoc.y < getSprite2Height() / 2) {
			int yVel = Math.round(sprite2YVelocity);
			upLoc.y = sprite2.y;
			// Take a steps along the yVel vector, making decisions as we go.
			while (Math.abs(yVel) > 0) {
				if (yVel > 0) {
					if (upLoc.y + 1 > getHeight() - getSprite2Height() / 2) {
						// Rebound
						upLoc.y -= 1;
						yVel *= -1 * REBOUND_FAC;
						sprite2YVelocity *= -1 * REBOUND_FAC;
						yFriction *= -1 * REBOUND_FAC;
					} else {
						upLoc.y += 1;
					}
					yVel--;
				} else {
					if (upLoc.y - 1 < getSprite2Height() / 2) {
						// Rebound
						upLoc.y += 1;
						yVel *= -1 * REBOUND_FAC;
						sprite2YVelocity *= -1 * REBOUND_FAC;
						yFriction *= -1 * REBOUND_FAC;
					} else {
						upLoc.y -= 1;
					}
					yVel++;
				}
			}
		}
		setSprite2(upLoc);
	}
}
