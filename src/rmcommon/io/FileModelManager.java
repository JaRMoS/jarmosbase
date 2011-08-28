/**
 * 
 */
package rmcommon.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 
 * Manages models loaded from the file system available via the java.io classes.
 * 
 * Takes an initial model root directory upon construction.
 * 
 * @author Daniel Wirtz
 * 
 */
public class FileModelManager extends AModelManager {

	private String root;

	/**
	 * @param root
	 */
	public FileModelManager(String root) {
		super();
		if (!new File(root).exists()) {
			throw new IllegalArgumentException("Directory does not exist: '"+root+"'");
		}
		this.root = root;
	}

	/**
	 * @see rmcommon.io.AModelManager#getClassLoader()
	 */
	@Override
	public ClassLoader getClassLoader() {
		try {
			URL url = new File(root + "/" + getModelDir() + "/"
					+ CLASSES_JARFILE).toURI().toURL();
			return new URLClassLoader(new URL[] { url }, super.getClassLoader());
		}
		catch (MalformedURLException e) {
			throw new RuntimeException("Creating a file with path '" + root
					+ "/" + getModelDir() + "/" + CLASSES_JARFILE
					+ "' caused a MalformedURLException.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kermor.java.io.IModelManager#getInStream(java.lang.String)
	 */
	@Override
	protected InputStream getInStreamImpl(String filename)
			throws FileNotFoundException {
		return new FileInputStream(getFullModelPath() + filename);
	}

	/**
	 * @see rmcommon.io.AModelManager#getFolderList()
	 */
	@Override
	protected String[] getFolderList() throws IOException {
		// return new File(root + File.separator + getModelDir()).list();
		return new File(root).list();
	}

	/**
	 * @see rmcommon.io.AModelManager#modelFileExists(java.lang.String)
	 */
	@Override
	public boolean modelFileExists(String filename) {
		String file = root + File.separator + getModelDir() + File.separator
				+ filename;
		return new File(file).exists();
	}

	protected String getFullModelPath() {
		return root + File.separator + getModelDir() + File.separator;
	}

}
