/**
 * 
 */
package rmcommon;

import org.apache.commons.math.complex.Complex;

import rmcommon.visual.ColorGenerator;
import rmcommon.visual.VisualFeature;

/**
 * @author CreaByte
 * 
 */
public class ComplexSolutionField extends LogicSolutionField {

	private float[] real;
	private float[] imaginary;
	private float[] norms;
	private float rmin, rmax, imin, imax;

	public int getSize() {
		return real.length;
	}

	public boolean isConstant() {
		return Math.abs(rmin - rmax) < 1e-8 && Math.abs(imin - imax) < 1e-8;
	}

	public ComplexSolutionField(FieldDescriptor f, int size) {
		super(f);
		real = new float[size];
		rmin = Float.MAX_VALUE;
		rmax = Float.MIN_VALUE;
		imin = rmin;
		imax = rmax;
	}

	public void setComplexValue(int index, Complex value) {
		setComplexValue(index, (float) value.getReal(), (float) value.getReal());
	}

	public void addComplexValue(int index, Complex value) {
		setComplexValue(index, real[index] + (float) value.getReal(), imaginary[index] + (float) value.getReal());
	}

	public void addComplexValue(int index, float r, float i) {
		setComplexValue(index, real[index] + r, imaginary[index] + i);
	}

	public void setComplexValue(int index, float r, float i) {
		if (imaginary == null) {
			imaginary = new float[real.length];
			norms = new float[real.length];
		}
		if (rmin > r)
			rmin = r;
		if (rmax < r)
			rmax = r;
		real[index] = r;
		if (imin > i)
			imin = i;
		if (imax < i)
			imax = i;
		imaginary[index] = i;
		norms[index] = (float) Math.sqrt(r * r + i * i);
	}

	/**
	 * Returns an two times field size-dimensional float array
	 * 
	 * @return
	 */
	public float[][] getComplexValues() {
		return new float[][] { real, imaginary };
	}

	public float[] getRealDoFs() {
		return real;
	}

	public float[] getImaginaryDoFs() {
		return imaginary;
	}

	/**
	 * Returns the norms of each entry.
	 * 
	 * For complex solution fields only, returns null for real types.
	 * 
	 * @return array of complex value norms
	 */
	public float[] getNorms() {
		return norms;
	}

	public float getMaxRe() {
		return rmax;
	}

	public float getMinRe() {
		return rmin;
	}

	public float getMaxIm() {
		return imax;
	}

	public float getMinIm() {
		return imin;
	}

	@Override
	public VisualFeature[] getVisualFeatures(ColorGenerator cg) {
		return new VisualFeature[] {
				new VisualFeature(descriptor.Name + " (norms)", cg.computeColors(norms)),
				new VisualFeature(descriptor.Name + " (real)", cg.computeColors(real)),
				new VisualFeature(descriptor.Name + " (imag)", cg.computeColors(imaginary)) };
	}
}
