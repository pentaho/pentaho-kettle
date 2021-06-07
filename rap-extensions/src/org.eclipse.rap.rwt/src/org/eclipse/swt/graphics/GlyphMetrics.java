/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
//import org.eclipse.swt.internal.*;

/**
 * Instances of this class represent glyph metrics.
 * <p>
 * The hashCode() method in this class uses the values of the public
 * fields to compute the hash value. When storing instances of the
 * class in hashed collections, do not modify these fields after the
 * object has been inserted.
 * </p>
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see TextStyle
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public final class GlyphMetrics {

  /**
   * the ascent of the GlyphMetrics
   */
  public int ascent;

  /**
   * the descent of the GlyphMetrics
   */
  public int descent;

  /**
   * the width of the GlyphMetrics
   */
  public int width;

  /**
   * Constructs an instance of this class with the given
   * ascent, descent and width values.
   *
   * @param ascent the GlyphMetrics ascent
   * @param descent the GlyphMetrics descent
   * @param width the GlyphMetrics width
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the ascent, descent or width argument is negative</li>
   * </ul>
   */
  public GlyphMetrics(int ascent, int descent, int width) {
    if (ascent < 0 || descent < 0 || width < 0) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    this.ascent = ascent;
    this.descent = descent;
    this.width = width;
  }

  //int getAscentInPixels() {
  //	return DPIUtil.autoScaleUp(ascent);
  //}
  //
  //int getDescentInPixels() {
  //	return DPIUtil.autoScaleUp(descent);
  //}
  //
  //int getWidthInPixels() {
  //	return DPIUtil.autoScaleUp(width);
  //}

  /**
   * Compares the argument to the receiver, and returns true
   * if they represent the <em>same</em> object using a class
   * specific comparison.
   *
   * @param object the object to compare with this object
   * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
   *
   * @see #hashCode()
   */
  @Override
  public boolean equals (Object object) {
    if (object == this) return true;
    if (!(object instanceof GlyphMetrics)) return false;
    GlyphMetrics metrics = (GlyphMetrics)object;
    return metrics.ascent == ascent && metrics.descent == descent && metrics.width == width;
  }

  /**
   * Returns an integer hash code for the receiver. Any two
   * objects that return <code>true</code> when passed to
   * <code>equals</code> must return the same value for this
   * method.
   *
   * @return the receiver's hash
   *
   * @see #equals(Object)
   */
  @Override
  public int hashCode () {
    return ascent ^ descent ^ width;
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the <code>GlyphMetrics</code>
   */
  @Override
  public String toString () {
    return "GlyphMetrics {" + ascent + ", " + descent + ", " + width + "}"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

}
