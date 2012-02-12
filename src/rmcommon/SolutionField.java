/**
 * 
 */
package rmcommon;

import org.apache.commons.math.complex.Complex;

/**
 * @author CreaByte
 *
 */
public class SolutionField {
	private float[] real;
	private float[] imaginary;
	private float[] norms;
	
	private float min, max;
	
	public SolutionField(int size) {
		real = new float[size];
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
	}
	
	/**
	 * Compatibility constructor.
	 * Takes a float array and performs insertions for all values.
	 * @param values
	 */
	public SolutionField(float[] values) {
		this(values.length);
		for (int i=0;i<values.length;i++) {
			setRealValue(i, values[i]);
		}
	}
	
	public void setRealValue(int index, float value) {
		real[index] = value;
		if (min > value) min = value;
		if (max < value) max = value;
		//norms[index] = Math.abs(value);
	}
	
	public void setComplexValue(int index, Complex value) {
		setComplexValue(index, (float)value.getReal(), (float)value.getReal());
	}
	
	public void setComplexValue(int index, float r, float i) {
		if (imaginary == null) {
			imaginary = new float[real.length];
			norms = new float[real.length];
		}
		real[index] = r;
		imaginary[index] = i;
		norms[index] = (float)Math.sqrt(r*r + i*i);
	}
	
	public float[] getRealValues() {
		return real;
	}
	
	/**
	 * Returns an two times field size-dimensional float array 
	 * @return
	 */
	public float[][] getComplexValues() {
		return new float[][]{real, imaginary};
	}
	
	/**
	 * Returns the norms of each entry.
	 * 
	 * For complex solution fields only, returns null for real types.
	 * @return array of complex value norms
	 */
	public float[] getNorms() {
		return norms;
	}
	
	public boolean isReal() {
		return imaginary == null;
	}
	
	public float getMax() {
		return max;
	}
	
	public float getMin() {
		return min;
	}
	
	public boolean isConstant() {
		return Math.abs(min - max) < 1e-8;
	}
	
	public int getSize() {
		return real.length;
	}
}
