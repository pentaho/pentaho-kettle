/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
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
 * Classes which implement this interface provide methods
 * that deal with the events that are generated as gestures
 * are triggered by the user interacting with a touch pad or
 * touch screen.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control using the
 * <code>addGestureListener</code> method and removed using
 * the <code>removeGestureListener</code> method. When a
 * gesture is triggered, the appropriate method will be invoked.
 * </p>
 *
 * @see GestureEvent
 *
 * @since 1.4
 */
public interface GestureListener extends SWTEventListener {

/**
 * Sent when a recognized gesture has occurred.
 *
 * @param e an event containing information about the gesture.
 */
public void gesture(GestureEvent e);

}