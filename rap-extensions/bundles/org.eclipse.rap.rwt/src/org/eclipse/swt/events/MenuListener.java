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

import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide methods
 * that deal with the hiding and showing of menus.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a menu using the
 * <code>addMenuListener</code> method and removed using
 * the <code>removeMenuListener</code> method. When the
 * menu is hidden or shown, the appropriate method will
 * be invoked.
 * </p>
 *
 * @see MenuAdapter
 * @see MenuEvent
 */
public interface MenuListener extends SWTEventListener {
  
	/**
	 * Sent when a menu is hidden.
	 *
	 * @param e an event containing information about the menu operation
	 */
	public void menuHidden(MenuEvent e);

	/**
	 * Sent when a menu is shown.
	 *
	 * @param e an event containing information about the menu operation
	 */
	public void menuShown(MenuEvent e);

}
