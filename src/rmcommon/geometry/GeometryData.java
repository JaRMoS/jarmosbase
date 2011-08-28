package rmcommon.geometry;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import rmcommon.Log;
import rmcommon.io.AModelManager;
import rmcommon.io.MathObjectReader;

/**
 * 2011-08-24: - Started changes to this class and moved it to JRMCommons
 * project - Commented out a lot of functions whose purpose are not yet
 * investigated but also not used in ROMSim. - New class name GeometryData
 * (previously GLObject) and some renames for readability improvement
 * 
 * @author dwirtz @date 2011-08-24
 * 
 */
public class GeometryData extends Object {

	@SuppressWarnings("unused")
	private int subdomains; // number of subdomains

	private int[] domain_of_node; // tell us which subdomain our vertices belong
									// to
	private int[] domain_of_face; // tell us which subdomain our faces belong to

	/**
	 * the bounding box (xyz range) of the model
	 */
	public float[] nminmax = { 1e9f, 1e9f, 1e9f, -1e9f, -1e9f, -1e9f };

	/**
	 * number of vertices
	 */
	public int nodes;

	/**
	 * number of faces
	 */
	public int faces;

	/**
	 * The node coordinate vector of size 3*nodes, each node described by
	 * (x,y,z) coordinates
	 */
	public float[] node = null; // vertex data

	/**
	 * For 3D geometry this contains the local node normal data (three
	 * coordinates (x,y,z) per node)
	 */
	public float[] normal; // vertex normal data

	/**
	 * For 3D geometry this contains the face normal data (three coordinates
	 * (x,y,z) per face)
	 */
	public float[] fnormal; // face normal data

	public float[] reference_node; // the original vertex data

	/**
	 * Faces data. Each face ranges over three short values, giving the indices
	 * of the three corner nodes/vertices.
	 */
	public short[] face; // face data

	/**
	 * Faces edge/wireframe data. Each wireframe ranges over six short values,
	 * giving the indices of the three node connecting edges in the order
	 * n1->n2, n2->n3, n3->n1.
	 */
	public short[] face_wf; // edge data

	/**
	 * The color data for each [field][face]
	 */
	public float[][] ucolor; // color data

	public float boxsize; // bounding box size
	public int[] frame_num; // number of animation frame for solution field
	public float[][] solution; // field solution data

	public int fields = 1; // number of solution field

	boolean is2D = true; // is our model 2D?
	public boolean isconstant = false; // is the field solution constant?

	public boolean isgeoani = false;
	public int vframe_num = 1; // number of animation frame for vertices
	public float[][][] vLTfunc = null;
	public float[] vnode = null; // animation node data

	public ShortBuffer _shortBuffer;
	public FloatBuffer _floatBuffer;

	/**
	 * Allocates short and float buffers for the rendering process and sets the
	 * position to zero.
	 * 
	 * Placed here as this is done during model data loading.
	 */
	public void allocateBuffer() {
		int SHORT_MAX = 250000;
		int FLOAT_MAX = 1000000;

		Log.d("GLRenderer", "Allocate (short):" + SHORT_MAX * 2 + " bytes");
		ByteBuffer vbb = ByteBuffer.allocateDirect(SHORT_MAX * 2);
		vbb.order(ByteOrder.nativeOrder());
		_shortBuffer = vbb.asShortBuffer();
		_shortBuffer.position(0);

		Log.d("GLRenderer", "Allocate (float):" + FLOAT_MAX * 4 + " bytes");
		ByteBuffer fbb = ByteBuffer.allocateDirect(FLOAT_MAX * 4);
		fbb.order(ByteOrder.nativeOrder());
		_floatBuffer = fbb.asFloatBuffer();
		_floatBuffer.position(0);
	}

	// public GLObject() {
	// // void constructor
	// }
	//
	// public GLObject(Context _context) {
	// context = _context;
	// }

	// // get the bounding box data
	// public float[] get_minmax() {
	// return nminmax;
	// }

	// calculate the color data (red green blue alpha) from the solution field
	public void cal_color_data() {
		float calphaval = 0.8f; // default alpha value, use 1.0f for nonblend
								// rendering

		ucolor = null;
		// all field must have the same length (for now)
		ucolor = new float[fields][solution[0].length * 4];

		isconstant = true;
		for (int ifn = 0; ifn < fields; ifn++) {
			// range of the current solution field
			float val_min = solution[ifn][0];
			float val_max = solution[ifn][0];
			for (int i = 0; i < solution[ifn].length; i++) {
				val_min = (val_min > solution[ifn][i]) ? solution[ifn][i]
						: val_min;
				val_max = (val_max < solution[ifn][i]) ? solution[ifn][i]
						: val_max;
			}
			if (Math.abs(val_max - val_min) > 1e-8) {
				isconstant = false;

				// calculate color data
				for (int i = 0; i < solution[0].length; i++) {
					float tmpvar = (solution[ifn][i] - val_min)
							/ (val_max - val_min);
					if (tmpvar <= 0.125f) {
						ucolor[ifn][i * 4 + 0] = 0.0f;
						ucolor[ifn][i * 4 + 1] = 0.0f;
						ucolor[ifn][i * 4 + 2] = 0.5f + tmpvar / 0.25f;
						ucolor[ifn][i * 4 + 3] = calphaval;
					}
					if ((tmpvar > 0.125f) && (tmpvar <= 0.375f)) {
						ucolor[ifn][i * 4 + 0] = 0.0f;
						ucolor[ifn][i * 4 + 1] = 0.0f + (tmpvar - 0.125f) / 0.25f;
						ucolor[ifn][i * 4 + 2] = 1.0f;
						ucolor[ifn][i * 4 + 3] = calphaval;
					}
					if ((tmpvar > 0.375f) && (tmpvar <= 0.625f)) {
						ucolor[ifn][i * 4 + 0] = 0.0f + (tmpvar - 0.375f) / 0.25f;
						ucolor[ifn][i * 4 + 1] = 1.0f;
						ucolor[ifn][i * 4 + 2] = 1.0f - (tmpvar - 0.375f) / 0.25f;
						ucolor[ifn][i * 4 + 3] = calphaval;
					}
					if ((tmpvar > 0.625f) && (tmpvar <= 0.875f)) {
						ucolor[ifn][i * 4 + 0] = 1.0f;
						ucolor[ifn][i * 4 + 1] = 1.0f - (tmpvar - 0.625f) / 0.25f;
						ucolor[ifn][i * 4 + 2] = 0.0f;
						ucolor[ifn][i * 4 + 3] = calphaval;
					}
					if (tmpvar > 0.875f) {
						ucolor[ifn][i * 4 + 0] = 1.0f - (tmpvar - 0.875f) / 0.25f;
						ucolor[ifn][i * 4 + 1] = 0.0f;
						ucolor[ifn][i * 4 + 2] = 0.0f;
						ucolor[ifn][i * 4 + 3] = calphaval;
					}
				}
			} else {
				for (int i = 0; i < solution[0].length; i++) {
					ucolor[ifn][i * 4 + 0] = 0.0f;
					ucolor[ifn][i * 4 + 1] = 0.0f;
					ucolor[ifn][i * 4 + 2] = 1.0f;
					ucolor[ifn][i * 4 + 3] = calphaval;
				}
			}

		}
	}

	// calculate normal data for the current model
	public void cal_normal_data() {
		normal = new float[nodes * 3];
		fnormal = new float[faces * 3];
		float[] vecAB = new float[3];
		float[] vecAC = new float[3];
		int i, j, k;
		float length;
		// Initialize normal data and contribution flag
		int[] icount = new int[nodes];
		for (i = 0; i < nodes; i++) {
			normal[i * 3 + 0] = 0.0f;
			normal[i * 3 + 1] = 0.0f;
			normal[i * 3 + 2] = 0.0f;
			icount[i] = 0;
		}
		// calculate local face normal
		for (i = 0; i < faces; i++) {
			for (j = 0; j < 3; j++) {
				vecAB[j] = node[face[i * 3 + 1] * 3 + j]
						- node[face[i * 3 + 0] * 3 + j];
				vecAC[j] = node[face[i * 3 + 2] * 3 + j]
						- node[face[i * 3 + 0] * 3 + j];
			}
			// normal of the face is the cross product of AB and AC
			fnormal[i * 3 + 0] = vecAB[1] * vecAC[2] - vecAB[2] * vecAC[1];
			fnormal[i * 3 + 1] = vecAB[2] * vecAC[0] - vecAB[0] * vecAC[2];
			fnormal[i * 3 + 2] = vecAB[0] * vecAC[1] - vecAB[1] * vecAC[0];
			// normalize
			length = (float) Math
					.sqrt((fnormal[i * 3 + 0] * fnormal[i * 3 + 0]
							+ fnormal[i * 3 + 1] * fnormal[i * 3 + 1] + fnormal[i * 3 + 2]
							* fnormal[i * 3 + 2]));
			for (j = 0; j < 3; j++)
				fnormal[i * 3 + j] = fnormal[i * 3 + j] / length;
			// add in contribution to all three vertices
			for (j = 0; j < 3; j++) {
				icount[face[i * 3 + j]]++;
				for (k = 0; k < 3; k++)
					normal[face[i * 3 + j] * 3 + k] += fnormal[i * 3 + k];
			}
		}
		// average and normalize all normal vectors
		for (i = 0; i < nodes; i++) {
			for (j = 0; j < 3; j++)
				normal[i * 3 + j] = normal[i * 3 + j] / icount[i];
			length = (float) Math.sqrt((normal[i * 3 + 0] * normal[i * 3 + 0]
					+ normal[i * 3 + 1] * normal[i * 3 + 1] + normal[i * 3 + 2]
					* normal[i * 3 + 2]));
			for (j = 0; j < 3; j++)
				normal[i * 3 + j] = normal[i * 3 + j] / length;
		}
	}

	// move the model center to (0,0,0)
	public void model_centerize() {
		cal_boxsize();
		float xcen = 0.5f * (nminmax[0] + nminmax[3]);
		float ycen = 0.5f * (nminmax[1] + nminmax[4]);
		float zcen = 0.5f * (nminmax[2] + nminmax[5]);
		for (int i = 0; i < nodes; i++) {
			node[i * 3 + 0] -= xcen;
			node[i * 3 + 1] -= ycen;
			node[i * 3 + 2] -= zcen;
		}
		// recalculating minmax box
		nminmax[0] -= xcen;
		nminmax[3] += xcen;
		nminmax[1] -= ycen;
		nminmax[4] += ycen;
		nminmax[2] -= zcen;
		nminmax[5] += zcen;
	}

	// calculate bounding box data
	public void cal_boxsize() {
		nminmax[0] = 1e9f;
		nminmax[1] = 1e9f;
		nminmax[2] = 1e9f;
		nminmax[3] = -1e9f;
		nminmax[4] = -1e9f;
		nminmax[5] = -1e9f;
		for (int i = 0; i < nodes; i++) {
			for (int j = 0; j < 3; j++) {
				nminmax[0 + j] = (nminmax[0 + j] > node[i * 3 + j]) ? node[i
						* 3 + j] : nminmax[0 + j];
				nminmax[3 + j] = (nminmax[3 + j] < node[i * 3 + j]) ? node[i
						* 3 + j] : nminmax[3 + j];
			}
		}
		is2D = false;
		if (Math.abs(nminmax[5] - nminmax[2]) < 1e-8)
			is2D = true;

		boxsize = 0.0f;
		boxsize = (nminmax[3] - nminmax[0]) > boxsize ? (nminmax[3] - nminmax[0])
				: boxsize;
		boxsize = (nminmax[4] - nminmax[1]) > boxsize ? (nminmax[4] - nminmax[1])
				: boxsize;
		boxsize = (nminmax[5] - nminmax[2]) > boxsize ? (nminmax[5] - nminmax[2])
				: boxsize;
	}

	/**
	 * Reads the geometry data for the current model using the ModelManager.
	 * 
	 * @throws IOException
	 */
	public boolean loadModelGeometry(AModelManager m) {
		
		try {
			// rb model or rbappmit-type model with new geometry
			if ("rb".equals(m.getModelType()) || ("rbappmit".equals(m.getModelType()) && !m.modelFileExists("geometry.dat"))) {
				loadGeometry(m);
			} else if ("rbappmit".equals(m.getModelType())){
				loadrbappmitGeometry(m);
			} else {
				Log.e("GeometryData", "Unknown model type '"+m.getModelType()+"' for use with GeometryData");
				return false;
			}
		} catch (IOException e) {
			Log.e("GeometryData", "Loading model geometry failed: "+e.getMessage(), e);
			return false;
		}

		/*
		 * Compute the bounding box size
		 */
		cal_boxsize();

		/*
		 * Init color array
		 */
		ucolor = new float[fields][nodes * 4];

		/*
		 * Create a wireframe list (edge data)
		 */
		face_wf = new short[faces * 3 * 2];
		for (int i = 0; i < faces; i++) {
			face_wf[i * 6 + 0 * 2 + 0] = face[i * 3 + 0];
			face_wf[i * 6 + 0 * 2 + 1] = face[i * 3 + 1];
			face_wf[i * 6 + 1 * 2 + 0] = face[i * 3 + 1];
			face_wf[i * 6 + 1 * 2 + 1] = face[i * 3 + 2];
			face_wf[i * 6 + 2 * 2 + 0] = face[i * 3 + 2];
			face_wf[i * 6 + 2 * 2 + 1] = face[i * 3 + 0];
		}

		/*
		 * Compute normals for 3D data
		 * 
		 * TODO: should be pre-computed if 3D models are used! -> distinct data
		 * to read for 2D and 3D model data, perhaps even use different
		 * GeometryData classes?
		 */
		if (!is2D())
			cal_normal_data();

		return true;
	}

	/**
	 * Old geometry loading method, using the geometry.dat file which is to
	 * parse! (slow)
	 * 
	 * @param tokens
	 * @throws IOException
	 */
	private void loadrbappmitGeometry(AModelManager m) throws IOException {
		String[] tokens = null;
		BufferedReader reader = m.getBufReader("geometry.dat");
		try {
			tokens = reader.readLine().split(" ");
		} finally {
			reader.close();
		}
		
		/**
		 * Read nodes and their locations
		 */
		nodes = Integer.parseInt(tokens[0]);
		int count = 1;
		reference_node = new float[nodes * 3];
		node = new float[nodes * 3];
		solution = new float[fields][nodes];
		for (int i = 0; i < nodes; i++) {
			reference_node[i * 3 + 0] = Float.parseFloat(tokens[count]);
			reference_node[i * 3 + 1] = Float.parseFloat(tokens[count + 1]);
			reference_node[i * 3 + 2] = Float.parseFloat(tokens[count + 2]);
			count += 3;
			node[i * 3 + 0] = reference_node[i * 3 + 0];
			node[i * 3 + 1] = reference_node[i * 3 + 1];
			node[i * 3 + 2] = reference_node[i * 3 + 2];
		}

		/**
		 * Read faces and their connections
		 */
		subdomains = Integer.parseInt(tokens[count]);
		faces = Integer.parseInt(tokens[count + 1]);
		count += 2;
		face = new short[faces * 3];
		for (int i = 0; i < faces; i++) {
			face[i * 3 + 0] = Short.parseShort(tokens[count]);
			face[i * 3 + 1] = Short.parseShort(tokens[count + 1]);
			face[i * 3 + 2] = Short.parseShort(tokens[count + 2]);
			count += 3;
		}
		domain_of_node = new int[nodes];
		for (int i = 0; i < nodes; i++) {
			domain_of_node[i] = Integer.parseInt(tokens[count]);
			count++;
		}
		domain_of_face = new int[faces];
		for (int i = 0; i < faces; i++) {
			domain_of_face[i] = Integer.parseInt(tokens[count]);
			count++;
		}
	}

	/**
	 * Loads the model geometry from the geometry files.
	 * So far those comprise a nodes.bin file for the geometry vertices and a
	 * faces.bin containing the faces and their three edge vertex numbers.
	 */
	private void loadGeometry(AModelManager m) throws IOException {
		
		MathObjectReader mr = new MathObjectReader();
		
		node = mr.readRawFloatVector(m.getInStream("vertices.bin"));
		/*
		 * Assume the vector to contain 3D data.
		 * If we have 2D data, we insert zeros at every 3rd position!
		 */
		if ("2".equals(m.getModelXMLTagValue("dimension"))) {
			float[] tmpnode = new float[node.length + node.length/2];
			for (int i=0; i<node.length/2; i++) {
				tmpnode[3*i] = node[2*i];
				tmpnode[3*i+1] = node[2*i+1];
				tmpnode[3*i+2] = 0;
			}
			node = tmpnode;
			tmpnode = null;
		}
		// Three coordinates per node
		nodes = node.length/3;
		reference_node = node.clone();
		
		face = mr.readRawShortVector(m.getInStream("faces.bin"));
		// Three edges per face
		faces = face.length/3;
		
		solution = new float[fields][nodes];
		domain_of_node = new int[nodes];
		domain_of_face = new int[faces];
	}

	/**
	 * Affine-linear transformation of a node to a new position. Uses the
	 * reference nodes and the LTfunc (linear transform function) to move the
	 * nodes to the specified location.
	 * 
	 * The crack in a pillar demo illustrates the use of this.
	 * 
	 * LTfunc is of size [number of subdomain, 12] a row of LTfunc is the
	 * rowwise flatten of the [3,3] transformation matrix and the [3,1]
	 * translation vector
	 * 
	 * @param LTfunc
	 */
	public void afflin_geometry_transform(float[][] LTfunc) {
		float[] old_node = new float[3];
		for (int i = 0; i < nodes; i++) {
			old_node[0] = reference_node[i * 3 + 0];
			old_node[1] = reference_node[i * 3 + 1];
			old_node[2] = reference_node[i * 3 + 2];
			node[i * 3 + 0] = LTfunc[domain_of_node[i]][0] * old_node[0]
					+ LTfunc[domain_of_node[i]][1] * old_node[1]
					+ LTfunc[domain_of_node[i]][2] * old_node[2]
					// + offset
					+ LTfunc[domain_of_node[i]][9];
			node[i * 3 + 1] = LTfunc[domain_of_node[i]][3] * old_node[0]
					+ LTfunc[domain_of_node[i]][4] * old_node[1]
					+ LTfunc[domain_of_node[i]][5] * old_node[2]
					// + offset
					+ LTfunc[domain_of_node[i]][10];
			node[i * 3 + 2] = LTfunc[domain_of_node[i]][6] * old_node[0]
					+ LTfunc[domain_of_node[i]][7] * old_node[1]
					+ LTfunc[domain_of_node[i]][8] * old_node[2]
					// + offset
					+ LTfunc[domain_of_node[i]][11];
		}
		cal_boxsize();
		model_centerize();
	}

	// assign 1 field solution
	public void set_field_data(float[] _val) {
		solution = null;
		fields = 1;
		solution = new float[1][_val.length];
		solution[0] = _val;
		frame_num = new int[1];
		frame_num[0] = solution[0].length / nodes;
		cal_color_data();
	}

	public void set_field_data(float[] _val1, float[] _val2) {
		set_field_data(_val1, _val2, true);
	}

	// assign 2 field solutions
	public void set_field_data(float[] _val1, float[] _val2, boolean isDeformed) {
		if (isDeformed) {
			solution = null;
			// the solution field is the displacement field
			// merge displacement field into current vertex data
			fields = 1;
			float val_min = _val1[0];
			float val_max = _val1[0];
			for (int i = 0; i < _val1.length; i++) {
				val_min = (val_min > _val1[i]) ? _val1[i] : val_min;
				val_max = (val_max < _val1[i]) ? _val1[i] : val_max;
			}
			for (int i = 0; i < _val2.length; i++) {
				val_min = (val_min > _val2[i]) ? _val2[i] : val_min;
				val_max = (val_max < _val2[i]) ? _val2[i] : val_max;
			}
			float sval = (val_max - val_min) / boxsize * 5;
			if ((_val1.length / nodes == 1) && (_val2.length / nodes == 1)) {
				solution = new float[1][nodes];
				for (int i = 0; i < nodes; i++) {
					node[i * 3 + 0] = node[i * 3 + 0] + _val1[i] / sval;
					node[i * 3 + 1] = node[i * 3 + 1] + _val2[i] / sval;
					node[i * 3 + 2] = node[i * 3 + 2];
					solution[0][i] = 0.0f;
				}
				frame_num = new int[1];
				frame_num[0] = 1;
			} else {
				int _vframe_num = (_val1.length / nodes);
				solution = new float[1][nodes * _vframe_num];
				for (int i = 0; i < nodes; i++)
					for (int j = 0; j < _vframe_num; j++) {
						vnode[j * nodes * 3 + i * 3 + 0] = vnode[j * nodes * 3
								+ i * 3 + 0]
								+ _val1[j * nodes + i] / sval;
						vnode[j * nodes * 3 + i * 3 + 1] = vnode[j * nodes * 3
								+ i * 3 + 1]
								+ _val2[j * nodes + i] / sval;
						vnode[j * nodes * 3 + i * 3 + 2] = vnode[j * nodes * 3
								+ i * 3 + 2];
						solution[0][j * nodes + i] = 0.0f;
					}
				frame_num = new int[1];
				frame_num[0] = _vframe_num;
			}

		} else {
			solution = null;
			fields = 2;
			if ((_val1.length / nodes == 1) && (_val2.length / nodes == 1)) {
				solution = new float[2][nodes];
				for (int i = 0; i < nodes; i++) {
					solution[0][i] = _val1[i];
					solution[1][i] = _val2[i];
				}
				frame_num = new int[2];
				frame_num[0] = 1;
				frame_num[1] = 1;
			} else {
				int _vframe_num = (_val1.length / nodes);
				solution = new float[2][nodes * _vframe_num];
				for (int i = 0; i < nodes; i++)
					for (int j = 0; j < _vframe_num; j++) {
						solution[0][j * nodes + i] = _val1[j * nodes + i];
						solution[1][j * nodes + i] = _val2[j * nodes + i];
					}
				frame_num = new int[2];
				frame_num[0] = _vframe_num;
				frame_num[1] = _vframe_num;
			}

		}

		cal_color_data();
	}

	public void set_field_data(float[] _val1, float[] _val2, float[] _val3) {
		set_field_data(_val1, _val2, _val3, true);
	}

	/**
	 * Complex data case. Assigns 3 field solutions.
	 * 
	 * @param _val1
	 * @param _val2
	 * @param _val3
	 * @param isDeformed
	 */
	public void set_field_data(float[] _val1, float[] _val2, float[] _val3,
			boolean isDeformed) {
		if (isDeformed) {
			solution = null;
			// the solution field is the displacement field
			// merge displacement field into current vertex data
			fields = 1;
			float val_min = _val1[0];
			float val_max = _val1[0];
			for (int i = 0; i < _val1.length; i++) {
				val_min = (val_min > _val1[i]) ? _val1[i] : val_min;
				val_max = (val_max < _val1[i]) ? _val1[i] : val_max;
			}
			for (int i = 0; i < _val2.length; i++) {
				val_min = (val_min > _val2[i]) ? _val2[i] : val_min;
				val_max = (val_max < _val2[i]) ? _val2[i] : val_max;
			}
			for (int i = 0; i < _val3.length; i++) {
				val_min = (val_min > _val3[i]) ? _val3[i] : val_min;
				val_max = (val_max < _val3[i]) ? _val3[i] : val_max;
			}
			float sval = (val_max - val_min) / boxsize * 5;
			if ((_val1.length / nodes == 1) && (_val2.length / nodes == 1)
					&& (_val3.length / nodes == 1)) {
				solution = new float[1][nodes];
				for (int i = 0; i < nodes; i++) {
					node[i * 3 + 0] = node[i * 3 + 0] + _val1[i] / sval;
					node[i * 3 + 1] = node[i * 3 + 1] + _val2[i] / sval;
					node[i * 3 + 2] = node[i * 3 + 2] + _val3[i] / sval;
					;
					solution[0][i] = 0.0f;
					frame_num = new int[1];
					frame_num[0] = 1;
				}
			} else {
				int _vframe_num = (_val1.length / nodes);
				solution = new float[1][nodes * _vframe_num];
				for (int i = 0; i < nodes; i++)
					for (int j = 0; j < _vframe_num; j++) {
						vnode[j * nodes * 3 + i * 3 + 0] = vnode[j * nodes * 3
								+ i * 3 + 0]
								+ _val1[j * nodes + i] / sval;
						vnode[j * nodes * 3 + i * 3 + 1] = vnode[j * nodes * 3
								+ i * 3 + 1]
								+ _val2[j * nodes + i] / sval;
						vnode[j * nodes * 3 + i * 3 + 2] = vnode[j * nodes * 3
								+ i * 3 + 2]
								+ _val3[j * nodes + i] / sval;
						solution[0][j * nodes + i] = 0.0f;
					}
				frame_num = new int[1];
				frame_num[0] = _vframe_num;
			}
		} else {
			solution = null;
			fields = 3;
			if ((_val1.length / nodes == 1) && (_val2.length / nodes == 1)
					&& (_val3.length / nodes == 1)) {
				solution = new float[3][nodes];
				for (int i = 0; i < nodes; i++) {
					solution[0][i] = _val1[i];
					solution[1][i] = _val2[i];
					solution[2][i] = _val3[i];
				}
				frame_num = new int[3];
				frame_num[0] = 1;
				frame_num[1] = 1;
				frame_num[2] = 1;
			} else {
				int _vframe_num = (_val1.length / nodes);
				solution = new float[3][nodes * _vframe_num];
				for (int i = 0; i < nodes; i++)
					for (int j = 0; j < _vframe_num; j++) {
						solution[0][j * nodes + i] = _val1[j * nodes + i];
						solution[1][j * nodes + i] = _val2[j * nodes + i];
						solution[2][j * nodes + i] = _val3[j * nodes + i];
					}
				frame_num = new int[3];
				frame_num[0] = _vframe_num;
				frame_num[1] = _vframe_num;
				frame_num[2] = _vframe_num;
			}
		}
		cal_color_data();
	}

	// assign 4 field solutions (4th field is sol col)
	public void set_field_data(float[] _val1, float[] _val2, float[] _val3,
			float[] _val4) {
		solution = null;

		// the solution field is the displacement field
		// merge displacement field into current vertex data
		fields = 1;
		float val_min = _val1[0];
		float val_max = _val1[0];
		for (int i = 0; i < _val1.length; i++) {
			val_min = (val_min > _val1[i]) ? _val1[i] : val_min;
			val_max = (val_max < _val1[i]) ? _val1[i] : val_max;
		}
		for (int i = 0; i < _val2.length; i++) {
			val_min = (val_min > _val2[i]) ? _val2[i] : val_min;
			val_max = (val_max < _val2[i]) ? _val2[i] : val_max;
		}
		for (int i = 0; i < _val3.length; i++) {
			val_min = (val_min > _val3[i]) ? _val3[i] : val_min;
			val_max = (val_max < _val3[i]) ? _val3[i] : val_max;
		}
		float sval = (val_max - val_min) / boxsize * 5.0f;
		if ((_val1.length / nodes == 1) && (_val2.length / nodes == 1)
				&& (_val3.length / nodes == 1)) {
			solution = new float[1][nodes];
			for (int i = 0; i < nodes; i++) {
				node[i * 3 + 0] = node[i * 3 + 0] + _val1[i] / sval;
				node[i * 3 + 1] = node[i * 3 + 1] + _val2[i] / sval;
				node[i * 3 + 2] = node[i * 3 + 2] + _val3[i] / sval;
				solution[0][i] = _val4[i];
				frame_num = new int[1];
				frame_num[0] = 1;
			}
		} else {
			int _vframe_num = (_val1.length / nodes);
			solution = new float[1][nodes * _vframe_num];
			for (int i = 0; i < nodes; i++)
				for (int j = 0; j < _vframe_num; j++) {
					vnode[j * nodes * 3 + i * 3 + 0] = vnode[j * nodes * 3 + i
							* 3 + 0]
							+ _val1[j * nodes + i] / sval;
					vnode[j * nodes * 3 + i * 3 + 1] = vnode[j * nodes * 3 + i
							* 3 + 1]
							+ _val2[j * nodes + i] / sval;
					vnode[j * nodes * 3 + i * 3 + 2] = vnode[j * nodes * 3 + i
							* 3 + 2]
							+ _val3[j * nodes + i] / sval;
					solution[0][j * nodes + i] = _val4[j * nodes + i];
				}
			frame_num = new int[1];
			frame_num[0] = _vframe_num;
		}
		cal_color_data();
	}

	// // assign vertex data
	// public void set_node_data(float[] _node) {
	// node_num = _node.length / 3;
	// node = null;
	// node = _node;
	// cal_boxsize();
	// model_centerize();
	// }

	// // assign node region data
	// public void set_node_reg_data(int[] _node_reg) {
	// node_reg = null;
	// node_reg = _node_reg;
	// }

	// // assign ref_node data
	// public void set_ref_node_data(float[] _node) {
	// node_num = _node.length / 3;
	// ref_node = null;
	// ref_node = _node;
	// // copy ref_node to node
	// if (node == null) {
	// node = new float[node_num * 3];
	// for (int i = 0; i < node_num; i++)
	// for (int j = 0; j < 3; j++)
	// node[i * 3 + j] = ref_node[i * 3 + j];
	// }
	// cal_boxsize();
	// model_centerize();
	// // node = null;
	// }

	// /**
	// * Assigns face data
	// *
	// * Appears not to be used.
	// *
	// * @param ifn
	// * @return
	// */
	// public void set_face_data(short[] _face) {
	// face_num = _face.length / 3;
	// face = null;
	// face = _face;
	//
	// // Create a wireframe list
	// face_wf = null;
	// face_wf = new short[face_num * 3 * 2];
	// for (int i = 0; i < face_num; i++) {
	// face_wf[i * 6 + 0 * 2 + 0] = face[i * 3 + 0];
	// face_wf[i * 6 + 0 * 2 + 1] = face[i * 3 + 1];
	// face_wf[i * 6 + 1 * 2 + 0] = face[i * 3 + 1];
	// face_wf[i * 6 + 1 * 2 + 1] = face[i * 3 + 2];
	// face_wf[i * 6 + 2 * 2 + 0] = face[i * 3 + 2];
	// face_wf[i * 6 + 2 * 2 + 1] = face[i * 3 + 0];
	// }
	//
	// // Calculate normal data for 3D object
	// if (!is2D())
	// cal_normal_data();
	// }

	// // how many field we currently have?
	// public float[] get_field_data(int ifn) {
	// return sol[ifn];
	// }

	// // the vertex data
	// public float[] get_node_data() {
	// return node;
	// }

	// // the reference vertex data
	// public float[] get_ref_node_data() {
	// return ref_node;
	// }

	// // the face data
	// public short[] get_face_data() {
	// return face;
	// }

	// // the number of subdomain
	// public int get_reg_num() {
	// return reg_num;
	// }

	// is our model flat (2D)?
	public boolean is2D() {
		return is2D;
	}

	// // get node_reg data
	// public int[] get_node_reg() {
	// return node_reg;
	// }

	// // get LTfunc data
	// // this also get us vertex animation data
	// public void set_LTfunc(float[][][] _LTfunc, int _reg_num, int
	// _vframe_num) {
	// reg_num = _reg_num;
	// vframe_num = _vframe_num;
	// if (vframe_num == 0)
	// isgeoani = false;
	// else
	// isgeoani = true;
	// vLTfunc = _LTfunc;
	// vnode = new float[vframe_num * node_num * 3];
	// for (int i = 0; i < vframe_num; i++) {
	// // get current nodal data
	// nodal_transform(vLTfunc[i]);
	// // copy current nodal data into animation list
	// for (int j = 0; j < node_num; j++)
	// for (int k = 0; k < 3; k++)
	// vnode[i * node_num * 3 + j * 3 + k] = node[j * 3 + k];
	// }
	// }

	// get LTfunc data
	// this also get us vertex animation data
	public void set_LTfunc(float[][][] _LTfunc) {
		vframe_num = _LTfunc.length;
		if (vframe_num == 1)
			isgeoani = false;
		else
			isgeoani = true;
		vLTfunc = _LTfunc;
		vnode = new float[vframe_num * nodes * 3];
		for (int i = 0; i < vframe_num; i++) {

			// Transform the nodes to the locations specified by _LTFunc
			afflin_geometry_transform(vLTfunc[i]);

			// copy current nodal data into animation list
			for (int j = 0; j < nodes; j++)
				for (int k = 0; k < 3; k++)
					vnode[i * nodes * 3 + j * 3 + k] = node[j * 3 + k];
		}
	}

}