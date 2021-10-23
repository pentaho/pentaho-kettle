/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.SwtUniversalImageBitmap;
import org.pentaho.di.core.SwtUniversalImageSvg;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.ui.core.ConstUI;

/**
 * Class for loading images from SVG, PNG, or other bitmap formats.
 * 
 * Logic is: if SVG is enabled, then SVG icon loaded if exist. Otherwise, class trying to change name into PNG and try
 * to load. If initial name is PNG, then PNG icon will be loaded.
 */
public class SwtSvgImageUtil {

  private static LogChannelInterface log = new LogChannel( "SwtSvgImageUtil" );

  private static final String NO_IMAGE = "ui/images/no_image.svg";

  private static FileObject base;

  static {
    try {
      base = KettleVFS.getInstance().getFileSystemManager().resolveFile( System.getProperty( "user.dir" ) );
    } catch ( FileSystemException e ) {
      e.printStackTrace();
      base = null;
    }
  }

  /**
   * Get the image for when all other fallbacks have failed.  This is an image
   * drawn on the canvas, a square with a red X.
   *
   * @param display the device to render the image to
   * @return the missing image
   */
  public static SwtUniversalImage getMissingImage( Display display ) {
    Image img = new Image( display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
    if ( !Const.isRunningOnWebspoonMode() ) {
      GC gc = new GC( img );
      gc.setForeground( new Color( display, 0, 0, 0 ) );
      gc.drawRectangle( 4, 4, ConstUI.ICON_SIZE - 8, ConstUI.ICON_SIZE - 8 );
      gc.setForeground( new Color( display, 255, 0, 0 ) );
      gc.drawLine( 4, 4, ConstUI.ICON_SIZE - 4, ConstUI.ICON_SIZE - 4 );
      gc.drawLine( ConstUI.ICON_SIZE - 4, 4, 4, ConstUI.ICON_SIZE - 4 );
      gc.dispose();
    }
    return new SwtUniversalImageBitmap( img );
  }

  /**
   * Load image from several sources.
   */
  private static SwtUniversalImage getImageAsResourceInternal( Display display, String location ) {
    SwtUniversalImage result = null;
    if ( result == null ) {
      result = loadFromCurrentClasspath( display, location );
    }
    if ( result == null ) {
      result = loadFromBasedVFS( display, location );
    }
    if ( result == null ) {
      result = loadFromSimpleVFS( display, location );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getImageAsResource( Display display, String location ) {
    SwtUniversalImage result = null;
    if ( result == null && SvgSupport.isSvgEnabled() ) {
      result = getImageAsResourceInternal( display, SvgSupport.toSvgName( location ) );
    }
    if ( result == null ) {
      result = getImageAsResourceInternal( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null && !location.equals( NO_IMAGE ) ) {
      log.logError( "Unable to load image [" + location + "]" );
      result = getImageAsResource( display, NO_IMAGE );
    }
    if ( result == null ) {
      log.logError( "Unable to load image [" + location + "]" );
      result = getMissingImage( display );
    }
    return result;
  }

  /**
   * Get an image using the provided classLoader and path.  An attempt will be made to load the image with the
   * classLoader first using SVG (regardless of extension), and falling back to PNG.  If the image cannot be 
   * loaded with the provided classLoader, the search path will be expanded to include the file system (ui/images).
   *
   * @param display the device to render the image to
   * @param classLoader the classLoader to use to load the image resource
   * @param filename the path to the image
   * @param width the width to scale the image to
   * @param height the height to scale the image to
   * @return an swt Image with width/height dimensions
   */
  public static Image getImage( Display display, ClassLoader classLoader, String filename, int width, int height ) {
    SwtUniversalImage u = getUniversalImage( display, classLoader, filename );
    return u.getAsBitmapForSize( display, width, height );
  }

  private static SwtUniversalImage getUniversalImageInternal( Display display,
                                                              ClassLoader classLoader, String filename ) {
    SwtUniversalImage result = loadFromClassLoader( display, classLoader, filename );
    if ( result == null ) {
      result = loadFromClassLoader( display, classLoader, "/" + filename );
      if ( result == null ) {
        result = loadFromClassLoader( display, classLoader, "ui/images/" + filename );
        if ( result == null ) {
          result = getImageAsResourceInternal( display, filename );
        }
      }
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getUniversalImage( Display display, ClassLoader classLoader, String filename ) {
    if ( StringUtils.isBlank( filename ) ) {
      log.logError( "Unable to load image [" + filename + "]" );
      return getImageAsResource( display, NO_IMAGE );
    }

    SwtUniversalImage result = null;
    if ( SvgSupport.isSvgEnabled() ) {
      result = getUniversalImageInternal( display, classLoader, SvgSupport.toSvgName( filename ) );
    }

    // if we haven't loaded SVG attempt to use PNG 
    if ( result == null ) {
      result = getUniversalImageInternal( display, classLoader, SvgSupport.toPngName( filename ) );
    }

    // if we can't load PNG, use default "no_image" graphic
    if ( result == null ) {
      log.logError( "Unable to load image [" + filename + "]" );
      result = getImageAsResource( display, NO_IMAGE );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getImage( Display display, String location ) {
    return getImageAsResource( display, location );
  }

  /**
   * Internal image loading by ClassLoader.getResourceAsStream.
   */
  private static SwtUniversalImage loadFromClassLoader( Display display, ClassLoader classLoader, String location ) {
    InputStream s = null;
    try {
      s = classLoader.getResourceAsStream( location );
    } catch ( Throwable t ) {
      log.logDebug( "Unable to load image from classloader [" + location + "]" );
    }
    if ( s == null ) {
      return null;
    }
    try {
      return loadImage( display, s, location );
    } finally {
      IOUtils.closeQuietly( s );
    }
  }

  /**
   * Internal image loading by Thread.currentThread.getContextClassLoader.getResource.
   */
  private static SwtUniversalImage loadFromCurrentClasspath( Display display, String location ) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if ( cl == null ) {
      // Can't count on Thread.currentThread().getContextClassLoader() being non-null on Mac
      // Have to provide some fallback
      cl = SwtSvgImageUtil.class.getClassLoader();
    }
    URL res = null;
    try {
      res = cl.getResource( location );
    } catch ( Throwable t ) {
      log.logDebug( "Unable to load image from classloader [" + location + "]" );
    }
    if ( res == null ) {
      return null;
    }
    InputStream s;
    try {
      s = res.openStream();
    } catch ( IOException ex ) {
      return null;
    }
    if ( s == null ) {
      return null;
    }
    try {
      return loadImage( display, s, location );
    } finally {
      IOUtils.closeQuietly( s );
    }
  }

  /**
   * Internal image loading from Kettle's user.dir VFS.
   */
  private static SwtUniversalImage loadFromBasedVFS( Display display, String location ) {
    try {
      FileObject imageFileObject = KettleVFS.getInstance().getFileSystemManager().resolveFile( base, location );
      InputStream s = KettleVFS.getInputStream( imageFileObject );
      if ( s == null ) {
        return null;
      }
      try {
        return loadImage( display, s, location );
      } finally {
        IOUtils.closeQuietly( s );
      }
    } catch ( FileSystemException ex ) {
      return null;
    }
  }

  /**
   * Internal image loading from Kettle's VFS.
   */
  private static SwtUniversalImage loadFromSimpleVFS( Display display, String location ) {
    try {
      InputStream s = KettleVFS.getInputStream( location );
      if ( s == null ) {
        return null;
      }
      try {
        return loadImage( display, s, location );
      } finally {
        IOUtils.closeQuietly( s );
      }
    } catch ( KettleFileException e ) {
      // do nothing. try to load next
    }
    return null;
  }

  /**
   * Load image from InputStream as bitmap image, or SVG image conversion to bitmap image.
   */
  private static SwtUniversalImage loadImage( Display display, InputStream in, String filename ) {
    if ( !SvgSupport.isSvgName( filename ) ) {
      // bitmap image
      return new SwtUniversalImageBitmap( new Image( display, in ) );
    } else {
      // svg image - need to convert to bitmap
      try {
        return new SwtUniversalImageSvg( SvgSupport.loadSvgImage( in ) );
      } catch ( Exception ex ) {
        throw new RuntimeException( ex );
      }
    }
  }
}
