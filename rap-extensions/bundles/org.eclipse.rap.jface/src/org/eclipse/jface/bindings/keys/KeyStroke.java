/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys;

import java.util.StringTokenizer;

import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A <code>KeyStroke</code> is defined as an optional set of modifier keys
 * followed optionally by a natural key. A <code>KeyStroke</code> is said to
 * be complete if it contains a natural key. A natural key is any Unicode
 * character (e.g., "backspace", etc.), any character belonging to a natural
 * language (e.g., "A", "1", "[", etc.), or any special control character
 * specific to computers (e.g., "F10", "PageUp", etc.).
 * </p>
 * <p>
 * All <code>KeyStroke</code> objects have a formal string representation
 * available via the <code>toString()</code> method. There are a number of
 * methods to get instances of <code>KeyStroke</code> objects, including one
 * which can parse this formal string representation.
 * </p>
 * <p>
 * All <code>KeyStroke</code> objects, via the <code>format()</code> method,
 * provide a version of their formal string representation translated by
 * platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>KeyStroke</code> objects are immutable. Clients are not permitted to
 * extend this class.
 * </p>
 * 
 * @since 1.4
 */
public final class KeyStroke extends Trigger implements Comparable {

	/**
	 * The delimiter between multiple keys in a single key strokes -- expressed
	 * in the formal key stroke grammar. This is not to be displayed to the
	 * user. It is only intended as an internal representation.
	 */
	public static final String KEY_DELIMITER = "\u002B"; //$NON-NLS-1$

	/**
	 * The set of delimiters for <code>Key</code> objects allowed during
	 * parsing of the formal string representation.
	 */
	public static final String KEY_DELIMITERS = KEY_DELIMITER;

	/**
	 * The representation for no key.
	 */
	public static final int NO_KEY = 0;

	/**
	 * Creates an instance of <code>KeyStroke</code> given a natural key.
	 * 
	 * @param naturalKey
	 *            the natural key. The format of this integer is defined by
	 *            whichever widget toolkit you are using; <code>NO_KEY</code>
	 *            always means no natural key.
	 * @return a key stroke. This key stroke will have no modifier keys.
	 *         Guaranteed not to be <code>null</code>.
	 * @see SWTKeySupport
	 */
	public static final KeyStroke getInstance(final int naturalKey) {
		return new KeyStroke(NO_KEY, naturalKey);
	}

	/**
	 * Creates an instance of <code>KeyStroke</code> given a set of modifier keys
	 * and a natural key.
	 * 
	 * @param modifierKeys
	 *            the modifier keys. The format of this integer is defined by
	 *            whichever widget toolkit you are using; <code>NO_KEY</code>
	 *            always means no modifier keys.
	 * @param naturalKey
	 *            the natural key. The format of this integer is defined by
	 *            whichever widget toolkit you are using; <code>NO_KEY</code>
	 *            always means no natural key.
	 * @return a key stroke. Guaranteed not to be <code>null</code>.
	 * @see SWTKeySupport
	 */
	public static final KeyStroke getInstance(final int modifierKeys,
			final int naturalKey) {
		return new KeyStroke(modifierKeys, naturalKey);
	}

	/**
	 * Creates an instance of <code>KeyStroke</code> by parsing a given a formal
	 * string representation.
	 * 
	 * @param string
	 *            the formal string representation to parse.
	 * @return a key stroke. Guaranteed not to be <code>null</code>.
	 * @throws ParseException
	 *             if the given formal string representation could not be parsed
	 *             to a valid key stroke.
	 */
	public static final KeyStroke getInstance(final String string)
			throws ParseException {
		if (string == null) {
			throw new NullPointerException("Cannot parse a null string"); //$NON-NLS-1$
		}

		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		int modifierKeys = NO_KEY;
		int naturalKey = NO_KEY;
		final StringTokenizer stringTokenizer = new StringTokenizer(string,
				KEY_DELIMITERS, true);
		int i = 0;

		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();

			if (i % 2 == 0) {
				if (stringTokenizer.hasMoreTokens()) {
					token = token.toUpperCase();
					final int modifierKey = lookup.formalModifierLookup(token);
					if (modifierKey == NO_KEY) {
						throw new ParseException(
								"Cannot create key stroke with duplicate or non-existent modifier key: " //$NON-NLS-1$
										+ token);
					}

					modifierKeys |= modifierKey;

				} else if (token.length() == 1) {
					naturalKey = token.charAt(0);

				} else {
					token = token.toUpperCase();
					naturalKey = lookup.formalKeyLookup(token);
				}
			}

			i++;
		}

		return new KeyStroke(modifierKeys, naturalKey);
	}

	/**
	 * An integer representation of the modifier keys; <code>NO_KEY</code>
	 * means that there is no modifier key.
	 */
	private final int modifierKeys;

	/**
	 * The natural key for this key stroke. This value is <code>NO_KEY</code>
	 * if the key stroke is incomplete (i.e., has no natural key).
	 */
	private final int naturalKey;

	/**
	 * Constructs an instance of <code>KeyStroke</code> given a set of
	 * modifier keys and a natural key.
	 * 
	 * @param modifierKeys
	 *            the modifier keys. The format of this integer is defined by
	 *            whichever widget toolkit you are using; <code>NO_KEY</code>
	 *            always means no modifier keys.
	 * @param naturalKey
	 *            the natural key. The format of this integer is defined by
	 *            whichever widget toolkit you are using; <code>NO_KEY</code>
	 *            always means no natural key.
	 * @see SWTKeySupport
	 */
	private KeyStroke(final int modifierKeys, final int naturalKey) {
		this.modifierKeys = modifierKeys;
		this.naturalKey = naturalKey;
	}

    /*
     * (non-Javadoc)
     * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public final int compareTo(final Object object) {
		final KeyStroke keyStroke = (KeyStroke) object;
		int compareTo = Util.compare(modifierKeys, keyStroke.modifierKeys);

		if (compareTo == 0) {
			compareTo = Util.compare(naturalKey, keyStroke.naturalKey);
		}

		return compareTo;
	}

    /*
     * (non-Javadoc)
     * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(final Object object) {
		if (!(object instanceof KeyStroke)) {
			return false;
		}

		final KeyStroke keyStroke = (KeyStroke) object;
		if (modifierKeys != keyStroke.modifierKeys) {
			return false;
		}

		return (naturalKey == keyStroke.naturalKey);
	}

	/**
	 * Formats this key stroke into the current default look.
	 * 
	 * @return A string representation for this key stroke using the default
	 *         look; never <code>null</code>.
	 */
	public final String format() {
		return KeyFormatterFactory.getDefault().format(this);
	}

	/**
	 * Returns the modifier keys for this key stroke.
	 * 
	 * @return the bit mask of modifier keys; <code>NO_KEY</code> means that
	 *         there is no modifier key.
	 */
	public final int getModifierKeys() {
		return modifierKeys;
	}

	/**
	 * Returns the natural key for this key stroke.
	 * 
	 * @return The natural key for this key stroke. This value is
	 *         <code>NO_KEY</code> if the key stroke is incomplete (i.e., has
	 *         no natural key).
	 */
	public final int getNaturalKey() {
		return naturalKey;
	}

    /*
     * (non-Javadoc)
     * 
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return modifierKeys << 4 + naturalKey;
	}

	/**
	 * Returns whether or not this key stroke is complete. Key strokes are
	 * complete iff they have a natural key which is not <code>NO_KEY</code>.
	 * 
	 * @return <code>true</code>, iff the key stroke is complete.
	 */
	public final boolean isComplete() {
		return (naturalKey != NO_KEY);
	}

	/**
	 * Returns the formal string representation for this key stroke.
	 * 
	 * @return The formal string representation for this key stroke. Guaranteed
	 *         not to be <code>null</code>.
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return KeyFormatterFactory.getFormalKeyFormatter().format(this);
	}
}
