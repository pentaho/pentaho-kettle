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
package org.eclipse.jface.action;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A <code>SubContributionItem</code> is a wrapper for an <code>IContributionItem</code>.  
 * It is used within a <code>SubContributionManager</code> to control the visibility
 * of items.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SubContributionItem implements IContributionItem {
    /**
     * The visibility of the item.
     */
    private boolean visible;

    /**
     * The inner item for this contribution.  
     */
    private IContributionItem innerItem;

    /**
     * Creates a new <code>SubContributionItem</code>.
     * @param item the contribution item to be wrapped
     */
    public SubContributionItem(IContributionItem item) {
        innerItem = item;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * delegates to the inner item. Subclasses may override.
     */
    public void dispose() {
        innerItem.dispose();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void fill(Composite parent) {
        if (visible) {
			innerItem.fill(parent);
		}
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void fill(Menu parent, int index) {
        if (visible) {
			innerItem.fill(parent, index);
		}
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void fill(ToolBar parent, int index) {
        if (visible) {
			innerItem.fill(parent, index);
		}
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public String getId() {
        return innerItem.getId();
    }

    /**
     * Returns the inner contribution item.
     *
     * @return the inner contribution item
     */
    public IContributionItem getInnerItem() {
        return innerItem;
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public boolean isEnabled() {
        return innerItem.isEnabled();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public boolean isDirty() {
        return innerItem.isDirty();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public boolean isDynamic() {
        return innerItem.isDynamic();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public boolean isGroupMarker() {
        return innerItem.isGroupMarker();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public boolean isSeparator() {
        return innerItem.isSeparator();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public boolean isVisible() {
        return visible && innerItem.isVisible();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void setParent(IContributionManager parent) {
        // do nothing, the parent of our inner item
        // is its SubContributionManager
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void update() {
        innerItem.update();
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    public void update(String id) {
        innerItem.update(id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar, int)
     */
    public void fill(CoolBar parent, int index) {
        if (visible) {
			innerItem.fill(parent, index);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
     */
    public void saveWidgetState() {
    }

}
