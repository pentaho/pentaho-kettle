/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

public final class WinICOFileFormat extends FileFormat {
	
byte[] bitInvertData(byte[] data, int startIndex, int endIndex) {
	// Destructively bit invert data in the given byte array.
	for (int i = startIndex; i < endIndex; i++) {
		data[i] = (byte)(255 - data[i - startIndex]);
	}
	return data;
}

static final byte[] convertPad(byte[] data, int width, int height, int depth, int pad, int newPad) {
	if (pad == newPad) return data;
	int stride = (width * depth + 7) / 8;
	int bpl = (stride + (pad - 1)) / pad * pad;
	int newBpl = (stride + (newPad - 1)) / newPad * newPad;
	byte[] newData = new byte[height * newBpl];
	int srcIndex = 0, destIndex = 0;
	for (int y = 0; y < height; y++) {
		System.arraycopy(data, srcIndex, newData, destIndex, newBpl);
		srcIndex += bpl;
		destIndex += newBpl;
	}
	return newData;
}
/**
 * Answer the size in bytes of the file representation of the given
 * icon
 */
int iconSize(ImageData i) {
	int shapeDataStride = (i.width * i.depth + 31) / 32 * 4;
	int maskDataStride = (i.width + 31) / 32 * 4;
	int dataSize = (shapeDataStride + maskDataStride) * i.height;
	int paletteSize = i.palette.colors != null ? i.palette.colors.length * 4 : 0;
	return WinBMPFileFormat.BMPHeaderFixedSize + paletteSize + dataSize;
}
@Override
boolean isFileFormat(LEDataInputStream stream) {
	try {
		byte[] header = new byte[4];
		stream.read(header);
		stream.unread(header);
		return header[0] == 0 && header[1] == 0 && header[2] == 1 && header[3] == 0;
	} catch (Exception e) {
		return false;
	}
}
boolean isValidIcon(ImageData i) {
	switch (i.depth) {
		case 1:
		case 4:
		case 8:
			if (i.palette.isDirect) return false;
			int size = i.palette.colors.length;
			return size == 2 || size == 16 || size == 32 || size == 256;
		case 24:
		case 32:
			return i.palette.isDirect;
	}
	return false;
}
int loadFileHeader(LEDataInputStream byteStream) {
	int[] fileHeader = new int[3];
	try {
		fileHeader[0] = byteStream.readShort();
		fileHeader[1] = byteStream.readShort();
		fileHeader[2] = byteStream.readShort();
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	if ((fileHeader[0] != 0) || (fileHeader[1] != 1))
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	int numIcons = fileHeader[2];
	if (numIcons <= 0)
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	return numIcons;
}
int loadFileHeader(LEDataInputStream byteStream, boolean hasHeader) {
	int[] fileHeader = new int[3];
	try {
		if (hasHeader) {
			fileHeader[0] = byteStream.readShort();
			fileHeader[1] = byteStream.readShort();
		} else {
			fileHeader[0] = 0;
			fileHeader[1] = 1;
		}
		fileHeader[2] = byteStream.readShort();
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	if ((fileHeader[0] != 0) || (fileHeader[1] != 1))
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	int numIcons = fileHeader[2];
	if (numIcons <= 0)
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	return numIcons;
}
@Override
ImageData[] loadFromByteStream() {
	int numIcons = loadFileHeader(inputStream);
	int[][] headers = loadIconHeaders(numIcons);
	ImageData[] icons = new ImageData[headers.length];
	for (int i = 0; i < icons.length; i++) {
		icons[i] = loadIcon(headers[i]);
	}
	return icons;
}
/**
 * Load one icon from the byte stream.
 */
ImageData loadIcon(int[] iconHeader) {
	try {
		FileFormat png = getFileFormat(inputStream, "PNG");
		if (png != null) {
			png.loader = this.loader;
			return png.loadFromStream(inputStream)[0];
		}
	} catch (Exception e) {
	}
	byte[] infoHeader = loadInfoHeader(iconHeader);
	WinBMPFileFormat bmpFormat = new WinBMPFileFormat();
	bmpFormat.inputStream = inputStream;
	PaletteData palette = bmpFormat.loadPalette(infoHeader);
	byte[] shapeData = bmpFormat.loadData(infoHeader);
	int width = (infoHeader[4] & 0xFF) | ((infoHeader[5] & 0xFF) << 8) | ((infoHeader[6] & 0xFF) << 16) | ((infoHeader[7] & 0xFF) << 24);
	int height = (infoHeader[8] & 0xFF) | ((infoHeader[9] & 0xFF) << 8) | ((infoHeader[10] & 0xFF) << 16) | ((infoHeader[11] & 0xFF) << 24);
	if (height < 0) height = -height;
	int depth = (infoHeader[14] & 0xFF) | ((infoHeader[15] & 0xFF) << 8);
	infoHeader[14] = 1;
	infoHeader[15] = 0;
	byte[] maskData = bmpFormat.loadData(infoHeader);
	maskData = convertPad(maskData, width, height, 1, 4, 2);
	bitInvertData(maskData, 0, maskData.length);
	return ImageData.internal_new(
		width,
		height,
		depth,
		palette,
		4,
		shapeData,
		2,
		maskData,
		null,
		-1,
		-1,
		SWT.IMAGE_ICO,
		0,
		0,
		0,
		0);
}
int[][] loadIconHeaders(int numIcons) {
	int[][] headers = new int[numIcons][7];
	try {
		for (int i = 0; i < numIcons; i++) {
			headers[i][0] = inputStream.read();
			headers[i][1] = inputStream.read();
			headers[i][2] = inputStream.readShort();
			headers[i][3] = inputStream.readShort();
			headers[i][4] = inputStream.readShort();
			headers[i][5] = inputStream.readInt();
			headers[i][6] = inputStream.readInt();
		}
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	return headers;
}
byte[] loadInfoHeader(int[] iconHeader) {
	int width = iconHeader[0];
	int height = iconHeader[1];
	int numColors = iconHeader[2]; // the number of colors is in the low byte, but the high byte must be 0
	if (numColors == 0) numColors = 256; // this is specified: '00' represents '256' (0x100) colors
	if ((numColors != 2) && (numColors != 8) && (numColors != 16) &&
		(numColors != 32) && (numColors != 256))
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	if (inputStream.getPosition() < iconHeader[6]) {
		// Seek to the specified offset
		try {
			inputStream.skip(iconHeader[6] - inputStream.getPosition());
		} catch (IOException e) {
			SWT.error(SWT.ERROR_IO, e);
			return null;
		}
	}
	byte[] infoHeader = new byte[WinBMPFileFormat.BMPHeaderFixedSize];
	try {
		inputStream.read(infoHeader);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	if (((infoHeader[12] & 0xFF) | ((infoHeader[13] & 0xFF) << 8)) != 1)
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	int infoWidth = (infoHeader[4] & 0xFF) | ((infoHeader[5] & 0xFF) << 8) | ((infoHeader[6] & 0xFF) << 16) | ((infoHeader[7] & 0xFF) << 24);
	int infoHeight = (infoHeader[8] & 0xFF) | ((infoHeader[9] & 0xFF) << 8) | ((infoHeader[10] & 0xFF) << 16) | ((infoHeader[11] & 0xFF) << 24);
	int bitCount = (infoHeader[14] & 0xFF) | ((infoHeader[15] & 0xFF) << 8);
	/*
	 * Feature in the ico spec. The spec says that a width/height of 0 represents 256, however, newer images can be created with even larger sizes. 
	 * Images with a width/height >= 256 will have their width/height set to 0 in the icon header; the fix for this case is to read the width/height 
	 * directly from the image header.
	 */
	if (width == 0) width = infoWidth;
	if (height == 0) height = infoHeight / 2;
	if (height == infoHeight && bitCount == 1) height /= 2;
	if (!((width == infoWidth) && (height * 2 == infoHeight) &&
		(bitCount == 1 || bitCount == 4 || bitCount == 8 || bitCount == 24 || bitCount == 32)))
			SWT.error(SWT.ERROR_INVALID_IMAGE);
	infoHeader[8] = (byte)(height & 0xFF);
	infoHeader[9] = (byte)((height >> 8) & 0xFF);
	infoHeader[10] = (byte)((height >> 16) & 0xFF);
	infoHeader[11] = (byte)((height >> 24) & 0xFF);
	return infoHeader;
}
/**
 * Unload a single icon
 */
void unloadIcon(ImageData icon) {
	int sizeImage = (((icon.width * icon.depth + 31) / 32 * 4) +
		((icon.width + 31) / 32 * 4)) * icon.height;
	try {
		outputStream.writeInt(WinBMPFileFormat.BMPHeaderFixedSize);
		outputStream.writeInt(icon.width);
		outputStream.writeInt(icon.height * 2);
		outputStream.writeShort(1);
		outputStream.writeShort((short)icon.depth);
		outputStream.writeInt(0);
		outputStream.writeInt(sizeImage);
		outputStream.writeInt(0);
		outputStream.writeInt(0);
		outputStream.writeInt(icon.palette.colors != null ? icon.palette.colors.length : 0);
		outputStream.writeInt(0);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	
	byte[] rgbs = WinBMPFileFormat.paletteToBytes(icon.palette);
	try {
		outputStream.write(rgbs);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	unloadShapeData(icon);
	unloadMaskData(icon);
}
/**
 * Unload the icon header for the given icon, calculating the offset.
 */
void unloadIconHeader(ImageData i) {
	int headerSize = 16;
	int offset = headerSize + 6;
	int iconSize = iconSize(i);
	try {
		outputStream.write(i.width);
		outputStream.write(i.height);
		outputStream.writeShort(i.palette.colors != null ? i.palette.colors.length : 0);
		outputStream.writeShort(0);
		outputStream.writeShort(0);
		outputStream.writeInt(iconSize);
		outputStream.writeInt(offset);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
}
@Override
void unloadIntoByteStream(ImageLoader loader) {
	/* We do not currently support writing multi-image ico,
	 * so we use the first image data in the loader's array. */
	ImageData image = loader.data[0];
	if (!isValidIcon(image))
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	try {
		outputStream.writeShort(0);
		outputStream.writeShort(1);
		outputStream.writeShort(1);
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	unloadIconHeader(image);
	unloadIcon(image);
}
/**
 * Unload the mask data for an icon. The data is flipped vertically
 * and inverted.
 */
void unloadMaskData(ImageData icon) {
	ImageData mask = icon.getTransparencyMask();
	int bpl = (icon.width + 7) / 8;
	int pad = mask.scanlinePad;
	int srcBpl = (bpl + pad - 1) / pad * pad;
	int destBpl = (bpl + 3) / 4 * 4;
	byte[] buf = new byte[destBpl];
	int offset = (icon.height - 1) * srcBpl;
	byte[] data = mask.data;
	try {
		for (int i = 0; i < icon.height; i++) {
			System.arraycopy(data, offset, buf, 0, bpl);
			bitInvertData(buf, 0, bpl);
			outputStream.write(buf, 0, destBpl);
			offset -= srcBpl;
		}
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
}
/**
 * Unload the shape data for an icon. The data is flipped vertically.
 */
void unloadShapeData(ImageData icon) {
	int bpl = (icon.width * icon.depth + 7) / 8;
	int pad = icon.scanlinePad;
	int srcBpl = (bpl + pad - 1) / pad * pad;
	int destBpl = (bpl + 3) / 4 * 4;
	byte[] buf = new byte[destBpl];
	int offset = (icon.height - 1) * srcBpl;
	byte[] data = icon.data;
	try {
		for (int i = 0; i < icon.height; i++) {
			System.arraycopy(data, offset, buf, 0, bpl);
			outputStream.write(buf, 0, destBpl);
			offset -= srcBpl;
		}
	} catch (IOException e) {
		SWT.error(SWT.ERROR_IO, e);
	}
}
}
