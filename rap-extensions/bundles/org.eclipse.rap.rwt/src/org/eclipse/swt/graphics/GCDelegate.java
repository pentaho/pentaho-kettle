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


abstract class GCDelegate {

  abstract void setBackground( Color color );
  abstract Color getBackground();

  abstract void setForeground( Color color );
  abstract Color getForeground();

  abstract void setFont( Font font );
  abstract Font getFont();

  abstract Font getDefaultFont();
  abstract void setAlpha( int alpha );
  abstract int getAlpha();

  abstract void setLineWidth( int lineWidth );
  abstract int getLineWidth();
  abstract void setLineCap( int lineCap );
  abstract int getLineCap();
  abstract void setLineJoin( int lineJoin );
  abstract int getLineJoin();

  abstract void setClipping( Rectangle rectangle );
  abstract void setClipping( Path path );
  abstract Rectangle getClipping();

  abstract Point stringExtent( String string );
  abstract Point textExtent( String string , int wrapWidth );

  abstract void drawPoint( int x, int y );
  abstract void drawLine( int x1, int y1, int x2, int y2 );
  abstract void drawPolyline( int[] pointArray, boolean close, boolean fill );
  abstract void drawRectangle( Rectangle bounds, boolean fill );

  abstract void drawRoundRectangle( Rectangle bounds, int arcWidth, int arcHeight, boolean fill );
  abstract void fillGradientRectangle( Rectangle bounds, boolean vertical );

  abstract void drawArc( Rectangle bounds, int startAngle, int arcAngle, boolean fill );

  abstract void drawImage( Image image, Rectangle src, Rectangle dest, boolean simple );

  abstract void drawText( String string, int x, int y, int flags );

  abstract void drawPath( Path path, boolean fill );

  abstract void setTransform( float[] elements );
  abstract float[] getTransform();

  protected Rectangle getClippingRectangle( Path path ) {
    if( path != null ) {
      int minX = Integer.MAX_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxX = -Integer.MAX_VALUE;
      int maxY = -Integer.MAX_VALUE;
      float[] points = path.getPathData().points;
      for( int i = 0; i < points.length; i++ ) {
        if( i % 2 == 0 ) {
          minX = Math.min( minX, ( int )points[ i ] );
          maxX = Math.max( maxX, ( int )points[ i ] );
        } else {
          minY = Math.min( minY, ( int )points[ i ] );
          maxY = Math.max( maxY, ( int )points[ i ] );
        }
      }
      return new Rectangle( minX, minY, maxX - minX, maxY - minY );
    }
    return null;
  }

}
