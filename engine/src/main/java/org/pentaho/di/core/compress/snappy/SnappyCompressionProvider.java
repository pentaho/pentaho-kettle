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


package org.pentaho.di.core.compress.snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.di.core.compress.CompressionProvider;

public class SnappyCompressionProvider implements CompressionProvider {

  @Override
  public SnappyCompressionInputStream createInputStream( InputStream in ) throws IOException {
    return new SnappyCompressionInputStream( in, this );
  }

  @Override
  public boolean supportsInput() {
    return true;
  }

  @Override
  public SnappyCompressionOutputStream createOutputStream( OutputStream out ) throws IOException {
    return new SnappyCompressionOutputStream( out, this );
  }

  @Override
  public boolean supportsOutput() {
    return true;
  }

  @Override
  public String getDescription() {
    return "Snappy compression";
  }

  @Override
  public String getName() {
    return "Snappy";
  }

  @Override
  public String getDefaultExtension() {
    return null;
  }

  /*
   * } else if ( sFileCompression != null && sFileCompression.equals( "Hadoop-snappy" ) ) { if ( log.isDetailed() ) {
   * logDetailed( "This is a snappy compressed file" ); } // data.sis = new SnappyInputStream(data.fr); data.sis =
   * HadoopCompression.getSnappyInputStream( data.in ); if ( meta.getEncoding() != null && meta.getEncoding().length() >
   * 0 ) { data.isr = new InputStreamReader( new BufferedInputStream( data.sis, BUFFER_SIZE_INPUT_STREAM ),
   * meta.getEncoding() ); } else { data.isr = new InputStreamReader( new BufferedInputStream( data.sis,
   * BUFFER_SIZE_INPUT_STREAM ) ); } }
   */
}
