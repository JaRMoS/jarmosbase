/**
 * 
 */
package rmcommon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CreaByte
 * 
 *         Represents the results of a simulation.
 */
public class SimulationResult {

	private List<LogicSolutionField> fields;

	public SimulationResult() {
		fields = new ArrayList<LogicSolutionField>();
	}

	public void addField(LogicSolutionField field) {
//		// Perform some checks for consistency
//		for (SolutionField f : fields) {
//			assert f.getSize() == field.getSize();
//			assert displacements == null || displacements.getSize() == f.getSize();
//			if (f.getSize() != field.getSize())
//				throw new RuntimeException(
//						"Inconsistency! Cannot add solution field to collection as the size does not equal the size of already present fields.");
//			if (displacements != null && displacements.getSize() != f.getSize())
//				throw new RuntimeException("Inconsistency! Cannot add field as displacement data has different length.");
//		}
		fields.add(field);
	}

	/**
	 * Returns the number of solution fields (excluding geometric deformations)
	 * 
	 * @return
	 */
	public int getNumValueFields() {
		return fields.size();
	}

//	public boolean isComplex() {
//		boolean res = false;
//		for (SolutionField s : fields) {
//			res |= !s.isReal();
//		}
//		return res;
//	}

	public LogicSolutionField getField(int nr) {
		return fields.get(nr);
	}

	public List<LogicSolutionField> getLogicFields() {
		return fields;
	}
}
