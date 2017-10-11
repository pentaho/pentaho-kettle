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

package org.pentaho.di.core.gui;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.SwingUniversalImage;
import org.pentaho.di.core.SwingUniversalImageBitmap;
import org.pentaho.di.core.SwingUniversalImageSvg;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.svg.SvgImage;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.reporting.libraries.base.util.WaitingImageObserver;

public class SwingGUIResource {
  private static LogChannelInterface log = new LogChannel( "SwingGUIResource" );

  private static SwingGUIResource instance;

  private Map<String, SwingUniversalImage> stepImages;
  private Map<String, SwingUniversalImage> entryImages;

  private SwingGUIResource() throws KettleException {
    this.stepImages = loadStepImages();
    this.entryImages = loadEntryImages();
  }

  public static SwingGUIResource getInstance() throws KettleException {
    if ( instance == null ) {
      instance = new SwingGUIResource();
    }
    return instance;
  }

  private Map<String, SwingUniversalImage> loadStepImages() throws KettleException {
    Map<String, SwingUniversalImage> map = new HashMap<String, SwingUniversalImage>();

    for ( PluginInterface plugin : PluginRegistry.getInstance().getPlugins( StepPluginType.class ) ) {
      try {
        SwingUniversalImage image = getUniversalImageIcon( plugin );
        for ( String id : plugin.getIds() ) {
          map.put( id, image );
        }
      } catch ( Exception e ) {
        log.logError( "Unable to load step icon image for plugin: "
          + plugin.getName() + " (id=" + plugin.getIds()[0], e );
      }
    }

    return map;
  }

  private Map<String, SwingUniversalImage> loadEntryImages() throws KettleException {
    Map<String, SwingUniversalImage> map = new HashMap<String, SwingUniversalImage>();

    for ( PluginInterface plugin : PluginRegistry.getInstance().getPlugins( JobEntryPluginType.class ) ) {
      try {
        if ( "SPECIAL".equals( plugin.getIds()[0] ) ) {
          continue;
        }

        SwingUniversalImage image = getUniversalImageIcon( plugin );
        if ( image == null ) {
          throw new KettleException( "Unable to find image file: "
            + plugin.getImageFile() + " for plugin: " + plugin );
        }

        map.put( plugin.getIds()[0], image );
      } catch ( Exception e ) {
        log.logError( "Unable to load job entry icon image for plugin: "
          + plugin.getName() + " (id=" + plugin.getIds()[0], e );
      }
    }

    return map;
  }

  private SwingUniversalImage getUniversalImageIcon( PluginInterface plugin ) throws KettleException {
    try {
      PluginRegistry registry = PluginRegistry.getInstance();
      String filename = plugin.getImageFile();

      ClassLoader classLoader = registry.getClassLoader( plugin );

      SwingUniversalImage image = null;

      if ( SvgSupport.isSvgEnabled() && SvgSupport.isSvgName( filename ) ) {
        // Try to use the plugin class loader to get access to the icon
        //
        InputStream inputStream = classLoader.getResourceAsStream( filename );
        if ( inputStream == null ) {
          inputStream = classLoader.getResourceAsStream( "/" + filename );
        }
        // Try to use the PDI class loader to get access to the icon
        //
        if ( inputStream == null ) {
          inputStream = registry.getClass().getResourceAsStream( filename );
        }
        if ( inputStream == null ) {
          inputStream = registry.getClass().getResourceAsStream( "/" + filename );
        }
        // As a last resort, try to use the standard file-system
        //
        if ( inputStream == null ) {
          try {
            inputStream = new FileInputStream( filename );
          } catch ( FileNotFoundException e ) {
            // Ignore, throws error below
          }
        }
        if ( inputStream != null ) {
          try {
            SvgImage svg = SvgSupport.loadSvgImage( inputStream );
            image = new SwingUniversalImageSvg( svg );
          } finally {
            IOUtils.closeQuietly( inputStream );
          }
        }
      }

      if ( image == null ) {
        filename = SvgSupport.toPngName( filename );
        // Try to use the plugin class loader to get access to the icon
        //
        InputStream inputStream = classLoader.getResourceAsStream( filename );
        if ( inputStream == null ) {
          inputStream = classLoader.getResourceAsStream( "/" + filename );
        }
        // Try to use the PDI class loader to get access to the icon
        //
        if ( inputStream == null ) {
          inputStream = registry.getClass().getResourceAsStream( filename );
        }
        if ( inputStream == null ) {
          inputStream = registry.getClass().getResourceAsStream( "/" + filename );
        }
        // As a last resort, try to use the standard file-system
        //
        if ( inputStream == null ) {
          try {
            inputStream = new FileInputStream( filename );
          } catch ( FileNotFoundException e ) {
            // Ignore, throws error below
          }
        }
        if ( inputStream != null ) {
          try {
            BufferedImage bitmap = ImageIO.read( inputStream );

            WaitingImageObserver wia = new WaitingImageObserver( bitmap );
            wia.waitImageLoaded();

            image = new SwingUniversalImageBitmap( bitmap );
          } finally {
            IOUtils.closeQuietly( inputStream );
          }
        }
      }

      if ( image == null ) {
        throw new KettleException( "Unable to find file: " + plugin.getImageFile() + " for plugin: " + plugin );
      }

      return image;
    } catch ( Throwable e ) {
      throw new KettleException( "Unable to load image from file : '"
        + plugin.getImageFile() + "' for plugin: " + plugin, e );
    }
  }

  public Map<String, SwingUniversalImage> getEntryImages() {
    return entryImages;
  }

  public Map<String, SwingUniversalImage> getStepImages() {
    return stepImages;
  }
}
