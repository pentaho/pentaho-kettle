/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
