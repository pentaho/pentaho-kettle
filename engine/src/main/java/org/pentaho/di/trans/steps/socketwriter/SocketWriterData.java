/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.socketwriter;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 27-nov-2006
 *
 */
public class SocketWriterData extends BaseStepData implements StepDataInterface {
  public DataOutputStream outputStream;
  public Socket clientSocket;
  public int flushInterval;
  public ServerSocket serverSocket;
  int serverSocketPort;

  public SocketWriterData() {
    super();
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      if ( clientSocket != null ) {
        clientSocket.shutdownInput();
        clientSocket.shutdownOutput();
        clientSocket.close();
      }
      if ( serverSocket != null ) {
        serverSocket.close();
      }
    } catch ( java.io.IOException e ) {
      // Ignore errors
    } finally {
      super.finalize();
    }
  }

}
