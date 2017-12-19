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

package org.pentaho.di.www;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * This singleton keeps a repository of all the server sockets.
 *
 * @author matt
 *
 */
public class SocketRepository {

  /**
   * This map contains a link between a (clustered) transformation and their used server sockets
   */
  private Map<Integer, SocketRepositoryEntry> socketMap;

  private LogChannelInterface log;

  public SocketRepository( LogChannelInterface log ) {
    this.log = log;
    socketMap = new HashMap<Integer, SocketRepositoryEntry>();
  }

  private ServerSocket createServerSocket( int port ) throws IOException {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.setPerformancePreferences( 1, 2, 3 ); // order of importance: bandwidth, latency, connection time
    serverSocket.setReuseAddress( true );

    // It happens in high-paced environments where lots of sockets are opened and closed that the operating
    // system keeps a lock on a socket. Because of this we have to wait at least for one minute and on some platforms
    // up to 2 minutes.
    // Let's take 5 to make sure we can get a socket connection and we still get into trouble.
    //
    // mc: It sucks and blows at the same time that we have to do this but I couldn't find another solution.
    //
    try {
      serverSocket.bind( new InetSocketAddress( port ) );
    } catch ( BindException e ) {
      long totalWait = 0L;
      long startTime = System.currentTimeMillis();

      IOException ioException = null;
      log.logMinimal( "Carte socket repository : Starting a retry loop to bind the server socket on port "
        + port + ".  We retry for 5 minutes until the socket clears in your operating system." );
      while ( !serverSocket.isBound() && totalWait < 300000 ) {
        try {
          totalWait = System.currentTimeMillis() - startTime;
          log.logMinimal( "Carte socket repository : Retry binding the server socket on port "
            + port + " after a " + ( totalWait / 1000 ) + " seconds wait..." );
          Thread.sleep( 10000 ); // wait 10 seconds, try again...
          serverSocket.bind( new InetSocketAddress( port ), 100 );
        } catch ( IOException ioe ) {
          ioException = ioe;
        } catch ( Exception ex ) {
          serverSocket.close();
          throw new IOException( ex.getMessage() );
        }

        totalWait = System.currentTimeMillis() - startTime;
      }
      if ( !serverSocket.isBound() ) {
        serverSocket.close();
        throw ioException;
      }
      log.logDetailed( "Carte socket repository : Succesfully bound the server socket on port "
        + port + " after " + ( totalWait / 1000 ) + " seconds." );
    }
    return serverSocket;
  }

  public synchronized ServerSocket openServerSocket( int port, String user ) throws IOException {

    SocketRepositoryEntry entry = socketMap.get( port );
    if ( entry == null ) {

      ServerSocket serverSocket = createServerSocket( port );
      entry = new SocketRepositoryEntry( port, serverSocket, true, user );

      // Store the entry in the map too!
      //
      socketMap.put( port, entry );

    } else {
      // Verify that the socket is not in use...
      //
      if ( entry.isInUse() ) {
        throw new IOException( "Server socket on port " + port + " is already in use by [" + entry.getUser() + "]" );
      }
      if ( entry.getServerSocket().isClosed() ) {
        entry.setServerSocket( createServerSocket( port ) );
      }
      entry.setInUse( true );
    }

    return entry.getServerSocket();
  }

  /**
   * We don't actually ever close a server socket, we re-use them as much as possible.
   *
   * @param port
   * @throws IOException
   */
  public synchronized void releaseSocket( int port ) throws IOException {

    SocketRepositoryEntry entry = socketMap.get( port );
    if ( entry == null ) {
      throw new IOException( "Port to close was not found in the Carte socket repository!" );
    }
    entry.setInUse( false );
  }

  /**
   * @return the socketMap
   */
  public Map<Integer, SocketRepositoryEntry> getSocketMap() {
    return socketMap;
  }

  /**
   * @param socketMap
   *          the socketMap to set
   */
  public void setSocketMap( Map<Integer, SocketRepositoryEntry> socketMap ) {
    this.socketMap = socketMap;
  }

  /**
   * Closes all sockets on application end...
   *
   * @throws IOException
   *           in case there is an error
   */
  public synchronized void closeAll() {
    for ( Iterator<Map.Entry<Integer, SocketRepositoryEntry>> iterator = socketMap.entrySet().iterator();
          iterator.hasNext(); ) {
      Map.Entry<Integer, SocketRepositoryEntry> repositoryEntry = iterator.next();
      SocketRepositoryEntry entry = repositoryEntry.getValue();
      ServerSocket serverSocket = entry.getServerSocket();
      try {
        if ( serverSocket != null ) {
          serverSocket.close();
          iterator.remove();
        }
      } catch ( IOException e ) {
        log.logError( "Carte socket repository : Failed to close socket during shutdown", e );
      }
    }
  }

  protected void finalize() throws Throwable {
    try {
      closeAll();
    } catch ( Exception e ) {
      // Ignore errors
    } finally {
      super.finalize();
    }
  }
}
