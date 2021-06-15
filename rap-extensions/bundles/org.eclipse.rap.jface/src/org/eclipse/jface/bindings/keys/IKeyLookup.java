/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.bindings.keys;

/**
 * <p>
 * A facilitiy for converting the formal representation for key strokes
 * (i.e., used in persistence) into real key stroke instances.
 * </p>
 * 
 * @since 1.4
 */
public interface IKeyLookup {
	/**
	 * The formal name of the 'Alt' key.
	 */
	public static final String ALT_NAME = "ALT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Down' key.
	 */
	public static final String ARROW_DOWN_NAME = "ARROW_DOWN"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Left' key.
	 */
	public static final String ARROW_LEFT_NAME = "ARROW_LEFT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Right' key.
	 */
	public static final String ARROW_RIGHT_NAME = "ARROW_RIGHT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Arrow Up' key.
	 */
	public static final String ARROW_UP_NAME = "ARROW_UP"; //$NON-NLS-1$
	
	/**
	 * An alternate name for the backspace key.
	 */
	public static final String BACKSPACE_NAME = "BACKSPACE"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Break' key.
	 */
	public static final String BREAK_NAME = "BREAK"; //$NON-NLS-1$

	/**
	 * The formal name of the backspace key.
	 */
	public static final String BS_NAME = "BS"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Caps Lock' key.
	 */
	public static final String CAPS_LOCK_NAME = "CAPS_LOCK"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Command' key.
	 */
	public static final String COMMAND_NAME = "COMMAND"; //$NON-NLS-1$

	/**
	 * The formal name of the carriage return (U+000D)
	 */
	public static final String CR_NAME = "CR"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Ctrl' key.
	 */
	public static final String CTRL_NAME = "CTRL"; //$NON-NLS-1$

	/**
	 * The formal name of the delete (U+007F) key
	 */
	public static final String DEL_NAME = "DEL"; //$NON-NLS-1$
	
	/**
	 * An alternative name for the delete key.
	 */
	public static final String DELETE_NAME = "DELETE"; //$NON-NLS-1$

	/**
	 * The formal name of the 'End' key.
	 */
	public static final String END_NAME = "END"; //$NON-NLS-1$
	
	/**
	 * An alternative name for the enter key.
	 */
	public static final String ENTER_NAME = "ENTER"; //$NON-NLS-1$

	/**
	 * The formal name of the escape (U+001B) key.
	 */
	public static final String ESC_NAME = "ESC"; //$NON-NLS-1$
	
	/**
	 * An alternative name for the escape key.
	 */
	public static final String ESCAPE_NAME = "ESCAPE"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F1' key.
	 */
	public static final String F1_NAME = "F1"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F10' key.
	 */
	public static final String F10_NAME = "F10"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F11' key.
	 */
	public static final String F11_NAME = "F11"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F12' key.
	 */
	public static final String F12_NAME = "F12"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F13' key.
	 */
	public static final String F13_NAME = "F13"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F14' key.
	 */
	public static final String F14_NAME = "F14"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F15' key.
	 */
	public static final String F15_NAME = "F15"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F16' key.
	 */
	public static final String F16_NAME = "F16"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F17' key.
	 */
	public static final String F17_NAME = "F17"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F18' key.
	 */
	public static final String F18_NAME = "F18"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F19' key.
	 */
	public static final String F19_NAME = "F19"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F20' key.
	 */
	public static final String F20_NAME = "F20"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F2' key.
	 */
	public static final String F2_NAME = "F2"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F3' key.
	 */
	public static final String F3_NAME = "F3"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F4' key.
	 */
	public static final String F4_NAME = "F4"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F5' key.
	 */
	public static final String F5_NAME = "F5"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F6' key.
	 */
	public static final String F6_NAME = "F6"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F7' key.
	 */
	public static final String F7_NAME = "F7"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F8' key.
	 */
	public static final String F8_NAME = "F8"; //$NON-NLS-1$

	/**
	 * The formal name of the 'F9' key.
	 */
	public static final String F9_NAME = "F9"; //$NON-NLS-1$

	/**
	 * The formal name of the form feed (U+000C) key.
	 */
	public static final String FF_NAME = "FF"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Home' key.
	 */
	public static final String HOME_NAME = "HOME"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Insert' key.
	 */
	public static final String INSERT_NAME = "INSERT"; //$NON-NLS-1$

	/**
	 * The formal name of the line feed (U+000A) key.
	 */
	public static final String LF_NAME = "LF"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M1' key.
	 */
	public static final String M1_NAME = "M1"; //$NON-NLS-1$	

	/**
	 * The formal name of the 'M2' key.
	 */
	public static final String M2_NAME = "M2"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M3' key.
	 */
	public static final String M3_NAME = "M3"; //$NON-NLS-1$

	/**
	 * The formal name of the 'M4' key.
	 */
	public static final String M4_NAME = "M4"; //$NON-NLS-1$

	/**
	 * The formal name of the null (U+0000) key.
	 */
	public static final String NUL_NAME = "NUL"; //$NON-NLS-1$

	/**
	 * The formal name of the 'NumLock' key.
	 */
	public static final String NUM_LOCK_NAME = "NUM_LOCK"; //$NON-NLS-1$

	/**
	 * The formal name of the '0' key on the numpad.
	 */
	public static final String NUMPAD_0_NAME = "NUMPAD_0"; //$NON-NLS-1$

	/**
	 * The formal name of the '1' key on the numpad.
	 */
	public static final String NUMPAD_1_NAME = "NUMPAD_1"; //$NON-NLS-1$

	/**
	 * The formal name of the '2' key on the numpad.
	 */
	public static final String NUMPAD_2_NAME = "NUMPAD_2"; //$NON-NLS-1$

	/**
	 * The formal name of the '3' key on the numpad.
	 */
	public static final String NUMPAD_3_NAME = "NUMPAD_3"; //$NON-NLS-1$

	/**
	 * The formal name of the '4' key on the numpad.
	 */
	public static final String NUMPAD_4_NAME = "NUMPAD_4"; //$NON-NLS-1$

	/**
	 * The formal name of the '5' key on the numpad.
	 */
	public static final String NUMPAD_5_NAME = "NUMPAD_5"; //$NON-NLS-1$

	/**
	 * The formal name of the '6' key on the numpad.
	 */
	public static final String NUMPAD_6_NAME = "NUMPAD_6"; //$NON-NLS-1$

	/**
	 * The formal name of the '7' key on the numpad.
	 */
	public static final String NUMPAD_7_NAME = "NUMPAD_7"; //$NON-NLS-1$

	/**
	 * The formal name of the '8' key on the numpad.
	 */
	public static final String NUMPAD_8_NAME = "NUMPAD_8"; //$NON-NLS-1$

	/**
	 * The formal name of the '9' key on the numpad.
	 */
	public static final String NUMPAD_9_NAME = "NUMPAD_9"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Add' key on the numpad.
	 */
	public static final String NUMPAD_ADD_NAME = "NUMPAD_ADD"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Decimal' key on the numpad.
	 */
	public static final String NUMPAD_DECIMAL_NAME = "NUMPAD_DECIMAL"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Divide' key on the numpad.
	 */
	public static final String NUMPAD_DIVIDE_NAME = "NUMPAD_DIVIDE"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Enter' key on the numpad.
	 */
	public static final String NUMPAD_ENTER_NAME = "NUMPAD_ENTER"; //$NON-NLS-1$

	/**
	 * The formal name of the '=' key on the numpad.
	 */
	public static final String NUMPAD_EQUAL_NAME = "NUMPAD_EQUAL"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Multiply' key on the numpad.
	 */
	public static final String NUMPAD_MULTIPLY_NAME = "NUMPAD_MULTIPLY"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Subtract' key on the numpad.
	 */
	public static final String NUMPAD_SUBTRACT_NAME = "NUMPAD_SUBTRACT"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Page Down' key.
	 */
	public static final String PAGE_DOWN_NAME = "PAGE_DOWN"; //$NON-NLS-1$

	/**
	 * The formal name of the 'Page Up' key.
	 */
	public static final String PAGE_UP_NAME = "PAGE_UP"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Pause' key.
	 */
	public static final String PAUSE_NAME = "PAUSE"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Print Screen' key.
	 */
	public static final String PRINT_SCREEN_NAME = "PRINT_SCREEN"; //$NON-NLS-1$
	
	/**
	 * An alternative name for the enter key.
	 */
	public static final String RETURN_NAME = "RETURN"; //$NON-NLS-1$

	/**
	 * The formal name for the 'Scroll Lock' key.
	 */
	public static final String SCROLL_LOCK_NAME = "SCROLL_LOCK"; //$NON-NLS-1$		

	/**
	 * The formal name of the 'Shift' key.
	 */
	public static final String SHIFT_NAME = "SHIFT"; //$NON-NLS-1$

	/**
	 * The formal name of the space (U+0020) key.
	 */
	public static final String SPACE_NAME = "SPACE"; //$NON-NLS-1$

	/**
	 * The formal name of the tab (U+0009) key.
	 */
	public static final String TAB_NAME = "TAB"; //$NON-NLS-1$

	/**
	 * The formal name of the vertical tab (U+000B) key.
	 */
	public static final String VT_NAME = "VT"; //$NON-NLS-1$	

	/**
	 * Looks up a single natural key by its formal name, and returns the integer
	 * representation for this natural key
	 * 
	 * @param name
	 *            The formal name of the natural key to look-up; must not be
	 *            <code>null</code>.
	 * @return The integer representation of this key. If the natural key cannot
	 *         be found, then this method returns <code>0</code>.
	 */
	public int formalKeyLookup(String name);	

	/**
	 * Looks up a single natural key by its formal name, and returns the integer
	 * representation for this natural key
	 * 
	 * @param name
	 *            The formal name of the natural key to look-up; must not be
	 *            <code>null</code>.
	 * @return The integer representation of this key. If the natural key cannot
	 *         be found, then this method returns <code>0</code>.
	 */
	public Integer formalKeyLookupInteger(String name);

	/**
	 * Looks up a single modifier key by its formal name, and returns the integer
	 * representation for this modifier key
	 * 
	 * @param name
	 *            The formal name of the modifier key to look-up; must not be
	 *            <code>null</code>.
	 * @return The integer representation of this key. If the modifier key
	 *         cannot be found, then this method returns <code>0</code>.
	 */
	public int formalModifierLookup(String name);

	/**
	 * Looks up a key value, and returns the formal string representation for
	 * that key
	 * 
	 * @param key
	 *            The key to look-up.
	 * @return The formal string representation of this key. If this key cannot
	 *         be found, then it is simply the character corresponding to that
	 *         integer value.
	 */
	public String formalNameLookup(int key);

	/**
	 * Returns the integer representation of the ALT key.
	 * 
	 * @return The ALT key
	 */
	public int getAlt();

	/**
	 * Returns the integer representation of the COMMAND key.
	 * 
	 * @return The COMMAND key
	 */
	public int getCommand();

	/**
	 * Returns the integer representation of the CTRL key.
	 * 
	 * @return The CTRL key
	 */
	public int getCtrl();

	/**
	 * Returns the integer representation of the SHIFT key.
	 * 
	 * @return The SHIFT key
	 */
	public int getShift();

	/**
	 * Returns whether the given key is a modifier key.
	 * 
	 * @param key
	 *            The integer value of the key to check.
	 * @return <code>true</code> if the key is one of the modifier keys;
	 *         <code>false</code> otherwise.
	 */
	public boolean isModifierKey(int key);
}
