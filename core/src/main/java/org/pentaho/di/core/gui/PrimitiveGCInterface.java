/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.gui;

import java.awt.image.BufferedImage;

public interface PrimitiveGCInterface {

  public enum EColor {
    BACKGROUND, BLACK, WHITE, RED, YELLOW, ORANGE, GREEN, BLUE, MAGENTA, GRAY, LIGHTGRAY, DARKGRAY, LIGHTBLUE, CRYSTAL,
    HOP_DEFAULT, HOP_OK, DEPRECATED
  }

  public enum EFont {
    NOTE, GRAPH, SMALL,
  }

  public enum ELineStyle {
    SOLID, DASHDOT, DOT, PARALLEL, DASH
  }

  public enum EImage {
    LOCK, STEP_ERROR, STEP_ERROR_RED, EDIT, CONTEXT_MENU, TRUE, FALSE, ERROR, INFO, TARGET, INPUT, OUTPUT, ARROW,
    COPY_ROWS, UNCONDITIONAL, PARALLEL, BUSY, INJECT, LOAD_BALANCE, CHECKPOINT, DB, ARROW_DEFAULT, ARROW_OK,
    ARROW_ERROR, ARROW_DISABLED, ARROW_CANDIDATE
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

  public void drawImage( String location, ClassLoader classLoader, int x, int y );

  public void drawImage( EImage image, int x, int y );

  public void drawImage( EImage image, int x, int y, float magnification );

  public void drawImage( EImage image, int x, int y, int width, int height, float magnification );

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
