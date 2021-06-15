/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.events;


import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide a method
 * that deals with the event that is generated when help is
 * requested for a control, typically when the user presses F1.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control using the
 * <code>addHelpListener</code> method and removed using
 * the <code>removeHelpListener</code> method. When help
 * is requested for a control, the helpRequested method
 * will be invoked.
 * </p>
 *
 * @see HelpEvent
 * @since 1.3
 */
public interface HelpListener extends SWTEventListener {

/**
 * Sent when help is requested for a control, typically
 * when the user presses F1.
 *
 * @param e an event containing information about the help
 */
public void helpRequested(HelpEvent e);
}
