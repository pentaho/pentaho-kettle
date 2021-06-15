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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;


/**
 * Instances of this class manage resources that define how text looks when it
 * is displayed.
 *
 * @see FontData
 * @since 1.0
 */
public class Font extends Resource {

  private final FontData internalFontData;

  // used by ResourceFactory#getFont()
  private Font( FontData fontData ) {
    super( null );
    internalFontData = findFontData( fontData );
  }

  /**
   * Constructs a new font given a device and font data
   * which describes the desired font's appearance.
   * <p>
   * You must dispose the font when it is no longer required.
   * </p>
   *
   * @param device the device to create the font on
   * @param fontData the FontData that describes the desired font (must not be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the fontData argument is null</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES - if a font could not be created from the given font data</li>
   * </ul>
   *
   * @since 1.3
   */
  public Font( Device device, FontData fontData ) {
    super( checkDevice( device ) );
    if( fontData == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    internalFontData = findFontData( fontData );
  }

  /**
   * Constructs a new font given a device and an array
   * of font data which describes the desired font's
   * appearance.
   * <p>
   * You must dispose the font when it is no longer required.
   * </p>
   *
   * @param device the device to create the font on
   * @param fontData the array of FontData that describes the desired font (must not be null)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the fontData argument is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the length of fontData is zero</li>
   *    <li>ERROR_NULL_ARGUMENT - if any font data in the array is null</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES - if a font could not be created from the given font data</li>
   * </ul>
   *
   * @since 1.3
   */
  public Font( Device device, FontData[] fontData ) {
    super( checkDevice( device ) );
    if( fontData == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( fontData.length == 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    for( int i = 0; i < fontData.length; i++ ) {
      if( fontData[ i ] == null ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
    }
    internalFontData = findFontData( fontData[ 0 ] );
  }

  /**
   * Constructs a new font given a device, a font name,
   * the height of the desired font in points, and a font
   * style.
   * <p>
   * You must dispose the font when it is no longer required.
   * </p>
   *
   * @param device the device to create the font on
   * @param name the name of the font (must not be null)
   * @param height the font height in points
   * @param style a bit or combination of NORMAL, BOLD, ITALIC
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
   *    <li>ERROR_NULL_ARGUMENT - if the name argument is null</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the height is negative</li>
   * </ul>
   * @exception SWTError <ul>
   *    <li>ERROR_NO_HANDLES - if a font could not be created from the given arguments</li>
   * </ul>
   */
  public Font( Device device, String name, int height, int style ) {
    super( checkDevice( device ) );
    if( name == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( height < 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    FontData fontData = new FontData( name, height, style );
    internalFontData = findFontData( fontData );
  }

  /**
   * Returns an array of <code>FontData</code>s representing the receiver.
   * <!--
   * On Windows, only one FontData will be returned per font. On X however,
   * a <code>Font</code> object <em>may</em> be composed of multiple X
   * fonts. To support this case, we return an array of font data objects.
   * -->
   *
   * @return an array of font data objects describing the receiver
   *
   * @exception SWTException <ul>
   *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
   * </ul>
   */
  public FontData[] getFontData() {
    if( isDisposed() ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    FontData fontData = new FontData( internalFontData.getName(),
                                      internalFontData.getHeight(),
                                      internalFontData.getStyle() );
    return new FontData[] { fontData };
  }

  @Override
  public boolean equals( Object object ) {
    boolean result;
    if( object == this ) {
      result = true;
    } else if( object instanceof Font ) {
      Font font = ( Font )object;
      result = font.internalFontData.equals( internalFontData );
    } else {
      result = false;
    }
    return result;
  }

  @Override
  public int hashCode() {
    return internalFontData.hashCode() * 7;
  }

  /**
   * Returns a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return a string representation of the receiver
   */
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "Font {" );
    buffer.append( internalFontData.getName() );
    buffer.append( "," );
    buffer.append( internalFontData.getHeight() );
    buffer.append( "," );
    int style = internalFontData.getStyle();
    String styleName;
    if( ( style & SWT.BOLD ) != 0 && ( style & SWT.ITALIC ) != 0 ) {
      styleName = "BOLD|ITALIC";
    } else if( ( style & SWT.BOLD ) != 0 ) {
      styleName = "BOLD";
    } else if( ( style & SWT.ITALIC ) != 0 ) {
      styleName = "ITALIC";
    } else {
      styleName = "NORMAL";
    }
    buffer.append( styleName );
    buffer.append( "}" );
    return buffer.toString();
  }

  private static FontData findFontData( FontData fontData ) {
    return getApplicationContext().getFontDataFactory().findFontData( fontData );
  }

}
