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

package org.pentaho.di.core.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NoneCompressionProvider implements CompressionProvider {

  @Override
  public CompressionInputStream createInputStream( InputStream in ) throws IOException {
    return new NoneCompressionInputStream( in, this );
  }

  @Override
  public boolean supportsInput() {
    return true;
  }

  @Override
  public CompressionOutputStream createOutputStream( OutputStream out ) throws IOException {
    return new NoneCompressionOutputStream( out, this );
  }

  @Override
  public boolean supportsOutput() {
    return true;
  }

  /**
   * Gets the description for the compression provider.
   *
   * @return a description of the provider. For core plugins this needs to match what's in the XML file
   */
  @Override
  public String getDescription() {
    return "No compression";
  }

  /**
   * Gets the name for the compression provider.
   *
   * @return the name of the provider. For core plugins this needs to match what's in the XML file
   */
  @Override
  public String getName() {
    return "None";
  }

  @Override
  public String getDefaultExtension() {
    return null;
  }

  public static class NoneCompressionInputStream extends CompressionInputStream {

    public NoneCompressionInputStream( InputStream in, CompressionProvider provider ) {
      super( in, provider );
    }

  }

  public static class NoneCompressionOutputStream extends CompressionOutputStream {

    public NoneCompressionOutputStream( OutputStream out, CompressionProvider provider ) {
      super( out, provider );
    }
  }
}
