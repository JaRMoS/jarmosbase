/**
 * 
 */
package rmcommon.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.math.linear.MatrixIndexException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

import rmcommon.MathFactory;

/**
 * 
 * Reading matrices and vectors with a bunch of convenient overloads for
 * different sources and output formats.
 * 
 * All methods operate on binary files, which can be written using either the
 * BIG ENDIAN machine format, (Java natively supports this format in the
 * DataInputStream class) or the LITTLE ENDIAN machine format. The property
 * MachineFormat determines which format is to be assumed when reading the
 * binary file.
 * 
 * When matrices or vectors are read by methods NOT taking the dimensions
 * explicitly, the MathObjectReader will read the first (two) 32-bit integer
 * value(s) as the dimensions of the vector (matrix).
 * 
 * @author Daniel Wirtz
 * 
 */
public class MathObjectReader {

	/**
	 * Enum for both known machine formats.
	 * 
	 * @author Daniel Wirtz
	 * 
	 */
	public enum MachineFormats {
		BigEndian, LittleEndian;
	}

	/**
	 * @author Daniel Wirtz
	 * @date Aug 10, 2011
	 * 
	 */
	public class MathReaderException extends Exception {

		private static final long serialVersionUID = -3505742802789851382L;

		/**
		 * @param msg
		 * @param inner
		 */
		public MathReaderException(String msg, Exception inner) {
			super(msg, inner);
		}
	}

	/**
	 * Determines which machine format to use.
	 * 
	 * Default: MachineFormats.BigEndian (native JVM format)
	 */
	public MachineFormats MachineFormat = MachineFormats.BigEndian;

	/**
	 * Returns the DataInput depending on the chosen machine format.
	 * 
	 * @param in
	 * @return
	 */
	private DataInput getDataInput(InputStream in) {
		switch (MachineFormat) {
		case BigEndian:
			return new DataInputStream(in);
		case LittleEndian:
			return new LittleEndianDataInput(in);
		default:
			return null;
		}
	}

	/**
	 * Reads a matrix from an InputStream, pointing to a binary file.
	 * 
	 * Closes the input stream after reading.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public RealMatrix readMatrix(InputStream in) throws IOException {
		return MathFactory.createRealMatrix(readRawDoubleMatrix(in));
	}

	/**
	 * Reads a matrix from a given binary file in the file system (accessible
	 * via java.io, i.e. FileStream) including its dimensions.
	 * 
	 * @param file
	 *            Path to a binary file
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public RealMatrix readMatrix(String file) throws IOException,
			FileNotFoundException {
		return readMatrix(new FileInputStream(file));
	}

	private double[][] readRawDoubleMatrix(DataInput rd, int rows, int cols)
			throws MatrixIndexException, IOException {
		double[][] res = new double[rows][];
		for (int i = 0; i < rows; i++) {
			res[i] = readRawDoubleVector(rd, cols);
		}
		return res;
	}

	/**
	 * Reads a real matrix as double[][] array from a given binary input stream,
	 * including dimension detection.
	 * 
	 * Closes the stream after reading.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public double[][] readRawDoubleMatrix(InputStream in) throws IOException {
		int rows = -1;
		int cols = -1;
		double[][] res = null;
		try {
			DataInput di = getDataInput(in);
			rows = di.readInt();
			cols = di.readInt();
			res = readRawDoubleMatrix(di, rows, cols);
		} finally {
			in.close();
		}
		return res;
	}

	/**
	 * Reads a double matrix of size rows*cols from the input stream.
	 * 
	 * Does NOT close the stream after reading to enable reading of more data
	 * from it.
	 * 
	 * @param in
	 * @param rows
	 * @param cols
	 * @return
	 * @throws IOException
	 * @deprecated Use binary files that contain the matrix size in first 8
	 *             bytes.
	 */
	public double[][] readRawDoubleMatrix(InputStream in, int rows, int cols)
			throws IOException {
		return readRawDoubleMatrix(getDataInput(in), rows, cols);
	}

	private double[] readRawDoubleVector(DataInput rd, int size)
			throws MatrixIndexException, IOException {
		double[] res = new double[size];
		for (int i = 0; i < size; i++) {
			res[i] = rd.readDouble();
		}
		return res;
	}

	/**
	 * Reads a real vector from a binary input stream, including dimension
	 * detection.
	 * 
	 * Closes the stream after reading.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public double[] readRawDoubleVector(InputStream in) throws IOException {
		double[] res = null;
		try {
			DataInput di = getDataInput(in);
			res = readRawDoubleVector(di, di.readInt());
		} finally {
			in.close();
		}
		return res;
	}

	private float[] readRawFloatVector(DataInput di, int size) throws IOException {
		float[] res = new float[size];
		for (int i = 0; i < size; i++) {
			res[i] = di.readFloat();
		}
		return res;
	}
	
	public float[] readRawFloatVector(InputStream in) throws IOException {
		float[] res = null;
		try {
			DataInput di = getDataInput(in);
			res = readRawFloatVector(in, di.readInt());
		} finally {
			in.close();
		}
		return res;
	}

	/**
	 * This method exists due to compatibility with the old rbappmit models.
	 * 
	 * Does NOT close the stream after reading to allow reading of more data.
	 * 
	 * @param in
	 * @param size
	 * @return
	 * @throws IOException
	 * @deprecated Binary files should include the array size for themselves (in
	 *             first bytes)
	 */
	public float[] readRawFloatVector(InputStream in, int size) throws IOException {
		return readRawFloatVector(getDataInput(in), size);
	}
	
	/**
	 * Reads a real vector including dimension from a given input stream which
	 * points to a binary file.
	 * 
	 * Closes the input stream after reading.
	 * 
	 * @param in
	 *            InputStream of a binary file
	 * @return
	 * @throws IOException
	 */
	public RealVector readVector(InputStream in) throws IOException {
		return MathFactory.createRealVector(readRawDoubleVector(in));
	}
	
	/**
	 * Reads a short array/vector from the given input stream, autodetecting its size from the first bytes read as (4byte-)integer.
	 * 
	 * Closes the stream after reading.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public short[] readRawShortVector(InputStream in) throws IOException {
		short[] res = null;
		try {
			DataInput di = getDataInput(in);
			res = readRawShortVector(di, di.readInt());
		} finally {
			in.close();
		}
		return res;
	}
	
	private short[] readRawShortVector(DataInput di, int size) throws IOException {
		short[] res = new short[size];
		for (int i = 0; i < size; i++) {
			res[i] = di.readShort();
		}
		return res;
	}
	
	/**
	 * Reads a real vector including dimension from a given binary file.
	 * 
	 * @param filename
	 *            Name of a binary file
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public RealVector readVector(String filename) throws FileNotFoundException,
			IOException {
		return readVector(new FileInputStream(filename));
	}

}
