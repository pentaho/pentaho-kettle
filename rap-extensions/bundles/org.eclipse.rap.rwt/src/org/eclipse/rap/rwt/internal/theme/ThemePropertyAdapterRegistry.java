/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.internal.theme.CssAnimation.Animation;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.theme.BoxDimensions;


public final class ThemePropertyAdapterRegistry {

  static interface ThemePropertyAdapter {

    /**
     * The slot in the client's theme store to write the value into or <code>null</code> if no value
     * needs to be written.
     */
    String getSlot( CssValue value );

    /**
     * The id that references the property in the client's theme store of the value itself if no
     * translation is needed.
     */
    String getKey( CssValue value );

    /**
     * The value to write into the client's theme store or <code>null</code> if no value needs to be
     * written.
     */
    JsonValue getValue( CssValue value );
  }

  static class DirectPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return value.toDefaultString();
    }

    @Override
    public String getSlot( CssValue value ) {
      return null;
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      return null;
    }
  }

  static class DimensionPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "dimensions";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      if( CssDimension.AUTO.equals( value ) ) {
        return JsonValue.valueOf( "auto" );
      }
      return JsonValue.valueOf( ( ( CssDimension )value ).value );
    }
  }

  static class BoxDimensionsPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "boxdims";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      BoxDimensions boxdim = ( ( CssBoxDimensions )value ).dimensions;
      JsonArray result = new JsonArray();
      result.add( boxdim.top );
      result.add( boxdim.right );
      result.add( boxdim.bottom );
      result.add( boxdim.left );
      return result;
    }
  }

  static class FontPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "fonts";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssFont font = ( CssFont )value;
      JsonObject result = new JsonObject();
      result.add( "family", JsonUtil.createJsonArray( font.family ) );
      result.add( "size", font.size );
      result.add( "bold", font.bold );
      result.add( "italic", font.italic );
      return result;
    }
  }

  static class ImagePropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return createKey( value );
    }

    @Override
    public String getSlot( CssValue value ) {
      String result;
      CssImage image = ( CssImage )value;
      if( image.isGradient() ) {
        result = "gradients";
      } else {
        result = "images";
      }
      return result;
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssImage image = ( CssImage )value;
      JsonValue result = null;
      if( image.isGradient() ) {
        JsonObject gradientObject = null;
        gradientObject = new JsonObject();
        JsonValue percents = createJsonArray( image.gradientPercents );
        gradientObject.add( "percents", percents );
        JsonValue colors = createJsonArray( image.gradientColors );
        gradientObject.add( "colors", colors );
        gradientObject.add( "vertical", image.vertical );
        result = gradientObject;
      } else if( !image.none ) {
        Size imageSize = image.getSize();
        result = new JsonArray().add( imageSize.width ).add( imageSize.height );
      }
      return result;
    }

    private static String createKey( CssValue value ) {
      String result = Integer.toHexString( value.hashCode() );
      CssImage image = ( CssImage )value;
      if( image.path != null ) {
        int index = image.path.lastIndexOf( '.' );
        if( index >= 0 ) {
          result = result + image.path.substring( index );
        }
      }
      return result;
    }
  }

  static class ColorPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "colors";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssColor color = ( CssColor )value;
      JsonValue result;
      if( color.isTransparent() ) {
        result = JsonValue.valueOf( "undefined" );
      } else {
        JsonArray colorArray = new JsonArray();
        colorArray.add( color.red );
        colorArray.add( color.green );
        colorArray.add( color.blue );
        colorArray.add( color.alpha );
        result = colorArray;
      }
      return result;
    }
  }

  static class BorderPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "borders";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssBorder border = ( CssBorder )value;
      JsonObject result = new JsonObject();
      result.add( "width", border.width );
      result.add( "style", border.style );
      result.add( "color", border.color == null ? null : border.color.toDefaultString() );
      return result;
    }
  }

  static class CursorPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return createKey( value );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "cursors";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssCursor cursor = ( CssCursor )value;
      JsonValue result;
      if( cursor.isCustomCursor() ) {
        result = JsonValue.NULL;
      } else {
        result = JsonValue.valueOf( cursor.value );
      }
      return result;
    }

    private static String createKey( CssValue value ) {
      String result = Integer.toHexString( value.hashCode() );
      CssCursor cursor = ( CssCursor )value;
      if( cursor.isCustomCursor() ) {
        int index = cursor.value.lastIndexOf( '.' );
        if( index >= 0 ) {
          result = result + cursor.value.substring( index );
        }
      }
      return result;
    }
  }

  static class AnimationPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "animations";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssAnimation animation = ( CssAnimation )value;
      JsonObject result = new JsonObject();
      for( int j = 0; j < animation.animations.length; j++ ) {
        Animation currentAnimation = animation.animations[ j ];
        JsonArray currentAnimationArray = new JsonArray();
        currentAnimationArray.add( currentAnimation.duration );
        String timingFunction = CssAnimation.toCamelCaseString( currentAnimation.timingFunction );
        currentAnimationArray.add( timingFunction );
        result.add( currentAnimation.name, currentAnimationArray );
      }
      return result;
    }
  }

  static class ShadowPropertyAdapter implements ThemePropertyAdapter {

    @Override
    public String getKey( CssValue value ) {
      return Integer.toHexString( value.hashCode() );
    }

    @Override
    public String getSlot( CssValue value ) {
      return "shadows";
    }

    @Override
    public JsonValue getValue( CssValue value ) {
      CssShadow shadow = ( CssShadow )value;
      JsonValue result;
      if( shadow.equals( CssShadow.NONE ) ) {
        result = JsonValue.NULL;
      } else {
        JsonArray array = new JsonArray();
        array.add( shadow.inset );
        array.add( shadow.offsetX );
        array.add( shadow.offsetY );
        array.add( shadow.blur );
        array.add( shadow.spread );
        array.add( shadow.color );
        array.add( shadow.opacity );
        result = array;
      }
      return result;
    }
  }

  private static final Object LOCK = new Object();

  private static final String ATTR_NAME
    = ThemePropertyAdapterRegistry.class.getName() + "#instance";

  public static ThemePropertyAdapterRegistry getInstance( ApplicationContext applicationContext ) {
    ThemePropertyAdapterRegistry result;
    synchronized( LOCK ) {
      result = ( ThemePropertyAdapterRegistry )applicationContext.getAttribute( ATTR_NAME );
      if( result == null ) {
        result = new ThemePropertyAdapterRegistry();
        applicationContext.setAttribute( ATTR_NAME, result );
      }
    }
    return result;
  }

  private final Map<Class<? extends CssValue>,ThemePropertyAdapter> map;

  private ThemePropertyAdapterRegistry() {
    map = new HashMap<>();
    map.put( CssAnimation.class, new AnimationPropertyAdapter() );
    map.put( CssBorder.class, new BorderPropertyAdapter() );
    map.put( CssBoxDimensions.class, new BoxDimensionsPropertyAdapter() );
    map.put( CssColor.class, new ColorPropertyAdapter() );
    map.put( CssCursor.class, new CursorPropertyAdapter() );
    map.put( CssDimension.class, new DimensionPropertyAdapter() );
    map.put( CssFloat.class, new DirectPropertyAdapter() );
    map.put( CssFont.class, new FontPropertyAdapter() );
    map.put( CssIdentifier.class, new DirectPropertyAdapter() );
    map.put( CssImage.class, new ImagePropertyAdapter() );
    map.put( CssShadow.class, new ShadowPropertyAdapter() );
  }

  ThemePropertyAdapter getPropertyAdapter( Class<?> key ) {
    return map.get( key );
  }

}
