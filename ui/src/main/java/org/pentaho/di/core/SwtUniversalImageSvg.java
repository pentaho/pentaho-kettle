/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.ext.awt.image.codec.png.PNGRegistryEntry;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.pentaho.di.core.svg.SvgImage;

public class SwtUniversalImageSvg extends SwtUniversalImage {
  private final GraphicsNode svgGraphicsNode;
  private final Dimension2D svgGraphicsSize;

  static {
    // workaround due to known issue in batik 1.8 - https://issues.apache.org/jira/browse/BATIK-1125
    ImageTagRegistry registry = ImageTagRegistry.getRegistry();
    registry.register( new PNGRegistryEntry() );
  }

  public SwtUniversalImageSvg( SvgImage svg ) {
    // get GraphicsNode and size from svg document
    UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
    DocumentLoader documentLoader = new DocumentLoader( userAgentAdapter );
    BridgeContext ctx = new BridgeContext( userAgentAdapter, documentLoader );
    GVTBuilder builder = new GVTBuilder();
    svgGraphicsNode = builder.build( ctx, svg.getDocument() );
    svgGraphicsSize = ctx.getDocumentSize();
  }

  @Override
  protected Image renderSimple( Device device ) {
    return renderSimple( device, (int) Math.round( svgGraphicsSize.getWidth() ), (int) Math.round( svgGraphicsSize
        .getHeight() ) );
  }

  @Override
  protected Image renderSimple( Device device, int width, int height ) {
    BufferedImage area = SwingUniversalImage.createBitmap( width, height );

    Graphics2D gc = SwingUniversalImage.createGraphics( area );
    SwingUniversalImageSvg.render( gc, svgGraphicsNode, svgGraphicsSize, width / 2, height / 2, width, height, 0 );
    gc.dispose();

    return swing2swt( device, area );
  }

  @Override
  protected Image renderRotated( Device device, int width, int height, double angleRadians ) {
    BufferedImage doubleArea = SwingUniversalImage.createDoubleBitmap( width, height );

    Graphics2D gc = SwingUniversalImage.createGraphics( doubleArea );
    SwingUniversalImageSvg.render( gc, svgGraphicsNode, svgGraphicsSize, doubleArea.getWidth() / 2, doubleArea
        .getHeight() / 2, width, height, angleRadians );

    gc.dispose();

    return swing2swt( device, doubleArea );
  }
}
