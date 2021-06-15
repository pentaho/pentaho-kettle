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

/**
 * Interface for a selection.
 *
 * @see ISelectionProvider
 * @see ISelectionChangedListener
 * @see SelectionChangedEvent
 */
public interface ISelection {

    /**
     * Returns whether this selection is empty.
     * 
     * @return <code>true</code> if this selection is empty,
     *   and <code>false</code> otherwise
     */
    public boolean isEmpty();
}
