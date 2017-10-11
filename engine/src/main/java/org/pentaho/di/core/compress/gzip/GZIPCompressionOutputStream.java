/*! ******************************************************************************
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

package org.pentaho.di.core.compress.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class GZIPCompressionOutputStream extends CompressionOutputStream {

  public GZIPCompressionOutputStream( OutputStream out, CompressionProvider provider ) throws IOException {
    super( getDelegate( out ), provider );

  }

  protected static GZIPOutputStream getDelegate( OutputStream out ) throws IOException {
    GZIPOutputStream delegate = null;
    if ( out instanceof ZipOutputStream ) {
      delegate = (GZIPOutputStream) out;
    } else {
      delegate = new GZIPOutputStream( out );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    GZIPOutputStream zos = (GZIPOutputStream) delegate;
    zos.close();
  }
}
