/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public final class ResourceUtil {

  static void write( File file, byte[] content ) throws IOException {
    FileOutputStream fos = new FileOutputStream( file );
    try {
      OutputStream out = new BufferedOutputStream( fos );
      try {
        out.write( content );
      } finally {
        out.close();
      }
    } finally {
      fos.close();
    }
  }

  public static byte[] readBinary( InputStream stream ) throws IOException {
    ByteArrayOutputStream bufferedResult = new ByteArrayOutputStream();
    BufferedInputStream bufferedStream = new BufferedInputStream( stream );
    byte[] buffer = new byte[ 256 ];
    int read = bufferedStream.read( buffer );
    while( read != -1 ) {
      bufferedResult.write( buffer, 0, read );
      read = bufferedStream.read( buffer );
    }
    return bufferedResult.toByteArray();
  }

  private ResourceUtil() {
    // prevent instantiation
  }
}
