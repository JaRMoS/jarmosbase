/**
 * 
 */
package jarmos.affine;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * @author CreaByte
 * 
 */
public class AffParamMatrix {

	private RealMatrix matrices;
	private IAffineCoefficients coeffs;
	private int rowsize;
	
	public boolean isTimeDependent() {
		return coeffs.isTimeDependent();
	}

	public AffParamMatrix(RealMatrix matrices, int rowsize,
			IAffineCoefficients coeffs) {
		this.matrices = matrices;
		this.coeffs = coeffs;
		this.rowsize = rowsize;
	}

	public RealMatrix compose(double t, double[] mu) {
		double[] c = coeffs.evaluateCoefficients(t, mu);
		RealVector comp = new ArrayRealVector(matrices.operate(c));
		int cols = comp.getDimension() / this.rowsize;
		Array2DRowRealMatrix res = new Array2DRowRealMatrix(rowsize, cols);
		for (int i = 0; i < cols; i++) {
			RealVector tmp = comp.getSubVector(i * rowsize, rowsize);
			res.setColumnVector(i, tmp);
		}
		return res;
	}
}
