/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Transform;

public class SwtUniversalImageBitmap extends SwtUniversalImage {
  private final Image bitmap;

  public SwtUniversalImageBitmap( Image bitmap ) {
    this.bitmap = bitmap;
  }

  @Override
  public synchronized void dispose() {
    super.dispose();
    if ( !bitmap.isDisposed() ) {
      bitmap.dispose();
    }
  }

  @Override
  protected Image renderSimple( Device device ) {
    return bitmap;
  }

  @Override
  protected Image renderSimple( Device device, int width, int height ) {
    ImageData imageData = bitmap.getImageData();
    ImageData scaledData = imageData.scaledTo( width, height );
    return new Image( device, scaledData );
  }

  @Override
  protected Image renderRotated( Device device, int width, int height, double angleRadians ) {
    Image result = new Image( device, width * 2, height * 2 );

    GC gc = new GC( result );

    int bw = bitmap.getBounds().width;
    int bh = bitmap.getBounds().height;
    Transform affineTransform = new Transform( device );
    affineTransform.translate( width, height );
    affineTransform.rotate( (float) Math.toDegrees( angleRadians ) );
    affineTransform.scale( (float) 1.0 * width / bw, (float) 1.0 * height / bh );
    gc.setTransform( affineTransform );

    gc.drawImage( bitmap, 0, 0, bw, bh, -bw / 2, -bh / 2, bw, bh );

    gc.dispose();

    return result;
  }
}
