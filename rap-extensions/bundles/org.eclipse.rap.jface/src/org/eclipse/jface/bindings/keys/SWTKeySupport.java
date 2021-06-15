/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys;

import org.eclipse.jface.bindings.keys.formatting.IKeyFormatter;
import org.eclipse.jface.bindings.keys.formatting.NativeKeyFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * A utility class for converting SWT events into key strokes.
 * </p>
 * 
 * @since 1.4
 */
public final class SWTKeySupport {

	/**
	 * A formatter that displays key sequences in a style native to the
	 * platform.
	 */
	private static final IKeyFormatter NATIVE_FORMATTER = new NativeKeyFormatter();

	/**
	 * Given an SWT accelerator value, provide the corresponding key stroke.
	 * 
	 * @param accelerator
	 *            The accelerator to convert; should be a valid SWT accelerator
	 *            value.
	 * @return The equivalent key stroke; never <code>null</code>.
	 */
	public static final KeyStroke convertAcceleratorToKeyStroke(int accelerator) {
		final int modifierKeys = accelerator & SWT.MODIFIER_MASK;
		final int naturalKey;
		if (accelerator == modifierKeys) {
			naturalKey = KeyStroke.NO_KEY;
		} else {
			naturalKey = accelerator - modifierKeys;
		}
		
		return KeyStroke.getInstance(modifierKeys, naturalKey);
	}

	/**
	 * <p>
	 * Converts the given event into an SWT accelerator value -- considering the
	 * modified character with the shift modifier. This is the third accelerator
	 * value that should be checked when processing incoming key events.
	 * </p>
	 * <p>
	 * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
	 * "Ctrl+Shift+%".
	 * </p>
	 * 
	 * @param event
	 *            The event to be converted; must not be <code>null</code>.
	 * @return The combination of the state mask and the unmodified character.
	 */
	public static final int convertEventToModifiedAccelerator(final Event event) {
		int modifiers = event.stateMask & SWT.MODIFIER_MASK;
		char character = topKey(event);
		return modifiers + toUpperCase(character);
	}

	/**
	 * <p>
	 * Converts the given event into an SWT accelerator value -- considering the
	 * unmodified character with all modifier keys. This is the first
	 * accelerator value that should be checked when processing incoming key
	 * events. However, all alphabetic characters are considered as their
	 * uppercase equivalents.
	 * </p>
	 * <p>
	 * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
	 * "Ctrl+Shift+5".
	 * </p>
	 * 
	 * @param event
	 *            The event to be converted; must not be <code>null</code>.
	 * @return The combination of the state mask and the unmodified character.
	 */
	public static final int convertEventToUnmodifiedAccelerator(
			final Event event) {
		return convertEventToUnmodifiedAccelerator(event.stateMask,
				event.keyCode);
	}

	/**
	 * <p>
	 * Converts the given state mask and key code into an SWT accelerator value --
	 * considering the unmodified character with all modifier keys. All
	 * alphabetic characters are considered as their uppercase equivalents.
	 * </p>
	 * <p>
	 * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
	 * "Ctrl+Shift+5".
	 * </p>
	 * 
	 * @param stateMask
	 *            The integer mask of modifiers keys depressed when this was
	 *            pressed.
	 * @param keyCode
	 *            The key that was pressed, before being modified.
	 * @return The combination of the state mask and the unmodified character.
	 */
	private static final int convertEventToUnmodifiedAccelerator(
			final int stateMask, final int keyCode) {
		int modifiers = stateMask & SWT.MODIFIER_MASK;
		int character = keyCode;
		return modifiers + toUpperCase(character);
	}

	/**
	 * <p>
	 * Converts the given event into an SWT accelerator value -- considering the
	 * unmodified character with all modifier keys. This is the first
	 * accelerator value that should be checked. However, all alphabetic
	 * characters are considered as their uppercase equivalents.
	 * </p>
	 * <p>
	 * For example, on a standard US keyboard, "Ctrl+Shift+5" would be viewed as
	 * "Ctrl+%".
	 * </p>
	 * 
	 * @param event
	 *            The event to be converted; must not be <code>null</code>.
	 * @return The combination of the state mask and the unmodified character.
	 */
	public static final int convertEventToUnmodifiedAccelerator(
			final KeyEvent event) {
		return convertEventToUnmodifiedAccelerator(event.stateMask,
				event.keyCode);
	}

	/**
	 * Converts the given event into an SWT accelerator value -- considering the
	 * modified character without the shift modifier. This is the second
	 * accelerator value that should be checked when processing incoming key
	 * events. Key strokes with alphabetic natural keys are run through
	 * <code>convertEventToUnmodifiedAccelerator</code>.
	 * 
	 * @param event
	 *            The event to be converted; must not be <code>null</code>.
	 * @return The combination of the state mask without shift, and the modified
	 *         character.
	 */
	public static final int convertEventToUnshiftedModifiedAccelerator(
			final Event event) {
		// Disregard alphabetic key strokes.
		if (Character.isLetter((char) event.keyCode)) {
			return convertEventToUnmodifiedAccelerator(event);
		}

		int modifiers = event.stateMask & (SWT.MODIFIER_MASK ^ SWT.SHIFT);
		char character = topKey(event);
		return modifiers + toUpperCase(character);
	}

	/**
	 * Given a key stroke, this method provides the equivalent SWT accelerator
	 * value. The functional inverse of
	 * <code>convertAcceleratorToKeyStroke</code>.
	 * 
	 * @param keyStroke
	 *            The key stroke to convert; must not be <code>null</code>.
	 * @return The SWT accelerator value
	 */
	public static final int convertKeyStrokeToAccelerator(
			final KeyStroke keyStroke) {
		return keyStroke.getModifierKeys() + keyStroke.getNaturalKey();
	}

	/**
	 * Provides an instance of <code>IKeyFormatter</code> appropriate for the
	 * current instance.
	 * 
	 * @return an instance of <code>IKeyFormatter</code> appropriate for the
	 *         current instance; never <code>null</code>.
	 */
	public static IKeyFormatter getKeyFormatterForPlatform() {
		return NATIVE_FORMATTER;
	}

	/**
	 * Makes sure that a fully-modified character is converted to the normal
	 * form. This means that "Ctrl+" key strokes must reverse the modification
	 * caused by control-escaping. Also, all lower case letters are converted to
	 * uppercase.
	 * 
	 * @param event
	 *            The event from which the fully-modified character should be
	 *            pulled.
	 * @return The modified character, uppercase and without control-escaping.
	 */
	private static final char topKey(final Event event) {
		char character = event.character;
		boolean ctrlDown = (event.stateMask & SWT.CTRL) != 0;

		if (ctrlDown && event.character != event.keyCode
				&& event.character < 0x20 
				&& (event.keyCode & SWT.KEYCODE_BIT) == 0) {
			character += 0x40;
		}

		return character;
	}

	/**
	 * Makes the given character uppercase if it is a letter.
	 * 
	 * @param keyCode
	 *            The character to convert.
	 * @return The uppercase equivalent, if any; otherwise, the character
	 *         itself.
	 */
	private static final int toUpperCase(int keyCode) {
		// Will this key code be truncated?
		if (keyCode > 0xFFFF) {
			return keyCode;
		}

		// Downcast in safety. Only make characters uppercase.
		final char character = (char) keyCode;
		return Character.isLetter(character) ? Character.toUpperCase(character)
				: keyCode;
	}

	/**
	 * This class should never be instantiated.
	 */
	protected SWTKeySupport() {
		// This class should never be instantiated.
	}
}
