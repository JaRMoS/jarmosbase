/**
 * 
 */
package jarmos;

import jarmos.geometry.FieldMapping;
import jarmos.visual.ColorGenerator;
import jarmos.visual.VisualFeature;

/**
 * @author CreaByte
 * 
 */
public class DefaultSolutionField extends LogicSolutionField {

	private float[] values;

	public int getSize() {
		return values.length;
	}

	private float min, max;

	public DefaultSolutionField(FieldDescriptor f, int size) {
		super(f);
		values = new float[size];
		min = Float.MAX_VALUE;
		max = Float.MIN_VALUE;
	}

	/**
	 * Compatibility constructor. Takes a float array and performs insertions
	 * for all values.
	 * 
	 * @param values
	 */
	public DefaultSolutionField(FieldDescriptor f, float[] values) {
		this(f, values.length);
		for (int i = 0; i < values.length; i++) {
			setValue(i, values[i]);
		}
	}

	public void setValue(int index, float value) {
		values[index] = value;
		if (min > value)
			min = value;
		if (max < value)
			max = value;
		// norms[index] = Math.abs(value);
	}

//	public float[] getDoFs() {
//		return values;
//	}

	public float getMax() {
		return max;
	}

	public float getMin() {
		return min;
	}

	public boolean isConstant() {
		return Math.abs(min - max) < 1e-8;
	}

	public static LogicSolutionField getZeroField(int size, FieldMapping mapping) {
		FieldDescriptor f = new FieldDescriptor(SolutionFieldType.RealValue);
		f.Name = "All-Zero real field";
		f.Mapping = mapping;
		return new DefaultSolutionField(f, size);
	}

	@Override
	public VisualFeature[] getVisualFeatures(ColorGenerator cg) {
		String n = descriptor.Name;
		if (n == null || n == "") {
			n = "(no name)";
		}
		return new VisualFeature[] { new VisualFeature(descriptor.Name, cg.computeColors(values), this) };
	}
}
