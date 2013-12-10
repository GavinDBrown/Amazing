package com.TeamAmazing.game;

import com.TeamAmazing.drawing.GameBoard;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Point;

public class Main extends Activity implements OnClickListener {
	private static final float PREVIOUS_VELOCITY_FAC = .49f;
	private static final float TOUCH_FACTOR = .10f;
	private static final float FRICTION = .05f;
	private float sprite2XVelocity = 0;
	private float sprite2YVelocity = 0;
	private float xFriction = 0;
	private float yFriction = 0;
	private static final int MAX_SPEED = 20;
	private static final float REBOUND_FAC = .5f;
	private final Point startPos = new Point(50, 50);

	private Handler frame = new Handler();
	// The delay in milliseconds between frame updates
	private static final int FRAME_DELAY = 17; // 17 => about 59 frames per
												// second

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Handler h = new Handler();
		((Button) findViewById(R.id.the_button)).setOnClickListener(this);
		// According to the TechRepublic blog:
		// We can't initialize the graphics immediately because the layout
		// manager
		// needs to run first, thus we post with a 1 second delay.
		// I changed it to 0 seconds, change this if it crashes at startup.
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				initGfx();
			}
		}, 0);
	}

	synchronized public void initGfx() {
		GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		gb.resetStarField();
		gb.resetMaze();
		gb.setSprite2(startPos);
		resetSprite2Velocity();
		((Button) findViewById(R.id.the_button)).setEnabled(true);
		frame.removeCallbacksAndMessages(frameUpdate);
		((GameBoard) findViewById(R.id.the_canvas)).invalidate();
		frame.postDelayed(frameUpdate, FRAME_DELAY);
	}

	@Override
	// Runs when the reset button is clicked.
	synchronized public void onClick(View v) {
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
		GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		Point sprite2 = gb.getSpite2();
		Point upLoc = new Point(Math.round(sprite2.x + sprite2XVelocity), Math.round(sprite2.y
				+ sprite2YVelocity));
		if (upLoc.x > gb.getWidth() - gb.getSprite2Width() / 2
				|| upLoc.x < gb.getSprite2Width() / 2) {
			int xVel = Math.round(sprite2XVelocity);
			upLoc.x = sprite2.x;
			// Take a steps along the xVel vector, making decisions as we go.
			while (Math.abs(xVel) > 0) {
				if (xVel > 0) {
					if (upLoc.x + 1 > gb.getWidth() - gb.getSprite2Width() / 2) {
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
					if (upLoc.x - 1 < gb.getSprite2Width() / 2) {
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
		if (upLoc.y > gb.getHeight() - gb.getSprite2Height() / 2
				|| upLoc.y < gb.getSprite2Height() / 2) {
			int yVel = Math.round(sprite2YVelocity);
			upLoc.y = sprite2.y;
			// Take a steps along the yVel vector, making decisions as we go.
			while (Math.abs(yVel) > 0) {
				if (yVel > 0) {
					if (upLoc.y + 1 > gb.getHeight() - gb.getSprite2Height() / 2) {
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
					if (upLoc.y - 1 < gb.getSprite2Height() / 2) {
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
		gb.setSprite2(upLoc);
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
}
