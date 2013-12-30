package OpenGL;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;


/**
 * Provides drawing instructions for a GLSurfaceView object. This class must
 * override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
	private final int NUM_OF_SQUARES = 100;

	private static final String TAG = "MyGLRenderer";
	private Square[] mSquares = new Square[NUM_OF_SQUARES];

	// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[][] mViewMatrixs = new float[NUM_OF_SQUARES][16];

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

		// Set the background frame color
		GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);

		for (int i = 0; i < mSquares.length; i++) {
			if (i % 2 == 0) {
				// White square
				mSquares[i] = new Square(1.0f, 1.0f, 1.0f, 1.0f);
			} else {
				// Red square
				mSquares[i] = new Square(1.0f, 0, 0, 1.0f);
			}
		}
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		float squareWidth = 2 / (float) NUM_OF_SQUARES;
		float xTranslation = squareWidth / 2.0f - 1;
		float increment = 2 / (float) NUM_OF_SQUARES;
		for (int i = 0; i < NUM_OF_SQUARES; i++) {
			// Set the camera position (View matrix)
			Matrix.setLookAtM(mViewMatrixs[i], 0, 0, 0, 4, 0f, 0f,
					0f, 0f, 1.0f, 0.0f);

			// Translate
			Matrix.translateM(mViewMatrixs[i], 0, xTranslation, squareWidth / 2.0f + 1, 0);
			xTranslation += increment;

			// Scale the matrix
			Matrix.scaleM(mViewMatrixs[i], 0, squareWidth, squareWidth, 1);

			// Calculate the projection and view transformation
			Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0,
					mViewMatrixs[i], 0);

			// Draw square
			mSquares[i].draw(mMVPMatrix);
		}

	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	}

	/**
	 * Utility method for compiling a OpenGL shader.
	 * 
	 * <p>
	 * <strong>Note:</strong> When developing shaders, use the checkGlError()
	 * method to debug shader coding errors.
	 * </p>
	 * 
	 * @param type
	 *            - Vertex or fragment shader type.
	 * @param shaderCode
	 *            - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 * 
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 * 
	 * If the operation is not successful, the check throws an error.
	 * 
	 * @param glOperation
	 *            - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}
}