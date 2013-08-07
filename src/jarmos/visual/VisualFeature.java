package jarmos.visual;

import jarmos.LogicSolutionField;

/**
 * 
 * Simple class for feature that can be visualized.
 * 
 * Contains the name of the feature, a reference to the underlying LogicSolutionField and a float array containinig the
 * corresponding colors.
 * 
 * @author Daniel Wirtz
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
