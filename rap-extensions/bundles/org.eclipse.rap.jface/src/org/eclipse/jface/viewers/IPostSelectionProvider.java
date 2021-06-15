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
 * Selection provider extension interface to allow providers
 * to notify about post selection changed events.
 * A post selection changed event is equivalent to selection changed event
 * if the selection change was triggered by the mouse, but it has a delay 
 * if the selection change is triggered by keyboard navigation.
 *
 * @see ISelectionProvider
 * 
 * @since 1.0
 */
public interface IPostSelectionProvider extends ISelectionProvider {

    /**
     * Adds a listener for post selection changes in this selection provider.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a selection changed listener
     */
    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener);

    /**
     * Removes the given listener for post selection changes from this selection 
     * provider.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a selection changed listener
     */
    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener);

}
