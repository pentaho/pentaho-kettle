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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.SwtUniversalImageBitmap;
import org.pentaho.di.core.SwtUniversalImageSvg;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * Class for loading images from SVG, PNG, or other bitmap formats.
 * 
 * Logic is: if SVG is enabled, then SVG icon loaded if exist. Otherwise, class trying to change name into PNG and try
 * to load. If initial name is PNG, then PNG icon will be loaded.
 */
public class SwtSvgImageUtil {

  private static FileObject base;
  private static final String NO_IMAGE = "ui" + File.separator + "images" + File.separator + "no_image.svg";
  
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
    if ( result == null && SvgSupport.isSvgEnabled() && SvgSupport.isSvgName( location ) ) {
      result = loadFromCurrentClasspath( display, location );
    }
    if ( result == null && SvgSupport.isSvgEnabled() && SvgSupport.isSvgName( location ) ) {
      result = loadFromBasedVFS( display, location );
    }
    if ( result == null ) {
      result = loadFromCurrentClasspath( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null ) {
      result = loadFromBasedVFS( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null ) {
      result = getImageAsResource( display, NO_IMAGE );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getUniversalImage( Display display, ClassLoader classLoader, String filename ) {

    if ( !SvgSupport.isSvgEnabled() ) {
      filename = SvgSupport.toPngName( filename );
    }

    SwtUniversalImage result = loadFromClassLoader( display, classLoader, filename );
    if ( result == null ) {
      result = loadFromClassLoader( display, classLoader, File.separator + filename );
      if ( result == null ) {
        result = loadFromClassLoader( display, classLoader, "ui" + File.separator + "images" + File.separator + filename );
        if ( result == null ) {
          result = loadFromSimpleVFS( display, filename );
          if ( result == null ) {
            result = loadFromSimpleVFS( display, File.separator + filename );
            if ( result == null ) {
              result = loadFromSimpleVFS( display, "ui" + File.separator + "images" + File.separator + filename );
              if ( result == null ) {
                result = loadFromCurrentClasspath( display, filename );
                if ( result == null ) {
                  result = loadFromCurrentClasspath( display, File.separator + filename );
                  if ( result == null ) {
                    result = loadFromCurrentClasspath( display, "ui" + File.separator + "images" + File.separator + filename );
                  }
                }
              }
            }
          }
        }
      }
    }

    // if we haven't loaded SVG attempt to use PNG 
    if ( result == null && SvgSupport.isSvgEnabled() && SvgSupport.isSvgName( filename ) ) {
      result = getUniversalImage( display, classLoader, SvgSupport.toPngName( filename ) );
    }

    // if we can't load PNG, use default "no_image" graphic
    if ( result == null ) {
      result = getUniversalImage( display, classLoader, NO_IMAGE );
    }
    return result;
  }

  /**
   * Load image from several sources.
   */
  public static SwtUniversalImage getImage( Display display, String location ) {
    SwtUniversalImage result = null;
    if ( result == null && SvgSupport.isSvgEnabled() && SvgSupport.isSvgName( location ) ) {
      result = loadFromSimpleVFS( display, location );
    }
    if ( result == null && SvgSupport.isSvgEnabled() && SvgSupport.isSvgName( location ) ) {
      result = loadFromCurrentClasspath( display, location );
    }
    if ( result == null ) {
      result = loadFromSimpleVFS( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null ) {
      result = loadFromCurrentClasspath( display, SvgSupport.toPngName( location ) );
    }
    if ( result == null ) {
      result = getImage( display, NO_IMAGE );
    }
    return result;
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
