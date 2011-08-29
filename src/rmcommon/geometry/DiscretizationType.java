/**
 * Created on Aug 29, 2011 in Project JRMCommons
 * Location: rmcommon.geometry.DiscretizationType.java
 */
package rmcommon.geometry;

/**
 * @author Daniel Wirtz
 * @date Aug 29, 2011
 *
 */
/**
 * @author Daniel Wirtz
 * @date Aug 30, 2011
 *
 */
public enum DiscretizationType {
	
	/**
	 * Model is discretized using finite element methods.
	 * 
	 * The field solution values are values on nodes/vertices. 
	 */
	FEM,
	
	/**
	 * Model is discretized using finite volume methods.
	 * 
	 * The field solution values are mean values on faces.
	 */
	FV;
	
	/**
	 * Parses a string into a DiscretizationType
	 * 
	 * @param value
	 * @return The matching discretization type or FEM if no match is found.
	 */
	public static DiscretizationType parse(String value) {
		for (DiscretizationType type: DiscretizationType.values()) {
			if (type.toString().equals(value)) return type;
		}
		return DiscretizationType.FEM;
	}
}
