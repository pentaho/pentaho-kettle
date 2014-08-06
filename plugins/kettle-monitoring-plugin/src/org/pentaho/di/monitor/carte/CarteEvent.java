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
import org.pentaho.di.monitor.IKettleMonitoringEvent;
import org.pentaho.di.www.WebServer;

import java.io.Serializable;

public class CarteEvent implements IKettleMonitoringEvent {

  private static final long serialVersionUID = -3589233687711692569L;

  private static final String ID = "1.1.1.1.1.1.1.1"; // TODO replace with an actual oid

  public static enum EventType {STARTUP, SHUTDOWN}

  private Serializable id = ID;
  private EventType eventType;
  private String hostname;
  private int port;

  public CarteEvent( EventType eventType ) {
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
