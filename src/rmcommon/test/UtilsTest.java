/**
 * 
 */
package rmcommon.test;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import rmcommon.Log;
import rmcommon.visual.ColorGenerator;

/**
 * @author Ernst
 *
 */
public class UtilsTest {

	/**
	 * Test method for {@link rmcommon.Util#linspace(double, double, int)}.
	 */
	@Test
	public void testLinspace() {
//		double a = 0;
//		double b = 10;
//		double[] s = Util.linspace(a, b, 10);
//		
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link rmcommon.Util#range(double, double, double)}.
	 */
	@Test
	public void testRange() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testColorGen() {
		int s = 1000;
		ColorGenerator cg = new ColorGenerator();
		Log.d("VisData","Default colors of size "+s+": "+Arrays.toString(cg.getDefaultColor(s)));
	} 
}
