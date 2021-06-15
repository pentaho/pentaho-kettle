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


final class JPEGAppn extends JPEGVariableSizeSegment {

	public JPEGAppn(byte[] reference) {
		super(reference);
	}
	
	public JPEGAppn(LEDataInputStream byteStream) {
		super(byteStream);
	}
	
	@Override
	public boolean verify() {
		int marker = getSegmentMarker();
		return marker >= JPEGFileFormat.APP0 && marker <= JPEGFileFormat.APP15;
	}
}
