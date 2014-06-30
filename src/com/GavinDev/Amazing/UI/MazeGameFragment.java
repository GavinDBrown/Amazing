
package com.GavinDev.Amazing.UI;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.drawing.MazeSurfaceView;
import com.GavinDev.Amazing.drawing.MazeThread;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

import java.util.Locale;

/**
 * Controls the life-cycle of the MazeThread and receives it's callbacks.
 * Whenever a new MazeSurfaceView is created this must pass it the MazeThread.
 * Responsible for updating the UI with the TimerText.
 */

public class MazeGameFragment extends Fragment implements MazeThread.Callback {
    /** The thread that's running the maze. */
    private MazeThread mMazeThread;

    private MazeCompletedDialog mCongratulationsDialog;

    /** The View displaying the maze. */
    private MazeSurfaceView mMazeView;

    private GameHelper mGameServicesHelper;

    public int mMazeType;

    private static final int MINIMUM_TIME_TO_UNLOCK_NOVICE = 30000;
    private static final int MINIMUM_TIME_TO_UNLOCK_MASTER = 10000;

    private String mTimerText = "";
    private Menu mOptionsMenu;

    public static final String MAZE_COMPLETED_TIME_ID = "mazeCompletedTime";

    public static final String MAZE_TYPE_ID = "mazeType";

    private static final String TAG = "MazeGameFragment";

    private Callback mCallback;

    /**
     * Callback the attached Activity must implement for updates to the UI.
     */
    public interface Callback {
        public void onScores(int mazeType);

        public void onStartMenu();
    }

    /**
     * Create a new instance of MazeGameFragment, with the supplied mazeType
     */
    public static MazeGameFragment newInstance(int mazeType) {
        MazeGameFragment f = new MazeGameFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(GameActivity.MAZE_TYPE, mazeType);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        try {
            mCallback = (Callback) act;
        } catch (ClassCastException e) {
            throw new ClassCastException(act.toString()
                    + " must implement MazeGameFragment.Callback");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mMazeType = args.getInt(GameActivity.MAZE_TYPE, -1);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGameServicesHelper = ((GameActivity) getActivity()).getGameServicesHelper();
        if (savedInstanceState != null) {
            // restore previous state
            mMazeThread.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mOptionsMenu = menu;
        inflater.inflate(R.menu.maze_game_activity_actions, menu);

    }

    /**
     * Recursively looks through all of the children views of ViewGroup and
     * returns the first instance of MazeSurfaceView or null if none is found.
     */
    private MazeSurfaceView getMazeSurfaceViewIn(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); ++i) {
            View child = v.getChildAt(i);
            if (child instanceof MazeSurfaceView) {
                return (MazeSurfaceView) child;
            } else if (child instanceof ViewGroup) {
                MazeSurfaceView m = getMazeSurfaceViewIn((ViewGroup) child);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.maze_game, container, false);
        mMazeView = getMazeSurfaceViewIn((ViewGroup) v);
        if (mMazeView == null) {
            Log.e(TAG, "Layout must contain a class of type MazeSurfaceView");
        }
        if (mMazeThread == null) {
            mMazeThread = new MazeThread(mMazeView.getHolder(), getActivity(), this, mMazeType);
            mMazeView.setThread(mMazeThread);
            mMazeThread.start();
        } else {
            mMazeThread.unpause();
        }

        return v;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMazeThread.pause();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMazeThread.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMazeThread.unpause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMazeThread.saveState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        if (mMazeThread != null) {
            mMazeThread.halt();
            boolean retry = true;
            while (retry) {
                try {
                    mMazeThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }
        mMazeThread = null;
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        final MenuItem menuItem = menu.findItem(R.id.maze_timer);
        if (menuItem != null) {
            final TextView textView = (TextView) menuItem.getActionView();
            textView.setText(mTimerText);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.reset_maze:
                mMazeThread.newMaze();
                return true;
            case android.R.id.home:
                mCallback.onStartMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called from MazeThread, must be thread safe!
     */
    @Override
    public void mazeCompleted(final int time, final int mazeType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // display congratulatory dialog
                mCongratulationsDialog = MazeCompletedDialog.newInstance(time, mazeType);
                mCongratulationsDialog.show(getFragmentManager(), "TAG_MAZE_COMPLETED");

                // Submit score to leaderboard
                if (mGameServicesHelper.isSignedIn()) {
                    switch (mazeType) {
                        case GameActivity.PERFECT_MAZE:
                            Games.Leaderboards.submitScore(mGameServicesHelper.getApiClient(),
                                    getString(R.string.leaderboard_easy), time);
                            if (time < MINIMUM_TIME_TO_UNLOCK_NOVICE) {
                                Games.Achievements.unlock(mGameServicesHelper.getApiClient(),
                                        getString(R.string.achievement_easy_maze_novice));
                            }
                            if (time < MINIMUM_TIME_TO_UNLOCK_MASTER) {
                                Games.Achievements.unlock(mGameServicesHelper.getApiClient(),
                                        getString(R.string.achievement_easy_maze_master));
                            }
                            break;
                        case GameActivity.GROWING_TREE_MAZE:
                            Games.Leaderboards.submitScore(mGameServicesHelper.getApiClient(),
                                    getString(R.string.leaderboard_medium), time);
                            if (time < MINIMUM_TIME_TO_UNLOCK_NOVICE) {
                                Games.Achievements.unlock(mGameServicesHelper.getApiClient(),
                                        getString(R.string.achievement_medium_maze_novice));
                            }
                            if (time < MINIMUM_TIME_TO_UNLOCK_MASTER) {
                                Games.Achievements.unlock(mGameServicesHelper.getApiClient(),
                                        getString(R.string.achievement_medium_maze_master));
                            }
                            break;
                        case GameActivity.DFS_MAZE:
                            Games.Leaderboards.submitScore(mGameServicesHelper.getApiClient(),
                                    getString(R.string.leaderboard_hard), time);
                            if (time < MINIMUM_TIME_TO_UNLOCK_NOVICE) {
                                Games.Achievements.unlock(mGameServicesHelper.getApiClient(),
                                        getString(R.string.achievement_hard_maze_novice));
                            }
                            if (time < MINIMUM_TIME_TO_UNLOCK_MASTER) {
                                Games.Achievements.unlock(mGameServicesHelper.getApiClient(),
                                        getString(R.string.achievement_hard_maze_master));
                            }
                            break;
                    }
                }

            }
        });
    }

    /**
     * Called from MazeThread, must be thread safe!
     */
    @Override
    public void updateTimer(final int timeElapsed) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTimerText = millisToString(timeElapsed);
                if (mOptionsMenu != null) {
                    onPrepareOptionsMenu(mOptionsMenu);
                }
            }
        });

    }

    /**
     * Called from MazeThread, must be thread safe!
     */
    @Override
    public void dismissMazeFinishedDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCongratulationsDialog != null)
                    mCongratulationsDialog.dismiss();

            }
        });

    }

    public void reset() {
        mMazeThread.newMaze();

    }

    /**
     * Convert an integer representing time in milliseconds to a pretty string
     * format.
     */
    private String millisToString(int time) {
        int millis = (time % 1000) / 100;
        int second = (time / 1000) % 60;
        int minute = (time / (1000 * 60)) % 60;
        int hour = (time / (1000 * 60 * 60)) % 24;
        String string;

        if (hour > 0) {
            string = String.format(Locale.US, "%d:%02d:%02d.%d", hour, minute, second, millis);
        } else if (minute > 0) {
            string = String.format(Locale.US, "%d:%02d.%d", minute, second, millis);
        } else {
            string = String.format(Locale.US, "%d.%d", second, millis);
        }

        return string;
    }

}
