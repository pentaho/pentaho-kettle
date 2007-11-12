/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.core.gui;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Class to keep track of which font is a system font (managed by the OS) and which is not.
 * 
 * @author Matt
 * @since 2006-06-15
 *
 */
public class ManagedFont
{
    private Font font;
    private boolean systemFont;
    
    /**
     * @param font The font
     * @param systemFont true if this is a system font and doesn't need to be disposed off
     */
    public ManagedFont(Font font, boolean systemFont)
    {
        this.font = font;
        this.systemFont = systemFont;
    }

    /**
     * Create a new managed font by using fontdata
     * @param display the display to use
     * @param fontData The fontdata to create the font with.
     */
    public ManagedFont(Display display, FontData fontData)
    {
        this.font = new Font(display, fontData);
        this.systemFont = false;
    }

    /**
     * Free the managed resource if it hasn't already been done and if this is not a system font
     *
     */
    public void dispose()
    {
        // System color and already disposed off colors don't need to be disposed!
        if (!systemFont && !font.isDisposed())
        {
            font.dispose();
        }
    }

    /**
     * @return Returns the font.
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * @return true if this is a system font.
     */
    public boolean isSystemFont()
    {
        return systemFont;
    }

    /**
     * @param font the font to set
     */
    public void setFont(Font font)
    {
        this.font = font;
    }

    /**
     * @param systemFont the systemFont to set
     */
    public void setSystemFont(boolean systemFont)
    {
        this.systemFont = systemFont;
    }
}
