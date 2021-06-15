/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * that deals with the event that is generated when a widget,
 * such as a menu item, is armed.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a widget using the
 * <code>addArmListener</code> method and removed using
 * the <code>removeArmListener</code> method. When the
 * widget is armed, the widgetArmed method will be invoked.
 * </p>
 *
 * @see ArmEvent
 *
 * @since 1.3
 */
public interface ArmListener extends SWTEventListener {

  /**
   * Sent when a widget is armed, or 'about to be selected'.
   *
   * @param e an event containing information about the arm
   */
  public void widgetArmed( ArmEvent e );
}
