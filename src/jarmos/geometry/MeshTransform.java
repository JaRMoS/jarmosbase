package jarmos.geometry;

/**
 * A common interface for classes providing a mesh transformation.
 * 
 * Included in JaRMoSBase as part of the rbappmit heritage.
 * 
 * See the subclasses for concrete examples.
 * 
 * @author Daniel Wirtz
 * 
 */
public interface MeshTransform {

	public float[] transformMesh(float[] vertices);

}
