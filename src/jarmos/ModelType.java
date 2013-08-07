package jarmos;

/**
 * @short Known model types within the JaRMoSBase project
 * 
 * @author Daniel Wirtz @date 2013-08-07
 * 
 */
public enum ModelType {
	/**
	 * This model type is unknown to JaRMoSBase.
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
	 * 
	 * @param strtype
	 * @return The matchin model type if found, ModelType.Unknown otherwise
	 */
	public static ModelType parse(String strtype) {
		if (strtype != null) {
			for (ModelType type : ModelType.values()) {
				if (type.toString().toLowerCase().equals(strtype.toLowerCase()))
					return type;
			}
		}
		return ModelType.Unknown;
	}
}
