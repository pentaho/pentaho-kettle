/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

public class PngInputStream extends InputStream {
	PngChunkReader reader;
	PngChunk chunk;
	int offset, length;
	
	final static int DATA_OFFSET = 8; 
	
public PngInputStream(PngIdatChunk chunk, PngChunkReader reader) {
	this.chunk = chunk;
	this.reader = reader;
	length = chunk.getLength();
	offset = 0;
}

private boolean checkChunk() throws IOException {
	while (offset == length) {
		chunk = reader.readNextChunk();
		if (chunk == null) throw new IOException();
		if (chunk.getChunkType() == PngChunk.CHUNK_IEND) return false;
		if (chunk.getChunkType() != PngChunk.CHUNK_IDAT) throw new IOException();
		length = chunk.getLength();
		offset = 0;
	}
	return true;
}

@Override
public void close() throws IOException {
	chunk = null;
}

@Override
public int read() throws IOException {
	if (chunk == null) throw new IOException();
	if (offset == length && !checkChunk()) return -1;
	int b = chunk.reference[DATA_OFFSET + offset] & 0xFF;
	offset++;
	return b;
}

@Override
public int read(byte[] b, int off, int len) throws IOException {
	if (chunk == null) throw new IOException();
	if (offset == length && !checkChunk()) return -1;
	len = Math.min(len, length - offset);
	System.arraycopy(chunk.reference, DATA_OFFSET + offset, b, off, len);
	offset += len;
	return len;
}
}
