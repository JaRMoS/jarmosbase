package rmcommon;

/**
 * @author Daniel Wirtz
 * @date Aug 23, 2011
 * 
 *       Fake Android Log class as the rbappmit code is packed with debug
 *       notices.
 * 
 */
public class Log {

	/**
	 * @param ID
	 * @param msg
	 */
	public static void d(String ID, String msg) {
		System.out.println(ID + ": " + msg);
	}

	/**
	 * @param ID
	 * @param msg
	 */
	public static void e(String ID, String msg) {
		System.out.println("ERROR in " + ID + ":" + msg);
	}
	
	/**
	 * @param ID
	 * @param msg
	 */
	public static void w(String ID, String msg) {
		System.out.println("WARNING in " + ID + ":" + msg);
	}

	/**
	 * @param debugTag
	 * @param string
	 * @param e
	 */
	public static void e(String debugTag, String string, Exception e) {
		e(debugTag, string + ", Exception: " + e.getMessage());
		e.printStackTrace();
	}

}
