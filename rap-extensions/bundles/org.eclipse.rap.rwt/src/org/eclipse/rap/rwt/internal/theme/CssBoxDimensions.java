/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.io.Serializable;
import java.util.zip.CRC32;

import org.eclipse.rap.rwt.theme.BoxDimensions;


public class CssBoxDimensions implements CssValue, Serializable {

  public static final CssBoxDimensions ZERO = new CssBoxDimensions( 0, 0, 0, 0 );

  public final BoxDimensions dimensions;

  private CssBoxDimensions( int top, int right, int bottom, int left ) {
    dimensions = new BoxDimensions( top, right, bottom, left );
  }

  public static CssBoxDimensions create( int top, int right, int bottom, int left ) {
    if( top == 0 && right == 0 && bottom == 0 && left == 0 ) {
      return ZERO;
    }
    return new CssBoxDimensions( top, right, bottom, left );
  }

  public static CssBoxDimensions valueOf( String input ) {
    if( input == null ) {
      throw new NullPointerException( "null argument" );
    }
    String[] parts = input.split( "\\s+" );
    if( parts.length == 0 || parts.length > 4 ) {
      String msg = "Illegal number of arguments for box dimensions";
      throw new IllegalArgumentException( msg );
    }
    int top, right, left, bottom;
    top = right = bottom = left = parsePxValue( parts[ 0 ] );
    if( parts.length >= 2 ) {
      right = left = parsePxValue( parts[ 1 ] );
    }
    if( parts.length >= 3 ) {
      bottom = parsePxValue( parts[ 2 ] );
    }
    if( parts.length == 4 ) {
      left = parsePxValue( parts[ 3 ] );
    }
    return create( top, right, bottom, left );
  }

  @Override
  public String toDefaultString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append( dimensions.top + "px" );
    if(    dimensions.right != dimensions.top
        || dimensions.bottom != dimensions.top
        || dimensions.left != dimensions.top )
    {
      buffer.append( " " + dimensions.right + "px" );
    }
    if( dimensions.bottom != dimensions.top || dimensions.left != dimensions.right ) {
      buffer.append( " " + dimensions.bottom + "px" );
    }
    if( dimensions.left != dimensions.right ) {
      buffer.append( " " + dimensions.left + "px" );
    }
    return buffer.toString();
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssBoxDimensions ) {
      CssBoxDimensions other = ( CssBoxDimensions )object;
      return other.dimensions.equals( this.dimensions );
    }
    return false;
  }

  @Override
  public int hashCode() {
    CRC32 result = new CRC32();
    result.update( dimensions.top );
    result.update( dimensions.right );
    result.update( dimensions.bottom );
    result.update( dimensions.left );
    return ( int )result.getValue();
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "CssBoxDimensions{ " );
    buffer.append( dimensions.top );
    buffer.append( ", " );
    buffer.append( dimensions.right );
    buffer.append( ", " );
    buffer.append( dimensions.bottom );
    buffer.append( ", " );
    buffer.append( dimensions.left );
    buffer.append( " }" );
    return buffer.toString();
  }

  private static int parsePxValue( String part ) {
    Integer result = CssDimension.parseLength( part );
    if( result == null ) {
      throw new IllegalArgumentException( "Illegal parameter: " + part );
    }
    return result.intValue();
  }

}
