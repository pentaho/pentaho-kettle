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
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.gvt.GraphicsNode;
import org.pentaho.di.core.svg.SvgImage;

public class SwingUniversalImageSvg extends SwingUniversalImage {
  private final GraphicsNode svgGraphicsNode;
  private final Dimension2D svgGraphicsSize;

  public SwingUniversalImageSvg( SvgImage svg ) {
    // get GraphicsNode and size from svg document
    UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
    DocumentLoader documentLoader = new DocumentLoader( userAgentAdapter );
    BridgeContext ctx = new BridgeContext( userAgentAdapter, documentLoader );
    GVTBuilder builder = new GVTBuilder();
    svgGraphicsNode = builder.build( ctx, svg.getDocument() );
    svgGraphicsSize = ctx.getDocumentSize();
  }

  @Override
  public boolean isBitmap() {
    return false;
  }

  @Override
  protected void renderSimple( BufferedImage area ) {
    Graphics2D gc = createGraphics( area );

    render( gc, area.getWidth() / 2, area.getHeight() / 2, area.getWidth(), area.getHeight(), 0 );

    gc.dispose();
  }

  /**
   * Draw SVG image to Graphics2D.
   */
  @Override
  protected void render( Graphics2D gc, int centerX, int centerY, int width, int height, double angleRadians ) {
    render( gc, svgGraphicsNode, svgGraphicsSize, centerX, centerY, width, height, angleRadians );
  }

  public static void render( Graphics2D gc, GraphicsNode svgGraphicsNode, Dimension2D svgGraphicsSize, int centerX,
      int centerY, int width, int height, double angleRadians ) {
    double scaleX = width / svgGraphicsSize.getWidth();
    double scaleY = height / svgGraphicsSize.getHeight();

    AffineTransform affineTransform = new AffineTransform();
    if ( centerX != 0 || centerY != 0 ) {
      affineTransform.translate( centerX, centerY );
    }
    affineTransform.scale( scaleX, scaleY );
    if ( angleRadians != 0 ) {
      affineTransform.rotate( angleRadians );
    }
    affineTransform.translate( -svgGraphicsSize.getWidth() / 2, -svgGraphicsSize.getHeight() / 2 );

    svgGraphicsNode.setTransform( affineTransform );

    // Fix to remove the thrown warning message:
    // Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint
    if ( gc.getRenderingHint( RenderingHintsKeyExt.KEY_TRANSCODING ) == null ) {
      gc.setRenderingHint( RenderingHintsKeyExt.KEY_TRANSCODING, RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING );
    }

    svgGraphicsNode.paint( gc );
  }
}
