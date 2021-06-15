/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer.InstanceCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;


public class InternalImageFactory {

  private final SharedInstanceBuffer<String, InternalImage> cache;

  public InternalImageFactory() {
    cache = new SharedInstanceBuffer<String, InternalImage>();
  }

  public InternalImage findInternalImage( final String fileName ) {
    return cache.get( fileName, new InstanceCreator<String, InternalImage>() {
      public InternalImage createInstance( String fileName ) {
        return createInternalImage( fileName );
      }
    } );
  }

  public InternalImage findInternalImage( InputStream stream ) {
    final BufferedInputStream bufferedStream = new BufferedInputStream( stream );
    final ImageData imageData = readImageData( bufferedStream );
    final String path = createGeneratedImagePath( imageData );
    return cache.get( path, new InstanceCreator<String, InternalImage>() {
      public InternalImage createInstance( String path ) {
        return createInternalImage( path, bufferedStream, imageData );
      }
    } );
  }

  public InternalImage findInternalImage( final ImageData imageData ) {
    final String path = createGeneratedImagePath( imageData );
    return cache.get( path, new InstanceCreator<String, InternalImage>() {
      public InternalImage createInstance( String path ) {
        InputStream stream = createInputStream( imageData );
        return createInternalImage( path, stream, imageData );
      }
    } );
  }

  InternalImage findInternalImage( String key, final InputStream inputStream ) {
    return cache.get( key, new InstanceCreator<String, InternalImage>() {
      public InternalImage createInstance( String key ) {
        BufferedInputStream bufferedStream = new BufferedInputStream( inputStream );
        ImageData imageData = readImageData( bufferedStream );
        String path = createGeneratedImagePath( imageData );
        return createInternalImage( path, bufferedStream, imageData );
      }
    } );
  }

  static ImageData readImageData( InputStream stream ) throws SWTException {
    ////////////////////////////////////////////////////////////////////////////
    // TODO: [fappel] Image size calculation and resource registration both
    //                read the input stream. Because of this I use a workaround
    //                with a BufferedInputStream. Resetting it after reading the
    //                image size enables the ResourceManager to reuse it for
    //                registration. Note that the order is crucial here, since
    //                the ResourceManager seems to close the stream (shrug).
    //                It would be nice to find a solution without reading the
    //                stream twice.
    stream.mark( Integer.MAX_VALUE );
    ImageData result = new ImageData( stream );
    try {
      stream.reset();
    } catch( IOException shouldNotHappen ) {
      String msg = "Could not reset input stream after reading image";
      throw new RuntimeException( msg, shouldNotHappen );
    }
    return result;
  }

  static InputStream createInputStream( ImageData imageData ) {
    ImageLoader imageLoader = new ImageLoader();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    imageLoader.data = new ImageData[] { imageData };
    imageLoader.save( outputStream, getOutputFormat( imageData ) );
    byte[] bytes = outputStream.toByteArray();
    return new ByteArrayInputStream( bytes );
  }

  private static InternalImage createInternalImage( String fileName ) {
    InternalImage result;
    try {
      FileInputStream stream = new FileInputStream( fileName );
      try {
        result = createInternalImage( stream );
      } finally {
        stream.close();
      }
    } catch( IOException ioe ) {
      throw new SWTException( SWT.ERROR_IO, ioe.getMessage() );
    }
    return result;
  }

  private static InternalImage createInternalImage( InputStream stream ) {
    InputStream bufferedStream = new BufferedInputStream( stream );
    ImageData imageData = readImageData( bufferedStream );
    String path = createGeneratedImagePath( imageData );
    return createInternalImage( path, bufferedStream, imageData );
  }

  private static InternalImage createInternalImage( String path,
                                                    InputStream stream,
                                                    ImageData imageData )
  {
    RWT.getResourceManager().register( path, stream );
    return new InternalImage( path, imageData.width, imageData.height, false );
  }

  private static int getOutputFormat( ImageData imageData ) {
    int result = imageData.type;
    if( imageData.type == SWT.IMAGE_UNDEFINED ) {
      result = SWT.IMAGE_PNG;
    }
    return result;
  }

  private static String createGeneratedImagePath( ImageData data ) {
    int outputFormat = getOutputFormat( data );
    String extension;
    switch( outputFormat ) {
      case SWT.IMAGE_BMP:
      case SWT.IMAGE_BMP_RLE:
        extension = ".bmp";
        break;
      case SWT.IMAGE_GIF:
        extension = ".gif";
        break;
      case SWT.IMAGE_JPEG:
        extension = ".jpg";
        break;
      case SWT.IMAGE_TIFF:
        extension = ".tif";
        break;
      case SWT.IMAGE_ICO:
        extension = ".ico";
        break;
      case SWT.IMAGE_PNG:
      default:
        extension = ".png";
        break;
    }
    String hash = getHash( data ) + extension;
    return "generated/" + hash;
  }

  /*
   * [cm] Compute a CRC32 value using all of the parts of the ImageData. For
   * parts that may be null, a unique salt is added to avoid collisions in rare
   * cases. There is a possibility that, for instance, the alphaData is set in
   * one image but not the maskData. Then in a second image, the maskData is set
   * to the same thing as the previous image, but no alphaData is set. In this
   * case there would be a collision if no other information is added.
   */
  private static String getHash( ImageData imageData ) {
    CRC32 crc32 = new CRC32();
    if( imageData.data != null ) {
      crc32.update( 1 );
      crc32.update( imageData.data );
    }
    if( imageData.alphaData != null ) {
      crc32.update( 2 );
      crc32.update( imageData.alphaData );
    }
    if( imageData.maskData != null ) {
      crc32.update( 3 );
      crc32.update( imageData.maskData );
    }
    if( imageData.palette != null ) {
      crc32.update( 4 );
      if( imageData.palette.isDirect ) {
        crc32.update( 5 );
        crc32.update( imageData.palette.redMask );
        crc32.update( imageData.palette.greenMask );
        crc32.update( imageData.palette.blueMask );
      } else {
        crc32.update( 6 );
        RGB[] rgb = imageData.palette.getRGBs();
        for( int i = 0; i < rgb.length; i++ ) {
          crc32.update( rgb[ i ].red );
          crc32.update( rgb[ i ].green );
          crc32.update( rgb[ i ].blue );
        }
      }
    }
    crc32.update( imageData.alpha );
    crc32.update( imageData.transparentPixel );
    crc32.update( imageData.type );
    crc32.update( imageData.bytesPerLine );
    crc32.update( imageData.scanlinePad );
    crc32.update( imageData.maskPad );
    crc32.update( imageData.x );
    crc32.update( imageData.y );
    crc32.update( imageData.width );
    crc32.update( imageData.height );
    crc32.update( imageData.depth );
    crc32.update( imageData.delayTime );
    crc32.update( imageData.disposalMethod );
    return Long.toHexString( crc32.getValue() );
  }
}
