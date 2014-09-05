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
package org.pentaho.di.monitor.job;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;

import java.io.Serializable;
import java.util.Date;

@SnmpTrapEvent( oid="1.1.1.1.3.1.2.3" )
public class JobEvent extends BaseEvent {

  private static final long serialVersionUID = -2727216752120528962L;

  private EventType.Job eventType;
  private String name;
  private String filename;
  private String filetype;
  private String xml;
  private String creationUser;
  private Date creationDate;
  private String executingServer;
  private String executingUser;
  private long startTimeMillis;
  private long endTimeMillis;

  public JobEvent( EventType.Job eventType ) {
    this.eventType = eventType;
  }

  @Override
  public Serializable getId() {
    return getName();
  }

  public EventType.Job getEventType() {
    return eventType;
  }

  public void setEventType( EventType.Job eventType ) {
    this.eventType = eventType;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFiletype() {
    return filetype;
  }

  public void setFiletype( String filetype ) {
    this.filetype = filetype;
  }

  public String getXml() {
    return xml;
  }

  public void setXml( String xml ) {
    this.xml = xml;
  }

  public String getCreationUser() {
    return creationUser;
  }

  public void setCreationUser( String creationUser ) {
    this.creationUser = creationUser;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate( Date creationDate ) {
    this.creationDate = creationDate;
  }

  public String getExecutingServer() {
    return executingServer;
  }

  public void setExecutingServer( String executingServer ) {
    this.executingServer = executingServer;
  }

  public String getExecutingUser() {
    return executingUser;
  }

  public void setExecutingUser( String executingUser ) {
    this.executingUser = executingUser;
  }

  public long getStartTimeMillis() {
    return startTimeMillis;
  }

  public void setStartTimeMillis( long startTimeMillis ) {
    this.startTimeMillis = startTimeMillis;
  }

  public long getEndTimeMillis() {
    return endTimeMillis;
  }

  public void setEndTimeMillis( long endTimeMillis ) {
    this.endTimeMillis = endTimeMillis;
  }

  public JobEvent build( Job job ) throws KettleException {

    if ( job == null ) {
      return this;
    }

    setExecutingServer( job.getExecutingServer() );
    setExecutingUser( job.getExecutingUser() );
    setStartTimeMillis( job.getCurrentDate() != null ? job.getCurrentDate().getTime() : 0 );
    setLogChannelId( job.getLogChannelId() );

    if ( this.eventType == EventType.Job.FINISHED ) {
      setEndTimeMillis( new Date().getTime() );
    }

    setEventLogs( filterEventLogging( getLogChannelId() ) );

    return build( job.getJobMeta() );
  }

  public JobEvent build( JobMeta meta ) throws KettleException {

    if ( meta == null ) {
      return this;
    }

    setName( meta.getName() );
    setFilename( meta.getFilename() );
    setFiletype( meta.getFileType() );
    setCreationUser( meta.getCreatedUser() );
    setCreationDate( meta.getCreatedDate() );
    setXml( meta.getXML() );

    return this;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer( "[" + getClass().getSimpleName() + "]" );
    sb.append( "[" + this.eventType.toString() + "]" );
    sb.append( " Name: '" + getName() + "' " );
    sb.append( ", Created by: '" + getCreationUser() + "' " );
    sb.append( ", executed in server: '" + getExecutingServer() + "' " );
    sb.append( ", by user: '" + getExecutingUser() + "' " );

    long completionTimeSecs =
      ( getStartTimeMillis() > 0 ? ( ( ( getEndTimeMillis() - getStartTimeMillis() ) / 1000 ) % 60 ) : 0 );

    if ( this.eventType == EventType.Job.FINISHED && completionTimeSecs > 0 ) {
      sb.append( ", executed in: " + completionTimeSecs + " seconds " );
    }

    return sb.toString();
  }
}
