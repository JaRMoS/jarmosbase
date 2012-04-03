/**
 * 
 */
package jarmos.test;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Test;

/**
 * @author CreaByte
 *
 */
public class ModelXMLTest {

	@Test
	public void test() throws Exception {
		InputStream in = new FileInputStream("model.xsd");
		assertTrue(in != null);
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema s = sf.newSchema(new StreamSource(in));
		s.newValidator();
	}

}
