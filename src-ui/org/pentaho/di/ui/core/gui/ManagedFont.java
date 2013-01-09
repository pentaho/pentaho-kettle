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
