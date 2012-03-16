package jarmos;

import java.util.Arrays;

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
	
	public static String subArr(float[] arr, int N) {
		float[] tmp = new float[N];
		System.arraycopy(arr, 0, tmp, 0, N);
		return Arrays.toString(tmp);
	}
	
	public static String dumpArr(float[][] arr) {
		String res = "";
		for (int i=0;i<arr.length;i++) {
			res += i+": "+Arrays.toString(arr[i])+", ";
		}
		return res;
	}

}
