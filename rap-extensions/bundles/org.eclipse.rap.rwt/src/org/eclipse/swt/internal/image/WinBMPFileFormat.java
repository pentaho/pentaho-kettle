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


import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import java.io.*;

public final class WinBMPFileFormat extends FileFormat {
	static final int BMPFileHeaderSize = 14;
	static final int BMPHeaderFixedSize = 40;
	int importantColors;
	Point pelsPerMeter = new Point(0, 0);

/**
 * Compress numBytes bytes of image data from src, storing in dest
 * (starting at 0), using the technique specified by comp.
 * If last is true, this indicates the last line of the image.
 * Answer the size of the compressed data.
 */
int compress(int comp, byte[] src, int srcOffset, int numBytes, byte[] dest, boolean last) {
	if (comp == 1) { // BMP_RLE8_COMPRESSION
		return compressRLE8Data(src, srcOffset, numBytes, dest, last);
	}
	if (comp == 2) { // BMP_RLE4_COMPRESSION
		return compressRLE4Data(src, srcOffset, numBytes, dest, last);
	}
	SWT.error(SWT.ERROR_INVALID_IMAGE);
	return 0;
}
int compressRLE4Data(byte[] src, int srcOffset, int numBytes, byte[] dest, boolean last) {
	int sp = srcOffset, end = srcOffset + numBytes, dp = 0;
	int size = 0, left, i, n;
	byte theByte;
	while (sp < end) {
		/* find two consecutive bytes that are the same in the next 128 */
		left = end - sp - 1;
		if (left > 127)
			left = 127;
		for (n = 0; n < left; n++) {
			if (src[sp + n] == src[sp + n + 1])
				break;
		}
		/* if there is only one more byte in the scan line, include it */
		if (n < 127 && n == left)
			n++;
		/* store the intervening data */
		switch (n) {
			case 0:
				break;
			case 1: /* handled separately because 0,2 is a command */
				dest[dp] = 2; dp++; /* 1 byte == 2 pixels */
				dest[dp] = src[sp];
				dp++; sp++;
				size += 2;
				break;
			default:
				dest[dp] = 0; dp++;
				dest[dp] = (byte)(n + n); dp++; /* n bytes = n*2 pixels */
				for (i = n; i > 0; i--) {
					dest[dp] = src[sp];
					dp++; sp++;
				}
				size += 2 + n;
				if ((n & 1) != 0) { /* pad to word */
					dest[dp] = 0;
					dp++;
					size++;
				}
				break;
		}
		/* find the length of the next run (up to 127) and store it */
		left = end - sp;
		if (left > 0) {
			if (left > 127)
				left = 127;
			theByte = src[sp];
			for (n = 1; n < left; n++) {
				if (src[sp + n] != theByte)
					break;
			}
			dest[dp] = (byte)(n + n); dp++; /* n bytes = n*2 pixels */
			dest[dp] = theByte; dp++;
			sp += n;
			size += 2;
		}
	}

	/* store the end of line or end of bitmap codes */
	dest[dp] = 0; dp++;
	if (last) {
		dest[dp] = 1; dp++;
	} else {
		dest[dp] = 0; dp++;
	}
	size += 2;
	
	return size;
}
int compressRLE8Data(byte[] src, int srcOffset, int numBytes, byte[] dest, boolean last) {
	int sp = srcOffset, end = srcOffset + numBytes, dp = 0;
	int size = 0, left, i, n;
	byte theByte;
	while (sp < end) {
		/* find two consecutive bytes that are the same in the next 256 */
		left = end - sp - 1;
		if (left > 254)
			left = 254;
		for (n = 0; n < left; n++) {
			if (src[sp + n] == src[sp + n + 1])
				break;
		}
		/* if there is only one more byte in the scan line, include it */
		if (n == left)
			n++;
		/* store the intervening data */
		switch (n) {
			case 0:
				break;
			case 2: /* handled separately because 0,2 is a command */
				dest[dp] = 1; dp++;
				dest[dp] = src[sp];
				dp++; sp++;
				size += 2;
				/* don't break, fall through */
			case 1: /* handled separately because 0,1 is a command */
				dest[dp] = 1; dp++;
				dest[dp] = src[sp];
				dp++; sp++;
				size += 2;
				break;
			default:
				dest[dp] = 0; dp++;
				dest[dp] = (byte)n; dp++;
				for (i = n; i > 0; i--) {
					dest[dp] = src[sp];
					dp++; sp++;
				}
				size += 2 + n;
				if ((n & 1) != 0) { /* pad to word */
					dest[dp] = 0;
					dp++;
					size++;
				}
				break;
		}
		/* find the length of the next run (up to 255) and store it */
		left = end - sp;
		if (left > 0) {
			if (left > 255)
				left = 255;
			theByte = src[sp];
			for (n = 1; n < left; n++) {
				if (src[sp + n] != theByte)
					break;
			}
			dest[dp] = (byte)n; dp++;
			dest[dp] = theByte; dp++;
			sp += n;
			size += 2;
		}
	}

	/* store the end of line or end of bitmap codes */
	dest[dp] = 0; dp++;
	if (last) {
		dest[dp] = 1; dp++;
	} else {
		dest[dp] = 0; dp++;
	}
	size += 2;
	
	return size;
}
void convertPixelsToBGR(ImageData image, byte[] dest) {
	/*
	 * For direct palette, uncompressed image, BMP encoders expect the
	 * pixels to be in BGR format for 24 & 32 bit and RGB 1555 for 16 bit
	 * On Linux and MacOS, the pixels are in RGB format. Also, in
	 * MacOS, the alpha byte may be first. 
	 * Hence, we use the palette information of the image and convert 
	 * the pixels to the required format. Converted pixels are stored
	 * in dest byte array.
	 */
	byte[] data = image.data;
	PaletteData palette = image.palette;
	for (int y = 0; y < image.height; y++) {
		int index;
		int srcX = 0, srcY = y;
		int numOfBytes = image.depth / 8;
		index = (y * image.bytesPerLine);

		for (int i = 0; i < image.width; i++) {
			int pixel = 0;
			switch (image.depth) {
				case 32:
					pixel = ((data[index] & 0xFF) << 24)
							| ((data[index + 1] & 0xFF) << 16)
							| ((data[index + 2] & 0xFF) << 8)
							| (data[index + 3] & 0xFF);
					break;
				case 24:
					pixel = ((data[index] & 0xFF) << 16)
							| ((data[index + 1] & 0xFF) << 8)
							| (data[index + 2] & 0xFF);
					break;
				case 16:
					pixel = ((data[index + 1] & 0xFF) << 8)
							| (data[index] & 0xFF);
					break;
				default:
					SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
			}

			if (image.depth == 16) {
				/* convert to RGB 555 format */
				int r = pixel & palette.redMask;
				r = ((palette.redShift < 0) ? r >>> -palette.redShift
						: r << palette.redShift);

				int g = pixel & palette.greenMask;
				g = ((palette.greenShift < 0) ? g >>> -palette.greenShift
						: g << palette.greenShift);
				g = (g & 0xF8);	/* In 565 format, G is 6 bit, mask it to 5 bit */

				int b = pixel & palette.blueMask;
				b = ((palette.blueShift < 0) ? b >>> -palette.blueShift
						: b << palette.blueShift);

				int modPixel = (r << 7) | (g << 2) | (b >> 3);
				dest[index] = (byte) (modPixel & 0xFF);
				dest[index + 1] = (byte) ((modPixel >> 8) & 0xFF);
			} else {
				/* convert to BGR format */
				int b = pixel & palette.blueMask;
				dest[index] = (byte) ((palette.blueShift < 0) ? b >>> -palette.blueShift
						: b << palette.blueShift);

				int g = pixel & palette.greenMask;
				dest[index + 1] = (byte) ((palette.greenShift < 0) ? g >>> -palette.greenShift
						: g << palette.greenShift);

				int r = pixel & palette.redMask;
				dest[index + 2] = (byte) ((palette.redShift < 0) ? r >>> -palette.redShift
						: r << palette.redShift);

				if (numOfBytes == 4) dest[index + 3] = 0;
			}

			srcX++;
			if (srcX >= image.width) {
				srcY++;
				index = srcY * image.bytesPerLine;
				srcX = 0;
			} else {
				index += numOfBytes;
			}
		}
	}
}
void decompressData(byte[] src, byte[] dest, int stride, int cmp) {
	if (cmp == 1) { // BMP_RLE8_COMPRESSION
		if (decompressRLE8Data(src, src.length, stride, dest, dest.length) <= 0)
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		return;
	}
	if (cmp == 2) { // BMP_RLE4_COMPRESSION
		if (decompressRLE4Data(src, src.length, stride, dest, dest.length) <= 0)
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		return;
	}
	SWT.error(SWT.ERROR_INVALID_IMAGE);
}
int decompressRLE4Data(byte[] src, int numBytes, int stride, byte[] dest, int destSize) {
	int sp = 0;
	int se = numBytes;
	int dp = 0;
	int de = destSize;
	int x = 0, y = 0;
	while (sp < se) {
		int len = src[sp] & 0xFF;
		sp++;
		if (len == 0) {
			len = src[sp] & 0xFF;
			sp++;
			switch (len) {
				case 0: /* end of line */
					y++;
					x = 0;
					dp = y * stride;
					if (dp > de)
						return -1;
					break;
				case 1: /* end of bitmap */
					return 1;
				case 2: /* delta */
					x += src[sp] & 0xFF;
					sp++;
					y += src[sp] & 0xFF;
					sp++;
					dp = y * stride + x / 2;
					if (dp > de)
						return -1;
					break;
				default: /* absolute mode run */
					if ((len & 1) != 0) /* odd run lengths not currently supported */
						return -1;
					x += len;
					len = len / 2;
					if (len > (se - sp))
						return -1;
					if (len > (de - dp))
						return -1;
					for (int i = 0; i < len; i++) {
						dest[dp] = src[sp];
						dp++;
						sp++;
					}
					if ((sp & 1) != 0)
						sp++; /* word align sp? */
					break;
			}
		} else {
			if ((len & 1) != 0)
				return -1;
			x += len;
			len = len / 2;
			byte theByte = src[sp];
			sp++;
			if (len > (de - dp))
				return -1;
			for (int i = 0; i < len; i++) {
				dest[dp] = theByte;
				dp++;
			}
		}
	}
	return 1;
}
int decompressRLE8Data(byte[] src, int numBytes, int stride, byte[] dest, int destSize) {
	int sp = 0;
	int se = numBytes;
	int dp = 0;
	int de = destSize;
	int x = 0, y = 0;
	while (sp < se) {
		int len = src[sp] & 0xFF;
		sp++;
		if (len == 0) {
			len = src[sp] & 0xFF;
			sp++;
			switch (len) {
				case 0: /* end of line */
					y++;
					x = 0;
					dp = y * stride;
					if (dp > de)
						return -1;
					break;
				case 1: /* end of bitmap */
					return 1;
				case 2: /* delta */
					x += src[sp] & 0xFF;
					sp++;
					y += src[sp] & 0xFF;
					sp++;
					dp = y * stride + x;
					if (dp > de)
						return -1;
					break;
				default: /* absolute mode run */
					if (len > (se - sp))
						return -1;
					if (len > (de - dp))
						return -1;
					for (int i = 0; i < len; i++) {
						dest[dp] = src[sp];
						dp++;
						sp++;
					}
					if ((sp & 1) != 0)
						sp++; /* word align sp? */
					x += len;
					break;
			}
		} else {
			byte theByte = src[sp];
			sp++;
			if (len > (de - dp))
				return -1;
			for (int i = 0; i < len; i++) {
				dest[dp] = theByte;
				dp++;
			}
			x += len;
		}
	}
	return 1;
}
@Override
boolean isFileFormat(LEDataInputStream stream) {
	try {
		byte[] header = new byte[18];
		stream.read(header);
		stream.unread(header);
		int infoHeaderSize = (header[14] & 0xFF) | ((header[15] & 0xFF) << 8) | ((header[16] & 0xFF) << 16) | ((header[17] & 0xFF) << 24);
		return header[0] == 0x42 && header[1] == 0x4D && infoHeaderSize >= BMPHeaderFixedSize;
	} catch (Exception e) {
		return false;
	}
}
boolean isPaletteBMP(PaletteData pal, int depth) {
	switch(depth) {
		case 32:
		    if ((pal.redMask == 0xFF00) && (pal.greenMask == 0xFF0000) && (pal.blueMask == 0xFF000000)) return true;
		    return false;
		case 24:
			if ((pal.redMask == 0xFF) && (pal.greenMask == 0xFF00) && (pal.blueMask == 0xFF0000)) return true;
			return false;
		case 16:
		    if ((pal.redMask == 0x7C00) && (pal.greenMask == 0x3E0) && (pal.blueMask == 0x1F)) return true;
		    return false;
		default:
			return true;
	}
}
byte[] loadData(byte[] infoHeader) {
	int width = (infoHeader[4] & 0xFF) | ((infoHeader[5] & 0xFF) << 8) | ((infoHeader[6] & 0xFF) << 16) | ((infoHeader[7] & 0xFF) << 24);
	int height = (infoHeader[8] & 0xFF) | ((infoHeader[9] & 0xFF) << 8) | ((infoHeader[10] & 0xFF) << 16) | ((infoHeader[11] & 0xFF) << 24);
	int bitCount = (infoHeader[14] & 0xFF) | ((infoHeader[15] & 0xFF) << 8);
	int stride = (width * bitCount + 7) / 8;
	stride = (stride + 3) / 4 * 4; // Round up to 4 byte multiple
	byte[] data = loadData(infoHeader, stride);
	flipScanLines(data, stride, height);
	return data;
}
byte[] loadData(byte[] infoHeader, int stride) {
	int height = (infoHeader[8] & 0xFF) | ((infoHeader[9] & 0xFF) << 8) | ((infoHeader[10] & 0xFF) << 16) | ((infoHeader[11] & 0xFF) << 24);
	if (height < 0) height = -height;
	int dataSize = height * stride;
	byte[] data = new byte[dataSize];
	int cmp = (infoHeader[16] & 0xFF) | ((infoHeader[17] & 0xFF) << 8) | ((infoHeader[18] & 0xFF) << 16) | ((infoHeader[19] & 0xFF) << 24);
	if (cmp == 0 || cmp == 3) { // BMP_NO_COMPRESSION
		try {
			if (inputStream.read(data) != dataSize)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	} else {
		int compressedSize = (infoHeader[20] & 0xFF) | ((infoHeader[21] & 0xFF) << 8) | ((infoHeader[22] & 0xFF) << 16) | ((infoHeader[23] & 0xFF) << 24);
		byte[] compressed = new byte[compressedSize];
		try {
			if (inputStream.read(compressed) != compressedSize)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
		decompressData(compressed, data, stride, cmp);
	}
	return data;
}
int[] loadFileHeader() {
	int[] header = new int[5];
	try {
		header[0] = inputStream.readShort();
		header[1] = inputStream.readInt();
		header[2] = inputStream.readShort();
		header[3] = inputStream.readShort();
		header[4] = inputStream.readInt();
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	if (header[0] != 0x4D42)
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	return header;
}
@Override
ImageData[] loadFromByteStream() {
	int[] fileHeader = loadFileHeader();
	byte[] infoHeader = new byte[BMPHeaderFixedSize];
	try {
		inputStream.read(infoHeader);
	} catch (Exception e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	int width = (infoHeader[4] & 0xFF) | ((infoHeader[5] & 0xFF) << 8) | ((infoHeader[6] & 0xFF) << 16) | ((infoHeader[7] & 0xFF) << 24);
	int height = (infoHeader[8] & 0xFF) | ((infoHeader[9] & 0xFF) << 8) | ((infoHeader[10] & 0xFF) << 16) | ((infoHeader[11] & 0xFF) << 24);
	if (height < 0) height = -height;
	int bitCount = (infoHeader[14] & 0xFF) | ((infoHeader[15] & 0xFF) << 8);
	this.compression = (infoHeader[16] & 0xFF) | ((infoHeader[17] & 0xFF) << 8) | ((infoHeader[18] & 0xFF) << 16) | ((infoHeader[19] & 0xFF) << 24);
	PaletteData palette = loadPalette(infoHeader);
	if (inputStream.getPosition() < fileHeader[4]) {
		// Seek to the specified offset
		try {
			inputStream.skip(fileHeader[4] - inputStream.getPosition());
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}
	byte[] data = loadData(infoHeader);
	this.importantColors = (infoHeader[36] & 0xFF) | ((infoHeader[37] & 0xFF) << 8) | ((infoHeader[38] & 0xFF) << 16) | ((infoHeader[39] & 0xFF) << 24);
	int xPelsPerMeter = (infoHeader[24] & 0xFF) | ((infoHeader[25] & 0xFF) << 8) | ((infoHeader[26] & 0xFF) << 16) | ((infoHeader[27] & 0xFF) << 24);
	int yPelsPerMeter = (infoHeader[28] & 0xFF) | ((infoHeader[29] & 0xFF) << 8) | ((infoHeader[30] & 0xFF) << 16) | ((infoHeader[31] & 0xFF) << 24);
	this.pelsPerMeter = new Point(xPelsPerMeter, yPelsPerMeter);
	int type = (this.compression == 1 /*BMP_RLE8_COMPRESSION*/) || (this.compression == 2 /*BMP_RLE4_COMPRESSION*/) ? SWT.IMAGE_BMP_RLE : SWT.IMAGE_BMP;
	return new ImageData[] {
		ImageData.internal_new(
			width,
			height,
			bitCount,
			palette,
			4,
			data,
			0,
			null,
			null,
			-1,
			-1,
			type,
			0,
			0,
			0,
			0)
	};
}
PaletteData loadPalette(byte[] infoHeader) {
	int depth = (infoHeader[14] & 0xFF) | ((infoHeader[15] & 0xFF) << 8);
	if (depth <= 8) {
		int numColors = (infoHeader[32] & 0xFF) | ((infoHeader[33] & 0xFF) << 8) | ((infoHeader[34] & 0xFF) << 16) | ((infoHeader[35] & 0xFF) << 24);
		if (numColors == 0) {
			numColors = 1 << depth;
		} else {
			if (numColors > 256)
				numColors = 256;
		}
		byte[] buf = new byte[numColors * 4];
		try {
			if (inputStream.read(buf) != buf.length)
				SWT.error(SWT.ERROR_INVALID_IMAGE);
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
		return paletteFromBytes(buf, numColors);
	}
	if (depth == 16) {
		if (this.compression == 3) {
			try {
				return new PaletteData(inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
			} catch (IOException e) {
				SWT.error(SWT.ERROR_IO, e);
			}
		}
		return new PaletteData(0x7C00, 0x3E0, 0x1F);
	}
	if (depth == 24) return new PaletteData(0xFF, 0xFF00, 0xFF0000);
	if (this.compression == 3) {
		try {
			return new PaletteData(inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}
	return new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
}
PaletteData paletteFromBytes(byte[] bytes, int numColors) {
	int bytesOffset = 0;
	RGB[] colors = new RGB[numColors];
	for (int i = 0; i < numColors; i++) {
		colors[i] = new RGB(bytes[bytesOffset + 2] & 0xFF,
			bytes[bytesOffset + 1] & 0xFF,
			bytes[bytesOffset] & 0xFF);
		bytesOffset += 4;
	}
	return new PaletteData(colors);
}
/**
 * Answer a byte array containing the BMP representation of
 * the given device independent palette.
 */
static byte[] paletteToBytes(PaletteData pal) {
	int n = pal.colors == null ? 0 : (pal.colors.length < 256 ? pal.colors.length : 256);
	byte[] bytes = new byte[n * 4];
	int offset = 0;
	for (int i = 0; i < n; i++) {
		RGB col = pal.colors[i];
		bytes[offset] = (byte)col.blue;
		bytes[offset + 1] = (byte)col.green;
		bytes[offset + 2] = (byte)col.red;
		offset += 4;
	}
	return bytes;
}
/**
 * Unload the given image's data into the given byte stream
 * using the given compression strategy. 
 * Answer the number of bytes written.
 * Method modified to use the passed data if it is not null.
 */
int unloadData(ImageData image, byte[] data, OutputStream out, int comp) {
	int totalSize = 0;
	try {
		if (comp == 0)
			return unloadDataNoCompression(image, data, out);
		int bpl = (image.width * image.depth + 7) / 8;
		int bmpBpl = (bpl + 3) / 4 * 4; // BMP pads scanlines to multiples of 4 bytes
		int imageBpl = image.bytesPerLine;
		// Compression can actually take twice as much space, in worst case
		byte[] buf = new byte[bmpBpl * 2];
		int srcOffset = imageBpl * (image.height - 1); // Start at last line
		if (data == null) data = image.data;
		totalSize = 0;
		byte[] buf2 = new byte[32768];
		int buf2Offset = 0;
		for (int y = image.height - 1; y >= 0; y--) {
			int lineSize = compress(comp, data, srcOffset, bpl, buf, y == 0);
			if (buf2Offset + lineSize > buf2.length) {
				out.write(buf2, 0, buf2Offset);
				buf2Offset = 0;
			}
			System.arraycopy(buf, 0, buf2, buf2Offset, lineSize);
			buf2Offset += lineSize;
			totalSize += lineSize;
			srcOffset -= imageBpl;
		}
		if (buf2Offset > 0)
			out.write(buf2, 0, buf2Offset);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	return totalSize;
}
/**
 * Prepare the given image's data for unloading into a byte stream
 * using no compression strategy.
 * Answer the number of bytes written.
 * Method modified to use the passed data if it is not null.
 */
int unloadDataNoCompression(ImageData image, byte[] data, OutputStream out) {
	int bmpBpl = 0;
	try {
		int bpl = (image.width * image.depth + 7) / 8;
		bmpBpl = (bpl + 3) / 4 * 4; // BMP pads scanlines to multiples of 4 bytes
		int linesPerBuf = 32678 / bmpBpl;
		byte[] buf = new byte[linesPerBuf * bmpBpl];
		if (data == null) data = image.data;
		int imageBpl = image.bytesPerLine;
		int dataIndex = imageBpl * (image.height - 1); // Start at last line
		if (image.depth == 16) {
			for (int y = 0; y < image.height; y += linesPerBuf) {
				int count = image.height - y;
				if (linesPerBuf < count) count = linesPerBuf;
				int bufOffset = 0;
				for (int i = 0; i < count; i++) {
					for (int wIndex = 0; wIndex < bpl; wIndex += 2) {
						buf[bufOffset + wIndex + 1] = data[dataIndex + wIndex + 1];
						buf[bufOffset + wIndex] = data[dataIndex + wIndex];
					}
					bufOffset += bmpBpl;
					dataIndex -= imageBpl;
				}
				out.write(buf, 0, bufOffset);
			}
		} else {
			for (int y = 0; y < image.height; y += linesPerBuf) {
				int tmp = image.height - y;
				int count = tmp < linesPerBuf ? tmp : linesPerBuf;
				int bufOffset = 0;
				for (int i = 0; i < count; i++) {
					System.arraycopy(data, dataIndex, buf, bufOffset, bpl);
					bufOffset += bmpBpl;
					dataIndex -= imageBpl;
				}
				out.write(buf, 0, bufOffset);
			}
		}
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	return bmpBpl * image.height;
}
/**
 * Unload a DeviceIndependentImage using Windows .BMP format into the given
 * byte stream.
 */
@Override
void unloadIntoByteStream(ImageLoader loader) {
	ImageData image = loader.data[0];
	byte[] rgbs;
	int numCols;
	if (!((image.depth == 1) || (image.depth == 4) || (image.depth == 8) ||
		  (image.depth == 16) || (image.depth == 24) || (image.depth == 32)))
			SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
	int comp = this.compression;
	if (!((comp == 0) || ((comp == 1) && (image.depth == 8)) ||
		  ((comp == 2) && (image.depth == 4))))
			SWT.error(SWT.ERROR_INVALID_IMAGE);
	PaletteData pal = image.palette;
	if ((image.depth == 16) || (image.depth == 24) || (image.depth == 32)) {
		if (!pal.isDirect)
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		numCols = 0;
		rgbs = null;
	} else {
		if (pal.isDirect)
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		numCols = pal.colors.length;
		rgbs = paletteToBytes(pal);
	}
	// Fill in file header, except for bfsize, which is done later.
	int headersSize = BMPFileHeaderSize + BMPHeaderFixedSize;
	int[] fileHeader = new int[5];
	fileHeader[0] = 0x4D42;	// Signature
	fileHeader[1] = 0; // File size - filled in later
	fileHeader[2] = 0; // Reserved 1
	fileHeader[3] = 0; // Reserved 2
	fileHeader[4] = headersSize; // Offset to data
	if (rgbs != null) {
		fileHeader[4] += rgbs.length;
	}

	byte iData[] = null;
	// If the pixels are not in the expected BMP format, convert them.
	if (pal.isDirect && !isPaletteBMP(pal, image.depth)) {
		// array to store the converted pixels
	    iData = new byte[image.data.length];
		convertPixelsToBGR(image, iData);
	}
	
	// Prepare data. This is done first so we don't have to try to rewind
	// the stream and fill in the details later.
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	unloadData(image, iData, out, comp);
	byte[] data = out.toByteArray();
	
	// Calculate file size
	fileHeader[1] = fileHeader[4] + data.length;

	// Write the headers
	try {
		outputStream.writeShort(fileHeader[0]);
		outputStream.writeInt(fileHeader[1]);
		outputStream.writeShort(fileHeader[2]);
		outputStream.writeShort(fileHeader[3]);
		outputStream.writeInt(fileHeader[4]);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	try {
		outputStream.writeInt(BMPHeaderFixedSize);
		outputStream.writeInt(image.width);
		outputStream.writeInt(image.height);
		outputStream.writeShort(1);
		outputStream.writeShort((short)image.depth);
		outputStream.writeInt(comp);
		outputStream.writeInt(data.length);
		outputStream.writeInt(pelsPerMeter.x);
		outputStream.writeInt(pelsPerMeter.y);
		outputStream.writeInt(numCols);
		outputStream.writeInt(importantColors);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	
	// Unload palette
	if (numCols > 0) {
		try {
			outputStream.write(rgbs);
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}

	// Unload the data
	try {
		outputStream.write(data);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
}
void flipScanLines(byte[] data, int stride, int height) {
	int i1 = 0;
	int i2 = (height - 1) * stride;
	for (int i = 0; i < height / 2; i++) {
		for (int index = 0; index < stride; index++) {
			byte b = data[index + i1];
			data[index + i1] = data[index + i2];
			data[index + i2] = b;
		}
		i1 += stride;
		i2 -= stride;
	}
}

}
