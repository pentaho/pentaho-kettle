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


package org.pentaho.di.trans.steps.concatfields;

import java.io.IOException;
import java.io.OutputStream;

/*
 * ConcatFieldsOutputStream
 *
 * With this class you are able to use the existing OutputStream.write from TextFileOutput
 *
 * @author jb
 * @since 2012-08-31
 *
 */
public class ConcatFieldsOutputStream extends OutputStream {

  private byte[] streamdata;

  @Override
  public void write( byte[] b ) throws IOException {
    if ( streamdata == null ) {
      streamdata = new byte[ b.length ];
      System.arraycopy( b, 0, streamdata, 0, b.length );
    } else {
      byte[] tmp = new byte[ streamdata.length + b.length ];
      System.arraycopy( streamdata, 0, tmp, 0, streamdata.length );
      System.arraycopy( b, 0, tmp, streamdata.length, b.length );
      // now copy tmp back to streamdata
      streamdata = new byte[ tmp.length ];
      System.arraycopy( tmp, 0, streamdata, 0, tmp.length );
    }
  }

  // read and flush
  public byte[] read() {
    if ( streamdata == null ) {
      return null;
    }
    byte[] tmp = new byte[ streamdata.length ];
    System.arraycopy( streamdata, 0, tmp, 0, streamdata.length );
    streamdata = null;
    return tmp;
  }

  @Override
  public void write( int b ) throws IOException {
    if ( streamdata == null ) {
      streamdata = new byte[ 1 ];
      streamdata[ 0 ] = (byte) b;
    } else {
      byte[] tmp = new byte[ streamdata.length + 1 ];
      System.arraycopy( streamdata, 0, tmp, 0, streamdata.length );
      tmp[ tmp.length - 1 ] = (byte) b;
      // now copy tmp back to streamdata
      streamdata = new byte[ tmp.length ];
      System.arraycopy( tmp, 0, streamdata, 0, tmp.length );
    }
  }

  public void flush() throws IOException {
    // do nothing here since TextFileOutput calls this flush every 4K

  }

  public void close() throws IOException {
    streamdata = null;
  }

}
