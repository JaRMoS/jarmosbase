/**
 * 
 */
package rmcommon.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
	
	public FileModelManager(String root) {
		super();
		this.root = root;
	}

	/* (non-Javadoc)
	 * @see kermor.java.io.IModelManager#getInStream(java.lang.String)
	 */
	@Override
	public InputStream getInStreamImpl(String filename) throws FileNotFoundException {
		String file = root + File.separator + getModelDir() + File.separator + filename;
		return new FileInputStream(file);
	}

	@Override
	public String[] getFolderList() throws IOException {
//		return new File(root + File.separator + getModelDir()).list();
		return new File(root).list();
	}

	@Override
	public String getInfoFileURL() {
		return "file://" + root + "/" + getModelDir() + "/" + info_filename;
	}

	@Override
	public boolean modelFileExists(String filename) {
		return new File(root + File.separator + getModelDir() + File.separator + filename).exists();
	}

}
