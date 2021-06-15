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

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;

/**
 * <p>
 * Formats the keys in the internal key sequence grammar. This is used for
 * persistence, and is not really intended for display to the user.
 * </p>
 * 
 * @since 1.4
 */
public final class FormalKeyFormatter extends AbstractKeyFormatter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.KeyFormatter#format(org.eclipse.ui.keys.KeySequence)
	 */
	public String format(final int key) {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		return lookup.formalNameLookup(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#getKeyDelimiter()
	 */
	protected String getKeyDelimiter() {
		return KeyStroke.KEY_DELIMITER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.bindings.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
	 */
	protected String getKeyStrokeDelimiter() {
		return KeySequence.KEY_STROKE_DELIMITER;
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
