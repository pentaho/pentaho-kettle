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


package org.pentaho.di.core.compress.snappy;

import java.io.IOException;
import java.io.InputStream;

import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.xerial.snappy.SnappyInputStream;

public class SnappyCompressionInputStream extends CompressionInputStream {

  public SnappyCompressionInputStream( InputStream in, CompressionProvider provider ) throws IOException {
    super( getDelegate( in ), provider );
  }

  protected static SnappyInputStream getDelegate( InputStream in ) throws IOException {
    SnappyInputStream delegate = null;
    if ( in instanceof SnappyInputStream ) {
      delegate = (SnappyInputStream) in;
    } else {
      delegate = new SnappyInputStream( in );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    ( (SnappyInputStream) delegate ).close();
  }

  @Override
  public int read() throws IOException {
    return ( (SnappyInputStream) delegate ).read();
  }

  @Override
  public Object nextEntry() throws IOException {
    return null;
  }

}
