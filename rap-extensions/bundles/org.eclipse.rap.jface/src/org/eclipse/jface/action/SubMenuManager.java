/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A <code>SubMenuManager</code> is used to define a set of contribution
 * items within a parent manager.  Once defined, the visibility of the entire set can 
 * be changed as a unit.
 * <p>
 * A client may ask for and make additions to a submenu.  The visibility of these items
 * is also controlled by the visibility of the <code>SubMenuManager</code>.
 * </p>
 */
public class SubMenuManager extends SubContributionManager implements
        IMenuManager {

    /**
     * Maps each submenu in the manager to a wrapper.  The wrapper is used to
     * monitor additions and removals.  If the visibility of the manager is modified
     * the visibility of the submenus is also modified.
     */
    private Map mapMenuToWrapper;

    /**
     * List of registered menu listeners (element type: <code>IMenuListener</code>).
     */
    private ListenerList menuListeners = new ListenerList();

    /**
     * The menu listener added to the parent.  Lazily initialized
     * in addMenuListener.
     */
    private IMenuListener menuListener;

    /**
     * Constructs a new manager.
     *
     * @param mgr the parent manager.  All contributions made to the 
     *      <code>SubMenuManager</code> are forwarded and appear in the
     *      parent manager.
     */
    public SubMenuManager(IMenuManager mgr) {
        super(mgr);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuManager#addMenuListener(org.eclipse.jface.action.IMenuListener)
     */
    public void addMenuListener(IMenuListener listener) {
        menuListeners.add(listener);
        if (menuListener == null) {
            menuListener = new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    Object[] listeners = menuListeners.getListeners();
                    for (int i = 0; i < listeners.length; ++i) {
                        ((IMenuListener) listeners[i])
                                .menuAboutToShow(SubMenuManager.this);
                    }
                }
            };
        }
        getParentMenuManager().addMenuListener(menuListener);
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    public void dispose() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.SubContributionManager#disposeManager()
     */
    public void disposeManager() {
        if (menuListener != null) {
            getParentMenuManager().removeMenuListener(menuListener);
            menuListener = null;
            menuListeners.clear();
        }
        // Dispose wrapped menus in addition to removing them.
        // See bugs 64024 and 73715 for details.
        // important to dispose menu wrappers before call to super,
        // otherwise super's call to removeAll will remove them
        // before they can be disposed
        if (mapMenuToWrapper != null) {
            Iterator iter = mapMenuToWrapper.values().iterator();
            while (iter.hasNext()) {
                SubMenuManager wrapper = (SubMenuManager) iter.next();
                wrapper.disposeManager();
            }
            mapMenuToWrapper.clear();
            mapMenuToWrapper = null;
        }
        super.disposeManager();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Composite)
     */
    public void fill(Composite parent) {
        if (isVisible()) {
			getParentMenuManager().fill(parent);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar, int)
     */
    public void fill(CoolBar parent, int index) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
     */
    public void fill(Menu parent, int index) {
        if (isVisible()) {
			getParentMenuManager().fill(parent, index);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
     */
    public void fill(ToolBar parent, int index) {
        if (isVisible()) {
			getParentMenuManager().fill(parent, index);
		}
    }

    /* (non-Javadoc)
     * Method declared on IContributionManager.
     *
     * Returns the item passed to us, not the wrapper.
     * In the case of menu's not added by this manager,
     * ensure that we return a wrapper for the menu.
     */
    public IContributionItem find(String id) {
        IContributionItem item = getParentMenuManager().find(id);
        if (item instanceof SubContributionItem) {
			// Return the item passed to us, not the wrapper.
            item = unwrap(item);
		}

        if (item instanceof IMenuManager) {
            // if it is a menu manager wrap it before returning
            IMenuManager menu = (IMenuManager) item;
            item = getWrapper(menu);
        }

        return item;
    }

    /**
     * <p>
     * The menu returned is wrapped within a <code>SubMenuManager</code> to
     * monitor additions and removals.  If the visibility of this menu is modified
     * the visibility of the submenus is also modified.
     * </p>
     */
    public IMenuManager findMenuUsingPath(String path) {
        IContributionItem item = findUsingPath(path);
        if (item instanceof IMenuManager) {
            return (IMenuManager) item;
        }
        return null;
    }

    /* (non-Javadoc)
     * Method declared on IMenuManager.
     *
     * Returns the item passed to us, not the wrapper.
     *
     * We use use the same algorithm as MenuManager.findUsingPath, but unwrap
     * submenus along so that SubMenuManagers are visible.
     */
    public IContributionItem findUsingPath(String path) {
        String id = path;
        String rest = null;
        int separator = path.indexOf('/');
        if (separator != -1) {
            id = path.substring(0, separator);
            rest = path.substring(separator + 1);
        }
        IContributionItem item = find(id); // unwraps item
        if (rest != null && item instanceof IMenuManager) {
            IMenuManager menu = (IMenuManager) item;
            item = menu.findUsingPath(rest);
        }
        return item;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#getId()
     */
    public String getId() {
        return getParentMenuManager().getId();
    }

    /**
     * @return the parent menu manager that this sub-manager contributes to. 
     */
    protected final IMenuManager getParentMenuManager() {
        // Cast is ok because that's the only
        // thing we accept in the construtor.
        return (IMenuManager) getParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuManager#getRemoveAllWhenShown()
     */
    public boolean getRemoveAllWhenShown() {
        return false;
    }

    /**
     * Returns the menu wrapper for a menu manager.
     * <p>
     * The sub menus within this menu are wrapped within a <code>SubMenuManager</code> to
     * monitor additions and removals.  If the visibility of this menu is modified
     * the visibility of the sub menus is also modified.
     * <p>
     * @param mgr the menu manager to be wrapped
     *
     * @return the menu wrapper
     */
    protected IMenuManager getWrapper(IMenuManager mgr) {
        if (mapMenuToWrapper == null) {
            mapMenuToWrapper = new HashMap(4);
        }
        SubMenuManager wrapper = (SubMenuManager) mapMenuToWrapper.get(mgr);
        if (wrapper == null) {
            wrapper = wrapMenu(mgr);
            mapMenuToWrapper.put(mgr, wrapper);
        }
        return wrapper;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isDynamic()
     */
    public boolean isDynamic() {
        return getParentMenuManager().isDynamic();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isEnabled()
     */
    public boolean isEnabled() {
        return isVisible() && getParentMenuManager().isEnabled();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isGroupMarker()
     */
    public boolean isGroupMarker() {
        return getParentMenuManager().isGroupMarker();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isSeparator()
     */
    public boolean isSeparator() {
        return getParentMenuManager().isSeparator();
    }

    /**
     * Remove all contribution items.
     */
    public void removeAll() {
        super.removeAll();
        if (mapMenuToWrapper != null) {
            Iterator iter = mapMenuToWrapper.values().iterator();
            while (iter.hasNext()) {
                SubMenuManager wrapper = (SubMenuManager) iter.next();
                wrapper.removeAll();
            }
            mapMenuToWrapper.clear();
            mapMenuToWrapper = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuManager#removeMenuListener(org.eclipse.jface.action.IMenuListener)
     */
    public void removeMenuListener(IMenuListener listener) {
        menuListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
     */
    public void saveWidgetState() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#setParent(org.eclipse.jface.action.IContributionManager)
     */
    public void setParent(IContributionManager parent) {
        // do nothing, our "parent manager's" parent 
        // is set when it is added to a manager
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuManager#setRemoveAllWhenShown(boolean)
     */
    public void setRemoveAllWhenShown(boolean removeAll) {
        Assert.isTrue(false, "Should not be called on submenu manager"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.SubContributionManager#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (mapMenuToWrapper != null) {
            Iterator iter = mapMenuToWrapper.values().iterator();
            while (iter.hasNext()) {
                SubMenuManager wrapper = (SubMenuManager) iter.next();
                wrapper.setVisible(visible);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#update()
     */
    public void update() {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
        getParentMenuManager().update();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionManager#update(boolean)
     */
    public void update(boolean force) {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
        getParentMenuManager().update(force);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#update(java.lang.String)
     */
    public void update(String id) {
        getParentMenuManager().update(id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuManager#updateAll(boolean)
     */
    public void updateAll(boolean force) {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
        getParentMenuManager().updateAll(force);
    }

    /**
     * Wraps a menu manager in a sub menu manager, and returns the new wrapper.
     * @param menu the menu manager to wrap
     * @return the new wrapped menu manager
     */
    protected SubMenuManager wrapMenu(IMenuManager menu) {
        SubMenuManager mgr = new SubMenuManager(menu);
        mgr.setVisible(isVisible());
        return mgr;
    }
}
