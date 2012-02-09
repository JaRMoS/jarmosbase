/**
 * 
 */
package rmcommon.affine;

/**
 * @author CreaByte
 *
 */
public interface IAffineCoefficients {
	
	public int getNumCoeffFcns();
	
	public double[] evaluateCoefficients(double t, double[] mu);
	
	public boolean isTimeDependent();
}
