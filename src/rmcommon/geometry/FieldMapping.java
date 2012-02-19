/**
 * Created on Aug 29, 2011 in Project JRMCommons
 * Location: rmcommon.geometry.DiscretizationType.java
 */
package rmcommon.geometry;

/**
 * @author Daniel Wirtz
 * @date Aug 30, 2011
 * 
 */
public enum FieldMapping {

	/**
	 * The field variables are to be mapped to geometry vertices, e.g. if
	 * the model is discretized using finite element methods.
	 * 
	 * The field solution values are values on nodes/vertices.
	 */
	VERTEX,

	/**
	 * The field variables are to be mapped to geometry elements, e.g. if
	 * the model is discretized using finite volume methods.
	 * 
	 * The field solution values are mean values on faces.
	 */
	ELEMENT,

	/**
	 * Unknown field mapping type
	 */
	UNKNOWN;

	/**
	 * Parses a string into a DiscretizationType
	 * 
	 * @param value
	 * @return The matching discretization type or FEM if no match is found.
	 */
	public static FieldMapping parse(String value) {
		if (value != null) {
			for (FieldMapping type : FieldMapping.values()) {
				if (type.toString().toLowerCase().equals(value.toLowerCase()))
					return type;
			}
		}
		return FieldMapping.UNKNOWN;
	}
}
