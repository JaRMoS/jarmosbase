//    rbAPPmit: An Android front-end for the Certified Reduced Basis Method
//    Copyright (C) 2010 David J. Knezevic and Phuong Huynh
//
//    This file is part of rbAPPmit
//
//    rbAPPmit is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    rbAPPmit is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with rbAPPmit.  If not, see <http://www.gnu.org/licenses/>. 

package rmcommon.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/* source: http://www.captain.at/howto-java-convert-binary-data.php */

/**
 * Wraps the old BinaryReader into a Data
 * 
 * @author CreaByte
 * 
 */
public class LittleEndianDataInput implements DataInput {

	DataInputStream in;

	/**
	 * Takes an InputStream instance pointing to a binary file.
	 * 
	 * @param in
	 *            The inputstream to read the values from
	 */
	public LittleEndianDataInput(InputStream in) {
		this.in = new DataInputStream(in);
	}

	@Override
	protected void finalize() {
		try {
			this.in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Read a float array
	/**
	 * @param _size
	 * @return
	 */
	public float[] readFloat(int _size) throws IOException {
		float[] ofloat = new float[_size];
		int i = 0;
		byte[] tmp = new byte[4 * _size];
		for (i = 0; i < 4 * _size; i++)
			tmp[i] = in.readByte();
		int accum;
		for (int count = 0; count < _size; count++) {
			accum = 0;
			i = 0;
			for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
				accum |= ((long) (tmp[i + count * 4] & 0xff)) << shiftBy;
				i++;
			}
			ofloat[count] = Float.intBitsToFloat(accum);
		}
		return ofloat;
	}

	/**
	 * @see java.io.DataInput#readFully(byte[])
	 */
	@Override
	public void readFully(byte[] b) throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new RuntimeException("Not implemented.");

	}

	/**
	 * @see java.io.DataInput#skipBytes(int)
	 */
	@Override
	public int skipBytes(int n) throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readBoolean()
	 */
	@Override
	public boolean readBoolean() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readByte()
	 */
	@Override
	public byte readByte() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	@Override
	public int readUnsignedByte() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readShort()
	 */
	@Override
	public short readShort() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	@Override
	public int readUnsignedShort() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readChar()
	 */
	@Override
	public char readChar() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readInt()
	 */
	@Override
	public int readInt() throws IOException {
		int i = 0;
		byte[] tmp = new byte[2];
		for (i = 0; i < 2; i++)
			tmp[i] = in.readByte();
		int low = tmp[0] & 0xff;
		int high = tmp[1] & 0xff;
		return (int) (high << 8 | low);
	}

	/**
	 * @see java.io.DataInput#readLong()
	 */
	@Override
	public long readLong() throws IOException {
		int i = 0;
		byte[] tmp = new byte[4];
		for (i = 0; i < 4; i++)
			tmp[i] = in.readByte();
		long accum = 0;
		i = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return accum;
	}

	/**
	 * @see java.io.DataInput#readFloat()
	 */
	@Override
	public float readFloat() throws IOException {
		int i = 0;
		byte[] tmp = new byte[4];
		for (i = 0; i < 4; i++)
			tmp[i] = in.readByte();
		int accum = 0;
		i = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return Float.intBitsToFloat(accum);
	}

	/**
	 * @see java.io.DataInput#readDouble()
	 */
	@Override
	public double readDouble() throws IOException {
		int i = 0;
		byte[] tmp = new byte[8];
		for (i = 0; i < 8; i++)
			tmp[i] = in.readByte();
		long accum = 0;
		i = 0;
		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}

	/**
	 * @see java.io.DataInput#readLine()
	 */
	@Override
	public String readLine() throws IOException {
		throw new RuntimeException("Not implemented.");
	}

	/**
	 * @see java.io.DataInput#readUTF()
	 */
	@Override
	public String readUTF() throws IOException {
		throw new RuntimeException("Not implemented.");
	}
}
