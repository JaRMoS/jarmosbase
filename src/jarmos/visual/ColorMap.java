package jarmos.visual;

/**
 * Interface for different colorizations.
 * 
 * Can be used in Visualization, at least models can specify the color map in their XML definition
 * 
 * @TODO implement!
 * 
 * @author Daniel Wirtz
 * @date Aug 29, 2011
 * 
 */
public interface ColorMap {

	public float[] getColor(float value);

}
