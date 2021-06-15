/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * Interface for objects that support elements with a checked state.
 *
 * @see ICheckStateListener
 * @see CheckStateChangedEvent
 */
public interface ICheckable {
    /**
     * Adds a listener for changes to the checked state of elements
     * in this viewer.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a check state listener
     */
    public void addCheckStateListener(ICheckStateListener listener);

    /**
     * Returns the checked state of the given element.
     *
     * @param element the element
     * @return <code>true</code> if the element is checked,
     *   and <code>false</code> if not checked
     */
    public boolean getChecked(Object element);

    /**
     * Removes the given check state listener from this viewer.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a check state listener
     */
    public void removeCheckStateListener(ICheckStateListener listener);

    /**
     * Sets the checked state for the given element in this viewer.
     * Does not fire events to check state listeners.
     *
     * @param element the element
     * @param state <code>true</code> if the item should be checked,
     *  and <code>false</code> if it should be unchecked
     * @return <code>true</code> if the checked state could be set, 
     *  and <code>false</code> otherwise
     */
    public boolean setChecked(Object element, boolean state);
}
