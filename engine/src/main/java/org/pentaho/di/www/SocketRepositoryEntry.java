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
