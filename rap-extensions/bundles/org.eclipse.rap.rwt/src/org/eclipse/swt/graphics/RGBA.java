/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.SerializableCompatibility;

/**
 * Instances of this class are descriptions of colors in
 * terms of the primary additive color model (red, green, blue
 * and alpha). A color may be described in terms of the relative
 * intensities of these three primary colors. The brightness
 * of each color is specified by a value in the range 0 to 255,
 * where 0 indicates no color (blackness) and 255 indicates
 * maximum intensity and for alpha 0 indicates transparent and
 * 255 indicates opaque.
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
 * @see Color
 *
 * @since 3.1
 */
public final class RGBA implements SerializableCompatibility {

  /**
   * the RGB component of the RGBA
   */
  public final RGB rgb;

  /**
   * the alpha component of the RGBA
   */
  public int alpha;

  /**
   * Constructs an instance of this class with the given
   * red, green, blue and alpha values.
   *
   * @param red the red component of the new instance
   * @param green the green component of the new instance
   * @param blue the blue component of the new instance
   * @param alpha the alpha component of the new instance
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha argument is not between 0 and 255</li>
   * </ul>
   */
  public RGBA( int red, int green, int blue, int alpha ) {
    if( alpha > 255 || alpha < 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    this.rgb = new RGB( red, green, blue );
    this.alpha = alpha;
  }

  /**
  * Constructs an instance of this class with the given
  * hue, saturation, and brightness.
  *
  * @param hue the hue value for the HSBA color (from 0 to 360)
  * @param saturation the saturation value for the HSBA color (from 0 to 1)
  * @param brightness the brightness value for the HSBA color (from 0 to 1)
  * @param alpha the alpha value for the HSBA color (from 0 to 255)
  *
  * @exception IllegalArgumentException <ul>
  *    <li>ERROR_INVALID_ARGUMENT - if the hue is not between 0 and 360 or
  *    the saturation or brightness is not between 0 and 1 or if the alpha
  *    is not between 0 and 255</li>
  * </ul>
  *
  */
  public RGBA( float hue, float saturation, float brightness, float alpha ) {
    if( alpha > 255 || alpha < 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    rgb = new RGB( hue, saturation, brightness );
    this.alpha = ( int )( alpha + 0.5 );
  }

  /**
   * Returns the hue, saturation, and brightness of the color.
   *
   * @return color space values in float format:<ul>
   *             <li>hue (from 0 to 360)</li>
   *             <li>saturation (from 0 to 1)</li>
   *             <li>brightness (from 0 to 1)</li>
   *             <li>alpha (from 0 to 255)</li>
   *             </ul>
   * @see #RGBA(float, float, float, float)
   */
  public float[] getHSBA() {
    float[] hsb = rgb.getHSB();
    return new float[] {
      hsb[ 0 ],
      hsb[ 1 ],
      hsb[ 2 ],
      alpha
    };
  }

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
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( !( object instanceof RGBA ) ) {
      return false;
    }
    RGBA rgba = ( RGBA )object;
    return    ( rgba.rgb.red == this.rgb.red )
           && ( rgba.rgb.green == this.rgb.green )
           && ( rgba.rgb.blue == this.rgb.blue )
           && ( rgba.alpha == this.alpha );
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
  public int hashCode() {
    return ( alpha << 32 ) | ( rgb.blue << 16 ) | ( rgb.green << 8 ) | rgb.red;
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the <code>RGBA</code>
   */
  @Override
  public String toString() {
    return "RGBA {" + rgb.red + ", " + rgb.green + ", " + rgb.blue + ", " + alpha + "}";
  }

}
