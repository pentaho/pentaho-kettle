/*******************************************************************************
 * Copyright (c) 2011, 2015 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rüdiger Herrmann - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.graphics.FontUtil;
import org.eclipse.swt.internal.graphics.GCAdapter;
import org.eclipse.swt.internal.graphics.GCOperation;
import org.eclipse.swt.internal.graphics.GCOperation.DrawArc;
import org.eclipse.swt.internal.graphics.GCOperation.DrawImage;
import org.eclipse.swt.internal.graphics.GCOperation.DrawLine;
import org.eclipse.swt.internal.graphics.GCOperation.DrawPath;
import org.eclipse.swt.internal.graphics.GCOperation.DrawPoint;
import org.eclipse.swt.internal.graphics.GCOperation.DrawPolyline;
import org.eclipse.swt.internal.graphics.GCOperation.DrawRectangle;
import org.eclipse.swt.internal.graphics.GCOperation.DrawRoundRectangle;
import org.eclipse.swt.internal.graphics.GCOperation.DrawText;
import org.eclipse.swt.internal.graphics.GCOperation.FillGradientRectangle;
import org.eclipse.swt.internal.graphics.GCOperation.SetClipping;
import org.eclipse.swt.internal.graphics.GCOperation.SetProperty;
import org.eclipse.swt.internal.graphics.GCOperation.SetTransform;
import org.eclipse.swt.widgets.Control;


class ControlGC extends GCDelegate {
  private final Control control;
  private Color background;
  private Color foreground;
  private Font font;
  private int alpha;
  private int lineWidth;
  private int lineCap;
  private int lineJoin;
  private Rectangle clippingRect;
  private float[] transform = { 1, 0, 0, 1, 0, 0 };

  ControlGC( Control control ) {
    this.control = control;
    this.background = control.getBackground();
    this.foreground = control.getForeground();
    this.font = control.getFont();
    this.alpha = 255;
    this.lineWidth = 0;
    this.lineCap = SWT.CAP_FLAT;
    this.lineJoin = SWT.JOIN_MITER;
  }

  @Override
  void setBackground( Color color ) {
    if( !background.equals( color ) ) {
      GCOperation operation = new SetProperty( SetProperty.BACKGROUND, color.getRGB() );
      addGCOperation( operation );
    }
    background = color;
  }

  @Override
  Color getBackground() {
    return background;
  }

  @Override
  void setForeground( Color color ) {
    if( !foreground.equals( color ) ) {
      GCOperation operation = new SetProperty( SetProperty.FOREGROUND, color.getRGB() );
      addGCOperation( operation );
    }
    foreground = color;
  }

  @Override
  Color getForeground() {
    return foreground;
  }

  @Override
  void setFont( Font font ) {
    if( !this.font.equals( font ) ) {
      GCOperation operation = new SetProperty( cloneFontData( FontUtil.getData( font ) ) );
      addGCOperation( operation );
    }
    this.font = font;
  }

  @Override
  Font getFont() {
    return font;
  }

  @Override
  Font getDefaultFont() {
    return control.getDisplay().getSystemFont();
  }

  @Override
  void setAlpha( int alpha ) {
    this.alpha = alpha;
    GCOperation operation = new SetProperty( SetProperty.ALPHA, alpha );
    addGCOperation( operation );
  }

  @Override
  int getAlpha() {
    return alpha;
  }

  @Override
  void setLineWidth( int lineWidth ) {
    this.lineWidth = lineWidth;
    GCOperation operation = new SetProperty( SetProperty.LINE_WIDTH, lineWidth );
    addGCOperation( operation );
  }

  @Override
  int getLineWidth() {
    return lineWidth;
  }

  @Override
  void setLineCap( int lineCap ) {
    this.lineCap = lineCap;
    GCOperation operation = new SetProperty( SetProperty.LINE_CAP, lineCap );
    addGCOperation( operation );
  }

  @Override
  int getLineCap() {
    return lineCap;
  }

  @Override
  void setLineJoin( int lineJoin ) {
    this.lineJoin = lineJoin;
    GCOperation operation = new SetProperty( SetProperty.LINE_JOIN, lineJoin );
    addGCOperation( operation );
  }

  @Override
  int getLineJoin() {
    return lineJoin;
  }

  @Override
  void setClipping( Rectangle rectangle ) {
    if( clippingRect != null ) {
      addGCOperation( new SetClipping() );
    }
    clippingRect = rectangle;
    if( clippingRect != null ) {
      GCOperation operation = new SetClipping( rectangle );
      addGCOperation( operation );
    }
  }

  @Override
  void setClipping( Path path ) {
    if( clippingRect != null ) {
      addGCOperation( new SetClipping() );
    }
    clippingRect = getClippingRectangle( path );
    if( clippingRect != null ) {
      PathData pathData = path.getPathData();
      GCOperation operation = new SetClipping( pathData.types, pathData.points );
      addGCOperation( operation );
    }
  }

  @Override
  Rectangle getClipping() {
    if( clippingRect == null ) {
      return control.getBounds();
    }
    return new Rectangle( clippingRect.x, clippingRect.y, clippingRect.width, clippingRect.height );
  }

  @Override
  void setTransform( float[] elements ) {
    addGCOperation( new SetTransform( elements ) );
    transform = elements;
  }

  @Override
  float[] getTransform() {
    return transform;
  }

  @Override
  Point stringExtent( String string ) {
    return TextSizeUtil.stringExtent( font, string );
  }

  @Override
  Point textExtent( String string, int wrapWidth ) {
    return TextSizeUtil.textExtent( font, string, wrapWidth );
  }

  @Override
  void drawPoint( int x, int y ) {
    GCOperation operation = new DrawPoint( x, y );
    addGCOperation( operation );
  }

  @Override
  void drawLine( int x1, int y1, int x2, int y2 ) {
    GCOperation operation = new DrawLine( x1, y1, x2, y2 );
    addGCOperation( operation );
  }

  @Override
  void drawPolyline( int[] pointArray, boolean close, boolean fill ) {
    DrawPolyline operation = new DrawPolyline( pointArray, close, fill );
    addGCOperation( operation );
  }

  @Override
  void drawRectangle( Rectangle bounds, boolean fill ) {
    GCOperation operation = new DrawRectangle( bounds, fill );
    addGCOperation( operation );
  }

  @Override
  void drawRoundRectangle( Rectangle bounds, int arcWidth, int arcHeight, boolean fill ) {
    GCOperation operation = new DrawRoundRectangle( bounds, arcWidth, arcHeight, fill );
    addGCOperation( operation );
  }

  @Override
  void fillGradientRectangle( Rectangle bounds, boolean vertical ) {
    GCOperation operation = new FillGradientRectangle( bounds, vertical );
    addGCOperation( operation );
  }

  @Override
  void drawArc( Rectangle bounds, int startAngle, int arcAngle, boolean fill ) {
    GCOperation operation = new DrawArc( bounds, startAngle, arcAngle, fill );
    addGCOperation( operation );
  }

  @Override
  void drawImage( Image image, Rectangle src, Rectangle dest, boolean simple ) {
    GCOperation operation = new DrawImage( image, src, dest, simple );
    addGCOperation( operation );
  }

  @Override
  void drawText( String string, int x, int y, int flags ) {
    GCOperation operation = new DrawText( string, x, y, flags );
    addGCOperation( operation );
  }

  @Override
  void drawPath( Path path, boolean fill ) {
    PathData pathData = path.getPathData();
    GCOperation operation = new DrawPath( pathData.types, pathData.points, fill );
    addGCOperation( operation );
  }

  GCAdapter getGCAdapter() {
    return control.getAdapter( GCAdapter.class );
  }

  private void addGCOperation( GCOperation operation ) {
    GCAdapter adapter = getGCAdapter();
    if( adapter != null ) {
      adapter.addGCOperation( operation );
    }
  }

  private static FontData cloneFontData( FontData fontData ) {
    FontData result = new FontData( fontData.getName(), fontData.getHeight(), fontData.getStyle() );
    result.setLocale( fontData.getLocale() );
    return result;
  }

}
