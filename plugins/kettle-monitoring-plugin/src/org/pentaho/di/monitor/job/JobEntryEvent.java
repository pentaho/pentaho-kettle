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
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.monitor.OID;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;
import org.pentaho.platform.api.monitoring.snmp.SnmpVariable;

import java.io.Serializable;

@SnmpTrapEvent( oid = OID.JOB )
public class JobEntryEvent extends JobEvent {

  private static final long serialVersionUID = 4681625141567377040L;

  // Triggered Event Type
  private EventType.Job eventType;

  // job entry number
  private int entryNumber;

  // job entry name
  private String entryName;

  // 1 if configured to run in parallel, 0 otherwise
  private int runInParallel;

  public JobEntryEvent( EventType.Job eventType ) {
    super( eventType );
    this.eventType = eventType;
  }

  public JobEntryEvent build( JobExecutionExtension jobExecExtension ) throws KettleException {

    if ( jobExecExtension == null || jobExecExtension.jobEntryCopy == null ) {
      return this;
    }

    setEntryNumber( jobExecExtension.jobEntryCopy.getNr() );
    setEntryName( jobExecExtension.jobEntryCopy.getName() );
    setRunInParallel( EventType.Boolean.getValue( jobExecExtension.jobEntryCopy.isLaunchingInParallel() ) );
    setStatus( eventType.getSnmpId() );

    if ( jobExecExtension.job != null ) {
      JobEvent tempJob = super.build( jobExecExtension.job );

      if ( tempJob != null ) {

        setName( tempJob.getName() );
        setFilename( tempJob.getFilename() );
        setDirectory( tempJob.getDirectory() );
        setRepositoryId( tempJob.getRepositoryId() );
        setRepositoryName( tempJob.getRepositoryName() );
        setCreationUser( tempJob.getCreationUser() );
        setCreationDate( tempJob.getCreationDate() );
        setXml( tempJob.getXml() );
        setExecutingServer( tempJob.getExecutingServer() );
        setExecutingUser( tempJob.getExecutingUser() );
        setStartTimeMillis( tempJob.getStartTimeMillis() );
        setLogChannelId( tempJob.getLogChannelId() );
        setBatchId( tempJob.getBatchId() );
        setErrors( tempJob.getErrors() );
        setParentJobName( tempJob.getParentJobName() );
        setParentTransformationName( tempJob.getParentTransformationName() );
        setParentBatchId( tempJob.getParentBatchId() );
        setParentLogChannelId( tempJob.getParentLogChannelId() );
        setEventLogs( filterEventLogging( getLogChannelId() ) );
        setLog( getEventLogsAsString() );
      }
    }

    return this;
  }

  /**
   * Job entry ID
   *
   * @return Job entry ID
   */
  @Override
  @SnmpVariable( oid = OID.JOB_ENTRY_ID, type = SnmpVariable.TYPE.STRING )
  public Serializable getId() {
    return getEntryName() + "." + getEntryNumber();
  }

  /**
   * Job entry number
   *
   * @return entryNumber Job entry number
   */
  public int getEntryNumber() {
    return entryNumber;
  }

  /**
   * Job entry number
   *
   * @param entryNumber Job entry number
   */
  public void setEntryNumber( int entryNumber ) {
    this.entryNumber = entryNumber;
  }

  /**
   * Job entry name
   *
   * @return Job entry name
   */
  @SnmpVariable( oid = OID.JOB_ENTRY_NAME, type = SnmpVariable.TYPE.STRING )
  public String getEntryName() {
    return entryName;
  }

  /**
   * Job entry name
   *
   * @param entryName Job entry name
   */
  public void setEntryName( String entryName ) {
    this.entryName = entryName;
  }

  /**
   * EventType.Boolean.TRUE if configured to run in parallel, EventType.Boolean.FALSE otherwise
   *
   * @return EventType.Boolean.TRUE if configured to run in parallel, EventType.Boolean.FALSE otherwise
   */
  @SnmpVariable( oid = OID.JOB_RUN_IN_PARALLEL, type = SnmpVariable.TYPE.INTEGER )
  public int isRunInParallel() {
    return runInParallel;
  }

  /**
   * EventType.Boolean.TRUE if configured to run in parallel, EventType.Boolean.FALSE otherwise
   *
   * @param runInParallel EventType.Boolean.TRUE if configured to run in parallel, EventType.Boolean.FALSE otherwise
   */
  public void setRunInParallel( int runInParallel ) {
    this.runInParallel = runInParallel;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer( "[" + getClass().getSimpleName() + "]" );
    sb.append( "[" + this.eventType.toString() + "]" );
    sb.append( " Name: '" + getId() + "' " );
    sb.append( ", Created by: '" + getCreationUser() + "' " );
    sb.append( ", executed in server: '" + getExecutingServer() + "' " );
    sb.append( ", by user: '" + getExecutingUser() + "' " );

    return sb.toString();
  }
}
