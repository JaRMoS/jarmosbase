package jarmos.io;

import jarmos.FieldDescriptor;
import jarmos.IMessageHandler;
import jarmos.Log;
import jarmos.ModelDescriptor;
import jarmos.ModelType;
import jarmos.Parameters;
import jarmos.SolutionFieldType;
import jarmos.geometry.FieldMapping;
import jarmos.io.MathObjectReader.MachineFormats;
import jarmos.util.ConsoleProgressReporter;
import jarmos.util.IProgressReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class serves as base class for accessing various types of models at different locations.
 * 
 * Implementing classes implement the abstract members in order to reflect necessary adoptions to different input
 * sources like the file system, websites or others like Android-Assets.
 * 
 * Implemented in JKerMor are {@link jarmos.io.WebModelManager} and {@link jarmos.io.FileModelManager}.
 * 
 * Each manager has a root directory which must be, depending on the type, either provided at instantiation or are given
 * implicitly. The model system is organized in a way that the root directory contains folders which each contain a
 * single model. Within each such folder, a model.xml-file must be present that describes the model.
 * 
 * The allowed XML file structure is determined by the model.xsd file in the JaRMoSBase project.
 * 
 * @author Daniel Wirtz @date 2013-08-07
 * 
 * 
 */
public abstract class AModelManager {

	/**
	 * This Exception gets thrown when an error occurs regarding the functionality of the ModelManager.
	 * 
	 * @author Daniel Wirtz @date 2013-08-07
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
	 * The name of the jar file inside a models directory containing .class files in java bytecode. This file name can
	 * be used inside custom implementations of getClassLoader, if directory searches are not permitted/implemented
	 * (e.g. WebModelManager)
	 */
	public static final String CLASSES_JARFILE = "classes.jar";

	/**
	 * The model's info html file name (imported from rbappmit, might change later)
	 */
	public static final String info_filename = "site_info.html";

	private DocumentBuilder db = null;
	private Validator dv = null;
	private String mdir = "notset";
	private List<IMessageHandler> mhandlers;
	private Node modelnode = null;
	private Document modelxml = null;
	private ModelType mtype = ModelType.Unknown;

	private MathObjectReader mor = null;

	/**
	 * Constructs a new ModelManager and a private DocumentBuilder and SchemaFactory.
	 * 
	 * Unfortunately, the Android 8 API does not seem to support the W3C XML Schema, so no validation is performed on an
	 * android :-(
	 * 
	 * See
	 * {@link "http://stackoverflow.com/questions/3129934/schemafactory-doesnt-support-w3c-xml-schema-in-platform-level-8"}
	 * or
	 * {@link "http://developer.android.com/reference/javax/xml/validation/SchemaFactory.html#newInstance%28java.lang.String%29"}
	 * on how it SHOULD be..
	 */
	public AModelManager() {
		mhandlers = new ArrayList<IMessageHandler>();
		try {
			// Create the document builder
			DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
			bf.setIgnoringElementContentWhitespace(true);

			db = bf.newDocumentBuilder();

			InputStream in = getClass().getResourceAsStream("/model.xsd");

			// Create the schema validator (if xsd was found)
			if (in != null) {
				try {
					SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					Schema s = sf.newSchema(new StreamSource(in));
					// dv = s.newValidator();
				} catch (IllegalArgumentException e) { //
					/*
					 * See constructor comment on what happens here.. Set Validator to null if the
					 * IllegalArgumentException gets thrown
					 */
					dv = null;
				}
			} else
				Log.e("AModelManager", "No model.xsd validation resource found!");
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Error creating a XML document builder", e);
		} catch (SAXException e) {
			throw new RuntimeException("Error creating a XML schema validator", e);
		}
	}

	/**
	 * @param h
	 */
	public void addMessageHandler(IMessageHandler h) {
		mhandlers.add(h);
	}

	/**
	 * Loads a class available in the precompiled classes associated with the current model. The model.xml-tag "package"
	 * is used if set to specify the package under which the class can be found; if not set the default package (="") is
	 * used.
	 * 
	 * @param name
	 * @return
	 * @throws ModelManagerException
	 */
	public Object loadModelClass(String name) throws ModelManagerException {
		ClassLoader cl = getClassLoader();
		String pkg = getModelPackageStr();
		try {
			Class<?> af = cl.loadClass(pkg + name);
			return af.newInstance();
		} catch (Exception e) {
			throw new ModelManagerException("Error loading the model class '" + name + "' of package '"
					+ (pkg != "" ? pkg : "(default)") + "'.", e);
		}
	}

	/**
	 * 
	 * @param filename
	 * @return A buffered reader with 8192 bytes buffer, pointing at the file specified.
	 * @throws IOException
	 * @deprecated Dont use, only here for old @ref rbappmit model loading.
	 */
	public BufferedReader getBufReader(String filename) throws IOException {
		int buffer_size = 8192;

		InputStreamReader isr = new InputStreamReader(getInStream(filename));
		return new BufferedReader(isr, buffer_size);
	}

	/**
	 * This method yields access to any specialized class loaders in subclasses. As the android platform works with
	 * different loaders then a JRE, for example, this interface is provided to enable custom classes loaded with
	 * whatever platform.
	 * 
	 * If subclasses do not override, the default system class loader is provided.
	 * 
	 * The class loader must be configured in a way that a call to loadClass(String name) must search also inside the
	 * current model's directory.
	 * 
	 * @return A custom class loader instance.
	 */
	public ClassLoader getClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	/**
	 * 
	 * Returns the list of all models directories available at the ModelManagers source location. At this stage, no
	 * validity checks have to be performed regarding if a returned folder actually contains a valid model.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected abstract String[] getFolderList() throws IOException;

	/**
	 * A short message that writes "loading SD models" dependent on the actual instance
	 * 
	 * @return
	 */
	protected abstract String getLoadingMessage();

	/**
	 * Returns an InputStream instance streaming the contents of the file given by filename.
	 * 
	 * @param filename
	 * The model file to return a stream for
	 * @return An InputStream pointing to the resource
	 * @throws IOException
	 */
	public final InputStream getInStream(String filename) throws IOException {
		int pos = filename.indexOf(".bin");
		sendMessage((pos > -1) ? filename.substring(0, pos) : filename);
		return getInStreamImpl(filename);
	}

	/**
	 * Template method.
	 * 
	 * Implementations of this method must locate the given file inside the current model directory and return an input
	 * stream pointing to it.
	 * 
	 * @param filename
	 * The model file to return a stream for
	 * @return An InputStream pointing to the resource
	 * @throws IOException
	 */
	protected abstract InputStream getInStreamImpl(String filename) throws IOException;

	/**
	 * Use this method in order to get a MathObjectReader instance fitted for the current selected model. This method
	 * returns an automatically configured reader (the old model data is encoded in little endian).
	 * 
	 * @return A MathObjectReader for the current selected model.
	 */
	public MathObjectReader getMathObjReader() {
		return mor;
	}

	/**
	 * Scans all directories given by getFolderList() for valid models and returns a list of model descriptors for each
	 * valid model.
	 * 
	 * @return A list of ModelDescriptors
	 * @throws ModelManagerException
	 */
	public List<ModelDescriptor> getModelDescriptors() throws ModelManagerException {
		return getModelDescriptors(new ConsoleProgressReporter());
	}

	/**
	 * Scans all directories given by getFolderList() for valid models and returns a list of model descriptors for each
	 * valid model.
	 * 
	 * @param pr
	 * A IProgressReporter instance to report process in the loading to.
	 * 
	 * @return A list of ModelDescriptors
	 * @throws ModelManagerException
	 */
	public List<ModelDescriptor> getModelDescriptors(IProgressReporter pr) throws ModelManagerException {
		ArrayList<ModelDescriptor> res = new ArrayList<ModelDescriptor>();
		String[] list = null;
		try {
			list = getFolderList();
		} catch (IOException e) {
			throw new ModelManagerException("Listing model folders failed.", e);
		}
		// Set loading message
		pr.init(getLoadingMessage(), list.length);
		int cnt = 0;
		for (String modeldir : list) {
			pr.progress(++cnt);
			if (isValidModelDir(modeldir)) {
				useModel(modeldir);
				// Check for the correct model type
				ModelType mtype = ModelType.parse(getModelXMLAttribute("type"));
				if (mtype == ModelType.Unknown)
					continue;

				InputStream img = null;
				String imgfile = getModelXMLTagValue("description.image");
				if (modelFileExists(imgfile)) {
					try {
						img = getInStream(imgfile);
					} catch (IOException e) {
						// ignore
					}
				} else
					img = getClass().getClassLoader().getResourceAsStream("notfound.png");
				// Get model date
				Date d = null;
				try {
					d = DateFormat.getDateInstance().parse(getModelXMLTagValue("description.created"));
				} catch (ParseException e) {
					d = Calendar.getInstance().getTime();
				}
				ModelDescriptor md = new ModelDescriptor(modeldir, getModelXMLTagValue("description.name"), mtype, img,
						d);
				md.shortDescription = getModelXMLTagValue("description.short", "");
				res.add(md);
			}
		}
		pr.finish();
		return res;
	}

	/**
	 * 
	 * @return The directory of the current model.
	 */
	public String getModelDir() {
		return mdir;
	}

	public FieldDescriptor[] getModelFieldTypes() {
		FieldDescriptor[] res = null;
		Element params = getModelXMLElement("visual.fields");
		if (params != null) {
			NodeList nl = params.getElementsByTagName("field");
			res = new FieldDescriptor[nl.getLength()];
			String hlp;
			FieldDescriptor f;
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				f = new FieldDescriptor(SolutionFieldType.valueOf(getNodeAttributeValue(n, "type")));
				f.Name = n.getTextContent();
				hlp = getNodeAttributeValue(n, "mapping");
				f.Mapping = hlp != null ? FieldMapping.valueOf(hlp) : FieldMapping.UNKNOWN;
				res[i] = f;
			}
		}
		return res;
	}

	/**
	 * Returns the model type as given in the model.xml attribute "type" of the "model" tag.
	 * 
	 * @return The model type as string
	 */
	public ModelType getModelType() {
		return mtype;
	}

	/**
	 * Returns an URI for the current model location/directory
	 * 
	 * @return
	 */
	public abstract URI getModelURI();

	/**
	 * Returns the attribute value of any attributes of the "model" tag in the model.xml file. Returns null if no model
	 * directory has been set or the attribute does not exist.
	 * 
	 * @param attrib_name
	 * The attribute's name
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
	 * Returns the attribute value of any attributes of the tag given by tagname in the model.xml file. Returns null if
	 * no model directory has been set or the attribute does not exist.
	 * 
	 * @param attrib_name
	 * the attribute's name
	 * @param tagname
	 * The xml tag whos attributes are to be searched.
	 * @return The attribute value or null if the attribute does not exist
	 */
	public String getModelXMLAttribute(String attrib_name, String tagname) {
		assert attrib_name != null;
		assert tagname != null;

		Element e = getModelXMLElement(tagname);
		return (e != null) ? getNodeAttributeValue(e, attrib_name) : null;
	}

	// private String[] getXMLElementChildNames(String tags) {
	// String[] res = null;
	// Element parent = getModelXMLElement(tags);
	// if (parent != null) {
	// res = new String[parent.getChildNodes().getLength()];
	// for (int nidx = 0; nidx < res.length; nidx++) {
	// res[nidx] = parent.getChildNodes().item(nidx).getNodeName();
	// }
	// }
	// return res;
	// }

	private Element getModelXMLElement(String tags) {
		if (modelxml != null) {
			Element cur = modelxml.getDocumentElement();
			String[] elems = tags.split("\\.");
			for (String elem : elems) {
				NodeList nl = cur.getElementsByTagName(elem);
				if (nl != null && nl.getLength() > 0) {
					cur = (Element) nl.item(0);
				} else {
					cur = null;
					break;
				}
			}
			if (cur != null) {
				return cur;
			}
		}
		return null;
	}

	/**
	 * Works as the overload with default value, but returns null if no matchin element is found.
	 * 
	 * @param tagname
	 * @return The tag value or null.
	 */
	public String getModelXMLTagValue(String tagname) {
		return getModelXMLTagValue(tagname, null);
	}

	/**
	 * Returns the text content of a tag inside the model.xml file.
	 * 
	 * The tag given may be separated by a dot, so that a clear position can be addressed. So "description.name" will
	 * look up any tags named "description" and search therein for any tags named "name". Always the first item with a
	 * corresponding name is returned.
	 * 
	 * @param tagname
	 * The tag whos value should be returned.
	 * @param default_value
	 * The default value if no matching element is found
	 * @return The tag text content or the default value if no matching tag is found.
	 */
	public String getModelXMLTagValue(String tagname, String default_value) {
		Element res = getModelXMLElement(tagname);
		return (res != null) ? res.getTextContent() : default_value;
	}

	/**
	 * Returns the package of any java source files associated with this model. Defaults to the default package (="") if
	 * none is given.
	 * 
	 * @return
	 */
	public String getModelPackageStr() {
		String thepackage = getModelXMLTagValue("package");
		return thepackage != null ? thepackage + "." : "";
	}

	/**
	 * 
	 * @param n
	 * @param attrib_name
	 * @return Attribute value or null if not existent
	 */
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
	 * Reads the parameters from the model XML file and returns a Parameters object.
	 * 
	 * @return A Parameters object or null if the model definition does not contain parameters.
	 */
	public Parameters getParameters() {
		Element params = getModelXMLElement("parameters");
		if (params != null) {
			Parameters p = new Parameters();
			NodeList nl = params.getElementsByTagName("param");
			double[] cur = new double[nl.getLength()];
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				String name = getNodeAttributeValue(n, "name");
				// Set \mu_i if no name is given.
				if (name == null) {
					name = "\u00B5_" + i;
				}
				// Add
				p.addParam(name, Double.parseDouble(getNodeAttributeValue(n, "min")),
						Double.parseDouble(getNodeAttributeValue(n, "max")));
				// Extract default values and set as current, take min value if
				// no default is set
				String def = getNodeAttributeValue(n, "default");
				cur[i] = def != null ? Double.parseDouble(def) : p.getMinValue(i);
			}
			p.setCurrent(cur);
			return p;
		}
		return null;
	}

	/**
	 * Checks if a model.xml file exists in the specified directory and performs xsd-validation.
	 * 
	 * Note: On the current Android platform the validation using the xsd W3C Schema is somehow NOT implemented; so,
	 * validation is skipped on android platforms.
	 * 
	 * @pre dir != null
	 * 
	 * @param dir
	 * The directory to check
	 * @return True if the directory contains a valid model, false otherwise
	 */
	public boolean isValidModelDir(String dir) {
		assert dir != null;

		/*
		 * Store current model directory if set, and temporarily set the model dir to dir. This is done as subclass
		 * implementations will of course depend on getModelDir() when calling modelFileExists().
		 */
		String olddir = mdir;
		mdir = dir;
		if (!modelFileExists("model.xml"))
			return false;
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
				Log.e("AModelManager", "Invalid model.xml: " + se.getMessage(), se);
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
	 * Returns whether the specified file exists in the current model folder.
	 * 
	 * @param filename
	 * @return true if the file exists in the model, false otherwise
	 */
	public abstract boolean modelFileExists(String filename);

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
	 * Sets the specified source as current model path.
	 * 
	 * Requires the
	 * 
	 * @pre isValidModelDir(location) == true
	 * @post The model is loaded from the specified location and calls to getInStream etc. will serve files from there.
	 * 
	 * @param location
	 * The directory/path to change to
	 * @throws ModelManagerException
	 * The current model directory, path or location does not contain a valid model.
	 */
	public void useModel(String location) throws ModelManagerException {
		assert isValidModelDir(location);

		this.mdir = location;
		Log.d("ModelManager", "Loading model from " + getModelURI());
		try {
			modelxml = db.parse(getInStream("model.xml"));
		} catch (Exception e) {
			throw new ModelManagerException("Unexpected Exception in AModelManager.setModelDir: " + e.getMessage(), e);
		}

		modelxml.normalize();
		modelnode = modelxml.getElementsByTagName("model").item(0);

		String type = getModelXMLAttribute("type");
		mtype = ModelType.parse(type);
		if (mtype == ModelType.Unknown)
			throw new ModelManagerException("Unknown model type: " + type);

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
	 * Checks if a specified tag exists inside the current models model.xml file.
	 * 
	 * @param tagname
	 * The tag to check
	 * @return True if the tag exists or false otherwise
	 */
	public boolean xmlTagExists(String tagname) {
		return getModelXMLElement(tagname) != null;
	}
}