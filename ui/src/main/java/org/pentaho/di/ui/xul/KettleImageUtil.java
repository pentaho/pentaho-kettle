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

package org.pentaho.di.ui.xul;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.SwtUniversalImageSvg;
import org.pentaho.di.core.svg.SvgImage;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.ui.xul.XulDomContainer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bmorrise on 2/26/16.
 */
public class KettleImageUtil {

  /**
   * Icon sizes for rendering dialog icon from svg.
   */
  static final int[] IMAGE_SIZES = new int[] { 256, 128, 64, 48, 32, 16 };

  /**
   * Load multiple images from svg, or just png file.
   */
  public static Image[] loadImages( XulDomContainer container, Shell shell, String resource ) {
    Display d = shell.getDisplay();
    if ( d == null ) {
      d = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
    }

    if ( SvgSupport.isSvgEnabled() && ( SvgSupport.isSvgName( resource ) || SvgSupport.isPngName( resource ) ) ) {
      InputStream in = null;
      try {
        in = getResourceInputStream( resource, container );
        // getResourceInputStream( SvgSupport.toSvgName( resource ) );
        // load SVG
        SvgImage svg = SvgSupport.loadSvgImage( in );
        SwtUniversalImage image = new SwtUniversalImageSvg( svg );

        Image[] result = new Image[IMAGE_SIZES.length];
        for ( int i = 0; i < IMAGE_SIZES.length; i++ ) {
          result[i] = image.getAsBitmapForSize( d, IMAGE_SIZES[i], IMAGE_SIZES[i] );
        }
        return result;
      } catch ( Throwable ignored ) {
        // any exception will result in falling back to PNG
        ignored.printStackTrace();
      } finally {
        IOUtils.closeQuietly( in );
      }
      resource = SvgSupport.toPngName( resource );
    }

    InputStream in = null;
    try {
      in = getResourceInputStream( resource, container );
      return new Image[] { new Image( d, in ) };
    } catch ( Throwable ignored ) {
      // any exception will result in falling back to PNG
    } finally {
      IOUtils.closeQuietly( in );
    }
    return null;
  }

  /**
   * Retrieve file from original path.
   */
  private static InputStream getResourceInputStream( String resource, XulDomContainer container ) throws IOException {
    return ( (KettleXulLoader) container.getXulLoader() ).getOriginalResourceAsStream( resource );
  }

}
