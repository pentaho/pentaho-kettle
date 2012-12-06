/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.gui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Class to keep track of which color is a system color (managed by the OS) and which is not.
 * 
 * @author Matt
 * @since 2006-06-15
 *
 */
public class ManagedColor
{
    private Color color;
    private boolean systemColor;
    
    /**
     * @param color The color
     * @param systemColor true if this is a system color and doesn't need to be disposed off
     */
    public ManagedColor(Color color, boolean systemColor)
    {
        this.color = color;
        this.systemColor = systemColor;
    }

    /**
     * Create a new managed color by using the Red Green & Blue values.
     * @param display
     * @param rgb
     */
    public ManagedColor(Display display, RGB rgb)
    {
        this.color = new Color(display, rgb);
        this.systemColor = false;
    }

    /**
     * Create a new managed color by using the Red Green & Blue values.
     * @param display
     * @param r Red composite
     * @param g Green composite
     * @param b Blue composite
     */
    public ManagedColor(Display display, int r, int g, int b)
    {
        this.color = new Color(display, r, g, b);
        this.systemColor = false;
    }


    /**
     * Create a managed color by specifying the color (SWT.COLOR_*)
     * @param display
     * @param color
     */
    public ManagedColor(Display display, int color)
    {
        this.color = display.getSystemColor(color);
        this.systemColor = false;
    }

    /**
     * Free the managed resource if it hasn't already been done and if this is not a system color
     *
     */
    public void dispose()
    {
        // System color and already disposed off colors don't need to be disposed!
        if (!systemColor && !color.isDisposed())
        {
            color.dispose();
        }
    }

    /**
     * @return Returns the color.
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        this.color = color;
    }

    /**
     * @param systemColor the systemColor to set
     */
    public void setSystemColor(boolean systemColor)
    {
        this.systemColor = systemColor;
    }

    /**
     * @return true if this is a system color.
     */
    public boolean isSystemColor()
    {
        return systemColor;
    }
}
