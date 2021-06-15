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

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import org.eclipse.rap.rwt.internal.theme.CssFont;
import org.eclipse.rap.rwt.internal.theme.CssValue;
import org.eclipse.rap.rwt.internal.theme.SimpleSelector;
import org.eclipse.rap.rwt.internal.theme.ThemeUtil;
import org.eclipse.rap.rwt.internal.util.SerializableLock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.graphics.ResourceFactory;


/**
 * This class is the abstract superclass of all device objects,
 * such as Display.
 *
 * <p>This class is <em>not</em> intended to be directly used by clients.</p>
 */
public abstract class Device implements Drawable, SerializableCompatibility {

  // SWT code uses Device.class as the synchronization lock. This synchronize
  // access from all over the application. In RWT we need a way to synchronize
  // access from within a session. Therefore Device.class was replaced by the
  // 'deviceLock'.
  protected final SerializableLock deviceLock;
  private boolean disposed;

  public Device() {
    deviceLock = new SerializableLock();
  }

  /**
   * Returns the matching standard color for the given
   * constant, which should be one of the color constants
   * specified in class <code>SWT</code>. Any value other
   * than one of the SWT color constants which is passed
   * in will result in the color black. This color should
   * not be free'd because it was allocated by the system,
   * not the application.
   *
   * @param id the color constant
   * @return the matching color
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @see SWT
   */
  public Color getSystemColor( int id ) {
    checkDevice();
    ResourceFactory resourceFactory = getApplicationContext().getResourceFactory();
    Color result;
    switch( id ) {
      case SWT.COLOR_TRANSPARENT:
        result = resourceFactory.getColor( 0, 0, 0, 0 );
        break;
      case SWT.COLOR_WHITE:
        result = resourceFactory.getColor( 255, 255, 255 );
      break;
      case SWT.COLOR_BLACK:
        result = resourceFactory.getColor( 0, 0, 0 );
      break;
      case SWT.COLOR_RED:
        result = resourceFactory.getColor( 255, 0, 0 );
      break;
      case SWT.COLOR_DARK_RED:
        result = resourceFactory.getColor( 128, 0, 0 );
      break;
      case SWT.COLOR_GREEN:
        result = resourceFactory.getColor( 0, 255, 0 );
      break;
      case SWT.COLOR_DARK_GREEN:
        result = resourceFactory.getColor( 0, 128, 0 );
      break;
      case SWT.COLOR_YELLOW:
        result = resourceFactory.getColor( 255, 255, 0 );
      break;
      case SWT.COLOR_DARK_YELLOW:
        result = resourceFactory.getColor( 128, 128, 0 );
      break;
      case SWT.COLOR_BLUE:
        result = resourceFactory.getColor( 0, 0, 255 );
      break;
      case SWT.COLOR_DARK_BLUE:
        result = resourceFactory.getColor( 0, 0, 128 );
      break;
      case SWT.COLOR_MAGENTA:
        result = resourceFactory.getColor( 255, 0, 255 );
      break;
      case SWT.COLOR_DARK_MAGENTA:
        result = resourceFactory.getColor( 128, 0, 128 );
      break;
      case SWT.COLOR_CYAN:
        result = resourceFactory.getColor( 0, 255, 255 );
      break;
      case SWT.COLOR_DARK_CYAN:
        result = resourceFactory.getColor( 0, 128, 128 );
      break;
      case SWT.COLOR_GRAY:
        result = resourceFactory.getColor( 192, 192, 192 );
      break;
      case SWT.COLOR_DARK_GRAY:
        result = resourceFactory.getColor( 128, 128, 128 );
      break;
      default:
        result = resourceFactory.getColor( 0, 0, 0 );
      break;
    }
    return result;
  }

  /**
   * Returns a reasonable font for applications to use.
   * On some platforms, this will match the "default font"
   * or "system font" if such can be found.  This font
   * should not be free'd because it was allocated by the
   * system, not the application.
   * <p>
   * Typically, applications which want the default look
   * should simply not set the font on the widgets they
   * create. Widgets are always created with the correct
   * default font for the class of user-interface component
   * they represent.
   * </p>
   *
   * @return a font
   *
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public Font getSystemFont() {
    checkDevice();
    CssValue font = ThemeUtil.getCssValue( "Display", "font", SimpleSelector.DEFAULT );
    return CssFont.createFont( ( CssFont )font );
  }

  /**
   * Returns <code>FontData</code> objects which describe
   * the fonts that match the given arguments. If the
   * <code>faceName</code> is null, all fonts will be returned.
   *
   * @param faceName the name of the font to look for, or null
   * @param scalable if true only scalable fonts are returned, otherwise only non-scalable fonts are returned.
   * @return the matching font data
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   *
   * @since 1.3
   */
  public FontData[] getFontList( String faceName, boolean scalable ) {
    checkDevice();
    FontData[] result = new FontData[ 0 ];
    if( scalable ) {
      CssFont fontList
        = ( CssFont )ThemeUtil.getCssValue( "Display", "rwt-fontlist", SimpleSelector.DEFAULT );
      if( faceName == null ) {
        result = new FontData[ fontList.family.length ];
        for( int i = 0; i < result.length; i++ ) {
          result[ i ] = new FontData( fontList.family[ i ], 0, SWT.NORMAL );
        }
      } else {
        int counter = 0;
        for( int i = 0; i < fontList.family.length; i++ ) {
          if( fontList.family[ i ].startsWith( faceName ) ) {
            counter++;
          }
        }
        result = new FontData[ counter ];
        counter = 0;
        for( int i = 0; i < fontList.family.length; i++ ) {
          if( fontList.family[ i ].startsWith( faceName ) ) {
            result[ counter++ ] = new FontData( fontList.family[ i ], 0, SWT.NORMAL );
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns a rectangle which describes the area of the receiver which is
   * capable of displaying data.
   *
   * @return the client area
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @see #getBounds
   * @since 1.2
   */
  public Rectangle getClientArea() {
    checkDevice();
    return getBounds();
  }

  /**
   * Returns the bit depth of the screen, which is the number of
   * bits it takes to represent the number of unique colors that
   * the screen is currently capable of displaying. This number
   * will typically be one of 1, 8, 15, 16, 24 or 32.
   *
   * @return the depth of the screen
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 1.3
   */
  public int getDepth() {
    checkDevice();
    return 16;
  }

  /**
   * Returns a point whose x coordinate is the horizontal
   * dots per inch of the display, and whose y coordinate
   * is the vertical dots per inch of the display.
   *
   * @return the horizontal and vertical DPI
   *
   * @exception SWTException <ul>
   *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 1.3
   */
  public Point getDPI() {
    checkDevice();
    return new Point( 0, 0 );
  }

  /**
   * Returns a rectangle describing the receiver's size and location.
   *
   * @return the bounding rectangle
   * @exception SWTException <ul>
   *   <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   * @since 1.2
   */
  public Rectangle getBounds() {
    checkDevice();
    return new Rectangle( 0, 0, 0, 0 );
  }

  /**
   * Disposes of the operating system resources associated with
   * the receiver. After this method has been invoked, the receiver
   * will answer <code>true</code> when sent the message
   * <code>isDisposed()</code>.
   *
   * @see #release
   * @see #destroy
   * @see #checkDevice
   */
  public void dispose() {
    synchronized( deviceLock ) {
      if( !isDisposed() ) {
        checkDevice();
        release();
        destroy();
        disposed = true;
      }
    }
  }

  /**
   * Returns <code>true</code> if the device has been disposed,
   * and <code>false</code> otherwise.
   * <p>
   * This method gets the dispose state for the device.
   * When a device has been disposed, it is an error to
   * invoke any other method using the device.
   *
   * @return <code>true</code> when the device is disposed and <code>false</code> otherwise
   */
  public boolean isDisposed() {
    synchronized( deviceLock ) {
      return disposed;
    }
  }

  /**
   * Releases any internal resources <!-- back to the operating
   * system and clears all fields except the device handle -->.
   * <p>
   * When a device is destroyed, resources that were acquired
   * on behalf of the programmer need to be returned to the
   * operating system.  For example, if the device allocated a
   * font to be used as the system font, this font would be
   * freed in <code>release</code>.  Also,to assist the garbage
   * collector and minimize the amount of memory that is not
   * reclaimed when the programmer keeps a reference to a
   * disposed device, all fields except the handle are zero'd.
   * The handle is needed by <code>destroy</code>.
   * </p>
   * This method is called before <code>destroy</code>.
   * </p><p>
   * If subclasses reimplement this method, they must
   * call the <code>super</code> implementation.
   * </p>
   *
   * @see #dispose
   * @see #destroy
   */
  protected void release() {
  }

  /**
   * Destroys the device <!-- in the operating system and releases
   * the device's handle -->.  If the device does not have a handle,
   * this method may do nothing depending on the device.
   * <p>
   * This method is called after <code>release</code>.
   * </p><p>
   * Subclasses are supposed to reimplement this method and not
   * call the <code>super</code> implementation.
   * </p>
   *
   * @see #dispose
   * @see #release
   */
  protected void destroy() {
  }

  /**
   * Throws an <code>SWTException</code> if the receiver can not
   * be accessed by the caller. This may include both checks on
   * the state of the receiver and more generally on the entire
   * execution context. This method <em>should</em> be called by
   * device implementors to enforce the standard SWT invariants.
   * <p>
   * Currently, it is an error to invoke any method (other than
   * <code>isDisposed()</code> and <code>dispose()</code>) on a
   * device that has had its <code>dispose()</code> method called.
   * </p><p>
   * In future releases of SWT, there may be more or fewer error
   * checks and exceptions may be thrown for different reasons.
   * <p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  protected void checkDevice() {
    if( disposed ) {
      SWT.error( SWT.ERROR_DEVICE_DISPOSED );
    }
  }

}
