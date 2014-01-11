package com.TeamAmazing.activities;

// TODO add code for onResume, onStop, onPause etc...

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.TeamAmazing.Maze.GameOfLife;
import com.TeamAmazing.drawing.StartMenuBackground;
import com.TeamAmazing.game.R;

public class StartMenu extends Activity {
	private Handler frame = new Handler();
	// The delay in milliseconds between frame updates
	private static final int FRAME_DELAY = 50;
	private int maxGenerations;
	private int numCurrentGenerations = 0;
	private GameOfLife gameOfLife;

	public static final int PERFECT_MAZE = 0;
	public static final int DFS_MAZE = 1;
	public static final String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";
	private static final String GAME_OF_LIFE_ID = "gameoflife";
	private static final String NUM_CURRENT_GENERATIONS_ID = "numcurrentgenerations";
	private static final String MAX_GENERATIONS_ID = "maxgenerations";

	/**
	 * Sets the content view. Checks the savedInstanceState and restores from
	 * there if possible. If there is no savedInstanceState creates a new
	 * GameOfLife, initializes it, and points the view at it.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_menu);
		final StartMenuBackground smb = (StartMenuBackground) findViewById(R.id.start_menu_background);

		if (savedInstanceState != null) {
			// restore from savedInstanceState
			gameOfLife = (GameOfLife) savedInstanceState
					.getParcelable(GAME_OF_LIFE_ID);
			numCurrentGenerations = savedInstanceState
					.getInt(NUM_CURRENT_GENERATIONS_ID);
			maxGenerations = savedInstanceState.getInt(MAX_GENERATIONS_ID);

			// TODO rotate the game if the phone was rotated
			// TODO use a more sophisticated method for checking if the view has
			// been measured.
			if (smb.getWidth() == 0 || smb.getHeight() == 0) {
				ViewTreeObserver vto = smb.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					@SuppressLint("NewApi")
					@SuppressWarnings("deprecation")
					public void onGlobalLayout() {
						// Do stuff that requires the view to be measured
						rotateBoard();
						// Remove this ViewTreeObserver
						ViewTreeObserver obs = smb.getViewTreeObserver();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							obs.removeOnGlobalLayoutListener(this);
						} else {
							obs.removeGlobalOnLayoutListener(this);
						}
					}

				});
			} else {
				// The view has already been measured
				rotateBoard();
			}
			smb.setBoard(gameOfLife.getBoard());
		} else {
			// This is a new incarnation of the activity.
			gameOfLife = new GameOfLife();
			// Check if the View has been measured.
			// TODO use a more sophisticated method for checking if the view has
			// been measured.
			if (smb.getWidth() == 0 || smb.getHeight() == 0) {
				ViewTreeObserver vto = smb.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					@SuppressLint("NewApi")
					@SuppressWarnings("deprecation")
					public void onGlobalLayout() {
						// Do stuff that requires the view to be measured
						initializeGameOfLife();

						// Remove this ViewTreeObserver
						ViewTreeObserver obs = smb.getViewTreeObserver();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							obs.removeOnGlobalLayoutListener(this);
						} else {
							obs.removeGlobalOnLayoutListener(this);
						}
					}

				});
			} else {
				// The view has already been measured
				initializeGameOfLife();
			}
		}
	}

	private void rotateBoard() {
		final StartMenuBackground smb = (StartMenuBackground) findViewById(R.id.start_menu_background);
		int width = smb.getWidth() / StartMenuBackground.CELL_WIDTH;
		int height = smb.getHeight() / StartMenuBackground.CELL_HEIGHT;
		byte[][] oldBoard = gameOfLife.getBoard();
		if (width != oldBoard.length || height != oldBoard[0].length) {
			// the board needs to be rotated but for now create a new one.
			gameOfLife = new GameOfLife();
			initializeGameOfLife();

			// The below doesn't work because the new width and height are not
			// just the old width and height switched.
			// byte[][] newBoard = new byte[width][height];
			// for (int i = 0; i<newBoard.length; i++){
			// for (int j=0; j<newBoard[0].length; j++){
			// newBoard[i][j] = oldBoard[j][i];
			// }
			// }

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

		outState.putParcelable(GAME_OF_LIFE_ID, gameOfLife);
		outState.putInt(NUM_CURRENT_GENERATIONS_ID, numCurrentGenerations);
		outState.putInt(MAX_GENERATIONS_ID, maxGenerations);
	}

	private void initializeGameOfLife() {
		final StartMenuBackground smb = (StartMenuBackground) findViewById(R.id.start_menu_background);
		int width = smb.getWidth() / StartMenuBackground.CELL_WIDTH;
		int height = smb.getHeight() / StartMenuBackground.CELL_HEIGHT;
		maxGenerations = (int) Math.max(2.5 * width, 2.5 * height);
		gameOfLife.initializeCells(width, height);
		smb.setBoard(gameOfLife.getBoard());
	}

	private Runnable frameUpdate = new Runnable() {
		@Override
		synchronized public void run() {
			final StartMenuBackground smb = ((StartMenuBackground) findViewById(R.id.start_menu_background));
			frame.removeCallbacksAndMessages(frameUpdate);

			if (numCurrentGenerations > maxGenerations) {
				numCurrentGenerations = 0;
				initializeGameOfLife();
				smb.invalidate();
				frame.postDelayed(frameUpdate, FRAME_DELAY);
			} else {
				// Compute the next generation
				int[] bounds = gameOfLife.nextGeneration();
				numCurrentGenerations++;

				// Redraw the canvas
				smb.setBoard(gameOfLife.getBoard());
				smb.invalidateAreaOf(bounds);

				// Loop, after FRAME_DELAY milliseconds.
				frame.postDelayed(frameUpdate, FRAME_DELAY);
			}
		}
	};

	// Kruskal's algorithm
	public void startKruskalsMaze(View v) {
		Intent intent = new Intent(this, MazeGame.class);
		intent.putExtra(MAZE_TYPE, PERFECT_MAZE);
		startActivity(intent);
	}

	// Recursive backtracker algorithm
	public void startRecursiveBacktrackerMaze(View v) {
		Intent intent = new Intent(this, MazeGame.class);
		intent.putExtra(MAZE_TYPE, DFS_MAZE);
		startActivity(intent);
	}

}
