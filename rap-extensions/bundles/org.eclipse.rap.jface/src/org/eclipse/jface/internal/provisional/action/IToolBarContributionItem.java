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

package org.eclipse.jface.internal.provisional.action;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;

/**
 * The intention of this interface is to provide in interface for 
 * ToolBarContributionItem so that the implementation can be replaced.
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
public interface IToolBarContributionItem extends IContributionItem {

    /**
     * Returns the current height of the corresponding cool item.
     * 
     * @return the current height
     */
    public int getCurrentHeight();

    /**
     * Returns the current width of the corresponding cool item.
     * 
     * @return the current size
     */
    public int getCurrentWidth();

    /**
     * Returns the minimum number of tool items to show in the cool item.
     * 
     * @return the minimum number of tool items to show, or <code>SHOW_ALL_ITEMS</code>
     *         if a value was not set
     * @see #setMinimumItemsToShow(int)
     */
    public int getMinimumItemsToShow();
    
    /**
     * Returns whether chevron support is enabled.
     * 
     * @return <code>true</code> if chevron support is enabled, <code>false</code>
     *         otherwise
     */
    public boolean getUseChevron();
    
    /**
     * Sets the current height of the cool item. Update(SIZE) should be called
     * to adjust the widget.
     * 
     * @param currentHeight
     *            the current height to set
     */
    public void setCurrentHeight(int currentHeight);

    /**
     * Sets the current width of the cool item. Update(SIZE) should be called
     * to adjust the widget.
     * 
     * @param currentWidth
     *            the current width to set
     */
    public void setCurrentWidth(int currentWidth);

    /**
     * Sets the minimum number of tool items to show in the cool item. If this
     * number is less than the total tool items, a chevron will appear and the
     * hidden tool items appear in a drop down menu. By default, all the tool
     * items are shown in the cool item.
     * 
     * @param minimumItemsToShow
     *            the minimum number of tool items to show.
     * @see #getMinimumItemsToShow()
     * @see #setUseChevron(boolean)
     */
    public void setMinimumItemsToShow(int minimumItemsToShow);

    /**
     * Enables or disables chevron support for the cool item. By default,
     * chevron support is enabled.
     * 
     * @param value
     *            <code>true</code> to enable chevron support, <code>false</code>
     *            otherwise.
     */
    public void setUseChevron(boolean value);
    
    /**
     * Returns the internal tool bar manager of the contribution item.
     * 
     * @return the tool bar manager, or <code>null</code> if one is not
     *         defined.
     * @see IToolBarManager
     */
    public IToolBarManager getToolBarManager();
    
    /**
     * Returns the parent contribution manager, or <code>null</code> if this 
     * contribution item is not currently added to a contribution manager.
     * 
     * @return the parent contribution manager, or <code>null</code>
     * 
     * TODO may not need this, getToolBarManager may be enough.
     */
    public IContributionManager getParent();
    
}
