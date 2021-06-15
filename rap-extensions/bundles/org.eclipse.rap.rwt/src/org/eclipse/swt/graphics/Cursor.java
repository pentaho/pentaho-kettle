/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;

/**
 * Instances of this class manage operating system resources that specify the
 * appearance of the on-screen pointer.
 *
 * <p>To obtain cursors, it is recommended to use one of the
 * <code>getSystemCursor</code> method from class <code>Display</code>.
 * </p>
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>
 *   CURSOR_ARROW, CURSOR_WAIT, CURSOR_CROSS, CURSOR_APPSTARTING, CURSOR_HELP,
 *   CURSOR_SIZEALL, CURSOR_SIZENESW, CURSOR_SIZENS, CURSOR_SIZENWSE, CURSOR_SIZEWE,
 *   CURSOR_SIZEN, CURSOR_SIZES, CURSOR_SIZEE, CURSOR_SIZEW, CURSOR_SIZENE, CURSOR_SIZESE,
 *   CURSOR_SIZESW, CURSOR_SIZENW, CURSOR_UPARROW, CURSOR_IBEAM, CURSOR_NO, CURSOR_HAND
 * </dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p>
 *
 * @since 1.2
 */
public class Cursor extends Resource {

  private final int value;

  // used by ResourceFactory#getCursor()
  private Cursor( int style ) {
    super( null );
    checkStyle( style );
    value = style;
  }

  /**
   * Constructs a new cursor given a device and a style
   * constant describing the desired cursor appearance.
   * <p>
   * You must dispose the cursor when it is no longer required.
   * </p>
   * NOTE:
   * It is recommended to use {@link org.eclipse.swt.widgets.Display#getSystemCursor(int)}
   * instead of using this constructor. This way you can avoid the
   * overhead of disposing the Cursor resource.
   *
   * @param device the device on which to allocate the cursor
   * @param style the style of cursor to allocate
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_INVALID_ARGUMENT - when an unknown style is specified</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES - if a handle could not be obtained for cursor creation</li>
   * </ul>
   *
   * @see SWT#CURSOR_ARROW
   * @see SWT#CURSOR_WAIT
   * @see SWT#CURSOR_CROSS
   * @see SWT#CURSOR_APPSTARTING
   * @see SWT#CURSOR_HELP
   * @see SWT#CURSOR_SIZEALL
   * @see SWT#CURSOR_SIZENESW
   * @see SWT#CURSOR_SIZENS
   * @see SWT#CURSOR_SIZENWSE
   * @see SWT#CURSOR_SIZEWE
   * @see SWT#CURSOR_SIZEN
   * @see SWT#CURSOR_SIZES
   * @see SWT#CURSOR_SIZEE
   * @see SWT#CURSOR_SIZEW
   * @see SWT#CURSOR_SIZENE
   * @see SWT#CURSOR_SIZESE
   * @see SWT#CURSOR_SIZESW
   * @see SWT#CURSOR_SIZENW
   * @see SWT#CURSOR_UPARROW
   * @see SWT#CURSOR_IBEAM
   * @see SWT#CURSOR_NO
   * @see SWT#CURSOR_HAND
   * @see org.eclipse.swt.widgets.Display#getSystemCursor(int)
   *
   * @since 1.3
   */
  public Cursor( Device device, int style ) {
    super( checkDevice( device ) );
  	checkStyle( style );
  	value = style;
  }
  /**
   * Compares the argument to the receiver, and returns true
   * if they represent the <em>same</em> object using a class
   * specific comparison.
   *
   * @param object the object to compare with this object
   * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
   *
   * @see #hashCode
   */
  @Override
  public boolean equals( Object object ) {
    boolean result;
    if( object == this ) {
      result = true;
    } else if( object instanceof Cursor ) {
      Cursor cursor = ( Cursor )object;
      result = cursor.value == value;
    } else {
      result = false;
    }
    return result;
  }

  /**
   * Returns an integer hash code for the receiver. Any two
   * objects that return <code>true</code> when passed to
   * <code>equals</code> must return the same value for this
   * method.
   *
   * @return the receiver's hash
   *
   * @see #equals
   */
  @Override
  public int hashCode() {
    return 101 * value;
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the receiver
   */
  @Override
  public String toString () {
    String result;
    if (isDisposed()) {
      result = "Cursor {*DISPOSED*}";
    } else {
      result = "Cursor {" + value + "}";
    }
    return result;
  }

  private static void checkStyle( int style ) {
    switch( style ) {
      case SWT.CURSOR_ARROW:
      case SWT.CURSOR_WAIT:
      case SWT.CURSOR_APPSTARTING:
      case SWT.CURSOR_CROSS:
      case SWT.CURSOR_HELP:
      case SWT.CURSOR_SIZEALL:
      case SWT.CURSOR_SIZENESW:
      case SWT.CURSOR_SIZENS:
      case SWT.CURSOR_SIZENWSE:
      case SWT.CURSOR_SIZEWE:
      case SWT.CURSOR_SIZEN:
      case SWT.CURSOR_SIZES:
      case SWT.CURSOR_SIZEE:
      case SWT.CURSOR_SIZEW:
      case SWT.CURSOR_SIZENE:
      case SWT.CURSOR_SIZESE:
      case SWT.CURSOR_SIZESW:
      case SWT.CURSOR_SIZENW:
      case SWT.CURSOR_IBEAM:
      case SWT.CURSOR_HAND:
      case SWT.CURSOR_UPARROW:
      case SWT.CURSOR_NO:
        break;
      default:
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
  }
}
