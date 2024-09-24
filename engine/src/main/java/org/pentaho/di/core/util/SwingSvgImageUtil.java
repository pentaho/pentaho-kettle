/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.SwingUniversalImage;
import org.pentaho.di.core.SwingUniversalImageBitmap;
import org.pentaho.di.core.SwingUniversalImageSvg;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.di.core.vfs.KettleVFS;

import javax.imageio.ImageIO;

/**
 * Class for loading images from SVG, PNG, or other bitmap formats.
 *
 * Logic is: if SVG is enabled, then SVG icon loaded if exist. Otherwise, class trying to change name into PNG and try
 * to load. If initial name is PNG, then PNG icon will be loaded.
 */
public class SwingSvgImageUtil {

  private static FileObject base;
  private static final String NO_IMAGE = "ui/images/no_image.svg";

  static {
    try {
      base = KettleVFS.getInstance().getFileSystemManager().resolveFile( System.getProperty( "user.dir" ) );
    } catch ( FileSystemException e ) {
      e.printStackTrace();
      base = null;
    }
  }

  /**
   * Load image from several sources.
   */
  private static SwingUniversalImage getImageAsResourceInternal( String location ) {
    SwingUniversalImage result = null;
    if ( result == null ) {
      result = loadFromCurrentClasspath( location );
    }
    if ( result == null ) {
      result = loadFromBasedVFS( location );
    }
    if ( result == null ) {
      result = loadFromSimpleVFS( location );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwingUniversalImage getImageAsResource( String location ) {
    SwingUniversalImage result = null;
    if ( result == null && SvgSupport.isSvgEnabled() ) {
      result = getImageAsResourceInternal( SvgSupport.toSvgName( location ) );
    }
    if ( result == null ) {
      result = getImageAsResourceInternal( SvgSupport.toPngName( location ) );
    }
    if ( result == null && !location.equals( NO_IMAGE ) ) {
      result = getImageAsResource( NO_IMAGE );
    }
    return result;
  }

  private static SwingUniversalImage getUniversalImageInternal( ClassLoader classLoader, String filename ) {
    SwingUniversalImage result = loadFromClassLoader( classLoader, filename );
    if ( result == null ) {
      result = loadFromClassLoader( classLoader, "/" + filename );
      if ( result == null ) {
        result = loadFromClassLoader( classLoader, "ui/images/" + filename );
        if ( result == null ) {
          result = getImageAsResourceInternal( filename );
        }
      }
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwingUniversalImage getUniversalImage( ClassLoader classLoader, String filename ) {

    if ( StringUtils.isBlank( filename ) ) {
      throw new RuntimeException( "Filename not provided" );
    }

    SwingUniversalImage result = null;
    if ( SvgSupport.isSvgEnabled() ) {
      result = getUniversalImageInternal( classLoader, SvgSupport.toSvgName( filename ) );
    }

    // if we haven't loaded SVG attempt to use PNG
    if ( result == null ) {
      result = getUniversalImageInternal( classLoader, SvgSupport.toPngName( filename ) );
    }

    // if we can't load PNG, use default "no_image" graphic
    if ( result == null ) {
      result = getImageAsResource( NO_IMAGE );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwingUniversalImage getImage( String location ) {
    return getImageAsResource( location );
  }

  /**
   * Internal image loading by ClassLoader.getResourceAsStream.
   */
  private static SwingUniversalImage loadFromClassLoader( ClassLoader classLoader, String location ) {
    InputStream s = classLoader.getResourceAsStream( location );
    if ( s == null ) {
      return null;
    }
    try {
      return loadImage( s, location );
    } finally {
      IOUtils.closeQuietly( s );
    }
  }

  /**
   * Internal image loading by Thread.currentThread.getContextClassLoader.getResource.
   */
  private static SwingUniversalImage loadFromCurrentClasspath( String location ) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL res = cl.getResource( location );
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
      return loadImage( s, location );
    } finally {
      IOUtils.closeQuietly( s );
    }
  }

  /**
   * Internal image loading from Kettle's user.dir VFS.
   */
  private static SwingUniversalImage loadFromBasedVFS( String location ) {
    try {
      FileObject imageFileObject = KettleVFS.getInstance().getFileSystemManager().resolveFile( base, location );
      InputStream s = KettleVFS.getInputStream( imageFileObject );
      if ( s == null ) {
        return null;
      }
      try {
        return loadImage( s, location );
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
  private static SwingUniversalImage loadFromSimpleVFS( String location ) {
    try {
      InputStream s = KettleVFS.getInputStream( location );
      if ( s == null ) {
        return null;
      }
      try {
        return loadImage( s, location );
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
  private static SwingUniversalImage loadImage( InputStream in, String filename ) {
    if ( !SvgSupport.isSvgName( filename ) ) {
      // bitmap image
      try {
        return new SwingUniversalImageBitmap( ImageIO.read( in ) );
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    } else {
      // svg image - need to convert to bitmap
      try {
        return new SwingUniversalImageSvg( SvgSupport.loadSvgImage( in ) );
      } catch ( Exception ex ) {
        throw new RuntimeException( ex );
      }
    }
  }
}
