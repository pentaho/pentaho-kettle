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

final class JPEGScanHeader extends JPEGVariableSizeSegment {
	public int[][] componentParameters;

public JPEGScanHeader(byte[] reference) {
	super(reference);
}

public JPEGScanHeader(LEDataInputStream byteStream) {
	super(byteStream);
	initializeComponentParameters();
}

public int getApproxBitPositionHigh() {
	return reference[(2 * getNumberOfImageComponents()) + 7] >> 4;
}

public int getApproxBitPositionLow() {
	return reference[(2 * getNumberOfImageComponents()) + 7] & 0xF;
}

public int getEndOfSpectralSelection() {
	return reference[(2 * getNumberOfImageComponents()) + 6];
}

public int getNumberOfImageComponents() {
	return reference[4];
}

public int getStartOfSpectralSelection() {
	return reference[(2 * getNumberOfImageComponents()) + 5];
}

/* Used when decoding. */
void initializeComponentParameters() {
	int compCount = getNumberOfImageComponents();
	componentParameters = new int[0][];
	for (int i = 0; i < compCount; i++) {
		int ofs = 5 + i * 2;
		int cid = reference[ofs] & 0xFF;
		int dc = (reference[ofs + 1] & 0xFF) >> 4;
		int ac = reference[ofs + 1] & 0xF;
		if (componentParameters.length <= cid) {
			int[][] newParams = new int[cid + 1][];
			System.arraycopy(componentParameters, 0, newParams, 0, componentParameters.length);
			componentParameters = newParams;
		}
		componentParameters[cid] = new int[] { dc, ac };
	}
}

/* Used when encoding. */
public void initializeContents() {
	int compCount = getNumberOfImageComponents();
	int[][] compSpecParams = componentParameters;
	if (compCount == 0 || compCount != compSpecParams.length) {
		SWT.error(SWT.ERROR_INVALID_IMAGE);
	}
	for (int i = 0; i < compCount; i++) {
		int ofs = i * 2 + 5;
		int[] compParams = compSpecParams[i];
		reference[ofs] = (byte)(i + 1);
		reference[ofs + 1] = (byte)(compParams[0] * 16 + compParams[1]);
	}
}

public void setEndOfSpectralSelection(int anInteger) {
	reference[(2 * getNumberOfImageComponents()) + 6] = (byte)anInteger;
}

public void setNumberOfImageComponents(int anInteger) {
	reference[4] = (byte)(anInteger & 0xFF);
}

public void setStartOfSpectralSelection(int anInteger) {
	reference[(2 * getNumberOfImageComponents()) + 5] = (byte)anInteger;
}

@Override
public int signature() {
	return JPEGFileFormat.SOS;
}

public boolean verifyProgressiveScan() {
	int start = getStartOfSpectralSelection();
	int end = getEndOfSpectralSelection();
	int low = getApproxBitPositionLow();
	int high = getApproxBitPositionHigh();
	int count = getNumberOfImageComponents();
	if ((start == 0 && end == 00) || (start <= end && end <= 63)) {
		if (low <= 13 && high <= 13 && (high == 0 || high == low + 1)) {
			return start == 0 || (start > 0 && count == 1);
		}
	}
	return false;
}

public boolean isACProgressiveScan() {
	return getStartOfSpectralSelection() != 0 && getEndOfSpectralSelection() != 0;
}

public boolean isDCProgressiveScan() {
	return getStartOfSpectralSelection() == 0 && getEndOfSpectralSelection() == 0;
}

public boolean isFirstScan() {
	return getApproxBitPositionHigh() == 0;
}

}
