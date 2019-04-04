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

package org.pentaho.di.core.compress.hadoopsnappy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class HadoopSnappyCompressionInputStream extends CompressionInputStream {

  public HadoopSnappyCompressionInputStream( InputStream in, CompressionProvider provider ) throws IOException {
    super( getDelegate( in ), provider );
  }

  protected static InputStream getDelegate( InputStream in ) throws IOException {
    try {
      return getSnappyInputStream( in );
    } catch ( Exception e ) {
      throw new IOException( e );
    }
  }

  @Override
  public Object nextEntry() throws IOException {
    return null;
  }

  /**
   * Gets a CompressionInputStream that uses the snappy codec and wraps the supplied base input stream.
   *
   * @param in
   *          the base input stream to wrap around
   * @return an InputStream that uses the Snappy codec
   *
   * @throws Exception
   *           if snappy is not available or an error occurs during reflection
   */
  public static InputStream getSnappyInputStream( InputStream in ) throws Exception {
    return getSnappyInputStream( HadoopSnappyCompressionProvider.IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE, in );
  }

  /**
   * Gets an InputStream that uses the snappy codec and wraps the supplied base input stream.
   *
   * @param the
   *          buffer size for the codec to use (in bytes)
   * @param in
   *          the base input stream to wrap around
   * @return an InputStream that uses the Snappy codec
   *
   * @throws Exception
   *           if snappy is not available or an error occurs during reflection
   */
  public static InputStream getSnappyInputStream( int bufferSize, InputStream in ) throws Exception {
    if ( !HadoopSnappyCompressionProvider.isHadoopSnappyAvailable() ) {
      throw new Exception( "Hadoop-snappy does not seem to be available" );
    }

    Object snappyShim = HadoopSnappyCompressionProvider.getActiveSnappyShim();
    Method getSnappyInputStream =
        snappyShim.getClass().getMethod( "getSnappyInputStream", int.class, InputStream.class );
    return (InputStream) getSnappyInputStream.invoke( snappyShim, bufferSize, in );
  }
}
