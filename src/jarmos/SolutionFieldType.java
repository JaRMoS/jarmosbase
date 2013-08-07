package jarmos;

/**
 * @short This enum contains all so far known (to JaRMoSBase) types of logical solution fields.
 * 
 * @author Daniel Wirtz
 * 
 */
public enum SolutionFieldType {
	Displacement2D(2),
	Displacement3D(3),
	ComplexValue(1), // field units are Complex values (Type "Complex")
	RealValue(1);

	public final int requiredDoFFields;

	private SolutionFieldType(int usesfields) {
		requiredDoFFields = usesfields;
	}
}
