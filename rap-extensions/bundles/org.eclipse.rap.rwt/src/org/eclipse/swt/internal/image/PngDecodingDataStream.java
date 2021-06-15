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

import org.eclipse.swt.*;

public class PngDecodingDataStream extends InputStream {
	InputStream stream;
	byte currentByte;
	int nextBitIndex;
	
	PngLzBlockReader lzBlockReader;
	int adlerValue;
	
	static final int PRIME = 65521;
	static final int MAX_BIT = 7;		
	
PngDecodingDataStream(InputStream stream) throws IOException {
	super();
	this.stream = stream;
	nextBitIndex = MAX_BIT + 1;
	adlerValue = 1;
	lzBlockReader = new PngLzBlockReader(this);
	readCompressedDataHeader();
	lzBlockReader.readNextBlockHeader();
}

/**
 * This method should be called when the image decoder thinks
 * that all of the compressed image data has been read. This
 * method will ensure that the next data value is an end of 
 * block marker. If there are more blocks after this one,
 * the method will read them and ensure that they are empty.
 */
void assertImageDataAtEnd() throws IOException {
	lzBlockReader.assertCompressedDataAtEnd();
}

@Override
public void close() throws IOException {
	assertImageDataAtEnd();
	checkAdler();
}

int getNextIdatBits(int length) throws IOException {
	int value = 0;
	for (int i = 0; i < length; i++) {
		value |= (getNextIdatBit() << i);
	}
	return value;
}

int getNextIdatBit() throws IOException {
	if (nextBitIndex > MAX_BIT) {
		currentByte = getNextIdatByte();
		nextBitIndex = 0;
	}
	return (currentByte & (1 << nextBitIndex)) >> nextBitIndex++;
}

byte getNextIdatByte() throws IOException {	
	byte nextByte = (byte)stream.read();
	nextBitIndex = MAX_BIT + 1;
	return nextByte;
}

void updateAdler(byte value) {
	int low = adlerValue & 0xFFFF;
	int high = (adlerValue >> 16) & 0xFFFF;
	int valueInt = value & 0xFF;
	low = (low + valueInt) % PRIME;
	high = (low + high) % PRIME;
	adlerValue = (high << 16) | low;
}

@Override
public int read() throws IOException {
	byte nextDecodedByte = lzBlockReader.getNextByte();
	updateAdler(nextDecodedByte);
	return nextDecodedByte & 0xFF;
}

@Override
public int read(byte[] buffer, int off, int len) throws IOException {
	for (int i = 0; i < len; i++) {
		int b = read();
		if (b == -1) return i;
		buffer[off + i] = (byte)b;
	}
	return len;
}

void error() {
	SWT.error(SWT.ERROR_INVALID_IMAGE);
}

private void readCompressedDataHeader() throws IOException {
	byte headerByte1 = getNextIdatByte();
	byte headerByte2 = getNextIdatByte();
	
	int number = ((headerByte1 & 0xFF) << 8) | (headerByte2 & 0xFF);
	if (number % 31 != 0) error();
	
	int compressionMethod = headerByte1 & 0x0F;
	if (compressionMethod != 8) error();
	
	int windowSizeHint = (headerByte1 & 0xF0) >> 4;
	if (windowSizeHint > 7) error();
	int windowSize = (1 << (windowSizeHint + 8));
	lzBlockReader.setWindowSize(windowSize);
	
	int dictionary = (headerByte2 & (1 << 5));
	if (dictionary != 0) error();
	
//	int compressionLevel = (headerByte2 & 0xC0) >> 6;
}

void checkAdler() throws IOException {
	int storedAdler = ((getNextIdatByte() & 0xFF) << 24)
		| ((getNextIdatByte() & 0xFF) << 16)
		| ((getNextIdatByte() & 0xFF) << 8)
		| (getNextIdatByte() & 0xFF);
	if (storedAdler != adlerValue) error();
}

}
