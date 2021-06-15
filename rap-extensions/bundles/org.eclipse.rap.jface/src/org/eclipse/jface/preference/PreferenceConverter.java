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
package org.eclipse.jface.preference;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * A utility class for dealing with preferences whose values are
 * common SWT objects (color, points, rectangles, and font data).
 * The static methods on this class handle the conversion between
 * the SWT objects and their string representations.
 * <p>
 * Usage:
 * <pre>
 * IPreferenceStore store = ...;
 * PreferenceConverter.setValue(store, "bg", new RGB(127,127,127));
 * ...
 * RBG bgColor = PreferenceConverter.getValue(store, "bg");
 * </pre>
 * </p>
 * <p>
 * This class contains static methods and fields only and cannot 
 * be instantiated.
 * </p>
 * Note: touching this class has the side effect of creating a display (static initializer).
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PreferenceConverter {

    /**
     * The default-default value for point preferences
     * (the origin, <code>(0,0)</code>).
     */
    public static final Point POINT_DEFAULT_DEFAULT = new Point(0, 0);

    /**
     * The default-default value for rectangle preferences
     * (the empty rectangle <code>(0,0,0,0)</code>).
     */
    public static final Rectangle RECTANGLE_DEFAULT_DEFAULT = new Rectangle(0,
            0, 0, 0);

    /**
     * The default-default value for color preferences 
     * (black, <code>RGB(0,0,0)</code>).
     */
    public static final RGB COLOR_DEFAULT_DEFAULT = new RGB(0, 0, 0);

    private static final String ENTRY_SEPARATOR = ";"; //$NON-NLS-1$

    /**
     * The default-default value for <code>FontData[]</code> preferences.
     */
    public static final FontData[] FONTDATA_ARRAY_DEFAULT_DEFAULT;

    /**
     * The default-default value for <code>FontData</code> preferences.
     */
    public static final FontData FONTDATA_DEFAULT_DEFAULT;
    static {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault ();
		}
		
        FONTDATA_ARRAY_DEFAULT_DEFAULT = display.getSystemFont().getFontData();
        /**
         * The default-default value for <code>FontData</code> preferences.
         * This is left in for compatibility purposes. It is recommended that
         * FONTDATA_ARRAY_DEFAULT_DEFAULT is actually used.
         */

        FONTDATA_DEFAULT_DEFAULT = FONTDATA_ARRAY_DEFAULT_DEFAULT[0];
    }

    /* (non-Javadoc)
     * private constructor to prevent instantiation.
     */
    private PreferenceConverter() {
        //no-op
    }

    /**
     * Helper method to construct a color from the given string.
     * @param value the indentifier for the color
     * @return RGB
     */
    private static RGB basicGetColor(String value) {

        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value)) {
			return COLOR_DEFAULT_DEFAULT;
		}

        RGB color = StringConverter.asRGB(value, null);
        if (color == null) {
			return COLOR_DEFAULT_DEFAULT;
		}
        return color;
    }

    /**
     * Helper method to construct a <code>FontData</code> from the given string.
     * String is in the form FontData;FontData; in order that
     * multiple FontDatas can be defined.
     * @param value the identifier for the font
     * @return FontData[]
     * 
     * @since 1.0
     */
    public static FontData[] basicGetFontData(String value) {
        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value)) {
			return FONTDATA_ARRAY_DEFAULT_DEFAULT;
		}

        //Read in all of them to get the value
        StringTokenizer tokenizer = new StringTokenizer(value, ENTRY_SEPARATOR);
        int numTokens = tokenizer.countTokens();
        FontData[] fontData = new FontData[numTokens];

        for (int i = 0; i < numTokens; i++) {
            try {
                fontData[i] = new FontData(tokenizer.nextToken());
            } catch (SWTException error) {
                return FONTDATA_ARRAY_DEFAULT_DEFAULT;
            } catch (IllegalArgumentException error) {
                return FONTDATA_ARRAY_DEFAULT_DEFAULT;
            }
        }
        return fontData;
    }

    /**
     * Reads the supplied string and returns its corresponding
     * FontData. If it cannot be read then the default FontData
     * will be returned.
     * 
     * @param fontDataValue the string value for the font data  
     * @return the font data
     */
    public static FontData[] readFontData(String fontDataValue) {
        return basicGetFontData(fontDataValue);
    }

    /**
     * Helper method to construct a point from the given string.
     * @param value
     * @return Point
     */
    private static Point basicGetPoint(String value) {
        Point dp = new Point(POINT_DEFAULT_DEFAULT.x, POINT_DEFAULT_DEFAULT.y);
        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value)) {
			return dp;
		}
        return StringConverter.asPoint(value, dp);
    }

    /**
     *  Helper method to construct a rectangle from the given string.
     * @param value
     * @return Rectangle
     */
    private static Rectangle basicGetRectangle(String value) {
        // We can't just return RECTANGLE_DEFAULT_DEFAULT because
        // a rectangle object doesn't have value semantik.
        Rectangle dr = new Rectangle(RECTANGLE_DEFAULT_DEFAULT.x,
                RECTANGLE_DEFAULT_DEFAULT.y, RECTANGLE_DEFAULT_DEFAULT.width,
                RECTANGLE_DEFAULT_DEFAULT.height);

        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value)) {
			return dr;
		}
        return StringConverter.asRectangle(value, dr);
    }

    /**
     * Returns the current value of the color-valued preference with the
     * given name in the given preference store.
     * Returns the default-default value (<code>COLOR_DEFAULT_DEFAULT</code>) 
     * if there is no preference with the given name, or if the current value 
     * cannot be treated as a color.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the color-valued preference
     */
    public static RGB getColor(IPreferenceStore store, String name) {
        return basicGetColor(store.getString(name));
    }

    /**
     * Returns the default value for the color-valued preference
     * with the given name in the given preference store.
     * Returns the default-default value (<code>COLOR_DEFAULT_DEFAULT</code>) 
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a color.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the default value of the preference
     */
    public static RGB getDefaultColor(IPreferenceStore store, String name) {
        return basicGetColor(store.getDefaultString(name));
    }

    /**
     * Returns the default value array for the font-valued preference
     * with the given name in the given preference store.
     * Returns the default-default value (<code>FONTDATA_ARRAY_DEFAULT_DEFAULT</code>) 
     * is no default preference with the given name, or if the default 
     * value cannot be treated as font data.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the default value of the preference
     */
    public static FontData[] getDefaultFontDataArray(IPreferenceStore store,
            String name) {
        return basicGetFontData(store.getDefaultString(name));
    }

    /**
     * Returns a single default value for the font-valued preference
     * with the given name in the given preference store.
     * Returns the default-default value (<code>FONTDATA_DEFAULT_DEFAULT</code>) 
     * is no default preference with the given name, or if the default 
     * value cannot be treated as font data.
     * This method is provided for backwards compatibility. It is
     * recommended that <code>getDefaultFontDataArray</code> is
     * used instead.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the default value of the preference
     */
    public static FontData getDefaultFontData(IPreferenceStore store,
            String name) {
        return getDefaultFontDataArray(store, name)[0];
    }

    /**
     * Returns the default value for the point-valued preference
     * with the given name in the given preference store.
     * Returns the default-default value (<code>POINT_DEFAULT_DEFAULT</code>) 
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a point.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the default value of the preference
     */
    public static Point getDefaultPoint(IPreferenceStore store, String name) {
        return basicGetPoint(store.getDefaultString(name));
    }

    /**
     * Returns the default value for the rectangle-valued preference
     * with the given name in the given preference store.
     * Returns the default-default value (<code>RECTANGLE_DEFAULT_DEFAULT</code>) 
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a rectangle.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the default value of the preference
     */
    public static Rectangle getDefaultRectangle(IPreferenceStore store,
            String name) {
        return basicGetRectangle(store.getDefaultString(name));
    }

    /**
     * Returns the current value of the font-valued preference with the
     * given name in the given preference store.
     * Returns the default-default value (<code>FONTDATA_ARRAY_DEFAULT_DEFAULT</code>) 
     * if there is no preference with the given name, or if the current value 
     * cannot be treated as font data.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the font-valued preference
     */
    public static FontData[] getFontDataArray(IPreferenceStore store,
            String name) {
        return basicGetFontData(store.getString(name));
    }

    /**
     * Returns the current value of the first entry of the
     * font-valued preference with the
     * given name in the given preference store.
     * Returns the default-default value (<code>FONTDATA_ARRAY_DEFAULT_DEFAULT</code>) 
     * if there is no preference with the given name, or if the current value 
     * cannot be treated as font data.
     * This API is provided for backwards compatibility. It is
     * recommended that <code>getFontDataArray</code> is used instead.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the font-valued preference
     */
    public static FontData getFontData(IPreferenceStore store, String name) {
        return getFontDataArray(store, name)[0];
    }

    /**
     * Returns the current value of the point-valued preference with the
     * given name in the given preference store.
     * Returns the default-default value (<code>POINT_DEFAULT_DEFAULT</code>) 
     * if there is no preference with the given name, or if the current value 
     * cannot be treated as a point.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the point-valued preference
     */
    public static Point getPoint(IPreferenceStore store, String name) {
        return basicGetPoint(store.getString(name));
    }

    /**
     * Returns the current value of the rectangle-valued preference with the
     * given name in the given preference store.
     * Returns the default-default value (<code>RECTANGLE_DEFAULT_DEFAULT</code>) 
     * if there is no preference with the given name, or if the current value 
     * cannot be treated as a rectangle.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @return the rectangle-valued preference
     */
    public static Rectangle getRectangle(IPreferenceStore store, String name) {
        return basicGetRectangle(store.getString(name));
    }

    /**
     * Sets the default value of the preference with the given name
     * in the given preference store. As FontDatas are stored as 
     * arrays this method is only provided for backwards compatibility.
     * Use <code>setDefault(IPreferenceStore, String, FontData[])</code>
     * instead.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new default value of the preference
     */
    public static void setDefault(IPreferenceStore store, String name,
            FontData value) {
        FontData[] fontDatas = new FontData[1];
        fontDatas[0] = value;
        setDefault(store, name, fontDatas);
    }

    /**
     * Sets the default value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new default value of the preference
     */
    public static void setDefault(IPreferenceStore store, String name,
            FontData[] value) {
        store.setDefault(name, getStoredRepresentation(value));
    }

    /**
     * Sets the default value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new default value of the preference
     */
    public static void setDefault(IPreferenceStore store, String name,
            Point value) {
        store.setDefault(name, StringConverter.asString(value));
    }

    /**
     * Sets the default value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new default value of the preference
     */
    public static void setDefault(IPreferenceStore store, String name,
            Rectangle value) {
        store.setDefault(name, StringConverter.asString(value));
    }

    /**
     * Sets the default value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new default value of the preference
     */
    public static void setDefault(IPreferenceStore store, String name, RGB value) {
        store.setDefault(name, StringConverter.asString(value));
    }

    /**
     * Sets the current value of the preference with the given name
     * in the given preference store. 
     * <p>
     * Included for backwards compatibility.  This method is equivalent to
     * </code>setValue(store, name, new FontData[]{value})</code>.
     * </p>
     * 
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public static void setValue(IPreferenceStore store, String name,
            FontData value) {
        setValue(store, name, new FontData[] { value });
    }

    /**
     * Sets the current value of the preference with the given name
     * in the given preference store. This method also sets the corresponding
     * key in the JFace font registry to the value and fires a 
     * property change event to listeners on the preference store.
     * 
     * <p>
     * Note that this API does not update any other settings that may
     * be dependant upon it. Only the value in the preference store 
     * and in the font registry is updated.
     * </p> 
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new current value of the preference
     * 
     * @see #putValue(IPreferenceStore, String, FontData[])
     */
    public static void setValue(IPreferenceStore store, String name,
            FontData[] value) {
        FontData[] oldValue = getFontDataArray(store, name);
        // see if the font has changed
        if (!Arrays.equals(oldValue, value)) {
            store.putValue(name, getStoredRepresentation(value));
            JFaceResources.getFontRegistry().put(name, value);
            store.firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Sets the current value of the preference with the given name
     * in the given preference store. This method does not update
     * the font registry or fire a property change event.
     * 
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new current value of the preference
     * 
     * @see PreferenceConverter#setValue(IPreferenceStore, String, FontData[])
     */
    public static void putValue(IPreferenceStore store, String name,
            FontData[] value) {
        FontData[] oldValue = getFontDataArray(store, name);
        // see if the font has changed
        if (!Arrays.equals(oldValue, value)) {
            store.putValue(name, getStoredRepresentation(value));
        }
    }

    /**
     * Returns the stored representation of the given array of FontData objects.
     * The stored representation has the form FontData;FontData;
     * Only includes the non-null entries.
     * 
     * @param fontData the array of FontData objects
     * @return the stored representation of the FontData objects
     * @since 1.0
     */
    public static String getStoredRepresentation(FontData[] fontData) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < fontData.length; i++) {
            if (fontData[i] != null) {
                buffer.append(fontData[i].toString());
                buffer.append(ENTRY_SEPARATOR);
            }
        }
        return buffer.toString();
    }

    /**
     * Sets the current value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public static void setValue(IPreferenceStore store, String name, Point value) {
        Point oldValue = getPoint(store, name);
        if (oldValue == null || !oldValue.equals(value)) {
            store.putValue(name, StringConverter.asString(value));
            store.firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Sets the current value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public static void setValue(IPreferenceStore store, String name,
            Rectangle value) {
        Rectangle oldValue = getRectangle(store, name);
        if (oldValue == null || !oldValue.equals(value)) {
            store.putValue(name, StringConverter.asString(value));
            store.firePropertyChangeEvent(name, oldValue, value);
        }
    }

    /**
     * Sets the current value of the preference with the given name
     * in the given preference store.
     *
     * @param store the preference store
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public static void setValue(IPreferenceStore store, String name, RGB value) {
        RGB oldValue = getColor(store, name);
        if (oldValue == null || !oldValue.equals(value)) {
            store.putValue(name, StringConverter.asString(value));
            store.firePropertyChangeEvent(name, oldValue, value);
        }
    }
}
