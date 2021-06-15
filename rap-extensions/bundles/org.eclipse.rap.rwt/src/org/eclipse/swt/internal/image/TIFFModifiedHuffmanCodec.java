/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.image;

import org.eclipse.swt.*;

/*
* Decoder for 
* - CCITT Group 3 1-Dimensional Modified Huffman run length encoding
*   (TIFF compression type 2)
* - CCITT T.4 bi-level encoding 1D
*   (TIFF compression type 3 option 1D) 
*/
final class TIFFModifiedHuffmanCodec {
	static final short[][][] BLACK_CODE = {
		/* 2 bits  */
		{{2, 3}, {3, 2}},
		/* 3 bits  */
		{{2, 1}, {3, 4}},
		/* 4 bits  */
		{{2, 6}, {3, 5}},
		/* 5 bits  */
		{{3, 7}},
		/* 6 bits  */
		{{4, 9}, {5, 8}},
		/* 7 bits  */
		{{4, 10}, {5, 11}, {7, 12}},
		/* 8 bits  */
		{{4, 13}, {7, 14}},
		/* 9 bits  */
		{{24, 15}},
		/* 10 bits */
		{{8, 18}, {15, 64}, {23, 16}, {24, 17}, {55, 0}},
		/* 11 bits */
		{/* EOL */{0, -1}, {8, 1792}, {23, 24}, {24, 25}, {40, 23}, {55, 22}, {103, 19},
		{104, 20}, {108, 21}, {12, 1856}, {13, 1920}},
		/* 12 bits */
		{{18, 1984}, {19, 2048}, {20, 2112}, {21, 2176}, {22, 2240}, {23, 2304},
		{28, 2368}, {29, 2432}, {30, 2496}, {31, 2560}, {36, 52}, {39, 55}, {40, 56},
		{43, 59}, {44, 60}, {51, 320}, {52, 384}, {53, 448}, {55, 53}, {56, 54}, {82, 50},
		{83, 51}, {84, 44}, {85, 45}, {86, 46}, {87, 47}, {88, 57}, {89, 58}, {90, 61},
		{91, 256}, {100, 48}, {101, 49}, {102, 62}, {103, 63}, {104, 30}, {105, 31},
		{106, 32}, {107, 33}, {108, 40}, {109, 41}, {200, 128}, {201, 192}, {202, 26},
		{203, 27}, {204, 28}, {205, 29}, {210, 34}, {211, 35}, {212, 36}, {213, 37},
		{214, 38}, {215, 39}, {218, 42}, {219, 43}},
		/* 13 bits */
		{{74, 640}, {75, 704}, {76, 768}, {77, 832}, {82, 1280}, {83, 1344}, {84, 1408},
		{85, 1472}, {90, 1536}, {91, 1600}, {100, 1664}, {101, 1728}, {108, 512},
		{109, 576}, {114, 896}, {115, 960}, {116, 1024}, {117, 1088}, {118, 1152},
		{119, 1216}}
	};

	static final short[][][] WHITE_CODE = {
		/* 4 bits */
		{{7, 2}, {8, 3}, {11, 4}, {12, 5}, {14, 6}, {15, 7}},
		/* 5 bits */
		{{7, 10}, {8, 11}, {18, 128}, {19, 8}, {20, 9}, {27, 64}},
		/* 6 bits */
		{{3, 13}, {7, 1}, {8, 12}, {23, 192}, {24, 1664}, {42, 16}, {43, 17}, {52, 14},
		{53, 15}},
		/* 7 bits */
		{{3, 22}, {4, 23}, {8, 20}, {12, 19}, {19, 26}, {23, 21}, {24, 28}, {36, 27},
		{39, 18}, {40, 24}, {43, 25}, {55, 256}},
		/* 8 bits */
		{{2, 29}, {3, 30}, {4, 45}, {5, 46}, {10, 47}, {11, 48}, {18, 33}, {19, 34},
		{20, 35}, {21, 36}, {22, 37}, {23, 38}, {26, 31}, {27, 32}, {36, 53}, {37, 54},
		{40, 39}, {41, 40}, {42, 41}, {43, 42}, {44, 43}, {45, 44}, {50, 61}, {51, 62},
		{52, 63}, {53, 0}, {54, 320}, {55, 384}, {74, 59}, {75, 60}, {82, 49}, {83, 50},
		{84, 51}, {85, 52}, {88, 55}, {89, 56}, {90, 57}, {91, 58}, {100, 448},
		{101, 512}, {103, 640}, {104, 576}},
		/* 9 bits */
		{{152, 1472}, {153, 1536}, {154, 1600}, {155, 1728}, {204, 704}, {205, 768},
		{210, 832}, {211, 896}, {212, 960}, {213, 1024}, {214, 1088}, {215, 1152},
		{216, 1216}, {217, 1280}, {218, 1344}, {219, 1408}},
		/* 10 bits */
		{},
		/* 11 bits */
		{{8, 1792}, {12, 1856}, {13, 1920}},
		/* 12 bits */
		{/* EOL */{1, -1}, {18, 1984}, {19, 2048}, {20, 2112}, {21, 2176}, {22, 2240}, {23, 2304},
		{28, 2368}, {29, 2432}, {30, 2496}, {31, 2560}}
	};
	
	static final int BLACK_MIN_BITS = 2;
	static final int WHITE_MIN_BITS = 4;

	boolean isWhite;
	int whiteValue = 0;
	int blackValue = 1;
	byte[] src;
	byte[] dest;
	int byteOffsetSrc = 0;
	int bitOffsetSrc = 0;
	int byteOffsetDest = 0;
	int bitOffsetDest = 0;
	int code = 0;
	int nbrBits = 0;
	/* nbr of bytes per row */
	int rowSize;

public int decode(byte[] src, byte[] dest, int offsetDest, int rowSize, int nRows) {
	this.src = src;
	this.dest = dest;
	this.rowSize = rowSize;
	byteOffsetSrc = 0;
	bitOffsetSrc = 0;
	byteOffsetDest = offsetDest;
	bitOffsetDest = 0;
	int cnt = 0;
	while (cnt < nRows && decodeRow()) {
		cnt++;
		/* byte aligned */
		if (bitOffsetDest > 0) {
			byteOffsetDest++;
			bitOffsetDest = 0; 
		}
	}
	return byteOffsetDest - offsetDest;
}

boolean decodeRow() {
	isWhite = true;
	int n = 0;
	while (n < rowSize) {
		int runLength = decodeRunLength();
		if (runLength < 0) return false;
		n += runLength;
		setNextBits(isWhite ? whiteValue : blackValue, runLength);
		isWhite = !isWhite;
	}
	return true;
}

int decodeRunLength() {
	int runLength = 0;
	int partialRun = 0;
	short[][][] huffmanCode = isWhite ? WHITE_CODE : BLACK_CODE;
	while (true) {
		boolean found = false;
		nbrBits = isWhite ? WHITE_MIN_BITS : BLACK_MIN_BITS;
		code = getNextBits(nbrBits);
		for (int i = 0; i < huffmanCode.length; i++) {
			for (int j = 0; j < huffmanCode[i].length; j++) {
				if (huffmanCode[i][j][0] == code) {
					found = true;
					partialRun = huffmanCode[i][j][1];
					if (partialRun == -1) {
						/* Stop when reaching final EOL on last byte */
						if (byteOffsetSrc == src.length - 1) return -1;
						/* Group 3 starts each row with an EOL - ignore it */
					} else {
						runLength += partialRun;
						if (partialRun < 64) return runLength;
					}
					break;
				}
			}
			if (found) break;
			code = code << 1 | getNextBit();
		}
		if (!found) SWT.error(SWT.ERROR_INVALID_IMAGE);			 
	}
}

int getNextBit() {
	int value = (src[byteOffsetSrc] >>> (7 - bitOffsetSrc)) & 0x1;
	bitOffsetSrc++;
	if (bitOffsetSrc > 7) {
		byteOffsetSrc++;
		bitOffsetSrc = 0;
	}
	return value;
}

int getNextBits(int cnt) {
	int value = 0;
	for (int i = 0; i < cnt; i++) {
		value = value << 1 | getNextBit();
	}
	return value;
}

void setNextBits(int value, int cnt) {
	int n = cnt;
	while (bitOffsetDest > 0 && bitOffsetDest <= 7 && n > 0) {
		dest[byteOffsetDest] = value == 1 ?
			(byte)(dest[byteOffsetDest] | (1 << (7 - bitOffsetDest))) :
			(byte)(dest[byteOffsetDest] & ~(1 << (7 - bitOffsetDest)));
		n--;
		bitOffsetDest++; 
	}
	if (bitOffsetDest == 8) {
		byteOffsetDest++;
		bitOffsetDest = 0;
	}
	while (n >= 8) {
		dest[byteOffsetDest++] = (byte) (value == 1 ? 0xFF : 0);
		n -= 8;
	}
	while (n > 0) {
		dest[byteOffsetDest] = value == 1 ?
			(byte)(dest[byteOffsetDest] | (1 << (7 - bitOffsetDest))) :
			(byte)(dest[byteOffsetDest] & ~(1 << (7 - bitOffsetDest)));
		n--;
		bitOffsetDest++;		
	}	
}

}
