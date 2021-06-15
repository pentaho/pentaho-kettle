/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.io.Serializable;

/** 
 * A content provider mediates between the viewer's model
 * and the viewer itself.
 * 
 * @see org.eclipse.jface.viewers.ContentViewer#setContentProvider(IContentProvider)
 */
public interface IContentProvider extends Serializable {
    /**
     * Disposes of this content provider.  
     * This is called by the viewer when it is disposed.
     * <p>
     * The viewer should not be updated during this call, as it is in the process
     * of being disposed.
     * </p>
     */
    public void dispose();

    /**
     * Notifies this content provider that the given viewer's input
     * has been switched to a different element.
     * <p>
     * A typical use for this method is registering the content provider as a listener
     * to changes on the new input (using model-specific means), and deregistering the viewer 
     * from the old input. In response to these change notifications, the content provider
     * should update the viewer (see the add, remove, update and refresh methods on the viewers).
     * </p>
     * <p>
     * The viewer should not be updated during this call, as it might be in the process
     * of being disposed.
     * </p>
     *
     * @param viewer the viewer
     * @param oldInput the old input element, or <code>null</code> if the viewer
     *   did not previously have an input
     * @param newInput the new input element, or <code>null</code> if the viewer
     *   does not have an input
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput);
}
