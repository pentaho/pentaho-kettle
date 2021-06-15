/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.provisional.action;

import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;

/**
 * The <code>IToolBarManager2</code> extends <code>IToolBarManager</code> to
 * allow clients to be isolated from the actual kind of SWT control used by the
 * manager.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 */
public interface IToolBarManager2 extends IToolBarManager {

	/**
	 * The property id for changes to the control's layout
	 */
	public static final String PROP_LAYOUT = "PROP_LAYOUT"; //$NON-NLS-1$

	/**
	 * Creates and returns this manager's toolbar control. Does not create a new
	 * control if one already exists.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the toolbar control
	 */
	public ToolBar createControl(Composite parent);

	/**
	 * Creates and returns this manager's control. Does not create a new control
	 * if one already exists.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the control
	 */
	public Control createControl2(Composite parent);

	/**
	 * Returns the toolbar control for this manager.
	 * 
	 * @return the toolbar control, or <code>null</code> if none
	 */
	public ToolBar getControl();

	/**
	 * Returns the control for this manager.
	 * 
	 * @return the control, or <code>null</code> if none
	 */
	public Control getControl2();

	/**
	 * Disposes the resources for this manager.
	 */
	public void dispose();

	/**
	 * Returns the item count of the control used by this manager.
	 * 
	 * @return the number of items in the control
	 */
	public int getItemCount();

	/**
	 * Registers a property change listner with this manager.
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Removes a property change listner from this manager.
	 * 
	 * @param listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Sets the overrides for this contribution manager
	 * 
	 * @param newOverrides
	 *            the overrides for the items of this manager
	 */
	public void setOverrides(IContributionManagerOverrides newOverrides);

}
