/**
 * 
 */
package rmcommon.geometry;

/**
 * 
 * Simply copies the vertices as-is 
 * 
 * @author CreaByte
 *
 */
public class DefaultTransform implements MeshTransform {

	/** (non-Javadoc)
	 * @see rmcommon.geometry.MeshTransform#transformMesh(float[])
	 */
	@Override
	public float[] transformMesh(float[] vertices) {
		return vertices.clone();
	}

}