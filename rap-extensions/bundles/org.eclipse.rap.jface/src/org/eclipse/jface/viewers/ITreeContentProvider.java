/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * An interface to content providers for tree-structure-oriented
 * viewers.
 *
 * @see AbstractTreeViewer
 */
public interface ITreeContentProvider extends IStructuredContentProvider {

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>NOTE:</b> The returned array must not contain the given
	 * <code>inputElement</code>, since this leads to recursion issues in
	 * {@link AbstractTreeViewer} (see
	 * <a href="https://bugs.eclipse.org/9262">bug 9262</a>).
	 * </p>
	 */
	public Object[] getElements(Object inputElement);
	
    /**
     * Returns the child elements of the given parent element.
     * <p>
     * The difference between this method and <code>IStructuredContentProvider.getElements</code> 
     * is that <code>getElements</code> is called to obtain the 
     * tree viewer's root elements, whereas <code>getChildren</code> is used
     * to obtain the children of a given parent element in the tree (including a root).
     * </p>
     * The result is not modified by the viewer.
     *
     * @param parentElement the parent element
     * @return an array of child elements
     */
    public Object[] getChildren(Object parentElement);

    /**
     * Returns the parent for the given element, or <code>null</code> 
     * indicating that the parent can't be computed. 
     * In this case the tree-structured viewer can't expand
     * a given node correctly if requested.
     *
     * @param element the element
     * @return the parent element, or <code>null</code> if it
     *   has none or if the parent cannot be computed
     */
    public Object getParent(Object element);

    /**
     * Returns whether the given element has children.
     * <p>
     * Intended as an optimization for when the viewer does not
     * need the actual children.  Clients may be able to implement
     * this more efficiently than <code>getChildren</code>.
     * </p>
     *
     * @param element the element
     * @return <code>true</code> if the given element has children,
     *  and <code>false</code> if it has no children
     */
    public boolean hasChildren(Object element);
}
