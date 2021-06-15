/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * that deal with the events that are generated when the
 * platform-specific trigger for showing a context menu is
 * detected.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control or TrayItem
 * using the <code>addMenuDetectListener</code> method and
 * removed using the <code>removeMenuDetectListener</code> method.
 * When the context menu trigger occurs, the
 * <code>menuDetected</code> method will be invoked.
 * </p>
 *
 * @see MenuDetectEvent
 *
 * @since 1.3
 */
public interface MenuDetectListener extends SWTEventListener {

  /**
   * Sent when the platform-dependent trigger for showing a menu item is detected.
   *
   * @param e an event containing information about the menu detect
   */
  public void menuDetected( MenuDetectEvent e );
}
