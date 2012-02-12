/**
 * 
 */
package rmcommon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CreaByte
 *
 * Represents the results of a simulation.
 */
public class SimulationResult {
	
	private List<SolutionField> fields;
	
	public SimulationResult() {
		fields = new ArrayList<SolutionField>();
	}
	
	public boolean hasDeformationData() {
		return false;
	}
	
	/**
	 * 
	 * @return A three-dimensional array of SolutionFields for x,y and z deformations.
	 */
	public SolutionField[] getDeformationData() {
		return null;
	}

}
