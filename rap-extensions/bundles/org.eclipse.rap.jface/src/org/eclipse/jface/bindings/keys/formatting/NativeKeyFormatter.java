/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;

/**
 * <p>
 * Formats the key sequences and key strokes into the native human-readable
 * format. This is typically what you would see on the menus for the given
 * platform and locale.
 * </p>
 * 
 * @since 1.4
 */
public final class NativeKeyFormatter extends AbstractKeyFormatter {

    //RAP [if]
    private static final String RAP_PLATFORM = "rap"; //$NON-NLS-1$
    // RAPEND [if]

	/**
	 * The key into the internationalization resource bundle for the delimiter
	 * to use between keys (on the Carbon platform).
	 */
	private final static String CARBON_KEY_DELIMITER_KEY = "CARBON_KEY_DELIMITER"; //$NON-NLS-1$

	/**
	 * A look-up table for the string representations of various carbon keys.
	 */
	private final static HashMap CARBON_KEY_LOOK_UP = new HashMap();

	/**
	 * The resource bundle used by <code>format()</code> to translate formal
	 * string representations by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE;

	/**
	 * The key into the internationalization resource bundle for the delimiter
	 * to use between key strokes (on the Win32 platform).
	 */
	private final static String WIN32_KEY_STROKE_DELIMITER_KEY = "WIN32_KEY_STROKE_DELIMITER"; //$NON-NLS-1$

	static {
		RESOURCE_BUNDLE = ResourceBundle.getBundle(NativeKeyFormatter.class.getName());

		final String carbonBackspace = "\u232B"; //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.BS_NAME, carbonBackspace);
		CARBON_KEY_LOOK_UP.put(IKeyLookup.BACKSPACE_NAME, carbonBackspace);
		CARBON_KEY_LOOK_UP.put(IKeyLookup.CR_NAME, "\u21A9"); //$NON-NLS-1$
		final String carbonDelete = "\u2326"; //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.DEL_NAME, carbonDelete);
		CARBON_KEY_LOOK_UP.put(IKeyLookup.DELETE_NAME, carbonDelete);
		CARBON_KEY_LOOK_UP.put(IKeyLookup.ALT_NAME, "\u2325"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.COMMAND_NAME, "\u2318"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.CTRL_NAME, "\u2303"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.SHIFT_NAME, "\u21E7"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.ARROW_DOWN_NAME, "\u2193"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.ARROW_LEFT_NAME, "\u2190"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.ARROW_RIGHT_NAME, "\u2192"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.ARROW_UP_NAME, "\u2191"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.END_NAME, "\u2198"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.NUMPAD_ENTER_NAME, "\u2324"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.HOME_NAME, "\u2196"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.PAGE_DOWN_NAME, "\u21DF"); //$NON-NLS-1$
		CARBON_KEY_LOOK_UP.put(IKeyLookup.PAGE_UP_NAME, "\u21DE"); //$NON-NLS-1$
	}

	/**
	 * Formats an individual key into a human readable format. This uses an
	 * internationalization resource bundle to look up the key. This does the
	 * platform-specific formatting for Carbon.
	 * 
	 * @param key
	 *            The key to format.
	 * @return The key formatted as a string; should not be <code>null</code>.
	 */
	public final String format(final int key) {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		final String name = lookup.formalNameLookup(key);

		// TODO consider platform-specific resource bundles
		if (Util.isMac()) {    	
			String formattedName = (String) CARBON_KEY_LOOK_UP.get(name);
			if (formattedName != null) {
				return formattedName;
			}
		}

		return super.format(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#getKeyDelimiter()
	 */
	protected String getKeyDelimiter() {
		// We must do the look up every time, as our locale might change.
		if (Util.isMac()) {
//			return Util.translateString(RESOURCE_BUNDLE,
			return Util.translateString(getResourceBundle( NativeKeyFormatter.class ),
					CARBON_KEY_DELIMITER_KEY, Util.ZERO_LENGTH_STRING);
		}

//		return Util.translateString(RESOURCE_BUNDLE,
		return Util.translateString(getResourceBundle( NativeKeyFormatter.class ), 
		        KEY_DELIMITER_KEY, KeyStroke.KEY_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
	 */
	protected String getKeyStrokeDelimiter() {
		// We must do the look up every time, as our locale might change.
		if (Util.isWindows()) {
//			return Util.translateString(RESOURCE_BUNDLE,
	        return Util.translateString(getResourceBundle( NativeKeyFormatter.class ),
					WIN32_KEY_STROKE_DELIMITER_KEY, KeySequence.KEY_STROKE_DELIMITER);
		}

//		return Util.translateString(RESOURCE_BUNDLE,
	    return Util.translateString(getResourceBundle( NativeKeyFormatter.class ), 
	            KEY_STROKE_DELIMITER_KEY, KeySequence.KEY_STROKE_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#sortModifierKeys(int)
	 */
	protected int[] sortModifierKeys(final int modifierKeys) {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		final int[] sortedKeys = new int[4];
		int index = 0;

		// RAP [if] Sort modifiers in RAP as in Windows - see bug 410319
		if (Util.isWindows() || SWT.getPlatform().equals(RAP_PLATFORM)) {
			if ((modifierKeys & lookup.getCtrl()) != 0) {
				sortedKeys[index++] = lookup.getCtrl();
			}
			if ((modifierKeys & lookup.getAlt()) != 0) {
				sortedKeys[index++] = lookup.getAlt();
			}
			if ((modifierKeys & lookup.getShift()) != 0) {
				sortedKeys[index++] = lookup.getShift();
			}

		} else if (Util.isGtk() || Util.isMotif()) {
			if ((modifierKeys & lookup.getShift()) != 0) {
				sortedKeys[index++] = lookup.getShift();
			}
			if ((modifierKeys & lookup.getCtrl()) != 0) {
				sortedKeys[index++] = lookup.getCtrl();
			}
			if ((modifierKeys & lookup.getAlt()) != 0) {
				sortedKeys[index++] = lookup.getAlt();
			}

		} else if (Util.isMac()) {
			if ((modifierKeys & lookup.getShift()) != 0) {
				sortedKeys[index++] = lookup.getShift();
			}
			if ((modifierKeys & lookup.getCtrl()) != 0) {
				sortedKeys[index++] = lookup.getCtrl();
			}
			if ((modifierKeys & lookup.getAlt()) != 0) {
				sortedKeys[index++] = lookup.getAlt();
			}
			if ((modifierKeys & lookup.getCommand()) != 0) {
				sortedKeys[index++] = lookup.getCommand();
			}

		}

		return sortedKeys;
	}
}
