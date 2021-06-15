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


public class CssBorder implements CssValue {

  public static final CssBorder NONE = new CssBorder( 0, "none", null );

  private static final String[] VALID_STYLES = {
    "none",
    "hidden",
    "dotted",
    "dashed",
    "solid",
    "double",
    "groove",
    "ridge",
    "inset",
    "outset"
  };

  public final int width;
  public final String style;
  public final CssColor color;

  private CssBorder( int width, String style, CssColor color ) {
    this.width = width;
    this.style = style;
    this.color = color;
  }

  public static CssBorder create( int width, String style, CssColor color ) {
    if( width <= 0 || style == null || "none".equals( style ) || "hidden".equals( style ) ) {
      return NONE;
    }
    return new CssBorder( width, style, color );
  }

  public static CssBorder valueOf( String input ) {
    if( input == null ) {
      throw new NullPointerException( "null argument" );
    }
    String[] parts = input.split( "\\s+" );
    if( input.trim().length() == 0 ) {
      throw new IllegalArgumentException( "Empty border definition" );
    }
    if( parts.length > 3 ) {
      throw new IllegalArgumentException( "Illegal number of arguments for border" );
    }
    int width = -1;
    String style = null;
    CssColor color = null;
    for( int i = 0; i < parts.length; i++ ) {
      String part = parts[ i ];
      boolean consumed = "".equals( part );
      // parse width
      if( !consumed && width == -1 ) {
        Integer parsedWidth = CssDimension.parseLength( part );
        if( parsedWidth != null ) {
          if( parsedWidth.intValue() < 0 ) {
            throw new IllegalArgumentException( "Negative width: " + part );
          }
          width = parsedWidth.intValue();
          consumed = true;
        }
      }
      // parse style
      if( !consumed && style == null ) {
        String parsedStyle = parseStyle( part );
        if( parsedStyle != null ) {
          style = parsedStyle;
          consumed = true;
        }
      }
      // parse color
      if( !consumed && color == null ) {
        color = CssColor.valueOf( part );
        consumed = true;
      }
      if( !consumed ) {
        throw new IllegalArgumentException( "Illegal parameter for color: " + part );
      }
    }
    if( width == -1 ) {
      width = 1;
    }
    return CssBorder.create( width, style, color );
  }

  @Override
  public String toDefaultString() {
    if( width == 0 ) {
      return "none";
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append( width );
    buffer.append( "px " );
    buffer.append( style );
    if( color != null ) {
      buffer.append( " " );
      buffer.append( color.toDefaultString() );
    }
    return buffer.toString();
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssBorder ) {
      CssBorder other = ( CssBorder )object;
      return    other.width == width
             && style.equals( other.style )
             && ( color == null ? other.color == null : color.equals( other.color ) );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 23;
    result += 37 * result + width;
    if( style != null ) {
      result += 37 * result + style.hashCode();
    }
    if( color != null ) {
      result += 37 * result + color.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return "CssBorder{ " + width + ", " + style + ", " + color + " }";
  }

  private static String parseStyle( String part ) {
    String result = null;
    for( int j = 0; j < VALID_STYLES.length && result == null; j++ ) {
      if( VALID_STYLES[ j ].equalsIgnoreCase( part ) ) {
        result = VALID_STYLES[ j ];
      }
    }
    return result;
  }

}
