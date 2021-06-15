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

final class LEDataOutputStream extends OutputStream {
	OutputStream out;
public LEDataOutputStream(OutputStream output) {
	this.out = output;
}
/**
 * Write the specified number of bytes of the given byte array,
 * starting at the specified offset, to the output stream.
 */
@Override
public void write(byte b[], int off, int len) throws IOException {
	out.write(b, off, len);
}
/**
 * Write the given byte to the output stream.
 */
@Override
public void write(int b) throws IOException {
	out.write(b);
}
/**
 * Write the given byte to the output stream.
 */
public void writeByte(byte b) throws IOException {
	out.write(b & 0xFF);
}
/**
 * Write the four bytes of the given integer
 * to the output stream.
 */
public void writeInt(int theInt) throws IOException {
	out.write(theInt & 0xFF);
	out.write((theInt >> 8) & 0xFF);
	out.write((theInt >> 16) & 0xFF);
	out.write((theInt >> 24) & 0xFF);
}
/**
 * Write the two bytes of the given short
 * to the output stream.
 */
public void writeShort(int theShort) throws IOException {
	out.write(theShort & 0xFF);
	out.write((theShort >> 8) & 0xFF);
}
}
