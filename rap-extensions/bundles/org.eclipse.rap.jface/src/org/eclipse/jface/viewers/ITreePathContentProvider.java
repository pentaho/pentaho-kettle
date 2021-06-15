/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * An interface to content providers for tree-structure-oriented viewers that
 * provides content based on the path of elements in the tree viewer.
 * 
 * @see AbstractTreeViewer
 * @since 1.0
 */
public interface ITreePathContentProvider extends IStructuredContentProvider {

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
	 * Returns the child elements of the last element in the given path.
	 * Implementors may want to use the additional context of the complete path
	 * of a parent element in order to decide which children to return.
	 * <p>
	 * The provided path is relative to the input. The root elements must
	 * be obtained by calling
	 * {@link IStructuredContentProvider#getElements(Object)}.
	 * </p>
	 * The result is not modified by the viewer.
	 * 
	 * @param parentPath
	 *            the path of the parent element
	 * @return an array of child elements
	 */
	public Object[] getChildren(TreePath parentPath);

	/**
	 * Returns whether the last element of the given path has children.
	 * <p>
	 * Intended as an optimization for when the viewer does not need the actual
	 * children. Clients may be able to implement this more efficiently than
	 * <code>getChildren</code>.
	 * </p>
	 * 
	 * @param path
	 *            the path
	 * @return <code>true</code> if the lat element of the path has children,
	 *         and <code>false</code> if it has no children
	 */
	public boolean hasChildren(TreePath path);

	/**
	 * Return the possible parent paths for the given element. An empty array
	 * can be returned if the paths cannot be computed. If the element is 
	 * a potential child of the input of the viewer, an empty tree path
	 * should be an entry in the returned array.
	 * 
	 * @param element
	 *            the element
	 * @return the possible parent paths for the given element
	 */
	public TreePath[] getParents(Object element);
}
