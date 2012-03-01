/**
 * 
 */
package rmcommon.visual;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import rmcommon.Log;
import rmcommon.geometry.FieldMapping;
import rmcommon.geometry.GeometryData;

/**
 * @author CreaByte
 * 
 */
public class OpenGLBase {

	public static final float FRAME_INCREASE = 0.01f;

	/**
	 * Offset for the color data in the float buffer, for each field
	 */
	private int[] _color_off;
	protected float _height = 800f;

	/**
	 * Offset for the faces data in short buffer
	 */
	private int _faces_off = 0;

	/**
	 * Offset for the wireframe (edges) data in float buffer
	 */
	private int _indexwf_off = 0;

	/**
	 * Offset in the float buffer for the normal data
	 */
	private int[] _normal_off;

	/**
	 * Offset for the node data in float buffer
	 */
	private int[] _vertex_off;

	protected float _width = 480f;

	protected float[] AR = { 1f, 1f }; // aspect ratio

	// Camera control
	protected Camera camera;
	protected float currentFramef = 0f;

	/**
	 * The currently plotted color field
	 */
	protected int currentColorField = 0;

	protected int currentFrame = 0, oldFrame = 0;

	protected VisualizationData vData;

	protected FloatBuffer floatBuf;
	protected ShortBuffer shortBuf;

	protected boolean isconstant = false;
	public boolean isContinuousRotation = true;

	public boolean isFrontFace = true;
	protected boolean ispaused = false;
	protected String[] names;

	protected float pos[] = { 0f, 0f, 0f }; // touchscreeen control data
	/**
	 * scaling ratio (for zooming)
	 */
	public float scale_rat = 1.0f;

	public OpenGLBase(VisualizationData vData) {
		this.vData = vData;
		// Use the (perhaps global) buffers from VisData
		floatBuf = vData.getFloatBuffer();
		shortBuf = vData.getShortBuffer();
	}

	protected void frameRendered() {
		// Draw next animation frame if there are more than one
		if (!ispaused && (vData.numFrames > 1 || _vertex_off.length > 1)) {
			increaseFrame(FRAME_INCREASE);
		}
	}

	public boolean isPaused() {
		return ispaused;
	}

	protected int getCurrentColorOffset() {
		return _color_off[currentColorField] + (currentFrame) * (vData.getGeometryData().getNumVertices() * 4);
	}

	protected int getCurrentVertexOffset() {
		return _vertex_off[currentFrame];
	}

	protected int getFaceOffset() {
		return _faces_off;
	}

	protected int getCurrentNormalsOffset() {
		return _normal_off[currentFrame];
	}

	protected int getCurrentWireframeOffset() {
		return _indexwf_off;
	}

	protected float getBoxSize() {
		return vData.getGeometryData().boxsize;
	}

	protected boolean is2D() {
		return vData.getGeometryData().is2D();
	}

	protected int getNumFaces() {
		return vData.getGeometryData().numFaces;
	}

	/**
	 * Initializes the rendering process (Vertex, face, color and normal openGL
	 * buffers)
	 * 
	 * Fills the short/float buffers and records the offsets for certain parts.
	 * 
	 */
	protected void initRendering() {

		GeometryData gData = vData.getGeometryData();
		/*
		 * Camera setup
		 */
		camera = new Camera();
		// set initial position away from the model in the y-direction
		// looking toward the center of the model (0,0,0) horizontally
		camera.setCamera(0f, -gData.boxsize, 0f, 0f, 1f, 0f, 0f, 0f, 1f);

		/*
		 * Fill the buffers
		 */
		currentFrame = 0;
		currentFramef = 0.0f;

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
		float[][] v = gData.getVertices();
		_vertex_off = new int[v.length];
		for (int i = 0; i < v.length; i++) {
			_vertex_off[i] = curFloatBufOffset;
			floatBuf.put(v[i]);
			curFloatBufOffset += v[i].length;
			Log.d("OpenGLBase", "FloatBuffer: Added " + v[i].length + " float values for " + gData.getNumVertices()
					+ " vertices in set " + (i + 1) + ". Fill state: " + curFloatBufOffset + "/" + floatBuf.capacity());
		}

		/**
		 * Element faces buffer
		 */
		_faces_off = curShortBufOffset;
		shortBuf.put(gData.faces);
		curShortBufOffset += gData.faces.length;
		Log.d("OpenGLBase", "ShortBuffer: Added " + gData.faces.length + " short values for " + gData.numFaces
				+ " element faces. Fill state: " + curShortBufOffset + "/" + shortBuf.capacity());

		/**
		 * Element edges buffer
		 */
		_indexwf_off = curShortBufOffset;
		shortBuf.put(gData.faceWireframe);
		curShortBufOffset += gData.faceWireframe.length;
		Log.d("OpenGLBase", "ShortBuffer: Added " + gData.faceWireframe.length
				+ " short values for faces wireframe. Fill state: " + curShortBufOffset + "/" + shortBuf.capacity());

		/**
		 * Colors for each visualization field.
		 * 
		 * Animation color buffer contains RBSystem.getVisualNumTimesteps()
		 * times the color data for a single solution.
		 */
		_color_off = new int[vData.getNumVisFeatures()];
		names = new String[_color_off.length];
		for (int i = 0; i < vData.getNumVisFeatures(); i++) {
			_color_off[i] = curFloatBufOffset;
			VisualFeature vf = vData.getVisualizationFeature(i);
			float[] col = vf.Colors;
			names[i] = vf.Name;
			if (vf.Source != null && vf.Source.descriptor.Mapping == FieldMapping.ELEMENT) {
				col = elementToVertexColors(col);
			}
			floatBuf.put(col);
			curFloatBufOffset += col.length;
			Log.d("OpenGLBase", "FloatBuffer: Added " + col.length + " floats for color field '" + vf.Name + "' ("
					+ (i + 1) + "). Fill state: " + curFloatBufOffset + "/" + floatBuf.capacity());
		}

		// Init array for 3D object
		if (!gData.is2D()) {
			_normal_off = new int[gData.normal.length];
			for (int i = 0; i < _normal_off.length; i++) {
				_normal_off[i] = curFloatBufOffset;
				floatBuf.put(gData.normal[i]);
				curFloatBufOffset += gData.normal[i].length;
				Log.d("OpenGLBase", "FloatBuffer: Added " + gData.normal[i].length
						+ " floats for 3D normal data. Fill state: " + curFloatBufOffset + "/" + floatBuf.capacity());
			}
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
		Log.d("OpenGLBase", "Next color field '" + names[currentColorField] + "' (" + (currentColorField + 1) + "/"
				+ _color_off.length + ")");
	}

	/**
	 * nondelayed frame increasing
	 * 
	 * @param fdelay
	 */
	private void increaseFrame(float fdelay) {
		oldFrame = currentFrame;

		currentFramef += fdelay * vData.numFrames;
		currentFrame = (int) Math.floor(currentFramef);
		if (currentFrame >= vData.numFrames) {
			currentFrame = 0;
			currentFramef = 0;
		}
		// Log.d("OGLBase", "Current frame:"+currentFrame);
	}

	/*
	 * Conversion method for FV discretized field variables who give solution
	 * values on faces rather than nodes.
	 * 
	 * Computes the node color as mean of all adjacent face colors.
	 * 
	 * TODO move this algorithm to ROMSim, as other visualization libraries
	 * might be able to directly set colors for faces.
	 */
	private float[] elementToVertexColors(float[] faceCol) {
		GeometryData gData = vData.getGeometryData();
		int numTimeSteps = faceCol.length / (4 * gData.numFaces);
		float[] nodeCol = new float[numTimeSteps * gData.getNumVertices() * 4];

		// float T = numTimeSteps * nodes;
		// for (int ts = 0; ts < T; ts++) {
		// nodeCol[4*ts] = ts/T;
		// nodeCol[4*ts+1] = 0;
		// nodeCol[4*ts+2] = 0;
		// nodeCol[4*ts+3] = 0.8f;
		// }
		// Perform summary for each timestep (if more than one)!
		for (int ts = 0; ts < numTimeSteps; ts++) {
			int face_off = ts * 4 * gData.numFaces;
			int node_off = ts * 4 * gData.getNumVertices();
			float[] valuesAdded = new float[gData.getNumVertices()];
			for (int f = 0; f < gData.numFaces; f++) {
				// Edge 1
				int n1 = gData.faces[3 * f];
				nodeCol[node_off + 4 * n1] += faceCol[face_off + 4 * f];
				nodeCol[node_off + 4 * n1 + 1] += faceCol[face_off + 4 * f + 1];
				nodeCol[node_off + 4 * n1 + 2] += faceCol[face_off + 4 * f + 2];
				nodeCol[node_off + 4 * n1 + 3] += faceCol[face_off + 4 * f + 3];
				valuesAdded[n1]++;
				// Edge 2
				int n2 = gData.faces[3 * f + 1];
				nodeCol[node_off + 4 * n2] += faceCol[face_off + 4 * f];
				nodeCol[node_off + 4 * n2 + 1] += faceCol[face_off + 4 * f + 1];
				nodeCol[node_off + 4 * n2 + 2] += faceCol[face_off + 4 * f + 2];
				nodeCol[node_off + 4 * n2 + 3] += faceCol[face_off + 4 * f + 3];
				valuesAdded[n2]++;
				// Edge 3
				int n3 = gData.faces[3 * f + 2];
				nodeCol[node_off + 4 * n3] += faceCol[face_off + 4 * f];
				nodeCol[node_off + 4 * n3 + 1] += faceCol[face_off + 4 * f + 1];
				nodeCol[node_off + 4 * n3 + 2] += faceCol[face_off + 4 * f + 2];
				nodeCol[node_off + 4 * n3 + 3] += faceCol[face_off + 4 * f + 3];
				valuesAdded[n3]++;
			}
			// Compute means
			for (int n = 0; n < gData.getNumVertices(); n++) {
				nodeCol[node_off + 4 * n] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 1] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 2] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 3] /= valuesAdded[n];
			}
		}
		return nodeCol;
	}
}
