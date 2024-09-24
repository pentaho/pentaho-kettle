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

package org.pentaho.di.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

/**
 * Universal image storage for Swing processing. It contains SVG or bitmap image depends on file and settings.
 */
public abstract class SwingUniversalImage {

  private Map<String, BufferedImage> cache = new TreeMap<String, BufferedImage>();

  public abstract boolean isBitmap();

  /**
   * Just scale for area's size.
   */
  protected abstract void renderSimple( BufferedImage area );

  /**
   * Render with scale, at specified position, with possible rotation.
   */
  protected abstract void render( Graphics2D gc, int centerX, int centerY, int width, int height, double angleRadians );

  /**
   * Get scaled image.
   */
  public synchronized BufferedImage getAsBitmapForSize( int width, int height ) {
    String key = width + "x" + height;
    BufferedImage result = cache.get( key );
    if ( result == null ) {
      result = createBitmap( width, height );
      renderSimple( result );
      cache.put( key, result );
    }
    return result;
  }

  /**
   * Draw rotated image on double canvas size. It required against lost corners on rotate.
   */
  public synchronized BufferedImage getAsBitmapForSize( int width, int height, double angleRadians ) {
    int angleDegree = (int) Math.round( Math.toDegrees( angleRadians ) );
    while ( angleDegree < 0 ) {
      angleDegree += 360;
    }
    angleDegree %= 360;
    angleRadians = Math.toRadians( angleDegree );

    String key = width + "x" + height + "/" + Integer.toString( angleDegree );
    BufferedImage result = cache.get( key );
    if ( result == null ) {
      result = createDoubleBitmap( width, height );

      Graphics2D gc = createGraphics( result );
      render( gc, result.getWidth() / 2, result.getHeight() / 2, width, height, angleRadians );
      gc.dispose();

      cache.put( key, result );
    }
    return result;
  }

  public synchronized void drawToGraphics( Graphics2D gc, int locationX, int locationY, int width, int height ) {
    render( gc, locationX + width / 2, locationY + height / 2, width, height, 0 );
  }

  public synchronized void drawToGraphics( Graphics2D gc, int centerX, int centerY, int width, int height,
      double angleRadians ) {
    render( gc, centerX, centerY, width, height, angleRadians );
  }

  /**
   * Create bitmap with specified size and full colorspace.
   */
  public static BufferedImage createBitmap( int width, int height ) {
    return new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
  }

  /**
   * Create bitmap with double of specified size and full colorspace. Used for rotated images.
   */
  public static BufferedImage createDoubleBitmap( int width, int height ) {
    int sz = Math.max( width, height ) * 2;
    return new BufferedImage( sz, sz, BufferedImage.TYPE_INT_ARGB );
  }

  /**
   * Create Graphics2D for specified bitmap with rendering hints.
   */
  public static Graphics2D createGraphics( BufferedImage area ) {
    Graphics2D gc = (Graphics2D) area.getGraphics();
    gc.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
    gc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    gc.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
    gc.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    gc.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
    return gc;
  }
}
