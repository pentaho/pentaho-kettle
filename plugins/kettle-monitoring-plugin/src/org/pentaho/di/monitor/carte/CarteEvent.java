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
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.di.www.WebServer;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;

import java.io.Serializable;

@SnmpTrapEvent( oid="1.1.1.1.3.1.2.1" )
public class CarteEvent extends BaseEvent {

  private static final long serialVersionUID = -3589233687711692569L;

  private EventType.Carte eventType;
  private String hostname;
  private int port;

  public CarteEvent( EventType.Carte eventType ) {
    this.eventType = eventType;
  }

  @Override
  public Serializable getId() {
    return getHostname() + ":" + getPort();
  }

  public EventType.Carte getEventType() {
    return eventType;
  }

  public void setEventType( EventType.Carte eventType ) {
    this.eventType = eventType;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  public int getPort() {
    return port;
  }

  public void setPort( int port ) {
    this.port = port;
  }

  public CarteEvent build( WebServer ws ) throws KettleException {

    if ( ws == null ) {
      return this;
    }

    if ( ws.getLog() != null ) {
      setLogChannelId( ws.getLog().getLogChannelId() );
      setEventLogs( filterEventLogging( getLogChannelId() ) );
    }

    setPort( ws.getPort() );
    setHostname( ws.getHostname() );

    return this;
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
