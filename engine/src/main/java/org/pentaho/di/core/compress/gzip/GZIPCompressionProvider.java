/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.compress.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.di.core.compress.CompressionProvider;

public class GZIPCompressionProvider implements CompressionProvider {

  @Override
  public GZIPCompressionInputStream createInputStream( InputStream in ) throws IOException {
    return new GZIPCompressionInputStream( in, this );
  }

  @Override
  public boolean supportsInput() {
    return true;
  }

  @Override
  public GZIPCompressionOutputStream createOutputStream( OutputStream out ) throws IOException {
    return new GZIPCompressionOutputStream( out, this );
  }

  @Override
  public boolean supportsOutput() {
    return true;
  }

  @Override
  public String getDescription() {
    return "GZIP compression";
  }

  @Override
  public String getName() {
    return "GZip";
  }

  @Override
  public String getDefaultExtension() {
    return "gz";
  }
}
