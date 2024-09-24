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
package org.pentaho.di.core.util;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.pentaho.di.core.exception.KettleException;

/**
 * Utility class for socket related methods
 */
public class SocketUtil {

  private SocketUtil() {
  }

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
    try ( Socket socket = new Socket() ) {
      InetSocketAddress is = new InetSocketAddress( host, port );
      if ( timeout < 0 ) {
        socket.connect( is );
      } else {
        socket.connect( is, timeout );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }
}
