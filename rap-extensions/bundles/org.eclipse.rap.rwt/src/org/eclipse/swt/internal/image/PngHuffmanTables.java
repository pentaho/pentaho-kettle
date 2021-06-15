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

import java.io.*;

public class PngHuffmanTables {
	PngHuffmanTable literalTable;
	PngHuffmanTable distanceTable;
	
	static PngHuffmanTable FixedLiteralTable;
	static PngHuffmanTable FixedDistanceTable;
	
	static final int LiteralTableSize = 288;
	static final int[] FixedLiteralLengths = {
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8,
	};

	static final int DistanceTableSize = 32;
	static final int[] FixedDistanceLengths = {
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
	};
	
	static final int LengthCodeTableSize = 19;
	static final int[] LengthCodeOrder = {
		16, 17, 18, 0, 8, 7, 9, 6, 10, 5,
		11, 4, 12, 3, 13, 2, 14, 1, 15
	};
	
static PngHuffmanTables getDynamicTables(PngDecodingDataStream stream) throws IOException {
	return new PngHuffmanTables(stream);
}
static PngHuffmanTables getFixedTables() {
	return new PngHuffmanTables();
}

private PngHuffmanTable getFixedLiteralTable() {
	if (FixedLiteralTable == null) {
		FixedLiteralTable = new PngHuffmanTable(FixedLiteralLengths);
	}
	return FixedLiteralTable;
}

private PngHuffmanTable getFixedDistanceTable() {
	if (FixedDistanceTable == null) {
		FixedDistanceTable = new PngHuffmanTable(FixedDistanceLengths);
	}
	return FixedDistanceTable;
}

private PngHuffmanTables () {
	literalTable = getFixedLiteralTable();
	distanceTable = getFixedDistanceTable();
}

private PngHuffmanTables (PngDecodingDataStream stream) throws IOException {
	int literals = PngLzBlockReader.FIRST_LENGTH_CODE 
		+ stream.getNextIdatBits(5);
	int distances = PngLzBlockReader.FIRST_DISTANCE_CODE 
		+ stream.getNextIdatBits(5);
	int codeLengthCodes = PngLzBlockReader.FIRST_CODE_LENGTH_CODE 
		+ stream.getNextIdatBits(4);

	if (codeLengthCodes > PngLzBlockReader.LAST_CODE_LENGTH_CODE) {
		stream.error();
	}
	
	/* Tricky, tricky, tricky. The length codes are stored in
	 * a very odd order. (For the order, see the definition of
	 * the static field lengthCodeOrder.) Also, the data may 
	 * not contain values for all the codes. It may just contain 
	 * values for the first X number of codes. The table should
	 * be of size <LengthCodeTableSize> regardless of the number
	 * of values actually given in the table.
	 */	
	int[] lengthCodes = new int[LengthCodeTableSize];
	for (int i = 0; i < codeLengthCodes; i++) {
		lengthCodes[LengthCodeOrder[i]] = stream.getNextIdatBits(3);
	}
	PngHuffmanTable codeLengthsTable = new PngHuffmanTable(lengthCodes);
	
	int[] literalLengths = readLengths(
		stream, literals, codeLengthsTable, LiteralTableSize);
	int[] distanceLengths = readLengths(
		stream, distances, codeLengthsTable, DistanceTableSize);
	
	literalTable = new PngHuffmanTable(literalLengths);
	distanceTable = new PngHuffmanTable(distanceLengths);
}

private int [] readLengths (PngDecodingDataStream stream, 
	int numLengths, 
	PngHuffmanTable lengthsTable,
	int tableSize) throws IOException
{
	int[] lengths = new int[tableSize];
	
	for (int index = 0; index < numLengths;) {
		int value = lengthsTable.getNextValue(stream);
		if (value < 16) {
			// Literal value
			lengths[index] = value;
			index++;
		} else if (value == 16) {
			// Repeat the previous code 3-6 times.
			int count = stream.getNextIdatBits(2) + 3;
			for (int i = 0; i < count; i++) {
				lengths[index] = lengths [index - 1];
				index++;
			}
		} else if (value == 17) {
			// Repeat 0 3-10 times.
			int count = stream.getNextIdatBits(3) + 3;
			for (int i = 0; i < count; i++) {
				lengths[index] = 0;
				index++;
			}
		} else if (value == 18) {
			// Repeat 0 11-138 times.
			int count = stream.getNextIdatBits(7) + 11;
			for (int i = 0; i < count; i++) {
				lengths[index] = 0;
				index++;
			}
		} else {
			stream.error();
		}
	}
	return lengths;
}

int getNextLiteralValue(PngDecodingDataStream stream) throws IOException {
	return literalTable.getNextValue(stream);
}

int getNextDistanceValue(PngDecodingDataStream stream) throws IOException {
	return distanceTable.getNextValue(stream);
}

}
