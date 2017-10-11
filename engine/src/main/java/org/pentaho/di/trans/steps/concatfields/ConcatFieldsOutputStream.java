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
      streamdata = new byte[b.length];
      System.arraycopy( b, 0, streamdata, 0, b.length );
    } else {
      byte[] tmp = new byte[streamdata.length + b.length];
      System.arraycopy( streamdata, 0, tmp, 0, streamdata.length );
      System.arraycopy( b, 0, tmp, streamdata.length, b.length );
      // now copy tmp back to streamdata
      streamdata = new byte[tmp.length];
      System.arraycopy( tmp, 0, streamdata, 0, tmp.length );
    }
  }

  // read and flush
  public byte[] read() {
    if ( streamdata == null ) {
      return null;
    }
    byte[] tmp = new byte[streamdata.length];
    System.arraycopy( streamdata, 0, tmp, 0, streamdata.length );
    streamdata = null;
    return tmp;
  }

  @Override
  public void write( int b ) throws IOException {
    if ( streamdata == null ) {
      streamdata = new byte[1];
      streamdata[0] = (byte) b;
    } else {
      byte[] tmp = new byte[streamdata.length + 1];
      System.arraycopy( streamdata, 0, tmp, 0, streamdata.length );
      tmp[tmp.length - 1] = (byte) b;
      // now copy tmp back to streamdata
      streamdata = new byte[tmp.length];
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
