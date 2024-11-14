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


package org.pentaho.di.core.compress.hadoopsnappy;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class HadoopSnappyCompressionOutputStream extends CompressionOutputStream {

  public HadoopSnappyCompressionOutputStream( OutputStream out, CompressionProvider provider ) throws IOException {
    super( getDelegate( out ), provider );
  }

  private static OutputStream getDelegate( OutputStream out ) throws IOException {
    try {
      return getSnappyOutputStream( out );
    } catch ( Exception e ) {
      throw new IOException( e );
    }
  }

  /**
   * Gets an OutputStream that uses the snappy codec and wraps the supplied base output stream.
   *
   * @param the
   *          buffer size for the codec to use (in bytes)
   * @param out
   *          the base output stream to wrap around
   * @return a OutputStream that uses the Snappy codec
   *
   * @throws Exception
   *           if snappy is not available or an error occurs during reflection
   */
  public static OutputStream getSnappyOutputStream( OutputStream out ) throws Exception {
    return getSnappyOutputStream( HadoopSnappyCompressionProvider.IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE, out );
  }

  /**
   * Gets an OutputStream that uses the snappy codec and wraps the supplied base output stream.
   *
   * @param the
   *          buffer size for the codec to use (in bytes)
   *
   * @param out
   *          the base output stream to wrap around
   * @return a OutputStream that uses the Snappy codec
   *
   * @throws Exception
   *           if snappy is not available or an error occurs during reflection
   */
  public static OutputStream getSnappyOutputStream( int bufferSize, OutputStream out ) throws Exception {
    if ( !HadoopSnappyCompressionProvider.isHadoopSnappyAvailable() ) {
      throw new Exception( "Hadoop-snappy does not seem to be available" );
    }

    Object snappyShim = HadoopSnappyCompressionProvider.getActiveSnappyShim();
    Method getSnappyOutputStream =
        snappyShim.getClass().getMethod( "getSnappyOutputStream", int.class, OutputStream.class );
    return (OutputStream) getSnappyOutputStream.invoke( snappyShim, bufferSize, out );
  }
}
