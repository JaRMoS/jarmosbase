/**
 * 
 */
package rmcommon.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
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
		File r = new File(root);
		if (!r.exists()) {
			throw new IllegalArgumentException("Directory does not exist: '"
					+ root + "'");
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
		} catch (MalformedURLException e) {
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
		return new File(root + File.separator + getModelDir() + File.separator
				+ filename).exists();
	}

	protected String getFullModelPath() {
		return root + File.separator + getModelDir() + File.separator;
	}

	/**
	 * Writes the given inputstream to the file specified by filename to the
	 * current model directory.
	 * 
	 * @param filename
	 *            The file name
	 * @param in
	 *            The inputstream to read
	 * @throws IOException
	 */
	public void writeModelFile(String filename, InputStream in)
			throws IOException {
		File file = new File(getFullModelPath() + filename);
		// Delete if file exists
		if (file.exists())
			file.delete();
		// Create new
		FileOutputStream out = new FileOutputStream(file);
		byte[] buffer = new byte[1024];
		int bytes_read = 0;
		while ((bytes_read = in.read(buffer)) > 0) {
			out.write(buffer, 0, bytes_read);
		}
		in.close();
	}
	
	/**
	 * Removes  
	 * @param dirname
	 */
	public boolean clearCurrentModel() {
		return deleteDir(new File(getFullModelPath()));
	}
	
	private boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	    // The directory is now empty so delete it
	    return dir.delete();
	}


	/**
	 * Returns the root folder for the model directories
	 * 
	 * @return
	 */
	public String getRoot() {
		return root;
	}

	@Override
	public URI getModelURI() {
		return URI.create("file://" + root + "/" + getModelDir());
	}

}
