package jarmos.visual;

import jarmos.Log;
import jarmos.geometry.FieldMapping;
import jarmos.geometry.GeometryData;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 
 * Base class for OpenGL visualisation. Provides methods to access different geometry data and is used within the
 * GLRenderer class of the JaRMoSA android app and the JOGLRenderer of JaRMoSPC.
 * 
 * @author Daniel Wirtz
 * 
 */
public class OpenGLBase {

	public enum Orientation {
		LANDSCAPE,
		PORTRAIT;
	}

	public static final float FRAME_INCREASE = 0.01f;

	/**
	 * Offset for the color data in the float buffer, for each field
	 */
	private int[] _color_off;

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

	// Camera control
	private Camera camera;

	private Orientation orientation = Orientation.LANDSCAPE;

	/**
	 * The currently plotted color field
	 */
	private int currentColorField = 0;

	private int currentFrame = 0;

	private float currentFramef = 0f;

	protected FloatBuffer floatBuf;

	/**
	 * Flag that indicates whether the object rotates continuously in the 3D case
	 */
	public boolean isContinuousRotation = true;

	/**
	 * Flag that indicates whether the front face should be rendered or not
	 */
	public boolean isFrontFace = true;

	private boolean ispaused = false;

	private String[] names;

	private float pos[] = null;

	/**
	 * scaling ratio (for zooming)
	 */
	private float scaleFactor = 1.0f;

	private float[] orthoproj = null;

	protected ShortBuffer shortBuf;

	private VisualizationData vData;

	private int w, h;

	/**
	 * Creates an OpenGLBase with the default size.
	 * 
	 * @param vData
	 */
	public OpenGLBase(VisualizationData vData) {
		this(vData, 700, 400);
	}

	public OpenGLBase(VisualizationData vData, int width, int height) {
		this.vData = vData;
		// Use the (perhaps global) buffers from VisData
		floatBuf = vData.getFloatBuffer();
		shortBuf = vData.getShortBuffer();
		setSize(width, height);
	}

	public void setSize(int width, int height) {
		w = width;
		h = height;
		updateOrthographicProjection();
		// Log.d("OpenGLBase", "set size: w"+width+" h"+height);
	}

	/**
	 * Sets the initial position away from the model in the y-direction looking toward the center of the model (0,0,0)
	 * horizontally
	 */
	public void resetView() {
		camera = new Camera(0f, -getBoxSize(), 0f, 0f, 1f, 0f, 0f, 0f, 1f);
		pos = new float[] { 0f, 0f };
		scaleFactor = 1.0f;
		isFrontFace = true;
		// Log.d("OpenGLBase", "reset view!");
	}

	private void updateOrthographicProjection() {
		float exrat, ax = 0, ay = 0; // marginal extension ratio
		if (is2D())
			exrat = 0.65f * getBoxSize();
		else
			exrat = 0.95f * getBoxSize();
		switch (orientation) {
		case PORTRAIT:
			ax = 1.0f;
			ay = (float) w / (float) h;
			break;
		case LANDSCAPE:
			ax = (float) h / (float) w;
			ay = 1.0f;
		}
		orthoproj = new float[] { -exrat / ax, exrat / ax, -exrat / ay, exrat / ay, -100, 100 };
		// Log.d("OpenGLBase", "OrthProj update: "+Arrays.toString(orthoproj));
	}

	/**
	 * @param posx
	 * @param posy
	 */
	public void addPos(float posx, float posy) {
		pos[0] += posx;
		pos[1] += posy;
	}

	/*
	 * Conversion method for FV discretized field variables who give solution values on faces rather than nodes.
	 * 
	 * Computes the node color as mean of all adjacent face colors.
	 * 
	 * TODO move this algorithm to ROMSim, as other visualization libraries might be able to directly set colors for
	 * faces.
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

	protected void frameRendered() {
		/*
		 * Draw next animation frame if there are more than one
		 */
		if (!ispaused && (vData.numFrames > 1 || _vertex_off.length > 1)) {
			currentFramef += FRAME_INCREASE * vData.numFrames;
			currentFrame = (int) Math.floor(currentFramef);
			if (currentFrame >= vData.numFrames) {
				currentFrame = 0;
				currentFramef = 0;
			}
		}

		if (!is2D()) {
			/*
			 * Set yawing/pitching rotation angles and update camera
			 */
			camera.SetRotation(-pos[0], -pos[1]);
			/*
			 * Update rotation if continuous
			 */
			if (isContinuousRotation) {
				float minrot = 0.16f / scaleFactor;
				float sgnx = Math.signum(pos[0]), sgny = Math.signum(pos[1]);
				pos[0] = sgnx * Math.max(minrot, Math.min(24.00f, sgnx * pos[0] * 0.95f));
				pos[1] = sgny * Math.max(minrot, Math.min(24.00f, sgny * pos[1] * 0.95f));
			}
		}
	}

	protected float getBoxSize() {
		return vData.getGeometryData().boxsize;
	}

	protected int getCurrentColorOffset() {
		return _color_off[currentColorField] + (currentFrame) * (vData.getGeometryData().getNumVertices() * 4);
	}

	protected int getCurrentNormalsOffset() {
		return _normal_off[currentFrame];
	}

	protected int getCurrentVertexOffset() {
		return _vertex_off[currentFrame];
	}

	protected int getCurrentWireframeOffset() {
		return _indexwf_off;
	}

	protected int getFaceOffset() {
		return _faces_off;
	}

	/**
	 * 
	 * @return The height with respect to LANDSCAPE orientation
	 */
	protected int getHeight() {
		return h;
	}

	protected int getNumFaces() {
		return vData.getGeometryData().numFaces;
	}

	protected float[] getOrtographicProj() {
		return orthoproj;
	}

	protected float[] getRotationMatrix() {
		return camera.getRotationMatrix();
	}

	protected float getScalingFactor() {
		return scaleFactor;
	}

	/**
	 * 
	 * @return The width with respect to LANDSCAPE orientation
	 */
	protected int getWidth() {
		return w;
	}

	protected float getXTranslation() {
		return pos[0] * getBoxSize() / 20f;
	}

	protected float getYTranslation() {
		return pos[1] * getBoxSize() / 20f;
	}

	/**
	 * Initializes the rendering process (Vertex, face, color and normal openGL buffers)
	 * 
	 * Fills the short/float buffers and records the offsets for certain parts.
	 * 
	 */
	protected void initRendering() {

		GeometryData gData = vData.getGeometryData();

		resetView();

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
		 * Node float buffer (also includes animations if displacements are given)
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
		 * Animation color buffer contains RBSystem.getVisualNumTimesteps() times the color data for a single solution.
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

	public boolean is2D() {
		return vData.getGeometryData().is2D();
	}

	// public boolean isPaused() {
	// return ispaused;
	// }

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
	 * Shows the next color field, if available.
	 */
	public void prevColorField() {
		if (--currentColorField == -1)
			currentColorField = vData.getNumVisFeatures() - 1;
		Log.d("OpenGLBase", "Previous color field '" + names[currentColorField] + "' (" + (currentColorField + 1) + "/"
				+ _color_off.length + ")");
	}

	/**
	 * pause the animation if there is any
	 */
	public void togglePause() {
		ispaused = !ispaused;
	}

	/**
	 * Sets the current orientation
	 * 
	 * @param pmode
	 */
	public void setOrientation(Orientation o) {
		Log.d("OpenGLBase", "set orientation:" + o.toString());
		orientation = o;
		updateOrthographicProjection();
	}

	// /**
	// * @param pzoom
	// */
	// public void zoom(float pzoom) {
	// pzoom = (pzoom < 1) ? 1 : pzoom;
	// scaleFactor = pzoom;
	// }

	/**
	 * zoom in
	 */
	public void zoomIn() {
		scaleFactor *= 1.1f;
	}

	/**
	 * zoom out
	 */
	public void zoomOut() {
		scaleFactor = Math.max(scaleFactor * 0.9f, 0.15f);
	}
}
