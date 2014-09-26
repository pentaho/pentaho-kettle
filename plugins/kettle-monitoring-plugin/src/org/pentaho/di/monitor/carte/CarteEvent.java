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
package org.pentaho.di.monitor.carte;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.monitor.OID;
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.di.www.WebServer;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;
import org.pentaho.platform.api.monitoring.snmp.SnmpVariable;

import java.io.Serializable;

@SnmpTrapEvent(oid = OID.CARTE)
public class CarteEvent extends BaseEvent {

  private static final long serialVersionUID = -3589233687711692569L;

  private EventType.Carte eventType;

  // carte server name
  private String hostname;

  // port on which the carte server is running
  private int port;

  // event log
  private String log;

  // EventType.Boolean.TRUE if it has been started, EventType.Boolean.FALSE if it has been shutdown
  private int started;

  public CarteEvent( EventType.Carte eventType ) {
    this.eventType = eventType;
  }

  public CarteEvent build( WebServer ws ) throws KettleException {

    if ( ws == null ) {
      return this;
    }

    if ( ws.getLog() != null ) {
      setLogChannelId( ws.getLog().getLogChannelId() );
      setEventLogs( filterEventLogging( getLogChannelId() ) );
      setLog( getEventLogsAsString() );
    }

    setPort( ws.getPort() );
    setHostname( ws.getHostname() );
    setStarted( EventType.Boolean.getValue( EventType.Carte.STARTUP == eventType ) );

    return this;
  }

  @Override
  public Serializable getId() {
    return getHostname() + ":" + getPort();
  }

  /**
   * Triggered Event Type
   *
   * @return Triggered Event Type
   */
  public EventType.Carte getEventType() {
    return eventType;
  }

  /**
   * Triggered Event Type
   *
   * @param eventType Triggered Event Type
   */
  public void setEventType( EventType.Carte eventType ) {
    this.eventType = eventType;
  }

  /**
   * carte server name
   *
   * @return carte server name
   */
  @SnmpVariable( oid = OID.CARTE_HOSTNAME, type = SnmpVariable.TYPE.STRING )
  public String getHostname() {
    return hostname;
  }

  /**
   * carte server name
   *
   * @param hostname carte server name
   */
  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  /**
   * Triggered Event Type
   *
   * @return Triggered Event Type
   */
  @SnmpVariable( oid = OID.CARTE_PORT, type = SnmpVariable.TYPE.STRING )
  public int getPort() {
    return port;
  }

  /**
   * Triggered Event Type
   *
   * @param port Triggered Event Type
   */
  public void setPort( int port ) {
    this.port = port;
  }

  /**
   * event log
   *
   * @return event log
   */
  @SnmpVariable( oid = OID.CARTE_LOG, type = SnmpVariable.TYPE.STRING )
  public String getLog() {
    return log;
  }

  /**
   * event log
   *
   * @param log event log
   */
  public void setLog( String log ) {
    this.log = log;
  }

  /**
   * server status: EventType.Boolean.TRUE if it has been started, EventType.Boolean.FALSE if it has been shutdown
   * @return carte EventType.Boolean.TRUE if it has been started, EventType.Boolean.FALSE if it has been shutdown
   */
  @SnmpVariable( oid = OID.CARTE_STARTED, type = SnmpVariable.TYPE.INTEGER )
  public int getStarted() {
    return started;
  }

  /**
   * server status: EventType.Boolean.TRUE if it has been started, EventType.Boolean.FALSE if it has been shutdown
   * @param started EventType.Boolean.TRUE if it has been started, EventType.Boolean.FALSE if it has been shutdown
   */
  public void setStarted( int started ) {
    this.started = started;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer( "[" + getClass().getSimpleName() + "]" );
    sb.append( "[" + this.eventType.toString() + "]" );
    sb.append( " Hostname: '" + getHostname() + "' " );
    sb.append( ", Port: " + getPort() + " " );

    return sb.toString();
  }
}
