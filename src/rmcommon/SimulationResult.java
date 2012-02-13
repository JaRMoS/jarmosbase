/**
 * 
 */
package rmcommon;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

/**
 * @author CreaByte
 * 
 *         Represents the results of a simulation.
 */
public class SimulationResult {

	private List<SolutionField> fields;
	private List<SolutionField> deformations;

	public SimulationResult() {
		fields = new ArrayList<SolutionField>();
		deformations = new ArrayList<SolutionField>();
	}

	public void addField(SolutionField field, boolean isDeformation) {
		List<SolutionField> hlp = isDeformation ? deformations : fields;
		for (SolutionField f : hlp) {
			if (f.getSize() != field.getSize())
				throw new RuntimeException(
						"Cannot add solution field to collection as the size does not equal the size of already present fields.");
		}
		hlp.add(field);
	}

	/**
	 * Returns the number of solution fields (excluding geometric deformations)
	 * 
	 * @return
	 */
	public int getNumVisualFields() {
		return fields.size();
	}

	public boolean isComplex() {
		boolean res = false;
		for (SolutionField s : fields) {
			res |= !s.isReal();
		}
		return res;
	}

	public SolutionField getField(int nr) {
		return fields.get(nr);
	}

	public List<SolutionField> getFields() {
		return fields;
	}

	public boolean hasDeformationData() {
		return deformations.size() > 0;
	}

	/**
	 * 
	 * @return A list of SolutionFields for x,y and z deformations.
	 */
	public List<SolutionField> getDeformationData() {
		return deformations;
	}

}
