package jarmos;

import jarmos.geometry.GeometryData;
import jarmos.io.AModelManager;
import jarmos.io.AModelManager.ModelManagerException;
import jarmos.io.MathObjectReader.MathReaderException;

import java.io.IOException;

/**
 * @short Base class for all JaRMoSBase models.
 * 
 * Contains geometry data, the total number of DoF and methods to load the offline data.
 * 
 * @author Daniel Wirtz
 * 
 */
public abstract class ModelBase {

	private GeometryData geoData = null;

	/**
	 * The logical output fields of the model, each collecting one ore more model DoF's into a related unit, like
	 * displacements which have 2-3 DoF-fields (x,y,z)
	 */
	protected FieldDescriptor[] logicalFieldTypes;

	private int numDoFfields;

	/**
	 * Loads the model's offline data.
	 * 
	 * Override in subclasses for model-specific offline data loading
	 * 
	 * @param m
	 * The model manager
	 * @throws IOException
	 */
	public void loadOfflineData(AModelManager m) throws MathReaderException, ModelManagerException, IOException {
		/*
		 * Load geometry
		 */
		geoData = new GeometryData();
		geoData.loadModelGeometry(m);

		/*
		 * Read number of DoF-fields
		 */
		String dofs = m.getModelXMLTagValue("numDoFfields");
		numDoFfields = Integer.parseInt(dofs);

		/*
		 * Read additional visual field type definitions
		 */
		logicalFieldTypes = m.getModelFieldTypes();
		if (logicalFieldTypes == null) {
			Log.d("RBSystem", "No visual field type definitions found. Falling back to " + numDoFfields
					+ " default RealValue fields.");
			logicalFieldTypes = new FieldDescriptor[numDoFfields];
			for (int i = 0; i < numDoFfields; i++) {
				logicalFieldTypes[i] = FieldDescriptor.getDefault();
			}
		}
	}

	/**
	 * The model's geometry data
	 */
	public GeometryData getGeometry() {
		return geoData;
	}

	/**
	 * Returns the number of degree-of-freedom fields generated/computed by the model
	 * 
	 * @return
	 */
	public int getNumDoFFields() {
		return numDoFfields;
	}
}
