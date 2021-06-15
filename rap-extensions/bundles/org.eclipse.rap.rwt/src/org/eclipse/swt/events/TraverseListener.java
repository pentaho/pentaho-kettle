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
 * that deals with the events that are generated when a
 * traverse event occurs in a control.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control using the
 * <code>addTraverseListener</code> method and removed using
 * the <code>removeTraverseListener</code> method. When a
 * traverse event occurs in a control, the keyTraversed method
 * will be invoked.
 * </p>
 *
 * @see TraverseEvent
 * 
 * @since 1.2
 */
public interface TraverseListener extends SWTEventListener {

/**
 * Sent when a traverse event occurs in a control.
 * <p>
 * A traverse event occurs when the user presses a traversal
 * key. Traversal keys are typically tab and arrow keys, along
 * with certain other keys on some platforms. Traversal key
 * constants beginning with <code>TRAVERSE_</code> are defined
 * in the <code>SWT</code> class.
 * </p>
 *
 * @param e an event containing information about the traverse
 */
public void keyTraversed(TraverseEvent e);
}
