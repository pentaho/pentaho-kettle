/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.menus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * This extension to the {@link IWidget} interface allows clients adding
 * elements to the trim to receive notifications if the User moves the widget to
 * another trim area.
 * <p>
 * This class is intended to be the base for any trim contributions.
 * </p>
 * @since 1.0
 * 
 */
public abstract class AbstractTrimWidget implements IWidget {
	/**
	 * This method is called to initially construct the widget and is also
	 * called whenever the widget's composite has been moved to a trim area on a
	 * different side of the workbench. It is the client's responsibility to
	 * control the life-cycle of the Control it manages.
	 * <p>
	 * For example: If the implementation is constructing a {@link ToolBar} and
	 * the orientation were to change from horizontal to vertical it would have
	 * to <code>dispose</code> its old ToolBar and create a new one with the
	 * correct orientation.
	 * </p>
	 * <p>
	 * The sides can be one of:
	 * <ul>
	 * <li>{@link SWT#TOP}</li>
	 * <li>{@link SWT#BOTTOM}</li>
	 * <li>{@link SWT#LEFT}</li>
	 * <li>{@link SWT#RIGHT}</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 
	 * @param parent
	 *            The parent to (re)create the widget under
	 * 
	 * @param oldSide
	 *            The previous side ({@link SWT#DEFAULT} on the initial fill)
	 * @param newSide
	 *            The current side
	 */
	public abstract void fill(Composite parent, int oldSide, int newSide);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.IWidget#dispose()
	 */
	public abstract void dispose();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.IWidget#fill(org.eclipse.swt.widgets.Composite)
	 */
	public void fill(Composite parent) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.IWidget#fill(org.eclipse.swt.widgets.Menu, int)
	 */
	public void fill(Menu parent, int index) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.IWidget#fill(org.eclipse.swt.widgets.ToolBar, int)
	 */
	public void fill(ToolBar parent, int index) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.IWidget#fill(org.eclipse.swt.widgets.CoolBar, int)
	 */
	public void fill(CoolBar parent, int index) {
	}
}
