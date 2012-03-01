/**
 * 
 */
package rmcommon.geometry;

import rmcommon.FieldDescriptor;
import rmcommon.LogicSolutionField;
import rmcommon.visual.ColorGenerator;
import rmcommon.visual.VisualFeature;

/**
 * @author CreaByte
 * 
 */
public class DisplacementField extends LogicSolutionField {

	private float[][] displ;
	private float min, max;

	public DisplacementField(FieldDescriptor f, int size) {
		super(f);
		if (f.Mapping != FieldMapping.VERTEX) {
			throw new RuntimeException("Invalid field mapping type " + f.Mapping + " for displacement fields!");
		}
		displ = new float[3][size];
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
	}

	public DisplacementField(FieldDescriptor f, float[][] xyz) {
		this(f, xyz[0].length);
		if (xyz.length == 2) {
			xyz[3] = new float[xyz[0].length];
		}
		this.displ = xyz;
	}

	public void setDisplacement(int index, float x, float y, float z) {
		displ[0][index] = x;
		if (min > x)
			min = x;
		if (max < x)
			max = x;
		displ[1][index] = y;
		if (min > y)
			min = y;
		if (max < y)
			max = y;
		displ[2][index] = z;
		if (min > z)
			min = z;
		if (max < z)
			max = z;
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}

	public void setDisplacement(int index, float x, float y) {
		setDisplacement(index, x, y, 0);
	}

	public float[] getXDisplacements() {
		return displ[0];
	}

	public float[] getYDisplacements() {
		return displ[1];
	}

	public float[] getZDisplacements() {
		return displ[2];
	}

	@Override
	public int getSize() {
		return (displ != null) ? displ[0].length : 0;
	}

	@Override
	public boolean isConstant() {
		return Math.abs(min - max) < 1e-8;
	}

	@Override
	public VisualFeature[] getVisualFeatures(ColorGenerator cg) {
		boolean twodim = true;
		for (int i = 0; i < displ[2].length; i++) {
			twodim &= displ[2][i] == 0;
		}
		if (twodim) {
			return new VisualFeature[] {
					new VisualFeature(descriptor.Name + "x displ", cg.computeColors(displ[0]), this),
					new VisualFeature(descriptor.Name + "y displ", cg.computeColors(displ[1]), this) };
		} else {
			return new VisualFeature[] {
					new VisualFeature(descriptor.Name + "x displ", cg.computeColors(displ[0]), this),
					new VisualFeature(descriptor.Name + "y displ", cg.computeColors(displ[1]), this),
					new VisualFeature(descriptor.Name + "z displ", cg.computeColors(displ[2]), this) };
		}

	}
}
