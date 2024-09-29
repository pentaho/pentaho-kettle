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

package org.pentaho.di.core.compress.snappy;

import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.xerial.snappy.SnappyOutputStream;

public class SnappyCompressionOutputStream extends CompressionOutputStream {

  public SnappyCompressionOutputStream( OutputStream out, CompressionProvider provider ) throws IOException {
    super( getDelegate( out ), provider );
  }

  private static SnappyOutputStream getDelegate( OutputStream out ) throws IOException {
    SnappyOutputStream delegate;
    if ( out instanceof SnappyOutputStream ) {
      delegate = (SnappyOutputStream) out;
    } else {
      delegate = new SnappyOutputStream( out );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    SnappyOutputStream zos = (SnappyOutputStream) delegate;
    zos.flush();
    zos.close();
  }

  @Override
  public void write( int b ) throws IOException {
    delegate.write( b );
  }
}
