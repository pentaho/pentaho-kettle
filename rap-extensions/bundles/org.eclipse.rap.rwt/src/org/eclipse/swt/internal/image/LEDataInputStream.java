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

final class LEDataInputStream extends InputStream {
	int position;
	InputStream in;

	/**
	 * The byte array containing the bytes to read.
	 */
	protected byte[] buf;
	
	/**
	 * The current position within the byte array <code>buf</code>. A value
	 * equal to buf.length indicates no bytes available.  A value of
	 * 0 indicates the buffer is full.
	 */
	protected int pos;
	

	public LEDataInputStream(InputStream input) {
		this(input, 512);
	}
	
	public LEDataInputStream(InputStream input, int bufferSize) {
		this.in = input;
		if (bufferSize > 0) {
			buf = new byte[bufferSize];
			pos = bufferSize;
		} 
		else throw new IllegalArgumentException();
	}
	
	@Override
	public void close() throws IOException {
		buf = null;
		if (in != null) {
			in.close();
			in = null;
		}
	}
	
	/**
	 * Answer how many bytes were read.
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * Answers how many bytes are available for reading without blocking
	 */
	@Override
	public int available() throws IOException {
		if (buf == null) throw new IOException();
		return (buf.length - pos) + in.available();
	}
	
	/**
	 * Answer the next byte of the input stream.
	 */
	@Override
	public int read() throws IOException {
		if (buf == null) throw new IOException();
		if (pos < buf.length) {
			position++;
			return (buf[pos++] & 0xFF);
		}
		int c = in.read();
		if (c != -1) position++;
		return c;
	}
	
	/**
	 * Don't imitate the JDK behaviour of reading a random number
	 * of bytes when you can actually read them all.
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		int read = 0, count;
		while (read != len && (count = readData(b, off, len - read)) != -1) {
			off += count;
			read += count;
		}
		position += read;
		if (read == 0 && read != len) return -1;
		return read;
	}
	
	/**
 	 * Reads at most <code>length</code> bytes from this LEDataInputStream and 
 	 * stores them in byte array <code>buffer</code> starting at <code>offset</code>.
 	 * <p>
 	 * Answer the number of bytes actually read or -1 if no bytes were read and 
 	 * end of stream was encountered.  This implementation reads bytes from 
 	 * the pushback buffer first, then the target stream if more bytes are required
 	 * to satisfy <code>count</code>.
	 * </p>
	 * @param buffer the byte array in which to store the read bytes.
	 * @param offset the offset in <code>buffer</code> to store the read bytes.
	 * @param length the maximum number of bytes to store in <code>buffer</code>.
	 *
	 * @return int the number of bytes actually read or -1 if end of stream.
	 *
	 * @exception java.io.IOException if an IOException occurs.
	 */
	private int readData(byte[] buffer, int offset, int length) throws IOException {
		if (buf == null) throw new IOException();
		if (offset < 0 || offset > buffer.length ||
  		 	length < 0 || (length > buffer.length - offset)) {
	 		throw new ArrayIndexOutOfBoundsException();
		 	}
				
		int cacheCopied = 0;
		int newOffset = offset;
	
		// Are there pushback bytes available?
		int available = buf.length - pos;
		if (available > 0) {
			cacheCopied = (available >= length) ? length : available;
			System.arraycopy(buf, pos, buffer, newOffset, cacheCopied);
			newOffset += cacheCopied;
			pos += cacheCopied;
		}
	
		// Have we copied enough?
		if (cacheCopied == length) return length;

		int inCopied = in.read(buffer, newOffset, length - cacheCopied);

		if (inCopied > 0) return inCopied + cacheCopied;
		if (cacheCopied == 0) return inCopied;
		return cacheCopied;
	}
	
	/**
	 * Answer an integer comprised of the next
	 * four bytes of the input stream.
	 */
	public int readInt() throws IOException {
		byte[] buf = new byte[4];
		read(buf);
		return ((buf[3] & 0xFF) << 24) | 
			((buf[2] & 0xFF) << 16) | 
			((buf[1] & 0xFF) << 8) | 
			(buf[0] & 0xFF);
	}
	
	/**
	 * Answer a short comprised of the next
	 * two bytes of the input stream.
	 */
	public short readShort() throws IOException {
		byte[] buf = new byte[2];
		read(buf);
		return (short)(((buf[1] & 0xFF) << 8) | (buf[0] & 0xFF));
	}
	
	/**
	 * Push back the entire content of the given buffer <code>b</code>.
	 * <p>
	 * The bytes are pushed so that they would be read back b[0], b[1], etc. 
	 * If the push back buffer cannot handle the bytes copied from <code>b</code>, 
	 * an IOException will be thrown and no byte will be pushed back.
	 * </p>
	 * 
	 * @param b the byte array containing bytes to push back into the stream
	 *
	 * @exception 	java.io.IOException if the pushback buffer is too small
	 */
	public void unread(byte[] b) throws IOException {
		int length = b.length;
		if (length > pos) throw new IOException();
		position -= length;
		pos -= length;
		System.arraycopy(b, 0, buf, pos, length);
	}
}
