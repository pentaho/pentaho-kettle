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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Policy;

/**
 * Abstract base class for all contribution managers, and standard
 * implementation of <code>IContributionManager</code>. This class provides
 * functionality common across the specific managers defined by this framework.
 * <p>
 * This class maintains a list of contribution items and a dirty flag, both as
 * internal state. In addition to providing implementations of most
 * <code>IContributionManager</code> methods, this class automatically
 * coalesces adjacent separators, hides beginning and ending separators, and
 * deals with dynamically changing sets of contributions. When the set of
 * contributions does change dynamically, the changes are propagated to the
 * control via the <code>update</code> method, which subclasses must
 * implement.
 * </p>
 * <p>
 * Note: A <code>ContributionItem</code> cannot be shared between different
 * <code>ContributionManager</code>s.
 * </p>
 * @since 1.0
 */
public abstract class ContributionManager implements IContributionManager {

	// Internal debug flag.
	// protected static final boolean DEBUG = false;

	/**
	 * The list of contribution items.
	 */
	private List contributions = new ArrayList();

	/**
	 * Indicates whether the widgets are in sync with the contributions.
	 */
	private boolean isDirty = true;

	/**
	 * Number of dynamic contribution items.
	 */
	private int dynamicItems = 0;

	/**
	 * The overrides for items of this manager
	 */
	private IContributionManagerOverrides overrides;

	/**
	 * Creates a new contribution manager.
	 */
	protected ContributionManager() {
		// Do nothing.
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void add(IAction action) {
		Assert.isNotNull(action, "Action must not be null"); //$NON-NLS-1$
		add(new ActionContributionItem(action));
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void add(IContributionItem item) {
		Assert.isNotNull(item, "Item must not be null"); //$NON-NLS-1$
		if (allowItem(item)) {
			contributions.add(item);
			itemAdded(item);
		}
	}

	/**
	 * Adds a contribution item to the start or end of the group with the given
	 * name.
	 * 
	 * @param groupName
	 *            the name of the group
	 * @param item
	 *            the contribution item
	 * @param append
	 *            <code>true</code> to add to the end of the group, and
	 *            <code>false</code> to add the beginning of the group
	 * @exception IllegalArgumentException
	 *                if there is no group with the given name
	 */
	private void addToGroup(String groupName, IContributionItem item,
			boolean append) {
		int i;
		Iterator items = contributions.iterator();
		for (i = 0; items.hasNext(); i++) {
			IContributionItem o = (IContributionItem) items.next();
			if (o.isGroupMarker()) {
				String id = o.getId();
				if (id != null && id.equalsIgnoreCase(groupName)) {
					i++;
					if (append) {
						for (; items.hasNext(); i++) {
							IContributionItem ci = (IContributionItem) items
									.next();
							if (ci.isGroupMarker()) {
								break;
							}
						}
					}
					if (allowItem(item)) {
						contributions.add(i, item);
						itemAdded(item);
					}
					return;
				}
			}
		}
		throw new IllegalArgumentException("Group not found: " + groupName);//$NON-NLS-1$
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void appendToGroup(String groupName, IAction action) {
		addToGroup(groupName, new ActionContributionItem(action), true);
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void appendToGroup(String groupName, IContributionItem item) {
		addToGroup(groupName, item, true);
	}

	/**
	 * This method allows subclasses of <code>ContributionManager</code> to
	 * prevent certain items in the contributions list.
	 * <code>ContributionManager</code> will either block or allow an addition
	 * based on the result of this method call. This can be used to prevent
	 * duplication, for example.
	 * 
	 * @param itemToAdd
	 *            The contribution item to be added; may be <code>null</code>.
	 * @return <code>true</code> if the addition should be allowed;
	 *         <code>false</code> otherwise. The default implementation allows
	 *         all items.
	 */
	protected boolean allowItem(IContributionItem itemToAdd) {
		return true;
	}

	/**
	 * Internal debug method for printing statistics about this manager to
	 * <code>System.out</code>.
	 */
	protected void dumpStatistics() {
		int size = 0;
		if (contributions != null) {
			size = contributions.size();
		}

		System.out.println(this.toString());
		System.out.println("   Number of elements: " + size);//$NON-NLS-1$
		int sum = 0;
		for (int i = 0; i < size; i++) {
			if (((IContributionItem) contributions.get(i)).isVisible()) {
				sum++;
			}
		}
		System.out.println("   Number of visible elements: " + sum);//$NON-NLS-1$
		System.out.println("   Is dirty: " + isDirty()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public IContributionItem find(String id) {
		Iterator e = contributions.iterator();
		while (e.hasNext()) {
			IContributionItem item = (IContributionItem) e.next();
			String itemId = item.getId();
			if (itemId != null && itemId.equalsIgnoreCase(id)) {
				return item;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public IContributionItem[] getItems() {
		IContributionItem[] items = new IContributionItem[contributions.size()];
		contributions.toArray(items);
		return items;
	}
	
	/**
	 * Return the number of contributions in this manager.
	 * 
	 * @return the number of contributions in this manager
	 */
	public int getSize() {
		return contributions.size();
	}

	/**
	 * The <code>ContributionManager</code> implementation of this method
	 * declared on <code>IContributionManager</code> returns the current
	 * overrides. If there is no overrides it lazily creates one which overrides
	 * no item state.
	 * 
	 */
	public IContributionManagerOverrides getOverrides() {
		if (overrides == null) {
			overrides = new IContributionManagerOverrides() {
				public Boolean getEnabled(IContributionItem item) {
					return null;
				}

				public Integer getAccelerator(IContributionItem item) {
					return null;
				}

				public String getAcceleratorText(IContributionItem item) {
					return null;
				}

				public String getText(IContributionItem item) {
					return null;
				}

				public Boolean getVisible(IContributionItem item) {
					return null;
				}
			};
		}
		return overrides;
	}

	/**
	 * Returns whether this contribution manager contains dynamic items. A
	 * dynamic contribution item contributes items conditionally, dependent on
	 * some internal state.
	 * 
	 * @return <code>true</code> if this manager contains dynamic items, and
	 *         <code>false</code> otherwise
	 */
	protected boolean hasDynamicItems() {
		return (dynamicItems > 0);
	}

	/**
	 * Returns the index of the item with the given id.
	 * 
	 * @param id
	 *            The id of the item whose index is requested.
	 * 
	 * @return <code>int</code> the index or -1 if the item is not found
	 */
	public int indexOf(String id) {
		for (int i = 0; i < contributions.size(); i++) {
			IContributionItem item = (IContributionItem) contributions.get(i);
			String itemId = item.getId();
			if (itemId != null && itemId.equalsIgnoreCase(id)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the object in the internal structure. This is
	 * different from <code>indexOf(String id)</code> since some contribution
	 * items may not have an id.
	 * 
	 * @param item
	 *            The contribution item
	 * @return the index, or -1 if the item is not found
	 */
	protected int indexOf(IContributionItem item) {
		return contributions.indexOf(item);
	}

	/**
	 * Insert the item at the given index.
	 * 
	 * @param index
	 *            The index to be used for insertion
	 * @param item
	 *            The item to be inserted
	 */
	public void insert(int index, IContributionItem item) {
		if (index > contributions.size()) {
			throw new IndexOutOfBoundsException(
					"inserting " + item.getId() + " at " + index); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (allowItem(item)) {
			contributions.add(index, item);
			itemAdded(item);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void insertAfter(String ID, IAction action) {
		insertAfter(ID, new ActionContributionItem(action));
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void insertAfter(String ID, IContributionItem item) {
		IContributionItem ci = find(ID);
		if (ci == null) {
			throw new IllegalArgumentException("can't find ID" + ID);//$NON-NLS-1$
		}
		int ix = contributions.indexOf(ci);
		if (ix >= 0) {
			// System.out.println("insert after: " + ix);
			if (allowItem(item)) {
				contributions.add(ix + 1, item);
				itemAdded(item);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void insertBefore(String ID, IAction action) {
		insertBefore(ID, new ActionContributionItem(action));
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void insertBefore(String ID, IContributionItem item) {
		IContributionItem ci = find(ID);
		if (ci == null) {
			throw new IllegalArgumentException("can't find ID " + ID);//$NON-NLS-1$
		}
		int ix = contributions.indexOf(ci);
		if (ix >= 0) {
			// System.out.println("insert before: " + ix);
			if (allowItem(item)) {
				contributions.add(ix, item);
				itemAdded(item);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public boolean isDirty() {
		if (isDirty) {
			return true;
		}
		if (hasDynamicItems()) {
			for (Iterator iter = contributions.iterator(); iter.hasNext();) {
				IContributionItem item = (IContributionItem) iter.next();
				if (item.isDirty()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public boolean isEmpty() {
		return contributions.isEmpty();
	}

	/**
	 * The given item was added to the list of contributions. Marks the manager
	 * as dirty and updates the number of dynamic items, and the memento.
	 * 
	 * @param item
	 *            the item to be added
	 * 
	 */
	protected void itemAdded(IContributionItem item) {
		item.setParent(this);
		markDirty();
		if (item.isDynamic()) {
			dynamicItems++;
		}
	}

	/**
	 * The given item was removed from the list of contributions. Marks the
	 * manager as dirty and updates the number of dynamic items.
	 * 
	 * @param item
	 *            remove given parent from list of contributions
	 */
	protected void itemRemoved(IContributionItem item) {
		item.setParent(null);
		markDirty();
		if (item.isDynamic()) {
			dynamicItems--;
		}
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void markDirty() {
		setDirty(true);
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void prependToGroup(String groupName, IAction action) {
		addToGroup(groupName, new ActionContributionItem(action), false);
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void prependToGroup(String groupName, IContributionItem item) {
		addToGroup(groupName, item, false);
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public IContributionItem remove(String ID) {
		IContributionItem ci = find(ID);
		if (ci == null) {
			return null;
		}
		return remove(ci);
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public IContributionItem remove(IContributionItem item) {
		if (contributions.remove(item)) {
			itemRemoved(item);
			return item;
		}
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void removeAll() {
		IContributionItem[] items = getItems();
		contributions.clear();
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			itemRemoved(item);
		}
		dynamicItems = 0;
		markDirty();
	}

	/**
	 * Replaces the item of the given identifier with another contribution item.
	 * This can be used, for example, to replace large contribution items with
	 * placeholders to avoid memory leaks. If the identifier cannot be found in
	 * the current list of items, then this does nothing. If multiple
	 * occurrences are found, then the replacement items is put in the first
	 * position and the other positions are removed.
	 * 
	 * @param identifier
	 *            The identifier to look for in the list of contributions;
	 *            should not be <code>null</code>.
	 * @param replacementItem
	 *            The contribution item to replace the old item; must not be
	 *            <code>null</code>. Use
	 *            {@link org.eclipse.jface.action.ContributionManager#remove(java.lang.String) remove}
	 *            if that is what you want to do.
	 * @return <code>true</code> if the given identifier can be; <code>
	 */
	public boolean replaceItem(final String identifier,
			final IContributionItem replacementItem) {
		if (identifier == null) {
			return false;
		}

		final int index = indexOf(identifier);
		if (index < 0) {
			return false; // couldn't find the item.
		}

		// Remove the old item.
		final IContributionItem oldItem = (IContributionItem) contributions
				.get(index);
		itemRemoved(oldItem);

		// Add the new item.
		contributions.set(index, replacementItem);
		itemAdded(replacementItem); // throws NPE if (replacementItem == null)

		// Go through and remove duplicates.
		for (int i = contributions.size() - 1; i > index; i--) {
			IContributionItem item = (IContributionItem) contributions.get(i);
			if ((item != null) && (identifier.equals(item.getId()))) {
				if (Policy.TRACE_TOOLBAR) {
					System.out
							.println("Removing duplicate on replace: " + identifier); //$NON-NLS-1$
				}
				contributions.remove(i);
				itemRemoved(item);
			}
		}

		return true; // success
	}

	/**
	 * Sets whether this manager is dirty. When dirty, the list of contributions
	 * is not accurately reflected in the corresponding widgets.
	 * 
	 * @param dirty
	 *            <code>true</code> if this manager is dirty, and
	 *            <code>false</code> if it is up-to-date
	 */
	protected void setDirty(boolean dirty) {
		isDirty = dirty;
	}

	/**
	 * Sets the overrides for this contribution manager
	 * 
	 * @param newOverrides
	 *            the overrides for the items of this manager
	 */
	public void setOverrides(IContributionManagerOverrides newOverrides) {
		overrides = newOverrides;
	}

	/**
	 * An internal method for setting the order of the contribution items.
	 * 
	 * @param items
	 *            the contribution items in the specified order
	 */
	protected void internalSetItems(IContributionItem[] items) {
		contributions.clear();
		for (int i = 0; i < items.length; i++) {
			if (allowItem(items[i])) {
				contributions.add(items[i]);
			}
		}
	}
}
