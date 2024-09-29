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
