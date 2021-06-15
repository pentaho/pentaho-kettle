/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.graphics.ColorUtil;


/**
 * Instances of this class manage resources that implement SWT's RGB color
 * model.
 *
 * @see RGB
 * @see Device#getSystemColor
 *
 * @since 1.0
 */
public class Color extends Resource {

  /**
   * Holds the color values within one integer.
   */
  private int colorNr;

  /**
   * Prevents uninitialized instances from being created outside the package.
   */
  private Color( int colorNr ) {
    super( null );
    this.colorNr = colorNr;
  }

  /**
   * Constructs a new instance of this class given a device and an
   * <code>RGB</code> describing the desired red, green and blue values.
   * On limited color devices, the color instance created by this call
   * may not have the same RGB values as the ones specified by the
   * argument. The RGB values on the returned instance will be the color
   * values of the operating system color.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param rgb the RGB values of the desired color
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue components of the argument are not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 1.3
   */
  public Color( Device device, RGB rgb ) {
    this( device, rgb, 255 );
  }

  /**
   * Constructs a new instance of this class given a device, an
   * <code>RGB</code> describing the desired red, green and blue values,
   * alpha specifying the level of transparency.
   * On limited color devices, the color instance created by this call
   * may not have the same RGB values as the ones specified by the
   * argument. The RGB values on the returned instance will be the color
   * values of the operating system color.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param rgb the RGB values of the desired color
   * @param alpha the alpha value of the desired color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha components of the argument are not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 3.1
   */
  public Color(Device device, RGB rgb, int alpha) {
    super( checkDevice( device ) );
    if( rgb == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    colorNr = ColorUtil.computeColorNr( rgb.red, rgb.green, rgb.blue, alpha );
  }

  /**
   * Constructs a new instance of this class given a device and an
   * <code>RGBA</code> describing the desired red, green, blue & alpha values.
   * On limited color devices, the color instance created by this call
   * may not have the same RGBA values as the ones specified by the
   * argument. The RGBA values on the returned instance will be the color
   * values of the operating system color + alpha.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param rgba the RGBA values of the desired color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the rgba argument is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha components of the argument are not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 3.1
   */
  public Color( Device device, RGBA rgba ) {
    super( checkDevice( device ) );
    if( rgba == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    colorNr = ColorUtil.computeColorNr( rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha );
  }

  /**
   * Constructs a new instance of this class given a device and the
   * desired red, green and blue values expressed as ints in the range
   * 0 to 255 (where 0 is black and 255 is full brightness). On limited
   * color devices, the color instance created by this call may not have
   * the same RGB values as the ones specified by the arguments. The
   * RGB values on the returned instance will be the color values of
   * the operating system color.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param red the amount of red in the color
   * @param green the amount of green in the color
   * @param blue the amount of blue in the color
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green or blue argument is not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 1.3
   */
  public Color( Device device, int red, int green, int blue ) {
    this( device, red, green, blue, 255 );
  }

  /**
   * Constructs a new instance of this class given a device and the
   * desired red, green, blue & alpha values expressed as ints in the range
   * 0 to 255 (where 0 is black and 255 is full brightness). On limited
   * color devices, the color instance created by this call may not have
   * the same RGB values as the ones specified by the arguments. The
   * RGB values on the returned instance will be the color values of
   * the operating system color.
   * <p>
   * You must dispose the color when it is no longer required.
   * </p>
   *
   * @param device the device on which to allocate the color
   * @param red the amount of red in the color
   * @param green the amount of green in the color
   * @param blue the amount of blue in the color
   * @param alpha the amount of alpha in the color. Currently, SWT only honors extreme values for alpha i.e. 0 (transparent) or 255 (opaque).
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the red, green, blue or alpha argument is not between 0 and 255</li>
   * </ul>
   *
   * @see #dispose
   * @since 3.1
   */
  public Color (Device device, int red, int green, int blue, int alpha) {
    super( checkDevice( device ) );
    colorNr = ColorUtil.computeColorNr( red, green, blue, alpha );
  }

  /**
   * Returns the amount of blue in the color, from 0 to 255.
   *
   * @return the blue component of the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public int getBlue() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return ( colorNr & 0xFF0000 ) >> 16;
  }

  /**
   * Returns the amount of green in the color, from 0 to 255.
   *
   * @return the green component of the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public int getGreen() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return ( colorNr & 0xFF00 ) >> 8;
  }

  /**
   * Returns the amount of red in the color, from 0 to 255.
   *
   * @return the red component of the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public int getRed() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return colorNr & 0xFF;
  }

  /**
   * Returns the amount of alpha in the color, from 0 (transparent) to 255 (opaque).
   *
   * @return the alpha component of the color
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 3.1
   */
  public int getAlpha() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return colorNr >>> 24;
  }

  /**
   * Returns an <code>RGB</code> representing the receiver.
   *
   * @return the RGB for the color
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
   *              disposed</li>
   *              </ul>
   */
  public RGB getRGB() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return new RGB( getRed(), getGreen(), getBlue() );
  }

  /**
   * Returns an <code>RGBA</code> representing the receiver.
   *
   * @return the RGBA for the color
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 3.1
   */
  public RGBA getRGBA() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    return new RGBA( getRed(), getGreen(), getBlue(), getAlpha() );
  }

  /**
   * Compares the argument to the receiver, and returns true if they represent
   * the <em>same</em> object using a class specific comparison.
   *
   * @param object the object to compare with this object
   * @return <code>true</code> if the object is the same as this object and
   *         <code>false</code> otherwise
   * @see #hashCode
   */
  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( !( object instanceof Color ) ) {
      return false;
    }
    Color color = ( Color )object;
    return ( colorNr & 0xFFFFFFFF ) == ( color.colorNr & 0xFFFFFFFF );
  }

  /**
   * Returns an integer hash code for the receiver. Any two objects that return
   * <code>true</code> when passed to <code>equals</code> must return the
   * same value for this method.
   *
   * @return the receiver's hash
   * @see #equals
   */
  @Override
  public int hashCode() {
    return getRGBA().hashCode();
  }

  /**
   * Returns a string containing a concise, human-readable description of the
   * receiver.
   *
   * @return a string representation of the receiver
   */
  @Override
  public String toString() {
    if( isDisposed() ) {
      return "Color {*DISPOSED*}";
    }
    return "Color {" + getRed() + ", " + getGreen() + ", " + getBlue() + ", " + getAlpha() + "}";
  }

}
