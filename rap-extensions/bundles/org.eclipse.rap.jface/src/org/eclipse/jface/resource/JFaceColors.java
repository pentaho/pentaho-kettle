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
package org.eclipse.jface.resource;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * JFaceColors is the class that stores references
 * to all of the colors used by JFace.
 */
public class JFaceColors {

    /**
     * @param display the display the color is from
     * @return the Color used for banner backgrounds
     * @see SWT#COLOR_LIST_BACKGROUND
     * @see Display#getSystemColor(int)
     */
    public static Color getBannerBackground(Display display) {
        return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    }

    /**
     * @param display the display the color is from
     * @return the Color used for banner foregrounds
     * @see SWT#COLOR_LIST_FOREGROUND
     * @see Display#getSystemColor(int)
     */
    public static Color getBannerForeground(Display display) {
        return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    }

    /**
     * @param display the display the color is from
     * @return the background Color for widgets that display errors.
     * @see SWT#COLOR_WIDGET_BACKGROUND
     * @see Display#getSystemColor(int)
     */
    public static Color getErrorBackground(Display display) {
        return display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    }

    /**
     * @param display the display the color is from
     * @return the border Color for widgets that display errors.
     * @see SWT#COLOR_WIDGET_DARK_SHADOW
     * @see Display#getSystemColor(int)
     */
    public static Color getErrorBorder(Display display) {
        return display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    }

    /**
     * @param display the display the color is from
     * @return the default color to use for displaying errors.
     * @see ColorRegistry#get(String)
     * @see JFacePreferences#ERROR_COLOR
     */
    public static Color getErrorText(Display display) {
        return JFaceResources.getColorRegistry().get(
                JFacePreferences.ERROR_COLOR);
    }

    /**
     * @param display the display the color is from
     * @return the default color to use for displaying hyperlinks.
     * @see ColorRegistry#get(String)
     * @see JFacePreferences#HYPERLINK_COLOR
     */
    public static Color getHyperlinkText(Display display) {
        return JFaceResources.getColorRegistry().get(
                JFacePreferences.HYPERLINK_COLOR);
    }

    /**
     * @param display the display the color is from
     * @return the default color to use for displaying active hyperlinks.
     * @see ColorRegistry#get(String)
     * @see JFacePreferences#ACTIVE_HYPERLINK_COLOR
     */
    public static Color getActiveHyperlinkText(Display display) {
        return JFaceResources.getColorRegistry().get(
                JFacePreferences.ACTIVE_HYPERLINK_COLOR);
    }

    /**
     * Clear out the cached color for name. This is generally
     * done when the color preferences changed and any cached colors
     * may be disposed. Users of the colors in this class should add a IPropertyChangeListener
     * to detect when any of these colors change.
     * @param colorName name of the color
     * 
     * @deprecated JFaceColors no longer maintains a cache of colors.  This job 
     * is now handled by the ColorRegistry.
     */
    public static void clearColor(String colorName) {
        //no-op
    }

    /**
     * Dispose of all allocated colors. Called on workbench
     * shutdown.
     * 
     * @deprecated JFaceColors no longer maintains a cache of colors.  This job 
     * is now handled by the ColorRegistry.
     */
    public static void disposeColors() {
        //no-op
    }

    /**
     * Set the foreground and background colors of the
     * control to the specified values. If the values are
     * null than ignore them. 
     * @param control the control the foreground and/or background color should be set
     * 
     * @param foreground Color the foreground color (maybe <code>null</code>)
     * @param background Color the background color (maybe <code>null</code>)
     */
    public static void setColors(Control control, Color foreground,
            Color background) {
        if (foreground != null) {
			control.setForeground(foreground);
		}
        if (background != null) {
			control.setBackground(background);
		}
    }

}
