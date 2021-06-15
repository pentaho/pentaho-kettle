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
package org.eclipse.jface.resource;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Helper class for converting various data types to and from
 * strings. Supported types include:
 * <ul>
 *   <li><code>boolean</code></li>
 *   <li><code>int</code></li>
 *   <li><code>long</code></li>
 *   <li><code>float</code></li>
 *   <li><code>double</code></li>
 *   <li><code>org.eclipse.swt.graphics.Point</code></li>
 *   <li><code>org.eclipse.swt.graphics.Rectangle</code></li>
 *   <li><code>org.eclipse.swt.graphics.RGB</code></li>
 *   <li><code>org.eclipse.swt.graphics.FontData</code></li>
 * </ul>
 * <p>
 * All methods declared on this class are static. This
 * class cannot be instantiated.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public class StringConverter {

    /**
     * Internal font style constant for regular fonts.
     */
    private static final String REGULAR = "regular"; //$NON-NLS-1$

    /**
     * Internal font style constant for bold fonts.
     */
    private static final String BOLD = "bold"; //$NON-NLS-1$

    /**
     * Internal font style constant for italic fonts.
     */
    private static final String ITALIC = "italic"; //$NON-NLS-1$

    /**
     * Internal font style constant for bold italic fonts.
     */
    private static final String BOLD_ITALIC = "bold italic"; //$NON-NLS-1$

    /**
     * Internal constant for the separator character used in
     * font specifications.
     */
    private static final char SEPARATOR = '-';

    /**
     * Internal constant for the seperator character used in font list
     * specifications.
     */
    private static final String FONT_SEPARATOR = ";"; //$NON-NLS-1$

    /* (non-Javadoc)
     * Declare a private constructor to block instantiation.
     */
    private StringConverter() {
        //no-op
    }

    /**
     * Breaks out space-separated words into an array of words.
     * For example: <code>"no comment"</code> into an array 
     * <code>a[0]="no"</code> and <code>a[1]= "comment"</code>.
     *
     * @param value the string to be converted
     * @return the list of words
     * @throws DataFormatException thrown if request string could not seperated
     */
    public static String[] asArray(String value) throws DataFormatException {
        ArrayList list = new ArrayList();
        StringTokenizer stok = new StringTokenizer(value);
        while (stok.hasMoreTokens()) {
            list.add(stok.nextToken());
        }
        String result[] = new String[list.size()];
        list.toArray(result);
        return result;
    }

    /**
     /**
     * Breaks out space-separated words into an array of words.
     * For example: <code>"no comment"</code> into an array 
     * <code>a[0]="no"</code> and <code>a[1]= "comment"</code>.
     * Returns the given default value if the value cannot be parsed.
     *
     * @param value the string to be converted
     * @param dflt the default value
     * @return the list of words, or the default value
     */
    public static String[] asArray(String value, String[] dflt) {
        try {
            return asArray(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into a boolean.
     * This method fails if the value does not represent a boolean.
     * <p>
     * Valid representations of <code>true</code> include the strings
     * "<code>t</code>", "<code>true</code>", or equivalent in mixed
     * or upper case.
     * Similarly, valid representations of <code>false</code> include the strings
     * "<code>f</code>", "<code>false</code>", or equivalent in mixed
     * or upper case. 
     * </p>
     *
     * @param value the value to be converted
     * @return the value as a boolean
     * @exception DataFormatException if the given value does not represent
     *	a boolean
     */
    public static boolean asBoolean(String value) throws DataFormatException {
        String v = value.toLowerCase();
        if (v.equals("t") || v.equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
        if (value.equals("f") || v.equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
        throw new DataFormatException(
                "Value " + value + "doesn't represent a boolean"); //$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Converts the given value into a boolean.
     * Returns the given default value if the 
     * value does not represent a boolean.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a boolean, or the default value
     */
    public static boolean asBoolean(String value, boolean dflt) {
        try {
            return asBoolean(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into a double.
     * This method fails if the value does not represent a double.
     *
     * @param value the value to be converted
     * @return the value as a double
     * @exception DataFormatException if the given value does not represent
     *	a double
     */
    public static double asDouble(String value) throws DataFormatException {
        try {
            return (Double.valueOf(value)).doubleValue();
        } catch (NumberFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    /**
     * Converts the given value into a double.
     * Returns the given default value if the 
     * value does not represent a double.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a double, or the default value
     */
    public static double asDouble(String value, double dflt) {
        try {
            return asDouble(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into a float.
     * This method fails if the value does not represent a float.
     *
     * @param value the value to be converted
     * @return the value as a float
     * @exception DataFormatException if the given value does not represent
     *	a float
     */
    public static float asFloat(String value) throws DataFormatException {
        try {
            return (Float.valueOf(value)).floatValue();
        } catch (NumberFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    /**
     * Converts the given value into a float.
     * Returns the given default value if the 
     * value does not represent a float.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a float, or the default value
     */
    public static float asFloat(String value, float dflt) {
        try {
            return asFloat(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into an SWT font data object.
     * This method fails if the value does not represent font data.
     * <p>
     * A valid font data representation is a string of the form
     * <code><it>fontname</it>-<it>style</it>-<it>height</it></code> where
     * <code><it>fontname</it></code> is the name of a font,
     * <code><it>style</it></code> is a font style (one of
     * <code>"regular"</code>, <code>"bold"</code>,
     * <code>"italic"</code>, or <code>"bold italic"</code>)
     * and <code><it>height</it></code> is an integer representing the
     * font height. Example: <code>Times New Roman-bold-36</code>.
     * </p>
     *
     * @param value the value to be converted
     * @return the value as font data
     * @exception DataFormatException if the given value does not represent
     *	font data
     */
    public static FontData asFontData(String value) throws DataFormatException {
        if (value == null) {
			throw new DataFormatException(
                    "Null doesn't represent a valid font data"); //$NON-NLS-1$
		}
        String name = null;
        int height = 0;
        int style = 0;
        try {
            int length = value.length();
            int heightIndex = value.lastIndexOf(SEPARATOR);
            if (heightIndex == -1) {
				throw new DataFormatException(
                        "No correct font data format \"" + value + "\""); //$NON-NLS-2$//$NON-NLS-1$
			}
            height = StringConverter.asInt(value.substring(heightIndex + 1,
                    length));
            int faceIndex = value.lastIndexOf(SEPARATOR, heightIndex - 1);
            if (faceIndex == -1) {
				throw new DataFormatException(
                        "No correct font data format \"" + value + "\""); //$NON-NLS-2$//$NON-NLS-1$
			}
            String s = value.substring(faceIndex + 1, heightIndex);
            if (BOLD_ITALIC.equals(s)) {
                style = SWT.BOLD | SWT.ITALIC;
            } else if (BOLD.equals(s)) {
                style = SWT.BOLD;
            } else if (ITALIC.equals(s)) {
                style = SWT.ITALIC;
            } else if (REGULAR.equals(s)) {
                style = SWT.NORMAL;
            } else {
                throw new DataFormatException("Unknown face name \"" + s + "\""); //$NON-NLS-2$//$NON-NLS-1$
            }
            name = value.substring(0, faceIndex);
        } catch (NoSuchElementException e) {
            throw new DataFormatException(e.getMessage());
        }
        return new FontData(name, height, style);
    }

	/**
	 * Returns the result of converting a list of comma-separated tokens into an array
	 * 
	 * @return the array of string tokens
	 * @param prop the initial comma-separated string
	 */
	private static String[] getArrayFromList(String prop, String separator) {
		if (prop == null || prop.trim().equals("")) { //$NON-NLS-1$
			return new String[0];
		}
		ArrayList list = new ArrayList();
		StringTokenizer tokens = new StringTokenizer(prop, separator); 
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) { //$NON-NLS-1$
				list.add(token);
			}
		}
		return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[list.size()]);
	}

    /**
     * Convert the given value into an array of SWT font data objects.
     * 
     * @param value the font list string 
     * @return the value as a font list
     */
    public static FontData[] asFontDataArray(String value) {
        String[] strings = getArrayFromList(value, FONT_SEPARATOR);
        ArrayList data = new ArrayList(strings.length);
        for (int i = 0; i < strings.length; i++) {
            try {
                data.add(StringConverter.asFontData(strings[i]));
            } catch (DataFormatException e) {
                //do-nothing
            }
        }
        return (FontData[]) data.toArray(new FontData[data.size()]);
    }

    /**
     * Converts the given value into an SWT font data object.
     * Returns the given default value if the 
     * value does not represent a font data object.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a font data object, or the default value
     */
    public static FontData asFontData(String value, FontData dflt) {
        try {
            return asFontData(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into an int.
     * This method fails if the value does not represent an int.
     *
     * @param value the value to be converted
     * @return the value as an int
     * @exception DataFormatException if the given value does not represent
     *	an int
     */
    public static int asInt(String value) throws DataFormatException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    /**
     * Converts the given value into an int.
     * Returns the given default value if the 
     * value does not represent an int.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as an int, or the default value
     */
    public static int asInt(String value, int dflt) {
        try {
            return asInt(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into a long.
     * This method fails if the value does not represent a long.
     *
     * @param value the value to be converted
     * @return the value as a long
     * @exception DataFormatException if the given value does not represent
     *	a long
     */
    public static long asLong(String value) throws DataFormatException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    /**
     * Converts the given value into a long.
     * Returns the given default value if the 
     * value does not represent a long.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a long, or the default value
     */
    public static long asLong(String value, long dflt) {
        try {
            return asLong(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into an SWT point.
     * This method fails if the value does not represent a point.
     * <p>
     * A valid point representation is a string of the form
     * <code><it>x</it>,<it>y</it></code> where
     * <code><it>x</it></code> and <code><it>y</it></code>
     * are valid ints.
     * </p>
     *
     * @param value the value to be converted
     * @return the value as a point
     * @exception DataFormatException if the given value does not represent
     *	a point
     */
    public static Point asPoint(String value) throws DataFormatException {
        if (value == null) {
			throw new DataFormatException(
                    "Null doesn't represent a valid point"); //$NON-NLS-1$
		}
        StringTokenizer stok = new StringTokenizer(value, ","); //$NON-NLS-1$
        String x = stok.nextToken();
        String y = stok.nextToken();
        int xval = 0, yval = 0;
        try {
            xval = Integer.parseInt(x);
            yval = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
        return new Point(xval, yval);
    }

    /**
     * Converts the given value into an SWT point.
     * Returns the given default value if the 
     * value does not represent a point.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a point, or the default value
     */
    public static Point asPoint(String value, Point dflt) {
        try {
            return asPoint(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into an SWT rectangle.
     * This method fails if the value does not represent a rectangle.
     * <p>
     * A valid rectangle representation is a string of the form
     * <code><it>x</it>,<it>y</it>,<it>width</it>,<it>height</it></code>
     * where <code><it>x</it></code>, <code><it>y</it></code>,
     * <code><it>width</it></code>, and <code><it>height</it></code>
     * are valid ints.
     * </p>
     *
     * @param value the value to be converted
     * @return the value as a rectangle
     * @exception DataFormatException if the given value does not represent
     *	a rectangle
     */
    public static Rectangle asRectangle(String value)
            throws DataFormatException {
        if (value == null) {
			throw new DataFormatException(
                    "Null doesn't represent a valid rectangle"); //$NON-NLS-1$
		}
        StringTokenizer stok = new StringTokenizer(value, ","); //$NON-NLS-1$
        String x = stok.nextToken();
        String y = stok.nextToken();
        String width = stok.nextToken();
        String height = stok.nextToken();
        int xval = 0, yval = 0, wval = 0, hval = 0;
        try {
            xval = Integer.parseInt(x);
            yval = Integer.parseInt(y);
            wval = Integer.parseInt(width);
            hval = Integer.parseInt(height);
        } catch (NumberFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
        return new Rectangle(xval, yval, wval, hval);
    }

    /**
     * Converts the given value into an SWT rectangle.
     * Returns the given default value if the 
     * value does not represent a rectangle.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a rectangle, or the default value
     */
    public static Rectangle asRectangle(String value, Rectangle dflt) {
        try {
            return asRectangle(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given value into an SWT RGB color value.
     * This method fails if the value does not represent an RGB
     * color value.
     * <p>
     * A valid RGB color value representation is a string of the form
     * <code><it>red</it>,<it>green</it></code>,<it>blue</it></code> where
     * <code><it>red</it></code>, <it>green</it></code>, and 
     * <code><it>blue</it></code> are valid ints.
     * </p>
     *
     * @param value the value to be converted
     * @return the value as an RGB color value
     * @exception DataFormatException if the given value does not represent
     *	an RGB color value
     */
    public static RGB asRGB(String value) throws DataFormatException {
        if (value == null) {
			throw new DataFormatException("Null doesn't represent a valid RGB"); //$NON-NLS-1$
		}
        StringTokenizer stok = new StringTokenizer(value, ","); //$NON-NLS-1$

        try {
            String red = stok.nextToken().trim();
            String green = stok.nextToken().trim();
            String blue = stok.nextToken().trim();
            int rval = 0, gval = 0, bval = 0;
            try {
                rval = Integer.parseInt(red);
                gval = Integer.parseInt(green);
                bval = Integer.parseInt(blue);
            } catch (NumberFormatException e) {
                throw new DataFormatException(e.getMessage());
            }
            return new RGB(rval, gval, bval);
        } catch (NoSuchElementException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    /**
     * Converts the given value into an SWT RGB color value.
     * Returns the given default value if the 
     * value does not represent an RGB color value.
     *
     * @param value the value to be converted
     * @param dflt the default value
     * @return the value as a RGB color value, or the default value
     */
    public static RGB asRGB(String value, RGB dflt) {
        try {
            return asRGB(value);
        } catch (DataFormatException e) {
            return dflt;
        }
    }

    /**
     * Converts the given double value to a string.
     * Equivalent to <code>String.valueOf(value)</code>.
     *
     * @param value the double value
     * @return the string representing the given double
     */
    public static String asString(double value) {
        return String.valueOf(value);
    }

    /**
     * Converts the given float value to a string.
     * Equivalent to <code>String.valueOf(value)</code>.
     *
     * @param value the float value
     * @return the string representing the given float
     */
    public static String asString(float value) {
        return String.valueOf(value);
    }

    /**
     * Converts the given int value to a string.
     * Equivalent to <code>String.valueOf(value)</code>.
     *
     * @param value the int value
     * @return the string representing the given int
     */
    public static String asString(int value) {
        return String.valueOf(value);
    }

    /**
     * Converts the given long value to a string.
     * Equivalent to <code>String.valueOf(value)</code>.
     *
     * @param value the long value
     * @return the string representing the given long
     */
    public static String asString(long value) {
        return String.valueOf(value);
    }

    /**
     * Converts the given boolean object to a string.
     * Equivalent to <code>String.valueOf(value.booleanValue())</code>.
     *
     * @param value the boolean object
     * @return the string representing the given boolean value
     */
    public static String asString(Boolean value) {
        Assert.isNotNull(value);
        return String.valueOf(value.booleanValue());
    }

    /**
     * Converts the given double object to a string.
     * Equivalent to <code>String.valueOf(value.doubleValue())</code>.
     *
     * @param value the double object
     * @return the string representing the given double value
     */
    public static String asString(Double value) {
        Assert.isNotNull(value);
        return String.valueOf(value.doubleValue());
    }

    /**
     * Converts the given float object to a string.
     * Equivalent to <code>String.valueOf(value.floatValue())</code>.
     *
     * @param value the float object
     * @return the string representing the given float value
     */
    public static String asString(Float value) {
        Assert.isNotNull(value);
        return String.valueOf(value.floatValue());
    }

    /**
     * Converts the given integer object to a string.
     * Equivalent to <code>String.valueOf(value.intValue())</code>.
     *
     * @param value the integer object
     * @return the string representing the given integer value
     */
    public static String asString(Integer value) {
        Assert.isNotNull(value);
        return String.valueOf(value.intValue());
    }

    /**
     * Converts the given long object to a string.
     * Equivalent to <code>String.valueOf(value.longValue())</code>.
     *
     * @param value the long object
     * @return the string representing the given long value
     */
    public static String asString(Long value) {
        Assert.isNotNull(value);
        return String.valueOf(value.longValue());
    }

    /**
     * Converts a font data array  to a string. The string representation is
     * that of asString(FontData) seperated by ';'
     * 
     * @param value The font data.
     * @return The string representation of the font data arra.
     */
    public static String asString(FontData[] value) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < value.length; i++) {
            buffer.append(asString(value[i]));
            if (i != value.length - 1) {
				buffer.append(FONT_SEPARATOR);
			}
        }
        return buffer.toString();
    }

    /**
     * Converts a font data object to a string. The string representation is
     * "font name-style-height" (for example "Times New Roman-bold-36").
     * @param value The font data.
     * @return The string representation of the font data object.
     */
    public static String asString(FontData value) {
        Assert.isNotNull(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append(value.getName());
        buffer.append(SEPARATOR);
        int style = value.getStyle();
        boolean bold = (style & SWT.BOLD) == SWT.BOLD;
        boolean italic = (style & SWT.ITALIC) == SWT.ITALIC;
        if (bold && italic) {
            buffer.append(BOLD_ITALIC);
        } else if (bold) {
            buffer.append(BOLD);
        } else if (italic) {
            buffer.append(ITALIC);
        } else {
            buffer.append(REGULAR);
        }

        buffer.append(SEPARATOR);
        buffer.append(value.getHeight());
        return buffer.toString();
    }

    /**
     * Converts the given SWT point object to a string.
     * <p>
     * The string representation of a point has the form
     * <code><it>x</it>,<it>y</it></code> where
     * <code><it>x</it></code> and <code><it>y</it></code>
     * are string representations of integers.
     * </p>
     *
     * @param value the point object
     * @return the string representing the given point
     */
    public static String asString(Point value) {
        Assert.isNotNull(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append(value.x);
        buffer.append(',');
        buffer.append(value.y);
        return buffer.toString();
    }

    /**
     * Converts the given SWT rectangle object to a string.
     * <p>
     * The string representation of a rectangle has the form
     * <code><it>x</it>,<it>y</it>,<it>width</it>,<it>height</it></code>
     * where <code><it>x</it></code>, <code><it>y</it></code>,
     * <code><it>width</it></code>, and <code><it>height</it></code>
     * are string representations of integers.
     * </p>
     *
     * @param value the rectangle object
     * @return the string representing the given rectangle
     */
    public static String asString(Rectangle value) {
        Assert.isNotNull(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append(value.x);
        buffer.append(',');
        buffer.append(value.y);
        buffer.append(',');
        buffer.append(value.width);
        buffer.append(',');
        buffer.append(value.height);
        return buffer.toString();
    }

    /**
     * Converts the given SWT RGB color value object to a string.
     * <p>
     * The string representation of an RGB color value has the form
     * <code><it>red</it>,<it>green</it></code>,<it>blue</it></code> where
     * <code><it>red</it></code>, <it>green</it></code>, and 
     * <code><it>blue</it></code> are string representations of integers.
     * </p>
     *
     * @param value the RGB color value object
     * @return the string representing the given RGB color value
     */
    public static String asString(RGB value) {
        Assert.isNotNull(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append(value.red);
        buffer.append(',');
        buffer.append(value.green);
        buffer.append(',');
        buffer.append(value.blue);
        return buffer.toString();
    }

    /**
     * Converts the given boolean value to a string.
     * Equivalent to <code>String.valueOf(value)</code>.
     *
     * @param value the boolean value
     * @return the string representing the given boolean
     */
    public static String asString(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Returns the given string with all whitespace characters removed.
     * <p>
     * All characters that have codes less than or equal to <code>'&#92;u0020'</code> 
     * (the space character) are considered to be a white space.
     * </p>
     *
     * @param s the source string
     * @return the string with all whitespace characters removed
     */
    public static String removeWhiteSpaces(String s) {
        //check for no whitespace (common case)
        boolean found = false;
        int wsIndex = -1;
        int size = s.length();
        for (int i = 0; i < size; i++) {
            found = Character.isWhitespace(s.charAt(i));
            if (found) {
                wsIndex = i;
                break;
            }
        }
        if (!found) {
			return s;
		}

        StringBuffer result = new StringBuffer(s.substring(0, wsIndex));
        for (int i = wsIndex + 1; i < size; i++) {
            char ch = s.charAt(i);
            if (!Character.isWhitespace(ch)) {
				result.append(ch);
			}
        }
        return result.toString();
    }

    /**
     * Converts a font data object to a string representation for display. 
     * 	The string representation is
     * "font name-style-height" (for example "Times New Roman-bold-36").
     * @param value The font data.
     * @return The string representation of the font data object.
     * @deprecated use asString(FontData)
     */
    public static String asDisplayableString(FontData value) {
        Assert.isNotNull(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append(value.getName());
        buffer.append(SEPARATOR);
        int style = value.getStyle();
        boolean bold = (style & SWT.BOLD) == SWT.BOLD;
        boolean italic = (style & SWT.ITALIC) == SWT.ITALIC;
        if (bold && italic) {
            buffer.append(JFaceResources.getString("BoldItalicFont")); //$NON-NLS-1$
        } else if (bold) {
            buffer.append(JFaceResources.getString("BoldFont")); //$NON-NLS-1$
        } else if (italic) {
            buffer.append(JFaceResources.getString("ItalicFont")); //$NON-NLS-1$
        } else {
            buffer.append(JFaceResources.getString("RegularFont")); //$NON-NLS-1$
        }
        buffer.append(SEPARATOR);
        buffer.append(value.getHeight());
        return buffer.toString();

    }
}
