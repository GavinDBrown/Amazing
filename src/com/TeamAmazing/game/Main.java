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
		// TODO Why is the below delayed 1000 milliseconds instead of just
		// posted?
		// According to the TechRepublic blog:
		// We can't initialize the graphics immediately because the layout
		// manager
		// needs to run first, thus call back in a sec.
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				initGfx();
			}
		}, 1000);
	}

	synchronized public void initGfx() {
		GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
		gb.resetStarField();
		Point p2 = new Point (50,50);
		gb.setSprite2(p2.x, p2.y);
		gb.resetSprite2Velocity();
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
			GameBoard gb = ((GameBoard) findViewById(R.id.the_canvas));
			if (gb.wasCollisionDetected()) {
				return;
			}
			// TODO why do we remove callbacks here?
			frame.removeCallbacks(frameUpdate);

			// Add our call to increase or decrease velocity
			gb.updateVelocity();

			// Update position with boundary checking
			// TODO add sprite1's position and boundary checking to this call.
			gb.updatePosition();

			float sprite2XVelocity = gb.sprite2XVelocity;
			float sprite2YVelocity = gb.sprite2YVelocity;
			float speed = (float) Math.sqrt(Math.pow(
					sprite2XVelocity, 2)
					+ Math.pow(sprite2YVelocity, 2));
			float xFriction = gb.xFriction;
			float yFriction = gb.yFriction;
			// Display Velocity and Friction information
			((TextView) findViewById(R.id.the_label))
					.setText("Sprite Velocity ("
							+ String.format("%.2f",sprite2XVelocity) + ","
							+ String.format("%.2f",sprite2YVelocity) + ")");
			((TextView) findViewById(R.id.the_other_label))
					.setText("Sprite Friction (" + String.format("%.2f", xFriction) + ","
							+ String.format("%.2f", yFriction) + ")");
			((TextView) findViewById(R.id.the_third_label))
			.setText("Sprite speed (" + String.format("%.2f",speed) + ")");
			
			// Redraw the canvas
			((GameBoard) findViewById(R.id.the_canvas)).invalidate();
			// Loop, after FRAME_DELAY milliseconds.
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		}

	};
}
