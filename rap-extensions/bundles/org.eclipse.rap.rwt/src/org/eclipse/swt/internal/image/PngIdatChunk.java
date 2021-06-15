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

class PngIdatChunk extends PngChunk {

	static final int HEADER_BYTES_LENGTH = 2;
	static final int ADLER_FIELD_LENGTH = 4;
	static final int HEADER_BYTE1_DATA_OFFSET = DATA_OFFSET + 0;
	static final int HEADER_BYTE2_DATA_OFFSET = DATA_OFFSET + 1;
	static final int ADLER_DATA_OFFSET = DATA_OFFSET + 2; // plus variable compressed data length

PngIdatChunk(byte headerByte1, byte headerByte2, byte[] data, int adler) {
	super(data.length + HEADER_BYTES_LENGTH + ADLER_FIELD_LENGTH);
	setType(TYPE_IDAT);
	reference[HEADER_BYTE1_DATA_OFFSET] = headerByte1;
	reference[HEADER_BYTE2_DATA_OFFSET] = headerByte2;
	System.arraycopy(data, 0, reference, DATA_OFFSET, data.length);
	setInt32(ADLER_DATA_OFFSET, adler);
	setCRC(computeCRC());
}
		
PngIdatChunk(byte[] reference) {
	super(reference);
}

@Override
int getChunkType() {
	return CHUNK_IDAT;
}

/**
 * Answer whether the chunk is a valid IDAT chunk.
 */
@Override
void validate(PngFileReadState readState, PngIhdrChunk headerChunk) {
	if (!readState.readIHDR
		|| (headerChunk.getMustHavePalette() && !readState.readPLTE)
		|| readState.readIEND) 
	{
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	} else {
		readState.readIDAT = true;
	}
	
	super.validate(readState, headerChunk);
}

byte getDataByteAtOffset(int offset) {
	return reference[DATA_OFFSET + offset];
}

}
