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

final class LZWCodec {
	int bitsPerPixel, blockSize, blockIndex, currentByte, bitsLeft,
		codeSize, clearCode, endCode, newCodes, topSlot, currentSlot,
		imageWidth, imageHeight, imageX, imageY, pass, line, codeMask;
	byte[] block, lineArray;
	int[] stack, suffix, prefix;
	LZWNode[] nodeStack;
	LEDataInputStream inputStream;
	LEDataOutputStream outputStream;
	ImageData image;
	ImageLoader loader;
	boolean interlaced;
	static final int[] MASK_TABLE = new int[] {
		0x1, 0x3, 0x7, 0xF, 0x1F, 0x3F, 0x7F,
		0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF
	};

/**
 * Decode the input.
 */
void decode() {
	int code;
	int oc = 0;
	int fc = 0;
	byte[] buf = new byte[imageWidth];
	int stackIndex = 0;
	int bufIndex = 0;
	int c;
	while ((c = nextCode()) != endCode) {
		if (c == clearCode) {
			codeSize = bitsPerPixel + 1;
			codeMask = MASK_TABLE[bitsPerPixel];
			currentSlot = newCodes;
			topSlot = 1 << codeSize;
			while ((c = nextCode()) == clearCode) {}
			if (c != endCode) {
				oc = fc = c;
				buf[bufIndex] = (byte)c;
				bufIndex++;
				if (bufIndex == imageWidth) {
					nextPutPixels(buf);
					bufIndex = 0;
				}
			}
		} else {
			code = c;
			if (code >= currentSlot) {
				code = oc;
				stack[stackIndex] = fc;
				stackIndex++;
			}
			while (code >= newCodes) {
				stack[stackIndex] = suffix[code];
				stackIndex++;
				code = prefix[code];
			}
			stack[stackIndex] = code;
			stackIndex++;
			if (currentSlot < topSlot) {
				fc = code;
				suffix[currentSlot] = fc;
				prefix[currentSlot] = oc;
				currentSlot++;
				oc = c;
			}
			if (currentSlot >= topSlot) {
				if (codeSize < 12) {
					codeMask = MASK_TABLE[codeSize];
					codeSize++;
					topSlot = topSlot + topSlot;
				}
			}
			while (stackIndex > 0) {
				stackIndex--;
				buf[bufIndex] = (byte)stack[stackIndex];
				bufIndex++;
				if (bufIndex == imageWidth) {
					nextPutPixels(buf);
					bufIndex = 0;
				}
			}
		}
	}
	if (bufIndex != 0 && line < imageHeight) {
		nextPutPixels(buf);
	}
}
/**
 * Decode the LZW-encoded bytes in the given byte stream
 * into the given DeviceIndependentImage.
 */
public void decode(LEDataInputStream inputStream, ImageLoader loader, ImageData image, boolean interlaced, int depth) {
	this.inputStream = inputStream;
	this.loader = loader;
	this.image = image;
	this.interlaced = interlaced;
	this.bitsPerPixel = depth;
	initializeForDecoding();
	decode();
}
/**
 * Encode the image.
 */
void encode() {
	nextPutCode(clearCode);
	int lastPrefix = encodeLoop();
	nextPutCode(lastPrefix);
	nextPutCode(endCode);

	// Write out last partial block
	if (bitsLeft == 8) {
		block[0] = (byte)(blockIndex - 1); // Nothing in last byte
	} else {
		block[0] = (byte)(blockIndex); // Last byte has data
	}
	writeBlock();

	// Write out empty block to indicate the end (if needed)
	if (block[0] != 0) {
		block[0] = 0;
		writeBlock();
	}
}
/**
 * Encode the bytes into the given byte stream
 * from the given DeviceIndependentImage.
 */
public void encode(LEDataOutputStream byteStream, ImageData image) {
	this.outputStream = byteStream;
	this.image = image;
	initializeForEncoding();
	encode();
}
/**
 * Encoding loop broken out to allow early return.
 */
int encodeLoop() {
	int pixel = nextPixel();
	boolean found;
	LZWNode node;
	while (true) {
		int currentPrefix = pixel;
		node = nodeStack[currentPrefix];
		found = true;
		pixel = nextPixel();
		if (pixel < 0)
			return currentPrefix;
		while (found && (node.children != null)) {
			node = node.children;
			while (found && (node.suffix != pixel)) {
				if (pixel < node.suffix) {
					if (node.left == null) {
						node.left = new LZWNode();
						found = false;
					}
					node = node.left;
				} else {
					if (node.right == null) {
						node.right = new LZWNode();
						found = false;
					}
					node = node.right;
				}
			}
			if (found) {
				currentPrefix = node.code;
				pixel = nextPixel();
				if (pixel < 0)
					return currentPrefix;
			}
		}
		if (found) {
			node.children = new LZWNode();
			node = node.children;
		}
		node.children = null;
		node.left = null;
		node.right = null;
		node.code = currentSlot;
		node.prefix = currentPrefix;
		node.suffix = pixel;
		nextPutCode(currentPrefix);
		currentSlot++;
		// Off by one?
		if (currentSlot < 4096) {
			if (currentSlot > topSlot) {
				codeSize++;
				codeMask = MASK_TABLE[codeSize - 1];
				topSlot *= 2;
			}
		} else {
			nextPutCode(clearCode);
			for (int i = 0; i < nodeStack.length; i++)
				nodeStack[i].children = null;
			codeSize = bitsPerPixel + 1;
			codeMask = MASK_TABLE[codeSize - 1];
			currentSlot = newCodes;
			topSlot = 1 << codeSize;
		}
	}
}
/**
 * Initialize the receiver for decoding the given
 * byte array.
 */
void initializeForDecoding() {
	pass = 1;
	line = 0;
	codeSize = bitsPerPixel + 1;
	topSlot = 1 << codeSize;
	clearCode = 1 << bitsPerPixel;
	endCode = clearCode + 1;
	newCodes = currentSlot = endCode + 1;
	currentByte = -1;
	blockSize = bitsLeft = 0;
	blockIndex = 0;
	codeMask = MASK_TABLE[codeSize - 1];
	stack = new int[4096];
	suffix = new int[4096];
	prefix = new int[4096];
	block = new byte[256];
	imageWidth = image.width;
	imageHeight = image.height;
}
/**
 * Initialize the receiver for encoding the given
 * byte array.
 */
void initializeForEncoding() {
	interlaced = false;
	bitsPerPixel = image.depth;
	codeSize = bitsPerPixel + 1;
	topSlot = 1 << codeSize;
	clearCode = 1 << bitsPerPixel;
	endCode = clearCode + 1;
	newCodes = currentSlot = endCode + 1;
	bitsLeft = 8;
	currentByte = 0;
	blockIndex = 1;
	blockSize = 255;
	block = new byte[blockSize];
	block[0] = (byte)(blockSize - 1);
	nodeStack = new LZWNode[1 << bitsPerPixel];
	for (int i = 0; i < nodeStack.length; i++) {
		LZWNode node = new LZWNode();
		node.code = i + 1;
		node.prefix = -1;
		node.suffix = i + 1;
		nodeStack[i] = node;
	}
	imageWidth = image.width;
	imageHeight = image.height;
	imageY = -1;
	lineArray = new byte[imageWidth];
	imageX = imageWidth + 1; // Force a read
}
/**
 * Answer the next code from the input byte array.
 */
int nextCode() {
	int code;
	if (bitsLeft == 0) {
		if (blockIndex >= blockSize) {
			blockSize = readBlock();
			blockIndex = 0;
			if (blockSize == 0) return endCode;
		}
		blockIndex++;
		currentByte = block[blockIndex] & 0xFF;
		bitsLeft = 8;
		code = currentByte;
	} else {
		int shift = bitsLeft - 8;
		if (shift < 0)
			code = currentByte >> (0 - shift);
		else
			code = currentByte << shift;
	}
	while (codeSize > bitsLeft) {
		if (blockIndex >= blockSize) {
			blockSize = readBlock();
			blockIndex = 0;
			if (blockSize == 0) return endCode;
		}
		blockIndex++;
		currentByte = block[blockIndex] & 0xFF;
		code += currentByte << bitsLeft;
		bitsLeft += 8;
	}
	bitsLeft -= codeSize;
	return code & codeMask;
}
/**
 * Answer the next pixel to encode in the image
 */
int nextPixel() {
	imageX++;
	if (imageX > imageWidth) {
		imageY++;
		if (imageY >= imageHeight) {
			return -1;
		} else {
			nextPixels(lineArray, imageWidth);
		}
		imageX = 1;
	}
	return this.lineArray[imageX - 1] & 0xFF;
}
/**
 * Copy a row of pixel values from the image.
 */
void nextPixels(byte[] buf, int lineWidth) {
	if (image.depth == 8) {
		System.arraycopy(image.data, imageY * image.bytesPerLine, buf, 0, lineWidth);
	} else {
		image.getPixels(0, imageY, lineWidth, buf, 0);
	}
}
/**
 * Output aCode to the output stream.
 */
void nextPutCode(int aCode) {
	int codeToDo = aCode;
	int codeBitsToDo = codeSize;
	// Fill in the remainder of the current byte with the
	// *high-order* bits of the code.
	int c = codeToDo & MASK_TABLE[bitsLeft - 1];
	currentByte = currentByte | (c << (8 - bitsLeft));
	block[blockIndex] = (byte)currentByte;
	codeBitsToDo -= bitsLeft;
	if (codeBitsToDo < 1) {
		// The whole code fit in the first byte, so we are done.
		bitsLeft -= codeSize;
		if (bitsLeft == 0) {
			// We used the whole last byte, so get ready
			// for the next one.
			bitsLeft = 8;
			blockIndex++;
			if (blockIndex >= blockSize) {
				writeBlock();
				blockIndex = 1;
			}
			currentByte = 0;
		}
		return;
	}
	codeToDo = codeToDo >> bitsLeft;

	// Fill in any remaining whole bytes (i.e. not the last one!)
	blockIndex++;
	if (blockIndex >= blockSize) {
		writeBlock();
		blockIndex = 1;
	}
	while (codeBitsToDo >= 8) {
		currentByte = codeToDo & 0xFF;
		block[blockIndex] = (byte)currentByte;
		codeToDo = codeToDo >> 8;
		codeBitsToDo -= 8;
		blockIndex++;
		if (blockIndex >= blockSize) {
			writeBlock();
			blockIndex = 1;
		}
	}
	// Fill the *low-order* bits of the last byte with the remainder
	bitsLeft = 8 - codeBitsToDo;
	currentByte = codeToDo;
	block[blockIndex] = (byte)currentByte;
}
/**
 * Copy a row of pixel values to the image.
 */
void nextPutPixels(byte[] buf) {
	if (image.depth == 8) {
		// Slight optimization for depth = 8.
		int start = line * image.bytesPerLine;
		for (int i = 0; i < imageWidth; i++)
			image.data[start + i] = buf[i];
	} else {
		image.setPixels(0, line, imageWidth, buf, 0);
	}
	if (interlaced) {
		if (pass == 1) {
			copyRow(buf, 7);
			line += 8;
		} else if (pass == 2) {
			copyRow(buf, 3);
			line += 8;
		} else if (pass == 3) {
			copyRow(buf, 1);
			line += 4;
		} else if (pass == 4) {
			line += 2;
		} else if (pass == 5) {
			line += 0;
		}
		if (line >= imageHeight) {
			pass++;
			if (pass == 2) line = 4;
			else if (pass == 3) line = 2;
			else if (pass == 4) line = 1;
			else if (pass == 5) line = 0;
			if (pass < 5) {
				if (loader.hasListeners()) {
					ImageData imageCopy = (ImageData) image.clone();
					loader.notifyListeners(
						new ImageLoaderEvent(loader, imageCopy, pass - 2, false));
				}
			}
		}
		if (line >= imageHeight) line = 0;
	} else {
		line++;
	}
}
/**
 * Copy duplicate rows of pixel values to the image.
 * This is to fill in rows if the image is interlaced.
 */
void copyRow(byte[] buf, int copies) {
	for (int i = 1; i <= copies; i++) {
		if (line + i < imageHeight) {
			image.setPixels(0, line + i, imageWidth, buf, 0);
		}
	}
}
/**
 * Read a block from the byte stream.
 * Return the number of bytes read.
 * Throw an exception if the block could not be read.
 */
int readBlock() {
	int size = -1;
	try {
		size = inputStream.read();
		if (size == -1) {
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		}
		block[0] = (byte)size;
		size = inputStream.read(block, 1, size);
		if (size == -1) {
			SWT.error(SWT.ERROR_INVALID_IMAGE);
		}
	} catch (Exception e) {
		SWT.error(SWT.ERROR_IO, e);
	}
	return size;
}
/**
 * Write a block to the byte stream.
 * Throw an exception if the block could not be written.
 */
void writeBlock() {
	try {
		outputStream.write(block, 0, (block[0] & 0xFF) + 1);
	} catch (Exception e) {
		SWT.error(SWT.ERROR_IO, e);
	}
}
}
