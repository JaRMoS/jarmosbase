/**
 * 
 */
package rmcommon.visual;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.Animator;

/**
 * @author CreaByte
 * 
 */
public class JOGLRenderer extends OpenGLBase implements GLEventListener {
	
	public static void render(VisualizationData vData) {
		java.awt.Frame frame = new java.awt.Frame("Model visualization");
		frame.setSize(300, 300);
		frame.setLayout(new java.awt.BorderLayout());

		final Animator animator = new Animator();
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});

		GLCanvas canvas = new GLCanvas();
		animator.add(canvas);
		// GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
		// GLCanvas canvas = new GLCanvas(caps);

		final JOGLRenderer rend = new JOGLRenderer(vData);
		canvas.addGLEventListener(rend);

		frame.add(canvas, java.awt.BorderLayout.CENTER);
		frame.validate();

		frame.setVisible(true);
		animator.start();
	}

	public JOGLRenderer(VisualizationData vData) {
		super(vData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable
	 * )
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		initRendering();

		// define the color we want to be displayed as the "clipping wall"
		GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glMatrixMode(GL2.GL_PROJECTION);

		float exrat; // marginal extension ratio
		if (fGeoData.is2D())
			exrat = 0.65f;
		else
			exrat = 0.95f;
		// orthographic view
		gl.glOrthof(-exrat * fGeoData.boxsize / AR[0], exrat * fGeoData.boxsize / AR[0], -exrat * fGeoData.boxsize
				/ AR[1], exrat * fGeoData.boxsize / AR[1], -100, 100);

		gl.glViewport(0, 0, (int) _width, (int) _height);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// define the color we want to be displayed as the "clipping wall"
		// gl.glClearColor(0f, 0f, 0f, 1.0f);
		gl.glClearColor(1f, 1f, 1f, 1.0f);

		// enable the differentiation of which side may be visible
		gl.glEnable(GL2.GL_CULL_FACE);
		// which is the front? the one which is drawn counter clockwise
		gl.glFrontFace(GL2.GL_CCW);
		// which one should NOT be drawn
		gl.glCullFace(GL2.GL_BACK);

		// Switch on client states in order to make GL10 use the vertex and
		// color data
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

		// Enable normal for 3D object
		if (!fGeoData.is2D())
			gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.media.opengl.GLEventListener#dispose(javax.media.opengl.GLAutoDrawable
	 * )
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		// clear the screen to black (0,0,0) color
		GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		if (!fGeoData.is2D()) // enable depth test for 3D rendering
			gl.glEnable(GL2.GL_DEPTH_TEST);

		if ((isFrontFace) || (fGeoData.is2D())) {
			// enable blending (for rendering wireframe)
			if (!fGeoData.is2D())
				gl.glDisable(GL2.GL_CULL_FACE);
			gl.glFrontFace(GL2.GL_CCW);
		} else {
			gl.glEnable(GL2.GL_CULL_FACE);
			gl.glFrontFace(GL2.GL_CW);
		}

		// reset transformation matrix
		gl.glLoadIdentity();

		// setup Light
		if (!fGeoData.is2D()) {
			gl.glEnable(GL2.GL_LIGHTING); // Enable light
			gl.glEnable(GL2.GL_LIGHT0); // turn on the light
			gl.glEnable(GL2.GL_COLOR_MATERIAL); // turn on color lighting

			// material shininess
			gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 128);

			// ambient light
			float lightAmbient[] = { 0.5f, 0.5f, 0.5f, 1.0f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
			// diffuse light
			float lightDiffuse[] = { 0.8f, 0.8f, 0.8f, 1.0f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
			// specular light
			float[] lightSpecular = { 0.7f, 0.7f, 0.7f, 1.0f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
			// light position
			float[] lightPosition = { -fGeoData.boxsize, -fGeoData.boxsize, 0.0f, 0.0f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
			// light direction
			float[] lightDirection = { fGeoData.boxsize, fGeoData.boxsize, fGeoData.boxsize, 0.0f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, lightDirection, 0);
			// 90 degree FOV
			gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 45.0f);

			// using our normal data
			floatBuf.position(_normal_off);
			gl.glNormalPointer(GL2.GL_FLOAT, 0, floatBuf);
		}

		// zoom in/out the model
		gl.glScalef(scale_rat, scale_rat, scale_rat);

		/*
		 * touchscreen control Rotation, zoom etc
		 */
		if (fGeoData.is2D()) {// we just move the object around in 2D cases
			gl.glTranslatef(pos[0] * fGeoData.boxsize / 20f, pos[1] * fGeoData.boxsize / 20f, pos[2] * fGeoData.boxsize
					/ 20f);
		} else { // but we rotate the object in 3D cases
					// set yawing/pitching rotation angles and update camera
			camera.SetRotation(-pos[0] * 8f, -pos[1] * 8f);
			// update rotation matrix
			gl.glMultMatrixf(camera.M, 0);
			// update rotation parameters
			if (isContinuousRotation) {
				float minrot = 0.02f / scale_rat;
				// delay the rotation parameters...
				pos[0] = pos[0] * (1 - (float) Math.exp(-Math.abs(pos[0])));
				pos[1] = pos[1] * (1 - (float) Math.exp(-Math.abs(pos[1])));
				pos[0] = Math.abs(pos[0]) > 3.00f ? Math.signum(pos[0]) * 3.00f : pos[0];
				pos[1] = Math.abs(pos[1]) > 3.00f ? Math.signum(pos[1]) * 3.00f : pos[1];
				pos[0] = Math.abs(pos[0]) > minrot ? pos[0] : Math.signum(pos[0]) * minrot;
				pos[1] = Math.abs(pos[1]) > minrot ? pos[1] : Math.signum(pos[1]) * minrot;
			} else {
				// reset the rotation parameters
				pos[0] = 0.0f;
				pos[1] = 0.0f;
			}

			// gl.glTranslatef(-camera.Position[0],-camera.Position[1],-camera.Position[2]);
		}

		/*
		 * Set pointer to vertex data Always uses currentFrame, which is zero in
		 * case of no animation.
		 */
		floatBuf.position(_vertex_off + currentFrame * (fGeoData.numVertices * 3));
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, floatBuf);

		/*
		 * specify the color data for the current frame Four values each: R, G,
		 * B, Alpha
		 */
		floatBuf.position(_color_off[currentColorField] + currentFrame * (fGeoData.numVertices * 4));
		// if (oldFrame != currentFrame) {
		// Log.d("GLRenderer", "Plotting frame " + currentFrame +
		// " with color pointer at " + floatBuf.position());
		// int oldpos = floatBuf.position();
		// float[] data = new float[100];
		// floatBuf.get(data, 0, 100);
		// Log.d("GLRenderer", "Floats read for current frame: " +
		// Arrays.toString(data));
		// floatBuf.position(oldpos);
		// }
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, floatBuf);

		/*
		 * Draw the elements using the above declared nodes and color data
		 */

		shortBuf.position(_faces_off);
		gl.glDrawElements(GL2.GL_TRIANGLES, fGeoData.faces * 3, GL2.GL_UNSIGNED_SHORT, shortBuf);

		// Draw the wireframe for a n field object
		// if ((vData.isConstantFeature(currentColorField)) |
		// (!fGeoData.is2D())) {
		// // Draw the wireframe mesh
		// gl.glColor4f(0.1f, 0.1f, 0.1f, 0.5f);
		// shortBuf.position(_indexwf_off);
		// gl.glDrawElements(GL2.GL_LINES, fGeoData.faces * 6,
		// GL2.GL_UNSIGNED_SHORT, shortBuf);
		// }

		// Draw next animation frame if there are more than one
		if (!ispaused && vData.numFrames > 1) {
			increase_frame(0.01f);
		}
	}

	/**
	 * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable,
	 *      int, int, int, int)
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glViewport(0, 0, w, h);
	}

}
