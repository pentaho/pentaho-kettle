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
package org.pentaho.di.core.util;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.pentaho.di.core.exception.KettleException;

/**
 * Utility class for socket related methods
 */
public class SocketUtil {
  /**
   * Attempts to connect to the specified host, wrapping any exceptions in a KettleException
   *
   * @param host
   *          the host to connect to
   * @param port
   *          the port to connect to
   * @param timeout
   *          the timeout
   * @throws KettleException
   */
  public static void connectToHost( String host, int port, int timeout ) throws KettleException {
    Socket socket = new Socket();
    try {
      InetSocketAddress is = new InetSocketAddress( host, port );
      if ( timeout < 0 ) {
        socket.connect( is );
      } else {
        socket.connect( is, timeout );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      try {
        socket.close();
      } catch ( Exception e ) {
        // Ignore
      }
    }
  }
}
