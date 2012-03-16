/**
 * 
 */
package jarmos;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * 
 * Factory method to create new RealMatrix instances. 
 * Might be worth while to later use different implementations for more efficiency.
 * 
 * @author dwirtz
 * 
 */
public class MathFactory {

	public static RealMatrix createRealMatrix() {
		return new Array2DRowRealMatrix();
	}
	
	public static RealMatrix createRealMatrix(int rows, int cols) {
		return new Array2DRowRealMatrix(rows, cols);
	}
	
	public static RealMatrix createRealMatrix(double[][] data) {
		return new Array2DRowRealMatrix(data);
	}
	
	public static RealVector createRealVector() {
		return new ArrayRealVector();
	}
	
	public static RealVector createRealVector(int size) {
		return new ArrayRealVector(size);
	}
	
	public static RealVector createRealVector(double[] data) {
		return new ArrayRealVector(data);
	}
}
