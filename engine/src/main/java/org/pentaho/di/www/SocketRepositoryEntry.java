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

import java.net.ServerSocket;

/**
 * This entry contains a server socket as well as detailed about the process that is using it.
 *
 * @author matt
 *
 */
public class SocketRepositoryEntry {
  private int port;
  private ServerSocket serverSocket;
  private boolean inUse;
  private String user;

  /**
   * @param port
   * @param serverSocket
   * @param inUse
   * @param user
   */
  public SocketRepositoryEntry( int port, ServerSocket serverSocket, boolean inUse, String user ) {
    this.port = port;
    this.serverSocket = serverSocket;
    this.inUse = inUse;
    this.user = user;
  }

  public int hashCode() {
    return Integer.valueOf( port ).hashCode();
  }

  public boolean equals( Object e ) {
    if ( this == e ) {
      return true;
    }
    if ( !( e instanceof SocketRepositoryEntry ) ) {
      return false;
    }

    SocketRepositoryEntry entry = (SocketRepositoryEntry) e;

    return ( entry.port == port );
  }

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( int port ) {
    this.port = port;
  }

  /**
   * @return the serverSocket
   */
  public ServerSocket getServerSocket() {
    return serverSocket;
  }

  /**
   * @param serverSocket
   *          the serverSocket to set
   */
  public void setServerSocket( ServerSocket serverSocket ) {
    this.serverSocket = serverSocket;
  }

  /**
   * @return the inUse
   */
  public boolean isInUse() {
    return inUse;
  }

  /**
   * @param inUse
   *          the inUse to set
   */
  public void setInUse( boolean inUse ) {
    this.inUse = inUse;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user
   *          the user to set
   */
  public void setUser( String user ) {
    this.user = user;
  }

}
