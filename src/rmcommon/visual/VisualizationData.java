package rmcommon.visual;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rmcommon.Log;
import rmcommon.LogicSolutionField;
import rmcommon.SimulationResult;
import rmcommon.geometry.DisplacementField;
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
	private List<VisualFeature> visFeatures;

	// private List<LogicSolutionField> logicfields;

	private FloatBuffer floatBuf;
	private ShortBuffer shortBuf;

	public int numFrames;

	private GeometryData gData;
	private SimulationResult simres;

	public VisualizationData(GeometryData fGeo) {
		this(fGeo, createFloatBuffer(), createShortBuffer());
	}

	public VisualizationData(GeometryData fGeo, FloatBuffer fBuf, ShortBuffer sBuf) {
		// Use size one per default
		visFeatures = new ArrayList<VisualFeature>(1);
		this.gData = fGeo;
		floatBuf = fBuf;
		shortBuf = sBuf;
	}

	public GeometryData getGeometryData() {
		return gData;
	}

	public int getNumVisFeatures() {
		return visFeatures.size();
	}

	/**
	 * @param featureNr
	 * @return The field colors for the specified field
	 */
	public VisualFeature getVisualizationFeature(int featureNr) {
		return visFeatures.get(featureNr);
	}

	public void useResult(SimulationResult res) {
		if (res.getNumParts() != res.getTransforms().size()) {
			throw new RuntimeException("Invalid simulation result, number of parts (" + res.getNumParts()
					+ ") does not match transformation count (" + res.getTransforms().size()
					+ "). Forgot to add default transformation?");
		}

		// Handle geometric transformations
		gData.createMesh(res.getTransforms(), !res.hasDisplacements());

		for (LogicSolutionField f : res.getLogicFields()) {
			/**
			 * Apply displacements to geometry for displacement fields (if there
			 * should be more than one they are simply applied both)
			 */
			if (f instanceof DisplacementField) {
				gData.addDisplacements((DisplacementField) f, res.getNumParts());
			}
		}
		simres = res;
		numFrames = res.getNumParts();
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
		visFeatures.clear();
		if (simres.getLogicFields() != null) {
			// For displacement-only we add an default-color field
			if (simres.hasDisplacements() && simres.getNumValueFields() == 1) {
				visFeatures.add(new VisualFeature("Displacements", cg.getDefaultColor(gData.getNumVertices() * simres.getNumParts()), null));
			}
			for (int fieldNr = 0; fieldNr < simres.getLogicFields().size(); fieldNr++) {
				LogicSolutionField f = simres.getLogicFields().get(fieldNr);
				if (f.isConstant()) {
					Log.d("VisData", "Using default colors " + Arrays.toString(cg.getDefaultColor(1))
							+ " for constant logical field '" + f.descriptor + "'");
					visFeatures.add(new VisualFeature(f.descriptor.Name + " (constant)",
							cg.getDefaultColor(f.getSize()), f));
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
						visFeatures.add(vf);
					}
				}
			}
		} else {
			Log.w("VisData",
					"No solution fields given, using default color data " + Arrays.toString(cg.getDefaultColor(1))
							+ " for " + gData.getNumVertices() + " vertices");
			visFeatures.add(new VisualFeature("No field data", cg.getDefaultColor(gData.getNumVertices()), null));
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

	public FloatBuffer getFloatBuffer() {
		return floatBuf;
	}

	public ShortBuffer getShortBuffer() {
		return shortBuf;
	}
}