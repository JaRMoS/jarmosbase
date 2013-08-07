package jarmos;

import jarmos.geometry.DisplacementField;
import jarmos.geometry.MeshTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * @short Represents the results of a simulation.
 * 
 * Contains the LogicSolutionField list, how many parts (e.g. time-steps or simulations for different parameter values)
 * the fields contain. Moreover, the mesh transformations are also included (if given)
 * 
 * @author Daniel Wirtz
 * 
 */
public class SimulationResult {

	private List<LogicSolutionField> fields;
	private int parts;
	private List<MeshTransform> transforms;
	private boolean hasDispl;

	/**
	 * The number of parts in each LogicSolutionField.
	 * 
	 * Set to indicate how many different data sets are contained in all the fields (used for evolution problems or
	 * parameter sweeps)
	 * 
	 */
	public int getNumParts() {
		return parts;
	}

	public boolean hasDisplacements() {
		return hasDispl;
	}

	public SimulationResult(int parts) {
		fields = new ArrayList<LogicSolutionField>();
		transforms = new ArrayList<MeshTransform>();
		this.parts = parts;
		hasDispl = false;
	}

	public void addField(LogicSolutionField field) {
		// Perform some checks for consistency
		for (LogicSolutionField f : fields) {
			// assert f.getSize() == field.getSize();
			if (f.getSize() != field.getSize())
				throw new RuntimeException(
						"Inconsistency! Cannot add solution field to collection as the size does not equal the size of already present fields.");
		}
		hasDispl |= field instanceof DisplacementField;
		fields.add(field);
	}

	public void addTransform(MeshTransform m) {
		transforms.add(m);
	}

	public List<MeshTransform> getTransforms() {
		return transforms;
	}

	/**
	 * Returns the number of solution fields
	 * 
	 * @return
	 */
	public int getNumValueFields() {
		return fields.size();
	}

	public LogicSolutionField getField(int nr) {
		return fields.get(nr);
	}

	public List<LogicSolutionField> getLogicFields() {
		return fields;
	}
}
