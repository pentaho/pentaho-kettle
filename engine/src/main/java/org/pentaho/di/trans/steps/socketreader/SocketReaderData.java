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
