
package com.GavinDev.Amazing.UI;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.GavinDev.Amazing.R;
import com.GavinDev.Amazing.drawing.GolSurfaceView;
import com.GavinDev.Amazing.drawing.GolThread;

/**
 * Fragment with the main menu for the game. The main menu allows the player to
 * choose a gameplay mode (Easy, Medium or Hard), and displays the Game of Life
 * animation in the background.
 */
public class StartMenuFragment extends Fragment implements OnClickListener {
    private GolSurfaceView mGolSurfaceView;
    private GolThread mGolThread;
    private View mRootView;
    private Callback mCallback = null;

    /**
     * Callback the hosting activity must implement to recieve notice of when to
     * switch fragments.
     */
    public interface Callback {
        public void onStartMazeRequested(int mazeType);
    }

    public static StartMenuFragment newInstance() {
        return new StartMenuFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement StartMenu.Callback");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGolThread != null)
            mGolThread.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restores last state
            if (mGolThread != null)
                mGolThread.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGolThread != null)
            mGolThread.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGolThread != null)
            mGolThread.unpause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.start_menu, container, false);
        }

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
                "pref_start_background", true)) {
            // GOL is enabled
            if (mGolSurfaceView == null) {
                mGolSurfaceView = new GolSurfaceView(getActivity());
                mGolSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                ((ViewGroup) mRootView).addView(mGolSurfaceView, 0);
            }
            if (mGolThread == null) {
                mGolThread = new GolThread(mGolSurfaceView.getHolder());
                mGolSurfaceView.setThread(mGolThread);
                mGolThread.start();
            } else {
                mGolThread.unpause();
            }
        }
        final int[] CLICKABLES = new int[] {
                R.id.start_kruskals_maze, R.id.start_medium_maze,
                R.id.start_recursive_backtracker_maze
        };
        for (int i : CLICKABLES) {
            mRootView.findViewById(i).setOnClickListener(this);
        }

        return mRootView;
    }

    public void setCallback(Callback l) {
        mCallback = l;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_kruskals_maze:
                mCallback.onStartMazeRequested(GameActivity.PERFECT_MAZE);
                break;
            case R.id.start_medium_maze:
                mCallback.onStartMazeRequested(GameActivity.GROWING_TREE_MAZE);
                break;
            case R.id.start_recursive_backtracker_maze:
                mCallback.onStartMazeRequested(GameActivity.DFS_MAZE);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        if (mGolThread != null) {
            mGolThread.pause();
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mGolThread != null) {
            mGolThread.halt();
            boolean retry = true;
            while (retry) {
                try {
                    mGolThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
            mGolThread = null;
        }
    }
}
