/**
 * 
 */
package jarmos.visual;

import jarmos.LogicSolutionField;

/**
 * 
 * Simple class for feature that can be visualized.
 * @author CreaByte
 *
 */
public class VisualFeature {
	
	public String Name;
	
	public float[] Colors;
	
	public LogicSolutionField Source;
	
	public VisualFeature(String name, float[] colors, LogicSolutionField source) {
		Name = name;
		Colors = colors;
		this.Source = source;
	}

}
