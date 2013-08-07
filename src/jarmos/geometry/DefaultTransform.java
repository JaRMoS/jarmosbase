package jarmos.geometry;

/**
 * Default mesh transformation.
 * 
 * Simply copies the vertices as-is.
 * 
 * @author Daniel Wirtz
 * 
 */
public class DefaultTransform implements MeshTransform {

	/**
	 * 
	 * @see jarmos.geometry.MeshTransform#transformMesh(float[])
	 */
	@Override
	public float[] transformMesh(float[] vertices) {
		return vertices.clone();
	}

}
