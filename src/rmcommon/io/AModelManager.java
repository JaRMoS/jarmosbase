package rmcommon.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rmcommon.IMessageHandler;
import rmcommon.ModelDescriptor;
import rmcommon.io.MathObjectReader.MachineFormats;

/***
 * This class serves as base class for accessing various types of models at
 * different locations.
 * 
 * Implementing classes implement the abstract members in order to reflect
 * necessary adoptions to different input sources like the file system, websites
 * or others like Android-Assets. Implemented in JKerMor are
 * {@link rmcommon.io.WebModelManager} and
 * {@link rmcommon.io.FileModelManager}.
 * 
 * Each manager has a root directory which must be, depending on the type,
 * either provided at instantiation or are given implicitly. The model system is
 * organized in a way that the root directory contains folders which each
 * contain a single model. Within each such folder, a model.xml-file must be
 * present that describes the model.
 * 
 * The allowed XML file structure is determined by the model.xsd file in the
 * JRMCommons jar file / project.
 * 
 * Despite the model.xml file needed in every model folder,
 * 
 * @author dwirtz
 * 
 */
public abstract class AModelManager {

	/**
	 * The name of the jar file inside a models directory containing .class
	 * files in java bytecode. This file name can be used inside custom
	 * implementations of getClassLoader, if directory searches are not
	 * permitted/implemented (e.g. WebModelManager)
	 */
	public static final String CLASSES_JARFILE = "classes.jar";

	/**
	 * This Exception gets thrown when an error occurs regarding the
	 * functionality of the ModelManager.
	 * 
	 * @author dwirtz
	 * 
	 */
	public class ModelManagerException extends Exception {

		private static final long serialVersionUID = 7411589173897801550L;

		/**
		 * @param msg
		 */
		public ModelManagerException(String msg) {
			super(msg);
		}

		/**
		 * @param msg
		 * @param inner
		 */
		public ModelManagerException(String msg, Exception inner) {
			super(msg, inner);
		}

	}

	/**
	 * The model's info html file name (imported from rbappmit, might change
	 * later)
	 */
	public static final String info_filename = "site_info.html";

	private String mdir = "notset";
	private DocumentBuilder db = null;
	private Validator dv = null;
	private Document modelxml = null;
	private Node modelnode = null;
	private MathObjectReader mor = null;

	private List<IMessageHandler> mhandlers;

	/**
	 * Constructs a new ModelManager and a private DocumentBuilder and
	 * SchemaFactory.
	 * 
	 * Unfortunately, the Android 8 API does not seem to support the W3C XML
	 * Schema, so no validation is performed on an android :-(
	 * 
	 * See
	 * {@link "http://stackoverflow.com/questions/3129934/schemafactory-doesnt-support-w3c-xml-schema-in-platform-level-8"}
	 * or
	 * {@link "http://developer.android.com/reference/javax/xml/validation/SchemaFactory.html#newInstance%28java.lang.String%29"}
	 * on how it SHOULD be..
	 */
	public AModelManager() {
		super();
		mhandlers = new ArrayList<IMessageHandler>();
		try {
			// Create the document builder
			DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
			bf.setIgnoringElementContentWhitespace(true);

			db = bf.newDocumentBuilder();

			// Create the schema validator
			InputStream in = getClass().getResourceAsStream("/model.xsd");
			// SchemaFactory sf =
			// SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI,
			// "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
			// getClass().getClassLoader());

			try {
				SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema s = sf.newSchema(new StreamSource(in));
				dv = s.newValidator();
			} catch (IllegalArgumentException e) {
				/*
				 * See constructor comment on what happens here.. Set Validator
				 * to null if the IllegalArgumentException gets thrown
				 */
				dv = null;
			}
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Error creating a XML document builder", e);
		} catch (SAXException e) {
			throw new RuntimeException("Error creating a XML schema validator", e);
		}
	}

	/**
	 * 
	 * @return The directory of the current model.
	 */
	public String getModelDir() {
		return mdir;
	}

	/**
	 * This method yields access to any specialized class loaders in subclasses.
	 * As the android platform works with different loaders then a JRE, for
	 * example, this interface is provided to enable custom classes loaded with
	 * whatever platform.
	 * 
	 * If subclasses do not override, the default system class loader is
	 * provided.
	 * 
	 * The class loader must be configured in a way that a call to
	 * loadClass(String name) must search also inside the current model's
	 * directory.
	 * 
	 * @return A custom class loader instance.
	 */
	public ClassLoader getClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	/**
	 * Returns the model type as given in the model.xml attribute "type" of the
	 * "model" tag.
	 * 
	 * @return The model type as string
	 */
	public String getModelType() {
		return getModelXMLAttribute("type");
	}

	/**
	 * Use this method in order to get a MathObjectReader instance fitted for
	 * the current selected model. This method returns an automatically
	 * configured reader (the old model data is encoded in little endian).
	 * 
	 * @return A MathObjectReader for the current selected model.
	 */
	public MathObjectReader getMathObjReader() {
		return mor;
	}

	/**
	 * Checks if a model.xml file exists in the specified directory and performs
	 * xsd-validation.
	 * 
	 * Note: On the current Android platform the validation using the xsd W3C
	 * Schema is somehow NOT implemented; so, validation is skipped on android
	 * platforms.
	 * 
	 * @param dir
	 *            The directory to check
	 * @return True if the directory contains a valid model, false otherwise
	 */
	public boolean isValidModelDir(String dir) {
		/*
		 *  Store current model directory if set, and temporarily set the model dir to dir.
		 *  This is done as subclass implementations will of course depend on getModelDir() when calling
		 *  modelFileExists().
		 */
		String olddir = mdir;
		mdir = dir;
		if (!modelFileExists("model.xml")) return false;
		try {
			try {
				db.parse(getInStream("model.xml"));
			} catch (SAXException e) {
				return false;
			}
			try {
				if (dv != null) {
					dv.validate(new DOMSource(modelxml));
				}
			} catch (SAXException se) {
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException("I/O error while checking if model directory is valid.", e);
		}
		// Restore old model dir
		mdir = olddir;
		return true;
	}

	/**
	 * Attempts to set the current directory as model directory. If the
	 * specified directory does not contain an model.xml file or the file does
	 * not comply with the model.xsd schema, an exception is thrown.
	 * 
	 * @param dir
	 *            The directory to change to
	 * @throws ModelManagerException
	 *             The current model directory does not contain a valid model.
	 */
	public void setModelDir(String dir) throws ModelManagerException {
		String olddir = mdir;
		this.mdir = dir;

		if (isValidModelDir(dir)) {
			try {
				modelxml = db.parse(getInStream("model.xml"));
			} catch (Exception e) {
				mdir = olddir;
				throw new RuntimeException("Unexpected Exception (parsing was done already in isValidModelDir("
						+ dir + "))", e);
			}
		} else
			throw new ModelManagerException("Invalid model directory: " + dir);
		// try {
		//
		//
		// try {
		// if (dv != null) {
		// dv.validate(new DOMSource(modelxml));
		// }
		// } catch (SAXException se) {
		// mdir = olddir;
		// throw new
		// ModelManagerException("model.xml validation failed for model in "
		// + dir, se);
		// }
		// } catch (IOException e) {
		// mdir = olddir;
		// throw new
		// ModelManagerException("I/O error when accessing model.xml in "
		// + dir, e);
		// }

		modelxml.normalize();
		modelnode = modelxml.getElementsByTagName("model").item(0);

		// Parse model data machine format
		mor = new MathObjectReader();
		String machformat = getModelXMLAttribute("machformat");
		if ("le".equals(machformat)) {
			mor.MachineFormat = MachineFormats.LittleEndian;
		} else {
			mor.MachineFormat = MachineFormats.BigEndian;
		}
	}

	/**
	 * @param h
	 */
	public void addMessageHandler(IMessageHandler h) {
		mhandlers.add(h);
	}

	/**
	 * @param h
	 */
	public void removeMessageHandler(IMessageHandler h) {
		mhandlers.remove(h);
	}

	protected void sendMessage(String msg) {
		for (IMessageHandler h : mhandlers) {
			h.sendMessage(msg);
		}
	}

	/**
	 * Returns the text content of a tag inside the model.xml file.
	 * 
	 * @param tagname
	 *            The tag whos value should be returned.
	 * @return The tag text content or null if none found.
	 */
	public String getModelXMLTagValue(String tagname) {
		if (modelxml != null) {

			// System.out.println("\nDocument body contents are:");
			// listNodes(modelxml.getDocumentElement(),"");

			NodeList nl = modelxml.getDocumentElement().getElementsByTagName(tagname);
			if (nl != null && nl.getLength() > 0) {
				return nl.item(0).getTextContent();
			}
		}
		// if (modelnode != null) {
		// NodeList nl = modelnode.getChildNodes();
		// if (nl != null) {
		// for (int i = 0; i < nl.getLength(); i++) {
		// Node c = nl.item(i);
		// if (tagname.equals(c.getNodeName())) {
		// return c.getTextContent();
		// }
		// }
		// }
		// }
		return null;
	}

	// private void listNodes(Node node, String indent) {
	// String nodeName = node.getNodeName();
	// System.out.println(indent+" Node: " + nodeName);
	// short type = node.getNodeType();
	// System.out.println(indent+" Node Type: " + nodeType(type));
	// if(type == TEXT_NODE){
	// System.out.println(indent+" Content is: "+((Text)node).getWholeText());
	// }
	//
	// NodeList list = node.getChildNodes();
	// if(list.getLength() > 0) {
	// System.out.println(indent+" Child Nodes of "+nodeName+" are:");
	// for(int i = 0 ; i<list.getLength() ; i++) {
	// listNodes(list.item(i),indent+"  ");
	// }
	// }
	// }
	//
	// private String nodeType(short type) {
	// switch(type) {
	// case ELEMENT_NODE: return "Element";
	// case DOCUMENT_TYPE_NODE: return "Document type";
	// case ENTITY_NODE: return "Entity";
	// case ENTITY_REFERENCE_NODE: return "Entity reference";
	// case NOTATION_NODE: return "Notation";
	// case TEXT_NODE: return "Text";
	// case COMMENT_NODE: return "Comment";
	// case CDATA_SECTION_NODE: return "CDATA Section";
	// case ATTRIBUTE_NODE: return "Attribute";
	// case PROCESSING_INSTRUCTION_NODE: return "Attribute";
	// }
	// return "Unidentified";
	// }

	/**
	 * Returns the attribute value of any attributes of the "model" tag in the
	 * model.xml file. Returns null if no model directory has been set or the
	 * attribute does not exist.
	 * 
	 * @param attrib_name
	 *            The attribute's name
	 * @return The attribute value or null if the attribute does not exist
	 */
	public String getModelXMLAttribute(String attrib_name) {
		assert attrib_name != null;

		if (modelnode != null) {
			return getNodeAttributeValue(modelnode, attrib_name);
		}
		return null;
	}

	/**
	 * Returns the attribute value of any attributes of the tag given by tagname
	 * in the model.xml file. Returns null if no model directory has been set or
	 * the attribute does not exist.
	 * 
	 * @param attrib_name
	 *            the attribute's name
	 * @param tagname
	 *            The xml tag whos attributes are to be searched.
	 * @return The attribute value or null if the attribute does not exist
	 */
	public String getModelXMLAttribute(String attrib_name, String tagname) {
		assert attrib_name != null;
		assert tagname != null;

		NodeList nl = modelxml.getDocumentElement().getElementsByTagName(tagname);
		if (nl.getLength() > 0) {
			return getNodeAttributeValue(nl.item(0), attrib_name);
		}
		return null;
	}

	private String getNodeAttributeValue(Node n, String attrib_name) {
		assert n != null;
		assert attrib_name != null;

		Node a = n.getAttributes().getNamedItem(attrib_name);
		if (a != null) {
			return a.getNodeValue();
		} else
			return null;
	}

	/**
	 * Checks if a specified tag exists inside the current models model.xml
	 * file.
	 * 
	 * @param tagname
	 *            The tag to check
	 * @return True if the tag exists or false otherwise
	 */
	public boolean xmlTagExists(String tagname) {
		return modelxml.getDocumentElement().getElementsByTagName(tagname).getLength() > 0;
	}

	/**
	 * 
	 * Returns the list of all models directories available at the ModelManagers
	 * source location. At this stage, no validity checks have to be performed
	 * regarding if a returned folder actually contains a valid model.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected abstract String[] getFolderList() throws IOException;

	/**
	 * Returns whether the specified file exists in the current model folder.
	 * 
	 * @param filename
	 * @return true if the file exists in the model, false otherwise
	 */
	public abstract boolean modelFileExists(String filename);

	/**
	 * Returns an InputStream instance streaming the contents of the file given
	 * by filename.
	 * 
	 * @param filename
	 *            The model file to return a stream for
	 * @return An InputStream pointing to the resource
	 * @throws IOException
	 */
	public final InputStream getInStream(String filename) throws IOException {
		sendMessage(filename);
		return getInStreamImpl(filename);
	}

	/**
	 * Template method.
	 * 
	 * Implementations of this method must locate the given file inside the
	 * current model directory and return an input stream pointing to it.
	 * 
	 * @param filename
	 *            The model file to return a stream for
	 * @return An InputStream pointing to the resource
	 * @throws IOException
	 */
	protected abstract InputStream getInStreamImpl(String filename)
			throws IOException;

	/**
	 * 
	 * @param filename
	 * @return A buffered reader with 8192 bytes buffer, pointing at the file
	 *         specified.
	 * @throws IOException
	 */
	public BufferedReader getBufReader(String filename) throws IOException {
		int buffer_size = 8192;

		InputStreamReader isr = new InputStreamReader(getInStream(filename));
		return new BufferedReader(isr, buffer_size);
	}

	/**
	 * Scans all directories given by getFolderList() for valid models and
	 * returns a list of model descriptors for each valid model.
	 * 
	 * @return A list of ModelDescriptors
	 * @throws ModelManagerException
	 */
	public List<ModelDescriptor> getModelDescriptors()
			throws ModelManagerException {
		ArrayList<ModelDescriptor> res = new ArrayList<ModelDescriptor>();
		String[] list = null;
		try {
			list = getFolderList();
		} catch (IOException e) {
			throw new ModelManagerException("Listing model folders failed.", e);
		}
		for (String modeldir : list) {
			if (isValidModelDir(modeldir)) {
				setModelDir(modeldir);
				InputStream img = null;
				String imgfile = getModelXMLTagValue("image");
				if (imgfile != null) {
					try {
						img = getInStream(imgfile);
					} catch (IOException e) {
						// Ignore when the image could not be loaded.
					}
				}
				res.add(new ModelDescriptor(modeldir, getModelXMLTagValue("short"), getModelXMLAttribute("type"), img));
			}
		}
		return res;
	}
}