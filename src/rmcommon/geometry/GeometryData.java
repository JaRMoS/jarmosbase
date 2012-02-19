package rmcommon.geometry;

import java.io.BufferedReader;
import java.io.IOException;

import rmcommon.Log;
import rmcommon.ModelType;
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
	public FieldMapping fieldMap = FieldMapping.VERTEX;

	private int[] vertexLTFuncNr; // tell us which subdomain our vertices
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
	public int numVertices;

	/**
	 * number of faces
	 */
	public int faces;

	/**
	 * The node coordinate vector of size 3*nodes, each node described by
	 * (x,y,z) coordinates
	 */
	public float[] vertices = null; // vertex data

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

	/**
	 * The reference nodes. A copy of the original nodes array which might be
	 * modified for models with changing geometry.
	 */
	public float[] originalVertices; // the original vertex data

	/**
	 * Faces data. Each face ranges over three short values, giving the indices
	 * of the three corner nodes/vertices.
	 */
	public short[] face; // face data

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
	public short[] face_wf; // edge data

	public float boxsize; // bounding box size

	private boolean is2D = true; // is our model 2D?

	/**
	 * calculate normal data for the current model
	 */
	private void compute3DNormalData() {
		normal = new float[numVertices * 3];
		fnormal = new float[faces * 3];
		float[] vecAB = new float[3];
		float[] vecAC = new float[3];
		int i, j, k;
		float length;
		// Initialize normal data and contribution flag
		int[] icount = new int[numVertices];
		for (i = 0; i < numVertices; i++) {
			normal[i * 3 + 0] = 0.0f;
			normal[i * 3 + 1] = 0.0f;
			normal[i * 3 + 2] = 0.0f;
			icount[i] = 0;
		}
		// calculate local face normal
		for (i = 0; i < faces; i++) {
			for (j = 0; j < 3; j++) {
				vecAB[j] = vertices[face[i * 3 + 1] * 3 + j] - vertices[face[i * 3 + 0] * 3 + j];
				vecAC[j] = vertices[face[i * 3 + 2] * 3 + j] - vertices[face[i * 3 + 0] * 3 + j];
			}
			// normal of the face is the cross product of AB and AC
			fnormal[i * 3 + 0] = vecAB[1] * vecAC[2] - vecAB[2] * vecAC[1];
			fnormal[i * 3 + 1] = vecAB[2] * vecAC[0] - vecAB[0] * vecAC[2];
			fnormal[i * 3 + 2] = vecAB[0] * vecAC[1] - vecAB[1] * vecAC[0];
			// normalize
			length = (float) Math.sqrt((fnormal[i * 3 + 0] * fnormal[i * 3 + 0] + fnormal[i * 3 + 1]
					* fnormal[i * 3 + 1] + fnormal[i * 3 + 2] * fnormal[i * 3 + 2]));
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
		for (i = 0; i < numVertices; i++) {
			for (j = 0; j < 3; j++)
				normal[i * 3 + j] = normal[i * 3 + j] / icount[i];
			length = (float) Math
					.sqrt((normal[i * 3 + 0] * normal[i * 3 + 0] + normal[i * 3 + 1] * normal[i * 3 + 1] + normal[i * 3 + 2]
							* normal[i * 3 + 2]));
			for (j = 0; j < 3; j++)
				normal[i * 3 + j] = normal[i * 3 + j] / length;
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
		for (int i = 0; i < vertices.length / 3; i++) {
			vertices[i * 3 + 0] -= xcen;
			vertices[i * 3 + 1] -= ycen;
			vertices[i * 3 + 2] -= zcen;
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
		for (int i = 0; i < vertices.length / 3; i++) {
			for (int j = 0; j < 3; j++) {
				nminmax[0 + j] = (nminmax[0 + j] > vertices[i * 3 + j]) ? vertices[i * 3 + j] : nminmax[0 + j];
				nminmax[3 + j] = (nminmax[3 + j] < vertices[i * 3 + j]) ? vertices[i * 3 + j] : nminmax[3 + j];
			}
		}
		is2D = false;
		if (Math.abs(nminmax[5] - nminmax[2]) < 1e-8)
			is2D = true;

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
			if (m.getModelType() == ModelType.JRB) {
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
		 * Compute the bounding box size of the nodes
		 */
		computeBoundingBox();

		/*
		 * Init color array
		 */
		// ucolor = new float[fields][nodes * 4];

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
			compute3DNormalData();

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
		numVertices = Integer.parseInt(tokens[0]);
		int count = 1;
		originalVertices = new float[numVertices * 3];
		vertices = new float[numVertices * 3];
		for (int i = 0; i < numVertices; i++) {
			originalVertices[i * 3 + 0] = Float.parseFloat(tokens[count]);
			originalVertices[i * 3 + 1] = Float.parseFloat(tokens[count + 1]);
			originalVertices[i * 3 + 2] = Float.parseFloat(tokens[count + 2]);
			count += 3;
		}
		System.arraycopy(originalVertices, 0, vertices, 0, originalVertices.length);

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
		/**
		 * Read in the number of the transformation function for each vertex
		 * (effectively modeling different functions for different geometry
		 * domains)
		 */
		vertexLTFuncNr = new int[numVertices];
		for (int i = 0; i < numVertices; i++) {
			vertexLTFuncNr[i] = Integer.parseInt(tokens[count]);
			count++;
		}
		domain_of_face = new int[faces];
		for (int i = 0; i < faces; i++) {
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
		vertices = mr.readRawFloatVector(m.getInStream("vertices.bin"));
		/*
		 * Assume the vector to contain 3D data. If we have 2D data, we insert
		 * zeros at every 3rd position!
		 */
		if ("2".equals(m.getModelXMLTagValue("geometry.dimension"))) {
			float[] tmpnode = new float[vertices.length + vertices.length / 2];
			for (int i = 0; i < vertices.length / 2; i++) {
				tmpnode[3 * i] = vertices[2 * i];
				tmpnode[3 * i + 1] = vertices[2 * i + 1];
				tmpnode[3 * i + 2] = 0;
			}
			vertices = tmpnode;
			tmpnode = null;
		}
		// Three coordinates per node
		numVertices = vertices.length / 3;
		originalVertices = vertices.clone();
		// Only one transformation function for JRB models if any
		vertexLTFuncNr = new int[numVertices];

		face = null;
		if (m.modelFileExists("faces.bin") // check included for backwards
											// compatibility.
				|| m.xmlTagExists("geometry.hasFaces")
				&& Boolean.parseBoolean(m.getModelXMLTagValue("geometry.hasFaces"))) {
			face = mr.readRawShortVector(m.getInStream("faces.bin"));
			// Subtract the indices, as the nodes are addressed with zero offset
			// inside java arrays
			for (int i = 0; i < face.length; i++) {
				face[i] -= 1;
			}
			// Three edges per face
			faces = face.length / 3;
			domain_of_face = new int[faces];
		}

		edges = null;
		if (m.modelFileExists("edges.bin")) {
			edges = mr.readRawShortVector(m.getInStream("edges.bin"));
		}

		if (m.modelFileExists("dir_nodes.bin")) {
			edges = mr.readRawShortVector(m.getInStream("edges.bin"));
		}

		// Read discretization type
		fieldMap = FieldMapping.parse(m.getModelXMLTagValue("geometry.fieldmapping"));
	}

	/**
	 * Sets the displacement data for this geometry according to the
	 * DisplacementField provided.
	 * 
	 * @param d
	 *            The displacement field
	 * 
	 * @return The number of vertex sets with different displacements available
	 */
	public int addDisplacements(DisplacementField d) {
		if (fieldMap != FieldMapping.VERTEX) {
			throw new RuntimeException("Displacements not possible for non-vertex based field mapping");
		}
		if (vertices.length != d.getSize() * 3) {
			throw new RuntimeException("Invalid displacement field. numVertices=" + numVertices
					+ ", vertices field length=" + vertices.length + ", displacement field size=" + d.getSize()
					+ " (x3=" + d.getSize() * 3 + ")");
		}
		float scaling = (d.getMax() - d.getMin()) / boxsize * 5;
		Log.d("GeoData", "Adding data from '" + d.descriptor + "', first vertices before: [" + vertices[0] + ","
				+ vertices[1] + "," + vertices[2] + "," + vertices[3] + "," + vertices[4] + "], scaling: " + scaling);
		int numFrames = (d.getSize() / numVertices);
		Log.d("GeoData", "numVertices=" + numVertices + ", vertices field length=" + vertices.length
				+ ", displacement field size=" + d.getSize() + " (x3=" + d.getSize() * 3 + ")");
		for (int frame = 0; frame < numFrames; frame++) {
			for (int nodenr = 0; nodenr < numVertices; nodenr++) {
				int idx = frame * numFrames + nodenr;
				vertices[3*idx] += d.getXDisplacements()[idx] / scaling;
				vertices[3*idx + 1] += d.getYDisplacements()[idx] / scaling;
				vertices[3*idx + 2] += d.getZDisplacements()[idx] / scaling;
			}
		}
		Log.d("GeoData", "First vertices after: [" + vertices[0] + "," + vertices[1] + "," + vertices[2] + ","
				+ vertices[3] + "," + vertices[4] + "]");
		centerModelGeometry();
		return numFrames;
	}

	/**
	 * @return If the data is 2D data
	 */
	public boolean is2D() {
		return is2D;
	}

	/**
	 * Applies Affine Linear Transformation To Vertices
	 * 
	 * @param afflinfuncs
	 */
	public void applyAffLinVertexTransformation(float[][][] afflinfuncs) {
		Log.d("GeoData", "Applying affine linear transformation to nodes (" + afflinfuncs.length + " sets/frames)");
		vertices = new float[afflinfuncs.length * numVertices * 3];
		float baseX, baseY, baseZ;
		for (int funcNr = 0; funcNr < afflinfuncs.length; funcNr++) {
			float[][] funcs = afflinfuncs[funcNr];
			/**
			 * Affine-linear transformation of a node to a new position. Uses
			 * the reference nodes and the LTfunc (linear transform function) to
			 * move the nodes to the specified location.
			 * 
			 * The crack in a pillar demo illustrates the use of this.
			 * 
			 * func is of size [number of subdomain, 12] a row of LTfunc is the
			 * rowwise flatten of the [3,3] transformation matrix and the [3,1]
			 * translation vector
			 * 
			 * Copies the current nodal data into vertex list (flattened out)
			 */
			for (int vertexNr = 0; vertexNr < numVertices; vertexNr++) {
				baseX = originalVertices[vertexNr * 3 + 0];
				baseY = originalVertices[vertexNr * 3 + 1];
				baseZ = originalVertices[vertexNr * 3 + 2];
				/*
				 * Get transformation function for this vertex (possibly due to
				 * different functions on different subdomains)
				 */
				float[] fun = funcs[vertexLTFuncNr[vertexNr]];
				/*
				 * Apply affine linear transformation
				 */
				vertices[funcNr * numVertices * 3 + vertexNr * 3 + 0] = fun[0] * baseX + fun[1] * baseY + fun[2]
						* baseZ + fun[9];
				vertices[funcNr * numVertices * 3 + vertexNr * 3 + 1] = fun[3] * baseX + fun[4] * baseY + fun[5]
						* baseZ + fun[10];
				vertices[funcNr * numVertices * 3 + vertexNr * 3 + 2] = fun[6] * baseX + fun[7] * baseY + fun[8]
						* baseZ + fun[11];
			}
		}
		// Center geometry using ALL possibly used nodes
		centerModelGeometry();
	}

}