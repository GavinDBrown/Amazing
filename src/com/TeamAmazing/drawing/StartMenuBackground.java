package com.TeamAmazing.drawing;

import java.util.Random;

import com.TeamAmazing.game.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class StartMenuBackground extends View {
	private Paint p;
	Random r = new Random();
	private Handler frame = new Handler();
	// The delay in milliseconds between frame updates
	private static final int FRAME_DELAY = 5000; // 17 => about 59 frames per
													// second

	public StartMenuBackground(Context context, AttributeSet aSet) {
		super(context, aSet);
		p = new Paint();
		frame.removeCallbacksAndMessages(frameUpdate);
		((StartMenuBackground) findViewById(R.id.start_menu_background)).invalidate();
		frame.postDelayed(frameUpdate, FRAME_DELAY);
	}

	@Override
	public void onDraw(Canvas canvas) {
		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
		p.setAlpha(255);
		canvas.drawRect(0, 0, getWidth(), getHeight(), p);
	}

	private Runnable frameUpdate = new Runnable() {
		@Override
		synchronized public void run() {
			frame.removeCallbacksAndMessages(frameUpdate);

			// Redraw the canvas
			((StartMenuBackground) findViewById(R.id.start_menu_background)).invalidate();

			// Loop, after FRAME_DELAY milliseconds.
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		}
	};

}
