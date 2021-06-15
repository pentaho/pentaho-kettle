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

public class PngHuffmanTable {
	CodeLengthInfo[] codeLengthInfo;
	int[] codeValues;
	
	static final int MAX_CODE_LENGTH = 15;
	static final int BAD_CODE = 0xFFFFFFF;
	static final int incs[] = {1391376, 463792, 198768, 86961, 33936, 13776, 4592, 1968, 861, 336, 112, 48, 21, 7, 3, 1};

PngHuffmanTable (int[] lengths) {
	super();
	initialize(lengths);
	generateTable(lengths);
}

private void initialize(int[] lengths) {
	codeValues = new int[lengths.length];
	for (int i = 0; i < codeValues.length; i++) {
		codeValues[i] = i;
	}
	
	// minCodesByLength[n] : The smallest Huffman code of length n + 1.
	// maxCodesByLength[n] : The largest Huffman code of length n + 1.
	// indexesByLength[n] : Index into the values array. First value with a code of length n + 1.
	codeLengthInfo = new CodeLengthInfo[MAX_CODE_LENGTH];
	for (int i = 0; i < MAX_CODE_LENGTH; i++) {
		codeLengthInfo[i] = new CodeLengthInfo();
		codeLengthInfo[i].length = i;
		codeLengthInfo[i].baseIndex = 0;
		codeLengthInfo[i].min = BAD_CODE;
		codeLengthInfo[i].max = -1;
	}
}
	
private void generateTable(int[] lengths) {
	// Sort the values using shellsort. Primary key is code size. Secondary key is value.
	int codeValuesTemp;
	for (int k = 0; k < 16; k++) {
		for (int h = incs[k], i = h; i < lengths.length; i++) {
			int v = lengths[i];
			codeValuesTemp = codeValues[i];
			int j = i;
			while (j >= h && (lengths[j - h] > v || (lengths[j - h] == v && codeValues[j - h] > codeValuesTemp))) {
				lengths[j] = lengths[j - h];
				codeValues[j] = codeValues[j - h];
				j -= h;
			}
			lengths[j] = v;
			codeValues[j] = codeValuesTemp;
		}
	}

	// These values in these arrays correspond to the elements of the
	// "values" array. The Huffman code for codeValues[N] is codes[N]
	// and the length of the code is lengths[N].
	int[] codes = new int[lengths.length];
	int lastLength = 0;
	int code = 0;
	for (int i = 0; i < lengths.length; i++) {
		while (lastLength != lengths[i]) {
			lastLength++;
			code <<= 1;
		}
		if (lastLength != 0) {
			codes[i] = code;
			code++;
		}
	}
	
	int last = 0;
	for (int i = 0; i < lengths.length; i++) {
		if (last != lengths[i]) {
			last = lengths[i];
			codeLengthInfo[last - 1].baseIndex = i;
			codeLengthInfo[last - 1].min = codes[i];
		}
		if (last != 0) codeLengthInfo[last - 1].max = codes[i];
	}
}

int getNextValue(PngDecodingDataStream stream) throws IOException {
	int code = stream.getNextIdatBit();
	int codelength = 0;

	// Here we are taking advantage of the fact that 1 bits are used as
	// a prefix to the longer codeValues.
	while (codelength < MAX_CODE_LENGTH && code > codeLengthInfo[codelength].max) {
		code = ((code << 1) | stream.getNextIdatBit());
        codelength++;
	}
	if (codelength >= MAX_CODE_LENGTH) stream.error();

	// Now we have a Huffman code of length (codelength + 1) that
	// is somewhere in the range
	// minCodesByLength[codelength]..maxCodesByLength[codelength].
	// This code is the (offset + 1)'th code of (codelength + 1);
	int offset = code - codeLengthInfo[codelength].min;

	// indexesByLength[codelength] is the first code of length (codelength + 1)
	// so now we can look up the value for the Huffman code in the table.
	int index = codeLengthInfo[codelength].baseIndex + offset;
	return codeValues[index];
}	
	
static class CodeLengthInfo {
	int length;
	int max;
	int min;
	int baseIndex;
}

}
