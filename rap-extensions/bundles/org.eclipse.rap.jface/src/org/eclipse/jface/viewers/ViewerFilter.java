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
import java.util.ArrayList;

/**
 * A viewer filter is used by a structured viewer to
 * extract a subset of elements provided by its content provider.
 * <p>
 * Subclasses must implement the <code>select</code> method
 * and may implement the <code>isFilterProperty</code> method.
 * </p>
 * @see IStructuredContentProvider
 * @see StructuredViewer
 */
public abstract class ViewerFilter implements Serializable {
    /**
     * Creates a new viewer filter.
     */
    protected ViewerFilter() {
    }

    /**
     * Filters the given elements for the given viewer.
     * The input array is not modified.
     * <p>
     * The default implementation of this method calls 
     * <code>select</code> on each element in the array, 
     * and returns only those elements for which <code>select</code>
     * returns <code>true</code>.
     * </p>
     * @param viewer the viewer
     * @param parent the parent element
     * @param elements the elements to filter
     * @return the filtered elements
     */
    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
        int size = elements.length;
        ArrayList out = new ArrayList(size);
        for (int i = 0; i < size; ++i) {
            Object element = elements[i];
            if (select(viewer, parent, element)) {
				out.add(element);
			}
        }
        return out.toArray();
    }

    /**
     * Filters the given elements for the given viewer.
     * The input array is not modified.
     * <p>
     * The default implementation of this method calls 
     * {@link #filter(Viewer, Object, Object[])} with the 
     * parent from the path. Subclasses may override
     * </p>
     * @param viewer the viewer
     * @param parentPath the path of the parent element
     * @param elements the elements to filter
     * @return the filtered elements
     * @since 1.0
     */
    public Object[] filter(Viewer viewer, TreePath parentPath, Object[] elements) {
        return filter(viewer, parentPath.getLastSegment(), elements);
    }
    
    /**
     * Returns whether this viewer filter would be affected 
     * by a change to the given property of the given element.
     * <p>
     * The default implementation of this method returns <code>false</code>.
     * Subclasses should reimplement.
     * </p>
     *
     * @param element the element
     * @param property the property
     * @return <code>true</code> if the filtering would be affected,
     *    and <code>false</code> if it would be unaffected
     */
    public boolean isFilterProperty(Object element, String property) {
        return false;
    }

    /**
     * Returns whether the given element makes it through this filter.
     *
     * @param viewer the viewer
     * @param parentElement the parent element
     * @param element the element
     * @return <code>true</code> if element is included in the
     *   filtered set, and <code>false</code> if excluded
     */
    public abstract boolean select(Viewer viewer, Object parentElement,
            Object element);
}
