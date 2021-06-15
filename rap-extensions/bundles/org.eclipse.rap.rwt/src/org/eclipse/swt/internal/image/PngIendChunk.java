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

class PngIendChunk extends PngChunk {

PngIendChunk() {
	super(0);
	setType(TYPE_IEND);
	setCRC(computeCRC());
}

PngIendChunk(byte[] reference){
	super(reference);
}

@Override
int getChunkType() {
	return CHUNK_IEND;
}

/**
 * Answer whether the chunk is a valid IEND chunk.
 */
@Override
void validate(PngFileReadState readState, PngIhdrChunk headerChunk) {
	// An IEND chunk is invalid if no IHDR has been read.
	// Or if a palette is required and has not been read.
	// Or if no IDAT chunk has been read.
	if (!readState.readIHDR
		|| (headerChunk.getMustHavePalette() && !readState.readPLTE)
		|| !readState.readIDAT
		|| readState.readIEND) 
	{
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	} else {
		readState.readIEND = true;
	}
	
	super.validate(readState, headerChunk);
	
	// IEND chunks are not allowed to have any data.
	if (getLength() > 0) SWT.error(SWT.ERROR_INVALID_IMAGE);
}

}
