/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.gui;

import java.awt.image.BufferedImage;

public interface PrimitiveGCInterface {

  public enum EColor {
    BACKGROUND, BLACK, WHITE, RED, YELLOW, ORANGE, GREEN, BLUE, MAGENTA, GRAY, LIGHTGRAY, DARKGRAY, LIGHTBLUE, CRYSTAL, HOP_DEFAULT, HOP_OK
  }

  public enum EFont {
    NOTE, GRAPH, SMALL,
  }

  public enum ELineStyle {
    SOLID, DASHDOT, DOT, PARALLEL, DASH
  }

  public enum EImage {
    LOCK, STEP_ERROR, EDIT, CONTEXT_MENU, TRUE, FALSE, ERROR, INFO, TARGET, INPUT, OUTPUT, ARROW, COPY_ROWS,
      UNCONDITIONAL, PARALLEL, BUSY, INJECT, LOAD_BALANCE, CHECKPOINT, DB,
    ARROW_DEFAULT, ARROW_OK, ARROW_ERROR, ARROW_DISABLED, ARROW_CANDIDATE
  }

  public void setLineWidth( int width );

  public void setFont( EFont font );

  public Point textExtent( String text );

  public Point getDeviceBounds();

  public void setBackground( EColor color );

  public void setForeground( EColor color );

  public void setBackground( int red, int green, int blue );

  public void setForeground( int red, int green, int blue );

  // public EColor getBackground();
  // public EColor getForeground();

  public void fillRectangle( int x, int y, int width, int height );

  public void fillGradientRectangle( int x, int y, int width, int height, boolean vertical );

  public void drawImage( EImage image, int x, int y );
  
  public void drawImage( EImage image, int x, int y, float magnification );

  public void drawImage( EImage image, int x, int y, float magnification, double angle );

  public void drawImage( BufferedImage image, int x, int y );

  public void drawLine( int x, int y, int x2, int y2 );

  public void setLineStyle( ELineStyle lineStyle );

  public void drawRectangle( int x, int y, int width, int height );

  public void drawPoint( int x, int y );

  public void drawText( String text, int x, int y );

  public void drawText( String text, int x, int y, boolean transparent );

  public void fillRoundRectangle( int x, int y, int width, int height, int circleWidth, int circleHeight );

  public void drawRoundRectangle( int x, int y, int width, int height, int circleWidth, int circleHeight );

  public void fillPolygon( int[] polygon );

  public void drawPolygon( int[] polygon );

  public void drawPolyline( int[] polyline );

  public void setAntialias( boolean antiAlias );

  public void setTransform( float translationX, float translationY, int shadowsize, float magnification );

  public void setAlpha( int alpha );

  public void dispose();

  public int getAlpha();

  public void setFont( String fontName, int fontSize, boolean fontBold, boolean fontItalic );

  public Object getImage();

  public Point getImageBounds( EImage eImage );

  public void switchForegroundBackgroundColors();

  public Point getArea();
}
