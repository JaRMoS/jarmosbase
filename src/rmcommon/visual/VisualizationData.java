package rmcommon.visual;

import java.util.ArrayList;
import java.util.List;

import rmcommon.Log;
import rmcommon.SolutionField;
import rmcommon.geometry.DiscretizationType;
import rmcommon.geometry.GeometryData;

public class VisualizationData {
	/**
	 * The node color data for each field
	 */
	public List<float[]> fieldColors;
	
	private List<SolutionField> fields;
	
	public int numFrames;
	
	private GeometryData gData;

	public VisualizationData(GeometryData fGeo) {
		// Use size one per default
		fieldColors = new ArrayList<float[]>(1);
		this.gData = fGeo;
	}
	
	public GeometryData getGeometryData() {
		return gData;
	}
	
	public int getNumVisualizationFields() {
		return fieldColors.size();
	}
	
	/**
	 * @param fieldNr
	 * @return The field colors for the specified field
	 */
	public float[] getFieldColors(int fieldNr) {
		return fieldColors.get(fieldNr);
	}
	
	public void setSolutionFields(List<SolutionField> fields) {
		this.fields = fields;
		numFrames = fields.get(0).getSize() / gData.getNumFieldValues();
	}
	
	public boolean isConstantField(int fieldnr) {
		return fields.get(fieldnr).isConstant();
	}
	
//	/**
//	 * assign 1 field solution
//	 * 
//	 * @param _val
//	 */
//	public void set1FieldData(float[] _val) {
//		solution = new float[][]{_val};
//		//solution[0] = _val;
//		numFrames = solution[0].length / gData.getNumFieldValues();
//	}

//	/**
//	 * assign 2 field solutions
//	 * 
//	 * @param _val1
//	 * @param _val2
//	 */
//	public void set2FieldData(float[] _val1, float[] _val2) {
//		if (gData.discrType != DiscretizationType.FEM) {
//			throw new RuntimeException(
//					"Not yet checked to work with non-FEM discretization models");
//		}
//		solution = null;
//		int fvars = gData.getNumFieldValues();
//		if ((_val1.length / fvars == 1) && (_val2.length / fvars == 1)) {
//			solution = new float[2][fvars];
//			for (int i = 0; i < fvars; i++) {
//				solution[0][i] = _val1[i];
//				solution[1][i] = _val2[i];
//			}
//			numFrames = 1;
//		} else {
//			int _vframe_num = (_val1.length / fvars);
//			solution = new float[2][fvars * _vframe_num];
//			for (int i = 0; i < fvars; i++)
//				for (int j = 0; j < _vframe_num; j++) {
//					solution[0][j * fvars + i] = _val1[j * fvars + i];
//					solution[1][j * fvars + i] = _val2[j * fvars + i];
//				}
//			numFrames = _vframe_num;
//		}
//	}
//	
//	/**
//	 * Complex data case. Assigns 3 field solutions.
//	 * 
//	 * @param _val1
//	 * @param _val2
//	 * @param _val3
//	 */
//	public void set3FieldData(float[] _val1, float[] _val2, float[] _val3) {
//		if (gData.discrType != DiscretizationType.FEM) {
//			throw new RuntimeException(
//					"Not yet checked to work with non-FEM discretization models");
//		}
//		solution = null;
//		int fvars = gData.getNumFieldValues();
//		if ((_val1.length / fvars == 1) && (_val2.length / fvars == 1)
//				&& (_val3.length / fvars == 1)) {
//			solution = new float[3][fvars];
//			for (int i = 0; i < fvars; i++) {
//				solution[0][i] = _val1[i];
//				solution[1][i] = _val2[i];
//				solution[2][i] = _val3[i];
//			}
//			numFrames = 1;
//		} else {
//			int _vframe_num = (_val1.length / fvars);
//			solution = new float[3][fvars * _vframe_num];
//			for (int i = 0; i < fvars; i++)
//				for (int j = 0; j < _vframe_num; j++) {
//					solution[0][j * fvars + i] = _val1[j * fvars + i];
//					solution[1][j * fvars + i] = _val2[j * fvars + i];
//					solution[2][j * fvars + i] = _val3[j * fvars + i];
//				}
//			numFrames = _vframe_num;
//		}
//	}

	/**
	 * calculate the color data (red green blue alpha) from the solution field
	 * TODO: enable different colormaps via model.xml (i.e. matlab's color maps)
	 * 
	 * @param cg
	 *            The color generator
	 */
	public void computeColorData(ColorGenerator cg) {
		// Clear old colors
		fieldColors.clear();
		for (int fieldNr = 0; fieldNr < fields.size(); fieldNr++) {
			SolutionField f = fields.get(fieldNr);
			if (f.isReal()) {
				fieldColors.add(getColors(cg, f.getRealValues()));
			} else {
				fieldColors.add(getColors(cg, f.getComplexValues()[0]));
				fieldColors.add(getColors(cg, f.getComplexValues()[1]));
				fieldColors.add(getColors(cg, f.getNorms()));
			}
		}
	}
	
	private float[] getColors(ColorGenerator cg, float[] fieldvalues) {
		float[] colors = cg.computeColors(fieldvalues);
		if (gData.discrType == DiscretizationType.FV) {
			Log.d("GeometryData",
					"Converting face colors data to node color data");
			colors = faceColorsToNodeColors(colors);
		}
		return colors;
	}
	
	/*
	 * Conversion method for FV discretized field variables who give solution
	 * values on faces rather than nodes.
	 * 
	 * Computes the node color as mean of all adjacent face colors.
	 */
	private float[] faceColorsToNodeColors(float[] faceCol) {
		int numTimeSteps = faceCol.length / (4 * gData.faces);
		float[] nodeCol = new float[numTimeSteps * gData.nodes * 4];

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
			int node_off = ts * 4 * gData.nodes;
			float[] valuesAdded = new float[gData.nodes];
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
			for (int n = 0; n < gData.nodes; n++) {
				nodeCol[node_off + 4 * n] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 1] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 2] /= valuesAdded[n];
				nodeCol[node_off + 4 * n + 3] /= valuesAdded[n];
			}
		}
		return nodeCol;
	}
}