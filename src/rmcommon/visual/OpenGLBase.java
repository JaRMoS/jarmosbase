/**
 * 
 */
package rmcommon.visual;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import rmcommon.Log;
import rmcommon.geometry.GeometryData;

/**
 * @author CreaByte
 * 
 */
public class OpenGLBase {

	/**
	 * Offset for the color data in the float buffer, for each field
	 */
	protected int[] _color_off;
	protected float _height = 800f;

	/**
	 * Offset for the faces data in short buffer
	 */
	protected int _faces_off = 0;

	/**
	 * Offset for the wireframe (edges) data in float buffer
	 */
	protected int _indexwf_off = 0;

	/**
	 * Offset in the float buffer for the normal data
	 */
	protected int _normal_off = 0;

	/**
	 * Offset for the node data in float buffer
	 */
	protected int _vertex_off = 0;

	protected float _width = 480f;

	protected float[] AR = { 1f, 1f }; // aspect ratio

	// Camera control
	protected Camera camera;
	protected float current_framef = 0f;

	/**
	 * The currently plotted color field
	 */
	protected int currentColorField = 0;

	protected int currentFrame = 0, oldFrame = 0;

	public GeometryData fGeoData;
	protected VisualizationData vData;

	protected FloatBuffer floatBuf;
	protected ShortBuffer shortBuf;

	protected boolean isconstant = false;
	public boolean isContinuousRotation = true;

	public boolean isFrontFace = true;
	protected boolean ispaused = false;

	protected float pos[] = { 0f, 0f, 0f }; // touchscreeen control data
	/**
	 * scaling ratio (for zooming)
	 */
	public float scale_rat = 1.0f;

	public OpenGLBase(VisualizationData vData) {
		this.vData = vData;
		fGeoData = vData.getGeometryData();
		// Use the (perhaps global) buffers from VisData
		floatBuf = vData.getFloatBuffer();
		shortBuf = vData.getShortBuffer();
	}

	public boolean isPaused() {
		return ispaused;
	}

	/**
	 * Initializes the rendering process (Vertex, face, color and normal openGL
	 * buffers)
	 * 
	 * Fills the short/float buffers and records the offsets for certain parts.
	 * 
	 */
	protected void initRendering() {

		/*
		 * Camera setup
		 */
		camera = new Camera();
		// set initial position away from the model in the y-direction
		// looking toward the center of the model (0,0,0) horizontally
		camera.setCamera(0f, -fGeoData.boxsize, 0f, 0f, 1f, 0f, 0f, 0f, 1f);

		/*
		 * Fill the buffers
		 */
		currentFrame = 0;
		current_framef = 0.0f;

		/*
		 * Clear and init the buffers
		 */
		floatBuf.clear();
		shortBuf.clear();
		int curShortBufOffset = 0;
		int curFloatBufOffset = 0;

		/**
		 * Node float buffer (also includes animations if displacements are
		 * given)
		 */
		_vertex_off = curFloatBufOffset;
		floatBuf.put(fGeoData.vertices);
		curFloatBufOffset += fGeoData.vertices.length;
		Log.d("OpenGLBase", "FloatBuffer: Added " + fGeoData.vertices.length + " floats for vertices. Fill state: "
				+ curFloatBufOffset + "/" + floatBuf.capacity());

		/**
		 * Element faces buffer
		 */
		_faces_off = curShortBufOffset;
		shortBuf.put(fGeoData.face);
		curShortBufOffset += fGeoData.face.length;
		Log.d("OpenGLBase", "ShortBuffer: Added " + fGeoData.face.length + " short for element faces. Fill state: "
				+ curShortBufOffset + "/" + shortBuf.capacity());

		/**
		 * Element edges buffer
		 */
		_indexwf_off = curShortBufOffset;
		shortBuf.put(fGeoData.face_wf);
		curShortBufOffset += fGeoData.face_wf.length;
		Log.d("OpenGLBase", "ShortBuffer: Added " + fGeoData.face_wf.length
				+ " short for faces wireframe. Fill state: " + curShortBufOffset + "/" + shortBuf.capacity());

		/**
		 * Colors for each visualization field.
		 * 
		 * Animation color buffer contains RBSystem.getVisualNumTimesteps()
		 * times the color data for a single solution.
		 */
		_color_off = new int[vData.getNumVisFeatures()];
		for (int i = 0; i < vData.getNumVisFeatures(); i++) {
			_color_off[i] = curFloatBufOffset;
			float[] col = vData.getVisualizationFeature(i).Colors;
			floatBuf.put(col);
			curFloatBufOffset += col.length;
			Log.d("OpenGLBase", "FloatBuffer: Added " + col.length + " floats for color field " + (i + 1)
					+ ". Fill state: " + curFloatBufOffset + "/" + floatBuf.capacity());
		}

		// Init array for 3D object
		if (!fGeoData.is2D()) {
			_normal_off = curFloatBufOffset;
			floatBuf.put(fGeoData.normal);
			curFloatBufOffset += fGeoData.normal.length;
			Log.d("OpenGLBase", "FloatBuffer: Added " + fGeoData.normal.length
					+ " floats for 3D normal data. Fill state: " + curFloatBufOffset + "/" + floatBuf.capacity());
		}
	}

	/**
	 * @param iCR
	 * @param posx
	 * @param posy
	 * @param posz
	 */
	public void setPos(boolean iCR, float posx, float posy, float posz) {
		pos[0] += posx;
		pos[1] += posy;
		pos[2] += posz;
		isContinuousRotation = iCR;
	}

	/**
	 * resume animation
	 */
	public void unpause() {
		ispaused = false;
	}

	/**
	 * @param pzoom
	 */
	public void zoom(float pzoom) {
		pzoom = (pzoom < 1) ? 1 : pzoom;
		scale_rat = pzoom;
	}

	/**
	 * zoom in
	 */
	public void zoomin() {
		scale_rat += 0.1f;
	}

	/**
	 * zoom out
	 */
	public void zoomout() {
		scale_rat -= 0.1f;
		if (scale_rat < 0.25f)
			scale_rat = 0.25f;
	}

	/**
	 * pause the animation if there is any
	 */
	public void pause() {
		ispaused = true;
	}

	/**
	 * reset zoom parameter
	 */
	public void resetZoom() {
		scale_rat = 1.0f;
	}

	/**
	 * Sets the current orientation
	 * 
	 * @param pmode
	 */
	public void setOrientation(boolean pmode) {
		if (pmode) { // portrait mode
			_width = 480f;
			_height = 762f;
			AR[0] = 1.0f;
			AR[1] = _width / _height;
		} else { // landscape mode
			_width = 762f;
			_height = 480f;
			AR[0] = _height / _width;
			AR[1] = 1f;
		}
	}

	/**
	 * Shows the next color field, if available.
	 */
	public void nextColorField() {
		currentColorField++;
		currentColorField %= vData.getNumVisFeatures();
		Log.d("GLRenderer", "Next color field index: " + currentColorField + ", total: " + _color_off.length);
	}

	/**
	 * delayed frame increasing, only update animation after 5 frames
	 * 
	 * @param fdelay
	 */
	public void increase_frame(float fdelay) {
		oldFrame = currentFrame;

		current_framef += fdelay * vData.numFrames;
		currentFrame = Math.round(current_framef);
		if (currentFrame >= vData.numFrames) {
			currentFrame = 0;
			current_framef = 0;
		}
		if (currentFrame < 0) {
			currentFrame = vData.numFrames - 1;
			current_framef = vData.numFrames - 1;
		}
	}

	/**
	 * nondelayed frame increasing
	 * 
	 * @param fdelay
	 */
	public void increase_ndframe(float fdelay) {
		oldFrame = currentFrame;

		current_framef += fdelay;
		currentFrame = Math.round(current_framef);
		if (currentFrame >= vData.numFrames) {
			currentFrame = 0;
			current_framef = 0;
		}
		if (currentFrame < 0) {
			currentFrame = vData.numFrames - 1;
			current_framef = vData.numFrames - 1;
		}
	}
}
