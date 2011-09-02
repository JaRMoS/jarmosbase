/**
 * 
 */
package rmcommon.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import rmcommon.IMessageHandler;
import rmcommon.ModelDescriptor;
import rmcommon.ModelType;
import rmcommon.Parameters;

/**
 * 
 * TODO: Delete half-downloaded models if loading fails! 
 * @author dwirtz
 * 
 */
public class CachingModelManager extends AModelManager {

	private FileModelManager dest;

	private boolean overwriteFlag;

	private AModelManager source;

	/**
	 * Creates a new caching model manager with overwriteFlag set to false.
	 * @param source
	 * @param dest
	 */
	public CachingModelManager(AModelManager source, FileModelManager dest) {
		this(source, dest, false);
	}
	
	/**
	 * Creates a new caching model manager.
	 * @param source The source model manager
	 * @param dest The target model manager
	 * @param overwriteFlag Sets if existent model files should be overwritten or not
	 */
	public CachingModelManager(AModelManager source, FileModelManager dest, boolean overwriteFlag) {
		this.source = source;
		this.dest = dest;
		this.overwriteFlag = overwriteFlag;
	}

	@Override
	public void addMessageHandler(IMessageHandler h) {
		dest.addMessageHandler(h);
	}

	private void cacheFile(String filename) throws IOException {
		if (overwriteFlag || !dest.modelFileExists(filename)) {
			dest.writeModelFile(filename, source.getInStream(filename));
		}
	}

	private void cacheModelFiles() throws IOException, ModelManagerException {
		// Cache the model info file.
		// Existence of the tag guarantess the existence of the info html file. 
		if (source.xmlTagExists("model.description.infohtml")) {
			String infohtml = source
					.getModelXMLTagValue("model.description.infohtml");
			if (!source.modelFileExists(infohtml)) {
				throw new ModelManagerException(
						"Inconsistent model state: XML tag for infohtml exists but the file "+infohtml+" could not be found in the model directory.");
			}
			cacheFile(infohtml);
		}
		if (source.xmlTagExists("model.description.image")) {
			String image = source
					.getModelXMLTagValue("model.description.image");
			if (!source.modelFileExists(image)) {
				throw new ModelManagerException(
						"Inconsistent model state: XML tag for a model image exists but the file "+image+" could not be found in the model directory.");
			}
			cacheFile(image);
		}
		cacheFile("dexclasses.jar");
		cacheFile("classes.jar");
	}

	@Override
	public ClassLoader getClassLoader() {
		return dest.getClassLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmcommon.io.AModelManager#getFolderList()
	 */
	@Override
	protected String[] getFolderList() throws IOException {
		return source.getFolderList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmcommon.io.AModelManager#getInStreamImpl(java.lang.String)
	 */
	@Override
	protected InputStream getInStreamImpl(String filename) throws IOException {
		// Write file to the destination if not already exists
		if (overwriteFlag || !dest.modelFileExists(filename)) {
			dest.writeModelFile(filename, source.getInStream(filename));
		}
		// Return stream from destination
		return dest.getInStream(filename);
	}

	@Override
	public MathObjectReader getMathObjReader() {
		return dest.getMathObjReader();
	}

	@Override
	public List<ModelDescriptor> getModelDescriptors()
			throws ModelManagerException {
		// Only the source knows all models..
		return source.getModelDescriptors();
	}

	@Override
	public String getModelDir() {
		return dest.getModelDir();
	}

	@Override
	public ModelType getModelType() {
		return dest.getModelType();
	}

	@Override
	public URI getModelURI() {
		return dest.getModelURI();
	}

	@Override
	public String getModelXMLAttribute(String attrib_name) {
		return dest.getModelXMLAttribute(attrib_name);
	}

	@Override
	public String getModelXMLAttribute(String attrib_name, String tagname) {
		return dest.getModelXMLAttribute(attrib_name, tagname);
	}

	@Override
	public String getModelXMLTagValue(String tagname) {
		return dest.getModelXMLTagValue(tagname);
	}

	@Override
	public String getModelXMLTagValue(String tagname, String default_value) {
		return dest.getModelXMLTagValue(tagname, default_value);
	}

	@Override
	public Parameters getParameters() {
		return dest.getParameters();
	}

	@Override
	public boolean isValidModelDir(String dir) {
		// Have to check at the source
		return source.isValidModelDir(dir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rmcommon.io.AModelManager#modelFileExists(java.lang.String)
	 */
	@Override
	public boolean modelFileExists(String filename) {
		return dest.modelFileExists(filename)
				|| source.modelFileExists(filename);
	}
	
	@Override
	public void removeMessageHandler(IMessageHandler h) {
		dest.removeMessageHandler(h);
	}
	
	@Override
	protected void sendMessage(String msg) {
		dest.sendMessage(msg);
	}
	

	@Override
	public void setModelDir(String dir) throws ModelManagerException {
		source.setModelDir(dir);
		File destdir = new File(dest.getRoot() + File.separator + dir);
		if (!destdir.isDirectory()) {
			if (!destdir.mkdir())
				throw new ModelManagerException("Could not create directory "
						+ dir + " in " + dest.getRoot());
		}
		File modelxml = new File(destdir, "model.xml");
		if (overwriteFlag || !modelxml.exists()) {
			try {
				FileOutputStream out = new FileOutputStream(modelxml);
				byte[] buffer = new byte[1024];
				int bytes_read = 0;
				InputStream in = source.getInStream("model.xml");
				while ((bytes_read = in.read(buffer)) > 0) {
					out.write(buffer, 0, bytes_read);
				}
				in.close();
			} catch (IOException e) {
				throw new ModelManagerException("Error caching model.xml", e);
			}
		}
		dest.setModelDir(dir);
		try {
			cacheModelFiles();
		} catch (IOException e) {
			throw new ModelManagerException("Error caching model files.",e);
		}
	}
	
	public boolean deleteCachedFiles() {
		return dest.clearCurrentModel();
	}

	@Override
	public boolean xmlTagExists(String tagname) {
		return dest.xmlTagExists(tagname);
	}

}
