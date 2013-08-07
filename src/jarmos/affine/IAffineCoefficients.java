package jarmos.affine;

/**
 * Base class for implementation of a series of time/parameter-dependent affine coefficients.
 * 
 * Especially used to provide the coefficients for AffParamMatrix instances.
 * 
 * @author Daniel Wirtz
 * 
 */
public interface IAffineCoefficients {

	/**
	 * 
	 * @return The number of coefficient functions
	 */
	public int getNumCoeffFcns();

	/**
	 * Evaluates all coefficient functions and returns a vector of the size of getNumCoeffFcns()
	 * 
	 * @param t
	 * The time t
	 * @param mu
	 * Parameter array
	 * @return Coefficient value array
	 */
	public double[] evaluateCoefficients(double t, double[] mu);

	/**
	 * 
	 * @return If this instance has time-dependent coefficients
	 */
	public boolean isTimeDependent();
}
