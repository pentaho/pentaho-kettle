/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.io.InputStream;

import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


/**
 * Before RAP 2.0, this class was used to create resources like fonts and colors without a display
 * reference. This practice is obsolete, resources should always be created using a constructor.
 * <p>
 * The class has also been used as a helper class for measuring texts. This can also be done using a
 * GC.
 * </p>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated This class will be removed in future versions. For replacements, see the
 *             documentation of the single methods.
 */
@Deprecated
public final class Graphics {

  /**
   * Returns an instance of {@link Color} given an <code>RGB</code> describing the desired red,
   * green and blue values.
   * <p>
   * Note: it is considered an error to attempt to dispose of colors that were created by this
   * method. An <code>IllegalStateException</code> is thrown in this case.
   * </p>
   *
   * @param rgb the RGB values of the desired color - must not be null
   * @return the color
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the rgb argument is null</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @see RGB
   * @see Device#getSystemColor
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Color(Device, RGB)</code> instead.
   */
  @Deprecated
  public static Color getColor( RGB rgb ) {
    checkThread();
    if( rgb == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    ResourceFactory resourceFactory = getApplicationContext().getResourceFactory();
    return resourceFactory.getColor( rgb.red, rgb.green, rgb.blue );
  }

  /**
   * Returns a {@link Color} given the desired red, green and blue values expressed as ints in the
   * range 0 to 255 (where 0 is black and 255 is full brightness).
   * <p>
   * Note: it is considered an error to attempt to dispose of colors that were created by this
   * method. An <code>IllegalStateException</code> is thrown in this case.
   * </p>
   *
   * @param red the amount of red in the color
   * @param green the amount of green in the color
   * @param blue the amount of blue in the color
   * @return the color
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Color(Device, int, int, int)</code> instead.
   */
  @Deprecated
  public static Color getColor( int red, int green, int blue ) {
    checkThread();
    ResourceFactory resourceFactory = getApplicationContext().getResourceFactory();
    return resourceFactory.getColor( red, green, blue );
  }

  /**
   * Returns a new font given a font data which describes the desired font's appearance.
   * <p>
   * Note: it is considered an error to attempt to dispose of fonts that were created by this
   * method. An <code>IllegalStateException</code> is thrown in this case.
   * </p>
   *
   * @param data the {@link FontData} to use - must not be null
   * @return the font
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the data argument is null</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Font(Device, FontData)</code> instead.
   */
  @Deprecated
  public static Font getFont( FontData data ) {
    checkThread();
    if( data == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    ResourceFactory resourceFactory = getApplicationContext().getResourceFactory();
    return resourceFactory.getFont( data );
  }

  /**
   * Returns a {@link Font} object given a font name, the height of the desired font in points, and
   * a font style.
   * <p>
   * Note: it is considered an error to attempt to dispose of fonts that were created by this
   * method. An <code>IllegalStateException</code> is thrown in this case.
   * </p>
   *
   * @param name the name of the font (must not be null)
   * @param height the font height in points
   * @param style a bit or combination of <code>NORMAL</code>, <code>BOLD</code>,
   *          <code>ITALIC</code>
   * @return the font
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the name argument is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the height is negative</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Font(Device, String, int, int)</code> instead.
   */
  @Deprecated
  public static Font getFont( String name, int height, int style ) {
    checkThread();
    ResourceFactory resourceFactory = getApplicationContext().getResourceFactory();
    return resourceFactory.getFont( new FontData( name, height, style ) );
  }

  /**
   * Returns an instance of {@link Image} based on the specified image path. The image has to be on
   * the applications class-path.
   *
   * @param path the path to the image
   * @return the image
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the path is null</li>
   *              <li>ERROR_ILLEGAL_ARGUMENT - if the path is invalid</li>
   *              </ul>
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_IO - if an IO error occurs while reading from the stream</li>
   *              <li>ERROR_INVALID_IMAGE - if the image stream contains invalid data</li>
   *              <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an unrecognized format
   *              </li>
   *              </ul>
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Image(Device, InputStream)} instead.
   */
  @Deprecated
  public static Image getImage( String path ) {
    checkThread();
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( "".equals( path ) ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return getApplicationContext().getImageFactory().findImage( path );
  }

  /**
   * Returns an instance of {@link Image} based on the specified image path. The image has to be on
   * the applications class-path. Uses the specified classloader to load the image.
   *
   * @param path the path to the image
   * @param imageLoader the classloader to use
   * @return the image
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the path is null</li>
   *              <li>ERROR_ILLEGAL_ARGUMENT - if the path is invalid</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_IO - if an IO error occurs while reading from the stream</li>
   *              <li>ERROR_INVALID_IMAGE - if the image stream contains invalid data</li>
   *              <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an unrecognized format
   *              </li>
   *              </ul>
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Image(Device, InputStream)} instead.
   */
  @Deprecated
  public static Image getImage( String path, ClassLoader imageLoader ) {
    checkThread();
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( "".equals( path ) ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return getApplicationContext().getImageFactory().findImage( path, imageLoader );
  }

  /**
   * Returns an instance of {@link Image} based on the specified image path. The image will be read
   * from the provided InputStream.
   *
   * @param path the path the image resource is registered at
   * @param inputStream the input stream for the image
   * @return the image
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the path is null</li>
   *              <li>ERROR_NULL_ARGUMENT - if the inputStream is null</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the path is invalid</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_IO - if an IO error occurs while reading from the stream</li>
   *              <li>ERROR_INVALID_IMAGE - if the image stream contains invalid data</li>
   *              <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an unrecognized format
   *              </li>
   *              </ul>
   * @deprecated Factory created resources are obsolete and will be removed in a future release. Use
   *             the constructor <code>Image(Device, InputStream)} instead.
   */
  @Deprecated
  public static Image getImage( String path, InputStream inputStream ) {
    checkThread();
    if( path == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( inputStream == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( "".equals( path ) ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    return getApplicationContext().getImageFactory().findImage( path, inputStream );
  }

  /**
   * Returns the extent of the given string. Tab expansion and carriage return processing are
   * performed.
   * <p>
   * The <em>extent</em> of a string is the width and height of the rectangular area it would cover
   * if drawn in a particular font.
   * </p>
   *
   * @param font the font for which the result is valid
   * @param string the string to measure
   * @param wrapWidth the maximum width of the text. The text will be wrapped to match this width.
   *          If set to 0, no wrapping will be performed.
   * @return a point containing the extent of the string
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the font or string argument is null</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated In most cases, you can use SWT API to measure texts. Create a GC, set the font, and
   *             measure the text using {@link GC#textExtent(String)}. Don't forget to dispose the
   *             GC afterwards.
   */
  @Deprecated
  public static Point textExtent( Font font, String string, int wrapWidth ) {
    checkThread();
    if( font == null || string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return TextSizeUtil.textExtent( font, string, wrapWidth );
  }

  /**
   * Returns the extent of the given string. No tab expansion or carriage return processing will be
   * performed.
   * <p>
   * The <em>extent</em> of a string is the width and height of the rectangular area it would cover
   * if drawn in a particular font.
   * </p>
   *
   * @param font the font for which the result is valid
   * @param string the string to measure
   * @return a point containing the extent of the string
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the font or string arguemnt is null</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated You can use SWT API to measure a string. Create a GC, set the font, and measure the
   *             string using {@link GC#stringExtent(String)}. Don't forget to dispose the GC
   *             afterwards.
   */
  @Deprecated
  public static Point stringExtent( Font font, String string ) {
    checkThread();
    if( font == null || string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return TextSizeUtil.stringExtent( font, string );
  }

  /**
   * Returns the height of the specified font, measured in pixels.
   *
   * @param font the font for which the result is valid
   * @return the height of the font
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the font argument is null</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated Application code should not need to use this method. If you need this information,
   *             you can use {@link GC#stringExtent(String)} with a string of your choice and get
   *             the height of the result.
   */
  @Deprecated
  public static int getCharHeight( Font font ) {
    checkThread();
    if( font == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return TextSizeUtil.getCharHeight( font );
  }

  /**
   * Returns the average character width of the specified font, measured in pixels.
   *
   * @param font the font for which the result is valid
   * @return the average character width of the font
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the font argument is null</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the current display's UI
   *              thread</li>
   *              </ul>
   * @deprecated Application code should not need to use this method. If you need an average
   *             character width, you can use {@link GC#stringExtent(String)} with a string of your
   *             choice and calculate the average width.
   */
  @Deprecated
  public static float getAvgCharWidth( Font font ) {
    if( font == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return TextSizeUtil.getAvgCharWidth( font );
  }

  static void checkThread() {
    if( getDisplayThread() != Thread.currentThread() ) {
      SWT.error( SWT.ERROR_THREAD_INVALID_ACCESS );
    }
  }

  private static Thread getDisplayThread() {
    Thread result = null;
    Display display = LifeCycleUtil.getSessionDisplay();
    if( display != null ) {
      result = display.getThread();
    }
    return result;
  }

  private Graphics() {
    // prevent instantiation
  }
}
