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

import org.apache.commons.lang.math.NumberUtils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.monitor.OID;
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;
import org.pentaho.platform.api.monitoring.snmp.SnmpVariable;

import java.io.Serializable;
import java.util.Date;

@SnmpTrapEvent( oid = OID.DATABASE )
public class DatabaseEvent extends BaseEvent {

  private static final long serialVersionUID = 5995750699747792833L;

  // triggered Event Type
  private EventType.Database eventType;

  // database name
  private String databaseName;

  // database type (PostgreSQL, Oracle, MySQL, etc.)
  private String databaseType;

  // database access type (Native, JNDI etc.)
  private String databaseAccessType;

  // driver name pertaining this database connection
  private String driverName;

  // driver version pertaining this database connection
  private String driverVersion;

  // host name
  private String hostname;

  // server
  private String server;

  // port
  private int port;

  // initial pool size set for this database connection
  private int initialPoolSize;

  // maximum pool size set for this database connection
  private int maxPoolSize;

  // database connection url
  private String connectionUrl;

  // database connection group
  private String connectionGroup;

  // database connection name
  private String connectionName;

  // username used to connect to database
  private String username;

  // database partition
  private String partitionId;

  // log
  private String log;

  // upon database disconnection, the total number of executed commits
  private int nrExecutedCommits;

  // connection start time in millis
  private long connectionStartTimeMillis;

  // connection end time in millis
  private long connectionEndTimeMillis;

  // connection duration in millis
  private long connectionDurationMillis;

  // EventType.Boolean.TRUE if a DB connection has been established, EventType.Boolean.FALSE if it has been severed
  private int connected;

  public DatabaseEvent( EventType.Database eventType ) {
    this.eventType = eventType;
  }

  public DatabaseEvent build( Database db ) throws KettleException {

    if ( db == null ) {
      return this;
    }

    setConnected( EventType.Boolean.getValue( EventType.Database.CONNECTED == eventType ) );
    setPartitionId( db.getPartitionId() );
    setConnectionGroup( db.getConnectionGroup() );
    setLogChannelId( db.getLogChannelId() );
    setEventLogs( filterEventLogging( getLogChannelId() ) );
    setLog( getEventLogsAsString() );

    if ( db.getDatabaseMetaData() != null ) {
      try {
        setDriverName( db.getDatabaseMetaData().getDriverName() );
        setDriverVersion( db.getDatabaseMetaData().getDriverVersion() );
      } catch ( Throwable t ) {
        /* do nothing */
      }
    }

    if ( EventType.Database.DISCONNECTED == eventType ) {

      setConnectionEndTimeMillis( new Date().getTime() );
      setNrExecutedCommits( db.getNrExecutedCommits() );

      if ( getConnectionStartTimeMillis() != 0 ) {
        setConnectionDurationMillis( getConnectionEndTimeMillis() - getConnectionStartTimeMillis() );
      }
    }

    return build( db.getDatabaseMeta() );
  }

  public DatabaseEvent build( DatabaseMeta dbm ) throws KettleException {

    if ( dbm == null ) {
      return this;
    }

    setConnectionName( dbm.getName() );
    setConnectionUrl( dbm.getURL() );
    setDatabaseName( dbm.getDatabaseName() );
    setPort( NumberUtils.isDigits( dbm.getDatabasePortNumberString() )
      ? Integer.parseInt( dbm.getDatabasePortNumberString() ) : 0 );
    setHostname( dbm.getHostname() );
    setServer( dbm.getServername() );
    setInitialPoolSize( dbm.getInitialPoolSize() );
    setMaxPoolSize( dbm.getMaximumPoolSize() );
    setUsername( dbm.getUsername() );

    if ( dbm.getDatabaseInterface() != null ) {
      setDatabaseType( dbm.getDatabaseInterface().getPluginName() );
      setDatabaseAccessType( DatabaseMeta.getAccessTypeDesc( dbm.getDatabaseInterface().getAccessType() ) );
    }

    return this;
  }

  @Override
  public Serializable getId() {
    StringBuffer sb = new StringBuffer();
    sb.append( "[" ).append( getDriverName() ).append( ",v" ).append( getDriverVersion() ).append( "] " ).append(
      getUsername() ).append( "@" ).append( getHostname() ).append( ":" ).append( getPort() ).append( "/" )
      .append( getDatabaseName() );
    return sb.toString();
  }

  /**
   * triggered Event Type
   *
   * @return triggered Event Type
   */
  public EventType.Database getEventType() {
    return eventType;
  }

  /**
   * triggered Event Type
   *
   * @param eventType triggered Event Type
   */
  public void setEventType( EventType.Database eventType ) {
    this.eventType = eventType;
  }

  /**
   * database name
   *
   * @return database name
   */
  @SnmpVariable( oid = OID.DATABASE_NAME, type = SnmpVariable.TYPE.STRING )
  public String getDatabaseName() {
    return databaseName;
  }

  /**
   * database name
   *
   * @param databaseName database name
   */
  public void setDatabaseName( String databaseName ) {
    this.databaseName = databaseName;
  }

  /**
   * host name
   *
   * @return host name
   */
  @SnmpVariable( oid = OID.DATABASE_HOSTNAME, type = SnmpVariable.TYPE.STRING )
  public String getHostname() {
    return hostname;
  }

  /**
   * host name
   *
   * @param hostname host name
   */
  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  /**
   * server
   *
   * @return server
   */
  public String getServer() {
    return server;
  }

  /**
   * server
   *
   * @param server server
   */
  public void setServer( String server ) {
    this.server = server;
  }

  /**
   * port number
   *
   * @return port number
   */
  @SnmpVariable( oid = OID.DATABASE_PORT, type = SnmpVariable.TYPE.INTEGER )
  public int getPort() {
    return port;
  }

  /**
   * port number
   *
   * @param port port number
   */
  public void setPort( int port ) {
    this.port = port;
  }

  /**
   * initial pool size set for this database connection
   *
   * @return initial pool size set for this database connection
   */
  public int getInitialPoolSize() {
    return initialPoolSize;
  }

  /**
   * initial pool size set for this database connection
   *
   * @param initialPoolSize initial pool size set for this database connection
   */
  public void setInitialPoolSize( int initialPoolSize ) {
    this.initialPoolSize = initialPoolSize;
  }

  /**
   * maximum pool size set for this database connection
   *
   * @return maximum pool size set for this database connection
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * maximum pool size set for this database connection
   *
   * @param maxPoolSize maximum pool size set for this database connection
   */
  public void setMaxPoolSize( int maxPoolSize ) {
    this.maxPoolSize = maxPoolSize;
  }

  /**
   * database connection url
   *
   * @return database connection url
   */
  public String getConnectionUrl() {
    return connectionUrl;
  }

  /**
   * database connection url
   *
   * @param connectionUrl database connection url
   */
  public void setConnectionUrl( String connectionUrl ) {
    this.connectionUrl = connectionUrl;
  }

  /**
   * username used to connect to database
   *
   * @return username used to connect to database
   */
  @SnmpVariable( oid = OID.DATABASE_USER, type = SnmpVariable.TYPE.STRING )
  public String getUsername() {
    return username;
  }

  /**
   * username used to connect to database
   *
   * @param username username used to connect to database
   */
  public void setUsername( String username ) {
    this.username = username;
  }

  /**
   * database partition Id
   *
   * @return database partition Id
   */
  @SnmpVariable( oid = OID.DATABASE_PARTITION_ID, type = SnmpVariable.TYPE.STRING )
  public String getPartitionId() {
    return partitionId;
  }

  /**
   * database partition Id
   *
   * @param partitionId database partition Id
   */
  public void setPartitionId( String partitionId ) {
    this.partitionId = partitionId;
  }

  /**
   * log
   *
   * @return log
   */
  @SnmpVariable( oid = OID.DATABASE_LOG, type = SnmpVariable.TYPE.STRING )
  public String getLog() {
    return log;
  }

  /**
   * log
   *
   * @param log log
   */
  public void setLog( String log ) {
    this.log = log;
  }

  /**
   * database connection group
   *
   * @return database connection group
   */
  @SnmpVariable( oid = OID.DATABASE_CONN_GROUP, type = SnmpVariable.TYPE.STRING )
  public String getConnectionGroup() {
    return connectionGroup;
  }

  /**
   * database connection group
   *
   * @param connectionGroup database connection group
   */
  public void setConnectionGroup( String connectionGroup ) {
    this.connectionGroup = connectionGroup;
  }

  /**
   * database connection name
   *
   * @return database connection name
   */
  @SnmpVariable( oid = OID.DATABASE_CONN_NAME, type = SnmpVariable.TYPE.STRING )
  public String getConnectionName() {
    return connectionName;
  }

  /**
   * database connection name
   *
   * @param connectionName database connection name
   */
  public void setConnectionName( String connectionName ) {
    this.connectionName = connectionName;
  }

  /**
   * upon database disconnection, the total number of executed commits
   *
   * @return upon database disconnection, the total number of executed commits
   */
  @SnmpVariable( oid = OID.DATABASE_NR_EXEC_COMMITS, type = SnmpVariable.TYPE.INTEGER )
  public int getNrExecutedCommits() {
    return nrExecutedCommits;
  }

  /**
   * upon database disconnection, the total number of executed commits
   *
   * @param nrExecutedCommits upon database disconnection, the total number of executed commits
   */
  public void setNrExecutedCommits( int nrExecutedCommits ) {
    this.nrExecutedCommits = nrExecutedCommits;
  }

  /**
   * driver name pertaining this database connection
   *
   * @return driver name pertaining this database connection
   */
  public String getDriverName() {
    return driverName;
  }

  /**
   * driver name pertaining this database connection
   *
   * @param driverName driver name pertaining this database connection
   */
  public void setDriverName( String driverName ) {
    this.driverName = driverName;
  }

  /**
   * driver name pertaining this database connection
   *
   * @return driver name pertaining this database connection
   */
  public String getDriverVersion() {
    return driverVersion;
  }

  /**
   * driver name pertaining this database connection
   *
   * @param driverVersion driver name pertaining this database connection
   */
  public void setDriverVersion( String driverVersion ) {
    this.driverVersion = driverVersion;
  }

  /**
   * connection start time in millis
   *
   * @return connection start time in millis
   */
  public long getConnectionStartTimeMillis() {
    return connectionStartTimeMillis;
  }

  /**
   * connection start time in millis
   *
   * @param connectionStartTimeMillis connection start time in millis
   */
  public void setConnectionStartTimeMillis( long connectionStartTimeMillis ) {
    this.connectionStartTimeMillis = connectionStartTimeMillis;
  }

  /**
   * connection end time in millis
   *
   * @return connection end time in millis
   */
  public long getConnectionEndTimeMillis() {
    return connectionEndTimeMillis;
  }

  /**
   * connection end time in millis
   *
   * @param connectionEndTimeMillis connection end time in millis
   */
  public void setConnectionEndTimeMillis( long connectionEndTimeMillis ) {
    this.connectionEndTimeMillis = connectionEndTimeMillis;
  }

  /**
   * connection duration time in millis
   *
   * @return connection duration time in millis
   */
  public long getConnectionDurationMillis() {
    return connectionDurationMillis;
  }

  /**
   * connection duration time in millis
   *
   * @param connectionDurationMillis connection duration time in millis
   */
  public void setConnectionDurationMillis( long connectionDurationMillis ) {
    this.connectionDurationMillis = connectionDurationMillis;
  }

  /**
   * database type (PostgreSQL, Oracle, MySQL, etc.)
   *
   * @return database type (PostgreSQL, Oracle, MySQL, etc.)
   */
  @SnmpVariable( oid = OID.DATABASE_CONN_TYPE, type = SnmpVariable.TYPE.STRING )
  public String getDatabaseType() {
    return databaseType;
  }

  /**
   * database type (PostgreSQL, Oracle, MySQL, etc.)
   *
   * @param databaseType database type (PostgreSQL, Oracle, MySQL, etc.)
   */
  public void setDatabaseType( String databaseType ) {
    this.databaseType = databaseType;
  }

  /**
   * database access type (Native, JNDI etc.)
   *
   * @return database access type (Native, JNDI etc.)
   */
  @SnmpVariable( oid = OID.DATABASE_CONN_ACCESS, type = SnmpVariable.TYPE.STRING )
  public String getDatabaseAccessType() {
    return databaseAccessType;
  }

  /**
   * database access type (Native, JNDI etc.)
   *
   * @param databaseAccessType database access type (Native, JNDI etc.)
   */
  public void setDatabaseAccessType( String databaseAccessType ) {
    this.databaseAccessType = databaseAccessType;
  }

  /**
   * EventType.Boolean.TRUE if a DB connection has been established, EventType.Boolean.FALSE if it has been severed
   *
   * @return EventType.Boolean.TRUE if a DB conn has been established, EventType.Boolean.FALSE if it has been severed
   */
  @SnmpVariable( oid = OID.DATABASE_CONNECTED, type = SnmpVariable.TYPE.STRING )
  public int getConnected() {
    return connected;
  }

  /**
   * EventType.Boolean.TRUE if a DB connection has been established, EventType.Boolean.FALSE if it has been severed
   *
   * @return EventType.Boolean.TRUE if a DB conn has been established, EventType.Boolean.FALSE if it has been severed
   */
  public void setConnected( int connected ) {
    this.connected = connected;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer( "[" + this.eventType.toString() + "]" );
    sb.append( getId() );
    sb.append( ", initial pool size: '" + getInitialPoolSize() + "' " );
    sb.append( ", max pool size: '" + getMaxPoolSize() + "' " );

    return sb.toString();
  }
}
