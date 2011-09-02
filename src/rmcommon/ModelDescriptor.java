/**
 * 
 */
package rmcommon;

import java.io.InputStream;
import java.util.Date;

import rmcommon.io.AModelManager;


/**
 * Represents a short description of a model managed by a model manager.
 * 
 * So far this class is used to fill the grid view items in the KerMORDSApp, and it might be subject to change
 * if more applications (webservice / webtool) come into play.
 * 
 * @author dwirtz
 * 
 */
public class ModelDescriptor {

	/***
	 * The model directory inside the current model managers model root source.
	 * @see AModelManager for more information.
	 */
	public String modeldir;
	
	/**
	 * The model's title
	 */
	public String title;
	
	/**
	 * The model type as string. (JKerMor / rbmappmit etc)
	 */
	public ModelType type;
	
	/**
	 * The model creation date
	 */
	public Date created;
	
	/**
	 * A VERY short description of the model.
	 */
	public String shortDescription = null;
	
	/**
	 * An input stream pointing to an available model image, if given. Otherwise null.
	 */
	public InputStream image;

	public ModelDescriptor(String mdir, String t, ModelType type, InputStream i, Date created) {
		modeldir = mdir;
		title = t;
		image = i;
		this.type = type;
		this.created = created;
	}
}
