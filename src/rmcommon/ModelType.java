/**
 * 
 */
package rmcommon;

/**
 * Known model types within the JRMCommons project
 * @author dwirtz
 *
 */
public enum ModelType {
	/**
	 * This model type is unknown to JRMCommons. 
	 */
	Unknown,
	
	/**
	 * A JRB model
	 */
	JRB,
	
	/**
	 * A JKerMor model
	 */
	JKerMor,
	
	/**
	 * An rbappmit-model of old data format, compatible with JRB models.
	 */
	rbappmit;
	
	/**
	 * Parses a string into its matching ModelType
	 * @param strtype
	 * @return The matchin model type if found, ModelType.Unknown otherwise
	 */
	public static ModelType parse(String strtype) {
		for (ModelType type : ModelType.values()) {
			if (type.toString().toLowerCase().equals(strtype.toLowerCase())) return type;
		}
		return ModelType.Unknown;
	}
}
