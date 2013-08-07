package jarmos.geometry;

/**
 * An affine linear mesh transformation.
 * 
 * The different affine functions are stored in a 2D float array, where the first index corresponds to the function
 * index, and the second the different components of the affine transformation (multiplication matrix and offset = 12
 * values)
 * 
 * See the "demo1" AffineFunctions#get_local_transformation method for an example.
 * 
 * @author Daniel Wirtz
 * 
 */
public class AffineLinearMeshTransform implements MeshTransform {

	/**
	 * The affine functions array (n functions with each 12 floats for matrix multiplication + offset)
	 */
	private float[][] functions;

	/**
	 * Stores the index of the affine function for a given vertex index.
	 */
	private int[] vertexLTFuncNr;

	public AffineLinearMeshTransform(float[][] functions) {
		this(functions, null);
	}

	public AffineLinearMeshTransform(float[][] functions, int[] vertexLTFuncNr) {
		this.functions = functions;
		this.vertexLTFuncNr = vertexLTFuncNr;
	}

	/**
	 * @see jarmos.geometry.MeshTransform#transformMesh(float[])
	 */
	@Override
	public float[] transformMesh(float[] vertices) {
		float[] res = new float[vertices.length];
		float baseX, baseY, baseZ;
		/**
		 * Affine-linear transformation of a node to a new position. Uses the reference nodes and the LTfunc (linear
		 * transform function) to move the nodes to the specified location.
		 * 
		 * The crack in a pillar demo illustrates the use of this.
		 * 
		 * func is of size [number of subdomain, 12] a row of LTfunc is the rowwise flatten of the [3,3] transformation
		 * matrix and the [3,1] translation vector
		 * 
		 * Copies the current nodal data into vertex list (flattened out)
		 */
		for (int vertexNr = 0; vertexNr < res.length / 3; vertexNr++) {
			baseX = vertices[vertexNr * 3 + 0];
			baseY = vertices[vertexNr * 3 + 1];
			baseZ = vertices[vertexNr * 3 + 2];
			/*
			 * Get transformation function for this vertex (possibly due to different functions on different subdomains)
			 */
			float[] fun = functions[0];
			if (vertexLTFuncNr != null) {
				fun = functions[vertexLTFuncNr[vertexNr]];
			}
			/*
			 * Apply affine linear transformation
			 */
			res[vertexNr * 3 + 0] = fun[0] * baseX + fun[1] * baseY + fun[2] * baseZ + fun[9];
			res[vertexNr * 3 + 1] = fun[3] * baseX + fun[4] * baseY + fun[5] * baseZ + fun[10];
			res[vertexNr * 3 + 2] = fun[6] * baseX + fun[7] * baseY + fun[8] * baseZ + fun[11];
		}
		return res;
	}
}
