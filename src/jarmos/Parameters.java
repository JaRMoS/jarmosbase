package jarmos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @short A class for model parameters.
 * 
 * New parameters can be added, parameter ranges queried or random parameters extracted.
 * 
 * Moreover, a current parameter can be set and accessed for convenience.
 * 
 * @author Daniel Wirtz
 * 
 */
public class Parameters {

	private class Parameter {
		public String name, label;
		double min;
		double max;

		public Parameter(String name, double minval, double maxval) {
			this(name, minval, maxval, name);
		}

		public Parameter(String name, double minval, double maxval, String label) {
			this.name = name;
			this.label = label;
			this.max = maxval;
			this.min = minval;
		}
	}

	private List<Parameter> params;
	private double[] values;

	/**
	 * Creates a new Parameters object.
	 */
	public Parameters() {
		params = new ArrayList<Parameter>();
		values = new double[0];
	}

	public void addParam(String name, double minval, double maxval) {
		params.add(new Parameter(name, minval, maxval));
		double[] tmp = values.clone();
		values = new double[values.length + 1];
		for (int i = 0; i < tmp.length; i++) {
			values[i] = tmp[i];
		}
		values[values.length - 1] = minval;
	}

	/**
	 * Gets the current parameter
	 * 
	 * @return A double vector
	 */
	public double[] getCurrent() {
		return values;
	}

	/**
	 * Sets the current parameter to the values passed. If the vector size does not match the current parameter number,
	 * an IllegalArgumentException is thrown.
	 * 
	 * @param newvalues
	 */
	public void setCurrent(double[] newvalues) {
		if (values.length != newvalues.length)
			throw new IllegalArgumentException("Wrong parameter values length. Current: " + values.length + ", Wanted:"
					+ newvalues.length);
		for (int i = 0; i < values.length; i++) {
			values[i] = newvalues[i];
		}
	}

	/**
	 * Sets the value of the current parameter at position index
	 * 
	 * @param index
	 * The parameter index
	 * @param value
	 * The new value
	 */
	public void setCurrent(int index, double value) {
		if (index < 0 || index > values.length - 1)
			throw new IllegalArgumentException("Invalid parameter index: " + index + ". Max index is "
					+ (values.length - 1));
		values[index] = value;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public double getMaxValue(int i) {
		return params.get(i).max;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public double getMinValue(int i) {
		return params.get(i).min;
	}

	/**
	 * Returns the label for the i-th parameter
	 * 
	 * @param i
	 * @return
	 */
	public String getLabel(int i) {
		return params.get(i).label;
	}

	/**
	 * Returns the name for the i-th parameter
	 * 
	 * @param i
	 * @return
	 */
	public String getName(int i) {
		return params.get(i).name;
	}

	// public List<Parameter> getParams() {
	// return Collections.unmodifiableList(params);
	// }

	public int getNumParams() {
		return params.size();
	}

	public double[] getRandomParam() {
		double[] res = new double[getNumParams()];
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < res.length; i++) {
			res[i] = params.get(i).min + r.nextDouble() * (params.get(i).max - params.get(i).min);
		}
		return res;
	}

}
