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
package org.eclipse.swt.dnd;


/**
 * This adapter class provides default implementations for the
 * methods described by the <code>DragSourceListener</code> interface.
 * 
 * <p>Classes that wish to deal with <code>DragSourceEvent</code>s can
 * extend this class and override only the methods which they are
 * interested in.</p>
 *
 * @see DragSourceListener
 * @see DragSourceEvent
 * @since 1.3
 */
public class DragSourceAdapter implements DragSourceListener {

/**
 * This implementation of <code>dragStart</code> permits the drag operation to start.
 * For additional information see <code>DragSourceListener.dragStart</code>.
 * 
 * @param event the information associated with the drag start event
 */
public void dragStart(DragSourceEvent event){}

/**
 * This implementation of <code>dragFinished</code> does nothing.
 * For additional information see <code>DragSourceListener.dragFinished</code>.
 * 
 * @param event the information associated with the drag finished event
 */
public void dragFinished(DragSourceEvent event){}

/**
 * This implementation of <code>dragSetData</code> does nothing.
 * For additional information see <code>DragSourceListener.dragSetData</code>.
 * 
 * @param event the information associated with the drag set data event
 */
public void dragSetData(DragSourceEvent event){}

}
