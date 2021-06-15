/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.remote;

import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.parseFontName;
import static org.eclipse.swt.internal.graphics.FontUtil.getData;

import java.lang.reflect.Field;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.widgets.Widget;


/**
 * Provides utility methods that map common SWT types to their JSON representations used in the RAP
 * protocol and back.
 *
 * @see <a href="http://wiki.eclipse.org/RAP/Protocol">http://wiki.eclipse.org/RAP/Protocol</a>
 * @since 2.3
 */
public class JsonMapping {

  private static final String CURSOR_UPARROW
    = "rwt-resources/resource/widget/rap/cursors/up_arrow.cur";

  private JsonMapping() {
    // prevent instantiation
  }

  /**
   * Returns the JSON representation for the given Widget. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>. Disposed widgets cannot be mapped to JSON.
   *
   * @param widget the widget to encode or <code>null</code>, must not be disposed
   * @return a JSON value that represents the given widget
   */
  public static JsonValue toJson( Widget widget ) {
    if( widget == null ) {
      return JsonValue.NULL;
    }
    if( widget.isDisposed() ) {
      throw new IllegalArgumentException( "Widget is disposed" );
    }
    return JsonValue.valueOf( WidgetUtil.getId( widget ) );
  }

  /**
   * Returns the JSON representation for the given array of widgets. The array must not contain
   * disposed widgets, as those cannot be mapped to JSON.
   *
   * @param widgets the array of widgets to encode, must not be <code>null</code>
   * @return a JSON value that represents the given widget array
   * @since 3.1
   */
  public static JsonValue toJson( Widget[] widgets ) {
    JsonArray widgetIds = new JsonArray();
    for( Widget widget : widgets ) {
      widgetIds.add( toJson( widget ) );
    }
    return widgetIds;
  }

  /**
   * Returns the JSON representation for the given Point. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>.
   *
   * @param point the Point to encode or <code>null</code>
   * @return a JSON value that represents the given point
   */
  public static JsonValue toJson( Point point ) {
    return point == null ? JsonValue.NULL : new JsonArray().add( point.x ).add( point.y );
  }

  /**
   * Returns the JSON representation for the given Rectangle. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>.
   *
   * @param rect the Rectangle to encode or <code>null</code>
   * @return a JSON value that represents the given rectangle
   */
  public static JsonValue toJson( Rectangle rect ) {
    if( rect == null ) {
      return JsonValue.NULL;
    }
    return new JsonArray().add( rect.x ).add( rect.y ).add( rect.width ).add( rect.height );
  }

  /**
   * Returns the JSON representation for the given Color. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>. Disposed colors cannot be mapped to JSON.
   *
   * @param color the Color to encode or <code>null</code>, must not be disposed
   * @return a JSON value that represents the given color
   */
  public static JsonValue toJson( Color color ) {
    return toJson( color, 255 );
  }

  /**
   * Returns the JSON representation for the given Color with an additional alpha (opacity) value.
   * This method accepts <code>null</code>, which will be mapped to <code>JsonValue.NULL</code>
   * regardless of the given alpha value. Disposed colors cannot be mapped to JSON.
   *
   * @param color the Color to encode or <code>null</code>, must not be disposed
   * @param alpha a value in the range of 0 (transparent) to 255 (opaque)
   * @return a JSON value that represents the given color
   */
  public static JsonValue toJson( Color color, int alpha ) {
    if( alpha < 0 || alpha > 255 ) {
      throw new IllegalArgumentException( "Illegal alpha value: " + alpha );
    }
    if( color == null ) {
      return JsonValue.NULL;
    }
    if( color.isDisposed() ) {
      throw new IllegalArgumentException( "Color is disposed" );
    }
    return toJson( color.getRGB(), alpha );
  }

  /**
   * Returns the JSON representation for the given RGB. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>.
   *
   * @param rgb the Color to encode or <code>null</code>
   * @return a JSON value that represents the given color
   */
  public static JsonValue toJson( RGB rgb ) {
    return toJson( rgb, 255 );
  }

  /**
   * Returns the JSON representation for the given RGB with an additional alpha (opacity) value.
   * This method accepts <code>null</code>, which will be mapped to <code>JsonValue.NULL</code>
   * regardless of the given alpha value.
   *
   * @param rgb the RGB to encode or <code>null</code>
   * @param alpha a value in the range of 0 (transparent) to 255 (opaque)
   * @return a JSON value that represents the given rgb
   */
  public static JsonValue toJson( RGB rgb, int alpha ) {
    if( alpha < 0 || alpha > 255 ) {
      throw new IllegalArgumentException( "Illegal alpha value: " + alpha );
    }
    if( rgb == null ) {
      return JsonValue.NULL;
    }
    return new JsonArray().add( rgb.red ).add( rgb.green ).add( rgb.blue ).add( alpha );
  }

  /**
   * Returns the JSON representation for the given Image. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>. Disposed images cannot be mapped to JSON.
   *
   * @param image the Image to encode or <code>null</code>, must not be disposed
   * @return a JSON value that represents the given rgb
   */
  public static JsonValue toJson( Image image ) {
    if( image == null ) {
      return JsonValue.NULL;
    }
    if( image.isDisposed() ) {
      throw new IllegalArgumentException( "Image is disposed" );
    }
    String imagePath = ImageFactory.getImagePath( image );
    Rectangle bounds = image.getBounds();
    return new JsonArray()
      .add( imagePath )
      .add( bounds.width )
      .add( bounds.height );
  }

  /**
   * Returns the JSON representation for the given Font. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>. Disposed fonts cannot be mapped to JSON.
   *
   * @param font the Font to encode or <code>null</code>, must not be disposed
   * @return a JSON value that represents the given font
   */
  public static JsonValue toJson( Font font ) {
    if( font == null ) {
      return JsonValue.NULL;
    }
    if( font.isDisposed() ) {
      throw new IllegalArgumentException( "Font is disposed" );
    }
    return toJson( getData( font ) );
  }

  /**
   * Returns the JSON representation for the given FontData. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>.
   *
   * @param fontData the FontData to encode or <code>null</code>
   * @return a JSON value that represents the given font data
   */
  public static JsonValue toJson( FontData fontData ) {
    return fontData == null ? JsonValue.NULL : new JsonArray()
      .add( JsonUtil.createJsonArray( parseFontName( fontData.getName() ) ) )
      .add( fontData.getHeight() )
      .add( ( fontData.getStyle() & SWT.BOLD ) != 0 )
      .add( ( fontData.getStyle() & SWT.ITALIC ) != 0 );
  }

  /**
   * Returns the JSON representation for the given Cursor. This method accepts <code>null</code>,
   * which will be mapped to <code>JsonValue.NULL</code>.
   *
   * @param cursor the Cursor to encode or <code>null</code>
   * @return a JSON value that represents the given cursor
   *
   * @since 3.1
   */
  public static JsonValue toJson( Cursor cursor ) {
    if( cursor == null ) {
      return JsonValue.NULL;
    }
    // TODO [rst] Find a better way of obtaining the Cursor value
    // TODO [tb] adjust strings to match name of constants
    int value = 0;
    try {
      Field field = Cursor.class.getDeclaredField( "value" );
      field.setAccessible( true );
      value = field.getInt( cursor );
    } catch( Exception e ) {
      throw new RuntimeException( e );
    }
    switch( value ) {
      case SWT.CURSOR_ARROW:
        return JsonValue.valueOf( "default" );
      case SWT.CURSOR_WAIT:
        return JsonValue.valueOf( "wait" );
      case SWT.CURSOR_APPSTARTING:
        return JsonValue.valueOf( "progress" );
      case SWT.CURSOR_CROSS:
        return JsonValue.valueOf( "crosshair" );
      case SWT.CURSOR_HELP:
        return JsonValue.valueOf( "help" );
      case SWT.CURSOR_SIZEALL:
        return JsonValue.valueOf( "move" );
      case SWT.CURSOR_SIZENS:
        return JsonValue.valueOf( "row-resize" );
      case SWT.CURSOR_SIZEWE:
        return JsonValue.valueOf( "col-resize" );
      case SWT.CURSOR_SIZEN:
        return JsonValue.valueOf( "n-resize" );
      case SWT.CURSOR_SIZES:
        return JsonValue.valueOf( "s-resize" );
      case SWT.CURSOR_SIZEE:
        return JsonValue.valueOf( "e-resize" );
      case SWT.CURSOR_SIZEW:
        return JsonValue.valueOf( "w-resize" );
      case SWT.CURSOR_SIZENE:
      case SWT.CURSOR_SIZENESW:
        return JsonValue.valueOf( "ne-resize" );
      case SWT.CURSOR_SIZESE:
        return JsonValue.valueOf( "se-resize" );
      case SWT.CURSOR_SIZESW:
        return JsonValue.valueOf( "sw-resize" );
      case SWT.CURSOR_SIZENW:
      case SWT.CURSOR_SIZENWSE:
        return JsonValue.valueOf( "nw-resize" );
      case SWT.CURSOR_IBEAM:
        return JsonValue.valueOf( "text" );
      case SWT.CURSOR_HAND:
        return JsonValue.valueOf( "pointer" );
      case SWT.CURSOR_NO:
        return JsonValue.valueOf( "not-allowed" );
      case SWT.CURSOR_UPARROW:
        return JsonValue.valueOf( CURSOR_UPARROW );
      default:
        return JsonValue.NULL;
    }
  }

  /**
   * Returns an instance of Point for the given JSON representation. This method returns
   * <code>null</code> if the given JSON value is <code>JsonValue.NULL</code>.
   *
   * @param value a JsonValue that represents a point or <code>JsonValue.NULL</code>
   * @return a Point that corresponds to the given JSON value or <code>null</code>
   */
  public static Point readPoint( JsonValue value ) {
    ParamCheck.notNull( value, "value" );
    if( JsonValue.NULL.equals( value ) ) {
      return null;
    }
    try {
      JsonArray array = value.asArray();
      if( array.size() != 2 ) {
        throw new IllegalArgumentException( "array size != 2" );
      }
      return new Point( array.get( 0 ).asInt(), array.get( 1 ).asInt() );
    } catch( Exception exception ) {
      String message = "Could not create Point for: " + value;
      throw new IllegalArgumentException( message, exception );
    }
  }

  /**
   * Returns an instance of Rectangle for the given JSON representation. This method returns
   * <code>null</code> if the given JSON value is <code>JsonValue.NULL</code>.
   *
   * @param value a JsonValue that represents a rectangle or <code>JsonValue.NULL</code>
   * @return a Rectangle that corresponds to the given JSON value or <code>null</code>
   */
  public static Rectangle readRectangle( JsonValue value ) {
    ParamCheck.notNull( value, "value" );
    if( JsonValue.NULL.equals( value ) ) {
      return null;
    }
    try {
      JsonArray array = value.asArray();
      if( array.size() != 4 ) {
        throw new IllegalArgumentException( "array size != 4" );
      }
      return new Rectangle( array.get( 0 ).asInt(),
                            array.get( 1 ).asInt(),
                            array.get( 2 ).asInt(),
                            array.get( 3 ).asInt() );
    } catch( Exception exception ) {
      String message = "Could not create Rectangle for: " + value;
      throw new IllegalArgumentException( message, exception );
    }
  }

  /**
   * Returns an instance of RGB for the given JSON representation. This method returns
   * <code>null</code> if the given JSON value is <code>JsonValue.NULL</code>.
   *
   * @param value a JsonValue that represents a color or <code>JsonValue.NULL</code>
   * @return an RGB that corresponds to the given JSON value or <code>null</code>
   */
  public static RGB readRGB( JsonValue value ) {
    ParamCheck.notNull( value, "value" );
    if( value.isNull() ) {
      return null;
    }
    try {
      JsonArray array = value.asArray();
      if( array.size() < 3 || array.size() > 4 ) {
        throw new IllegalArgumentException( "Expected array of size 3 or 4" );
      }
      return new RGB( array.get( 0 ).asInt(), array.get( 1 ).asInt(), array.get( 2 ).asInt() );
    } catch( Exception exception ) {
      String message = "Could not create RGB for: " + value;
      throw new IllegalArgumentException( message, exception );
    }
  }

}
