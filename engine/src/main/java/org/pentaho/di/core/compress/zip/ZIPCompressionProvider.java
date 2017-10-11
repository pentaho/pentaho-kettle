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
