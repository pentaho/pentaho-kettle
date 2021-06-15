/*******************************************************************************
 * Copyright (c) 2002, 2007 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.swt.events;


/**
 * This adapter class provides default implementations for the
 * methods described by the <code>ShellListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>ShellEvent</code>s can
 * extend this class and override only the methods which they are
 * interested in.
 * </p>
 *
 * @see ShellListener
 * @see ShellEvent
 */
public abstract class ShellAdapter implements ShellListener {

/**
 * Sent when a shell becomes the active window.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the activation
 */
public void shellActivated(ShellEvent e) {
}

/**
 * Sent when a shell is closed.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the close
 */
public void shellClosed(ShellEvent e) {
}

/**
 * Sent when a shell stops being the active window.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the deactivation
 */
public void shellDeactivated(ShellEvent e) {
}

/**
 * Sent when a shell is un-minimized.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the un-minimization
 */
//public void shellDeiconified(ShellEvent e) {
//}

/**
 * Sent when a shell is minimized.
 * The default behavior is to do nothing.
 *
 * @param e an event containing information about the minimization
 */
//public void shellIconified(ShellEvent e) {
//}
}
