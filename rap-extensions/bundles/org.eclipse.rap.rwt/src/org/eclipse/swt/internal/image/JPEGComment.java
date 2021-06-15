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


final class JPEGComment extends JPEGVariableSizeSegment {

	public JPEGComment(byte[] reference) {
		super(reference);
	}
	
	public JPEGComment(LEDataInputStream byteStream) {
		super(byteStream);
	}
	
	@Override
	public int signature() {
		return JPEGFileFormat.COM;
	}
}
