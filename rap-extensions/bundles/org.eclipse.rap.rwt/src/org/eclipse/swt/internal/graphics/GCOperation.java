/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;


public abstract class GCOperation {

  public static final class SetProperty extends GCOperation {

    public static final int FOREGROUND = 0;
    public static final int BACKGROUND = 1;
    public static final int ALPHA = 2;
    public static final int LINE_WIDTH = 3;
    public static final int LINE_CAP = 4;
    public static final int LINE_JOIN = 5;
    public static final int FONT = 6;

    public final int id;
    public final Object value;

    public SetProperty( int id, RGB value ) {
      this.id = id;
      this.value = value;
    }

    public SetProperty( FontData value ) {
      this.id = FONT;
      this.value = value;
    }

    public SetProperty( int id, int value ) {
      this.id = id;
      this.value = Integer.valueOf( value );
    }
  }

  public static final class DrawLine extends GCOperation {

    public final int x1;
    public final int y1;
    public final int x2;
    public final int y2;

    public DrawLine( int x1, int y1, int x2, int y2 ) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
    }
  }

  public static final class DrawPoint extends GCOperation {

    public final int x;
    public final int y;

    public DrawPoint( int x, int y ) {
      this.x = x;
      this.y = y;
    }
  }

  public static class DrawRectangle extends GCOperation {

    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final boolean fill;

    public DrawRectangle( Rectangle bounds, boolean fill ) {
      this.x = bounds.x;
      this.y = bounds.y;
      this.width = bounds.width;
      this.height = bounds.height;
      this.fill = fill;
    }
  }

  public static final class DrawRoundRectangle extends DrawRectangle {

    public final int arcWidth;
    public final int arcHeight;

    public DrawRoundRectangle( Rectangle bounds, int arcWidth, int arcHeight, boolean fill ) {
      super( bounds, fill );
      this.arcWidth = arcWidth;
      this.arcHeight = arcHeight;
    }
  }

  public static final class FillGradientRectangle extends DrawRectangle {

    public final boolean vertical;

    public FillGradientRectangle( Rectangle bounds, boolean vertical ) {
      super( bounds, true );
      this.vertical = vertical;
    }
  }

  public static final class DrawArc extends GCOperation {

    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final int startAngle;
    public final int arcAngle;
    public final boolean fill;

    public DrawArc( Rectangle bounds, int startAngle, int arcAngle, boolean fill ) {
      this.x = bounds.x;
      this.y = bounds.y;
      this.width = bounds.width;
      this.height = bounds.height;
      this.startAngle = startAngle;
      this.arcAngle = arcAngle;
      this.fill = fill;
    }
  }

  public static final class DrawPolyline extends GCOperation {

    public final int[] points;
    public final boolean close;
    public final boolean fill;

    public DrawPolyline( int[] points, boolean close, boolean fill ) {
      this.points = new int[ points.length ];
      System.arraycopy( points, 0, this.points, 0, points.length );
      this.close = close;
      this.fill = fill;
    }
  }

  public static final class DrawImage extends GCOperation {

    public final Image image;
    public final int srcX;
    public final int srcY;
    public final int srcWidth;
    public final int srcHeight;
    public final int destX;
    public final int destY;
    public final int destWidth;
    public final int destHeight;
    public final boolean simple;

    public DrawImage( Image image, Rectangle src, Rectangle dest, boolean simple ) {
      this.image = image;
      this.srcX = src.x;
      this.srcY = src.y;
      this.srcWidth = src.width;
      this.srcHeight = src.height;
      this.destX = dest.x;
      this.destY = dest.y;
      this.destWidth = dest.width;
      this.destHeight = dest.height;
      this.simple = simple;
    }
  }

  public static final class DrawText extends GCOperation {

    public final String text;
    public final int x;
    public final int y;
    public final int flags;

    public DrawText( String text, int x, int y, int flags ) {
      this.text = text;
      this.x = x;
      this.y = y;
      this.flags = flags;
    }
  }

  public static final class DrawPath extends GCOperation {

    public final byte[] types;
    public final float[] points;
    public final boolean fill;

    public DrawPath( byte[] types, float[] points, boolean fill ) {
      this.types = types;
      this.points = points;
      this.fill = fill;
    }

  }

  public static final class SetClipping extends GCOperation {

    public final byte[] types;
    public final float[] points;
    public final Rectangle rectangle;

    public SetClipping() {
      types = null;
      points = null;
      rectangle = null;
    }

    public SetClipping( Rectangle rectangle ) {
      this.types = null;
      this.points = null;
      this.rectangle = rectangle;
    }

    public SetClipping( byte[] types, float[] points ) {
      this.types = types;
      this.points = points;
      rectangle = null;
    }

    public boolean isRectangular() {
      return rectangle != null;
    }

    public boolean isReset() {
      return types == null && points == null && rectangle == null;
    }

  }

  public static final class SetTransform extends GCOperation {

    public final float[] elements;

    public SetTransform( float[] elements ) {
      this.elements = elements;
    }

  }

}
