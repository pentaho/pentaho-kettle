/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.layout;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.Dialog;

/**
 * PixelConverter performs various conversions from device-independent units
 * (such as DLUs or characters) to pixels. It can be associated with a control or
 * a font. In the case of a control, the font used by the control at the time
 * the PixelConverter is created is used for the pixel calculations. In the case
 * of a specific font, the supplied font is used for the calculations.
 * 
 * The control and/or font must not be disposed at the time the PixelConverter
 * is created.
 * 
 * @since 1.3
 */
public class PixelConverter {

	private final FontMetrics fontMetrics;

	/**
	 * Create a PixelConverter which will convert device-independent units to
	 * pixels using the font of the specified control.
	 * 
	 * @param control
	 *            the control whose font should be used for pixel conversions.
	 *            Note that the font used by the control at the time this
	 *            constructor is called is the font that will be used for all
	 *            calculations. If the font of the specified control is changed
	 *            after this PixelConverter is created, then the conversions
	 *            from this instance will not produce the desired effect.
	 */
	public PixelConverter(Control control) {
		this(control.getFont());
	}

	/**
	 * Create a PixelConverter which will convert device-independent units to
	 * pixels using the specified font.
	 * 
	 * @param font
	 *            the font that should be used for pixel conversions.
	 */
	public PixelConverter(Font font) {
		GC gc = new GC(font.getDevice());
		gc.setFont(font);
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

	/**
	 * Returns the number of pixels corresponding to the height of the given
	 * number of characters.
	 * 
	 * @param chars
	 *            the number of characters
	 * @return the number of pixels
	 */
	public int convertHeightInCharsToPixels(int chars) {
		return Dialog.convertHeightInCharsToPixels(fontMetrics, chars);
	}

	/**
	 * Returns the number of pixels corresponding to the given number of
	 * horizontal dialog units.
	 * 
	 * @param dlus
	 *            the number of horizontal dialog units
	 * @return the number of pixels
	 */
	public int convertHorizontalDLUsToPixels(int dlus) {
		return Dialog.convertHorizontalDLUsToPixels(fontMetrics, dlus);
	}

	/**
	 * Returns the number of pixels corresponding to the given number of
	 * vertical dialog units.
	 * 
	 * @param dlus
	 *            the number of vertical dialog units
	 * @return the number of pixels
	 */
	public int convertVerticalDLUsToPixels(int dlus) {
		return Dialog.convertVerticalDLUsToPixels(fontMetrics, dlus);
	}

	/**
	 * Returns the number of pixels corresponding to the width of the given
	 * number of characters.
	 * 
	 * @param chars
	 *            the number of characters
	 * @return the number of pixels
	 */
	public int convertWidthInCharsToPixels(int chars) {
		return Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
	}

}
