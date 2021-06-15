/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.ResourceBundle;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A key formatter providing the Emacs-style accelerators using single letters
 * to represent the modifier keys.
 * </p>
 * 
 * @since 1.4
 */
public final class EmacsKeyFormatter extends AbstractKeyFormatter {

	/**
	 * The resource bundle used by <code>format()</code> to translate formal
	 * string representations by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(EmacsKeyFormatter.class.getName());

	/**
	 * Formats an individual key into a human readable format. This converts the
	 * key into a format similar to Xemacs.
	 * 
	 * @param key
	 *            The key to format; must not be <code>null</code>.
	 * @return The key formatted as a string; should not be <code>null</code>.
	 */
	public String format(final int key) {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		if (lookup.isModifierKey(key)) {
			String formattedName = Util.translateString(RESOURCE_BUNDLE, lookup
					.formalNameLookup(key), null);
			if (formattedName != null) {
				return formattedName;
			}
		}

		return super.format(key).toLowerCase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#getKeyDelimiter()
	 */
	protected String getKeyDelimiter() {
		return Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY,
				KeyStroke.KEY_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
	 */
	protected String getKeyStrokeDelimiter() {
		return Util.translateString(RESOURCE_BUNDLE, KEY_STROKE_DELIMITER_KEY,
				KeySequence.KEY_STROKE_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#sortModifierKeys(int)
	 */
	protected int[] sortModifierKeys(int modifierKeys) {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		final int[] sortedKeys = new int[4];
		int index = 0;

		if ((modifierKeys & lookup.getAlt()) != 0) {
			sortedKeys[index++] = lookup.getAlt();
		}
		if ((modifierKeys & lookup.getCommand()) != 0) {
			sortedKeys[index++] = lookup.getCommand();
		}
		if ((modifierKeys & lookup.getCtrl()) != 0) {
			sortedKeys[index++] = lookup.getCtrl();
		}
		if ((modifierKeys & lookup.getShift()) != 0) {
			sortedKeys[index++] = lookup.getShift();
		}

		return sortedKeys;
	}
}
