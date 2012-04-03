package jarmos.geometry;

import jarmos.Log;
import jarmos.ModelType;
import jarmos.io.AModelManager;
import jarmos.io.MathObjectReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * 2011-08-24: - Started changes to this class and moved it to JRMCommons
 * project - Commented out a lot of functions whose purpose are not yet
 * investigated but also not used in ROMSim. - New class name GeometryData
 * (previously GLObject) and some renames for readability improvement
 * 
 * @author dwirtz @date 2011-08-24
 * 
 *         TODO check if all tags in model.xml are needed
 * 
 *         TODO create subdomains property/management/classes
 */
public class GeometryData extends Object {

	@SuppressWarnings("unused")
	private int subdomains; // number of subdomains

	/**
	 * The model discretization type. Influences the way the color values are
	 * computed (either given on vertex or face)
	 */
//	public FieldMapping fieldMap = FieldMapping.VERTEX;

	public int[] vertexLTFuncNr; // tell us which subdomain our vertices
									// belong
									// to
	private int[] domain_of_face; // tell us which subdomain our faces belong to

	/**
	 * the bounding box (xyz range) of the model
	 */
	public float[] nminmax = { 1e9f, 1e9f, 1e9f, -1e9f, -1e9f, -1e9f };

	/**
	 * number of vertices
	 */
	private int numOrigVertices;

	/**
	 * The number of (original) vertices of the geometry
	 * 
	 * @return the numVertices
	 */
	public int getNumVertices() {
		return numOrigVertices;
	}

	/**
	 * number of faces
	 */
	public int numFaces;

	/**
	 * The node coordinate vector of size 3*nodes, each node described by
	 * (x,y,z) coordinates
	 */
	private float[][] vertices = null; // vertex data

	/**
	 * For 3D geometry this contains the local node normal data (three
	 * coordinates (x,y,z) per node)
	 */
	public float[][] normal; // vertex normal data

	/**
	 * For 3D geometry this contains the face normal data (three coordinates
	 * (x,y,z) per face)
	 */
	public float[][] fnormal; // face normal data

	/**
	 * The reference nodes. A copy of the original nodes array which might be
	 * modified for models with changing geometry.
	 */
	public float[] originalVertices; // the original vertex data

	/**
	 * Faces data. Each face ranges over three short values, giving the indices
	 * of the three corner nodes/vertices.
	 */
	public short[] faces; // face data

	/**
	 * Edges data. Each face ranges over two short values, giving the indices of
	 * the two corner nodes/vertices.
	 */
	public short[] edges; // edge data

	/**
	 * Contains the number of nodes whose values are given by dirichlet values.
	 */
	public short[] dir_nodes = null;

	/**
	 * The dirichlet values for the dir_nodes. The first index is the number of
	 * the field the value is dirichlet for, and the second index denotes x,y
	 * and z offsets from original positions defined in node.
	 */
	public float[][] dir_values = null;

	/**
	 * Faces edge/wireframe data. Each wireframe ranges over six short values,
	 * giving the indices of the three node connecting edges in the order
	 * n1->n2, n2->n3, n3->n1.
	 */
	public short[] faceWireframe; // edge data

	public float boxsize; // bounding box size

	private boolean is2D = true; // is our model 2D?

	// public GeometryData() {
	// vertexSets = new ArrayList<float[]>();
	// }

	public float[][] getVertices() {
		return vertices;
	}

	/**
	 * calculate normal data for the current model
	 */
	private void compute3DNormalData() {
		normal = new float[vertices.length][];
		fnormal = new float[vertices.length][];
		for (int vset = 0; vset < vertices.length; vset++) {
			float[] normal_ = new float[numOrigVertices * 3];
			float[] fnormal_ = new float[numFaces * 3];
			float[] vecAB = new float[3];
			float[] vecAC = new float[3];
			int i, j, k;
			float length;
			// Initialize normal data and contribution flag
			int[] icount = new int[numOrigVertices];
			for (i = 0; i < numOrigVertices; i++) {
				normal_[i * 3 + 0] = 0.0f;
				normal_[i * 3 + 1] = 0.0f;
				normal_[i * 3 + 2] = 0.0f;
				icount[i] = 0;
			}
			// calculate local face normal
			for (i = 0; i < numFaces; i++) {
				for (j = 0; j < 3; j++) {
					vecAB[j] = vertices[vset][faces[i * 3 + 1] * 3 + j] - vertices[vset][faces[i * 3 + 0] * 3 + j];
					vecAC[j] = vertices[vset][faces[i * 3 + 2] * 3 + j] - vertices[vset][faces[i * 3 + 0] * 3 + j];
				}
				// normal of the face is the cross product of AB and AC
				fnormal_[i * 3 + 0] = vecAB[1] * vecAC[2] - vecAB[2] * vecAC[1];
				fnormal_[i * 3 + 1] = vecAB[2] * vecAC[0] - vecAB[0] * vecAC[2];
				fnormal_[i * 3 + 2] = vecAB[0] * vecAC[1] - vecAB[1] * vecAC[0];
				// normalize
				length = (float) Math.sqrt((fnormal_[i * 3 + 0] * fnormal_[i * 3 + 0] + fnormal_[i * 3 + 1]
						* fnormal_[i * 3 + 1] + fnormal_[i * 3 + 2] * fnormal_[i * 3 + 2]));
				for (j = 0; j < 3; j++)
					fnormal_[i * 3 + j] = fnormal_[i * 3 + j] / length;
				// add in contribution to all three vertices
				for (j = 0; j < 3; j++) {
					icount[faces[i * 3 + j]]++;
					for (k = 0; k < 3; k++)
						normal_[faces[i * 3 + j] * 3 + k] += fnormal_[i * 3 + k];
				}
			}
			// average and normal_ize all normal_ vectors
			for (i = 0; i < numOrigVertices; i++) {
				for (j = 0; j < 3; j++)
					normal_[i * 3 + j] = normal_[i * 3 + j] / icount[i];
				length = (float) Math.sqrt((normal_[i * 3 + 0] * normal_[i * 3 + 0] + normal_[i * 3 + 1]
						* normal_[i * 3 + 1] + normal_[i * 3 + 2] * normal_[i * 3 + 2]));
				for (j = 0; j < 3; j++)
					normal_[i * 3 + j] = normal_[i * 3 + j] / length;
			}
			normal[vset] = normal_;
			fnormal[vset] = fnormal_;
		}
	}

	/**
	 * move the model center to (0,0,0)
	 */
	private void centerModelGeometry() {
		computeBoundingBox();
		float xcen = 0.5f * (nminmax[0] + nminmax[3]);
		float ycen = 0.5f * (nminmax[1] + nminmax[4]);
		float zcen = 0.5f * (nminmax[2] + nminmax[5]);
		for (int vset = 0; vset < vertices.length; vset++) {
			for (int i = 0; i < vertices[vset].length / 3; i++) {
				vertices[vset][i * 3 + 0] -= xcen;
				vertices[vset][i * 3 + 1] -= ycen;
				vertices[vset][i * 3 + 2] -= zcen;
			}
		}
		// recalculating minmax box
		nminmax[0] -= xcen;
		nminmax[3] += xcen;
		nminmax[1] -= ycen;
		nminmax[4] += ycen;
		nminmax[2] -= zcen;
		nminmax[5] += zcen;
	}

	/**
	 * calculate bounding box data
	 */
	private void computeBoundingBox() {
		nminmax[0] = 1e9f;
		nminmax[1] = 1e9f;
		nminmax[2] = 1e9f;
		nminmax[3] = -1e9f;
		nminmax[4] = -1e9f;
		nminmax[5] = -1e9f;
		for (int vset = 0; vset < vertices.length; vset++) {
			for (int i = 0; i < vertices[vset].length / 3; i++) {
				for (int j = 0; j < 3; j++) {
					nminmax[0 + j] = (nminmax[0 + j] > vertices[vset][i * 3 + j]) ? vertices[vset][i * 3 + j]
							: nminmax[0 + j];
					nminmax[3 + j] = (nminmax[3 + j] < vertices[vset][i * 3 + j]) ? vertices[vset][i * 3 + j]
							: nminmax[3 + j];
				}
			}
		}

		boxsize = 0.0f;
		boxsize = (nminmax[3] - nminmax[0]) > boxsize ? (nminmax[3] - nminmax[0]) : boxsize;
		boxsize = (nminmax[4] - nminmax[1]) > boxsize ? (nminmax[4] - nminmax[1]) : boxsize;
		boxsize = (nminmax[5] - nminmax[2]) > boxsize ? (nminmax[5] - nminmax[2]) : boxsize;
	}

	/**
	 * Reads the geometry data for the current model using the ModelManager.
	 * 
	 * @param m
	 * @return True if loading was successful, false otherwise
	 */
	public boolean loadModelGeometry(AModelManager m) {

		try {
			// rb model or rbappmit-type model with new geometry
			if (m.getModelType() == ModelType.JRB || m.getModelType() == ModelType.JKerMor) {
				loadGeometry(m);
			} else if (m.getModelType() == ModelType.rbappmit) {
				loadrbappmitGeometry(m);
			} else {
				Log.e("GeometryData", "Unknown model type '" + m.getModelType() + "' for use with GeometryData");
				return false;
			}
		} catch (IOException e) {
			Log.e("GeometryData", "Loading model geometry failed: " + e.getMessage(), e);
			return false;
		}

		/*
		 * Create a wireframe list (edge data)
		 */
		faceWireframe = new short[numFaces * 3 * 2];
		for (int i = 0; i < numFaces; i++) {
			faceWireframe[i * 6 + 0 * 2 + 0] = faces[i * 3 + 0];
			faceWireframe[i * 6 + 0 * 2 + 1] = faces[i * 3 + 1];
			faceWireframe[i * 6 + 1 * 2 + 0] = faces[i * 3 + 1];
			faceWireframe[i * 6 + 1 * 2 + 1] = faces[i * 3 + 2];
			faceWireframe[i * 6 + 2 * 2 + 0] = faces[i * 3 + 2];
			faceWireframe[i * 6 + 2 * 2 + 1] = faces[i * 3 + 0];
		}
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
		numOrigVertices = Integer.parseInt(tokens[0]);
		int count = 1;
		originalVertices = new float[numOrigVertices * 3];
		for (int i = 0; i < numOrigVertices; i++) {
			originalVertices[i * 3 + 0] = Float.parseFloat(tokens[count]);
			originalVertices[i * 3 + 1] = Float.parseFloat(tokens[count + 1]);
			originalVertices[i * 3 + 2] = Float.parseFloat(tokens[count + 2]);
			count += 3;
		}
		// Manually check if geometry is 2D
		is2D = true;
		for (int i = 0; i < numOrigVertices; i++) {
			is2D &= originalVertices[i * 3 + 2] == 0;
		}

		/**
		 * Read faces and their connections
		 */
		subdomains = Integer.parseInt(tokens[count]);
		numFaces = Integer.parseInt(tokens[count + 1]);
		count += 2;
		faces = new short[numFaces * 3];
		for (int i = 0; i < numFaces; i++) {
			faces[i * 3 + 0] = Short.parseShort(tokens[count]);
			faces[i * 3 + 1] = Short.parseShort(tokens[count + 1]);
			faces[i * 3 + 2] = Short.parseShort(tokens[count + 2]);
			count += 3;
		}
		/**
		 * Read in the number of the transformation function for each vertex
		 * (effectively modeling different functions for different geometry
		 * domains)
		 */
		vertexLTFuncNr = new int[numOrigVertices];
		for (int i = 0; i < numOrigVertices; i++) {
			vertexLTFuncNr[i] = Integer.parseInt(tokens[count]);
			count++;
		}
		domain_of_face = new int[numFaces];
		for (int i = 0; i < numFaces; i++) {
			domain_of_face[i] = Integer.parseInt(tokens[count]);
			count++;
		}
		// No edges for rbappmit-models (new feature)
		edges = null;
	}

	/**
	 * Loads the model geometry from the geometry files. So far those comprise a
	 * nodes.bin file for the geometry vertices and a faces.bin containing the
	 * faces and their three edge vertex numbers.
	 */
	private void loadGeometry(AModelManager m) throws IOException {

		MathObjectReader mr = new MathObjectReader();
		originalVertices = mr.readRawFloatVector(m.getInStream("vertices.bin"));
		Log.d("GeoData", "Loaded " + originalVertices.length + " vertex values");

		/*
		 * Assume the vector to contain 3D data. If we have 2D data, we insert
		 * zeros at every 3rd position!
		 */
		is2D = false;
		if ("2".equals(m.getModelXMLTagValue("geometry.dimension"))) {
			is2D = true;
			float[] tmpnode = new float[originalVertices.length + originalVertices.length / 2];
			for (int i = 0; i < originalVertices.length / 2; i++) {
				tmpnode[3 * i] = originalVertices[2 * i];
				tmpnode[3 * i + 1] = originalVertices[2 * i + 1];
				tmpnode[3 * i + 2] = 0;
			}
			originalVertices = tmpnode;
			tmpnode = null;
			Log.d("GeoData", "2D geometry - extending vertex data to 3rd dimension (zeros) to total number of "
					+ originalVertices.length + " values");
		}
		// Three coordinates per node
		numOrigVertices = originalVertices.length / 3;
		Log.d("GeoData", "Loaded " + numOrigVertices + " vertices");
		// Only one transformation function for JRB models if any
		vertexLTFuncNr = new int[numOrigVertices];

		faces = null;
		if (m.modelFileExists("faces.bin") // check included for backwards
											// compatibility.
				|| m.xmlTagExists("geometry.hasFaces")
				&& Boolean.parseBoolean(m.getModelXMLTagValue("geometry.hasFaces"))) {
			faces = mr.readRawShortVector(m.getInStream("faces.bin"));
			// Subtract the indices, as the nodes are addressed with zero offset
			// inside java arrays
			for (int i = 0; i < faces.length; i++) {
				faces[i] -= 1;
			}
			// Three edges per face
			numFaces = faces.length / 3;
			Log.d("GeoData", "Loaded " + numFaces + " faces");
			domain_of_face = new int[numFaces];
		}

		edges = null;
		if (m.modelFileExists("edges.bin")) {
			edges = mr.readRawShortVector(m.getInStream("edges.bin"));
		}

		if (m.modelFileExists("dir_nodes.bin")) {
			edges = mr.readRawShortVector(m.getInStream("edges.bin"));
		}

		// Read discretization type
		// fieldMap =
		// FieldMapping.parse(m.getModelXMLTagValue("geometry.fieldmapping"));
	}

	/**
	 * Sets the displacement data for this geometry according to the
	 * DisplacementField provided.
	 * 
	 * @param d
	 *            The displacement field
	 * 
	 *            TODO scaling automatically
	 * @return The number of vertex sets with different displacements available
	 */
	public void addDisplacements(DisplacementField d, int parts) {
		// if (fieldMap != FieldMapping.VERTEX) {
		// throw new
		// RuntimeException("Displacements not possible for non-vertex based field mapping");
		// }

		float scaling = 1f;// (d.getMax() - d.getMin()) / boxsize;
		Log.d("GeoData", "Adding displacements from " + d.descriptor + ", numVertices=" + numOrigVertices
				+ ", vertices field length=" + vertices.length + ", displacement field size=" + d.getSize() + " (x3="
				+ d.getSize() * 3 + "), parts=" + parts);
		for (int vset = 0; vset < parts; vset++) {
			for (int nodenr = 0; nodenr < numOrigVertices; nodenr++) {
				int idx = vset * numOrigVertices + nodenr;
				vertices[vset][3 * nodenr] += d.getXDisplacements()[idx] / scaling;
				vertices[vset][3 * nodenr + 1] += d.getYDisplacements()[idx] / scaling;
				vertices[vset][3 * nodenr + 2] += d.getZDisplacements()[idx] / scaling;
			}
		}
		compute3DNormalData();
		centerModelGeometry();
	}

	/**
	 * @return If the data is 2D data
	 */
	public boolean is2D() {
		return is2D;
	}

	/**
	 * Transforms the original vertices by all given MeshTransforms in the list
	 * and concatenates the results in a large vertices array.
	 * 
	 * @param transforms
	 */
	public void createMesh(List<MeshTransform> transforms, boolean update) {
		vertices = new float[transforms.size()][];
		int cnt = 0;
		// Log.d("GeoData", "Original   : " + Log.subArr(originalVertices,100));
		for (MeshTransform m : transforms) {
			vertices[cnt++] = m.transformMesh(originalVertices);
			// Log.d("GeoData", "Transform "+cnt+": " +
			// Log.subArr(vertices[cnt-1],100)+" ("+m.getClass().getName()+")");
			// float[] diff = new float[200];
			// for (int i=0; i < 200;i++) {
			// diff[i] = originalVertices[i] - vertices[cnt-1][i];
			// }
			// Log.d("GeoData", "Diff "+cnt+": " + Log.subArr(diff,200));
		}
		if (update) {
			if (!is2D) {
				compute3DNormalData();
			}
			centerModelGeometry();
		}
	}

	// /**
	// * Applies Affine Linear Transformation To Vertices
	// *
	// * @param afflinfuncs
	// */
	// public void applyAffLinVertexTransformation(float[][][] afflinfuncs) {
	// Log.d("GeoData", "Applying affine linear transformation to nodes (" +
	// afflinfuncs.length + " sets/frames)");
	// vertices = new float[afflinfuncs.length * numVertices * 3];
	// float baseX, baseY, baseZ;
	// for (int funcSetNr = 0; funcSetNr < afflinfuncs.length; funcSetNr++) {
	// float[][] funcSet = afflinfuncs[funcSetNr];
	// /**
	// * Affine-linear transformation of a node to a new position. Uses
	// * the reference nodes and the LTfunc (linear transform function) to
	// * move the nodes to the specified location.
	// *
	// * The crack in a pillar demo illustrates the use of this.
	// *
	// * func is of size [number of subdomain, 12] a row of LTfunc is the
	// * rowwise flatten of the [3,3] transformation matrix and the [3,1]
	// * translation vector
	// *
	// * Copies the current nodal data into vertex list (flattened out)
	// */
	// for (int vertexNr = 0; vertexNr < numVertices; vertexNr++) {
	// baseX = originalVertices[vertexNr * 3 + 0];
	// baseY = originalVertices[vertexNr * 3 + 1];
	// baseZ = originalVertices[vertexNr * 3 + 2];
	// /*
	// * Get transformation function for this vertex (possibly due to
	// * different functions on different subdomains)
	// */
	// float[] fun = funcSet[vertexLTFuncNr[vertexNr]];
	// /*
	// * Apply affine linear transformation
	// */
	// vertices[funcSetNr * numVertices * 3 + vertexNr * 3 + 0] = fun[0] * baseX
	// + fun[1] * baseY + fun[2]
	// * baseZ + fun[9];
	// vertices[funcSetNr * numVertices * 3 + vertexNr * 3 + 1] = fun[3] * baseX
	// + fun[4] * baseY + fun[5]
	// * baseZ + fun[10];
	// vertices[funcSetNr * numVertices * 3 + vertexNr * 3 + 2] = fun[6] * baseX
	// + fun[7] * baseY + fun[8]
	// * baseZ + fun[11];
	//
	// // float[] hlp = new float[3];
	// // System.arraycopy(vertices, funcNr * numVertices * 3 + vertexNr * 3,
	// hlp, 0, 3);
	// // if (hlp[0] > 4 || hlp[1] > 4 || hlp[2] > 4) {
	// // Log.d("GeoData", "Transformed node " + vertexNr + " from [" + baseX +
	// "," + baseY + "," + baseZ
	// // + "] to " + Arrays.toString(hlp));
	// // }
	// }
	// }
	// //System.arraycopy(originalVertices, 0, vertices, 0,
	// originalVertices.length);
	// //System.arraycopy(originalVertices, 0, vertices,
	// originalVertices.length, originalVertices.length);
	// // Center geometry using ALL possibly used nodes
	// centerModelGeometry();
	// }

}