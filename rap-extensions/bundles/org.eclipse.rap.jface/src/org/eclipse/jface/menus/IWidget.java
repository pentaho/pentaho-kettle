/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.menus;

import java.io.Serializable;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * <p>
 * Provides a hook by which third-party code can contribute SWT widgets to a
 * menu, tool bar or status line. This can be used, for example, to add a combo
 * box to the status line, or a "Location" bar to the tool bar.
 * </p>
 * <p>
 * It is possible for fill and dispose to be called multiple times for a single
 * instance of <code>IWidget</code>.
 * </p>
 * <p>
 * Clients may implement, but must not extend.
 * </p>
 * 
 * @since 1.0
 */
public interface IWidget extends Serializable {

	/**
	 * Disposes of the underlying widgets. This can be called when the widget is
	 * becoming hidden.
	 */
	public void dispose();

	/**
	 * Fills the given composite control with controls representing this widget.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public void fill(Composite parent);

	/**
	 * Fills the given menu with controls representing this widget.
	 * 
	 * @param parent
	 *            the parent menu
	 * @param index
	 *            the index where the controls are inserted, or <code>-1</code>
	 *            to insert at the end
	 */
	public void fill(Menu parent, int index);

	/**
	 * Fills the given tool bar with controls representing this contribution
	 * item.
	 * 
	 * @param parent
	 *            the parent tool bar
	 * @param index
	 *            the index where the controls are inserted, or <code>-1</code>
	 *            to insert at the end
	 */
	public void fill(ToolBar parent, int index);

	/**
	 * Fills the given cool bar with controls representing this contribution
	 * item.
	 * 
	 * @param parent
	 *            the parent cool bar
	 * @param index
	 *            the index where the controls are inserted, or <code>-1</code>
	 *            to insert at the end
	 */
	public void fill(CoolBar parent, int index);
}
