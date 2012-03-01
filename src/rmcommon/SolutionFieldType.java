/**
 * 
 */
package rmcommon;

/**
 * @author CreaByte
 *
 */
public enum SolutionFieldType {
	Displacement2D(2),
	Displacement3D(3),
	ComplexValue(2),
	RealValue(1);
	
	public final int requiredDoFFields;
	
	private SolutionFieldType(int usesfields) {
		requiredDoFFields = usesfields;
	}
}
