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

/**
 * The CompressionProvider interface specifies the operations needed to interact with a stream whose data is compressed
 * on output and decompressed on input.
 *
 */
public interface CompressionProvider {

  /**
   * Creates an input stream that decompresses data (according to the provider's implementation)
   *
   * @param in
   *          An existing input stream to wrap
   * @return a CompressionInputStream object that decompresses incoming data
   * @throws IOException
   */
  CompressionInputStream createInputStream( InputStream in ) throws IOException;

  /**
   * Whether this compression provider supports input streams
   *
   * @return true if the provider supports input streams, false otherwise
   */
  boolean supportsInput();

  /**
   * Creates an output stream that compresses data (according to the provider's implementation)
   *
   * @param out
   *          An existing output stream to wrap
   * @return a CompressionOutputStream object that compresses outgoing data
   * @throws IOException
   */
  CompressionOutputStream createOutputStream( OutputStream out ) throws IOException;

  /**
   * Whether this compression provider supports output streams
   *
   * @return true if the provider supports output streams, false otherwise
   */
  boolean supportsOutput();

  /**
   * Gets the name of this provider. Used for display and as a reference in saved artifacts (transformations, e.g.)
   *
   * @return A String containing the name of this provider
   */
  String getName();

  /**
   * Gets the name of this provider. Used for display e.g.
   *
   * @return A String containing a description of this provider
   */
  String getDescription();

  /**
   * Gets the default file extension for this provider. If the streams are wrapped in File streams, this method can be
   * used to determine an appropriate extension to append to the filename so the file will be recognized as an artifact
   * of the compression mechanism (.zip, .bz2, e.g.)
   *
   * @return A String containing the default file extension for this provider
   */
  String getDefaultExtension();
}
