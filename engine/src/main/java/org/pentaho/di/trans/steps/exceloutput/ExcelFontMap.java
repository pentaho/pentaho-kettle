/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.exceloutput;

import java.awt.Dimension;

import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableFont.FontName;
import jxl.write.WriteException;

public class ExcelFontMap {

  public static Colour getColour( int stepValue, Colour defaultColour ) {
    Colour retval = null;
    if ( defaultColour != null ) {
      retval = defaultColour;
    }
    if ( stepValue != ExcelOutputMeta.FONT_COLOR_NONE ) {
      switch ( stepValue ) {
        case ExcelOutputMeta.FONT_COLOR_BLACK:
          retval = Colour.BLACK;
          break;

        case ExcelOutputMeta.FONT_COLOR_WHITE:
          retval = Colour.WHITE;
          break;

        case ExcelOutputMeta.FONT_COLOR_RED:
          retval = Colour.RED;
          break;

        case ExcelOutputMeta.FONT_COLOR_BRIGHT_GREEN:
          retval = Colour.BRIGHT_GREEN;
          break;

        case ExcelOutputMeta.FONT_COLOR_BLUE:
          retval = Colour.BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_YELLOW:
          retval = Colour.YELLOW;
          break;

        case ExcelOutputMeta.FONT_COLOR_PINK:
          retval = Colour.PINK;
          break;

        case ExcelOutputMeta.FONT_COLOR_TURQUOISE:
          retval = Colour.TURQUOISE;
          break;

        case ExcelOutputMeta.FONT_COLOR_DARK_RED:
          retval = Colour.DARK_RED;
          break;

        case ExcelOutputMeta.FONT_COLOR_GREEN:
          retval = Colour.GREEN;
          break;

        case ExcelOutputMeta.FONT_COLOR_DARK_BLUE:
          retval = Colour.DARK_BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_DARK_YELLOW:
          retval = Colour.DARK_YELLOW;
          break;

        case ExcelOutputMeta.FONT_COLOR_VIOLET:
          retval = Colour.VIOLET;
          break;

        case ExcelOutputMeta.FONT_COLOR_TEAL:
          retval = Colour.TEAL;
          break;

        case ExcelOutputMeta.FONT_COLOR_GREY_25pct:
          retval = Colour.GREY_25_PERCENT;
          break;

        case ExcelOutputMeta.FONT_COLOR_GREY_50pct:
          retval = Colour.GRAY_50;
          break;

        case ExcelOutputMeta.FONT_COLOR_GREY_80pct:
          retval = Colour.GRAY_80;
          break;

        case ExcelOutputMeta.FONT_COLOR_PERIWINKLEpct:
          retval = Colour.PERIWINKLE;
          break;

        case ExcelOutputMeta.FONT_COLOR_PLUM:
          retval = Colour.PLUM;
          break;

        case ExcelOutputMeta.FONT_COLOR_IVORY:
          retval = Colour.IVORY;
          break;

        case ExcelOutputMeta.FONT_COLOR_LIGHT_TURQUOISE:
          retval = Colour.LIGHT_TURQUOISE;
          break;

        case ExcelOutputMeta.FONT_COLOR_DARK_PURPLE:
          retval = Colour.DARK_PURPLE;
          break;

        case ExcelOutputMeta.FONT_COLOR_CORAL:
          retval = Colour.CORAL;
          break;

        case ExcelOutputMeta.FONT_COLOR_OCEAN_BLUE:
          retval = Colour.OCEAN_BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_ICE_BLUE:
          retval = Colour.ICE_BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_TURQOISE:
          retval = Colour.TURQUOISE;
          break;

        case ExcelOutputMeta.FONT_COLOR_SKY_BLUE:
          retval = Colour.SKY_BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_LIGHT_GREEN:
          retval = Colour.LIGHT_GREEN;
          break;

        case ExcelOutputMeta.FONT_COLOR_VERY_LIGHT_YELLOW:
          retval = Colour.VERY_LIGHT_YELLOW;
          break;

        case ExcelOutputMeta.FONT_COLOR_PALE_BLUE:
          retval = Colour.PALE_BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_ROSE:
          retval = Colour.ROSE;
          break;

        case ExcelOutputMeta.FONT_COLOR_LAVENDER:
          retval = Colour.LAVENDER;
          break;

        case ExcelOutputMeta.FONT_COLOR_TAN:
          retval = Colour.TAN;
          break;

        case ExcelOutputMeta.FONT_COLOR_LIGHT_BLUE:
          retval = Colour.LIGHT_BLUE;
          break;

        case ExcelOutputMeta.FONT_COLOR_AQUA:
          retval = Colour.AQUA;
          break;

        case ExcelOutputMeta.FONT_COLOR_LIME:
          retval = Colour.LIME;
          break;

        case ExcelOutputMeta.FONT_COLOR_GOLD:
          retval = Colour.GOLD;
          break;

        case ExcelOutputMeta.FONT_COLOR_LIGHT_ORANGE:
          retval = Colour.LIGHT_ORANGE;
          break;

        case ExcelOutputMeta.FONT_COLOR_ORANGE:
          retval = Colour.ORANGE;
          break;

        case ExcelOutputMeta.FONT_COLOR_BLUE_GREY:
          retval = Colour.BLUE_GREY;
          break;

        case ExcelOutputMeta.FONT_COLOR_GREY_40pct:
          retval = Colour.GREY_40_PERCENT;
          break;

        case ExcelOutputMeta.FONT_COLOR_DARK_TEAL:
          retval = Colour.DARK_TEAL;
          break;

        case ExcelOutputMeta.FONT_COLOR_SEA_GREEN:
          retval = Colour.SEA_GREEN;
          break;

        case ExcelOutputMeta.FONT_COLOR_DARK_GREEN:
          retval = Colour.DARK_GREEN;
          break;

        case ExcelOutputMeta.FONT_COLOR_OLIVE_GREEN:
          retval = Colour.OLIVE_GREEN;
          break;

        case ExcelOutputMeta.FONT_COLOR_BROWN:
          retval = Colour.BROWN;
          break;
        default:
          break;
      }
    }
    return retval;
  }

  public static WritableCellFormat getOrientation( int stepValue, WritableCellFormat cellFormat ) throws WriteException {
    if ( stepValue != ExcelOutputMeta.FONT_ORIENTATION_HORIZONTAL ) {
      switch ( stepValue ) {
        case ExcelOutputMeta.FONT_ORIENTATION_MINUS_45:
          cellFormat.setOrientation( jxl.format.Orientation.MINUS_45 );
          break;
        case ExcelOutputMeta.FONT_ORIENTATION_MINUS_90:
          cellFormat.setOrientation( jxl.format.Orientation.MINUS_90 );
          break;
        case ExcelOutputMeta.FONT_ORIENTATION_PLUS_45:
          cellFormat.setOrientation( jxl.format.Orientation.PLUS_45 );
          break;
        case ExcelOutputMeta.FONT_ORIENTATION_PLUS_90:
          cellFormat.setOrientation( jxl.format.Orientation.PLUS_90 );
          break;
        case ExcelOutputMeta.FONT_ORIENTATION_STACKED:
          cellFormat.setOrientation( jxl.format.Orientation.STACKED );
          break;
        case ExcelOutputMeta.FONT_ORIENTATION_VERTICAL:
          cellFormat.setOrientation( jxl.format.Orientation.VERTICAL );
          break;
        default:
          break;
      }
    }
    return cellFormat;
  }

  public static FontName getFontName( int stepValue ) {

    FontName headerFontName = WritableFont.ARIAL;
    switch ( stepValue ) {
      case ExcelOutputMeta.FONT_NAME_COURIER:
        headerFontName = WritableFont.COURIER;
        break;
      case ExcelOutputMeta.FONT_NAME_TAHOMA:
        headerFontName = WritableFont.TAHOMA;
        break;
      case ExcelOutputMeta.FONT_NAME_TIMES:
        headerFontName = WritableFont.TIMES;
        break;
      default:
        break;
    }

    return headerFontName;
  }

  public static UnderlineStyle getUnderlineStyle( int stepValue ) {

    UnderlineStyle underline = UnderlineStyle.NO_UNDERLINE;
    switch ( stepValue ) {
      case ExcelOutputMeta.FONT_UNDERLINE_SINGLE:
        underline = UnderlineStyle.SINGLE;
        break;
      case ExcelOutputMeta.FONT_UNDERLINE_SINGLE_ACCOUNTING:
        underline = UnderlineStyle.SINGLE_ACCOUNTING;
        break;
      case ExcelOutputMeta.FONT_UNDERLINE_DOUBLE:
        underline = UnderlineStyle.DOUBLE;
        break;
      case ExcelOutputMeta.FONT_UNDERLINE_DOUBLE_ACCOUNTING:
        underline = UnderlineStyle.DOUBLE_ACCOUNTING;
        break;
      default:
        break;
    }

    return underline;
  }

  public static WritableCellFormat getAlignment( int stepValue, WritableCellFormat format ) throws WriteException {
    if ( stepValue != ExcelOutputMeta.FONT_ALIGNMENT_LEFT ) {
      switch ( stepValue ) {
        case ExcelOutputMeta.FONT_ALIGNMENT_RIGHT:
          format.setAlignment( jxl.format.Alignment.RIGHT );
          break;
        case ExcelOutputMeta.FONT_ALIGNMENT_CENTER:
          format.setAlignment( jxl.format.Alignment.CENTRE );
          break;
        case ExcelOutputMeta.FONT_ALIGNMENT_FILL:
          format.setAlignment( jxl.format.Alignment.FILL );
          break;
        case ExcelOutputMeta.FONT_ALIGNMENT_GENERAL:
          format.setAlignment( jxl.format.Alignment.GENERAL );
          break;
        case ExcelOutputMeta.FONT_ALIGNMENT_JUSTIFY:
          format.setAlignment( jxl.format.Alignment.JUSTIFY );
          break;
        default:
          break;
      }
    }
    return format;
  }

  public static Dimension getImageDimension( String filename ) throws Exception {
    Dimension m = new Dimension();

    // java.awt.Dimension dim= new java.awt.Dimension();
    java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
    /** lecture de l'image : */
    java.awt.Image image = toolkit.getImage( filename );
    int height = image.getHeight( null );
    int width = image.getWidth( null );

    boolean continueLoop = true;
    long timeStart = System.currentTimeMillis();
    while ( continueLoop ) {
      height = image.getHeight( null );
      width = image.getWidth( null );
      // Let's check the limit time
      if ( height > -1 ) {
        continueLoop = false;
      } else {
        // Update Time value
        long now = System.currentTimeMillis();
        // Let's check the limit time
        if ( now >= ( timeStart + 10000 ) ) {
          throw new Exception( "Time out! Can not load image [" + filename + "]!" );
        }
        try {
          Thread.sleep( 1000 );
        } catch ( Exception e ) {
          // handling this exception would be kind of silly.
        }
      }
    }
    m.height = height;
    m.width = width;

    return m;
  }

}
