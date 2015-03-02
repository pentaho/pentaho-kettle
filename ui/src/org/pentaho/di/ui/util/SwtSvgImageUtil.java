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

package org.pentaho.di.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.ui.svg.SvgSupport;

/**
 * Class for loading images from SVG, PNG, or other bitmap formats.
 * 
 * Logic is: if SVG is enabled, then SVG icon loaded if exist. Otherwise, class trying to change name into PNG and try
 * to load. If initial name is PNG, then PNG icon will be loaded.
 */
public class SwtSvgImageUtil {

  /** Flag for enable/disable SVG loading. */
  public static boolean enableSVG = true;

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
   * Load image from several sources.
   */
  public static SwtUniversalImage getImageAsResource( Display display, String location ) {
    SwtUniversalImage result = null;
    if ( result == null && enableSVG && SvgSupport.isSvgName( location ) ) {
      result = loadFromCurrentClasspath( display, location );
    }
    if ( result == null && enableSVG && SvgSupport.isSvgName( location ) ) {
      result = loadFromBasedVFS( display, location );
    }
    if ( result == null ) {
      result = loadFromCurrentClasspath( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null ) {
      result = loadFromBasedVFS( display, SvgSupport.toPngName( location ) );
    }

    if ( result == null ) {
      throw new RuntimeException( "Unable to load image with name [" + location + "]" );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  private static SwtUniversalImage getImage( Display display, Class<?> resourceClass, String filename ) {
    SwtUniversalImage result = null;
    if ( result == null && enableSVG && SvgSupport.isSvgName( filename ) ) {
      result = loadFromClass( display, resourceClass, filename );
    }
    if ( result == null && enableSVG && SvgSupport.isSvgName( filename ) ) {
      result = loadFromClass( display, resourceClass, "/" + filename );
    }
    if ( result == null && enableSVG && SvgSupport.isSvgName( filename ) ) {
      result = loadFromSimpleVFS( display, filename );
    }
    if ( result == null ) {
      result = loadFromClass( display, resourceClass, SvgSupport.toPngName( filename ) );
    }
    if ( result == null ) {
      result = loadFromClass( display, resourceClass, "/" + SvgSupport.toPngName( filename ) );
    }
    if ( result == null ) {
      result = loadFromSimpleVFS( display, SvgSupport.toPngName( filename ) );
    }

    if ( result == null ) {
      throw new RuntimeException( "Unable to load image with name [" + filename + "]" );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getUniversalImage( Display display, ClassLoader classLoader, String filename ) {
    SwtUniversalImage result = null;
    if ( result == null && enableSVG && SvgSupport.isSvgName( filename ) ) {
      result = loadFromClassLoader( display, classLoader, filename );
    }
    if ( result == null && enableSVG && SvgSupport.isSvgName( filename ) ) {
      result = loadFromClassLoader( display, classLoader, "/" + filename );
    }
    if ( result == null && enableSVG && SvgSupport.isSvgName( filename ) ) {
      result = loadFromSimpleVFS( display, filename );
    }
    if ( result == null ) {
      result = loadFromClassLoader( display, classLoader, SvgSupport.toPngName( filename ) );
    }
    if ( result == null ) {
      result = loadFromClassLoader( display, classLoader, "/" + SvgSupport.toPngName( filename ) );
    }
    if ( result == null ) {
      result = loadFromSimpleVFS( display, SvgSupport.toPngName( filename ) );
    }

    if ( result == null ) {
      throw new RuntimeException( "Unable to load image with name [" + filename + "]" );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getImage( Display display, String location ) {
    SwtUniversalImage result = null;
    if ( result == null && enableSVG && SvgSupport.isSvgName( location ) ) {
      result = loadFromSimpleVFS( display, location );
    }
    if ( result == null && enableSVG && SvgSupport.isSvgName( location ) ) {
      result = loadFromCurrentClasspath( display, location );
    }
    if ( result == null ) {
      result = loadFromSimpleVFS( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null ) {
      result = loadFromCurrentClasspath( display, SvgSupport.toPngName( location ) );
    }

    if ( result == null ) {
      throw new RuntimeException( "Unable to load image with name [" + location + "]" );
    }
    return result;
  }

  /**
   * Internal image loading by Class.getResourceAsStream.
   */
  private static SwtUniversalImage loadFromClass( Display display, Class<?> resourceClass, String location ) {
    InputStream s = resourceClass.getResourceAsStream( location );
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
   * Internal image loading by ClassLoader.getResourceAsStream.
   */
  private static SwtUniversalImage loadFromClassLoader( Display display, ClassLoader classLoader, String location ) {
    InputStream s = classLoader.getResourceAsStream( location );
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
      return new SwtUniversalImage( new Image( display, in ) );
    } else {
      // svg image - need to convert to bitmap
      try {
        return new SwtUniversalImage( SvgSupport.loadSvgImage( in ) );
      } catch ( Exception ex ) {
        throw new RuntimeException( ex );
      }
    }
  }
}
