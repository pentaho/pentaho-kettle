/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.layout;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * Contains various layout constants
 * 
 * @since 1.0
 */
public final class LayoutConstants {
	private static Point dialogMargins = null;
	private static Point dialogSpacing = null;
	private static Point minButtonSize = null;
	
	private static void initializeConstants() {
		if (dialogMargins != null) {
			return;
		}
		
		GC gc = new GC(Display.getCurrent());
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();

		dialogMargins = new Point(Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_MARGIN),
				Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_MARGIN));

		dialogSpacing = new Point(Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING),
				Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING));

		minButtonSize  = new Point(Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH), 0);
		
		gc.dispose();
	}
	
	/**
	 * Returns the default dialog margins, in pixels
	 * 
	 * @return the default dialog margins, in pixels
	 */
    public static final Point getMargins() {
    	initializeConstants();
    	return dialogMargins;
    }

    /**
     * Returns the default dialog spacing, in pixels
     * 
     * @return the default dialog spacing, in pixels
     */
    public static final Point getSpacing() {
    	initializeConstants();
    	return dialogSpacing;
    }

    /**
     * Returns the default minimum button size, in pixels
     * 
     * @return the default minimum button size, in pixels
     */
    public static final Point getMinButtonSize() {
    	initializeConstants();
    	return minButtonSize;
    }
}
