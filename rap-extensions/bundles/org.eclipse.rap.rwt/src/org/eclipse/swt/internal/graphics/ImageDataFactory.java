/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import java.io.InputStream;

import org.eclipse.rap.rwt.internal.util.StreamUtil;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.graphics.ImageData;


public class ImageDataFactory {
  private final ResourceManager resourceManager;
  private final ImageDataCache imageDataCache;
  
  public ImageDataFactory( ResourceManager resourceManager ) {
    this.resourceManager = resourceManager;
    this.imageDataCache = new ImageDataCache();
  }

  public ImageData findImageData( InternalImage internalImage ) {
    ImageData result;
    // Note [rst]: We don't need to synchronize access here. Since the creation
    //             of ImageData is deterministic, at worst it is done more than
    //             once when accessed concurrently.
    result = imageDataCache.getImageData( internalImage );
    if( result == null ) {
      result = createImageData( internalImage );
      if( result != null ) {
        imageDataCache.putImageData( internalImage, result );
      }
    }
    return result;
  }

  private ImageData createImageData( InternalImage internalImage ) {
    ImageData result = null;
    String imagePath = internalImage.getResourceName();
    InputStream inputStream = resourceManager.getRegisteredContent( imagePath );
    if( inputStream != null ) {
      try {
        result = new ImageData( inputStream );
      } finally {
        StreamUtil.close( inputStream );
      }
    }
    return result;
  }
}
