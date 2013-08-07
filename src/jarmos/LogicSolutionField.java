package jarmos;

import jarmos.visual.ColorGenerator;
import jarmos.visual.VisualFeature;

/**
 * @short Represents a logical solution field of a simulation.
 * 
 * In difference to the DoF fields (plain float/double arrays) these solution fields have their own structure to
 * represent different behavior. Examples are ComplexSolutionFields, which involve two DoF fields for real and imaginary
 * DoFs, or displacements fields which contain x,y (and z) DoF fields.
 * 
 * This class is a base class containing a FieldDescriptor instance providing solution field information. The data
 * storage is totally left to the subclasses, however, access to any desired visual.VisualFeature is enabled via the
 * #getVisualFeatures method.
 * 
 * For examples refer to the subclasses.
 * 
 * 
 * @author Daniel Wirtz
 * 
 */
public abstract class LogicSolutionField {

	public FieldDescriptor descriptor;

	/**
	 * Returns the number of graphical elements (nodes/vertices or elements/faces) that this field contains information
	 * for.
	 * 
	 * @return
	 */
	public abstract int getSize();

	/**
	 * Convenience method to determine if the solution field is constant in value. If this is the case, the default
	 * field colors are used for display.
	 * 
	 * @see visual.VisualizationData#computeVisualFeatures
	 * 
	 * @return True if the field data is constant
	 */
	public abstract boolean isConstant();

	public LogicSolutionField(FieldDescriptor f) {
		descriptor = f;
	}

	/**
	 * Abstract method that returns all available visual features for a logical solution field. This may be real and
	 * complex parts for complex fields or single x,y,z displacements
	 * 
	 * @param cg
	 * A color generator to use
	 * @return An array of VisualFeature instances
	 */
	public abstract VisualFeature[] getVisualFeatures(ColorGenerator cg);
}
