/**
 * Created on Aug 28, 2011 in Project JRMCommons
 * Location: rmcommon.io.AModelManagerTest.java
 */
package rmcommon.test;

import static org.junit.Assert.*;

import org.junit.Test;

import rmcommon.io.AModelManager;
import rmcommon.io.FileModelManager;
import rmcommon.io.AModelManager.ModelManagerException;

/**
 * @author Daniel Wirtz
 * @date Aug 28, 2011
 *
 */
public class AModelManagerTest {

//	/**
//	 * Test method for {@link rmcommon.io.AModelManager#getModelXMLAttribute(java.lang.String)}.
//	 */
//	@Test
//	public void testGetModelXMLAttributeString() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link rmcommon.io.AModelManager#getModelXMLAttribute(java.lang.String, java.lang.String)}.
//	 */
//	@Test
//	public void testGetModelXMLAttributeStringString() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link rmcommon.io.AModelManager#getModelXMLTagValue(java.lang.String)}.
	 */
	@Test
	public void testGetModelXMLTagValue() {
		AModelManager m = new FileModelManager(".");
		try {
			m.setModelDir("test");
		} catch (ModelManagerException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		assertTrue("someimage!".equals(m.getModelXMLTagValue("description.image")));
		assertTrue("someshortdesc".equals(m.getModelXMLTagValue("description.short")));
		assertTrue(m.getModelXMLTagValue("nonexistent.not.really") == null);
	}

}
