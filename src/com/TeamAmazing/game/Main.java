package com.TeamAmazing.game;

import java.util.Random;

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
	private Point sprite1Velocity;
	private int sprite1MaxX;
	private int sprite1MaxY;
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

	// TODO why do we always lookup the GameBoard view instead of just keeping
	// it as a variable?
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
		((GameBoard) findViewById(R.id.the_canvas)).resetSprite2Velocity();
		sprite1MaxX = findViewById(R.id.the_canvas).getWidth()
				- ((GameBoard) findViewById(R.id.the_canvas)).getSprite1Width();
		sprite1MaxY = findViewById(R.id.the_canvas).getHeight()
				- ((GameBoard) findViewById(R.id.the_canvas))
						.getSprite1Height();
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
//					((TextView) findViewById(R.id.the_other_label))
//							.setText("Last Collision XY ("
//									+ Integer.toString(collisionPoint.x) + ","
//									+ Integer.toString(collisionPoint.y) + ")");
				}
				return;
			}
			// TODO why do we remove callbacks here?
			frame.removeCallbacks(frameUpdate);
			Point sprite1 = new Point(
					((GameBoard) findViewById(R.id.the_canvas)).getSprite1X(),
					((GameBoard) findViewById(R.id.the_canvas)).getSprite1Y());
			// Add our call to increase or decrease velocity
			((GameBoard) findViewById(R.id.the_canvas)).updateVelocity();

			// Update position with boundary checking
			// TODO add sprite1's position and boundary checking to this call.
			((GameBoard) findViewById(R.id.the_canvas)).updatePosition();

			// Update position and boundary checking for Sprite1
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
			GameBoard gb = (GameBoard) findViewById(R.id.the_canvas);
			float sprite2XVelocity = gb.sprite2XVelocity;
			float sprite2YVelocity = gb.sprite2YVelocity;
			float speed = (float) Math.sqrt(Math.pow(
					sprite2XVelocity, 2)
					+ Math.pow(sprite2YVelocity, 2));
			float xFriction = gb.xFriction;
			float yFriction = gb.yFriction;
			// Display UFO speed
			((TextView) findViewById(R.id.the_label))
					.setText("Sprite Velocity ("
							+ String.format("%.2f",sprite2XVelocity) + ","
							+ String.format("%.2f",sprite2YVelocity) + ")");
			((TextView) findViewById(R.id.the_other_label))
					.setText("Sprite Friction (" + String.format("%.2f", xFriction) + ","
							+ String.format("%.2f", yFriction) + ")");
			((TextView) findViewById(R.id.the_third_label))
			.setText("Sprite speed (" + String.format("%.2f",speed) + ")");
			
			// TODO change this to be included in the call for sprite2, or it's
			// own call but in the GameBoard class.
			// Update position for sprite1
			((GameBoard) findViewById(R.id.the_canvas)).setSprite1(sprite1.x,
					sprite1.y);
			// Redraw the canvas
			((GameBoard) findViewById(R.id.the_canvas)).invalidate();
			// Loop, after FRAME_DELAY milliseconds.
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		}

	};
}
