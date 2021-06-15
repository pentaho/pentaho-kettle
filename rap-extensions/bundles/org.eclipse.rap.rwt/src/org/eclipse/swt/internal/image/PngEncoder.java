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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.internal.Compatibility;

final class PngEncoder extends Object {

	static final byte SIGNATURE[] = {(byte) '\211', (byte) 'P', (byte) 'N', (byte) 'G', (byte) '\r', (byte) '\n', (byte) '\032', (byte) '\n'};
	static final byte TAG_IHDR[] = {(byte) 'I', (byte) 'H', (byte) 'D', (byte) 'R'};
	static final byte TAG_PLTE[] = {(byte) 'P', (byte) 'L', (byte) 'T', (byte) 'E'};
	static final byte TAG_TRNS[] = {(byte) 't', (byte) 'R', (byte) 'N', (byte) 'S'};
	static final byte TAG_IDAT[] = {(byte) 'I', (byte) 'D', (byte) 'A', (byte) 'T'};
	static final byte TAG_IEND[] = {(byte) 'I', (byte) 'E', (byte) 'N', (byte) 'D'};
	
	static final int NO_COMPRESSION = 0;
	static final int BEST_SPEED = 1;
	static final int BEST_COMPRESSION = 9;
	static final int DEFAULT_COMPRESSION = -1;
	
	ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
	PngChunk chunk;
	
	ImageLoader loader;
	ImageData data;
	int transparencyType;
	
	int width, height, bitDepth, colorType;
	
	int compressionMethod = 0;
	int filterMethod = 0;
	int interlaceMethod = 0;
	
public PngEncoder(ImageLoader loader) {

	this.loader = loader;
	this.data = loader.data[0];
	this.transparencyType = data.getTransparencyType();
	
	this.width = data.width;
	this.height = data.height;
	
	this.bitDepth = 8;
	
	this.colorType = 2;
	
	if (data.palette.isDirect) {
		if (transparencyType == SWT.TRANSPARENCY_ALPHA) {
			this.colorType = 6;
		}
	}
	else {
		this.colorType = 3;
	}
	
	if (!(colorType == 2 || colorType == 3 || colorType == 6)) SWT.error(SWT.ERROR_INVALID_IMAGE);

}

void writeShort(ByteArrayOutputStream baos, int theShort) {

	byte byte1 = (byte) ((theShort >> 8) & 0xff);
	byte byte2 = (byte) (theShort & 0xff);
	byte[] temp = {byte1, byte2};
	baos.write(temp, 0, 2);

}

void writeInt(ByteArrayOutputStream baos, int theInt) {

	byte byte1 = (byte) ((theInt >> 24) & 0xff);
	byte byte2 = (byte) ((theInt >> 16) & 0xff);
	byte byte3 = (byte) ((theInt >> 8) & 0xff);
	byte byte4 = (byte) (theInt & 0xff);
	byte[] temp = {byte1, byte2, byte3, byte4};
	baos.write(temp, 0, 4);

}

void writeChunk(byte[] tag, byte[] buffer) {

	int bufferLength = (buffer != null) ? buffer.length : 0;
	
	chunk = new PngChunk(bufferLength);
	
	writeInt(bytes, bufferLength);
	bytes.write(tag, 0, 4);
	chunk.setType(tag);
	if (bufferLength != 0) {
		bytes.write(buffer, 0, bufferLength);
		chunk.setData(buffer);
	}
	else {
		chunk.setCRC(chunk.computeCRC());
	}
	writeInt(bytes, chunk.getCRC());

}

void writeSignature() {

	bytes.write(SIGNATURE, 0, 8);

}

void writeHeader() {

	ByteArrayOutputStream baos = new ByteArrayOutputStream(13);
	
	writeInt(baos, width);
	writeInt(baos, height);
	baos.write(bitDepth);
	baos.write(colorType);
	baos.write(compressionMethod);
	baos.write(filterMethod);
	baos.write(interlaceMethod);
	
	writeChunk(TAG_IHDR, baos.toByteArray());

}

void writePalette() {

	RGB[] RGBs = data.palette.getRGBs();
	
	if (RGBs.length > 256) SWT.error(SWT.ERROR_INVALID_IMAGE);
	
	ByteArrayOutputStream baos = new ByteArrayOutputStream(RGBs.length);
	
	for (int i = 0; i < RGBs.length; i++) {
	
		baos.write((byte) RGBs[i].red);
		baos.write((byte) RGBs[i].green);
		baos.write((byte) RGBs[i].blue);
	
	}
	
	writeChunk(TAG_PLTE, baos.toByteArray());

}

void writeTransparency() {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	switch (transparencyType) {
	
		case SWT.TRANSPARENCY_ALPHA:
			
			int pixelValue, alphaValue;
			
			byte[] alphas = new byte[data.palette.getRGBs().length];
			
			for (int y = 0; y < height; y++) {
			
				for (int x = 0; x < width; x++) {
				
					pixelValue = data.getPixel(x, y);
					alphaValue = data.getAlpha(x, y);
					
					alphas[pixelValue] = (byte) alphaValue;
				
				}
			
			}
			
			baos.write(alphas, 0, alphas.length);
			
			break;
		
		case SWT.TRANSPARENCY_PIXEL:
			
			int pixel = data.transparentPixel;
			
			if (colorType == 2) {
			
				int redMask = data.palette.redMask;
				int redShift = data.palette.redShift;
				int greenMask = data.palette.greenMask;
				int greenShift = data.palette.greenShift;
				int blueShift = data.palette.blueShift;
				int blueMask = data.palette.blueMask;
				
				int r = pixel & redMask;
				r = (redShift < 0) ? r >>> -redShift : r << redShift;
				int g = pixel & greenMask;
				g = (greenShift < 0) ? g >>> -greenShift : g << greenShift;
				int b = pixel & blueMask;
				b = (blueShift < 0) ? b >>> -blueShift : b << blueShift;
				
				writeShort(baos, r);
				writeShort(baos, g);
				writeShort(baos, b);
			
			}
			
			if (colorType == 3) {
			
				byte[] padding = new byte[pixel + 1];
				
				for (int i = 0; i < pixel; i++) {
				
					padding[i] = (byte) 255;
				
				}
				
				padding[pixel] = (byte) 0;
				
				baos.write(padding, 0, padding.length);
			
			}
			
			break;
	
	}
	
	writeChunk(TAG_TRNS, baos.toByteArray());

}

void writeImageData() throws IOException {

	ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
	OutputStream os = null;
	switch (loader.compression) {
	case 0:
		os = Compatibility.newDeflaterOutputStream(baos, NO_COMPRESSION);
		break;
	case 1:
		os = Compatibility.newDeflaterOutputStream(baos, BEST_SPEED);
		break;
	case 3:
		os = Compatibility.newDeflaterOutputStream(baos, BEST_COMPRESSION);
		break;
	default:
		os = Compatibility.newDeflaterOutputStream(baos, DEFAULT_COMPRESSION);
		break;
	}
	if (os == null) os = baos; // returns null for J2ME
	
	if (colorType == 3) {
	
		byte[] lineData = new byte[width];
		
		for (int y = 0; y < height; y++) {
			
			int filter = 0;
			os.write(filter);
			
			data.getPixels(0, y, width, lineData, 0);
			
			os.write(lineData);
		
		}
	
	}
	
	else {
	
		int[] lineData = new int[width];
		byte[] alphaData = null;
		if (colorType == 6) {
			alphaData = new byte[width];
		}
		
		int redMask = data.palette.redMask;
		int redShift = data.palette.redShift;
		int greenMask = data.palette.greenMask;
		int greenShift = data.palette.greenShift;
		int blueShift = data.palette.blueShift;
		int blueMask = data.palette.blueMask;
		
		byte[] lineBytes = new byte[width * (colorType == 6 ? 4 : 3)];
		
		for (int y = 0; y < height; y++) {
		
			int filter = 0;
			os.write(filter);
			
			data.getPixels(0, y, width, lineData, 0);
			
			if (colorType == 6) {
				data.getAlphas(0, y, width, alphaData, 0);
			}
			
			int offset = 0;
			for (int x = 0; x < lineData.length; x++) {
			
				int pixel = lineData[x];
				
				int r = pixel & redMask;
				lineBytes[offset++] = (byte) ((redShift < 0) ? r >>> -redShift
						: r << redShift);
				int g = pixel & greenMask;
				lineBytes[offset++] = (byte) ((greenShift < 0) ? g >>> -greenShift
						: g << greenShift);
				int b = pixel & blueMask;
				lineBytes[offset++] = (byte) ((blueShift < 0) ? b >>> -blueShift
						: b << blueShift);
				
				if (colorType == 6) {
					lineBytes[offset++] = alphaData[x];
				}
			
			}
			
			os.write(lineBytes);
			
		}
	
	}
	
	os.flush();
	os.close();
	
	byte[] compressed = baos.toByteArray();
	if (os == baos) {
		/* Use PngDeflater for J2ME. */
		PngDeflater deflater = new PngDeflater();
		compressed = deflater.deflate(compressed);
	}
	
	writeChunk(TAG_IDAT, compressed);

}

void writeEnd() {

	writeChunk(TAG_IEND, null);

}

public void encode(LEDataOutputStream outputStream) {

	try {
	
		writeSignature();
		writeHeader();
		
		if (colorType == 3) {
			writePalette();
		}
		
		boolean transparencyAlpha = (transparencyType == SWT.TRANSPARENCY_ALPHA);
		boolean transparencyPixel = (transparencyType == SWT.TRANSPARENCY_PIXEL);
		boolean type2Transparency = (colorType == 2 && transparencyPixel);
		boolean type3Transparency = (colorType == 3 && (transparencyAlpha || transparencyPixel));
		
		if (type2Transparency || type3Transparency) {
			writeTransparency();
		}
		
		writeImageData();
		writeEnd();
		
		outputStream.write(bytes.toByteArray());
	
	}
	
	catch (IOException e) {
	
		SWT.error(SWT.ERROR_IO, e);
	
	}

}

}
