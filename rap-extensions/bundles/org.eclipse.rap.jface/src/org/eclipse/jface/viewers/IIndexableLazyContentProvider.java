/*******************************************************************************
 * Copyright (c) 2008, 2009 Peter Centgraf and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Centgraf - initial API and implementation (bug 251575)
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * Adds efficient element indexing support to ILazyContentProvider.
 * 
 * @since 1.3
 */
public interface IIndexableLazyContentProvider extends ILazyContentProvider {
	/**
	 * Find the row index of the parameter element in the set of contents provided
	 * by this object.  Under normal usage, this method will only be used to 
	 * implement <code>StructuredViewer#setSelection(ISelection)</code> more
	 * efficiently.
	 *
	 * @param element the element to find within the contents served here
	 * @return the zero-based index of the element, or -1 if the element is not found
	 */
	public int findElement(Object element);
}
