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

import java.util.EventObject;

/**
 * Event object describing a tree node being expanded
 * or collapsed. The source of these events is the tree viewer.
 *
 * @see ITreeViewerListener
 */
public class TreeExpansionEvent extends EventObject {

    /**
     * Generated serial version UID for this class.
     * @since 1.0
     */
    private static final long serialVersionUID = 3618414930227835185L;
    
    /**
     * The element that was expanded or collapsed.
     */
    private Object element;

    /**
     * Creates a new event for the given source and element.
     *
     * @param source the tree viewer
     * @param element the element
     */
    public TreeExpansionEvent(AbstractTreeViewer source, Object element) {
        super(source);
        this.element = element;
    }

    /**
     * Returns the element that got expanded or collapsed.
     *
     * @return the element
     */
    public Object getElement() {
        return element;
    }

    /**
     * Returns the originator of the event.
     *
     * @return the originating tree viewer
     */
    public AbstractTreeViewer getTreeViewer() {
        return (AbstractTreeViewer) source;
    }
}
