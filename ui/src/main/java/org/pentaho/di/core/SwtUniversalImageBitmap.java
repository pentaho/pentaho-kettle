/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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
