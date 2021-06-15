/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     John Cortell, Freescale - bug 289409
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * The ILazyTreePathContentProvider is a tree path-based content provider for
 * tree viewers created using the SWT.VIRTUAL flag that only wish to return
 * their contents as they are queried.
 * 
 * @since 1.0
 */
public interface ILazyTreePathContentProvider extends IContentProvider {
	/**
	 * Called when a previously-blank item becomes visible in the TreeViewer. If
	 * the content provider knows the child element for the given parent at this
	 * index, it should respond by calling
	 * {@link TreeViewer#replace(Object, int, Object)}. The content provider
	 * should also update the child count for any replaced element by calling
	 * {@link TreeViewer#setChildCount(Object, int)}. If the given current child
	 * count is already correct, setChildCount does not have to be called since
	 * a call to replace will not change the child count. If the content
	 * provider doesn't know the child count at this point, and can more
	 * efficiently determine if the element has <i>any</i> children, then it can
	 * instead call {@link TreeViewer#setHasChildren(Object, boolean)}.
	 * <p>
	 * 
	 * <strong>NOTE</strong> #updateElement(int index) can be used to determine
	 * selection values. If TableViewer#replace(Object, int) is not called
	 * before returning from this method, selections may have missing or stale
	 * elements. In this situation it is suggested that the selection is asked
	 * for again after replace() has been called.
	 * 
	 * @param parentPath
	 *            The tree path of parent of the element, or if the element to
	 *            update is a root element, an empty tree path
	 * @param index
	 *            The index of the element to update in the tree
	 */
	public void updateElement(TreePath parentPath, int index);

	/**
	 * Called when the TreeViewer needs an up-to-date child count for the given
	 * tree path, for example from {@link TreeViewer#refresh()} and
	 * {@link TreeViewer#setInput(Object)}. If the content provider knows the
	 * element at the given tree path, it should respond by calling
	 * {@link TreeViewer#setChildCount(Object, int)}. If the given current
	 * child count is already correct, no action has to be taken by this content
	 * provider.
	 * 
	 * @param treePath
	 *            The tree path for which an up-to-date child count is needed, or
	 *            if the number of root elements is requested, the empty tree path 
	 * @param currentChildCount 
	 * 			  The current child count for the element that needs updating
	 */
	public void updateChildCount(TreePath treePath, int currentChildCount);
	
	/**
	 * Called when the TreeViewer needs up-to-date information whether the node
	 * at the given tree path can be expanded. If the content provider knows the
	 * element at the given tree path, it should respond by calling
	 * {@link TreeViewer#setHasChildren(Object, boolean)}. The content provider
	 * may also choose to call {@link TreeViewer#setChildCount(Object, int)}
	 * instead if it knows the number of children.
	 * 
	 * <p>
	 * Intended as an optimization for when the viewer does not need the actual
	 * children. Clients may be able to implement this more efficiently than
	 * <code>updateChildCount</code>.
	 * </p>
	 * 
	 * @param path
	 *            The tree path for which up-to-date information about children
	 *            is needed
	 */
	public void updateHasChildren(TreePath path);

	/**
	 * Return the possible parent paths for the given element. An empty array
	 * can be returned if the paths cannot be computed. In this case the
	 * tree-structured viewer can't expand a given node correctly if requested.
	 * If the element is a potential child of the input of the viewer, an empty
	 * tree path should be an entry in the returned array.
	 * 
	 * @param element
	 *            the element
	 * @return the possible parent paths for the given element
	 */
	public TreePath[] getParents(Object element);
}
