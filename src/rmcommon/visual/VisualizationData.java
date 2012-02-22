package rmcommon.visual;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rmcommon.ComplexSolutionField;
import rmcommon.Log;
import rmcommon.DefaultSolutionField;
import rmcommon.SimulationResult;
import rmcommon.LogicSolutionField;
import rmcommon.geometry.DisplacementField;
import rmcommon.geometry.FieldMapping;
import rmcommon.geometry.GeometryData;

public class VisualizationData {

	/**
	 * The default size for the short buffers
	 */
	public static final int SHORT_MAX = 250000;

	/**
	 * The default size for the float buffers
	 */
	public static final int FLOAT_MAX = 1000000;

	/**
	 * Allocates short and float buffers for the rendering process and sets the
	 * position to zero.
	 * 
	 */
	public static FloatBuffer createFloatBuffer(int size) {
		Log.d("VisualizationData", "Allocating GL float buffer:" + size * 4 + " bytes");
		ByteBuffer fbb = ByteBuffer.allocateDirect(size * 4);
		fbb.order(ByteOrder.nativeOrder()).position(0);
		return fbb.asFloatBuffer();
	}

	/**
	 * Creates the default float buffer of size FLOAT_MAX
	 * 
	 * @return
	 */
	public static FloatBuffer createFloatBuffer() {
		return createFloatBuffer(FLOAT_MAX);
	}

	/**
	 * Allocates short and float buffers for the rendering process and sets the
	 * position to zero.
	 * 
	 */
	public static ShortBuffer createShortBuffer(int size) {
		Log.d("VisualizationData", "Allocating GL short buffer:" + size * 2 + " bytes");
		ByteBuffer vbb = ByteBuffer.allocateDirect(size * 2);
		vbb.order(ByteOrder.nativeOrder()).position(0);
		return vbb.asShortBuffer();
	}

	/**
	 * Creates the default float buffer of size FLOAT_MAX
	 * 
	 * @return
	 */
	public static ShortBuffer createShortBuffer() {
		return createShortBuffer(SHORT_MAX);
	}

	/**
	 * The node color data for each field
	 */
	public List<VisualFeature> fieldColors;

	private List<LogicSolutionField> logicfields;

	private FloatBuffer floatBuf;
	private ShortBuffer shortBuf;

	public int numFrames;

	private GeometryData gData;

	public VisualizationData(GeometryData fGeo) {
		this(fGeo, createFloatBuffer(), createShortBuffer());
	}

	public VisualizationData(GeometryData fGeo, FloatBuffer fBuf, ShortBuffer sBuf) {
		// Use size one per default
		fieldColors = new ArrayList<VisualFeature>(1);
		this.gData = fGeo;
		floatBuf = fBuf;
		shortBuf = sBuf;
	}

	public GeometryData getGeometryData() {
		return gData;
	}

	public int getNumVisFeatures() {
		return fieldColors.size();
	}

	/**
	 * @param featureNr
	 * @return The field colors for the specified field
	 */
	public VisualFeature getVisualizationFeature(int featureNr) {
		return fieldColors.get(featureNr);
	}

	public void useResult(SimulationResult res) {
		numFrames = 0;
		int tmp = 0;
		logicfields = null;
		for (LogicSolutionField f : res.getLogicFields()) {
			/**
			 * Apply displacements to geometry for displacement fields (if there
			 * should be more than one they are simply applied both)
			 */
			if (f instanceof DisplacementField) {
				tmp = gData.addDisplacements((DisplacementField) f);
			} else {
				/**
				 * Check resulting frames for each field
				 */
				tmp = f.getSize() / gData.numVertices;
				if (f.descriptor.Mapping == FieldMapping.ELEMENT) {
					tmp = f.getSize() / gData.faces;
				}
			}
			Log.d("VisData", "Frames computed for field '" + f.descriptor.Name + "' (" + f.descriptor.Type + "): "
					+ tmp);

			if (numFrames == 0) {
				numFrames = tmp;
			} else if (tmp != numFrames) {
				throw new RuntimeException("Incompatible amount of frames for different solution fields! Current:"
						+ numFrames + ", new:" + tmp);
			}
		}
		logicfields = res.getLogicFields();
	}

	// /**
	// * Checks if the logical solution field is constant or not.
	// *
	// * @param fieldnr
	// * @return
	// */
	// public boolean isConstantFeature(int fieldnr) {
	// return fieldColors != null ? fieldColors.get(fieldnr).isConstant() :
	// true;
	// }

	/**
	 * calculate the color data (red green blue alpha) from the solution field
	 * TODO: enable different colormaps via model.xml (i.e. matlab's color maps)
	 * 
	 * @param cg
	 *            The color generator
	 * 
	 *            TODO create fieldColorName list and display in plot
	 */
	public void computeVisualFeatures(ColorGenerator cg) {
		// Clear old colors
		fieldColors.clear();
		if (logicfields != null) {
			for (int fieldNr = 0; fieldNr < logicfields.size(); fieldNr++) {
				LogicSolutionField f = logicfields.get(fieldNr);
				if (f.isConstant()) {
					Log.d("VisData", "Using default colors " + Arrays.toString(cg.getDefaultColor(1))
							+ " for constant logical field '" + f.descriptor + "'");
					fieldColors.add(new VisualFeature(f.descriptor.Name + " (constant)",
							cg.getDefaultColor(f.getSize())));
				} else {
					/*
					 * Check if the colors have to be mapped from element to
					 * vertex values
					 */
					Log.d("VisData",
							"Computing visual features for logical field '" + f.descriptor + "' of size " + f.getSize());
					/*
					 * Decide which feature to present/compute depending on
					 * logical field
					 */
					for (VisualFeature vf : f.getVisualFeatures(cg)) {
						fieldColors.add(vf);
					}
				}
			}
		} else {
			Log.w("VisData",
					"No solution fields given, using default color data " + Arrays.toString(cg.getDefaultColor(1))
							+ " for " + gData.numVertices + " vertices");
			fieldColors.add(new VisualFeature("No field data", cg.getDefaultColor(gData.numVertices)));
		}
	}

	// private float[] getColors(ColorGenerator cg, float[] values, boolean
	// convert) {
	// float[] colors = cg.computeColors(values);
	// if (convert) {
	// Log.d("VisualizationData",
	// "Converting element color data to vertex color data");
	// colors = elementToVertexColors(colors);
	// }
	// return colors;
	// }

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
		int numTimeSteps = faceCol.length / (4 * gData.faces);
		float[] nodeCol = new float[numTimeSteps * gData.numVertices * 4];

		// float T = numTimeSteps * nodes;
		// for (int ts = 0; ts < T; ts++) {
		// nodeCol[4*ts] = ts/T;
		// nodeCol[4*ts+1] = 0;
		// nodeCol[4*ts+2] = 0;
		// nodeCol[4*ts+3] = 0.8f;
		// }
		// Perform summary for each timestep (if more than one)!
		for (int ts = 0; ts < numTimeSteps; ts++) {
			int face_off = ts * 4 * gData.faces;
			int node_off = ts * 4 * gData.numVertices;
			float[] valuesAdded = new float[gData.numVertices];
			for (int f = 0; f < gData.faces; f++) {
				// Edge 1
				int n1 = gData.face[3 * f];
				nodeCol[node_off + 4 * n1] += faceCol[face_off + 4 * f];
				nodeCol[node_off + 4 * n1 + 1] += faceCol[face_off + 4 * f + 1];
				nodeCol[node_off + 4 * n1 + 2] += faceCol[face_off + 4 * f + 2];
				nodeCol[node_off + 4 * n1 + 3] += faceCol[face_off + 4 * f + 3];
				valuesAdded[n1]++;
				// Edge 2
				int n2 = gData.face[3 * f + 1];
				nodeCol[node_off + 4 * n2] += faceCol[face_off + 4 * f];
				nodeCol[node_off + 4 * n2 + 1] += faceCol[face_off + 4 * f + 1];
				nodeCol[node_off + 4 * n2 + 2] += faceCol[face_off + 4 * f + 2];
				nodeCol[node_off + 4 * n2 + 3] += faceCol[face_off + 4 * f + 3];
				valuesAdded[n2]++;
				// Edge 3
				int n3 = gData.face[3 * f + 2];
				nodeCol[node_off + 4 * n3] += faceCol[face_off + 4 * f];
				nodeCol[node_off + 4 * n3 + 1] += faceCol[face_off + 4 * f + 1];
				nodeCol[node_off + 4 * n3 + 2] += faceCol[face_off + 4 * f + 2];
				nodeCol[node_off + 4 * n3 + 3] += faceCol[face_off + 4 * f + 3];
				valuesAdded[n3]++;
			}
			// Compute means
			for (int n = 0; n < gData.numVertices; n++) {
				nodeCol[node_off + 4 * n] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 1] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 2] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 3] /= valuesAdded[n];
			}
		}
		return nodeCol;
	}

	public FloatBuffer getFloatBuffer() {
		return floatBuf;
	}

	public ShortBuffer getShortBuffer() {
		return shortBuf;
	}
}