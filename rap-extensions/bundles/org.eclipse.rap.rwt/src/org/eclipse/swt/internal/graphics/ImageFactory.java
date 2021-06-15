/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
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

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.io.InputStream;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer.InstanceCreator;
import org.eclipse.rap.rwt.internal.util.StreamUtil;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;


public class ImageFactory {

  private final SharedInstanceBuffer<String,Image> cache;

  public static String getImagePath( Image image ) {
    String result = null;
    if( image != null ) {
      InternalImage internalImage = image.internalImage;
      String resourceName = internalImage.getResourceName();
      if( internalImage.isExternal() ) {
        result = resourceName;
      } else {
        result = RWT.getResourceManager().getLocation( resourceName );
      }
    }
    return result;
  }

  public ImageFactory() {
    cache = new SharedInstanceBuffer<String,Image>();
  }

  public Image findImage( String path ) {
    return findImage( path, ImageFactory.class.getClassLoader() );
  }

  public Image findImage( final String path, final ClassLoader imageLoader ) {
    return cache.get( path, new InstanceCreator<String, Image>() {
      public Image createInstance( String path ) {
        return createImage( path, imageLoader );
      }
    } );
  }

  public Image findImage( final String path, final InputStream inputStream ) {
    return cache.get( path, new InstanceCreator<String, Image>() {
      public Image createInstance( String path ) {
        return createImage( null, path, inputStream );
      }
    } );
  }

  private Image createImage( String path, ClassLoader imageLoader ) {
    Image result;
    InputStream inputStream = imageLoader.getResourceAsStream( path );
    try {
      result = createImage( null, path, inputStream );
    } finally {
      if( inputStream != null ) {
        StreamUtil.close( inputStream );
      }
    }
    return result;
  }

  public Image createImage( Device device, String key, InputStream inputStream ) {
    InternalImageFactory internalImageFactory = getApplicationContext().getInternalImageFactory();
    InternalImage internalImage = internalImageFactory.findInternalImage( key, inputStream );
    return createImageInstance( device, internalImage );
  }

  static Image createImageInstance( Device device, InternalImage internalImage ) {
    Class[] paramTypes = new Class[] { Device.class, InternalImage.class };
    Object[] paramValues = new Object[] { device, internalImage };
    return ClassUtil.newInstance( Image.class, paramTypes, paramValues );
  }

}
