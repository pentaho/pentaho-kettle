/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

public class PngDeflater {

	static final int BASE = 65521;
	static final int WINDOW = 32768;
	static final int MIN_LENGTH = 3;
	static final int MAX_MATCHES = 32;
	static final int HASH = 8209;
	
	byte[] in;
	int inLength;
	
	ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
	
	int adler32 = 1;
	
	int buffer, bitCount;
	
	Link[] hashtable = new Link[HASH];
	Link[] window = new Link[WINDOW];
	int nextWindow;

static class Link {

	int hash, value;
	Link previous, next;
	
	Link() {
	
		this.hash = 0;
		this.value = 0;
		this.previous = null;
		this.next = null;
	
	}

}

static class Match {

	int length, distance;
	
	Match(int length, int distance) {
	
		this.length = length;
		this.distance = distance;
	
	}

}

static final short mirrorBytes[] = {

    0x00, 0x80, 0x40, 0xc0, 0x20, 0xa0, 0x60, 0xe0,
    0x10, 0x90, 0x50, 0xd0, 0x30, 0xb0, 0x70, 0xf0,
    0x08, 0x88, 0x48, 0xc8, 0x28, 0xa8, 0x68, 0xe8,
    0x18, 0x98, 0x58, 0xd8, 0x38, 0xb8, 0x78, 0xf8,
    0x04, 0x84, 0x44, 0xc4, 0x24, 0xa4, 0x64, 0xe4,
    0x14, 0x94, 0x54, 0xd4, 0x34, 0xb4, 0x74, 0xf4,
    0x0c, 0x8c, 0x4c, 0xcc, 0x2c, 0xac, 0x6c, 0xec,
    0x1c, 0x9c, 0x5c, 0xdc, 0x3c, 0xbc, 0x7c, 0xfc,
    0x02, 0x82, 0x42, 0xc2, 0x22, 0xa2, 0x62, 0xe2,
    0x12, 0x92, 0x52, 0xd2, 0x32, 0xb2, 0x72, 0xf2,
    0x0a, 0x8a, 0x4a, 0xca, 0x2a, 0xaa, 0x6a, 0xea,
    0x1a, 0x9a, 0x5a, 0xda, 0x3a, 0xba, 0x7a, 0xfa,
    0x06, 0x86, 0x46, 0xc6, 0x26, 0xa6, 0x66, 0xe6,
    0x16, 0x96, 0x56, 0xd6, 0x36, 0xb6, 0x76, 0xf6,
    0x0e, 0x8e, 0x4e, 0xce, 0x2e, 0xae, 0x6e, 0xee,
    0x1e, 0x9e, 0x5e, 0xde, 0x3e, 0xbe, 0x7e, 0xfe,
    0x01, 0x81, 0x41, 0xc1, 0x21, 0xa1, 0x61, 0xe1,
    0x11, 0x91, 0x51, 0xd1, 0x31, 0xb1, 0x71, 0xf1,
    0x09, 0x89, 0x49, 0xc9, 0x29, 0xa9, 0x69, 0xe9,
    0x19, 0x99, 0x59, 0xd9, 0x39, 0xb9, 0x79, 0xf9,
    0x05, 0x85, 0x45, 0xc5, 0x25, 0xa5, 0x65, 0xe5,
    0x15, 0x95, 0x55, 0xd5, 0x35, 0xb5, 0x75, 0xf5,
    0x0d, 0x8d, 0x4d, 0xcd, 0x2d, 0xad, 0x6d, 0xed,
    0x1d, 0x9d, 0x5d, 0xdd, 0x3d, 0xbd, 0x7d, 0xfd,
    0x03, 0x83, 0x43, 0xc3, 0x23, 0xa3, 0x63, 0xe3,
    0x13, 0x93, 0x53, 0xd3, 0x33, 0xb3, 0x73, 0xf3,
    0x0b, 0x8b, 0x4b, 0xcb, 0x2b, 0xab, 0x6b, 0xeb,
    0x1b, 0x9b, 0x5b, 0xdb, 0x3b, 0xbb, 0x7b, 0xfb,
    0x07, 0x87, 0x47, 0xc7, 0x27, 0xa7, 0x67, 0xe7,
    0x17, 0x97, 0x57, 0xd7, 0x37, 0xb7, 0x77, 0xf7,
    0x0f, 0x8f, 0x4f, 0xcf, 0x2f, 0xaf, 0x6f, 0xef,
    0x1f, 0x9f, 0x5f, 0xdf, 0x3f, 0xbf, 0x7f, 0xff,

};

static class Code {

	int code, extraBits, min, max;
	
	Code(int code, int extraBits, int min, int max) {
	
		this.code = code;
		this.extraBits = extraBits;
		this.min = min;
		this.max = max;
    
    }

}

static final Code lengthCodes[] = {

	new Code(257, 0, 3, 3),
	new Code(258, 0, 4, 4),
	new Code(259, 0, 5, 5),
	new Code(260, 0, 6, 6),
	new Code(261, 0, 7, 7),
	new Code(262, 0, 8, 8),
	new Code(263, 0, 9, 9),
	new Code(264, 0, 10, 10),
	new Code(265, 1, 11, 12),
	new Code(266, 1, 13, 14),
	new Code(267, 1, 15, 16),
	new Code(268, 1, 17, 18),
	new Code(269, 2, 19, 22),
	new Code(270, 2, 23, 26),
	new Code(271, 2, 27, 30),
	new Code(272, 2, 31, 34),
	new Code(273, 3, 35, 42),
	new Code(274, 3, 43, 50),
	new Code(275, 3, 51, 58),
	new Code(276, 3, 59, 66),
	new Code(277, 4, 67, 82),
	new Code(278, 4, 83, 98),
	new Code(279, 4, 99, 114),
	new Code(280, 4, 115, 130),
	new Code(281, 5, 131, 162),
	new Code(282, 5, 163, 194),
	new Code(283, 5, 195, 226),
	new Code(284, 5, 227, 257),
	new Code(285, 0, 258, 258)

};

static final Code distanceCodes[] = {

	new Code(0, 0, 1, 1),
	new Code(1, 0, 2, 2),
	new Code(2, 0, 3, 3),
	new Code(3, 0, 4, 4),
	new Code(4, 1, 5, 6),
	new Code(5, 1, 7, 8),
	new Code(6, 2, 9, 12),
	new Code(7, 2, 13, 16),
	new Code(8, 3, 17, 24),
	new Code(9, 3, 25, 32),
	new Code(10, 4, 33, 48),
	new Code(11, 4, 49, 64),
	new Code(12, 5, 65, 96),
	new Code(13, 5, 97, 128),
	new Code(14, 6, 129, 192),
	new Code(15, 6, 193, 256),
	new Code(16, 7, 257, 384),
	new Code(17, 7, 385, 512),
	new Code(18, 8, 513, 768),
	new Code(19, 8, 769, 1024),
	new Code(20, 9, 1025, 1536),
	new Code(21, 9, 1537, 2048),
	new Code(22, 10, 2049, 3072),
	new Code(23, 10, 3073, 4096),
	new Code(24, 11, 4097, 6144),
	new Code(25, 11, 6145, 8192),
	new Code(26, 12, 8193, 12288),
	new Code(27, 12, 12289, 16384),
	new Code(28, 13, 16385, 24576),
	new Code(29, 13, 24577, 32768)

};

void writeShortLSB(ByteArrayOutputStream baos, int theShort) {

	byte byte1 = (byte) (theShort & 0xff);
	byte byte2 = (byte) ((theShort >> 8) & 0xff);
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

void updateAdler(byte value) {

	int low = adler32 & 0xffff;
	int high = (adler32 >> 16) & 0xffff;
	int valueInt = value & 0xff;
	low = (low + valueInt) % BASE;
	high = (low + high) % BASE;
	adler32 = (high << 16) | low;

}

int hash(byte[] bytes) {

	int hash = ((bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8) % HASH;
	if (hash < 0) {
		hash = hash + HASH;
	}
	return hash;

}

void writeBits(int value, int count) {

	buffer |= value << bitCount;
	bitCount += count;
	if (bitCount >= 16) {
		bytes.write((byte) buffer);
		bytes.write((byte) (buffer >>> 8));
		buffer >>>= 16;
		bitCount -= 16;
	}

}

void alignToByte() {

	if (bitCount > 0) {
		bytes.write((byte) buffer);
		if (bitCount > 8) bytes.write((byte) (buffer >>> 8));
	}
	buffer = 0;
	bitCount = 0;

}

void outputLiteral(byte literal) {

	int i = literal & 0xff;
	
	if (i <= 143) {
		// 0 through 143 are 8 bits long starting at 00110000
		writeBits(mirrorBytes[0x30 + i], 8);
	}
	else {
		// 144 through 255 are 9 bits long starting at 110010000
		writeBits(1 + 2 * mirrorBytes[0x90 - 144 + i], 9);
	}

}

Code findCode(int value, Code[] codes) {

	int i, j, k;
	
	i = -1;
	j = codes.length;
	while (true) {
		k = (j + i) / 2;
		if (value < codes[k].min) {
			j = k;
		}
		else if (value > codes[k].max) {
			i = k;
		}
		else {
			return codes[k];
		}
	}

}

void outputMatch(int length, int distance) {

	Code d, l;
	int thisLength;
	
	while (length > 0) {

		// we can transmit matches of lengths 3 through 258 inclusive
		// so if length exceeds 258, we must transmit in several steps,
		// with 258 or less in each step
		
		if (length > 260) {
			thisLength = 258;
		}
		else if (length <= 258) {
			thisLength = length;
		}
		else {
			thisLength = length - 3;
		}
		
		length = length - thisLength;
				
		// find length code
		l = findCode(thisLength, lengthCodes);
		
		// transmit the length code
		// 256 through 279 are 7 bits long starting at 0000000
		// 280 through 287 are 8 bits long starting at 11000000
		if (l.code <= 279) {
			writeBits(mirrorBytes[(l.code - 256) * 2], 7);
		}
		else {
			writeBits(mirrorBytes[0xc0 - 280 + l.code], 8);
		}
		
		// transmit the extra bits
		if (l.extraBits != 0) {
			writeBits(thisLength - l.min, l.extraBits);
		}
		
		// find distance code
		d = findCode(distance, distanceCodes);
		
		// transmit the distance code
		// 5 bits long starting at 00000
		writeBits(mirrorBytes[d.code * 8], 5);
		
		// transmit the extra bits
		if (d.extraBits != 0) {
			writeBits(distance - d.min, d.extraBits);
		}
	
	}

}

Match findLongestMatch(int position, Link firstPosition) {

	Link link = firstPosition;
	int numberOfMatches = 0;
	Match bestMatch = new Match(-1, -1);
	
	while (true) {
	
		int matchPosition = link.value;
		
		if (position - matchPosition < WINDOW && matchPosition != 0) {

			int i;
			
			for (i = 1; position + i < inLength; i++) {
				if (in[position + i] != in[matchPosition + i]) {
					break;
				}
			}
			
			if (i >= MIN_LENGTH) {
			
				if (i > bestMatch.length) {
					bestMatch.length = i;
					bestMatch.distance = position - matchPosition;
				}
				
				numberOfMatches = numberOfMatches + 1;
				
				if (numberOfMatches == MAX_MATCHES) {
					break;
				}
			
			}
						
		}
		
		link = link.next;
		if (link == null) {
			break;
		}
	
	}
	
	if (bestMatch.length < MIN_LENGTH || bestMatch.distance < 1 || bestMatch.distance > WINDOW) {
		return null;
	}
	
	return bestMatch;	

}

void updateHashtable(int to, int from) {

	byte[] data = new byte[3];
	int hash;
	Link temp;
	
	for (int i = to; i < from; i++) {
		
		if (i + MIN_LENGTH > inLength) {
			break;
		}
		
		data[0] = in[i];
		data[1] = in[i + 1];
		data[2] = in[i + 2];
		
		hash = hash(data);
		
		if (window[nextWindow].previous != null) {
			window[nextWindow].previous.next = null;
		}
		else if (window[nextWindow].hash != 0) {
			hashtable[window[nextWindow].hash].next = null;
		}
		
		window[nextWindow].hash = hash;
		window[nextWindow].value = i;
		window[nextWindow].previous = null;
		temp = window[nextWindow].next = hashtable[hash].next;
		hashtable[hash].next = window[nextWindow];
		if (temp != null) {
			temp.previous = window[nextWindow];
		}
		
		nextWindow = nextWindow + 1;
		if (nextWindow == WINDOW) {
			nextWindow = 0;
		}
			
	}

}

void compress() {

	int position, newPosition;
	byte[] data = new byte[3];
	int hash;
	for (int i = 0; i < HASH; i++) {
		hashtable[i] = new Link();
	}
	for (int i = 0; i < WINDOW; i++) {
		window[i] = new Link();
	}
	nextWindow = 0;
	Link firstPosition;
	Match match;
	int deferredPosition = -1;
	Match deferredMatch = null;
	
	writeBits(0x01, 1); // BFINAL = 0x01 (final block)
	writeBits(0x01, 2); // BTYPE = 0x01 (compression with fixed Huffman codes)
	
	// just output first byte so we never match at zero
	outputLiteral(in[0]);
	position = 1;
	
	while (position < inLength) {
	
		if (inLength - position < MIN_LENGTH) {
			outputLiteral(in[position]);
			position = position + 1;
			continue;
		}
		
		data[0] = in[position];
		data[1] = in[position + 1];
		data[2] = in[position + 2];
		
		hash = hash(data);
		firstPosition = hashtable[hash];
		
		match = findLongestMatch(position, firstPosition);
		
		updateHashtable(position, position + 1);
		
		if (match != null) {
		
			if (deferredMatch != null) {
				if (match.length > deferredMatch.length + 1) {
					// output literal at deferredPosition
					outputLiteral(in[deferredPosition]);
					// defer this match
					deferredPosition = position;
					deferredMatch = match;
					position = position + 1;
				}
				else {
					// output deferredMatch
					outputMatch(deferredMatch.length, deferredMatch.distance);
					newPosition = deferredPosition + deferredMatch.length;
					deferredPosition = -1;
					deferredMatch = null;
					updateHashtable(position + 1, newPosition);
					position = newPosition;
				}
			}
			else {
				// defer this match
				deferredPosition = position;
				deferredMatch = match;
				position = position + 1;
			}
		
		}
		
		else {
		
			// no match found
			if (deferredMatch != null) {
				outputMatch(deferredMatch.length, deferredMatch.distance);
				newPosition = deferredPosition + deferredMatch.length;
				deferredPosition = -1;
				deferredMatch = null;
				updateHashtable(position + 1, newPosition);
				position = newPosition;
			}
			else {
				outputLiteral(in[position]);
				position = position + 1;
			}
		
		}
	
	}
	
	writeBits(0, 7); // end of block code
	alignToByte();

}

void compressHuffmanOnly() {

	int position;
	
	writeBits(0x01, 1); // BFINAL = 0x01 (final block)
	writeBits(0x01, 2); // BTYPE = 0x01 (compression with fixed Huffman codes)
	
	for (position = 0; position < inLength;) {
	
		outputLiteral(in[position]);
		position = position + 1;
	
	}
	
	writeBits(0, 7); // end of block code
	alignToByte();

}

void store() {

	// stored blocks are limited to 0xffff bytes
	
	int start = 0;
	int length = inLength;
	int blockLength;
	int BFINAL = 0x00; // BFINAL = 0x00 or 0x01 (if final block), BTYPE = 0x00 (no compression)
	
	while (length > 0) {
	
		if (length < 65535) {
			blockLength = length;
			BFINAL = 0x01;
		}
		else {
			blockLength = 65535;
			BFINAL = 0x00;
		}
		
		// write data header
		bytes.write((byte) BFINAL);
		writeShortLSB(bytes, blockLength); // LEN
		writeShortLSB(bytes, blockLength ^ 0xffff); // NLEN (one's complement of LEN)
	
		// write actual data
		bytes.write(in, start, blockLength);
		
		length = length - blockLength;
		start = start + blockLength;
	
	}

}

public byte[] deflate(byte[] input) {

	in = input;
	inLength = input.length;
	
	// write zlib header
	bytes.write((byte) 0x78); // window size = 0x70 (32768), compression method = 0x08
	bytes.write((byte) 0x9C); // compression level = 0x80 (default), check bits = 0x1C
	
	// compute checksum
	for (int i = 0; i < inLength; i++) {
		updateAdler(in[i]);
	}
	
	//store();
	
	//compressHuffmanOnly();
	
	compress();
	
	// write checksum
	writeInt(bytes, adler32);
	
	return bytes.toByteArray();

}

}
