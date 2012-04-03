/**
 * Created on Aug 28, 2011 in Project JRMCommons
 * Location: jarmos.io.AModelManagerTest.java
 */
package jarmos.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jarmos.io.AModelManager;
import jarmos.io.AModelManager.ModelManagerException;
import jarmos.io.FileModelManager;

import org.junit.Test;


/**
 * @author Daniel Wirtz
 * @date Aug 28, 2011
 *
 */
public class AModelManagerTest {

//	/**
//	 * Test method for {@link jarmos.io.AModelManager#getModelXMLAttribute(java.lang.String)}.
//	 */
//	@Test
//	public void testGetModelXMLAttributeString() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link jarmos.io.AModelManager#getModelXMLAttribute(java.lang.String, java.lang.String)}.
//	 */
//	@Test
//	public void testGetModelXMLAttributeStringString() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link jarmos.io.AModelManager#getModelXMLTagValue(java.lang.String)}.
	 */
	@Test
	public void testGetModelXMLTagValue() {
		AModelManager m = new FileModelManager(".");
		try {
			m.useModel("test");
		} catch (ModelManagerException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		assertTrue("someimage!".equals(m.getModelXMLTagValue("description.image")));
		assertTrue("someshortdesc".equals(m.getModelXMLTagValue("description.short")));
		assertTrue(m.getModelXMLTagValue("nonexistent.not.really") == null);
	}

}
