/*******************************************************************************
 * Copyright (c) 2009, 2011 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.events;

import org.eclipse.swt.internal.SWTEventListener;


/**
 * Classes which implement this interface provide methods
 * that deal with the events that are generated when a drag
 * gesture is detected.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control using the
 * <code>addDragDetectListener</code> method and removed using
 * the <code>removeDragDetectListener</code> method. When the
 * drag is detected, the drageDetected method will be invoked.
 * </p>
 *
 * @see DragDetectEvent
 * 
 * @since 1.3
 */
public interface DragDetectListener extends SWTEventListener {
  
  /**
   * Sent when a drag gesture is detected.
   *
   * @param event an event containing information about the drag
   */
  void dragDetected( DragDetectEvent event );
}
