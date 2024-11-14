/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Universal image storage for SWT processing. It contains SVG or bitmap image depends on file and settings.
 */
public abstract class SwtUniversalImage {

  private Map<String, Image> cache = new TreeMap<String, Image>();

  @Deprecated
  protected abstract Image renderSimple( Device device );

  protected abstract Image renderSimple( Device device, int width, int height );

  protected abstract Image renderRotated( Device device, int width, int height, double angleRadians );

  public synchronized void dispose() {
    if ( cache == null ) {
      return;
    }

    for ( Image img : cache.values() ) {
      if ( !img.isDisposed() ) {
        img.dispose();
      }
    }
    cache = null;
  }

  private void checkDisposed() {
    if ( cache == null ) {
      throw new RuntimeException( "Already disposed" );
    }
  }

  /**
   * @deprecated Use getAsBitmapForSize() instead.
   */
  @Deprecated
  public synchronized Image getAsBitmap( Device device ) {
    checkDisposed();

    Image result = cache.get( "" );

    if ( result == null ) {
      result = renderSimple( device );
      cache.put( "", result );
    }
    return result;
  }

  /**
   * Method getAsBitmapForSize(..., angle) can't be called, because it returns bigger picture.
   */
  public synchronized Image getAsBitmapForSize( Device device, int width, int height ) {
    checkDisposed();

    String key = width + "x" + height;
    Image result = cache.get( key );
    if ( result == null ) {
      result = renderSimple( device, width, height );
      cache.put( key, result );
    }
    return result;
  }

  /**
   * Draw rotated image on double canvas size. It required against lost corners on rotate.
   */
  public synchronized Image getAsBitmapForSize( Device device, int width, int height, double angleRadians ) {
    checkDisposed();

    int angleDegree = (int) Math.round( Math.toDegrees( angleRadians ) );
    while ( angleDegree < 0 ) {
      angleDegree += 360;
    }
    angleDegree %= 360;
    angleRadians = Math.toRadians( angleDegree );

    String key = width + "x" + height + "/" + angleDegree;
    Image result = cache.get( key );
    if ( result == null ) {
      result = renderRotated( device, width, height, angleRadians );
      cache.put( key, result );
    }

    return result;
  }

  /**
   * Converts BufferedImage to SWT/Image with alpha channel.
   */
  protected Image swing2swt( Device device, BufferedImage img ) {
    PaletteData palette = new PaletteData( 0xFF0000, 0xFF00, 0xFF );
    ImageData data = new ImageData( img.getWidth(), img.getHeight(), 32, palette );
    for ( int y = 0; y < data.height; y++ ) {
      for ( int x = 0; x < data.width; x++ ) {
        int rgba = img.getRGB( x, y );
        int rgb = palette.getPixel( new RGB( ( rgba >> 16 ) & 0xFF, ( rgba >> 8 ) & 0xFF, rgba & 0xFF ) );
        int a = ( rgba >> 24 ) & 0xFF;
        data.setPixel( x, y, rgb );
        data.setAlpha( x, y, a );
      }
    }
    return new Image( device, data );
  }
}
