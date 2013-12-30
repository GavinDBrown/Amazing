package OpenGL;

import com.TeamAmazing.activities.MazeGame;
import com.TeamAmazing.game.R;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

public class OpenGLES20Activity extends Activity {
	public final static int PERFECT_MAZE = 0;
	public final static int DFS_MAZE = 1;
	public final static String MAZE_TYPE = "com.TeamAmazing.game.StartMenu.MAZE_TYPE";

    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        setContentView(R.layout.main);
        setContentView(R.layout.start_menu);
//
//        myRenderer = new MyRenderer(); // create the renderer object
//
//        GLSurfaceView mGLView = (GLSurfaceView)findViewById(R.id.glsurfaceview1);
//        mGLView.setEGLConfigChooser(true);
//        mGLView.setRenderer(myRenderer); // set the surfaceView to use the renderer

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new MyGLSurfaceView(this);
//        setContentView(mGLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }
    
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