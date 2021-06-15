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

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.graphics.ResourceFactory;


public class CssColor implements CssValue {

  private static final String TRANSPARENT_STR = "transparent";
  private static final Map<String, int[]> NAMED_COLORS = new HashMap<>();
  public static final CssColor BLACK = new CssColor( 0, 0, 0, 1f );
  public static final CssColor WHITE = new CssColor( 255, 255, 255, 1f );
  public static final CssColor TRANSPARENT = new CssColor();

  public final int red;
  public final int green;
  public final int blue;
  public final float alpha;

  static {
    // register 16 standard HTML colors
    NAMED_COLORS.put( "black", new int[] { 0, 0, 0 } );
    NAMED_COLORS.put( "gray", new int[] { 128, 128, 128 } );
    NAMED_COLORS.put( "silver", new int[] { 192, 192, 192 } );
    NAMED_COLORS.put( "white", new int[] { 255, 255, 255 } );
    NAMED_COLORS.put( "maroon", new int[] { 128, 0, 0 } );
    NAMED_COLORS.put( "red", new int[] { 255, 0, 0 } );
    NAMED_COLORS.put( "purple", new int[] { 128, 0, 128 } );
    NAMED_COLORS.put( "fuchsia", new int[] { 255, 0, 255 } );
    NAMED_COLORS.put( "green", new int[] { 0, 128, 0 } );
    NAMED_COLORS.put( "lime", new int[] { 0, 255, 0 } );
    NAMED_COLORS.put( "navy", new int[] { 0, 0, 128 } );
    NAMED_COLORS.put( "blue", new int[] { 0, 0, 255 } );
    NAMED_COLORS.put( "olive", new int[] { 128, 128, 0 } );
    NAMED_COLORS.put( "yellow", new int[] { 255, 255, 0 } );
    NAMED_COLORS.put( "teal", new int[] { 0, 128, 128 } );
    NAMED_COLORS.put( "aqua", new int[] { 0, 255, 255 } );
  }

  private CssColor() {
    red = 0;
    green = 0;
    blue = 0;
    alpha = 0f;
  }

  private CssColor( int red, int green, int blue, float alpha ) {
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.alpha = alpha;
  }

  public static CssColor create( int red, int green, int blue ) {
    if( red == 0 && green == 0 && blue == 0 ) {
      return BLACK;
    }
    if( red == 255 && green == 255 && blue == 255 ) {
      return WHITE;
    }
    return new CssColor( red, green, blue, 1f );
  }

  public static CssColor create( int red, int green, int blue, float alpha ) {
    checkAlpha( alpha );
    if( alpha == 1f ) {
      return create( red, green, blue );
    }
    return new CssColor( red, green, blue, alpha );
  }

  private static void checkAlpha( float alpha ) {
    if( alpha < 0 || alpha > 1 ) {
      String msg = "Alpha out of range [ 0, 1 ]: " + alpha;
      throw new IllegalArgumentException( msg );
    }
  }

  public static CssColor valueOf( String input ) {
    if( input == null ) {
      throw new NullPointerException( "null argument" );
    }
    if( TRANSPARENT_STR.equals( input ) ) {
      return TRANSPARENT;
    }
    int red, green, blue;
    float alpha = 1f;
    String lowerCaseInput = input.toLowerCase( Locale.ENGLISH );
    if( input.startsWith( "#" ) ) {
      try {
        if( input.length() == 7 ) {
          red = Integer.parseInt( input.substring( 1, 3 ), 16 );
          green = Integer.parseInt( input.substring( 3, 5 ), 16 );
          blue = Integer.parseInt( input.substring( 5, 7 ), 16 );
        } else if( input.length() == 4 ) {
          red = Integer.parseInt( input.substring( 1, 2 ), 16 ) * 17;
          green = Integer.parseInt( input.substring( 2, 3 ), 16 ) * 17;
          blue = Integer.parseInt( input.substring( 3, 4 ), 16 ) * 17;
        } else {
          String message = "Illegal number of characters in color definition: " + input;
          throw new IllegalArgumentException( message );
        }
      } catch( NumberFormatException e ) {
        String message = "Illegal number format in color definition: " + input;
        throw new IllegalArgumentException( message, e );
      }
    } else if( NAMED_COLORS.containsKey( lowerCaseInput ) ) {
      int[] values = NAMED_COLORS.get( lowerCaseInput );
      red = values[ 0 ];
      green = values[ 1 ];
      blue = values[ 2 ];
    } else {
      String[] parts = input.split( "\\s*,\\s*" );
      if( parts.length >= 3 && parts.length <= 4 ) {
        try {
          red = Integer.parseInt( parts[ 0 ] );
          green = Integer.parseInt( parts[ 1 ] );
          blue = Integer.parseInt( parts[ 2 ] );
          if( parts.length == 4 ) {
            alpha = Float.parseFloat( parts[ 3 ] );
          }
        } catch( NumberFormatException e ) {
          String message = "Illegal number format in color definition: " + input;
          throw new IllegalArgumentException( message, e );
        }
      } else {
        String message = "Invalid color name: " + input;
        throw new IllegalArgumentException( message );
      }
    }
    return create( red, green, blue, alpha );
  }

  public boolean isTransparent() {
    return alpha == 0f;
  }

  @Override
  public String toDefaultString() {
    if( isTransparent() ) {
      return TRANSPARENT_STR;
    }
    return alpha == 1f ? toHtmlString( red, green, blue ) : toRgbaString( red, green, blue, alpha );
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssColor ) {
      CssColor other = ( CssColor )object;
      return other.red == red && other.green == green && other.blue == blue && other.alpha == alpha;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = -1;
    if( !isTransparent() ) {
      result = 41;
      result += 19 * result + red;
      result += 19 * result + green;
      result += 19 * result + blue;
      result += 19 * result + Float.floatToIntBits( alpha );
    }
    return result;
  }

  @Override
  public String toString() {
    String colors = red + ", " + green + ", " + blue + ", " + alpha;
    return "CssColor{ " + ( isTransparent() ? TRANSPARENT_STR : colors ) + " }";
  }

  public static String toHtmlString( int red, int green, int blue ) {
    StringBuilder sb = new StringBuilder();
    sb.append( "#" );
    sb.append( getHexStr( red ) );
    sb.append( getHexStr( green ) );
    sb.append( getHexStr( blue ) );
    return sb.toString();
  }

  public static Color createColor( CssColor color ) {
    Color result = null;
    if( color.alpha != 0f ) {
      ResourceFactory resourceFactory = getApplicationContext().getResourceFactory();
      result = resourceFactory.getColor( color.red, color.green, color.blue );
    }
    return result;
  }

  private static String getHexStr( int value ) {
    String hex = Integer.toHexString( value );
    return hex.length() == 1 ? "0" + hex : hex;
  }

  private static String toRgbaString( int red, int green, int blue, float alpha ) {
    StringBuilder sb = new StringBuilder();
    sb.append( "rgba(" );
    sb.append( red );
    sb.append( "," );
    sb.append( green );
    sb.append( "," );
    sb.append( blue );
    sb.append( "," );
    sb.append( alpha );
    sb.append( ")" );
    return sb.toString();
  }

}
