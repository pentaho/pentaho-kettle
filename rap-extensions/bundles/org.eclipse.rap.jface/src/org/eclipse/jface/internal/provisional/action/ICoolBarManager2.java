/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.provisional.action;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Extends <code>ICoolBarManager</code> to allow clients to be decoupled
 * from the actual kind of control used.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 */
public interface ICoolBarManager2 extends ICoolBarManager {

    /**
     * Creates and returns this manager's control. Does not create a
     * new control if one already exists.
     * 
     * 
     * @param parent
     *            the parent control
     * @return the control
     */
    public Control createControl2(Composite parent);

    /**
     * Returns the bar control for this manager.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     *  
     * @return the bar control, or <code>null</code> if none
     */
    public Control getControl2();

    /**
     * Synchronizes the visual order of the cool items in the control with this
     * manager's internal data structures. This method should be called before
     * requesting the order of the contribution items to ensure that the order
     * is accurate.
     * <p>
     * Note that <code>update()</code> and <code>refresh()</code> are
     * converses: <code>update()</code> changes the visual order to match the
     * internal structures, and <code>refresh</code> changes the internal
     * structures to match the visual order.
     * </p>
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     */
    public void refresh();
    
    /**
	 * Disposes the resources for this manager.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     */
    public void dispose();

    /**
     * Restores the canonical order of this cool bar manager. The canonical
     * order is the order in which the contribution items where added.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
	 */
    public void resetItemOrder();

    /**
     * Replaces the current items with the given items.
     * Forces an update.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     * @param newItems the items with which to replace the current items
	 */
    public void setItems(IContributionItem[] newItems);

	/**
	 * Sets the overrides for this contribution manager
	 * 
	 * @param newOverrides
	 *            the overrides for the items of this manager
	 * @since 1.3
	 */
	public void setOverrides(IContributionManagerOverrides newOverrides);

}
