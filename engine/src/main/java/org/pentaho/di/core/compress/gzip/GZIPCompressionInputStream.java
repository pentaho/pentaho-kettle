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

package org.pentaho.di.core.compress.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class GZIPCompressionInputStream extends CompressionInputStream {

  public GZIPCompressionInputStream( InputStream in, CompressionProvider provider ) throws IOException {
    super( getDelegate( in ), provider );
  }

  protected static GZIPInputStream getDelegate( InputStream in ) throws IOException {
    GZIPInputStream delegate = null;
    if ( in instanceof GZIPInputStream ) {
      delegate = (GZIPInputStream) in;
    } else {
      delegate = new GZIPInputStream( in );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    GZIPInputStream gis = (GZIPInputStream) delegate;
    gis.close();
  }

  @Override
  public int read() throws IOException {
    GZIPInputStream gis = (GZIPInputStream) delegate;
    return gis.read();
  }

}
