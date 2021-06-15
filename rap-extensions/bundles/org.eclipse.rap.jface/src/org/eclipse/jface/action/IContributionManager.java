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

import java.io.Serializable;

/**
 * A contribution manager organizes contributions to such UI components
 * as menus, toolbars and status lines.
 * <p>
 * A contribution manager keeps track of a list of contribution
 * items. Each contribution item may has an optional identifier, which can be used
 * to retrieve items from a manager, and for positioning items relative to
 * each other. The list of contribution items can be subdivided into named groups 
 * using special contribution items that serve as group markers.
 * </p>
 * <p>
 * The <code>IContributionManager</code> interface provides general
 * protocol for adding, removing, and retrieving contribution items.
 * It also provides convenience methods that make it convenient
 * to contribute actions. This interface should be implemented
 * by all objects that wish to manage contributions.
 * </p>
 * <p>
 * There are several implementions of this interface in this package,
 * including ones for menus ({@link MenuManager <code>MenuManager</code>}),
 * tool bars ({@link ToolBarManager <code>ToolBarManager</code>}),
 * and status lines ({@link StatusLineManager <code>StatusLineManager</code>}).
 * </p>
 */
public interface IContributionManager extends Serializable {
    /**
     * Adds an action as a contribution item to this manager.
     * Equivalent to <code>add(new ActionContributionItem(action))</code>.
     *
     * @param action the action, this cannot be <code>null</code>
     */
    public void add(IAction action);

    /**
     * Adds a contribution item to this manager.
     *
     * @param item the contribution item, this cannot be <code>null</code>
     */
    public void add(IContributionItem item);

    /**
     * Adds a contribution item for the given action at the end of the group
     * with the given name.
     * Equivalent to
     * <code>appendToGroup(groupName,new ActionContributionItem(action))</code>.
     *
     * @param groupName the name of the group
     * @param action the action
     * @exception IllegalArgumentException if there is no group with
     *   the given name
     */
    public void appendToGroup(String groupName, IAction action);

    /**
     * Adds a contribution item to this manager at the end of the group
     * with the given name.
     *
     * @param groupName the name of the group
     * @param item the contribution item
     * @exception IllegalArgumentException if there is no group with
     *   the given name
     */
    public void appendToGroup(String groupName, IContributionItem item);

    /**
     * Finds the contribution item with the given id.
     *
     * @param id the contribution item id
     * @return the contribution item, or <code>null</code> if
     *   no item with the given id can be found
     */
    public IContributionItem find(String id);

    /**
     * Returns all contribution items known to this manager.
     *
     * @return a list of contribution items
     */
    public IContributionItem[] getItems();

    /**
     * Returns the overrides for the items of this manager.
     * 
     * @return the overrides for the items of this manager
     */
    public IContributionManagerOverrides getOverrides();

    /**
     * Inserts a contribution item for the given action after the item 
     * with the given id.
     * Equivalent to
     * <code>insertAfter(id,new ActionContributionItem(action))</code>.
     *
     * @param id the contribution item id
     * @param action the action to insert
     * @exception IllegalArgumentException if there is no item with
     *   the given id
     */
    public void insertAfter(String id, IAction action);

    /**
     * Inserts a contribution item after the item with the given id.
     *
     * @param id the contribution item id
     * @param item the contribution item to insert
     * @exception IllegalArgumentException if there is no item with
     *   the given id
     */
    public void insertAfter(String id, IContributionItem item);

    /**
     * Inserts a contribution item for the given action before the item 
     * with the given id.
     * Equivalent to
     * <code>insertBefore(id,new ActionContributionItem(action))</code>.
     *
     * @param id the contribution item id
     * @param action the action to insert
     * @exception IllegalArgumentException if there is no item with
     *   the given id
     */
    public void insertBefore(String id, IAction action);

    /**
     * Inserts a contribution item before the item with the given id.
     *
     * @param id the contribution item id
     * @param item the contribution item to insert
     * @exception IllegalArgumentException if there is no item with
     *   the given id
     */
    public void insertBefore(String id, IContributionItem item);

    /**
     * Returns whether the list of contributions has recently changed and
     * has yet to be reflected in the corresponding widgets.
     *
     * @return <code>true</code> if this manager is dirty, and <code>false</code>
     *   if it is up-to-date
     */
    public boolean isDirty();

    /**
     * Returns whether this manager has any contribution items.
     *
     * @return <code>true</code> if there are no items, and
     *   <code>false</code> otherwise
     */
    public boolean isEmpty();

    /**
     * Marks this contribution manager as dirty.
     */
    public void markDirty();

    /**
     * Adds a contribution item for the given action at the beginning of the 
     * group with the given name.
     * Equivalent to
     * <code>prependToGroup(groupName,new ActionContributionItem(action))</code>.
     *
     * @param groupName the name of the group
     * @param action the action
     * @exception IllegalArgumentException if there is no group with
     *   the given name
     */
    public void prependToGroup(String groupName, IAction action);

    /**
     * Adds a contribution item to this manager at the beginning of the 
     * group with the given name.
     *
     * @param groupName the name of the group
     * @param item the contribution item
     * @exception IllegalArgumentException if there is no group with
     *   the given name
     */
    public void prependToGroup(String groupName, IContributionItem item);

    /**
     * Removes and returns the contribution item with the given id from this manager.  
     * Returns <code>null</code> if this manager has no contribution items
     * with the given id.
     *
     * @param id the contribution item id
     * @return the item that was found and removed, or <code>null</code> if none
     */
    public IContributionItem remove(String id);

    /**
     * Removes the given contribution item from the contribution items
     * known to this manager.
     *
     * @param item the contribution item
     * @return the <code>item</code> parameter if the item was removed,
     *   and <code>null</code> if it was not found
     */
    public IContributionItem remove(IContributionItem item);

    /**
     * Removes all contribution items from this manager.
     */
    public void removeAll();

    /**
     * Updates this manager's underlying widget(s) with any changes which
     * have been made to it or its items.  Normally changes to a contribution
     * manager merely mark it as dirty, without updating the underlying widgets.
     * This brings the underlying widgets up to date with any changes.
     *
     * @param force <code>true</code> means update even if not dirty,
     *   and <code>false</code> for normal incremental updating
     */
    public void update(boolean force);
}
