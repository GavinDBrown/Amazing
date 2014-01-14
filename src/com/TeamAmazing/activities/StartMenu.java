package com.TeamAmazing.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.TeamAmazing.Maze.GameOfLife;
import com.TeamAmazing.drawing.GameOfLifeBackground;
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

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);


		// Check if the game of life background is enabled
		if (sharedPrefs.getBoolean("pref_start_background", true)) {
			startGameOfLife(savedInstanceState);
		} else {
			// The game of life background is disabled
			setContentView(R.layout.start_menu_background);
		}
	}

	private void startGameOfLife(Bundle savedInstanceState) {
		setContentView(R.layout.game_of_life_background);
		final GameOfLifeBackground background = (GameOfLifeBackground) findViewById(R.id.game_of_life_background);

		if (savedInstanceState != null) {
			// restore from savedInstanceState
			gameOfLife = (GameOfLife) savedInstanceState
					.getParcelable(GAME_OF_LIFE_ID);
			numCurrentGenerations = savedInstanceState
					.getInt(NUM_CURRENT_GENERATIONS_ID);
			maxGenerations = savedInstanceState.getInt(MAX_GENERATIONS_ID);

			initilizeAfterMeasure(background);
			background.setBoard(gameOfLife.getBoard());
		} else {
			// This is a new incarnation of the activity.
			gameOfLife = new GameOfLife();
			initilizeAfterMeasure(background);
		}
	}

	private void initilizeAfterMeasure(final View background) {
		if (background.getWidth() == 0 || background.getHeight() == 0) {
			ViewTreeObserver vto = background.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				@SuppressLint("NewApi")
				@SuppressWarnings("deprecation")
				public void onGlobalLayout() {
					// Do stuff that requires the view to be measured
					initializeGameOfLife();

					// Remove this ViewTreeObserver
					ViewTreeObserver obs = background.getViewTreeObserver();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.start_menu_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void checkForResize() {
		final GameOfLifeBackground background = (GameOfLifeBackground) findViewById(R.id.game_of_life_background);
		int width = background.getWidth() / GameOfLifeBackground.CELL_WIDTH;
		int height = background.getHeight() / GameOfLifeBackground.CELL_HEIGHT;
		byte[][] oldBoard = gameOfLife.getBoard();
		if (width != oldBoard.length || height != oldBoard[0].length) {
			// the board needs to be resized (or rotated) but for now create a
			// new one.
			gameOfLife = new GameOfLife();
			initializeGameOfLife();

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

		 SharedPreferences sharedPrefs = PreferenceManager
		 .getDefaultSharedPreferences(this);

		// Check if the game of life background is enabled
		if (sharedPrefs.getBoolean("pref_start_background", true)) {
			if (findViewById(R.id.game_of_life_background) == null) {
				startGameOfLife(null);
			}
			final GameOfLifeBackground background = (GameOfLifeBackground) findViewById(R.id.game_of_life_background);
			background.invalidate();
			frame.postDelayed(frameUpdate, FRAME_DELAY);
		} else {
			setContentView(R.layout.start_menu_background);
			findViewById(R.id.start_menu_background).invalidate();

		}
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Check if the game of life background is enabled
		if (sharedPrefs.getBoolean("pref_start_background", true)) {
			outState.putParcelable(GAME_OF_LIFE_ID, gameOfLife);
			outState.putInt(NUM_CURRENT_GENERATIONS_ID, numCurrentGenerations);
			outState.putInt(MAX_GENERATIONS_ID, maxGenerations);
		}
	}

	private void initializeGameOfLife() {
		final GameOfLifeBackground background = (GameOfLifeBackground) findViewById(R.id.game_of_life_background);
		int width = background.getWidth() / GameOfLifeBackground.CELL_WIDTH;
		int height = background.getHeight() / GameOfLifeBackground.CELL_HEIGHT;
		maxGenerations = (int) Math.max(2.5 * width, 2.5 * height);
		numCurrentGenerations = 0;
		gameOfLife.initializeCells(width, height);
		background.setBoard(gameOfLife.getBoard());
	}

	private Runnable frameUpdate = new Runnable() {
		@Override
		synchronized public void run() {
			final GameOfLifeBackground background = ((GameOfLifeBackground) findViewById(R.id.game_of_life_background));
			frame.removeCallbacksAndMessages(frameUpdate);

			if (numCurrentGenerations > maxGenerations) {
				numCurrentGenerations = 0;
				initializeGameOfLife();
				background.invalidate();
				frame.postDelayed(frameUpdate, FRAME_DELAY);
			} else {
				// Compute the next generation
				int[] bounds = gameOfLife.nextGeneration();
				numCurrentGenerations++;

				// Redraw the canvas
				background.setBoard(gameOfLife.getBoard());
				background.invalidateAreaOf(bounds);

				// Loop, after FRAME_DELAY milliseconds.
				frame.postDelayed(frameUpdate, FRAME_DELAY);
			}
		}
	};

	// Kruskal's algorithm
	public void startKruskalsMaze(View v) {
		frame.removeCallbacksAndMessages(frameUpdate);
		Intent intent = new Intent(this, MazeGame.class);
		intent.putExtra(MAZE_TYPE, PERFECT_MAZE);
		startActivity(intent);
	}

	// Recursive backtracker algorithm
	public void startRecursiveBacktrackerMaze(View v) {
		frame.removeCallbacksAndMessages(frameUpdate);
		Intent intent = new Intent(this, MazeGame.class);
		intent.putExtra(MAZE_TYPE, DFS_MAZE);
		startActivity(intent);
	}

}
