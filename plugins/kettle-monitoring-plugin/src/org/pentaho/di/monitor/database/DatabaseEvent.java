/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.di.monitor.database;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.monitor.IKettleMonitoringEvent;

import java.io.Serializable;

public class DatabaseEvent implements IKettleMonitoringEvent {

  private static final long serialVersionUID = 5995750699747792833L;

  private static final String ID = "1.1.1.1.1.1.1.1"; // TODO replace with an actual oid

  public static enum EventType {CONNECTED, DISCONNECTED}

  private Serializable id = ID;
  private EventType eventType;
  private String databaseName;
  private String driver;
  private String hostname;
  private String server;
  private String port;
  private int initialPoolSize;
  private int maxPoolSize;
  private String connectionUrl;
  private String username;

  public DatabaseEvent( EventType eventType ) {
    this.eventType = eventType;
  }

  @Override
  public Serializable getId() {
    return id;
  }

  public void setId( Serializable id ) {
    this.id = id;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType( EventType eventType ) {
    this.eventType = eventType;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName( String databaseName ) {
    this.databaseName = databaseName;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver( String driver ) {
    this.driver = driver;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  public String getServer() {
    return server;
  }

  public void setServer( String server ) {
    this.server = server;
  }

  public String getPort() {
    return port;
  }

  public void setPort( String port ) {
    this.port = port;
  }

  public int getInitialPoolSize() {
    return initialPoolSize;
  }

  public void setInitialPoolSize( int initialPoolSize ) {
    this.initialPoolSize = initialPoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize( int maxPoolSize ) {
    this.maxPoolSize = maxPoolSize;
  }

  public String getConnectionUrl() {
    return connectionUrl;
  }

  public void setConnectionUrl( String connectionUrl ) {
    this.connectionUrl = connectionUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public DatabaseEvent build( DatabaseMeta dbm ) throws KettleException {

    if ( dbm == null ) {
      return this;
    }

    setConnectionUrl( dbm.getURL() );
    setDatabaseName( dbm.getDatabaseName() );
    setPort( dbm.getDatabasePortNumberString() );
    setHostname( dbm.getHostname() );
    setServer( dbm.getServername() );
    setDriver( dbm.getDriverClass() );
    setInitialPoolSize( dbm.getInitialPoolSize() );
    setMaxPoolSize( dbm.getMaximumPoolSize() );
    setUsername( dbm.getUsername() );

    return this;
  }
}
