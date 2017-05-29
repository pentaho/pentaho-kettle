/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.SwtUniversalImageSvg;
import org.pentaho.di.core.svg.SvgImage;
import org.pentaho.di.core.svg.SvgSupport;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class KettleXulLoader extends SwtXulLoader {

  /** Icons size for SVG icons rasterization. */
  private int iconWidth = 16;
  private int iconHeight = 16;

  public KettleXulLoader() throws XulException {
    parser.handlers.remove( "DIALOG" );
    parser.registerHandler( "DIALOG", org.pentaho.di.ui.xul.KettleDialog.class.getName() );

    parser.handlers.remove( "ICONWAITBOX" );
    parser.registerHandler( "ICONWAITBOX", org.pentaho.di.ui.xul.KettleWaitBox.class.getName() );
  }

  public void setIconsSize( int width, int height ) {
    iconWidth = width;
    iconHeight = height;
  }

  /**
   * Get original stream without svg->png transformation.
   */
  public InputStream getOriginalResourceAsStream( String resource ) {
    return super.getResourceAsStream( resource );
  }

  @Override
  public InputStream getResourceAsStream( String resource ) {
    int height = iconHeight;
    int width = iconWidth;
    if ( resource.contains( ":" ) ) {
      // we have height/width overrides
      width = Integer.parseInt( resource.substring( resource.indexOf( ":" ) + 1, resource.indexOf( "#" ) ) );
      height = Integer.parseInt( resource.substring( resource.indexOf( "#" ) + 1, resource.indexOf( "." ) ) );
      resource = resource.substring( 0, resource.indexOf( ":" ) ) + resource.substring( resource.indexOf( "." ) );
    }
    if ( SvgSupport.isSvgEnabled() && ( SvgSupport.isSvgName( resource ) || SvgSupport.isPngName( resource ) ) ) {
      InputStream in = null;
      try {
        in = super.getResourceAsStream( SvgSupport.toSvgName( resource ) );
        // load SVG
        SvgImage svg = SvgSupport.loadSvgImage( in );
        SwtUniversalImage image = new SwtUniversalImageSvg( svg );

        Display d = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        // write to png
        Image result = image.getAsBitmapForSize( d, width, height );
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] { result.getImageData() };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        loader.save( out, SWT.IMAGE_PNG );

        image.dispose();

        return new ByteArrayInputStream( out.toByteArray() );
      } catch ( Throwable ignored ) {
        // any exception will result in falling back to PNG
      } finally {
        IOUtils.closeQuietly( in );
      }
      resource = SvgSupport.toPngName( resource );
    }
    return super.getResourceAsStream( resource );
  }
}
