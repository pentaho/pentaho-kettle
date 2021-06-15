/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;

import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;

/**
 * A <code>TransferDragSourceListener</code> is a <code>DragSourceListener</code> 
 * that can handle one type of SWT {@link Transfer}. 
 * The purpose of a <code>TransferDragSourceListener</code> is to:
 * <ul>
 *   <li>Determine enablement for a drag operation. A <code>TransferDragSourceListener</code>
 * 	will not be used in a drag operation if the <code>DragSourceEvent#doit</code> field 
 * 	is set to false in <code>DragSourceListener#dragStart(DragSourceEvent)</code>.
 *   <li>Set data for a single type of drag and <code>Transfer</code> type.
 * </ul>
 * <p>
 * A <code>DelegatingDragAdapter</code> allows these functions to be implemented 
 * separately for unrelated types of drags. <code>DelegatingDragAdapter</code> then 
 * combines the function of each <code>TransferDragSourceListener</code>, while 
 * allowing them to be implemented as if they were the only <code>DragSourceListener</code>.
 * </p>
 * @since 1.3
 */
public interface TransferDragSourceListener extends DragSourceListener {
    /**
     * Returns the <code>Transfer</code> type that this listener can provide data for.
     * 
     * @return the <code>Transfer</code> associated with this listener
     */
    Transfer getTransfer();
}
