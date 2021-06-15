/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.util;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;

/**
 * <p>
 * A static class providing utility methods to all of JFace.
 * </p>
 * 
 * @since 1.0
 */
public final class Util {

	/**
	 * An unmodifiable, empty, sorted set. This value is guaranteed to never
	 * change and never be <code>null</code>.
	 */
	public static final SortedSet EMPTY_SORTED_SET = Collections
			.unmodifiableSortedSet(new TreeSet());

	/**
	 * A common zero-length string. It avoids needing write <code>NON-NLS</code>
	 * next to code fragments. It's also a bit clearer to read.
	 */
	public static final String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	/**
	 * Verifies that the given object is an instance of the given class.
	 * 
	 * @param object
	 *            The object to check; may be <code>null</code>.
	 * @param c
	 *            The class which the object should be; must not be
	 *            <code>null</code>.
	 */
	public static final void assertInstance(final Object object, final Class c) {
		assertInstance(object, c, false);
	}

	/**
	 * Verifies the given object is an instance of the given class. It is
	 * possible to specify whether the object is permitted to be
	 * <code>null</code>.
	 * 
	 * @param object
	 *            The object to check; may be <code>null</code>.
	 * @param c
	 *            The class which the object should be; must not be
	 *            <code>null</code>.
	 * @param allowNull
	 *            Whether the object is allowed to be <code>null</code>.
	 */
	private static final void assertInstance(final Object object,
			final Class c, final boolean allowNull) {
		if (object == null && allowNull) {
			return;
		}

		if (object == null || c == null) {
			throw new NullPointerException();
		} else if (!c.isInstance(object)) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Compares two boolean values. <code>false</code> is considered to be
	 * "less than" <code>true</code>.
	 * 
	 * @param left
	 *            The left value to compare
	 * @param right
	 *            The right value to compare
	 * @return <code>-1</code> if the left is <code>false</code> and the
	 *         right is <code>true</code>. <code>1</code> if the opposite
	 *         is true. If they are equal, then it returns <code>0</code>.
	 */
	public static final int compare(final boolean left, final boolean right) {
		return left == false ? (right == true ? -1 : 0) : 1;
	}

	/**
	 * Compares two integer values.
	 * 
	 * @param left
	 *            The left value to compare
	 * @param right
	 *            The right value to compare
	 * @return <code>left - right</code>
	 */
	public static final int compare(final int left, final int right) {
		return left - right;
	}

	/**
	 * Compares to comparable objects -- defending against <code>null</code>.
	 * 
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return The result of the comparison. <code>null</code> is considered
	 *         to be the least possible value.
	 */
	public static final int compare(final Comparable left,
			final Comparable right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			return left.compareTo(right);
		}
	}

	/**
	 * Compares two arrays of comparable objects -- accounting for
	 * <code>null</code>.
	 * 
	 * @param left
	 *            The left array to be compared; may be <code>null</code>.
	 * @param right
	 *            The right array to be compared; may be <code>null</code>.
	 * @return The result of the comparison. <code>null</code> is considered
	 *         to be the least possible value. A shorter array is considered
	 *         less than a longer array.
	 */
	public static final int compare(final Comparable[] left,
			final Comparable[] right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			int l = left.length;
			int r = right.length;

			if (l != r) {
				return l - r;
			}

			for (int i = 0; i < l; i++) {
				int compareTo = compare(left[i], right[i]);

				if (compareTo != 0) {
					return compareTo;
				}
			}

			return 0;
		}
	}

	/**
	 * Compares two lists -- account for <code>null</code>. The lists must
	 * contain comparable objects.
	 * 
	 * @param left
	 *            The left list to compare; may be <code>null</code>. This
	 *            list must only contain instances of <code>Comparable</code>.
	 * @param right
	 *            The right list to compare; may be <code>null</code>. This
	 *            list must only contain instances of <code>Comparable</code>.
	 * @return The result of the comparison. <code>null</code> is considered
	 *         to be the least possible value. A shorter list is considered less
	 *         than a longer list.
	 */
	public static final int compare(final List left, final List right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			int l = left.size();
			int r = right.size();

			if (l != r) {
				return l - r;
			}

			for (int i = 0; i < l; i++) {
				int compareTo = compare((Comparable) left.get(i),
						(Comparable) right.get(i));

				if (compareTo != 0) {
					return compareTo;
				}
			}

			return 0;
		}
	}

	/**
	 * Tests whether the first array ends with the second array.
	 * 
	 * @param left
	 *            The array to check (larger); may be <code>null</code>.
	 * @param right
	 *            The array that should be a subsequence (smaller); may be
	 *            <code>null</code>.
	 * @param equals
	 *            Whether the two array are allowed to be equal.
	 * @return <code>true</code> if the second array is a subsequence of the
	 *         array list, and they share end elements.
	 */
	public static final boolean endsWith(final Object[] left,
			final Object[] right, final boolean equals) {
		if (left == null || right == null) {
			return false;
		}

		int l = left.length;
		int r = right.length;

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!equals(left[l - i - 1], right[r - i - 1])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the two objects are <code>null</code> -- allowing for
	 * <code>null</code>.
	 * 
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return <code>true</code> if the two objects are equivalent;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean equals(final Object left, final Object right) {
		return left == null ? right == null : ((right != null) && left
				.equals(right));
	}

	/**
	 * Tests whether two arrays of objects are equal to each other. The arrays
	 * must not be <code>null</code>, but their elements may be
	 * <code>null</code>.
	 * 
	 * @param leftArray
	 *            The left array to compare; may be <code>null</code>, and
	 *            may be empty and may contain <code>null</code> elements.
	 * @param rightArray
	 *            The right array to compare; may be <code>null</code>, and
	 *            may be empty and may contain <code>null</code> elements.
	 * @return <code>true</code> if the arrays are equal length and the
	 *         elements at the same position are equal; <code>false</code>
	 *         otherwise.
	 */
	public static final boolean equals(final Object[] leftArray,
			final Object[] rightArray) {
		if (leftArray == rightArray) {
			return true;
		}

		if (leftArray == null) {
			return (rightArray == null);
		} else if (rightArray == null) {
			return false;
		}

		if (leftArray.length != rightArray.length) {
			return false;
		}

		for (int i = 0; i < leftArray.length; i++) {
			final Object left = leftArray[i];
			final Object right = rightArray[i];
			final boolean equal = (left == null) ? (right == null) : (left
					.equals(right));
			if (!equal) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Provides a hash code based on the given integer value.
	 * 
	 * @param i
	 *            The integer value
	 * @return <code>i</code>
	 */
	public static final int hashCode(final int i) {
		return i;
	}

	/**
	 * Provides a hash code for the object -- defending against
	 * <code>null</code>.
	 * 
	 * @param object
	 *            The object for which a hash code is required.
	 * @return <code>object.hashCode</code> or <code>0</code> if
	 *         <code>object</code> if <code>null</code>.
	 */
	public static final int hashCode(final Object object) {
		return object != null ? object.hashCode() : 0;
	}

	/**
	 * Computes the hash code for an array of objects, but with defense against
	 * <code>null</code>.
	 * 
	 * @param objects
	 *            The array of objects for which a hash code is needed; may be
	 *            <code>null</code>.
	 * @return The hash code for <code>objects</code>; or <code>0</code> if
	 *         <code>objects</code> is <code>null</code>.
	 */
	public static final int hashCode(final Object[] objects) {
		if (objects == null) {
			return 0;
		}

		int hashCode = 89;
		for (int i = 0; i < objects.length; i++) {
			final Object object = objects[i];
			if (object != null) {
				hashCode = hashCode * 31 + object.hashCode();
			}
		}

		return hashCode;
	}

	/**
	 * Checks whether the second array is a subsequence of the first array, and
	 * that they share common starting elements.
	 * 
	 * @param left
	 *            The first array to compare (large); may be <code>null</code>.
	 * @param right
	 *            The second array to compare (small); may be <code>null</code>.
	 * @param equals
	 *            Whether it is allowed for the two arrays to be equivalent.
	 * @return <code>true</code> if the first arrays starts with the second
	 *         list; <code>false</code> otherwise.
	 */
	public static final boolean startsWith(final Object[] left,
			final Object[] right, final boolean equals) {
		if (left == null || right == null) {
			return false;
		}

		int l = left.length;
		int r = right.length;

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!equals(left[i], right[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Converts an array into a string representation that is suitable for
	 * debugging.
	 * 
	 * @param array
	 *            The array to convert; may be <code>null</code>.
	 * @return The string representation of the array; never <code>null</code>.
	 */
	public static final String toString(final Object[] array) {
		if (array == null) {
			return "null"; //$NON-NLS-1$
		}

		final StringBuffer buffer = new StringBuffer();
		buffer.append('[');

		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			final Object object = array[i];
			final String element = String.valueOf(object);
			buffer.append(element);
		}
		buffer.append(']');

		return buffer.toString();
	}

	/**
	 * Provides a translation of a particular key from the resource bundle.
	 * 
	 * @param resourceBundle
	 *            The key to look up in the resource bundle; should not be
	 *            <code>null</code>.
	 * @param key
	 *            The key to look up in the resource bundle; should not be
	 *            <code>null</code>.
	 * @param defaultString
	 *            The value to return if the resource cannot be found; may be
	 *            <code>null</code>.
	 * @return The value of the translated resource at <code>key</code>. If
	 *         the key cannot be found, then it is simply the
	 *         <code>defaultString</code>.
	 */
	public static final String translateString(
			final ResourceBundle resourceBundle, final String key,
			final String defaultString) {
		if (resourceBundle != null && key != null) {
			try {
				final String translatedString = resourceBundle.getString(key);

				if (translatedString != null) {
					return translatedString;
				}
			} catch (MissingResourceException eMissingResource) {
				// Such is life. We'll return the key
			}
		}

		return defaultString;
	}
	
	/**
     * Foundation replacement for <code>String#replaceAll(String,
     * String)</code>, but <strong>without support for regular
     * expressions</strong>.
     * 
     * @param src the original string
     * @param find the string to find
     * @param replacement the replacement string
     * @return the new string, with all occurrences of <code>find</code>
     *         replaced by <code>replacement</code> (not using regular
     *         expressions)
     * @since 1.3
     */
	public static final String replaceAll(String src, String find, String replacement) {
		final int len = src.length();
		final int findLen = find.length();

		int idx = src.indexOf(find);
		if (idx < 0) {
			return src;
		}

		StringBuffer buf = new StringBuffer();
		int beginIndex = 0;
		while (idx != -1 && idx < len) {
			buf.append(src.substring(beginIndex, idx));
			buf.append(replacement);
			
			beginIndex = idx + findLen;
			if (beginIndex < len) {
				idx = src.indexOf(find, beginIndex);
			} else {
				idx = -1;
			}
		}
		if (beginIndex<len) {
			buf.append(src.substring(beginIndex, (idx==-1?len:idx)));
		}
		return buf.toString();
	}

	//
	// Methods for working with the windowing system
	//
	
	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_MOTIF = "motif";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_GTK = "gtk";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_PHOTON = "photon";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_CARBON = "carbon";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_COCOA = "cocoa";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_WPF = "wpf";//$NON-NLS-1$

	/**
	 * Windowing system constant.
	 * @since 1.3
	 */
	public static final String WS_UNKNOWN = "unknown";//$NON-NLS-1$
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for windows platforms
	 * @since 1.3
	 */
	public static final boolean isWindows() {
		final String ws = SWT.getPlatform();
		return WS_WIN32.equals(ws) || WS_WPF.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for mac platforms
	 * @since 1.3
	 */
	public static final boolean isMac() {
		final String ws = SWT.getPlatform();
		return WS_CARBON.equals(ws) || WS_COCOA.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for linux platform
	 * @since 1.3
	 */
	public static final boolean isLinux() {
		final String ws = SWT.getPlatform();
		return WS_GTK.equals(ws) || WS_MOTIF.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for gtk platforms
	 * @since 1.3
	 */
	public static final boolean isGtk() {
		final String ws = SWT.getPlatform();
		return WS_GTK.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for motif platforms
	 * @since 1.3
	 */
	public static final boolean isMotif() {
		final String ws = SWT.getPlatform();
		return WS_MOTIF.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for photon platforms
	 * @since 1.3
	 */
	public static final boolean isPhoton() {
		final String ws = SWT.getPlatform();
		return WS_PHOTON.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for carbon platforms
	 * @since 1.3
	 */
	public static final boolean isCarbon() {
		final String ws = SWT.getPlatform();
		return WS_CARBON.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for the cocoa platform.
	 * @since 1.3
	 */
	public static final boolean isCocoa() {
		final String ws = SWT.getPlatform();
		return WS_COCOA.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for WPF
	 * @since 1.3
	 */
	public static final boolean isWpf() {
		final String ws = SWT.getPlatform();
		return WS_WPF.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return <code>true</code> for win32
	 * @since 1.3
	 */
	public static final boolean isWin32() {
		final String ws = SWT.getPlatform();
		return WS_WIN32.equals(ws);
	}
	
	/**
	 * Common WS query helper method. 
	 * @return the SWT windowing platform string.
	 * @see SWT#getPlatform()
	 * @since 1.3
	 */
	public static final String getWS() {
		return SWT.getPlatform();
	}

	/**
	 * This class should never be constructed.
	 */
	private Util() {
		// Not allowed.
	}
}
