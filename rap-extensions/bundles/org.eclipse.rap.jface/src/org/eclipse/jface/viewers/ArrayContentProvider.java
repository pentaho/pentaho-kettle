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

package org.eclipse.jface.viewers;

import java.util.Collection;

/**
 * This implementation of <code>IStructuredContentProvider</code> handles
 * the case where the viewer input is an unchanging array or collection of elements.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework.
 * </p> 
 * 
 * @since 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ArrayContentProvider implements IStructuredContentProvider {

	private static ArrayContentProvider instance;

	/**
	 * Returns an instance of ArrayContentProvider. Since instances of this
	 * class do not maintain any state, they can be shared between multiple
	 * clients.
	 * 
	 * @return an instance of ArrayContentProvider
	 * 
	 * @since 1.3
	 */
	public static ArrayContentProvider getInstance() {
		synchronized(ArrayContentProvider.class) {
			if (instance == null) {
				instance = new ArrayContentProvider();
			}
			return instance;
		}
	}
    /**
     * Returns the elements in the input, which must be either an array or a
     * <code>Collection</code>. 
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
        if (inputElement instanceof Collection) {
			return ((Collection) inputElement).toArray();
		}
        return new Object[0];
    }

    /**
     * This implementation does nothing.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing.
    }

    /**
     * This implementation does nothing.
     */
    public void dispose() {
        // do nothing.
    }
}
