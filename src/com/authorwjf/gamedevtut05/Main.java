package com.authorwjf.gamedevtut05;

import java.util.Random;

import com.authorwjf.drawing.GameBoard;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Point;

public class Main extends Activity implements OnClickListener {

	private Handler frame = new Handler();
	private Point sprite1Velocity;
	private Point sprite2Velocity;
	private int sprite1MaxX;
	private int sprite1MaxY;
	private int sprite2MaxX;
	private int sprite2MaxY;
	// acceleration flag
	private boolean isAccelerating = false;
	private int xTouch;
	private int yTouch;
	// The delay in milliseconds between frame updates
	private static final int FRAME_DELAY = 40; // 17 => about 59 frames per
												// second

	// Method for getting touch state--requires android 2.1 or greater
	// TODO Make the spaceship accelerate towards wherever is being touched, and
	// deaccelerate while the screen is not being touched.
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

	// Increase or decrease the velocity based on state and sprite2's locations
	private void updateVelocity(Point sprite2) {
		if (isAccelerating) {
			// Increase velocity with touch information
			// TODO add a scale factor;
			sprite2Velocity.x += xTouch - sprite2.x;
			sprite2Velocity.y += yTouch - sprite2.y;
		} else {
			// Decrease velocity
			int xFriction = -1 * (int) Math.signum(sprite2Velocity.x); // -1, 0, or 1
			int yFriction = -1 * (int) Math.signum(sprite2Velocity.y);
			sprite2Velocity.x = (sprite2Velocity.x > 0) ? sprite2Velocity.x
					- xFriction : sprite2Velocity.x + xFriction;
			sprite2Velocity.y = (sprite2Velocity.y > 0) ? sprite2Velocity.y
					- yFriction : sprite2Velocity.y + yFriction;
		}

		// Set and enforce max and min values for speed.
		final int maxSpeed = 5;
		final int minSpeed = 0;
		int xDir = (int) Math.signum(sprite2Velocity.x); 
		int yDir = (int) Math.signum(sprite2Velocity.y);
//		int xDir = (sprite2Velocity.x > 0) ? 1 : -1;
//		int yDir = (sprite2Velocity.y > 0) ? 1 : -1;
		int xSpeed = Math.abs(sprite2Velocity.x);
		int ySpeed = Math.abs(sprite2Velocity.y);
		if (xSpeed > maxSpeed)
			xSpeed = maxSpeed;
		if (xSpeed < minSpeed)
			xSpeed = minSpeed;
		if (ySpeed > maxSpeed)
			ySpeed = maxSpeed;
		if (ySpeed < minSpeed)
			ySpeed = minSpeed;
		sprite2Velocity.x = xSpeed * xDir;
		sprite2Velocity.y = ySpeed * yDir;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Handler h = new Handler();
		((Button) findViewById(R.id.the_button)).setOnClickListener(this);
		// TODO Why is the below delayed 1000 milliseconds instead of just
		// posted?
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				initGfx();
			}
		}, 1000);
	}

	private Point getRandomVelocity() {
		Random r = new Random();
		int min = 1;
		int max = 5;
		int x = r.nextInt(max - min + 1) + min;
		int y = r.nextInt(max - min + 1) + min;
		return new Point(x, y);
	}

	private Point getRandomPoint() {
		Random r = new Random();
		int minX = 0;
		int maxX = findViewById(R.id.the_canvas).getWidth()
				- ((GameBoard) findViewById(R.id.the_canvas)).getSprite1Width();
		int x = 0;
		int minY = 0;
		int maxY = findViewById(R.id.the_canvas).getHeight()
				- ((GameBoard) findViewById(R.id.the_canvas))
						.getSprite1Height();
		int y = 0;
		x = r.nextInt(maxX - minX + 1) + minX;
		y = r.nextInt(maxY - minY + 1) + minY;
		return new Point(x, y);
	}

	synchronized public void initGfx() {
		((GameBoard) findViewById(R.id.the_canvas)).resetStarField();
		Point p1, p2;
		do {
			p1 = getRandomPoint();
			p2 = getRandomPoint();
		} while (Math.abs(p1.x - p2.x) < ((GameBoard) findViewById(R.id.the_canvas))
				.getSprite1Width());
		((GameBoard) findViewById(R.id.the_canvas)).setSprite1(p1.x, p1.y);
		((GameBoard) findViewById(R.id.the_canvas)).setSprite2(p2.x, p2.y);
		sprite1Velocity = getRandomVelocity();
		sprite2Velocity = new Point(0, 0);
		sprite1MaxX = findViewById(R.id.the_canvas).getWidth()
				- ((GameBoard) findViewById(R.id.the_canvas)).getSprite1Width();
		sprite1MaxY = findViewById(R.id.the_canvas).getHeight()
				- ((GameBoard) findViewById(R.id.the_canvas))
						.getSprite1Height();
		sprite2MaxX = findViewById(R.id.the_canvas).getWidth()
				- ((GameBoard) findViewById(R.id.the_canvas)).getSprite2Width();
		sprite2MaxY = findViewById(R.id.the_canvas).getHeight()
				- ((GameBoard) findViewById(R.id.the_canvas))
						.getSprite2Height();
		((Button) findViewById(R.id.the_button)).setEnabled(true);
		// TODO why do we remove callbacks here?
		frame.removeCallbacks(frameUpdate);
		((GameBoard) findViewById(R.id.the_canvas)).invalidate();
		frame.postDelayed(frameUpdate, FRAME_DELAY);
	}

	@Override
	synchronized public void onClick(View v) {
		initGfx();
	}

	private Runnable frameUpdate = new Runnable() {

		// The main loop that drives the game.
		// Checks if there was a collision,
		// Updates velocity,
		// Does boundary checking,
		// Updates position based on velocity,
		// Redraws the canvas.

		@Override
		synchronized public void run() {
			if (((GameBoard) findViewById(R.id.the_canvas))
					.wasCollisionDetected()) {
				Point collisionPoint = ((GameBoard) findViewById(R.id.the_canvas))
						.getLastCollision();
				if (collisionPoint.x >= 0) {
					((TextView) findViewById(R.id.the_other_label))
							.setText("Last Collision XY ("
									+ Integer.toString(collisionPoint.x) + ","
									+ Integer.toString(collisionPoint.y) + ")");
				}
				return;
			}
			// TODO why do we remove callbacks here?
			frame.removeCallbacks(frameUpdate);
			Point sprite1 = new Point(
					((GameBoard) findViewById(R.id.the_canvas)).getSprite1X(),
					((GameBoard) findViewById(R.id.the_canvas)).getSprite1Y());
			Point sprite2 = new Point(
					((GameBoard) findViewById(R.id.the_canvas)).getSprite2X(),
					((GameBoard) findViewById(R.id.the_canvas)).getSprite2Y());
			// Add our call to increase or decrease velocity based on touch
			// information
			updateVelocity(sprite2);
			// TODO better boundary checking
			// What this does: update the position, then check if we are outside
			// the boundary,
			// if we are: reverse direction.
			// This sort of boundary checking has issues, because the velocity
			// gets overwritten by the next call. So we can head outside the
			// boundary with two successive impulses.
			// Something about how I changed update velocity broke this.
			// Currently Sprite 2 thinks the X boundary is shifted right.
			sprite1.x = sprite1.x + sprite1Velocity.x;
			// TODO why 5? Shouldn't it be sprite1.width? I think this is
			// because 5 was the max speed, so if it was within 5 of the
			// boundary it needed to turn back.
			if (sprite1.x > sprite1MaxX || sprite1.x < 5) {
				sprite1Velocity.x *= -1;
			}
			sprite1.y = sprite1.y + sprite1Velocity.y;
			if (sprite1.y > sprite1MaxY || sprite1.y < 5) {
				sprite1Velocity.y *= -1;
			}
			sprite2.x = sprite2.x + sprite2Velocity.x;
			if (sprite2.x > sprite2MaxX || sprite2.x < 5) {
				sprite2Velocity.x *= -1;
			}
			sprite2.y = sprite2.y + sprite2Velocity.y;
			if (sprite2.y > sprite2MaxY || sprite2.y < 5) {
				sprite2Velocity.y *= -1;
			}
			// Display UFO speed
			((TextView) findViewById(R.id.the_label))
					.setText("Sprite Acceleration ("
							+ Integer.toString(sprite2Velocity.x) + ","
							+ Integer.toString(sprite2Velocity.y) + ")");
			// Update position
			((GameBoard) findViewById(R.id.the_canvas)).setSprite1(sprite1.x,
					sprite1.y);
			((GameBoard) findViewById(R.id.the_canvas)).setSprite2(sprite2.x,
					sprite2.y);
			// Redraw the canvas
			((GameBoard) findViewById(R.id.the_canvas)).invalidate();
			// Loop, after FRAME_DELAY milliseconds.
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		}

	};
}
