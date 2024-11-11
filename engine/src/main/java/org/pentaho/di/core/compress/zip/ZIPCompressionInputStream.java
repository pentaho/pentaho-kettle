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


package org.pentaho.di.core.compress.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class ZIPCompressionInputStream extends CompressionInputStream {
  private static final String INVALID_INPUT_MSG = "Not a valid input stream!";

  public ZIPCompressionInputStream( InputStream in, CompressionProvider provider ) {
    super( getDelegate( in ), provider );
  }

  protected static ZipInputStream getDelegate( InputStream in ) {
    ZipInputStream delegate;
    if ( in instanceof ZipInputStream ) {
      delegate = (ZipInputStream) in;
    } else {
      delegate = new ZipInputStream( in );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    ZipInputStream zis = (ZipInputStream) delegate;
    if ( zis == null ) {
      throw new IOException( INVALID_INPUT_MSG );
    }
    zis.close();
  }

  @Override
  public int read() throws IOException {
    ZipInputStream zis = (ZipInputStream) delegate;
    if ( zis == null ) {
      throw new IOException( INVALID_INPUT_MSG );
    }
    return zis.read();
  }

  @Override
  public Object nextEntry() throws IOException {
    ZipInputStream zis = (ZipInputStream) delegate;
    if ( zis == null ) {
      throw new IOException( INVALID_INPUT_MSG );
    }
    return zis.getNextEntry();
  }

}
