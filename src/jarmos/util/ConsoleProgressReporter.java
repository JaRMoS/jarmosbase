/**
 * 
 */
package jarmos.util;

/**
 * @author CreaByte
 * 
 */
public class ConsoleProgressReporter implements IProgressReporter {

	private int total = -1;
	private String msg = "ConsoleProgressReporter";

	/*
	 * (non-Javadoc)
	 * 
	 * @see jarmos.util.IProgressReporter#setMessage(java.lang.String)
	 */
	@Override
	public void setMessage(String msg) {
		this.msg = msg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jarmos.util.IProgressReporter#progress(int)
	 */
	@Override
	public void progress(int value) {
		System.out.println(msg + ": " + value + "/" + total);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jarmos.util.IProgressReporter#init(int)
	 */
	@Override
	public void init(String title, int total) {
		System.out.println("New console progress: " + title);
		this.total = total;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jarmos.util.IProgressReporter#finish()
	 */
	@Override
	public void finish() {
		total = 0;
		msg = "";
	}

}
