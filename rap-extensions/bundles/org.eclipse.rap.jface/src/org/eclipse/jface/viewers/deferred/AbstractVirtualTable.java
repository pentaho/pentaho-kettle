/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.io.Serializable;

import org.eclipse.swt.widgets.Control;

/**
 * Wrapper for a virtual-table-like widget. Contains all methods needed for lazy updates.
 * The JFace algorithms for deferred or lazy content providers should talk to this class
 * instead of directly to a TableViewer. This will allow them to be used with other virtual
 * viewers and widgets in the future.
 * 
 * <p>
 * For example, if SWT starts to support virtual Lists in the future, it should be possible
 * to create an adapter from <code>AbstractVirtualTable</code> to <code>ListViewer</code> in 
 * order to reuse the existing algorithms for deferred updates. 
 * </p>
 * 
 * <p>
 * This is package visiblity by design. It would only need to be made public if there was
 * a demand to use the deferred content provider algorithms like 
 * <code>BackgroundContentProvider</code> with non-JFace viewers.
 * </p>
 * 
 * @since 1.0
 */
abstract class AbstractVirtualTable implements Serializable {
    /**
     * Tells the receiver that the item at given row has changed. This may indicate
     * that a different element is now at this row, but does not necessarily indicate
     * that the element itself has changed. The receiver should request information for
     * this row the next time it becomes visibile.
     * 
     * @param index row to clear
     */
    public abstract void clear(int index);
    
    /**
     * Notifies the receiver that the given element is now located at the given index.
     * 
     * @param element object located at the row
     * @param itemIndex row number
     */
    public abstract void replace(Object element, int itemIndex);
    
    /**
     * Sets the item count for this table 
     * 
     * @param total new total number of items
     */
    public abstract void setItemCount(int total);
    
    /**
     * Returns the index of the top item visible in the table
     * 
     * @return the index of the top item visible in the table
     */
    public abstract int getTopIndex();
    
    /**
     * Returns the number of items currently visible in the table. This is
     * the size of the currently visible window, not the total size of the table.
     * 
     * @return the number of items currently visible in the table
     */
    public abstract int getVisibleItemCount();
    
    /**
     * Returns the total number of items in the table
     * 
     * @return the total number of items in the table
     */
    public abstract int getItemCount();
    
    /**
     * Returns the SWT control that this API is wrappering.
     * @return Control.
     */
    public abstract Control getControl();
}
