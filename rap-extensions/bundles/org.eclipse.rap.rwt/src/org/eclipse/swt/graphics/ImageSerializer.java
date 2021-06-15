/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.graphics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.rap.rwt.internal.engine.PostDeserialization;
import org.eclipse.rap.rwt.internal.resources.ResourceUtil;
import org.eclipse.rap.rwt.internal.util.StreamUtil;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Display;


class ImageSerializer {

  private static class SerializableBytes implements Serializable {

    final byte[] data;

    SerializableBytes( byte[] data ) {
      this.data = data;
    }
  }

  private class PostDeserializationValidation implements ObjectInputValidation {
    private final SerializableBytes imageBytes;

    PostDeserializationValidation( SerializableBytes imageBytes ) {
      this.imageBytes = imageBytes;
    }

    public void validateObject() throws InvalidObjectException {
      PostDeserialization.addProcessor( getUISession(), new Runnable() {
        public void run() {
          InputStream inputStream = new ByteArrayInputStream( imageBytes.data );
          getResourceManager().register( image.internalImage.getResourceName(), inputStream );
        }
      } );
    }
  }

  private final Image image;

  ImageSerializer( Image image ) {
    this.image = image;
  }

  void writeObject( ObjectOutputStream stream ) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject( new SerializableBytes( getImageBytes() ) );
  }

  void readObject( ObjectInputStream stream ) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    SerializableBytes imageBytes = ( SerializableBytes )stream.readObject();
    stream.registerValidation( new PostDeserializationValidation( imageBytes ), 0 );
  }

  private byte[] getImageBytes() {
    String resourceName = image.internalImage.getResourceName();
    InputStream inputStream = getResourceManager().getRegisteredContent( resourceName );
    try {
      return ResourceUtil.readBinary( inputStream );
    } catch( IOException ioe ) {
      throw new RuntimeException( ioe );
    } finally {
      StreamUtil.close( inputStream );
    }
  }

  private UISession getUISession() {
    Display display = ( Display )image.getDevice();
    IDisplayAdapter adapter = display.getAdapter( IDisplayAdapter.class );
    return adapter.getUISession();
  }

  private ResourceManager getResourceManager() {
    return getUISession().getApplicationContext().getResourceManager();
  }
}
