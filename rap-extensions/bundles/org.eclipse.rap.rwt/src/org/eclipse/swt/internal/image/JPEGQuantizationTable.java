/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.image;


final class JPEGQuantizationTable extends JPEGVariableSizeSegment {
	public static byte[] DefaultLuminanceQTable = {
		(byte)255, (byte)219, 0, 67, 0,
		16, 11, 10, 16, 24, 40, 51, 61,
		12, 12, 14, 19, 26, 58, 60, 55,
		14, 13, 16, 24, 40, 57, 69, 56,
		14, 17, 22, 29, 51, 87, 80, 62,
		18, 22, 37, 56, 68, 109, 103, 77,
		24, 35, 55, 64, 81, 104, 113, 92,
		49, 64, 78, 87, 103, 121, 120, 101,
		72, 92, 95, 98, 112, 100, 103, 99
	};
	public static byte[] DefaultChrominanceQTable = {
		(byte)255, (byte)219, 0, 67, 1,
		17, 18, 24, 47, 99, 99, 99, 99,
		18, 21, 26, 66, 99, 99, 99, 99,
		24, 26, 56, 99, 99, 99, 99, 99,
		47, 66, 99, 99, 99, 99, 99, 99,
		99, 99, 99, 99, 99, 99, 99, 99,
		99, 99, 99, 99, 99, 99, 99, 99,
		99, 99, 99, 99, 99, 99, 99, 99,
		99, 99, 99, 99, 99, 99, 99, 99
	};
	
public JPEGQuantizationTable(byte[] reference) {
	super(reference);
}

public JPEGQuantizationTable(LEDataInputStream byteStream) {
	super(byteStream);
}

public static JPEGQuantizationTable defaultChrominanceTable() {
	byte[] data = new byte[DefaultChrominanceQTable.length];
	System.arraycopy(DefaultChrominanceQTable, 0, data, 0, data.length);
	return new JPEGQuantizationTable(data);
}

public static JPEGQuantizationTable defaultLuminanceTable() {
	byte[] data = new byte[DefaultLuminanceQTable.length];
	System.arraycopy(DefaultLuminanceQTable, 0, data, 0, data.length);
	return new JPEGQuantizationTable(data);
}

public int[] getQuantizationTablesKeys() {
	int[] keys = new int[4];
	int keysIndex = 0;
	int totalLength = getSegmentLength() - 2;
	int ofs = 4;
	while (totalLength > 64) {
		int tq = reference[ofs] & 0xF;
		int pq = (reference[ofs] & 0xFF) >> 4;
		if (pq == 0) {
			ofs += 65;
			totalLength -= 65;
		} else {
			ofs += 129;
			totalLength -= 129;
		}
		if (keysIndex >= keys.length) {
			int[] newKeys = new int[keys.length + 4];
			System.arraycopy(keys, 0, newKeys, 0, keys.length);
			keys = newKeys;
		}
		keys[keysIndex] = tq;
		keysIndex++;
	}
	int[] newKeys = new int[keysIndex];
	System.arraycopy(keys, 0, newKeys, 0, keysIndex);
	return newKeys;
}

public int[][] getQuantizationTablesValues() {
	int[][] values = new int[4][];
	int valuesIndex = 0;
	int totalLength = getSegmentLength() - 2;
	int ofs = 4;
	while (totalLength > 64) {
		int[] qk = new int[64];
		int pq = (reference[ofs] & 0xFF) >> 4;
		if (pq == 0) {
			for (int i = 0; i < qk.length; i++) {
				qk[i] = reference[ofs + i + 1] & 0xFF;
			}
			ofs += 65;
			totalLength -= 65;
		} else {
			for (int i = 0; i < qk.length; i++) {
				int idx = (i - 1) * 2 ;
				qk[i] = (reference[ofs + idx + 1] & 0xFF) * 256 + (reference[ofs + idx + 2] & 0xFF);
			}
			ofs += 129;
			totalLength -= 129;
		}
		if (valuesIndex >= values.length) {
			int[][] newValues = new int[values.length + 4][];
			System.arraycopy(values, 0, newValues, 0, values.length);
			values = newValues;
		}
		values[valuesIndex] = qk;
		valuesIndex++;
	}
	int[][] newValues = new int[valuesIndex][];
	System.arraycopy(values, 0, newValues, 0, valuesIndex);
	return newValues;
}

public void scaleBy(int qualityFactor) {
	int qFactor = qualityFactor;
	if (qFactor <= 0) {
		qFactor = 1;
	}
	if (qFactor > 100) {
		qFactor = 100;
	}
	if (qFactor < 50) {
		qFactor = 5000 / qFactor;
	} else {
		qFactor = 200 - (qFactor * 2);
	}
	int totalLength = getSegmentLength() - 2;
	int ofs = 4;
	while (totalLength > 64) {
//		int tq = reference[ofs] & 0xFF;
		int pq = (reference[ofs] & 0xFF) >> 4;
		if (pq == 0) {
			for (int i = ofs + 1; i <= ofs + 64; i++) {
				int temp = ((reference[i] & 0xFF) * qFactor + 50) / 100;
				if (temp <= 0) temp = 1;
				if (temp > 255) temp = 255;
				reference[i] = (byte)temp;
			}
			ofs += 65;
			totalLength -= 65;
		} else {
			for (int i = ofs + 1; i <= ofs + 128; i += 2) {
				int temp = (((reference[i] & 0xFF) * 256 + (reference[i + 1] & 0xFF)) * qFactor + 50) / 100;
				if (temp <= 0) temp = 1;
				if (temp > 32767) temp = 32767;
				reference[i] = (byte)(temp >> 8);
				reference[i + 1] = (byte)(temp & 0xFF);
			}
			ofs += 129;
			totalLength -= 129;
		}
	}
}

@Override
public int signature() {
	return JPEGFileFormat.DQT;
}
}
