/**
 * 
 */
package rmcommon.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.junit.Test;

import rmcommon.MathFactory;
import rmcommon.Util;
import rmcommon.io.MathObjectReader;

/**
 * @author dwirtz
 *
 */
public class MathObjectReaderTest {

	/**
	 * Test method for {@link rmcommon.io.MathObjectReader#readMatrix(java.lang.String)}.
	 */
	@Test
	public void testReadMatrixString() {
		
//		FileModelManager f = new FileModelManager(new File(".").getAbsolutePath());
//		try {
//			f.setModelDir("test");
//		} catch (ModelManagerException e1) {
//			fail(e1.getMessage());
//			e1.printStackTrace();
//		}
//		f.fileExists("test.bin");
		
		MathObjectReader rd = new MathObjectReader();
		
		RealMatrix m = null;
		try {
			m = rd.readMatrix("./test/test.bin");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		RealMatrix truth = MathFactory.createRealMatrix(new double[][]{new double[]{.5, 1.5}, new double[]{2.0, 1.0}});
		assertTrue(truth.equals(m));
		
		try {
			m = rd.readMatrix("./test/test2.bin");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		truth = MathFactory.createRealMatrix(new double[][]{new double[]{0, -.5, 3}, new double[]{13516.23425666, -13513.336, 100.000001}});
		assertTrue(truth.equals(m));
	}
	
	/**
	 * Test method for {@link rmcommon.io.MathObjectReader#readVector(java.lang.String)}.
	 */
	@Test
	public void testReadVectorString() {
		MathObjectReader rd = new MathObjectReader();
		
		RealVector v = null;
		try {
			v = rd.readVector("./test/testvec.bin");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		RealVector truth = MathFactory.createRealVector(Util.range(.5, .5, 23));
		System.out.println(truth);
		System.out.println(v);
		assertTrue(truth.equals(v));
	}

}
