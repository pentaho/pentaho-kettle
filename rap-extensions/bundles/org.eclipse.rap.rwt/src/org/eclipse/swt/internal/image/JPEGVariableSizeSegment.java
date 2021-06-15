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

abstract class JPEGVariableSizeSegment extends JPEGSegment {

	public JPEGVariableSizeSegment(byte[] reference) {
		super(reference);
	}
	
	public JPEGVariableSizeSegment(LEDataInputStream byteStream) {
		try {
			byte[] header = new byte[4];
			byteStream.read(header);
			reference = header; // to use getSegmentLength()
			byte[] contents = new byte[getSegmentLength() + 2];
			contents[0] = header[0];
			contents[1] = header[1];
			contents[2] = header[2];
			contents[3] = header[3];
			byteStream.read(contents, 4, contents.length - 4);
			reference = contents;
		} catch (Exception e) {
			SWT.error(SWT.ERROR_IO, e);
		}
	}
}
