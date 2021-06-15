/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This source file is based in part on the work of the Independent JPEG Group (IJG)
 * and is made available under the terms contained in the about_files/IJG_README
 * file accompanying this program.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.image;


import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import java.io.*;

public final class JPEGFileFormat extends FileFormat {
	int restartInterval;
	JPEGFrameHeader frameHeader;
	int imageWidth, imageHeight;
	int interleavedMcuCols, interleavedMcuRows;
	int maxV, maxH;
	boolean progressive;
	int samplePrecision;
	int nComponents;
	int[][] frameComponents;
	int[] componentIds;
	byte[][] imageComponents;
	int[] dataUnit;
	int[][][] dataUnits;
	int[] precedingDCs;
	JPEGScanHeader scanHeader;
	byte[] dataBuffer;
	int currentBitCount;
	int bufferCurrentPosition;
	int restartsToGo;
	int nextRestartNumber;
	JPEGHuffmanTable[] acHuffmanTables;
	JPEGHuffmanTable[] dcHuffmanTables;
	int[][] quantizationTables;
	int currentByte;
	int encoderQFactor = 75;
	int eobrun = 0;
	/* JPEGConstants */
	public static final int DCTSIZE = 8;
	public static final int DCTSIZESQR = 64;
	/* JPEGFixedPointConstants */
	public static final int FIX_0_899976223 = 7373;
	public static final int FIX_1_961570560 = 16069;
	public static final int FIX_2_053119869 = 16819;
	public static final int FIX_0_298631336 = 2446;
	public static final int FIX_1_847759065 = 15137;
	public static final int FIX_1_175875602 = 9633;
	public static final int FIX_3_072711026 = 25172;
	public static final int FIX_0_765366865 = 6270;
	public static final int FIX_2_562915447 = 20995;
	public static final int FIX_0_541196100 = 4433;
	public static final int FIX_0_390180644 = 3196;
	public static final int FIX_1_501321110 = 12299;
	/* JPEGMarkerCodes */
	public static final int APP0  = 0xFFE0;
	public static final int APP15 = 0xFFEF;
	public static final int COM   = 0xFFFE;
	public static final int DAC   = 0xFFCC;
	public static final int DHP   = 0xFFDE;
	public static final int DHT   = 0xFFC4;
	public static final int DNL   = 0xFFDC;
	public static final int DRI   = 0xFFDD;
	public static final int DQT   = 0xFFDB;
	public static final int EOI   = 0xFFD9;
	public static final int EXP   = 0xFFDF;
	public static final int JPG   = 0xFFC8;
	public static final int JPG0  = 0xFFF0;
	public static final int JPG13 = 0xFFFD;
	public static final int RST0  = 0xFFD0;
	public static final int RST1  = 0xFFD1;
	public static final int RST2  = 0xFFD2;
	public static final int RST3  = 0xFFD3;
	public static final int RST4  = 0xFFD4;
	public static final int RST5  = 0xFFD5;
	public static final int RST6  = 0xFFD6;
	public static final int RST7  = 0xFFD7;
	public static final int SOF0  = 0xFFC0;
	public static final int SOF1  = 0xFFC1;
	public static final int SOF2  = 0xFFC2;
	public static final int SOF3  = 0xFFC3;
	public static final int SOF5  = 0xFFC5;
	public static final int SOF6  = 0xFFC6;
	public static final int SOF7  = 0xFFC7;
	public static final int SOF9  = 0xFFC9;
	public static final int SOF10 = 0xFFCA;
	public static final int SOF11 = 0xFFCB;
	public static final int SOF13 = 0xFFCD;
	public static final int SOF14 = 0xFFCE;
	public static final int SOF15 = 0xFFCF;
	public static final int SOI   = 0xFFD8;
	public static final int SOS   = 0xFFDA;
	public static final int TEM   = 0xFF01;
	/* JPEGFrameComponentParameterConstants */
	public static final int TQI	= 0;
	public static final int HI	= 1;
	public static final int VI	= 2;
	public static final int CW	= 3;
	public static final int CH	= 4;
	/* JPEGScanComponentParameterConstants */
	public static final int DC	= 0;
	public static final int AC	= 1;
	/* JFIF Component Constants */
	public static final int ID_Y		= 1 - 1;
	public static final int ID_CB	= 2 - 1;
	public static final int ID_CR	= 3 - 1;
	public static final RGB[] RGB16 = new RGB[] {
		new RGB(0,0,0),
		new RGB(0x80,0,0),
		new RGB(0,0x80,0),
		new RGB(0x80,0x80,0),
		new RGB(0,0,0x80),
		new RGB(0x80,0,0x80),
		new RGB(0,0x80,0x80),
		new RGB(0xC0,0xC0,0xC0),
		new RGB(0x80,0x80,0x80),
		new RGB(0xFF,0,0),
		new RGB(0,0xFF,0),
		new RGB(0xFF,0xFF,0),
		new RGB(0,0,0xFF),
		new RGB(0xFF,0,0xFF),
		new RGB(0,0xFF,0xFF),
		new RGB(0xFF,0xFF,0xFF),
	};
	public static final int[] ExtendTest = {
		0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 
		4096, 8192, 16384, 32768, 65536, 131072, 262144
	};
	public static final int[] ExtendOffset = new int[] {
		0, -1, -3, -7, -15, -31, -63, -127, -255, -511, -1023, -2047, 
		-4095, -8191, -16383, -32767, -65535, -131071, -262143
	};
	public static final int[] ZigZag8x8 = {
		0, 1, 8, 16, 9, 2, 3, 10,
		17, 24, 32, 25, 18, 11, 4, 5,
		12, 19, 26, 33, 40, 48, 41, 34,
		27, 20, 13, 6, 7, 14, 21, 28,
		35, 42, 49, 56, 57, 50, 43, 36,
		29, 22, 15, 23, 30, 37, 44, 51,
		58, 59, 52, 45, 38, 31, 39, 46,
		53, 60, 61, 54, 47, 55, 62, 63
	};

	public static final int[] CrRTable, CbBTable, CrGTable, CbGTable;
	public static final int[] RYTable, GYTable, BYTable,
		RCbTable, GCbTable, BCbTable, RCrTable, GCrTable, BCrTable, NBitsTable;
	static {
		/* Initialize RGB-YCbCr Tables */
		int [] rYTable = new int[256];
		int [] gYTable = new int[256];
		int [] bYTable = new int[256];
		int [] rCbTable = new int[256];
		int [] gCbTable = new int[256];
		int [] bCbTable = new int[256];
		int [] gCrTable = new int[256];
		int [] bCrTable = new int[256];
		for (int i = 0; i < 256; i++) {
			rYTable[i] = i * 19595;
			gYTable[i] = i * 38470;
			bYTable[i] = i * 7471 + 32768;
			rCbTable[i] = i * -11059;
			gCbTable[i] = i * -21709;
			bCbTable[i] = i * 32768 + 8388608;
			gCrTable[i] = i * -27439;
			bCrTable[i] = i * -5329;
		}
		RYTable = rYTable;
		GYTable = gYTable;
		BYTable = bYTable;
		RCbTable = rCbTable;
		GCbTable = gCbTable;
		BCbTable = bCbTable;
		RCrTable = bCbTable;
		GCrTable = gCrTable;
		BCrTable = bCrTable;

		/* Initialize YCbCr-RGB Tables */
		int [] crRTable = new int[256];
		int [] cbBTable = new int[256];
		int [] crGTable = new int[256];
		int [] cbGTable = new int[256];
		for (int i = 0; i < 256; i++) {
			int x2 = 2 * i - 255;
			crRTable[i] = (45941 * x2 + 32768) >> 16;
			cbBTable[i] = (58065 * x2 + 32768) >> 16;
			crGTable[i] = -23401 * x2;
			cbGTable[i] = -11277 * x2 + 32768;
		}
		CrRTable = crRTable;
		CbBTable = cbBTable;
		CrGTable = crGTable;
		CbGTable = cbGTable;

		/* Initialize BitCount Table */
		int nBits = 1;
		int power2 = 2;
		int [] nBitsTable = new int[2048];
		nBitsTable[0] = 0;
		for (int i = 1; i < nBitsTable.length; i++) {
			if (!(i < power2)) {
				nBits++;
				power2 *= 2;
			}
			nBitsTable[i] = nBits;
		}
		NBitsTable = nBitsTable;
	}
void compress(ImageData image, byte[] dataYComp, byte[] dataCbComp, byte[] dataCrComp) {
	int srcWidth = image.width;
	int srcHeight = image.height;
	int vhFactor = maxV * maxH;
	int[] frameComponent;
	imageComponents = new byte[nComponents][];
	for (int i = 0; i < nComponents; i++) {
		frameComponent = frameComponents[componentIds[i]];
		imageComponents[i] = new byte[frameComponent[CW] * frameComponent[CH]];
	}
	frameComponent = frameComponents[componentIds[ID_Y]];
	for (int yPos = 0; yPos < srcHeight; yPos++) {
		int srcOfs = yPos * srcWidth;
		int dstOfs = yPos * frameComponent[CW];
		System.arraycopy(dataYComp, srcOfs, imageComponents[ID_Y], dstOfs, srcWidth);
	}
	frameComponent = frameComponents[componentIds[ID_CB]];
	for (int yPos = 0; yPos < srcHeight / maxV; yPos++) {
		int destRowIndex = yPos * frameComponent[CW];
		for (int xPos = 0; xPos < srcWidth / maxH; xPos++) {
			int sum = 0;
			for (int iv = 0; iv < maxV; iv++) {
				int srcIndex = (yPos * maxV + iv) * srcWidth + (xPos * maxH);
				for (int ih = 0; ih < maxH; ih++) {
					sum += dataCbComp[srcIndex + ih] & 0xFF;
				}
			}
			imageComponents[ID_CB][destRowIndex + xPos] = (byte)(sum / vhFactor);
		}
	}
	frameComponent = frameComponents[componentIds[ID_CR]];
	for (int yPos = 0; yPos < srcHeight / maxV; yPos++) {
		int destRowIndex = yPos * frameComponent[CW];
		for (int xPos = 0; xPos < srcWidth / maxH; xPos++) {
			int sum = 0;
			for (int iv = 0; iv < maxV; iv++) {
				int srcIndex = (yPos * maxV + iv) * srcWidth + (xPos * maxH);
				for (int ih = 0; ih < maxH; ih++) {
					sum += dataCrComp[srcIndex + ih] & 0xFF;
				}
			}
			imageComponents[ID_CR][destRowIndex + xPos] = (byte)(sum / vhFactor);
		}
	}
	for (int iComp = 0; iComp < nComponents; iComp++) {
		byte[] imageComponent = imageComponents[iComp];
		frameComponent = frameComponents[componentIds[iComp]];
		int hFactor = frameComponent[HI];
		int vFactor = frameComponent[VI];
		int componentWidth = frameComponent[CW];
		int componentHeight = frameComponent[CH];
		int compressedWidth = srcWidth / (maxH / hFactor);
		int compressedHeight = srcHeight / (maxV / vFactor);
		if (compressedWidth < componentWidth) {
			int delta = componentWidth - compressedWidth;
			for (int yPos = 0; yPos < compressedHeight; yPos++) {
				int dstOfs = ((yPos + 1) * componentWidth - delta);
				int dataValue = imageComponent[(dstOfs > 0) ? dstOfs - 1 : 0] & 0xFF;
				for (int i = 0; i < delta; i++) {
					imageComponent[dstOfs + i] = (byte)dataValue;
				}
			}
		}
		if (compressedHeight < componentHeight) {
			int srcOfs = (compressedHeight > 0) ? (compressedHeight - 1) * componentWidth : 1;
			for (int yPos = (compressedHeight > 0) ? compressedHeight : 1; yPos <= componentHeight; yPos++) {
				int dstOfs = (yPos - 1) * componentWidth;
				System.arraycopy(imageComponent, srcOfs, imageComponent, dstOfs, componentWidth);
			}
		}
	}
}
void convert4BitRGBToYCbCr(ImageData image) {
	RGB[] rgbs = image.getRGBs();
	int paletteSize = rgbs.length;
	byte[] yComp = new byte[paletteSize];
	byte[] cbComp = new byte[paletteSize];
	byte[] crComp = new byte[paletteSize];
	int srcWidth = image.width;
	int srcHeight = image.height;
	for (int i = 0; i < paletteSize; i++) {
		RGB color = rgbs[i];
		int r = color.red;
		int g = color.green;
		int b = color.blue;
		int n = RYTable[r] + GYTable[g] + BYTable[b];
		yComp[i] = (byte)(n >> 16);
		if ((n < 0) && ((n & 0xFFFF) != 0)) yComp[i]--;
		n = RCbTable[r] + GCbTable[g] + BCbTable[b];
		cbComp[i] = (byte)(n >> 16);
		if ((n < 0) && ((n & 0xFFFF) != 0)) cbComp[i]--;
		n = RCrTable[r] + GCrTable[g] + BCrTable[b];
		crComp[i] = (byte)(n >> 16);
		if ((n < 0) && ((n & 0xFFFF) != 0)) crComp[i]--;
	}
	int bSize = srcWidth * srcHeight;
	byte[] dataYComp = new byte[bSize];
	byte[] dataCbComp = new byte[bSize];
	byte[] dataCrComp = new byte[bSize];
	byte[] origData = image.data;
	int bytesPerLine = image.bytesPerLine;
	int maxScanlineByte = srcWidth >> 1;
	for (int yPos = 0; yPos < srcHeight; yPos++) {
		for (int xPos = 0; xPos < maxScanlineByte; xPos++) {
			int srcIndex = yPos * bytesPerLine + xPos;
			int dstIndex = yPos * srcWidth + (xPos * 2);
			int value2 = origData[srcIndex] & 0xFF;
			int value1 = value2 >> 4;
			value2 &= 0x0F;
			dataYComp[dstIndex] = yComp[value1];
			dataCbComp[dstIndex] = cbComp[value1];
			dataCrComp[dstIndex] = crComp[value1];
			dataYComp[dstIndex + 1] = yComp[value2];
			dataCbComp[dstIndex + 1] = cbComp[value2];
			dataCrComp[dstIndex + 1] = crComp[value2];
		}
	}
	compress(image, dataYComp, dataCbComp, dataCrComp);
}
void convert8BitRGBToYCbCr(ImageData image) {
	RGB[] rgbs = image.getRGBs();
	int paletteSize = rgbs.length;
	byte[] yComp = new byte[paletteSize];
	byte[] cbComp = new byte[paletteSize];
	byte[] crComp = new byte[paletteSize];
	int srcWidth = image.width;
	int srcHeight = image.height;
	for (int i = 0; i < paletteSize; i++) {
		RGB color = rgbs[i];
		int r = color.red;
		int g = color.green;
		int b = color.blue;
		int n = RYTable[r] + GYTable[g] + BYTable[b];
		yComp[i] = (byte)(n >> 16);
		if ((n < 0) && ((n & 0xFFFF) != 0)) yComp[i]--;
		n = RCbTable[r] + GCbTable[g] + BCbTable[b];
		cbComp[i] = (byte)(n >> 16);
		if ((n < 0) && ((n & 0xFFFF) != 0)) cbComp[i]--;
		n = RCrTable[r] + GCrTable[g] + BCrTable[b];
		crComp[i] = (byte)(n >> 16);
		if ((n < 0) && ((n & 0xFFFF) != 0)) crComp[i]--;
	}
	int dstWidth = image.width;
	int dstHeight = srcHeight;
	int stride = ((srcWidth + 3) >> 2) << 2;
	int bSize = dstWidth * dstHeight;
	byte[] dataYComp = new byte[bSize];
	byte[] dataCbComp = new byte[bSize];
	byte[] dataCrComp = new byte[bSize];
	byte[] origData = image.data;
	for (int yPos = 0; yPos < srcHeight; yPos++) {
		int srcRowIndex = yPos * stride;
		int dstRowIndex = yPos * dstWidth;
		for (int xPos = 0; xPos < srcWidth; xPos++) {
			int value = origData[srcRowIndex + xPos] & 0xFF;
			int dstIndex = dstRowIndex + xPos;
			dataYComp[dstIndex] = yComp[value];
			dataCbComp[dstIndex] = cbComp[value];
			dataCrComp[dstIndex] = crComp[value];
		}
	}
	compress(image, dataYComp, dataCbComp, dataCrComp);
}
byte[] convertCMYKToRGB() {
	/* Unsupported CMYK format. Answer an empty byte array. */
	return new byte[0];
}
void convertImageToYCbCr(ImageData image) {
	switch (image.depth) {
		case 4:
			convert4BitRGBToYCbCr(image);
			return;
		case 8:
			convert8BitRGBToYCbCr(image);
			return;
		case 16:
		case 24:
		case 32:
			convertMultiRGBToYCbCr(image);
			return;
		default:
			SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
	}
	return;
}
void convertMultiRGBToYCbCr(ImageData image) {
	int srcWidth = image.width;
	int srcHeight = image.height;
	int bSize = srcWidth * srcHeight;
	byte[] dataYComp = new byte[bSize];
	byte[] dataCbComp = new byte[bSize];
	byte[] dataCrComp = new byte[bSize];
	PaletteData palette = image.palette;
	int[] buffer = new int[srcWidth];
	if (palette.isDirect) {
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;
		for (int yPos = 0; yPos < srcHeight; yPos++) {
			image.getPixels(0, yPos, srcWidth, buffer, 0);
			int dstRowIndex = yPos * srcWidth;
			for (int xPos = 0; xPos < srcWidth; xPos++) {
				int pixel = buffer[xPos];
				int dstDataIndex = dstRowIndex + xPos;
				int r = pixel & redMask;
				r = (redShift < 0) ? r >>> -redShift : r << redShift;
				int g = pixel & greenMask;
				g = (greenShift < 0) ? g >>> -greenShift : g << greenShift;
				int b = pixel & blueMask;
				b = (blueShift < 0) ? b >>> -blueShift : b << blueShift;				
				dataYComp[dstDataIndex] = (byte)((RYTable[r] + GYTable[g] + BYTable[b]) >> 16);
				dataCbComp[dstDataIndex] = (byte)((RCbTable[r] + GCbTable[g] + BCbTable[b]) >> 16);
				dataCrComp[dstDataIndex] = (byte)((RCrTable[r] + GCrTable[g] + BCrTable[b]) >> 16);
			}
		}
	} else {
		for (int yPos = 0; yPos < srcHeight; yPos++) {
			image.getPixels(0, yPos, srcWidth, buffer, 0);
			int dstRowIndex = yPos * srcWidth;
			for (int xPos = 0; xPos < srcWidth; xPos++) {
				int pixel = buffer[xPos];
				int dstDataIndex = dstRowIndex + xPos;
				RGB rgb = palette.getRGB(pixel);
				int r = rgb.red;
				int g = rgb.green;
				int b = rgb.blue;
				dataYComp[dstDataIndex] = (byte)((RYTable[r] + GYTable[g] + BYTable[b]) >> 16);
				dataCbComp[dstDataIndex] = (byte)((RCbTable[r] + GCbTable[g] + BCbTable[b]) >> 16);
				dataCrComp[dstDataIndex] = (byte)((RCrTable[r] + GCrTable[g] + BCrTable[b]) >> 16);
			}
		}
	}
	compress(image, dataYComp, dataCbComp, dataCrComp);
}
byte[] convertYToRGB() {
	int compWidth = frameComponents[componentIds[ID_Y]][CW];
	int bytesPerLine = (((imageWidth * 8 + 7) / 8) + 3) / 4 * 4;
	byte[] data = new byte[bytesPerLine * imageHeight];
	byte[] yComp = imageComponents[ID_Y];
	int destIndex = 0;
	for (int i = 0; i < imageHeight; i++) {
		int srcIndex = i * compWidth;
		for (int j = 0; j < bytesPerLine; j++) {
			int y = yComp[srcIndex] & 0xFF;
			if (y < 0) {
				y = 0;
			} else {
				if (y > 255) y = 255;
			}
			if (j >= imageWidth) {
				y = 0;
			}
			data[destIndex] = (byte)y;
			srcIndex++;
			destIndex++;
		}
	}
	return data;
}
byte[] convertYCbCrToRGB() {
	/**
	 * Convert existing image components into an RGB format.
	 * YCbCr is defined per CCIR 601-1, except that Cb and Cr are
	 * normalized to the range 0..MAXJSAMPLE rather than -0.5 .. 0.5.
	 * The conversion equations to be implemented are therefore
	 * 	R = Y                + 1.40200 * Cr
	 * 	G = Y - 0.34414 * Cb - 0.71414 * Cr
	 * 	B = Y + 1.77200 * Cb
	 * where Cb and Cr represent the incoming values less MAXJSAMPLE/2.
	 * (These numbers are derived from TIFF 6.0 section 21, dated 3-June-92.)
	 * 
	 * To avoid floating-point arithmetic, we represent the fractional constants
	 * as integers scaled up by 2^16 (about 4 digits precision); we have to divide
	 * the products by 2^16, with appropriate rounding, to get the correct answer.
	 * Notice that Y, being an integral input, does not contribute any fraction
	 * so it need not participate in the rounding.
	 * 
	 * For even more speed, we avoid doing any multiplications in the inner loop
	 * by precalculating the constants times Cb and Cr for all possible values.
	 * For 8-bit JSAMPLEs this is very reasonable (only 256 entries per table);
	 * for 12-bit samples it is still acceptable.  It's not very reasonable for
	 * 16-bit samples, but if you want lossless storage you shouldn't be changing
	 * colorspace anyway.
	 * The Cr=>R and Cb=>B values can be rounded to integers in advance; the
	 * values for the G calculation are left scaled up, since we must add them
	 * together before rounding.
	 */
	int bSize = imageWidth * imageHeight * nComponents;
	byte[] rgbData = new byte[bSize];
	int destIndex = 0;
	expandImageComponents();
	byte[] yComp = imageComponents[ID_Y];
	byte[] cbComp = imageComponents[ID_CB];
	byte[] crComp = imageComponents[ID_CR];
	int compWidth = frameComponents[componentIds[ID_Y]][CW];
	for (int v = 0; v < imageHeight; v++) {
		int srcIndex = v * compWidth;
		for (int i = 0; i < imageWidth; i++) {
			int y = yComp[srcIndex] & 0xFF;
			int cb = cbComp[srcIndex] & 0xFF;
			int cr = crComp[srcIndex] & 0xFF;
			int r = y + CrRTable[cr];
			int g = y + ((CbGTable[cb] + CrGTable[cr]) >> 16);
			int b = y + CbBTable[cb];
			if (r < 0) {
				r = 0;
			} else {
				if (r > 255) r = 255;
			}
			if (g < 0) {
				g = 0;
			} else {
				if (g > 255) g = 255;
			}
			if (b < 0) {
				b = 0;
			} else {
				if (b > 255) b = 255;
			}
			rgbData[destIndex] = (byte)b;
			rgbData[destIndex + 1] = (byte)g;
			rgbData[destIndex + 2] = (byte)r;
			destIndex += 3;
			srcIndex++;
		}
	}
	return rgbData;
}
void decodeACCoefficients(int[] dataUnit, int iComp) {
	int[] sParams = scanHeader.componentParameters[componentIds[iComp]];
	JPEGHuffmanTable acTable = acHuffmanTables[sParams[AC]];
	int k = 1;
	while (k < 64) {
		int rs = decodeUsingTable(acTable);
		int r = rs >> 4;
		int s = rs & 0xF;
		if (s == 0) {
			if (r == 15) {
				k += 16;
			} else {
				break;
			}
		} else {
			k += r;
			int bits = receive(s);
			dataUnit[ZigZag8x8[k]] = extendBy(bits, s);
			k++;
		}
	}
}
void decodeACFirstCoefficients(int[] dataUnit, int iComp, int start, int end, int approxBit) {
	if (eobrun > 0) {
		eobrun--;
		return;
	}
	int[] sParams = scanHeader.componentParameters[componentIds[iComp]];
	JPEGHuffmanTable acTable = acHuffmanTables[sParams[AC]];
	int k = start;
	while (k <= end) {
		int rs = decodeUsingTable(acTable);
		int r = rs >> 4;
		int s = rs & 0xF;
		if (s == 0) {
			if (r == 15) {
				k += 16;
			} else {
				eobrun = (1 << r) + receive(r) - 1;
				break;
			}
		} else {
			k += r;
			int bits = receive(s);
			dataUnit[ZigZag8x8[k]] = extendBy(bits, s) << approxBit;
			k++;
		}
	}
}
void decodeACRefineCoefficients(int[] dataUnit, int iComp, int start, int end, int approxBit) {
	int[] sParams = scanHeader.componentParameters[componentIds[iComp]];
	JPEGHuffmanTable acTable = acHuffmanTables[sParams[AC]];
	int k = start;
	while (k <= end) {
		if (eobrun > 0) {
			while (k <= end) {
				int zzIndex = ZigZag8x8[k];
				if (dataUnit[zzIndex] != 0) {
					dataUnit[zzIndex] = refineAC(dataUnit[zzIndex], approxBit);
				}
				k++;
			}
			eobrun--;
		} else {
			int rs = decodeUsingTable(acTable);
			int r = rs >> 4;
			int s = rs & 0xF;
			if (s == 0) {
				if (r == 15) {
					int zeros = 0;
					while (zeros < 16 && k <= end) {
						int zzIndex = ZigZag8x8[k];
						if (dataUnit[zzIndex] != 0) {
							dataUnit[zzIndex] = refineAC(dataUnit[zzIndex], approxBit);
						} else {
							zeros++;
						}
						k++;
					}
				} else {
					eobrun = (1 << r) + receive(r);
				}
			} else {
				int bit = receive(s);
				int zeros = 0;
				int zzIndex = ZigZag8x8[k];
				while ((zeros < r || dataUnit[zzIndex] != 0) && k <= end) {
					if (dataUnit[zzIndex] != 0) {
						dataUnit[zzIndex] = refineAC(dataUnit[zzIndex], approxBit);
					} else {
						zeros++;
					}
					k++;
					zzIndex = ZigZag8x8[k];
				}
				if (bit != 0) {
					dataUnit[zzIndex] = 1 << approxBit;
				} else {
					dataUnit[zzIndex] = -1 << approxBit;
				}
				k++;
			}
		}
	}
}
int refineAC(int ac, int approxBit) {
	if (ac > 0) {
		int bit = nextBit();
		if (bit != 0) {
			ac += 1 << approxBit;
		}
	} else if (ac < 0) {
		int bit = nextBit();
		if (bit != 0) {
			ac += -1 << approxBit;
		}
	}
	return ac;
}
void decodeDCCoefficient(int[] dataUnit, int iComp, boolean first, int approxBit) {
	int[] sParams = scanHeader.componentParameters[componentIds[iComp]];
	JPEGHuffmanTable dcTable = dcHuffmanTables[sParams[DC]];
	int lastDC = 0;
	if (progressive && !first) {
		int bit = nextBit();
		lastDC = dataUnit[0] + (bit << approxBit);
	} else {
		lastDC = precedingDCs[iComp];
		int nBits = decodeUsingTable(dcTable);
		if (nBits != 0) {
			int bits = receive(nBits);
			int diff = extendBy(bits, nBits);
			lastDC += diff;
			precedingDCs[iComp] = lastDC;
		}
		if (progressive) {
			lastDC = lastDC << approxBit;
		}
	}
	dataUnit[0] = lastDC;
}
void dequantize(int[] dataUnit, int iComp) {
	int[] qTable = quantizationTables[frameComponents[componentIds[iComp]][TQI]];
	for (int i = 0; i < dataUnit.length; i++) {
		int zzIndex = ZigZag8x8[i];
		dataUnit[zzIndex] = dataUnit[zzIndex] * qTable[i];
	}
}
byte[] decodeImageComponents() {
	if (nComponents == 3) { // compIds 1, 2, 3
		return convertYCbCrToRGB();
	}
//	if (nComponents == 3) { // compIds 1, 4, 5
//		Unsupported CMYK format.
//		return convertYIQToRGB();
//	}
	if (nComponents == 4) {
		return convertCMYKToRGB();
	}
	return convertYToRGB();
}
void decodeMCUAtXAndY(int xmcu, int ymcu, int nComponentsInScan, boolean first, int start, int end, int approxBit) {
	for (int iComp = 0; iComp < nComponentsInScan; iComp++) {
		int scanComponent = iComp;
		while (scanHeader.componentParameters[componentIds[scanComponent]] == null) {
			scanComponent++;
		}
		int[] frameComponent = frameComponents[componentIds[scanComponent]];
		int hi = frameComponent[HI];
		int vi = frameComponent[VI];
		if (nComponentsInScan == 1) {
			hi = 1;
			vi = 1;
		}
		int compWidth = frameComponent[CW];
		for (int ivi = 0; ivi < vi; ivi++) {
			for (int ihi = 0; ihi < hi; ihi++) {
				if (progressive) {
					// Progressive: First scan - create a new data unit.
					// Subsequent scans - refine the existing data unit.
					int index = (ymcu * vi + ivi) * compWidth + xmcu * hi + ihi;
					dataUnit = dataUnits[scanComponent][index];
					if (dataUnit == null) {
						dataUnit = new int[64];
						dataUnits[scanComponent][index] = dataUnit;
					}
				} else {
					// Sequential: Clear and reuse the data unit buffer.
					for (int i = 0; i < dataUnit.length; i++) {
						dataUnit[i] = 0;
					}
				}
				if (!progressive || scanHeader.isDCProgressiveScan()) {
					decodeDCCoefficient(dataUnit, scanComponent, first, approxBit);
				}
				if (!progressive) {
					decodeACCoefficients(dataUnit, scanComponent);
				} else {
					if (scanHeader.isACProgressiveScan()) {
						if (first) {
							decodeACFirstCoefficients(dataUnit, scanComponent, start, end, approxBit);
						} else {
							decodeACRefineCoefficients(dataUnit, scanComponent, start, end, approxBit);
						}
					}
					if (loader.hasListeners()) {
						// Dequantization, IDCT, up-sampling and color conversion
						// are done on a copy of the coefficient data in order to
						// display the image incrementally.
						int[] temp = dataUnit;
						dataUnit = new int[64];
						System.arraycopy(temp, 0, dataUnit, 0, 64);
					}
				}
				if (!progressive || (progressive && loader.hasListeners())) {
					dequantize(dataUnit, scanComponent);
					inverseDCT(dataUnit);
					storeData(dataUnit, scanComponent, xmcu, ymcu, hi, ihi, vi, ivi);
				}
			}
		}
	}
}
void decodeScan() {
	if (progressive && !scanHeader.verifyProgressiveScan()) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	int nComponentsInScan = scanHeader.getNumberOfImageComponents();
	int mcuRowsInScan = interleavedMcuRows;
	int mcusPerRow = interleavedMcuCols;
	if (nComponentsInScan == 1) {
		// Non-interleaved.
		int scanComponent = 0;
		while (scanHeader.componentParameters[componentIds[scanComponent]] == null) {
			scanComponent++;
		}
		int[] frameComponent = frameComponents[componentIds[scanComponent]];
		int hi = frameComponent[HI];
		int vi = frameComponent[VI];
		int mcuWidth = DCTSIZE * maxH / hi;
		int mcuHeight = DCTSIZE * maxV / vi;
		mcusPerRow = (imageWidth + mcuWidth - 1) / mcuWidth;
		mcuRowsInScan = (imageHeight + mcuHeight - 1) / mcuHeight;
	}
	boolean first = scanHeader.isFirstScan();
	int start = scanHeader.getStartOfSpectralSelection();
	int end = scanHeader.getEndOfSpectralSelection();
	int approxBit = scanHeader.getApproxBitPositionLow();
	restartsToGo = restartInterval;
	nextRestartNumber = 0;
	for (int ymcu = 0; ymcu < mcuRowsInScan; ymcu++) {
		for (int xmcu = 0; xmcu < mcusPerRow; xmcu++) {
			if (restartInterval != 0) {
				if (restartsToGo == 0) processRestartInterval();
				restartsToGo--;
			}
			decodeMCUAtXAndY(xmcu, ymcu, nComponentsInScan, first, start, end, approxBit);
		}
	}
}
int decodeUsingTable(JPEGHuffmanTable huffmanTable) {
	int i = 0;
	int[] maxCodes = huffmanTable.getDhMaxCodes();
	int[] minCodes = huffmanTable.getDhMinCodes();
	int[] valPtrs = huffmanTable.getDhValPtrs();
	int[] huffVals = huffmanTable.getDhValues();
	int code = nextBit();
	while (code > maxCodes[i]) {
		code = code * 2 + nextBit();
		i++;
	}
	int j = valPtrs[i] + code - minCodes[i];
	return huffVals[j];
}
void emit(int huffCode, int nBits) {
	if (nBits == 0) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	int[] power2m1 = new int[] {
		1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 
		16383, 32767, 65535, 131125
	};
	int code = (huffCode & power2m1[nBits - 1]) << (24 - nBits - currentBitCount);
	byte[] codeBuffer = new byte[4];
	codeBuffer[0] = (byte)(code & 0xFF);
	codeBuffer[1] = (byte)((code >> 8) & 0xFF);
	codeBuffer[2] = (byte)((code >> 16) & 0xFF);
	codeBuffer[3] = (byte)((code >> 24) & 0xFF);
	int abs = nBits - (8 - currentBitCount);
	if (abs < 0) abs = -abs;
	if ((abs >> 3) > 0) {
		currentByte += codeBuffer[2];
		emitByte((byte)currentByte);
		emitByte(codeBuffer[1]);
		currentByte = codeBuffer[0];
		currentBitCount += nBits - 16;
	} else {
		currentBitCount += nBits;
		if (currentBitCount >= 8) {
			currentByte += codeBuffer[2];
			emitByte((byte)currentByte);
			currentByte = codeBuffer[1];
			currentBitCount -= 8;
		} else {
			currentByte += codeBuffer[2];
		}
	}
}
void emitByte(byte byteValue) {
	if (bufferCurrentPosition >= 512) {
		resetOutputBuffer();
	}
	dataBuffer[bufferCurrentPosition] = byteValue;
	bufferCurrentPosition++;
	if (byteValue == -1) {
		emitByte((byte)0);
	}
}
void encodeACCoefficients(int[] dataUnit, int iComp) {
	int[] sParams = scanHeader.componentParameters[iComp];
	JPEGHuffmanTable acTable = acHuffmanTables[sParams[AC]];
	int[] ehCodes = acTable.ehCodes;
	byte[] ehSizes = acTable.ehCodeLengths;
	int r = 0;
	int k = 1;
	while (k < 64) {
		k++;
		int acValue = dataUnit[ZigZag8x8[k - 1]];
		if (acValue == 0) {
			if (k == 64) {
				emit(ehCodes[0], ehSizes[0] & 0xFF);
			} else {
				r++;
			}
		} else {
			while (r > 15) {
				emit(ehCodes[0xF0], ehSizes[0xF0] & 0xFF);
				r -= 16;
			}
			if (acValue < 0) {
				int absACValue = acValue;
				if (absACValue < 0) absACValue = -absACValue;
				int nBits = NBitsTable[absACValue];
				int rs = r * 16 + nBits;
				emit(ehCodes[rs], ehSizes[rs] & 0xFF);
				emit(0xFFFFFF - absACValue, nBits);
			} else {
				int nBits = NBitsTable[acValue];
				int rs = r * 16 + nBits;
				emit(ehCodes[rs], ehSizes[rs] & 0xFF);
				emit(acValue, nBits);
			}
			r = 0;
		}
	}
}
void encodeDCCoefficients(int[] dataUnit, int iComp) {
	int[] sParams = scanHeader.componentParameters[iComp];
	JPEGHuffmanTable dcTable = dcHuffmanTables[sParams[DC]];
	int lastDC = precedingDCs[iComp];
	int dcValue = dataUnit[0];
	int diff = dcValue - lastDC;
	precedingDCs[iComp] = dcValue;
	if (diff < 0) {
		int absDiff = 0 - diff;
		int nBits = NBitsTable[absDiff];
		emit(dcTable.ehCodes[nBits], dcTable.ehCodeLengths[nBits]);
		emit(0xFFFFFF - absDiff, nBits);
	} else {
		int nBits = NBitsTable[diff];
		emit(dcTable.ehCodes[nBits], dcTable.ehCodeLengths[nBits]);
		if (nBits != 0) {
			emit(diff, nBits);
		}
	}
}
void encodeMCUAtXAndY(int xmcu, int ymcu) {
	int nComponentsInScan = scanHeader.getNumberOfImageComponents();
	dataUnit = new int[64];
	for (int iComp = 0; iComp < nComponentsInScan; iComp++) {
		int[] frameComponent = frameComponents[componentIds[iComp]];
		int hi = frameComponent[HI];
		int vi = frameComponent[VI];
		for (int ivi = 0; ivi < vi; ivi++) {
			for (int ihi = 0; ihi < hi; ihi++) {
				extractData(dataUnit, iComp, xmcu, ymcu, ihi, ivi);
				forwardDCT(dataUnit);
				quantizeData(dataUnit, iComp);
				encodeDCCoefficients(dataUnit, iComp);
				encodeACCoefficients(dataUnit, iComp);
			}
		}
	}
}
void encodeScan() {
	for (int ymcu = 0; ymcu < interleavedMcuRows; ymcu++) {
		for (int xmcu = 0; xmcu < interleavedMcuCols; xmcu++) {
			encodeMCUAtXAndY(xmcu, ymcu);
		}
	}
	if (currentBitCount != 0) {
		emitByte((byte)currentByte);
	}
	resetOutputBuffer();
}
void expandImageComponents() {
	for (int iComp = 0; iComp < nComponents; iComp++) {
		int[] frameComponent = frameComponents[componentIds[iComp]];
		int hi = frameComponent[HI];
		int vi = frameComponent[VI];
		int upH = maxH / hi;
		int upV = maxV / vi;
		if ((upH * upV) > 1) {
			byte[] component = imageComponents[iComp];
			int compWidth = frameComponent[CW];
			int compHeight = frameComponent[CH];
			int upCompWidth = compWidth * upH;
			int upCompHeight = compHeight * upV;
			ImageData src = new ImageData(compWidth, compHeight, 8, new PaletteData(RGB16), 4, component);
			ImageData dest = src.scaledTo(upCompWidth, upCompHeight);
			imageComponents[iComp] = dest.data;
		}
	}
}
int extendBy(int diff, int t) {
	if (diff < ExtendTest[t]) {
		return diff + ExtendOffset[t];
	} else {
		return diff;
	}
}
void extractData(int[] dataUnit, int iComp, int xmcu, int ymcu, int ihi, int ivi) {
	byte[] compImage = imageComponents[iComp];
	int[] frameComponent = frameComponents[componentIds[iComp]];
	int hi = frameComponent[HI];
	int vi = frameComponent[VI];
	int compWidth = frameComponent[CW];
	int srcIndex = ((ymcu * vi + ivi) * compWidth * DCTSIZE) + ((xmcu * hi + ihi) * DCTSIZE);
	int destIndex = 0;
	for (int i = 0; i < DCTSIZE; i++) {
		for (int col = 0; col < DCTSIZE; col++) {
			dataUnit[destIndex] = (compImage[srcIndex + col] & 0xFF) - 128;
			destIndex++;
		}
		srcIndex += compWidth;
	}
}
void forwardDCT(int[] dataUnit) {
	for (int row = 0; row < 8; row++) {
		int rIndex = row * DCTSIZE;
		int tmp0 = dataUnit[rIndex] + dataUnit[rIndex + 7];
		int tmp7 = dataUnit[rIndex] - dataUnit[rIndex + 7];
		int tmp1 = dataUnit[rIndex + 1] + dataUnit[rIndex + 6];
		int tmp6 = dataUnit[rIndex + 1] - dataUnit[rIndex + 6];
		int tmp2 = dataUnit[rIndex + 2] + dataUnit[rIndex + 5];
		int tmp5 = dataUnit[rIndex + 2] - dataUnit[rIndex + 5];
		int tmp3 = dataUnit[rIndex + 3] + dataUnit[rIndex + 4];
		int tmp4 = dataUnit[rIndex + 3] - dataUnit[rIndex + 4];

		/**
		 * Even part per LL&M figure 1 --- note that published figure 
		 * is faulty; rotator 'sqrt(2)*c1' should be 'sqrt(2)*c6'.
		 */
		int tmp10 = tmp0 + tmp3;
		int tmp13 = tmp0 - tmp3;
		int tmp11 = tmp1 + tmp2;
		int tmp12 = tmp1 - tmp2;

		dataUnit[rIndex] = (tmp10 + tmp11) * 4;
		dataUnit[rIndex + 4]  = (tmp10 - tmp11) * 4;

		int z1 = (tmp12 + tmp13) * FIX_0_541196100;
		int n = z1 + (tmp13 * FIX_0_765366865) + 1024;
		dataUnit[rIndex + 2] = n >> 11;
		if ((n < 0) && ((n & 0x07FF) != 0)) dataUnit[rIndex + 2]--;
		n = z1 + (tmp12 * (0 - FIX_1_847759065)) + 1024;
 		dataUnit[rIndex + 6] = n >> 11;
		if ((n < 0) && ((n & 0x07FF) != 0)) dataUnit[rIndex + 6]--;

		/**
		 * Odd part per figure 8 --- note paper omits factor of sqrt(2).
		 * cK represents cos(K*pi/16).
		 * i0..i3 in the paper are tmp4..tmp7 here.
		 */
		z1 = tmp4 + tmp7;
		int z2 = tmp5 + tmp6;
		int z3 = tmp4 + tmp6;
		int z4 = tmp5 + tmp7;
		int z5 = (z3 + z4) * FIX_1_175875602;	// sqrt(2) * c3

		tmp4 *= FIX_0_298631336;	// sqrt(2) * (-c1+c3+c5-c7)
		tmp5 *= FIX_2_053119869;	// sqrt(2) * ( c1+c3-c5+c7)
		tmp6 *= FIX_3_072711026;	// sqrt(2) * ( c1+c3+c5-c7)
		tmp7 *= FIX_1_501321110;	// sqrt(2) * ( c1+c3-c5-c7)
		z1 *= 0 - FIX_0_899976223;	// sqrt(2) * (c7-c3)
		z2 *= 0 - FIX_2_562915447;	// sqrt(2) * (-c1-c3)
		z3 *= 0 - FIX_1_961570560;	// sqrt(2) * (-c3-c5)
		z4 *= 0 - FIX_0_390180644;	// sqrt(2) * (c5-c3)

		z3 += z5;
		z4 += z5;

		n = tmp4 + z1 + z3 + 1024;
		dataUnit[rIndex + 7] = n >> 11;
		if ((n < 0) && ((n & 0x07FF) != 0)) dataUnit[rIndex + 7]--;
		n = tmp5 + z2 + z4 + 1024;
		dataUnit[rIndex + 5] = n >> 11;
		if ((n < 0) && ((n & 0x07FF) != 0)) dataUnit[rIndex + 5]--;
		n = tmp6 + z2 + z3 + 1024;
		dataUnit[rIndex + 3] = n >> 11;
		if ((n < 0) && ((n & 0x07FF) != 0)) dataUnit[rIndex + 3]--;
		n = tmp7 + z1 + z4 + 1024;
		dataUnit[rIndex + 1] = n >> 11;
		if ((n < 0) && ((n & 0x07FF) != 0)) dataUnit[rIndex + 1]--;
	}

	/**
	 * Pass 2: process columns.
	 * Note that we must descale the results by a factor of 8 == 2**3,
	 * and also undo the PASS1_BITS scaling.
	 */
	for (int col = 0; col < 8; col++) {
		int c0 = col;
		int c1 = col + 8;
		int c2 = col + 16;
		int c3 = col + 24;
		int c4 = col + 32;
		int c5 = col + 40;
		int c6 = col + 48;
		int c7 = col + 56;
		int tmp0 = dataUnit[c0] + dataUnit[c7];
		int tmp7 = dataUnit[c0] - dataUnit[c7];
		int tmp1 = dataUnit[c1] + dataUnit[c6];
		int tmp6 = dataUnit[c1] - dataUnit[c6];
		int tmp2 = dataUnit[c2] + dataUnit[c5];
		int tmp5 = dataUnit[c2] - dataUnit[c5];
		int tmp3 = dataUnit[c3] + dataUnit[c4];
		int tmp4 = dataUnit[c3] - dataUnit[c4];

		/**
		 * Even part per LL&M figure 1 --- note that published figure 
		 * is faulty; rotator 'sqrt(2)*c1' should be 'sqrt(2)*c6'.
		 */
		int tmp10 = tmp0 + tmp3;
		int tmp13 = tmp0 - tmp3;
		int tmp11 = tmp1 + tmp2;
		int tmp12 = tmp1 - tmp2;

		int n = tmp10 + tmp11 + 16;
		dataUnit[c0] = n >> 5;
		if ((n < 0) && ((n & 0x1F) != 0)) dataUnit[c0]--;
		n = tmp10 - tmp11 + 16;
		dataUnit[c4] = n >> 5;
		if ((n < 0) && ((n & 0x1F) != 0)) dataUnit[c4]--;

		int z1 = (tmp12 + tmp13) * FIX_0_541196100;
		n = z1 + (tmp13 * FIX_0_765366865) + 131072;
		dataUnit[c2] = n >> 18;
		if ((n < 0) && ((n & 0x3FFFF) != 0)) dataUnit[c2]--;
		n = z1 + (tmp12 * (0 - FIX_1_847759065)) + 131072;
		dataUnit[c6] = n >> 18;
		if ((n < 0) && ((n & 0x3FFFF) != 0)) dataUnit[c6]--;

		/**
		 * Odd part per figure 8 --- note paper omits factor of sqrt(2).
		 * cK represents cos(K*pi/16).
		 * i0..i3 in the paper are tmp4..tmp7 here.
		 */
		z1 = tmp4 + tmp7;
		int z2 = tmp5 + tmp6;
		int z3 = tmp4 + tmp6;
		int z4 = tmp5 + tmp7;
		int z5 = (z3 + z4) * FIX_1_175875602;	// sqrt(2) * c3

		tmp4 *= FIX_0_298631336;	// sqrt(2) * (-c1+c3+c5-c7)
		tmp5 *= FIX_2_053119869;	// sqrt(2) * ( c1+c3-c5+c7)
		tmp6 *= FIX_3_072711026;	// sqrt(2) * ( c1+c3+c5-c7)
		tmp7 *= FIX_1_501321110;	// sqrt(2) * ( c1+c3-c5-c7)
		z1 *= 0 - FIX_0_899976223;	// sqrt(2) * (c7-c3)
		z2 *= 0 - FIX_2_562915447;	// sqrt(2) * (-c1-c3)
		z3 *= 0 - FIX_1_961570560;	// sqrt(2) * (-c3-c5)
		z4 *= 0 - FIX_0_390180644;	// sqrt(2) * (c5-c3)

		z3 += z5;
		z4 += z5;

		n = tmp4 + z1 + z3 + 131072;
		dataUnit[c7] = n >> 18;
		if ((n < 0) && ((n & 0x3FFFF) != 0)) dataUnit[c7]--;
		n = tmp5 + z2 + z4 + 131072;
		dataUnit[c5] = n >> 18;
		if ((n < 0) && ((n & 0x3FFFF) != 0)) dataUnit[c5]--;
		n = tmp6 + z2 + z3 + 131072;
		dataUnit[c3] = n >> 18;
		if ((n < 0) && ((n & 0x3FFFF) != 0)) dataUnit[c3]--;
		n = tmp7 + z1 + z4 + 131072;
		dataUnit[c1] = n >> 18;
		if ((n < 0) && ((n & 0x3FFFF) != 0)) dataUnit[c1]--;
	}
}
void getAPP0() {
	JPEGAppn appn = new JPEGAppn(inputStream);
	if (!appn.verify()) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
}
void getCOM() {
	new JPEGComment(inputStream);
}
void getDAC() {
	new JPEGArithmeticConditioningTable(inputStream);
}
void getDHT() {
	JPEGHuffmanTable dht = new JPEGHuffmanTable(inputStream);
	if (!dht.verify()) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	if (acHuffmanTables == null) {
		acHuffmanTables = new JPEGHuffmanTable[4];
	}
	if (dcHuffmanTables == null) {
		dcHuffmanTables = new JPEGHuffmanTable[4];
	}
	JPEGHuffmanTable[] dhtTables = dht.getAllTables();
	for (int i = 0; i < dhtTables.length; i++) {
		JPEGHuffmanTable dhtTable = dhtTables[i];
		if (dhtTable.getTableClass() == 0) {
			dcHuffmanTables[dhtTable.getTableIdentifier()] = dhtTable;
		} else {
			acHuffmanTables[dhtTable.getTableIdentifier()] = dhtTable;
		}
	}
}
void getDNL() {
	new JPEGRestartInterval(inputStream);
}
void getDQT() {
	JPEGQuantizationTable dqt = new JPEGQuantizationTable(inputStream);
	int[][] currentTables = quantizationTables;
	if (currentTables == null) {
		currentTables = new int[4][];
	}
	int[] dqtTablesKeys = dqt.getQuantizationTablesKeys();
	int[][] dqtTablesValues = dqt.getQuantizationTablesValues();
	for (int i = 0; i < dqtTablesKeys.length; i++) {
		int index = dqtTablesKeys[i];
		currentTables[index] = dqtTablesValues[i];
	}
	quantizationTables = currentTables;
}
void getDRI() {
	JPEGRestartInterval dri = new JPEGRestartInterval(inputStream);
	if (!dri.verify()) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	restartInterval = dri.getRestartInterval();
}
void inverseDCT(int[] dataUnit) {
	for (int row = 0; row < 8; row++) {
		int rIndex = row * DCTSIZE;
		/**
		 * Due to quantization, we will usually find that many of the input
		 * coefficients are zero, especially the AC terms.  We can exploit this
		 * by short-circuiting the IDCT calculation for any row in which all
		 * the AC terms are zero.  In that case each output is equal to the
		 * DC coefficient (with scale factor as needed).
		 * With typical images and quantization tables, half or more of the
		 * row DCT calculations can be simplified this way.
		 */
		if (isZeroInRow(dataUnit, rIndex)) {
			int dcVal = dataUnit[rIndex] << 2;
			for (int i = rIndex + 7; i >= rIndex; i--) {
				dataUnit[i] = dcVal;
			}
		} else {
			/**
			 * Even part: reverse the even part of the forward DCT.
			 * The rotator is sqrt(2)*c(-6).
			 */
			int z2 = dataUnit[rIndex + 2];
			int z3 = dataUnit[rIndex + 6];
			int z1 = (z2 + z3) * FIX_0_541196100;
			int tmp2 = z1 + (z3 * (0 - FIX_1_847759065));
			int tmp3 = z1 + (z2 * FIX_0_765366865);
			int tmp0 = (dataUnit[rIndex] + dataUnit[rIndex + 4]) << 13;
			int tmp1 = (dataUnit[rIndex] - dataUnit[rIndex + 4]) << 13;
			int tmp10 = tmp0 + tmp3;
			int tmp13 = tmp0 - tmp3;
			int tmp11 = tmp1 + tmp2;
			int tmp12 = tmp1 - tmp2;
			/**
			 * Odd part per figure 8; the matrix is unitary and hence its
			 * transpose is its inverse. i0..i3 are y7,y5,y3,y1 respectively.
			 */
			tmp0 = dataUnit[rIndex + 7];
			tmp1 = dataUnit[rIndex + 5];
			tmp2 = dataUnit[rIndex + 3];
			tmp3 = dataUnit[rIndex + 1];
			z1 = tmp0 + tmp3;
			z2 = tmp1 + tmp2;
			z3 = tmp0 + tmp2;
			int z4 = tmp1 + tmp3;
			int z5 = (z3 + z4) * FIX_1_175875602; /* sqrt(2) * c3 */
			  
			tmp0 *= FIX_0_298631336;		/* sqrt(2) * (-c1+c3+c5-c7) */
			tmp1 *= FIX_2_053119869;		/* sqrt(2) * ( c1+c3-c5+c7) */
			tmp2 *= FIX_3_072711026;		/* sqrt(2) * ( c1+c3+c5-c7) */
			tmp3 *= FIX_1_501321110;		/* sqrt(2) * ( c1+c3-c5-c7) */
			z1 *= 0 - FIX_0_899976223;	/* sqrt(2) * (c7-c3) */
			z2 *= 0 - FIX_2_562915447;	/* sqrt(2) * (-c1-c3) */
			z3 *= 0 - FIX_1_961570560;	/* sqrt(2) * (-c3-c5) */
			z4 *= 0 - FIX_0_390180644;	/* sqrt(2) * (c5-c3) */

			z3 += z5;
			z4 += z5;
			tmp0 += z1 + z3;
			tmp1 += z2 + z4;
			tmp2 += z2 + z3;
			tmp3 += z1 + z4;

			dataUnit[rIndex] = (tmp10 + tmp3 + 1024) >> 11;
			dataUnit[rIndex + 7] = (tmp10 - tmp3 + 1024) >> 11;
			dataUnit[rIndex + 1] = (tmp11 + tmp2 + 1024) >> 11;
			dataUnit[rIndex + 6] = (tmp11 - tmp2 + 1024) >> 11;
			dataUnit[rIndex + 2] = (tmp12 + tmp1 + 1024) >> 11;
			dataUnit[rIndex + 5] = (tmp12 - tmp1 + 1024) >> 11;
			dataUnit[rIndex + 3] = (tmp13 + tmp0 + 1024) >> 11;
			dataUnit[rIndex + 4] = (tmp13 - tmp0 + 1024) >> 11;
		 }
	}
	/**
	 * Pass 2: process columns.
	 * Note that we must descale the results by a factor of 8 == 2**3,
	 * and also undo the PASS1_BITS scaling.
	 */
	for (int col = 0; col < 8; col++) {
		int c0 = col;
		int c1 = col + 8;
		int c2 = col + 16;
		int c3 = col + 24;
		int c4 = col + 32;
		int c5 = col + 40;
		int c6 = col + 48;
		int c7 = col + 56;
		if (isZeroInColumn(dataUnit, col)) {
			int dcVal = (dataUnit[c0] + 16) >> 5;
			dataUnit[c0] = dcVal;
			dataUnit[c1] = dcVal;
			dataUnit[c2] = dcVal;
			dataUnit[c3] = dcVal;
			dataUnit[c4] = dcVal;
			dataUnit[c5] = dcVal;
			dataUnit[c6] = dcVal;
			dataUnit[c7] = dcVal;
		} else {
			/**
			 * Even part: reverse the even part of the forward DCT.
			 * The rotator is sqrt(2)*c(-6).
			 */
			int z0 = dataUnit[c0];
			int z2 = dataUnit[c2];
			int z3 = dataUnit[c6];
			int z4 = dataUnit[c4];
			int z1 = (z2 + z3) * FIX_0_541196100;
			int tmp2 = z1 + (z3 * (0 - FIX_1_847759065));
			int tmp3 = z1 + (z2 * FIX_0_765366865);
			int tmp0 = (z0 + z4) << 13;
			int tmp1 = (z0 - z4) << 13;
			int tmp10 = tmp0 + tmp3;
			int tmp13 = tmp0 - tmp3;
			int tmp11 = tmp1 + tmp2;
			int tmp12 = tmp1 - tmp2;
			/**
			 * Odd part per figure 8; the matrix is unitary and hence its
			 * transpose is its inverse. i0..i3 are y7,y5,y3,y1 respectively.
			 */
			tmp0 = dataUnit[c7];
			tmp1 = dataUnit[c5];
			tmp2 = dataUnit[c3];
			tmp3 = dataUnit[c1];
			z1 = tmp0 + tmp3;
			z2 = tmp1 + tmp2;
			z3 = tmp0 + tmp2;
			z4 = tmp1 + tmp3;
			z0 = (z3 + z4) * FIX_1_175875602;	/* sqrt(2) * c3 */
			
			tmp0 *= FIX_0_298631336;		/* sqrt(2) * (-c1+c3+c5-c7) */
			tmp1 *= FIX_2_053119869;		/* sqrt(2) * ( c1+c3-c5+c7) */
			tmp2 *= FIX_3_072711026;		/* sqrt(2) * ( c1+c3+c5-c7) */
			tmp3 *= FIX_1_501321110;		/* sqrt(2) * ( c1+c3-c5-c7) */
			z1 *= 0 - FIX_0_899976223;	/* sqrt(2) * (c7-c3) */
			z2 *= 0 - FIX_2_562915447;	/* sqrt(2) * (-c1-c3) */
			z3 *= 0 - FIX_1_961570560;	/* sqrt(2) * (-c3-c5) */
			z4 *= 0 - FIX_0_390180644;	/* sqrt(2) * (c5-c3) */
			
			z3 += z0;
			z4 += z0;
			
			tmp0 += z1 + z3;
			tmp1 += z2 + z4;
			tmp2 += z2 + z3;
			tmp3 += z1 + z4;

			/* Final output stage: inputs are tmp10..tmp13, tmp0..tmp3 */
			dataUnit[c0] = (tmp10 + tmp3 + 131072) >> 18;
			dataUnit[c7] = (tmp10 - tmp3 + 131072) >> 18;
			dataUnit[c1] = (tmp11 + tmp2 + 131072) >> 18;
			dataUnit[c6] = (tmp11 - tmp2 + 131072) >> 18;
			dataUnit[c2] = (tmp12 + tmp1 + 131072) >> 18;
			dataUnit[c5] = (tmp12 - tmp1 + 131072) >> 18;
			dataUnit[c3] = (tmp13 + tmp0 + 131072) >> 18;
			dataUnit[c4] = (tmp13 - tmp0 + 131072) >> 18;
		}
	}
}
@Override
boolean isFileFormat(LEDataInputStream stream) {
	try {
		JPEGStartOfImage soi = new JPEGStartOfImage(stream);
		stream.unread(soi.reference);
		return soi.verify();  // we no longer check for appN
	} catch (Exception e) {
		return false;
	}
}
boolean isZeroInColumn(int[] dataUnit, int col) {
	return dataUnit[col + 8] == 0 && dataUnit[col + 16] == 0
			&& dataUnit[col + 24] == 0 && dataUnit[col + 32] == 0
			&& dataUnit[col + 40] == 0 && dataUnit[col + 48] == 0
			&& dataUnit[col + 56] == 0;
}
boolean isZeroInRow(int[] dataUnit, int rIndex) {
	return dataUnit[rIndex + 1] == 0 && dataUnit[rIndex + 2] == 0
			&& dataUnit[rIndex + 3] == 0 && dataUnit[rIndex + 4] == 0
			&& dataUnit[rIndex + 5] == 0 && dataUnit[rIndex + 6] == 0
			&& dataUnit[rIndex + 7] == 0;
}
@Override
ImageData[] loadFromByteStream() {
	//TEMPORARY CODE
	if (System.getProperty("org.eclipse.swt.internal.image.JPEGFileFormat_3.2") == null) {
		return JPEGDecoder.loadFromByteStream(inputStream, loader);
	}
	JPEGStartOfImage soi = new JPEGStartOfImage(inputStream);
	if (!soi.verify()) SWT.error(SWT.ERROR_INVALID_IMAGE);
	restartInterval = 0;

	/* Process the tables preceding the frame header. */
	processTables();
	
	/* Start of Frame. */
	frameHeader = new JPEGFrameHeader(inputStream);
	if (!frameHeader.verify()) SWT.error(SWT.ERROR_INVALID_IMAGE);
	imageWidth = frameHeader.getSamplesPerLine();
	imageHeight = frameHeader.getNumberOfLines();
	maxH = frameHeader.getMaxHFactor();
	maxV = frameHeader.getMaxVFactor();
	int mcuWidth = maxH * DCTSIZE;
	int mcuHeight = maxV * DCTSIZE;
	interleavedMcuCols = (imageWidth + mcuWidth - 1) / mcuWidth;
	interleavedMcuRows = (imageHeight + mcuHeight - 1) / mcuHeight;
	progressive = frameHeader.isProgressive();
	samplePrecision = frameHeader.getSamplePrecision();
	nComponents = frameHeader.getNumberOfImageComponents();
	frameComponents = frameHeader.componentParameters;
	componentIds = frameHeader.componentIdentifiers;
	imageComponents = new byte[nComponents][];
	if (progressive) {
		// Progressive jpeg: need to keep all of the data units.
		dataUnits = new int[nComponents][][];
	} else {
		// Sequential jpeg: only need one data unit.
		dataUnit = new int[8 * 8];
	}
	for (int i = 0; i < nComponents; i++) {
		int[] frameComponent = frameComponents[componentIds[i]];
		int bufferSize = frameComponent[CW] * frameComponent[CH];
		imageComponents[i] = new byte[bufferSize];
		if (progressive) {
			dataUnits[i] = new int[bufferSize][];
		}
	}

	/* Process the tables preceding the scan header. */
	processTables();
	
	/* Start of Scan. */
	scanHeader = new JPEGScanHeader(inputStream);
	if (!scanHeader.verify()) SWT.error(SWT.ERROR_INVALID_IMAGE);
	
	/* Process scan(s) and further tables until EOI. */
	int progressiveScanCount = 0;
	boolean done = false;
	while(!done) {
		resetInputBuffer();
		precedingDCs = new int[4];
		decodeScan();
		if (progressive && loader.hasListeners()) {
			ImageData imageData = createImageData();
			loader.notifyListeners(new ImageLoaderEvent(loader, imageData, progressiveScanCount, false));
			progressiveScanCount++;
		}

		/* Unread any buffered data before looking for tables again. */
		int delta = 512 - bufferCurrentPosition - 1;
		if (delta > 0) {
			byte[] unreadBuffer = new byte[delta];
			System.arraycopy(dataBuffer, bufferCurrentPosition + 1, unreadBuffer, 0, delta);
			try {
				inputStream.unread(unreadBuffer);
			} catch (IOException e) {
				SWT.error(SWT.ERROR_IO, e);
			}
		}
		
		/* Process the tables preceding the next scan header. */
		JPEGSegment jpegSegment = processTables();
		if (jpegSegment == null || jpegSegment.getSegmentMarker() == EOI) {
			done = true;
		} else {
			scanHeader = new JPEGScanHeader(inputStream);
			if (!scanHeader.verify()) SWT.error(SWT.ERROR_INVALID_IMAGE);
		}
	}
	
	if (progressive) {
		for (int ymcu = 0; ymcu < interleavedMcuRows; ymcu++) {
			for (int xmcu = 0; xmcu < interleavedMcuCols; xmcu++) {
				for (int iComp = 0; iComp < nComponents; iComp++) {
					int[] frameComponent = frameComponents[componentIds[iComp]];
					int hi = frameComponent[HI];
					int vi = frameComponent[VI];
					int compWidth = frameComponent[CW];
					for (int ivi = 0; ivi < vi; ivi++) {
						for (int ihi = 0; ihi < hi; ihi++) {
							int index = (ymcu * vi + ivi) * compWidth + xmcu * hi + ihi;
							dataUnit = dataUnits[iComp][index];
							dequantize(dataUnit, iComp);
							inverseDCT(dataUnit);
							storeData(dataUnit, iComp, xmcu, ymcu, hi, ihi, vi, ivi);
						}
					}
				}
			}
		}
		dataUnits = null; // release memory
	}
	ImageData imageData = createImageData();
	if (progressive && loader.hasListeners()) {
		loader.notifyListeners(new ImageLoaderEvent(loader, imageData, progressiveScanCount, true));
	}
	return new ImageData[] {imageData};
}
ImageData createImageData() {
	return ImageData.internal_new(
		imageWidth,
		imageHeight, 
		nComponents * samplePrecision,
		setUpPalette(),
		nComponents == 1 ? 4 : 1,
		decodeImageComponents(),
		0,
		null,
		null,
		-1,
		-1,
		SWT.IMAGE_JPEG,
		0,
		0,
		0,
		0);
}
int nextBit() {
	if (currentBitCount != 0) {
		currentBitCount--;
		currentByte *= 2;
		if (currentByte > 255) {
			currentByte -= 256;
			return 1;
		} else {
			return 0;
		}
	}
	bufferCurrentPosition++;
	if (bufferCurrentPosition >= 512) {
		resetInputBuffer();
		bufferCurrentPosition = 0;
	}
	currentByte = dataBuffer[bufferCurrentPosition] & 0xFF;
	currentBitCount = 8;
	byte nextByte;
	if (bufferCurrentPosition == 511) {
		resetInputBuffer();
		currentBitCount = 8;
		nextByte = dataBuffer[0];
	} else {
		nextByte = dataBuffer[bufferCurrentPosition + 1];
	}
	if (currentByte == 0xFF) {
		if (nextByte == 0) {
			bufferCurrentPosition ++;
			currentBitCount--;
			currentByte *= 2;
			if (currentByte > 255) {
				currentByte -= 256;
				return 1;
			} else {
				return 0;
			}
		} else {
			if ((nextByte & 0xFF) + 0xFF00 == DNL) {
				getDNL();
				return 0;
			} else {
				SWT.error(SWT.ERROR_INVALID_IMAGE);
				return 0;
			}
		}
	} else {
		currentBitCount--;
		currentByte *= 2;
		if (currentByte > 255) {
			currentByte -= 256;
			return 1;
		} else {
			return 0;
		}
	}
}
void processRestartInterval() {
	do {
		bufferCurrentPosition++;
		if (bufferCurrentPosition > 511) {
			resetInputBuffer();
			bufferCurrentPosition = 0;
		}
		currentByte = dataBuffer[bufferCurrentPosition] & 0xFF;
	} while (currentByte != 0xFF);
	while (currentByte == 0xFF) {
		bufferCurrentPosition++;
		if (bufferCurrentPosition > 511) {
			resetInputBuffer();
			bufferCurrentPosition = 0;
		}
		currentByte = dataBuffer[bufferCurrentPosition] & 0xFF;
	}
	if (currentByte != ((RST0 + nextRestartNumber) & 0xFF)) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	bufferCurrentPosition++;
	if (bufferCurrentPosition > 511) {
		resetInputBuffer();
		bufferCurrentPosition = 0;
	}
	currentByte = dataBuffer[bufferCurrentPosition] & 0xFF;
	currentBitCount = 8;
	restartsToGo = restartInterval;
	nextRestartNumber = (nextRestartNumber + 1) & 0x7;
	precedingDCs = new int[4];
	eobrun = 0;
}
/* Process all markers until a frame header, scan header, or EOI is found. */
JPEGSegment processTables() {
	while (true) {
		JPEGSegment jpegSegment = seekUnspecifiedMarker(inputStream);
		if (jpegSegment == null) return null;
		JPEGFrameHeader sof = new JPEGFrameHeader(jpegSegment.reference);
		if (sof.verify()) {
			return jpegSegment;
		}
		int marker = jpegSegment.getSegmentMarker();
		switch (marker) {
			case SOI: // there should only be one SOI per file
				SWT.error(SWT.ERROR_INVALID_IMAGE);
			case EOI:
			case SOS:
				return jpegSegment;
			case DQT:
				getDQT();
				break;
			case DHT:
				getDHT();
				break;
			case DAC:
				getDAC();
				break;
			case DRI:
				getDRI();
				break;
			case APP0:
				getAPP0();
				break;
			case COM:
				getCOM();
				break;
			default:
				skipSegmentFrom(inputStream);
			
		}
	}
}
void quantizeData(int[] dataUnit, int iComp) {
	int[] qTable = quantizationTables[frameComponents[componentIds[iComp]][TQI]];
	for (int i = 0; i < dataUnit.length; i++) {
		int zzIndex = ZigZag8x8[i];
		int data = dataUnit[zzIndex];
		int absData = data < 0 ? 0 - data : data;
		int qValue = qTable[i];
		int q2 = qValue >> 1;
		absData += q2;
		if (absData < qValue) {
			dataUnit[zzIndex] = 0;
		} else {
			absData /= qValue;
			if (data >= 0) {
				dataUnit[zzIndex] = absData;
			} else {
				dataUnit[zzIndex] = 0 - absData;
			}
		}
	}
}
int receive(int nBits) {
	int v = 0;
	for (int i = 0; i < nBits; i++) {
		v = v * 2 + nextBit();
	}
	return v;
}
void resetInputBuffer() {
	if (dataBuffer == null) {
		dataBuffer = new byte[512];
	}
	try {
		inputStream.read(dataBuffer);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	currentBitCount = 0;
	bufferCurrentPosition = -1;
}
void resetOutputBuffer() {
	if (dataBuffer == null) {
		dataBuffer = new byte[512];
	} else {
		try {
			outputStream.write(dataBuffer, 0, bufferCurrentPosition);
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}
	bufferCurrentPosition = 0;
}
static JPEGSegment seekUnspecifiedMarker(LEDataInputStream byteStream) {
	byte[] byteArray = new byte[2];
	try {
		while (true) {
			if (byteStream.read(byteArray, 0, 1) != 1) return null;
			if (byteArray[0] == (byte) 0xFF) {
				if (byteStream.read(byteArray, 1, 1) != 1) return null;
				if (byteArray[1] != (byte) 0xFF && byteArray[1] != 0) {
					byteStream.unread(byteArray);
					return new JPEGSegment(byteArray);
				}
			}
		}
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	return null;
}
PaletteData setUpPalette() {
	if (nComponents == 1) {
		RGB[] entries = new RGB[256];
		for (int i = 0; i < 256; i++) {
			entries[i] = new RGB(i, i, i);
		}
		return new PaletteData(entries);
	}
	return new PaletteData(0xFF, 0xFF00, 0xFF0000);
}
static void skipSegmentFrom(LEDataInputStream byteStream) {
	try {
		byte[] byteArray = new byte[4];
		JPEGSegment jpegSegment = new JPEGSegment(byteArray);
	
		if (byteStream.read(byteArray) != byteArray.length) {
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		}
		if (!(byteArray[0] == -1 && byteArray[1] != 0 && byteArray[1] != -1)) {
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		}
		int delta = jpegSegment.getSegmentLength() - 2;
		byteStream.skip(delta);
	} catch (Exception e) {
		SWT.error(SWT.ERROR_IO, e);
	}
}
void storeData(int[] dataUnit, int iComp, int xmcu, int ymcu, int hi, int ihi, int vi, int ivi) {
	byte[] compImage = imageComponents[iComp];
	int[] frameComponent = frameComponents[componentIds[iComp]];
	int compWidth = frameComponent[CW];
	int destIndex = ((ymcu * vi + ivi) * compWidth * DCTSIZE) + ((xmcu * hi + ihi) * DCTSIZE);
	int srcIndex = 0;
	for (int i = 0; i < DCTSIZE; i++) {
		for (int col = 0; col < DCTSIZE; col++) {
			int x = dataUnit[srcIndex] + 128;
			if (x < 0) {
				x = 0;
			} else {
				if (x > 255) x = 255;
			}
			compImage[destIndex + col] = (byte)x;
			srcIndex++;
		}
		destIndex += compWidth;
	}
}
@Override
void unloadIntoByteStream(ImageLoader loader) {
	ImageData image = loader.data[0];
	if (!new JPEGStartOfImage().writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
	JPEGAppn appn = new JPEGAppn(new byte[] {(byte)0xFF, (byte)0xE0, 0, 0x10, 0x4A, 0x46, 0x49, 0x46, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0});
	if (!appn.writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
	quantizationTables = new int[4][];
	JPEGQuantizationTable chromDQT = JPEGQuantizationTable.defaultChrominanceTable();
	int encoderQFactor = loader.compression >= 1 && loader.compression <= 100 ? loader.compression : 75;
	chromDQT.scaleBy(encoderQFactor);
	int[] jpegDQTKeys = chromDQT.getQuantizationTablesKeys();
	int[][] jpegDQTValues = chromDQT.getQuantizationTablesValues();
	for (int i = 0; i < jpegDQTKeys.length; i++) {
		quantizationTables[jpegDQTKeys[i]] = jpegDQTValues[i];
	}
	JPEGQuantizationTable lumDQT = JPEGQuantizationTable.defaultLuminanceTable();
	lumDQT.scaleBy(encoderQFactor);
	jpegDQTKeys = lumDQT.getQuantizationTablesKeys();
	jpegDQTValues = lumDQT.getQuantizationTablesValues();
	for (int i = 0; i < jpegDQTKeys.length; i++) {
		quantizationTables[jpegDQTKeys[i]] = jpegDQTValues[i];
	}
	if (!lumDQT.writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
	if (!chromDQT.writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
	int frameLength, scanLength, precision;
	int[][] frameParams, scanParams;
	if (image.depth == 1) {
		frameLength = 11;
		frameParams = new int[1][];
		frameParams[0] = new int[] {1, 1, 1, 0, 0};
		scanParams = new int[1][];
		scanParams[0] = new int[] {0, 0};
		scanLength = 8;
		nComponents = 1;
		precision = 1;
	} else {
		frameLength = 17;
		frameParams = new int[3][];
		frameParams[0] = new int[] {0, 2, 2, 0, 0};
		frameParams[1] = new int[] {1, 1, 1, 0, 0};
		frameParams[2] = new int[] {1, 1, 1, 0, 0};
		scanParams = new int[3][];
		scanParams[0] = new int[] {0, 0};
		scanParams[1] = new int[] {1, 1};
		scanParams[2] = new int[] {1, 1};
		scanLength = 12;
		nComponents = 3;
		precision = 8;
	}
	imageWidth = image.width;
	imageHeight = image.height;
	frameHeader = new JPEGFrameHeader(new byte[19]);
	frameHeader.setSegmentMarker(SOF0);
	frameHeader.setSegmentLength(frameLength);
	frameHeader.setSamplePrecision(precision);
	frameHeader.setSamplesPerLine(imageWidth);
	frameHeader.setNumberOfLines(imageHeight);
	frameHeader.setNumberOfImageComponents(nComponents);
	frameHeader.componentParameters = frameParams;
	frameHeader.componentIdentifiers = new int[] {0, 1, 2};
	frameHeader.initializeContents();
	if (!frameHeader.writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
	frameComponents = frameParams;
	componentIds = frameHeader.componentIdentifiers;
	maxH = frameHeader.getMaxHFactor();
	maxV = frameHeader.getMaxVFactor();
	int mcuWidth = maxH * DCTSIZE;
	int mcuHeight = maxV * DCTSIZE;
	interleavedMcuCols = (imageWidth + mcuWidth - 1) / mcuWidth;
	interleavedMcuRows = (imageHeight + mcuHeight - 1) / mcuHeight;
	acHuffmanTables = new JPEGHuffmanTable[4];
	dcHuffmanTables = new JPEGHuffmanTable[4];
	JPEGHuffmanTable[] dhtTables = new JPEGHuffmanTable[] {
		JPEGHuffmanTable.getDefaultDCLuminanceTable(),
		JPEGHuffmanTable.getDefaultDCChrominanceTable(),
		JPEGHuffmanTable.getDefaultACLuminanceTable(),
		JPEGHuffmanTable.getDefaultACChrominanceTable()
	};
	for (int i = 0; i < dhtTables.length; i++) {
		JPEGHuffmanTable dhtTable = dhtTables[i];
		if (!dhtTable.writeToStream(outputStream)) {
			SWT.error(SWT.ERROR_IO);
		}
		JPEGHuffmanTable[] allTables = dhtTable.getAllTables();
		for (int j = 0; j < allTables.length; j++) {
			JPEGHuffmanTable huffmanTable = allTables[j];
			if (huffmanTable.getTableClass() == 0) {
				dcHuffmanTables[huffmanTable.getTableIdentifier()] = huffmanTable;
			} else {
				acHuffmanTables[huffmanTable.getTableIdentifier()] = huffmanTable;
			}
		}
	}
	precedingDCs = new int[4];
	scanHeader = new JPEGScanHeader(new byte[14]);
	scanHeader.setSegmentMarker(SOS);
	scanHeader.setSegmentLength(scanLength);
	scanHeader.setNumberOfImageComponents(nComponents);
	scanHeader.setStartOfSpectralSelection(0);
	scanHeader.setEndOfSpectralSelection(63);
	scanHeader.componentParameters = scanParams;
	scanHeader.initializeContents();
	if (!scanHeader.writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
	convertImageToYCbCr(image);
	resetOutputBuffer();
	currentByte = 0;
	currentBitCount = 0;
	encodeScan();
	if (!new JPEGEndOfImage().writeToStream(outputStream)) {
		SWT.error(SWT.ERROR_IO);
	}
}
}
