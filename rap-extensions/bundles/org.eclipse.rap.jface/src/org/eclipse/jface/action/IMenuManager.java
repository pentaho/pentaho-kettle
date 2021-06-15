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

/**
 * The <code>IMenuManager</code> interface provides protocol for managing
 * contributions to a menu bar and its sub menus.
 * An <code>IMenuManager</code> is also an <code>IContributionItem</code>,
 * allowing sub-menus to be nested in parent menus.
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 * <p>
 * This package provides a concrete menu manager implementation,
 * {@link MenuManager <code>MenuManager</code>}.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMenuManager extends IContributionManager, IContributionItem {
    /**
     * Adds a menu listener to this menu.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a menu listener
     */
    public void addMenuListener(IMenuListener listener);

    /**
     * Finds the manager for the menu at the given path. A path
     * consists of contribution item ids separated by the separator 
     * character.  The path separator character is <code>'/'</code>.
     * <p>
     * Convenience for <code>findUsingPath(path)</code> which
     * extracts an <code>IMenuManager</code> if possible.
     * </p>
     *
     * @param path the path string
     * @return the menu contribution item, or <code>null</code>
     *   if there is no such contribution item or if the item does
     *   not have an associated menu manager
     */
    public IMenuManager findMenuUsingPath(String path);

    /**
     * Finds the contribution item at the given path. A path
     * consists of contribution item ids separated by the separator 
     * character. The path separator character is <code>'/'</code>.
     *
     * @param path the path string
     * @return the contribution item, or <code>null</code> if there is no
     *   such contribution item
     */
    public IContributionItem findUsingPath(String path);

    /**
	 * Returns whether all items should be removed when the menu is about to
	 * show, but before notifying menu listeners. The default is
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if all items should be removed when shown,
	 *         <code>false</code> if not
	 */
    public boolean getRemoveAllWhenShown();

    /**
     * Returns whether this menu should be enabled or not.
     *
     * @return <code>true</code> if enabled, and
     *   <code>false</code> if disabled
     */
    public boolean isEnabled();

    /**
     * Removes the given menu listener from this menu.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener the menu listener
     */
    public void removeMenuListener(IMenuListener listener);

    /**
	 * Sets whether all items should be removed when the menu is about to show,
	 * but before notifying menu listeners.
	 * 
	 * @param removeAll
	 *            <code>true</code> if all items should be removed when shown,
	 *            <code>false</code> if not
	 */
    public void setRemoveAllWhenShown(boolean removeAll);

    /**
     * Incrementally builds the menu from the contribution items, and
     * does so recursively for all submenus.
     *
     * @param force <code>true</code> means update even if not dirty,
     *   and <code>false</code> for normal incremental updating
     */
    public void updateAll(boolean force);
}
