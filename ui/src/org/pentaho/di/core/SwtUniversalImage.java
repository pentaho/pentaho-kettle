/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core;

import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.pentaho.di.core.svg.SvgImage;

/**
 * Universal image storage for SWT processing. It contains SVG or bitmap image depends on file and settings.
 */
public class SwtUniversalImage {
  private final SvgImage svg;
  private final Image bitmap;

  private Map<String, Image> cache = new TreeMap<String, Image>();

  public SwtUniversalImage( SvgImage svg ) {
    this.svg = svg;
    this.bitmap = null;
  }

  public SwtUniversalImage( Image bitmap ) {
    this.svg = null;
    this.bitmap = bitmap;
  }

  public synchronized void dispose() {
    if ( cache == null ) {
      return;
    }
    for ( Image img : cache.values() ) {
      if ( !img.isDisposed() ) {
        img.dispose();
      }
    }
    if ( bitmap != null && !bitmap.isDisposed() ) {
      bitmap.dispose();
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

    if ( result != null && !result.isDisposed() ) {
      return result;
    }

    if ( svg != null ) {
      result = renderToBitmap( device, svg );
    } else {
      result = bitmap;
    }
    cache.put( "", result );
    return result;
  }

  public synchronized Image getAsBitmapForSize( Device device, int width, int height ) {
    checkDisposed();

    String key = width + "x" + height;
    Image result = cache.get( key );
    if ( result != null && !result.isDisposed() ) {
      return result;
    }

    if ( svg != null ) {
      result = renderToBitmap( device, svg, width, height );
    } else {
      int xsize = bitmap.getBounds().width;
      int ysize = bitmap.getBounds().height;
      result = new Image( device, width, height );
      GC gc = new GC( result );
      gc.drawImage( bitmap, 0, 0, xsize, ysize, 0, 0, width, height );
      gc.dispose();
    }
    cache.put( key, result );
    return result;
  }

  /**
   * Convert SVG image to swt Image.
   */
  private static Image renderToBitmap( Device device, SvgImage svg, PNGTranscoder tr ) {
    TranscoderInput input = new TranscoderInput( svg.getDocument() );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TranscoderOutput output = new TranscoderOutput( out );
    try {
      tr.transcode( input, output );
    } catch ( Exception ex ) {
      throw new RuntimeException( ex );
    }

    return new Image( device, new ByteArrayInputStream( out.toByteArray() ) );
  }
  
  /**
   * Convert SVG image to swt Image.
   */
  private static Image renderToBitmap( Device device, SvgImage svg ) {
    PNGTranscoder tr = new PNGTranscoder();
    return renderToBitmap( device, svg, tr );
  }

  /**
   * Convert SVG image to swt Image with specified size.
   */
  private static Image renderToBitmap( Device device, SvgImage svg, int width, int height ) {
    PNGTranscoder tr = new PNGTranscoder() {
      protected ImageRenderer createRenderer() {
        ImageRenderer ir = super.createRenderer();
        RenderingHints h = ir.getRenderingHints();
        h.add( new RenderingHints( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE ) );
        h.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
        h.add( new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY ) );
        h.add( new RenderingHints( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC ) );
        h.add( new RenderingHints( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE ) );
        return ir;
      }
    };
    tr.addTranscodingHint( PNGTranscoder.KEY_WIDTH, (float) width );
    tr.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, (float) height );
    return renderToBitmap( device, svg, tr );
  }
}
