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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.pentaho.di.core.svg.SvgImage;

/**
 * Universal image storage for Swing processing. It contains SVG or bitmap image depends on file and settings.
 */
public class SwingUniversalImage {
  private final SvgImage svg;
  private final BufferedImage bitmap;

  private Map<String, BufferedImage> cache = new TreeMap<String, BufferedImage>();

  public SwingUniversalImage( SvgImage svg ) {
    this.svg = svg;
    this.bitmap = null;
  }

  public SwingUniversalImage( BufferedImage bitmap ) {
    this.svg = null;
    this.bitmap = bitmap;
  }

  public synchronized BufferedImage getAsBitmapForSize( int width, int height ) {
    String key = width + "x" + height;
    BufferedImage result = cache.get( key );
    if ( result != null ) {
      return result;
    }

    if ( svg != null ) {
      result = renderToBitmap( svg, width, height );
    } else {
      result = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

      Graphics g = result.createGraphics();
      g.drawImage( bitmap, 0, 0, width, height, null );
      g.dispose();
    }
    cache.put( key, result );
    return result;
  }

  /**
   * Convert SVG image to swt Image with specified size. TODO: change to GVTBuilder rendering.
   */
  private static BufferedImage renderToBitmap( SvgImage svg, int width, int height ) {
    PNGTranscoder tr = new PNGTranscoder();
    tr.addTranscodingHint( PNGTranscoder.KEY_WIDTH, (float) width );
    tr.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, (float) height );

    TranscoderInput input = new TranscoderInput( svg.getDocument() );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TranscoderOutput output = new TranscoderOutput( out );
    try {
      tr.transcode( input, output );

      return ImageIO.read( new ByteArrayInputStream( out.toByteArray() ) );
    } catch ( Exception ex ) {
      throw new RuntimeException( ex );
    }
  }
}
