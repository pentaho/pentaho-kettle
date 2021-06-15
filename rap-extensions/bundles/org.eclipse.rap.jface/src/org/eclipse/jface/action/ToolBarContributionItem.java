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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The <code>ToolBarContributionItem</code> class provides a wrapper for tool
 * bar managers when used in cool bar managers. It extends <code>ContributionItem</code>
 * but and provides some additional methods to customize the size of the cool
 * item and to retrieve the underlying tool bar manager.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ToolBarContributionItem extends ContributionItem implements IToolBarContributionItem {

    /**
     * A constant used by <code>setMinimumItemsToShow</code> and <code>getMinimumItemsToShow</code>
     * to indicate that all tool items should be shown in the cool item.
     */
    public static final int SHOW_ALL_ITEMS = -1;

    /**
     * The pull down menu used to list all hidden tool items if the current
     * size is less than the preffered size.
     */
    private MenuManager chevronMenuManager = null;

    /**
     * The widget created for this item; <code>null</code> before creation
     * and after disposal.
     */
    private CoolItem coolItem = null;

    /**
     * Current height of cool item
     */
    private int currentHeight = -1;

    /**
     * Current width of cool item.
     */
    private int currentWidth = -1;

    /**
     * A flag indicating that this item has been disposed. This prevents future
     * method invocations from doing things they shouldn't.
     */
    private boolean disposed = false;

    /**
     * Mininum number of tool items to show in the cool item widget.
     */
    private int minimumItemsToShow = SHOW_ALL_ITEMS;

    /**
     * The tool bar manager used to manage the tool items contained in the cool
     * item widget.
     */
    private ToolBarManager toolBarManager = null;

    /**
     * Enable/disable chevron support.
     */
    private boolean useChevron = true;

    /**
     * Convenience method equivalent to <code>ToolBarContributionItem(new ToolBarManager(), null)</code>.
     */
    public ToolBarContributionItem() {
        this(new ToolBarManager(), null);
    }

    /**
     * Convenience method equivalent to <code>ToolBarContributionItem(toolBarManager, null)</code>.
     * 
     * @param toolBarManager
     *            the tool bar manager
     */
    public ToolBarContributionItem(IToolBarManager toolBarManager) {
        this(toolBarManager, null);
    }

    /**
     * Creates a tool bar contribution item.
     * 
     * @param toolBarManager
     *            the tool bar manager to wrap
     * @param id
     *            the contribution item id, or <code>null</code> if none
     */
    public ToolBarContributionItem(IToolBarManager toolBarManager, String id) {
        super(id);
        Assert.isTrue(toolBarManager instanceof ToolBarManager);
        this.toolBarManager = (ToolBarManager) toolBarManager;
    }

    /**
     * Checks whether this contribution item has been disposed. If it has, and
     * the tracing options are active, then it prints some debugging
     * information.
     * 
     * @return <code>true</code> if the item is disposed; <code>false</code>
     *         otherwise.
     *  
     */
    private final boolean checkDisposed() {
        if (disposed) {
            if (Policy.TRACE_TOOLBAR) {
                System.out
                        .println("Method invocation on a disposed tool bar contribution item."); //$NON-NLS-1$
                new Exception().printStackTrace(System.out);
            }

            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#dispose()
     */
    public void dispose() {
        // Dispose of the ToolBar and all its contributions
        if (toolBarManager != null) {
            toolBarManager.dispose();
            toolBarManager = null;
        }

        /*
         * We need to dispose the cool item or we might be left holding a cool
         * item with a disposed control.
         */
        if ((coolItem != null) && (!coolItem.isDisposed())) {
            coolItem.dispose();
            coolItem = null;
        }

        // Mark this item as disposed.
        disposed = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar,
     *      int)
     */
    public void fill(CoolBar coolBar, int index) {
        if (checkDisposed()) {
            return;
        }

        if (coolItem == null && coolBar != null) {
            ToolBar oldToolBar = toolBarManager.getControl();
            ToolBar toolBar = toolBarManager.createControl(coolBar);
            if ((oldToolBar != null) && (oldToolBar.equals(toolBar))) {
                // We are using an old tool bar, so we need to update.
                toolBarManager.update(true);
            }

            // Do not create a coolItem if the toolbar is empty
            if (toolBar.getItemCount() < 1) {
				return;
			}
            int flags = SWT.DROP_DOWN;
            if (index >= 0) {
                coolItem = new CoolItem(coolBar, flags, index);
            } else {
                coolItem = new CoolItem(coolBar, flags);
            }
            // sets the back reference
            coolItem.setData(this);
            // Add the toolbar to the CoolItem widget
            coolItem.setControl(toolBar);

            // Handle Context Menu
            // ToolBarManager.createControl can actually return a pre-existing control.
            // Only add the listener if the toolbar was newly created (bug 62097).
            if (oldToolBar != toolBar) {
	            toolBar.addListener(SWT.MenuDetect, new Listener() {
	
	                public void handleEvent(Event event) {
	                    // if the toolbar does not have its own context menu then
	                    // handle the event
	                    if (toolBarManager.getContextMenuManager() == null) {
	                        handleContextMenu(event);
	                    }
	                }
	            });
            }

            // Handle for chevron clicking
            if (getUseChevron()) {
                // Chevron Support
                coolItem.addSelectionListener(new SelectionAdapter() {

                    public void widgetSelected(SelectionEvent event) {
                        if (event.detail == SWT.ARROW) {
                            handleChevron(event);
                        }
                    }
                });
            }

            // Handle for disposal
            coolItem.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent event) {
                    handleWidgetDispose(event);
                }
            });

            // Sets the size of the coolItem
            updateSize(true);
        }
    }

    /**
     * Returns a consistent set of wrap indices. The return value will always
     * include at least one entry and the first entry will always be zero.
     * CoolBar.getWrapIndices() is inconsistent in whether or not it returns an
     * index for the first row.
     */
    private int[] getAdjustedWrapIndices(int[] wraps) {
        int[] adjustedWrapIndices;
        if (wraps.length == 0) {
            adjustedWrapIndices = new int[] { 0 };
        } else {
            if (wraps[0] != 0) {
                adjustedWrapIndices = new int[wraps.length + 1];
                adjustedWrapIndices[0] = 0;
                for (int i = 0; i < wraps.length; i++) {
                    adjustedWrapIndices[i + 1] = wraps[i];
                }
            } else {
                adjustedWrapIndices = wraps;
            }
        }
        return adjustedWrapIndices;
    }

    /**
     * Returns the current height of the corresponding cool item.
     * 
     * @return the current height
     */
    public int getCurrentHeight() {
        if (checkDisposed()) {
            return -1;
        }
        return currentHeight;
    }

    /**
     * Returns the current width of the corresponding cool item.
     * 
     * @return the current size
     */
    public int getCurrentWidth() {
        if (checkDisposed()) {
            return -1;
        }
        return currentWidth;
    }

    /**
     * Returns the minimum number of tool items to show in the cool item.
     * 
     * @return the minimum number of tool items to show, or <code>SHOW_ALL_ITEMS</code>
     *         if a value was not set
     * @see #setMinimumItemsToShow(int)
     */
    public int getMinimumItemsToShow() {
        if (checkDisposed()) {
            return -1;
        }
        return minimumItemsToShow;
    }

    /**
     * Returns the internal tool bar manager of the contribution item.
     * 
     * @return the tool bar manager, or <code>null</code> if one is not
     *         defined.
     * @see IToolBarManager
     */
    public IToolBarManager getToolBarManager() {
        if (checkDisposed()) {
            return null;
        }
        return toolBarManager;
    }

    /**
     * Returns whether chevron support is enabled.
     * 
     * @return <code>true</code> if chevron support is enabled, <code>false</code>
     *         otherwise
     */
    public boolean getUseChevron() {
        if (checkDisposed()) {
            return false;
        }
        return useChevron;
    }

    /**
     * Create and display the chevron menu.
     */
    private void handleChevron(SelectionEvent event) {
        CoolItem item = (CoolItem) event.widget;
        Control control = item.getControl();
        if ((control instanceof ToolBar) == false) {
            return;
        }
        CoolBar coolBar = item.getParent();
        ToolBar toolBar = (ToolBar) control;
        Rectangle toolBarBounds = toolBar.getBounds();
        ToolItem[] items = toolBar.getItems();
        ArrayList hidden = new ArrayList();
        for (int i = 0; i < items.length; ++i) {
            Rectangle itemBounds = items[i].getBounds();
            if (!((itemBounds.x + itemBounds.width <= toolBarBounds.width) && (itemBounds.y
                    + itemBounds.height <= toolBarBounds.height))) {
                hidden.add(items[i]);
            }
        }

        // Create a pop-up menu with items for each of the hidden buttons.
        if (chevronMenuManager != null) {
            chevronMenuManager.dispose();
        }
        chevronMenuManager = new MenuManager();
        for (Iterator i = hidden.iterator(); i.hasNext();) {
            ToolItem toolItem = (ToolItem) i.next();
            IContributionItem data = (IContributionItem) toolItem.getData();
            if (data instanceof ActionContributionItem) {
                ActionContributionItem contribution = new ActionContributionItem(
                        ((ActionContributionItem) data).getAction());
                chevronMenuManager.add(contribution);
            } else if (data instanceof SubContributionItem) {
                IContributionItem innerData = ((SubContributionItem) data)
                        .getInnerItem();
                if (innerData instanceof ActionContributionItem) {
                    ActionContributionItem contribution = new ActionContributionItem(
                            ((ActionContributionItem) innerData).getAction());
                    chevronMenuManager.add(contribution);
                }
            } else if (data.isSeparator()) {
                chevronMenuManager.add(new Separator());
            }
        }
        Menu popup = chevronMenuManager.createContextMenu(coolBar);
        Point chevronPosition = coolBar.toDisplay(event.x, event.y);
        popup.setLocation(chevronPosition.x, chevronPosition.y);
        popup.setVisible(true);
    }

    /**
     * Handles the event when the toobar item does not have its own context
     * menu.
     * 
     * @param event
     *            the event object
     */
    private void handleContextMenu(Event event) {
        ToolBar toolBar = toolBarManager.getControl();
        // If parent has a menu then use that one
        Menu parentMenu = toolBar.getParent().getMenu();
        if ((parentMenu != null) && (!parentMenu.isDisposed())) {
            toolBar.setMenu(parentMenu);
            // Hook listener to remove menu once it has disapeared
            parentMenu.addListener(SWT.Hide, new Listener() {

                public void handleEvent(Event innerEvent) {
                    ToolBar innerToolBar = toolBarManager.getControl();
                    if (innerToolBar != null) {
                        innerToolBar.setMenu(null);
                        Menu innerParentMenu = innerToolBar.getParent()
                                .getMenu();
                        if (innerParentMenu != null) {
                            innerParentMenu.removeListener(SWT.Hide, this);
                        }
                    }
                }
            });
        }
    }

    /**
     * Handles the disposal of the widget.
     * 
     * @param event
     *            the event object
     */
    private void handleWidgetDispose(DisposeEvent event) {
        coolItem = null;
    }

    /**
     * A contribution item is visible iff its internal state is visible <em>or</em>
     * the tool bar manager contains something other than group markers and
     * separators.
     * 
     * @return <code>true</code> if the tool bar manager contains something
     *         other than group marks and separators, and the internal state is
     *         set to be visible.
     */
    public boolean isVisible() {
        if (checkDisposed()) {
            return false;
        }

        boolean visibleItem = false;
        if (toolBarManager != null) {
            IContributionItem[] contributionItems = toolBarManager.getItems();
            for (int i = 0; i < contributionItems.length; i++) {
                IContributionItem contributionItem = contributionItems[i];
                if ((!contributionItem.isGroupMarker())
                        && (!contributionItem.isSeparator())) {
                    visibleItem = true;
                    break;
                }
            }
        }

        return (visibleItem || super.isVisible());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
     */
    public void saveWidgetState() {
        if (checkDisposed()) {
            return;
        }
        if (coolItem == null) {
			return;
		}

        //1. Save current size
        CoolBar coolBar = coolItem.getParent();
        boolean isLastOnRow = false;
        int lastIndex = coolBar.getItemCount() - 1;
        int coolItemIndex = coolBar.indexOf(coolItem);
        int[] wrapIndicies = getAdjustedWrapIndices(coolBar.getWrapIndices());
        // Traverse through all wrap indicies backwards
        for (int row = wrapIndicies.length - 1; row >= 0; row--) {
            if (wrapIndicies[row] <= coolItemIndex) {

                int nextRow = row + 1;
                int nextRowStartIndex;
                if (nextRow > (wrapIndicies.length - 1)) {
                    nextRowStartIndex = lastIndex + 1;
                } else {
                    nextRowStartIndex = wrapIndicies[nextRow];
                }

                // Check to see if its the last item on the row
                if (coolItemIndex == (nextRowStartIndex - 1)) {
                    isLastOnRow = true;
                }
                break;
            }
        }

        // Save the preferred size as actual size for the last item on a row
        int nCurrentWidth;
        if (isLastOnRow) {
            nCurrentWidth = coolItem.getPreferredSize().x;
        } else {
            nCurrentWidth = coolItem.getSize().x;
        }
        setCurrentWidth(nCurrentWidth);
        setCurrentHeight(coolItem.getSize().y);
    }

    /**
     * Sets the current height of the cool item. Update(SIZE) should be called
     * to adjust the widget.
     * 
     * @param currentHeight
     *            the current height to set
     */
    public void setCurrentHeight(int currentHeight) {
        if (checkDisposed()) {
            return;
        }
        this.currentHeight = currentHeight;
    }

    /**
     * Sets the current width of the cool item. Update(SIZE) should be called
     * to adjust the widget.
     * 
     * @param currentWidth
     *            the current width to set
     */
    public void setCurrentWidth(int currentWidth) {
        if (checkDisposed()) {
            return;
        }
        this.currentWidth = currentWidth;
    }

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
    public void setMinimumItemsToShow(int minimumItemsToShow) {
        if (checkDisposed()) {
            return;
        }
        this.minimumItemsToShow = minimumItemsToShow;
    }

    /**
     * Enables or disables chevron support for the cool item. By default,
     * chevron support is enabled.
     * 
     * @param value
     *            <code>true</code> to enable chevron support, <code>false</code>
     *            otherwise.
     */
    public void setUseChevron(boolean value) {
        if (checkDisposed()) {
            return;
        }
        useChevron = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#update(java.lang.String)
     */
    public void update(String propertyName) {
        if (checkDisposed()) {
            return;
        }
        if (coolItem != null) {
            IToolBarManager manager = getToolBarManager();
            if (manager != null) {
                manager.update(true);
            }

            if ((propertyName == null)
                    || propertyName.equals(ICoolBarManager.SIZE)) {
                updateSize(true);
            }
        }
    }

    /**
     * Updates the cool items' preferred, minimum, and current size. The
     * preferred size is calculated based on the tool bar size and extra trim.
     * 
     * @param changeCurrentSize
     *            <code>true</code> if the current size should be changed to
     *            the preferred size, <code>false</code> to not change the
     *            current size
     */
    private void updateSize(boolean changeCurrentSize) {
        if (checkDisposed()) {
            return;
        }
        // cannot set size if coolItem is null
        if (coolItem == null || coolItem.isDisposed()) {
            return;
        }
        boolean locked = false;
        CoolBar coolBar = coolItem.getParent();
        try {
            // Fix odd behaviour with locked tool bars
            if (coolBar != null) {
                if (coolBar.getLocked()) {
                    coolBar.setLocked(false);
                    locked = true;
                }
            }
            ToolBar toolBar = (ToolBar) coolItem.getControl();
            if ((toolBar == null) || (toolBar.isDisposed())
                    || (toolBar.getItemCount() <= 0)) {
                // if the toolbar does not contain any items then dispose of
                // coolItem
                coolItem.setData(null);
                Control control = coolItem.getControl();
                if ((control != null) && !control.isDisposed()) {
                    control.dispose();
                    coolItem.setControl(null);
                }
                if (!coolItem.isDisposed()) {
                    coolItem.dispose();
                }
            } else {
                // If the toolbar item exists then adjust the size of the cool
                // item
                Point toolBarSize = toolBar.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT);
                // Set the preffered size to the size of the toolbar plus trim
                Point preferredSize = coolItem.computeSize(toolBarSize.x,
                        toolBarSize.y);
                coolItem.setPreferredSize(preferredSize);
                // note setMinimumSize must be called before setSize, see PR
                // 15565
                // Set minimum size
                if (getMinimumItemsToShow() != SHOW_ALL_ITEMS) {
                    int toolItemWidth = toolBar.getItems()[0].getWidth();
                    int minimumWidth = toolItemWidth * getMinimumItemsToShow();
                    coolItem.setMinimumSize(minimumWidth, toolBarSize.y);
                } else {
                    coolItem.setMinimumSize(toolBarSize.x, toolBarSize.y);
                }
                if (changeCurrentSize) {
                    // Set current size to preferred size
                    coolItem.setSize(preferredSize);
                }
            }
        } finally {
            // If the cool bar was locked, then set it back to locked
            if ((locked) && (coolBar != null)) {
                coolBar.setLocked(true);
            }
        }
    }

}
