/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

final class TIFFRandomFileAccess {

	LEDataInputStream inputStream;
	int start, current, next;
	byte[][] buffers;

	static final int CHUNK_SIZE = 8192;
	static final int LIST_SIZE = 128;

public TIFFRandomFileAccess(LEDataInputStream stream) {
	inputStream = stream;
	start = current = next = inputStream.getPosition();
	buffers = new byte[LIST_SIZE][];
}

void seek(int pos) throws IOException {
	if (pos == current) return;
	if (pos < start) throw new IOException();
	current = pos;	
	if (current > next) {
		int n = current - next;
		/* store required bytes */
		int index = next / CHUNK_SIZE;
		int offset = next % CHUNK_SIZE;
		while (n > 0) {
			if (index >= buffers.length) {
				byte[][] oldBuffers = buffers;
				buffers = new byte[Math.max(index + 1, oldBuffers.length + LIST_SIZE)][];
				System.arraycopy(oldBuffers, 0, buffers, 0, oldBuffers.length);
			}
			if (buffers[index] == null) buffers[index] = new byte[CHUNK_SIZE];
			int cnt = inputStream.read(buffers[index], offset, Math.min(n, CHUNK_SIZE - offset));
			n -= cnt;
			next += cnt;
			index++;
			offset = 0;
		}
	}
}

void read(byte b[]) throws IOException {
	int size = b.length;
	int nCached = Math.min(size, next - current);
	int nMissing = size - next + current;
	int destNext = 0;
	if (nCached > 0) {
		/* Get cached bytes */
		int index = current / CHUNK_SIZE;
		int offset = current % CHUNK_SIZE;		
		while (nCached > 0) {
			int cnt = Math.min(nCached, CHUNK_SIZE - offset);
			System.arraycopy(buffers[index], offset, b, destNext, cnt);
			nCached -= cnt; 
			destNext += cnt;
			index++;
			offset = 0;
		}
	}
	if (nMissing > 0) {
		/* Read required bytes */
		int index = next / CHUNK_SIZE;
		int offset = next % CHUNK_SIZE;
		while (nMissing > 0) {
			if (index >= buffers.length) {
				byte[][] oldBuffers = buffers;
				buffers = new byte[Math.max(index, oldBuffers.length + LIST_SIZE)][];
				System.arraycopy(oldBuffers, 0, buffers, 0, oldBuffers.length);
			}
			if (buffers[index] == null) buffers[index] = new byte[CHUNK_SIZE];
			int cnt = inputStream.read(buffers[index], offset, Math.min(nMissing, CHUNK_SIZE - offset));
			System.arraycopy(buffers[index], offset, b, destNext, cnt);
			nMissing -= cnt;
			next += cnt;
			destNext += cnt;
			index++;
			offset = 0;
		}
	}
	current += size;
}

}
