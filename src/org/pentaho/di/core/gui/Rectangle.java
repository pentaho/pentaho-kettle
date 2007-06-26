/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.pentaho.di.core.gui;

public final class Rectangle extends org.eclipse.swt.graphics.Rectangle
{
	
    public Rectangle(int x, int y, int width, int height)
    {
    		super(x, y, width, height);
    }

    /**
     * Returns <code>true</code> if the given point is inside the area specified by the receiver, and
     * <code>false</code> otherwise.
     * 
     * @param pt the point to test for containment
     * @return <code>true</code> if the rectangle contains the point and <code>false</code> otherwise
     * 
     * @exception IllegalArgumentException
     * <ul>
     * <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
     * </ul>
     */
    public boolean contains(Point pt)
    {
        return contains(pt.x, pt.y);
    }

}
