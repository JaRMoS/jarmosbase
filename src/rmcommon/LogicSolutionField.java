/**
 * 
 */
package rmcommon;

import java.util.List;

import rmcommon.visual.ColorGenerator;
import rmcommon.visual.VisualFeature;


/**
 * Represents a logical solution field of a simulation.
 * In difference to the DoF fields (plain float/double arrays) these solution fields have their own structure
 * to represent different behavior. Examples are ComplexSolutionFields, which involve two DoF fields for real and imaginary DoFs,
 * or displacements fields which contain x,y (and z) DoF fields.
 * @author CreaByte
 * 
 */
public abstract class LogicSolutionField {
	
	public FieldDescriptor descriptor;

	/**
	 * Returns the number of graphical elements (nodes/vertices or
	 * elements/faces) that this field contains information for.
	 * 
	 * @return
	 */
	public abstract int getSize();

	public abstract boolean isConstant();

	public LogicSolutionField(FieldDescriptor f) {
		descriptor = f;
	}
	
	public abstract VisualFeature[] getVisualFeatures(ColorGenerator cg);
}
