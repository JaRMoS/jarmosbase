/**
 * 
 */
package rmcommon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Daniel Wirtz
 * 
 */
public class Parameters {

	public class Parameter {
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

	public Parameters() {
		params = new ArrayList<Parameter>();
	}

	public void addParam(String name, double minval, double maxval) {
		params.add(new Parameter(name, minval, maxval));
	}

	public List<Parameter> getParams() {
		return Collections.unmodifiableList(params);
	}

	public int getParamNumber() {
		return params.size();
	}

	public double[] getRandomParam() {
		double[] res = new double[getParamNumber()];
		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < res.length; i++) {
			res[i] = r.nextDouble() * (params.get(i).max - params.get(i).min);
		}
		return res;
	}

}
