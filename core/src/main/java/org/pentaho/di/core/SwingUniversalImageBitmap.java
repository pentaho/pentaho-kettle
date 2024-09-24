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
import java.awt.image.BufferedImage;

public class SwingUniversalImageBitmap extends SwingUniversalImage {
  private final BufferedImage bitmap;

  public SwingUniversalImageBitmap( BufferedImage bitmap ) {
    this.bitmap = bitmap;
  }

  @Override
  public boolean isBitmap() {
    return true;
  }

  @Override
  protected void renderSimple( BufferedImage area ) {
    Graphics2D gc = createGraphics( area );
    gc.drawImage( bitmap, 0, 0, area.getWidth(), area.getHeight(), null );
    gc.dispose();
  }

  @Override
  protected void render( Graphics2D gc, int centerX, int centerY, int width, int height, double angleRadians ) {
    AffineTransform oldTransform = gc.getTransform();
    try {
      double scaleX = width * 1.0 / bitmap.getWidth();
      double scaleY = height * 1.0 / bitmap.getHeight();

      AffineTransform affineTransform = new AffineTransform( oldTransform );
      if ( centerX != 0 || centerY != 0 ) {
        affineTransform.translate( centerX, centerY );
      }
      affineTransform.scale( scaleX, scaleY );
      if ( angleRadians != 0 ) {
        affineTransform.rotate( angleRadians );
      }
      affineTransform.translate( -bitmap.getWidth() / 2, -bitmap.getHeight() / 2 );

      gc.setTransform( affineTransform );

      gc.drawImage( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null );
    } finally {
      gc.setTransform( oldTransform );
    }
  }
}
