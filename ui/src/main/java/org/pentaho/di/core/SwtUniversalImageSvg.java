/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
