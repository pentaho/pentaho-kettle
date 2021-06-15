/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.events;


/**
 * This adapter class provides default implementations for the
 * methods described by the <code>KeyListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>KeyEvent</code>s can
 * extend this class and override only the methods which they are
 * interested in.
 * </p>
 *
 * @see KeyListener
 * @see KeyEvent
 * 
 * @since 1.2
 */
public abstract class KeyAdapter implements KeyListener {

/**
 * Sent when a key is pressed on the system keyboard.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the key press
 */
public void keyPressed(KeyEvent e) {
}

/**
 * Sent when a key is released on the system keyboard.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the key release
 */
public void keyReleased(KeyEvent e) {
}
}
