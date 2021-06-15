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

abstract class JPEGFixedSizeSegment extends JPEGSegment {

	public JPEGFixedSizeSegment() {
		reference = new byte[fixedSize()];
		setSegmentMarker(signature());
	}
	
	public JPEGFixedSizeSegment(byte[] reference) {
		super(reference);
	}
	
	public JPEGFixedSizeSegment(LEDataInputStream byteStream) {
		reference = new byte[fixedSize()];
		try {
			byteStream.read(reference);
		} catch (Exception e) { 
			SWT.error(SWT.ERROR_IO, e);
		}
	}
	
	abstract public int fixedSize();

	@Override
	public int getSegmentLength() {
		return fixedSize() - 2;
	}
	
	@Override
	public void setSegmentLength(int length) {
	}
}
