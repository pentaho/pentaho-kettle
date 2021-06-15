/*******************************************************************************
 * Copyright (c) 2008, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.rap.rwt.internal.theme.CssAnimation;
import org.eclipse.rap.rwt.internal.theme.CssBorder;
import org.eclipse.rap.rwt.internal.theme.CssBoxDimensions;
import org.eclipse.rap.rwt.internal.theme.CssColor;
import org.eclipse.rap.rwt.internal.theme.CssCursor;
import org.eclipse.rap.rwt.internal.theme.CssDimension;
import org.eclipse.rap.rwt.internal.theme.CssFloat;
import org.eclipse.rap.rwt.internal.theme.CssFont;
import org.eclipse.rap.rwt.internal.theme.CssIdentifier;
import org.eclipse.rap.rwt.internal.theme.CssImage;
import org.eclipse.rap.rwt.internal.theme.CssShadow;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.w3c.css.sac.LexicalUnit;


public final class PropertyResolver {

  private static final String BOLD = "bold";
  private static final String ITALIC = "italic";
  private static final String NORMAL = "normal";
  // No border; the computed border width is zero.
  private static final String NONE = "none";
  // Same as 'none', except in terms of border conflict resolution for table
  // elements.
  private static final String HIDDEN = "hidden";
  // The border is a series of dots.
  private static final String DOTTED = "dotted";
  // The border is a series of short line segments.
  private static final String DASHED = "dashed";
  // The border is a single line segment.
  private static final String SOLID = "solid";
  // The border is two solid lines. The sum of the two lines and the space
  // between them equals the value of 'border-width'.
  private static final String DOUBLE = "double";
  // The border looks as though it were carved into the canvas.
  private static final String GROOVE = "groove";
  // The opposite of 'groove': the border looks as though it were coming out of
  // the canvas.
  private static final String RIDGE = "ridge";
  // The border makes the box look as though it were embedded in the canvas.
  private static final String INSET = "inset";
  // The opposite of 'inset': the border makes the box look as though it were
  // coming out of the canvas.
  private static final String OUTSET = "outset";
  /** A thin border. */
  private static final String THIN = "thin";
  /** A medium border. */
  private static final String MEDIUM = "medium";
  /** A thick border. */
  private static final String THICK = "thick";
  private static final String TRANSPARENT = "transparent";
  private static final String AUTO = "auto";
  private static final Map<String,NamedColor> NAMED_COLORS = new HashMap<>();
  private static final List<String> BORDER_STYLES = new ArrayList<>();
  /** Width value for "thin" identifier. */
  static final int THIN_VALUE = 1;
  /** Width value for "medium" identifier. */
  static final int MEDIUM_VALUE = 3;
  /** Width value for "thick" identifier. */
  static final int THICK_VALUE = 5;
  static {
    // register 16 standard HTML colors
    NAMED_COLORS.put( "black", new NamedColor( 0, 0, 0 ) );
    NAMED_COLORS.put( "gray", new NamedColor( 128, 128, 128 ) );
    NAMED_COLORS.put( "silver", new NamedColor( 192, 192, 192 ) );
    NAMED_COLORS.put( "white", new NamedColor( 255, 255, 255 ) );
    NAMED_COLORS.put( "maroon", new NamedColor( 128, 0, 0 ) );
    NAMED_COLORS.put( "red", new NamedColor( 255, 0, 0 ) );
    NAMED_COLORS.put( "purple", new NamedColor( 128, 0, 128 ) );
    NAMED_COLORS.put( "fuchsia", new NamedColor( 255, 0, 255 ) );
    NAMED_COLORS.put( "green", new NamedColor( 0, 128, 0 ) );
    NAMED_COLORS.put( "lime", new NamedColor( 0, 255, 0 ) );
    NAMED_COLORS.put( "navy", new NamedColor( 0, 0, 128 ) );
    NAMED_COLORS.put( "blue", new NamedColor( 0, 0, 255 ) );
    NAMED_COLORS.put( "olive", new NamedColor( 128, 128, 0 ) );
    NAMED_COLORS.put( "yellow", new NamedColor( 255, 255, 0 ) );
    NAMED_COLORS.put( "teal", new NamedColor( 0, 128, 128 ) );
    NAMED_COLORS.put( "aqua", new NamedColor( 0, 255, 255 ) );
    // register border styles
    BORDER_STYLES.add( NONE );
    BORDER_STYLES.add( HIDDEN );
    BORDER_STYLES.add( DOTTED );
    BORDER_STYLES.add( DASHED );
    BORDER_STYLES.add( SOLID );
    BORDER_STYLES.add( DOUBLE );
    BORDER_STYLES.add( GROOVE );
    BORDER_STYLES.add( RIDGE );
    BORDER_STYLES.add( INSET );
    BORDER_STYLES.add( OUTSET );
  }

  private final StylePropertyMap resolvedProperties;

  public PropertyResolver() {
    resolvedProperties = new StylePropertyMap();
  }

  public void resolveProperty( String name, LexicalUnit unit, ResourceLoader loader ) {
    if( isBorderProperty( name ) ) {
      CssBorder border = readBorder( unit );
      resolvedProperties.setProperty( "border-top", border );
      resolvedProperties.setProperty( "border-right", border );
      resolvedProperties.setProperty( "border-bottom", border );
      resolvedProperties.setProperty( "border-left", border );
    } else if( isBorderEdgeProperty( name ) ) {
      resolvedProperties.setProperty( name, readBorder( unit ) );
    } else if( isBoxDimensionProperty( name ) ) {
      resolvedProperties.setProperty( name, readBoxDimensions( unit ) );
    } else if( isColorProperty( name ) ) {
      resolvedProperties.setProperty( name, readColor( unit ) );
    } else if( isDimensionProperty( name ) ) {
      resolvedProperties.setProperty( name, readDimension( unit ) );
    } else if( isFontProperty( name ) ) {
      resolvedProperties.setProperty( name, readFont( unit ) );
    } else if( isImageProperty( name ) ) {
      resolvedProperties.setProperty( name, readBackgroundImage( unit, loader ) );
    } else if( isBackgroundRepeatProperty( name ) ) {
      resolvedProperties.setProperty( name, readBackgroundRepeat( unit ) );
    } else if( isBackgroundPositionProperty( name ) ) {
      resolvedProperties.setProperty( name, readBackgroundPosition( unit ) );
    } else if( isTextDecorationProperty( name ) ) {
      resolvedProperties.setProperty( name, readTextDecoration( unit ) );
    } else if( isTextOverflowProperty( name ) ) {
      resolvedProperties.setProperty( name, readTextOverflow( unit ) );
    } else if( isTextAlignProperty( name ) ) {
      resolvedProperties.setProperty( name, readTextAlign( unit ) );
    } else if( isCursorProperty( name ) ) {
      resolvedProperties.setProperty( name, readCursor( unit, loader ) );
    } else if( isFloatProperty( name ) ) {
      resolvedProperties.setProperty( name, readFloat( unit ) );
    } else if( isAnimationProperty( name ) ) {
      resolvedProperties.setProperty( name, readAnimation( unit ) );
    } else if( isShadowProperty( name ) ) {
      resolvedProperties.setProperty( name, readShadow( unit ) );
    } else {
      throw new IllegalArgumentException( "Unknown property " + name );
    }
  }

  public StylePropertyMap getResolvedProperties() {
    return resolvedProperties;
  }

  static boolean isColorProperty( String property ) {
    return "color".equals( property ) || property.endsWith( "-color" );
  }

  static CssColor readColor( LexicalUnit unit ) {
    CssColor result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_RGBCOLOR ) {
      result = readColorRgb( unit );
    } else if( type == LexicalUnit.SAC_FUNCTION && "rgba".equals( unit.getFunctionName() )  ) {
      result = readColorRgba( unit );
    } else  if( type == LexicalUnit.SAC_FUNCTION && "rgb".equals( unit.getFunctionName() ) ) {
      throw new IllegalArgumentException( "Failed to parse rgb() function" );
    } else if( type == LexicalUnit.SAC_IDENT ) {
      result = readColorNamed( unit );
    } else if( type == LexicalUnit.SAC_INHERIT ) {
      result = CssColor.TRANSPARENT;
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse color " + toString( unit ) );
    }
    return result;
  }

  private static CssColor readColorNamed( LexicalUnit unit ) {
    CssColor result = null;
    String string = unit.getStringValue();
    String lowerCaseString = string.toLowerCase( Locale.ENGLISH );
    if( TRANSPARENT.equals( string ) ) {
      result = CssColor.TRANSPARENT;
    } else if( NAMED_COLORS.containsKey( lowerCaseString ) ) {
      NamedColor color = NAMED_COLORS.get( lowerCaseString );
      result = CssColor.create( color.red, color.green, color.blue );
    }
    return result;
  }

  private static CssColor readColorRgb( LexicalUnit unit ) {
    CssColor result = null;
    // The parser ensures that we have exactly three parameters for this type
    LexicalUnit redParam = unit.getParameters();
    LexicalUnit greenParam = redParam.getNextLexicalUnit().getNextLexicalUnit();
    LexicalUnit blueParam = greenParam.getNextLexicalUnit().getNextLexicalUnit();
    short valueType = redParam.getLexicalUnitType();
    if(    greenParam.getLexicalUnitType() == valueType
        || blueParam.getLexicalUnitType() == valueType )
    {
      if( valueType == LexicalUnit.SAC_INTEGER ) {
        int red = normalizeRGBValue( redParam.getIntegerValue() );
        int green = normalizeRGBValue( greenParam.getIntegerValue() );
        int blue = normalizeRGBValue( blueParam.getIntegerValue() );
        result = CssColor.create( red, green, blue );
      } else if( valueType == LexicalUnit.SAC_PERCENTAGE ) {
        float redPercent = normalizePercentValue( redParam.getFloatValue() );
        float greenPercent = normalizePercentValue( greenParam.getFloatValue() );
        float bluePercent = normalizePercentValue( blueParam.getFloatValue() );
        int red = ( int )( 255 * redPercent / 100 );
        int green = ( int )( 255 * greenPercent / 100 );
        int blue = ( int )( 255 * bluePercent / 100 );
        result = CssColor.create( red, green, blue );
      }
    }
    return result;
  }

  static CssColor readColorRgba( LexicalUnit unit ) {
    CssColor result = null;
    int[] values = new int[ 3 ];
    float alpha = 1f;
    short type = unit.getLexicalUnitType();
    LexicalUnit nextUnit = unit.getParameters();
    boolean ok = nextUnit != null;
    boolean mixedTypes = false;
    short previousType = -1;
    int pos = 0;
    while( ok ) {
      type = nextUnit.getLexicalUnitType();
      if( pos == 0 || pos == 2 || pos == 4 ) {
        // color number
        if( type == LexicalUnit.SAC_INTEGER ) {
          values[ pos / 2 ] = normalizeRGBValue( nextUnit.getIntegerValue() );
        } else if( type == LexicalUnit.SAC_PERCENTAGE ) {
          float percentValue = normalizePercentValue( nextUnit.getFloatValue() );
          values[ pos / 2 ] = ( int )( 255 * percentValue / 100 );
        } else {
          ok = false;
        }
        mixedTypes = previousType != -1 && previousType != type;
        previousType = type;
      } else if( pos == 1 || pos == 3 || pos == 5 ) {
        // comma
        if( type != LexicalUnit.SAC_OPERATOR_COMMA ) {
          ok = false;
        }
      } else if( pos == 6 ) {
        // alpha number
        if( type == LexicalUnit.SAC_REAL ) {
          alpha = normalizeAlphaValue( nextUnit.getFloatValue() );
        } else {
          ok = false;
        }
      }
      pos++;
      nextUnit = nextUnit.getNextLexicalUnit();
      ok &= nextUnit != null && pos < 7 && !mixedTypes;
    }
    if( pos == 7 ) {
      result = CssColor.create( values[ 0 ], values[ 1 ], values[ 2 ], alpha );
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse rgba color" );
    }
    return result;
  }

  static boolean isDimensionProperty( String property ) {
    return    "spacing".equals( property )
           || "width".equals( property )
           || "height".equals( property )
           || "min-height".equals( property );
  }

  static CssDimension readDimension( LexicalUnit unit ) {
    CssDimension result = null;
    if( unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT ) {
      if( AUTO.equals( unit.getStringValue() ) ) {
        result = CssDimension.AUTO;
      }
    } else {
      Integer length = readSingleLengthUnit( unit );
      if( length != null ) {
        result = CssDimension.create( length.intValue() );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse dimension " + toString( unit ) );
    }
    return result;
  }

  static boolean isBorderProperty( String property ) {
    return "border".equals( property );
  }

  static boolean isBorderEdgeProperty( String property ) {
    return    "border-top".equals( property )
           || "border-right".equals( property )
           || "border-bottom".equals( property )
           || "border-left".equals( property );
  }

  static CssBorder readBorder( LexicalUnit unit ) {
    CssBorder result = null;
    CssColor color = null;
    String style = null;
    int width = -1;
    LexicalUnit nextUnit = unit;
    boolean consumed = false;
    while( nextUnit != null ) {
      consumed = false;
      if( !consumed && width == -1 ) {
        width = readBorderWidth( nextUnit );
        consumed |= width != -1;
      }
      if( !consumed && style == null ) {
        style = readBorderStyle( nextUnit );
        consumed |= style != null;
      }
      if( !consumed && color == null ) {
        color = readColor( nextUnit );
        consumed |= color != null;
      }
      nextUnit = consumed ? nextUnit.getNextLexicalUnit() : null;
    }
    if( consumed ) {
      result = CssBorder.create( width, style, color );
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse border " + toString( unit ) );
    }
    return result;
  }

  static String readBorderStyle( LexicalUnit unit ) {
    String result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String string = unit.getStringValue();
      if( BORDER_STYLES.contains( string ) ) {
        result = string;
      }
    }
    return result;
  }

  static int readBorderWidth( LexicalUnit unit ) {
    int result = -1;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String string = unit.getStringValue();
      if( THIN.equals( string ) ) {
        result = THIN_VALUE;
      } else if( MEDIUM.equals( string ) ) {
        result = MEDIUM_VALUE;
      } else if( THICK.equals( string ) ) {
        result = THICK_VALUE;
      }
    } else if( type == LexicalUnit.SAC_INTEGER ) {
      int value = unit.getIntegerValue();
      if( value == 0 ) {
        result = 0;
      }
    } else if( type == LexicalUnit.SAC_PIXEL ) {
      float value = unit.getFloatValue();
      if( value >= 0f ) {
        result = Math.round( value );
      }
    }
    return result;
  }

  static boolean isBoxDimensionProperty( String property ) {
    return    "padding".equals( property )
           || "margin".equals( property )
           || "border-radius".equals( property );
  }

  static CssBoxDimensions readBoxDimensions( LexicalUnit unit ) {
    CssBoxDimensions result = null;
    Integer value1 = readSingleLengthUnit( unit );
    if( value1 != null ) {
      int top, right, left, bottom;
      top = right = bottom = left = value1.intValue();
      LexicalUnit nextUnit = unit.getNextLexicalUnit();
      boolean ok = true;
      int pos = 1;
      while( nextUnit != null && ok ) {
        pos++;
        Integer nextValue = readSingleLengthUnit( nextUnit );
        ok &= nextValue != null && pos <= 4;
        if( ok ) {
          if( pos == 2 ) {
            right = left = nextValue.intValue();
          } else if( pos == 3 ) {
            bottom = nextValue.intValue();
          } else if( pos == 4 ) {
            left = nextValue.intValue();
          }
        }
        nextUnit = nextUnit.getNextLexicalUnit();
      }
      ok &= nextUnit == null;
      if( ok ) {
        result = CssBoxDimensions.create( top, right, bottom, left );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse box dimensions " + toString( unit ) );
    }
    return result;
  }

  static boolean isFontProperty( String property ) {
    return "font".equals( property ) || property.endsWith( "-fontlist" );
  }

  // The format of a URI value is 'url(' followed by optional whitespace
  // followed by an optional single quote (') or double quote (") character
  // followed by the URI itself, followed by an optional single quote (') or
  // double quote (") character followed by optional whitespace followed by ')'.
  // The two quote characters must be the same.
  static CssFont readFont( LexicalUnit unit ) {
    CssFont result = null;
    String[] family = null;
    String style = null;
    String weight = null;
    int size = -1;
    boolean consumed = false;
    boolean consumedSize = false;
    boolean consumedFamily = false;
    LexicalUnit nextUnit = unit;
    while( nextUnit != null && !consumedFamily ) {
      consumed = false;
      if( !consumed && !consumedSize && style == null ) {
        style = readFontStyle( nextUnit );
        consumed |= style != null;
      }
      if( !consumed && !consumedSize && weight == null ) {
        weight = readFontWeight( nextUnit );
        consumed |= weight != null;
      }
      if( !consumed && !consumedFamily && size == -1 ) {
        size = readFontSize( nextUnit );
        consumedSize = size != -1;
        consumed |= consumedSize;
      }
      if( !consumed && consumedSize && family == null ) {
        family = readFontFamily( nextUnit );
        consumedFamily = family != null;
        consumed |= consumedFamily;
      }
      nextUnit = consumed ? nextUnit.getNextLexicalUnit() : null;
    }
    if( consumed && consumedSize && consumedFamily ) {
      boolean bold = BOLD.equals( weight );
      boolean italic = ITALIC.equals( style );
      result = CssFont.create( family, size, bold, italic );
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse font " + toString( unit ) );
    }
    return result;
  }

  static String readFontStyle( LexicalUnit unit ) {
    String result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if( NORMAL.equals( value ) ) {
        result = value;
      } else if( ITALIC.equals( value ) ) {
        result = value;
      }
    }
    return result;
  }

  static String readFontWeight( LexicalUnit unit ) {
    String result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if( NORMAL.equals( value ) ) {
        result = value;
      } else if( BOLD.equals( value ) ) {
        result = value;
      }
    }
    return result;
  }

  static int readFontSize( LexicalUnit unit ) {
    int result = -1;
    Integer length = readSingleLengthUnit( unit );
    if( length != null ) {
      int value = length.intValue();
      if( value >= 0 ) {
        result = value;
      }
    }
    return result;
  }

  static String[] readFontFamily( LexicalUnit unit ) {
    List<String> list = new ArrayList<>();
    LexicalUnit nextUnit = unit;
    boolean ok = true;
    String buffer = "";
    while( nextUnit != null && ok ) {
      short type = nextUnit.getLexicalUnitType();
      if( type == LexicalUnit.SAC_STRING_VALUE || type == LexicalUnit.SAC_IDENT ) {
        if( buffer.length() > 0 ) {
          buffer += " ";
        }
        buffer += nextUnit.getStringValue();
      } else if( type == LexicalUnit.SAC_OPERATOR_COMMA ) {
        if( buffer.length() > 0 ) {
          list.add( buffer );
        } else {
          ok = false;
        }
        buffer = "";
      }
      nextUnit = nextUnit.getNextLexicalUnit();
    }
    String[] result = null;
    if( buffer.length() > 0 ) {
      list.add( buffer );
      result = new String[ list.size() ];
      list.toArray( result );
    }
    return result;
  }

  static boolean isImageProperty( String property ) {
    return property.endsWith( "-image" );
  }

  static CssImage readBackgroundImage( LexicalUnit unit, ResourceLoader loader ) {
    CssImage result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_URI ) {
      String value = unit.getStringValue();
      result = CssImage.valueOf( value, loader );
    } else if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if( NONE.equals( value ) ) {
        result = CssImage.NONE;
      }
    } else if( type == LexicalUnit.SAC_FUNCTION ) {
      String function = unit.getFunctionName();
      if( "gradient".equals( function ) ) {
        result = readGradient( unit );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse image " + toString( unit ) );
    }
    return result;
  }

  static CssImage readGradient( LexicalUnit unit ) {
    CssImage result = null;
    boolean vertical;
    LexicalUnit nextUnit = unit.getParameters();
    String gradientType = readGradientType( nextUnit );
    if( !"linear".equals( gradientType ) ) {
      String msg = "Invalid value for background-image gradient type: " + gradientType;
      throw new IllegalArgumentException( msg );
    }
    nextUnit = nextUnit.getNextLexicalUnit();
    if( !checkComma( nextUnit) ) {
      throw new IllegalArgumentException( "Failed to parse background-image gradient." );
    }
    nextUnit = nextUnit.getNextLexicalUnit();
    String[] startPoint = readGradientPoint( nextUnit );
    if( !( "left".equals( startPoint[ 0 ] ) && "top".equals( startPoint[ 1 ] ) ) ) {
      String msg = "Invalid value for background-image gradient start point: "
                   + startPoint[ 0 ]
                   + " "
                   + startPoint[ 1 ];
      throw new IllegalArgumentException( msg );
    }
    nextUnit = nextUnit.getNextLexicalUnit();
    nextUnit = nextUnit.getNextLexicalUnit();
    if( !checkComma( nextUnit) ) {
      throw new IllegalArgumentException( "Failed to parse background-image gradient." );
    }
    nextUnit = nextUnit.getNextLexicalUnit();
    String[] endPoint = readGradientPoint( nextUnit );
    if( "left".equals( endPoint[ 0 ] ) && "bottom".equals( endPoint[ 1 ] ) ) {
      vertical = true;
    } else if( "right".equals( endPoint[ 0 ] ) && "top".equals( endPoint[ 1 ] ) ) {
      vertical = false;
    } else {
      String msg = "Invalid value for background-image gradient end point: "
                 + endPoint[ 0 ]
                 + " "
                 + endPoint[ 1 ];
      throw new IllegalArgumentException( msg );
    }
    nextUnit = nextUnit.getNextLexicalUnit();
    nextUnit = nextUnit.getNextLexicalUnit();
    TreeMap<Float, String> gradient = readGradientColorsPercents( nextUnit );
    if( gradient.size() > 0 ) {
      gradient = normalizeGradientValue( gradient );
      String[] gradientColors = getGradientColors( gradient );
      float[] gradientPercents = getGradientPercents( gradient );
      result = CssImage.createGradient( gradientColors, gradientPercents, vertical );
    }
    return result;
  }

  static String readGradientType( LexicalUnit unit ) {
    String result = null;
    if( unit != null && unit.getLexicalUnitType() == LexicalUnit.SAC_IDENT ) {
      result = unit.getStringValue();
    }
    return result;
  }

  static String[] readGradientPoint( LexicalUnit unit ) {
    String[] result = new String[ 2 ];
    LexicalUnit x = unit;
    LexicalUnit y = null;
    if( unit != null ) {
      y = unit.getNextLexicalUnit();
    }
    if( x != null && y != null ) {
      short xType = x.getLexicalUnitType();
      short yType = y.getLexicalUnitType();
      if( xType == LexicalUnit.SAC_IDENT && yType == LexicalUnit.SAC_IDENT ) {
        result[ 0 ] = x.getStringValue();
        result[ 1 ] = y.getStringValue();
      } else if( xType == LexicalUnit.SAC_INTEGER && yType == LexicalUnit.SAC_INTEGER ) {
        result[ 0 ] = Integer.toString( x.getIntegerValue() );
        result[ 1 ] = Integer.toString( y.getIntegerValue() );
      }
    }
    return result;
  }

  static TreeMap<Float, String> readGradientColorsPercents( LexicalUnit unit ) {
    TreeMap<Float, String> result = new TreeMap<>();
    LexicalUnit nextUnit = unit;
    while( nextUnit != null ) {
      Float percent = null;
      String color = null;
      nextUnit = nextUnit.getNextLexicalUnit();
      if(    nextUnit != null
          && nextUnit.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION )
      {
        String function = nextUnit.getFunctionName();
        if( "from".equals( function ) ) {
          percent = Float.valueOf( 0f );
          LexicalUnit colorUnit = nextUnit.getParameters();
          color = readGradientColor( colorUnit );
        } else if( "to".equals( function ) ) {
          percent = Float.valueOf( 100f );
          LexicalUnit colorUnit = nextUnit.getParameters();
          color = readGradientColor( colorUnit );
        } else if( "color-stop".equals( function ) ) {
          LexicalUnit percentUnit = nextUnit.getParameters();
          percent = readGradientPercent( percentUnit );
          LexicalUnit colorUnit = percentUnit.getNextLexicalUnit().getNextLexicalUnit();
          color = readGradientColor( colorUnit );
        } else {
          String msg = "Invalid value for background-image gradient: " + function;
          throw new IllegalArgumentException( msg );
        }
        nextUnit = nextUnit.getNextLexicalUnit();
      }
      if( percent != null && color != null ) {
        result.put( percent, color );
      }
    }
    return result;
  }

  static String readGradientColor( LexicalUnit unit ) {
    CssColor result = readColor( unit );
    return result != null ? result.toDefaultString() : null;
  }

  static Float readGradientPercent( LexicalUnit unit ) {
    Float result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_PERCENTAGE ) {
      result = Float.valueOf( normalizePercentValue( unit.getFloatValue() ) );
    } else if( type == LexicalUnit.SAC_REAL ) {
      result = Float.valueOf( normalizePercentValue( unit.getFloatValue() * 100 ) );
    }
    return result;
  }

  static boolean isBackgroundRepeatProperty( String property ) {
    return "background-repeat".equals( property );
  }

  static CssIdentifier readBackgroundRepeat( LexicalUnit unit ) {
    CssIdentifier result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if(    "repeat".equals( value )
          || "repeat-x".equals( value )
          || "repeat-y".equals( value )
          || "no-repeat".equals( value ) )
      {
        result = new CssIdentifier( value );
      } else {
         String msg = "Invalid value for background-repeat: " + value;
         throw new IllegalArgumentException( msg );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse background-repeat " + toString( unit ) );
    }
    return result;
  }

  static boolean isBackgroundPositionProperty( String property ) {
    return "background-position".equals( property );
  }

  static CssIdentifier readBackgroundPosition( LexicalUnit unit ) {
    CssIdentifier result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      StringBuilder buffer = new StringBuilder();
      buffer.append( unit.getStringValue() );
      LexicalUnit nextUnit = unit.getNextLexicalUnit();
      if( nextUnit != null && nextUnit.getStringValue() != null ) {
        buffer.append( " " );
        buffer.append( nextUnit.getStringValue() );
      } else {
        buffer.append( " center" );
      }
      String value = buffer.toString();
      if(    "left top".equals( value )
          || "left center".equals( value )
          || "left bottom".equals( value )
          || "right top".equals( value )
          || "right center".equals( value )
          || "right bottom".equals( value )
          || "center top".equals( value )
          || "center center".equals( value )
          || "center bottom".equals( value ) )
      {
        result = new CssIdentifier( value );
      } else {
        String msg = "Invalid value for background-position: " + value;
        throw new IllegalArgumentException( msg );
      }
    }
    if( result == null ) {
      String msg = "Failed to parse background-position " + toString( unit );
      throw new IllegalArgumentException( msg );
    }
    return result;
  }

  static boolean isTextDecorationProperty( String property ) {
    return "text-decoration".equals( property );
  }

  static CssIdentifier readTextDecoration( LexicalUnit unit ) {
    CssIdentifier result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if(    "underline".equals( value )
          || "overline".equals( value )
          || "line-through".equals( value )
          || "none".equals( value ) )
      {
        result = new CssIdentifier( value );
      } else {
        throw new IllegalArgumentException( "Invalid value for text-decoration: " + value );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse text-decoration " + toString( unit ) );
    }
    return result;
  }

  static boolean isTextOverflowProperty( String property ) {
    return "text-overflow".equals( property );
  }

  static CssIdentifier readTextOverflow( LexicalUnit unit ) {
    CssIdentifier result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if( "clip".equals( value ) || "ellipsis".equals( value ) ) {
        result = new CssIdentifier( value );
      } else {
        throw new IllegalArgumentException( "Invalid value for text-overflow: " + value );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse text-overflow " + toString( unit ) );
    }
    return result;
  }

  static boolean isTextAlignProperty( String property ) {
    return "text-align".equals( property );
  }

  static CssIdentifier readTextAlign( LexicalUnit unit ) {
    CssIdentifier result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      if( "left".equals( value ) || "right".equals( value ) || "center".equals( value ) ) {
        result = new CssIdentifier( value );
      } else {
        throw new IllegalArgumentException( "Invalid value for text-align: " + value );
      }
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse text-align " + toString( unit ) );
    }
    return result;
  }

  static boolean isCursorProperty( String property ) {
    return "cursor".equals( property );
  }

  static CssCursor readCursor( LexicalUnit unit, ResourceLoader loader ) {
    CssCursor result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_URI ) {
      String value = unit.getStringValue();
      result = CssCursor.valueOf( value, loader );
    } else if( type == LexicalUnit.SAC_IDENT ) {
      String value = unit.getStringValue();
      result = CssCursor.valueOf( value );
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse cursor " + toString( unit ) );
    }
    return result;
  }

  static boolean isFloatProperty( String property ) {
    return "opacity".equals( property );
  }

  static CssFloat readFloat( LexicalUnit unit ) {
    CssFloat result;
    if(    unit.getLexicalUnitType() == LexicalUnit.SAC_REAL
        || unit.getLexicalUnitType() == LexicalUnit.SAC_INTEGER )
    {
      float value;
      if( unit.getLexicalUnitType() == LexicalUnit.SAC_INTEGER ) {
        value = unit.getIntegerValue();
      } else {
        value = unit.getFloatValue();
      }
      if( value >= 0 && value <= 1 ) {
        result = CssFloat.create( value );
      } else {
        throw new IllegalArgumentException( "Float out of bounds: " + value );
      }
    } else {
      String msg = "Failed to parse float " + toString( unit );
      throw new IllegalArgumentException( msg );
    }
    return result;
  }

  static boolean isAnimationProperty( String property ) {
    return "animation".equals( property );
  }

  static CssAnimation readAnimation( LexicalUnit unit ) {
    CssAnimation result = new CssAnimation();
    LexicalUnit nextUnit = unit;
    short type = nextUnit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = nextUnit.getStringValue();
      if( "none".equals( value ) ) {
        nextUnit = null;
      }
    }
    while( nextUnit != null ) {
      String name;
      int duration;
      String timingFunction;
      type = nextUnit.getLexicalUnitType();
      if( type == LexicalUnit.SAC_IDENT ) {
        name = nextUnit.getStringValue();
      } else {
        String msg = "Invalid value for animation name: " + toString( nextUnit );
        throw new IllegalArgumentException( msg );
      }
      nextUnit = nextUnit.getNextLexicalUnit();
      if( nextUnit == null ) {
        String msg = "Missing value for animation duration.";
        throw new IllegalArgumentException( msg );
      }
      type = nextUnit.getLexicalUnitType();
      if( type == LexicalUnit.SAC_SECOND ) {
        duration =  Math.round( nextUnit.getFloatValue() * 1000 );
      } else if( type == LexicalUnit.SAC_MILLISECOND ) {
        duration = Math.round( nextUnit.getFloatValue() );
      } else {
        String msg = "Invalid value for animation duration: " + toString( nextUnit );
        throw new IllegalArgumentException( msg );
      }
      nextUnit = nextUnit.getNextLexicalUnit();
      if( nextUnit == null ) {
        String msg = "Missing value for animation timing function.";
        throw new IllegalArgumentException( msg );
      }
      type = nextUnit.getLexicalUnitType();
      if( type == LexicalUnit.SAC_IDENT ) {
        timingFunction = nextUnit.getStringValue();
      } else {
        String msg = "Invalid value for animation timing function: " + toString( nextUnit );
        throw new IllegalArgumentException( msg );
      }
      result.addAnimation( name, duration, timingFunction );
      nextUnit = nextUnit.getNextLexicalUnit();
      if( nextUnit != null ) {
        type = nextUnit.getLexicalUnitType();
        if( type == LexicalUnit.SAC_OPERATOR_COMMA ) {
          nextUnit = nextUnit.getNextLexicalUnit();
        } else {
          String msg = "Failed to parse animation " + toString( nextUnit );
          throw new IllegalArgumentException( msg );
        }
      }
    }
    return result;
  }

  static boolean isShadowProperty( String property ) {
    return "box-shadow".equals( property ) || "text-shadow".equals( property );
  }

  static CssShadow readShadow( LexicalUnit unit ) {
    CssShadow result = null;
    boolean inset = false;
    Integer offsetX = null;
    Integer offsetY = null;
    int blur = 0;
    int spread = 0;
    CssColor color = CssColor.BLACK;
    LexicalUnit nextUnit = unit;
    short type = nextUnit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_IDENT ) {
      String value = nextUnit.getStringValue();
      if( NONE.equals( value ) ) {
        result = CssShadow.NONE;
      } else if( INSET.equals( value ) ) {
        inset = true;
      }
      nextUnit = nextUnit.getNextLexicalUnit();
    }
    if( result == null ) {
      boolean ok = true;
      int pos = 0;
      while( nextUnit != null && ok ) {
        pos++;
        Integer nextValue = readSingleLengthUnit( nextUnit );
        ok &= nextValue != null && pos <= 4;
        if( ok ) {
          if( pos == 1 ) {
            offsetX = nextValue;
          } else if( pos == 2 ) {
            offsetY = nextValue;
          } else if( pos == 3 ) {
            blur = nextValue.intValue();
          } else if( pos == 4 ) {
            spread = nextValue.intValue();
          }
          nextUnit = nextUnit.getNextLexicalUnit();
        }
      }
      if( nextUnit != null ) {
        type = nextUnit.getLexicalUnitType();
        if( type == LexicalUnit.SAC_FUNCTION && "rgba".equals( nextUnit.getFunctionName() ) ) {
          color = readColorRgba( nextUnit );
        } else {
          color = readColor( nextUnit );
        }
      }
    }
    if( offsetX != null && offsetY != null ) {
      result = CssShadow.create( inset,
                                offsetX.intValue(),
                                offsetY.intValue(),
                                blur,
                                spread,
                                color );
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Failed to parse shadow " + toString( unit ) );
    }
    return result;
  }

  private static Integer readSingleLengthUnit( LexicalUnit unit ) {
    Integer result = null;
    short type = unit.getLexicalUnitType();
    if( type == LexicalUnit.SAC_INTEGER ) {
      if( unit.getIntegerValue() == 0 ) {
        result = Integer.valueOf( 0 );
      }
    } else if( type == LexicalUnit.SAC_PIXEL ) {
      result = Integer.valueOf( ( int )unit.getFloatValue() );
    }
    return result;
  }

  private static int normalizeRGBValue( int input ) {
    int result = input;
    if( input < 0 ) {
      result = 0;
    } else if( input > 255 ) {
      result = 255;
    }
    return result;
  }

  private static float normalizeAlphaValue( float input ) {
    float result = input;
    if( input < 0 ) {
      result = 0f;
    } else if( input > 1 ) {
      result = 1f;
    }
    return result;
  }

  private static float normalizePercentValue( float input ) {
    float result = input;
    if( input < 0f ) {
      result = 0f;
    } else if( input > 100f ) {
      result = 100f;
    }
    return result;
  }

  private static TreeMap<Float, String> normalizeGradientValue( TreeMap<Float, String> gradient ) {
    TreeMap<Float, String> result = gradient;
    if( gradient.size() > 0 ) {
      Float zero = Float.valueOf( 0f );
      Float hundred = Float.valueOf( 100f );
      if( gradient.get( zero ) == null ) {
        String firstColor = gradient.get( gradient.firstKey() );
        result.put( zero, firstColor );
      }
      if( gradient.get( hundred ) == null ) {
        String lastColor = gradient.get( gradient.lastKey() );
        result.put( hundred, lastColor );
      }
    }
    return result;
  }

  private static String[] getGradientColors( TreeMap<Float, String> gradient ) {
    Object[] values = gradient.values().toArray();
    String[] result = new String[ values.length ];
    for( int i = 0; i < values.length; i++ ) {
      result[ i ] = ( String )values[ i ];
    }
    return result;
  }

  private static float[] getGradientPercents( TreeMap<Float, String> gradient ) {
    Object[] keys = gradient.keySet().toArray();
    float[] result = new float[ keys.length ];
    for( int i = 0; i < keys.length; i++ ) {
      result[ i ] = ( ( Float )keys[ i ] ).floatValue();
    }
    return result;
  }

  static boolean checkComma( LexicalUnit unit ) {
    boolean result = false;
    if( unit != null && unit.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA ) {
      result = true;
    }
    return result;
  }

  static String toString( LexicalUnit value ) {
    StringBuilder buffer = new StringBuilder();
    short type = value.getLexicalUnitType();
    if( type == LexicalUnit.SAC_ATTR ) {
      buffer.append( "ATTR " + value.getStringValue() );
    } else if(    type == LexicalUnit.SAC_CENTIMETER
               || type == LexicalUnit.SAC_DEGREE
               || type == LexicalUnit.SAC_EM
               || type == LexicalUnit.SAC_EX
               || type == LexicalUnit.SAC_GRADIAN
               || type == LexicalUnit.SAC_HERTZ
               || type == LexicalUnit.SAC_INCH
               || type == LexicalUnit.SAC_KILOHERTZ
               || type == LexicalUnit.SAC_MILLIMETER
               || type == LexicalUnit.SAC_MILLISECOND
               || type == LexicalUnit.SAC_PERCENTAGE
               || type == LexicalUnit.SAC_PICA
               || type == LexicalUnit.SAC_POINT
               || type == LexicalUnit.SAC_PIXEL
               || type == LexicalUnit.SAC_RADIAN
               || type == LexicalUnit.SAC_SECOND
               || type == LexicalUnit.SAC_DIMENSION )
    {
      buffer.append( "DIM " + value.getFloatValue() + value.getDimensionUnitText() );
    } else if( type == LexicalUnit.SAC_RGBCOLOR ) {
      LexicalUnit parameters = value.getParameters();
      buffer.append( "rgb(" + toString( parameters ) + ")" );
    } else if( type == LexicalUnit.SAC_STRING_VALUE ) {
      buffer.append( "\"" + value.getStringValue() + "\"" );
    } else if( type == LexicalUnit.SAC_IDENT ) {
      buffer.append( value.getStringValue() );
    } else if( type == LexicalUnit.SAC_PIXEL ) {
      buffer.append( value.getFloatValue() + "px" );
    } else if( type == LexicalUnit.SAC_INTEGER ) {
      buffer.append( value.getIntegerValue() );
    } else if( type == LexicalUnit.SAC_OPERATOR_COMMA ) {
      buffer.append( "," );
    } else if( type == LexicalUnit.SAC_ATTR ) {
      buffer.append( "ATTR " + value.getStringValue() );
    } else if( type == LexicalUnit.SAC_FUNCTION ) {
      buffer.append( "UNKNOWN FUNCTION " + value.getFunctionName() );
    } else if( type == LexicalUnit.SAC_DIMENSION ) {
      buffer.append( "UNKNOWN DIMENSION " + value );
    } else {
      buffer.append( "unsupported unit " + value.getLexicalUnitType() );
    }
    LexicalUnit next = value.getNextLexicalUnit();
    if( next != null ) {
      buffer.append( " " );
      buffer.append( toString( next ) );
    }
    return buffer.toString();
  }

  private static final class NamedColor {

    public NamedColor( int red, int green, int blue ) {
      this.red = red;
      this.green = green;
      this.blue = blue;
    }
    final int red;
    final int green;
    final int blue;

  }

}
