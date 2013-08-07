package jarmos;

import jarmos.geometry.FieldMapping;

/**
 * @short Contains information about the logical solution fields.
 * 
 * Those information comprises of the SolutionFieldType (real / complex / displacement), the geometrical
 * geometry.FieldMapping for allocation of values to vertices or elements and a descriptive #toString method.
 * 
 * @author Daniel Wirtz
 * 
 */
public class FieldDescriptor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FieldDescriptor " + Name + " (Type " + Type + ", mapping " + Mapping + ")";
	}

	private static int fcnt = 0, dcnt = 0;

	public SolutionFieldType Type = SolutionFieldType.RealValue;

	public String Name;

	/**
	 * Unused so far, as mixed field mappings are not yet implemented.
	 */
	public FieldMapping Mapping = FieldMapping.UNKNOWN;

	public FieldDescriptor(SolutionFieldType type) {
		Type = type;
		Name = "Solution field " + ++fcnt + ": " + type;
	}

	public static FieldDescriptor getDefault() {
		FieldDescriptor f = new FieldDescriptor(SolutionFieldType.RealValue);
		f.Mapping = FieldMapping.VERTEX;
		f.Name = "Default real valued field " + ++dcnt;
		return f;
	}

}
