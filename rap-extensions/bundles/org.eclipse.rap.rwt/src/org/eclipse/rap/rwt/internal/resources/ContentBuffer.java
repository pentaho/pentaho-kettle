/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ContentBuffer {

  private final ByteArrayOutputStream bufferOutputStream;

  public ContentBuffer() {
    bufferOutputStream = new ByteArrayOutputStream();
  }

  public void append( byte[] content ) {
    if( content.length > 0 ) {
      bufferOutputStream.write( content, 0, content.length );
      bufferOutputStream.write( '\n' );
    }
  }

  public void append( InputStream inputStream ) throws IOException {
    byte[] buffer = new byte[ 40960 ];
    boolean contentWritten = false;
    int read = inputStream.read( buffer );
    while( read != -1 ) {
      bufferOutputStream.write( buffer, 0, read );
      read = inputStream.read( buffer );
      contentWritten = true;
    }
    if( contentWritten ) {
      bufferOutputStream.write( '\n' );
    }
  }

  public InputStream getContentAsStream() {
    return new ByteArrayInputStream( bufferOutputStream.toByteArray() );
  }

  public byte[] getContent() {
    return bufferOutputStream.toByteArray();
  }

}
