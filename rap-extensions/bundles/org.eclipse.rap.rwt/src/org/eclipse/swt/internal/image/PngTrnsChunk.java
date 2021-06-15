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

public class PngTrnsChunk extends PngChunk {
	static final int TRANSPARENCY_TYPE_PIXEL = 0;
	static final int TRANSPARENCY_TYPE_ALPHAS = 1;
	static final int RGB_DATA_LENGTH = 6;
	
PngTrnsChunk(RGB rgb) {
	super(RGB_DATA_LENGTH);
	setType(TYPE_tRNS);
	setInt16(DATA_OFFSET, rgb.red);
	setInt16(DATA_OFFSET + 2, rgb.green);
	setInt16(DATA_OFFSET + 4, rgb.blue);	
	setCRC(computeCRC());
}

PngTrnsChunk(byte[] reference){
	super(reference);
}

@Override
int getChunkType() {
	return CHUNK_tRNS;
}

void validateLength(PngIhdrChunk header, PngPlteChunk paletteChunk) {
	boolean valid;
	switch (header.getColorType()) {
		case PngIhdrChunk.COLOR_TYPE_RGB:
			// Three 2-byte values (RGB)
			valid = getLength() == 6;
			break;
		case PngIhdrChunk.COLOR_TYPE_PALETTE:
			// Three 2-byte values (RGB)
			valid = getLength() <= paletteChunk.getLength();
			break;
		case PngIhdrChunk.COLOR_TYPE_GRAYSCALE:
			// One 2-byte value
			valid = getLength() == 2;
			break;
		// Cannot use both Alpha and tRNS
		case PngIhdrChunk.COLOR_TYPE_RGB_WITH_ALPHA:
		case PngIhdrChunk.COLOR_TYPE_GRAYSCALE_WITH_ALPHA:
		default:
			valid = false;
	}
	if (!valid) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
}

/**
 * Answer whether the chunk is a valid tRNS chunk.
 */
void validate(PngFileReadState readState, PngIhdrChunk headerChunk, PngPlteChunk paletteChunk) {
	if (!readState.readIHDR
		|| (headerChunk.getMustHavePalette() && !readState.readPLTE)
		|| readState.readIDAT
		|| readState.readIEND) 
	{
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	} else {
		readState.readTRNS = true;
	}
	
	validateLength(headerChunk, paletteChunk);
	
	super.validate(readState, headerChunk);
}


int getTransparencyType(PngIhdrChunk header) {
	if (header.getColorType() == PngIhdrChunk.COLOR_TYPE_PALETTE) {
		return TRANSPARENCY_TYPE_ALPHAS;
	}
	return TRANSPARENCY_TYPE_PIXEL;
}

/**
 * Answer the transparent pixel RGB value.
 * This is not valid for palette color types.
 * This is not valid for alpha color types.
 * This will convert a grayscale value into
 * a palette index.
 * It will compress a 6 byte RGB into a 3 byte
 * RGB.
 */
int getSwtTransparentPixel(PngIhdrChunk header) {
	switch (header.getColorType()) {
		case PngIhdrChunk.COLOR_TYPE_GRAYSCALE:
			int gray = ((reference[DATA_OFFSET] & 0xFF) << 8)
				+ (reference[DATA_OFFSET + 1] & 0xFF);
			if (header.getBitDepth() > 8) {
				return PNGFileFormat.compress16BitDepthTo8BitDepth(gray);
			}
			return gray & 0xFF;
		case PngIhdrChunk.COLOR_TYPE_RGB:
			int red = ((reference[DATA_OFFSET] & 0xFF) << 8)
				| (reference[DATA_OFFSET + 1] & 0xFF);
			int green = ((reference[DATA_OFFSET + 2] & 0xFF) << 8)
				| (reference[DATA_OFFSET + 3] & 0xFF);
			int blue = ((reference[DATA_OFFSET + 4] & 0xFF) << 8)
				| (reference[DATA_OFFSET + 5] & 0xFF);			
			if (header.getBitDepth() > 8) {
				red = PNGFileFormat.compress16BitDepthTo8BitDepth(red);
				green = PNGFileFormat.compress16BitDepthTo8BitDepth(green);
				blue = PNGFileFormat.compress16BitDepthTo8BitDepth(blue);			
			}
			return (red << 16) | (green << 8) | blue;	
		default:
			SWT.error(SWT.ERROR_INVALID_IMAGE);
			return -1;
	}
}

/**
 * Answer an array of Alpha values that correspond to the 
 * colors in the palette.
 * This is only valid for the COLOR_TYPE_PALETTE color type.
 */
byte[] getAlphaValues(PngIhdrChunk header, PngPlteChunk paletteChunk) {
	if (header.getColorType() != PngIhdrChunk.COLOR_TYPE_PALETTE) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	byte[] alphas = new byte[paletteChunk.getPaletteSize()];
	int dataLength = getLength();
	int i = 0;
	for (i = 0; i < dataLength; i++) {
		alphas[i] = reference[DATA_OFFSET + i];
	}
	/**
	 * Any palette entries which do not have a corresponding
	 * alpha value in the tRNS chunk are spec'd to have an 
	 * alpha of 255.
	 */
	for (int j = i; j < alphas.length; j++) {
		alphas[j] = (byte) 255;
	}
	return alphas;
}
}
