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


package org.pentaho.di.trans.steps.socketreader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 27-nov-2006
 *
 */
public class SocketReaderData extends BaseStepData implements StepDataInterface {

  public Socket socket;
  public DataOutputStream outputStream;
  public DataInputStream inputStream;
  public RowMetaInterface rowMeta;

  public SocketReaderData() {
    super();
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      if ( socket != null ) {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
      }
    } catch ( java.io.IOException e ) {
      // Ignore errors
    } finally {
      super.finalize();
    }
  }

}
