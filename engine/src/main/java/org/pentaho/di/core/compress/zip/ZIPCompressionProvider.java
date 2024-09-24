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
import java.io.OutputStream;

import org.pentaho.di.core.compress.CompressionProvider;

public class ZIPCompressionProvider implements CompressionProvider {

  @Override
  public ZIPCompressionInputStream createInputStream( InputStream in ) throws IOException {
    return new ZIPCompressionInputStream( in, this );
  }

  @Override
  public boolean supportsInput() {
    return true;
  }

  @Override
  public ZIPCompressionOutputStream createOutputStream( OutputStream out ) throws IOException {
    return new ZIPCompressionOutputStream( out, this );
  }

  @Override
  public boolean supportsOutput() {
    return true;
  }

  @Override
  public String getDescription() {
    return "ZIP compression";
  }

  @Override
  public String getName() {
    return "Zip";
  }

  @Override
  public String getDefaultExtension() {
    return "zip";
  }
}
