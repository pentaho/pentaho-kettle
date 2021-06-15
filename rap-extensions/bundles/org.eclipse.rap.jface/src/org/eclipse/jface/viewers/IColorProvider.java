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

import org.eclipse.swt.graphics.Color;

/**
 * Interface to provide color representation for a given element.
 * @see org.eclipse.jface.viewers.IColorDecorator
 */
public interface IColorProvider {

    /**
     * Provides a foreground color for the given element.
     * 
     * @param element the element
     * @return	the foreground color for the element, or <code>null</code> 
     *   to use the default foreground color
     */
    Color getForeground(Object element);

    /**
     * Provides a background color for the given element.
     * 
     * @param element the element
     * @return	the background color for the element, or <code>null</code> 
     *   to use the default background color
     */
    Color getBackground(Object element);
}
