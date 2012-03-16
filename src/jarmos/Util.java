/**
 * 
 */
package jarmos;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * @author Ernst
 * 
 */
public class Util {
	public static double[] linspace(double a, double b, int num) {
		double[] res = new double[num];
		double step = (b - a) / (num - 2);
		for (int i = 0; i < num - 1; i++) {
			res[i] = a + i * step;
		}
		res[num - 1] = b;
		return res;
	}

	/**
	 * The Java equivalent to Matlab's "range = a:step:b".
	 * 
	 * Returns null if a > b.
	 * 
	 * @param a
	 * @param step
	 * @param b
	 * @return
	 */
	public static double[] range(double a, double step, double b) {
//		int num = (int) Math.ceil((b - a) / step);
		if (a > b) return null;
		int num = 1;
		double hlp = a+step;
		while (hlp <= b) {
			hlp+=step;
			num++;
		}
		double[] res = new double[num];
		for (int i = 0; i < num; i++) {
			res[i] = a + i * step;
		}
		return res;
	}
	
	public static void vecAdd(double[] to, double[] what) {
		for (int i = 0; i < to.length; i++) {
			to[i] += what[i];
		}
	}

	public static RealVector randVector(int dim) {
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		RealVector res = MathFactory.createRealVector(dim);
		for (int i = 0; i < dim; i++) {
			res.setEntry(i, r.nextDouble());
		}
		return res;
	}

	public static RealMatrix randMatrix(int rows, int cols) {
		RealMatrix res = MathFactory.createRealMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			res.setRow(i, randVector(cols).getData());
		}
		return res;
	}
	
	public static String realMatToString(RealMatrix r) {
		String res = "RealMatrix("+r.getRowDimension()+"x"+r.getColumnDimension()+") of type '"+r.getClass().getName()+"'\n";
		for (int i=0;i<r.getRowDimension();i++) {
			res += Arrays.toString(r.getRow(i))+"\n";
		}
		return res;
	}
	
	public static String realVecToString(RealVector r) {
		String res = "RealVector("+r.getDimension()+") of type '"+r.getClass().getName()+"'\n";
		return res += Arrays.toString(r.getData())+"\n";
	}
	
}
