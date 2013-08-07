package jarmos.util;

/**
 * Simple interface for progress reporting.
 * 
 * See subclasses for implementation details and applications.
 * 
 * @author Daniel Wirtz
 * 
 */
public interface IProgressReporter {
	/**
	 * 
	 * @param msg
	 */
	public void setMessage(String msg);

	public void progress(int value);

	public void init(String title, int total);

	public void finish();
}
