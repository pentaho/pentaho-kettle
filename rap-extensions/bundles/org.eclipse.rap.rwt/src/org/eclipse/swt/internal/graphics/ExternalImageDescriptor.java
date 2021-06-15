/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;


public class ExternalImageDescriptor {

  private final InternalImage internalImage;

  public ExternalImageDescriptor( String url, int width, int height ) {
    ParamCheck.notNull( url, "url" );
    if( width <= 0 || height <= 0 ) {
      throw new IllegalArgumentException( "Illegal size" );
    }
    internalImage = new InternalImage( url, width, height, true );
  }

  public Image createImage( Device device ) {
    ParamCheck.notNull( device, "device" );
    return ImageFactory.createImageInstance( device, internalImage );
  }

}
