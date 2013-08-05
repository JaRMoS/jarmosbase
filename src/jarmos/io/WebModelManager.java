/**
 * 
 */
package jarmos.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A model manager reading models from a remote web location.
 * 
 * The remote root url must be given upon instantiation.
 * 
 * The getFolderList() method tries to access the file DIRLIST_FILE in that
 * location in order to retrieve the possible model folders located at the
 * remote root url.
 * 
 * @author dwirtz
 * 
 */
public class WebModelManager extends AModelManager {

	/**
	 * The file containing the model folders to consider per line.
	 */
	public static final String DIRLIST_FILE = "models.txt";

	private URL rooturl;

	/**
	 * @param rooturl
	 */
	public WebModelManager(URL rooturl) {
		super();
		this.rooturl = rooturl;
	}
	
	public WebModelManager(String url) throws MalformedURLException {
		this(new URL(url));
	}
		
//	/**
//	 * @return The root web url
//	 */
//	public String getRootURL() {
//		return rooturl;
//	}
	
	/**
	 * @see jarmos.io.AModelManager#getClassLoader()
	 */
	@Override
	public ClassLoader getClassLoader() {
		try {
			URL url = new URL(rooturl + "/" + getModelDir() + "/" + CLASSES_JARFILE);
			return new URLClassLoader(new URL[] { url }, super.getClassLoader());
		}
		catch (MalformedURLException e) {
			return super.getClassLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kermor.java.io.IModelManager#getInStream(java.lang.String)
	 */
	@Override
	protected InputStream getInStreamImpl(String filename) throws IOException {
		URL u = new URL(rooturl + "/" + getModelDir() + "/" + filename);
		return u.openStream();
	}

	@Override
	protected String[] getFolderList() throws IOException {
		URL u = new URL(rooturl + "/" + DIRLIST_FILE);
		Scanner s = new Scanner(u.openStream());
		List<String> folders = new ArrayList<String>();
		while (s.hasNextLine()) {
			folders.add(s.nextLine());
		}
		s.close();
		if (folders.size() > 0) {
			return folders.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	/**
	 * @see jarmos.io.AModelManager#modelFileExists(java.lang.String)
	 */
	@Override
	public boolean modelFileExists(String filename) {
		try {
			getInStreamImpl(filename).close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public URI getModelURI() {
		return URI.create(rooturl.toString());
	}

	@Override
	protected String getLoadingMessage() {
		return "Reading remote models";
	}

}
