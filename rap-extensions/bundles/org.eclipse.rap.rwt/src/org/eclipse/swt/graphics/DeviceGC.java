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


class DeviceGC extends GCDelegate {
  private final Device device;
  private Color background;
  private Color foreground;
  private Font font;
  private int alpha;
  private int lineWidth;
  private int lineCap;
  private int lineJoin;
  private Rectangle clippingRect;
  private float[] transform = { 1, 0, 0, 1, 0, 0 };

  DeviceGC( Device device ) {
    this.device = device;
    this.background = device.getSystemColor( SWT.COLOR_WHITE );
    this.foreground = device.getSystemColor( SWT.COLOR_BLACK );
    this.font = device.getSystemFont();
    this.alpha = 255;
    this.lineWidth = 0;
    this.lineCap = SWT.CAP_FLAT;
    this.lineJoin = SWT.JOIN_MITER;
  }

  @Override
  void setBackground( Color color ) {
    background = color;
  }

  @Override
  Color getBackground() {
    return background;
  }

  @Override
  void setForeground( Color color ) {
    foreground = color;
  }

  @Override
  Color getForeground() {
    return foreground;
  }

  @Override
  void setFont( Font font ) {
    this.font = font;
  }

  @Override
  Font getFont() {
    return font;
  }

  @Override
  Font getDefaultFont() {
    return device.getSystemFont();
  }

  @Override
  void setAlpha( int alpha ) {
    this.alpha = alpha;
  }

  @Override
  int getAlpha() {
    return alpha;
  }

  @Override
  void setLineWidth( int lineWidth ) {
    this.lineWidth = lineWidth;
  }

  @Override
  int getLineWidth() {
    return lineWidth;
  }

  @Override
  void setLineCap( int lineCap ) {
    this.lineCap = lineCap;
  }

  @Override
  int getLineCap() {
    return lineCap;
  }

  @Override
  void setLineJoin( int lineJoin ) {
    this.lineJoin = lineJoin;
  }

  @Override
  int getLineJoin() {
    return lineJoin;
  }

  @Override
  void setClipping( Rectangle rectangle ) {
    clippingRect = rectangle;
  }

  @Override
  void setClipping( Path path ) {
    clippingRect = getClippingRectangle( path );
  }

  @Override
  Rectangle getClipping() {
    if( clippingRect == null ) {
      return device.getBounds();
    }
    return new Rectangle( clippingRect.x, clippingRect.y, clippingRect.width, clippingRect.height );
  }

  @Override
  void setTransform( float[] elements ) {
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
  }

  @Override
  void drawLine( int x1, int y1, int x2, int y2 ) {
  }

  @Override
  void drawPolyline( int[] pointArray, boolean close, boolean fill ) {
  }

  @Override
  void drawRectangle( Rectangle bounds, boolean fill ) {
  }

  @Override
  void drawRoundRectangle( Rectangle bounds, int arcWidth, int arcHeight, boolean fill ) {
  }

  @Override
  void fillGradientRectangle( Rectangle bounds, boolean vertical ) {
  }

  @Override
  void drawArc( Rectangle boundsx, int startAngle, int arcAngle, boolean fill ) {
  }

  @Override
  void drawImage( Image image, Rectangle src, Rectangle dest, boolean simple ) {
  }

  @Override
  void drawText( String string, int x, int y, int flags ) {
  }

  @Override
  void drawPath( Path path, boolean fill ) {
  }

}
