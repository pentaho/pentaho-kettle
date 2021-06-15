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
 * methods described by the <code>DropTargetListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>DropTargetEvent</code>s can
 * extend this class and override only the methods which they are
 * interested in.
 * </p>
 *
 * @see DropTargetListener
 * @see DropTargetEvent
 * @since 1.3
 */
public class DropTargetAdapter implements DropTargetListener {

/**
 * This implementation of <code>dragEnter</code> permits the default 
 * operation defined in <code>event.detail</code>to be performed on the current data type
 * defined in <code>event.currentDataType</code>.
 * For additional information see <code>DropTargetListener.dragEnter</code>.
 * 
 * @param event the information associated with the drag enter event
 */
public void dragEnter(DropTargetEvent event){}

/**
 * This implementation of <code>dragLeave</code> does nothing.
 * For additional information see <code>DropTargetListener.dragOperationChanged</code>.
 * 
 * @param event the information associated with the drag leave event
 */
public void dragLeave(DropTargetEvent event){}

/**
 * This implementation of <code>dragOperationChanged</code> permits the default 
 * operation defined in <code>event.detail</code>to be performed on the current data type
 * defined in <code>event.currentDataType</code>.
 * For additional information see <code>DropTargetListener.dragOperationChanged</code>.
 * 
 * @param event the information associated with the drag operation changed event
 */
public void dragOperationChanged(DropTargetEvent event){}

/**
 * This implementation of <code>dragOver</code> permits the default 
 * operation defined in <code>event.detail</code>to be performed on the current data type
 * defined in <code>event.currentDataType</code>.
 * For additional information see <code>DropTargetListener.dragOver</code>.
 * 
 * @param event the information associated with the drag over event
 */
public void dragOver(DropTargetEvent event){}

/**
 * This implementation of <code>drop</code> does nothing.
 * For additional information see <code>DropTargetListener.drop</code>.
 * 
 * @param event the information associated with the drop event
 */
public void drop(DropTargetEvent event){}

/**
 * This implementation of <code>dropAccept</code> permits the default 
 * operation defined in <code>event.detail</code>to be performed on the current data type
 * defined in <code>event.currentDataType</code>.
 * For additional information see <code>DropTargetListener.dropAccept</code>.
 * 
 * @param event the information associated with the drop accept event
 */
public void dropAccept(DropTargetEvent event){}

}
