/**
 * 
 */
package jarmos.util;

/**
 * @author CreaByte
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
