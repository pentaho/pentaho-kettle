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
import org.pentaho.di.monitor.OID;
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;
import org.pentaho.platform.api.monitoring.snmp.SnmpVariable;

import java.io.Serializable;
import java.util.Date;

@SnmpTrapEvent( oid = OID.JOB )
public class JobEvent extends BaseEvent {

  private static final long serialVersionUID = -2727216752120528962L;

  // Triggered Event Type
  private EventType.Job eventType;

  // Job name
  private String name;

  // Job Filename (when file system and not repository)
  private String filename;

  // Job Directory (when repository)
  private String directory;

  // Repository ID (when repository)
  private String repositoryId;

  // Repository name (when repository)
  private String repositoryName;

  // Parent Job Name (when part of a job)
  private String parentJobName;

  // Parent Transformation Name (when this is a sub-Job)
  private String parentTransformationName;

  // Executing Server (when executed on a server)
  private String executingServer;

  // Executing User
  private String executingUser;

  // Batch ID (when database logging is activated)
  private long batchId;

  // Parent Batch ID (when database logging is activated and a sub-Job)
  private long parentBatchId;

  //Parent Channel ID
  private String parentLogChannelId;

  // status
  private int status;

  // EventType.Boolean.TRUE if it has finished successfully, EventType.Boolean.FALSE otherwise
  private int success;

  // error count
  private int errors;

  // runtime
  private long runtimeInMillis;

  // event log
  private String log;

  // Job XML string content
  private String xml;

  // User who created this Job
  private String creationUser;

  // Job creation date
  private Date creationDate;

  // Job start time ( milliseconds )
  private long startTimeMillis;

  // Job end time ( milliseconds )
  private long endTimeMillis;


  public JobEvent( EventType.Job eventType ) {
    this.eventType = eventType;
  }

  public JobEvent build( Job job ) throws KettleException {

    if ( job == null ) {
      return this;
    }

    setExecutingServer( job.getExecutingServer() );
    setExecutingUser( job.getExecutingUser() );
    setStartTimeMillis( job.getCurrentDate() != null ? job.getCurrentDate().getTime() : 0 );
    setLogChannelId( job.getLogChannelId() );
    setBatchId( job.getBatchId() );
    setStatus( eventType.getSnmpId() );
    setErrors( job.getErrors() );

    if ( job.getParentJob() != null ) {
      setParentJobName( job.getParentJob().getName() );
      setParentBatchId( job.getParentJob().getBatchId() );
      setParentLogChannelId( job.getParentJob().getLogChannelId() );

    } else if ( job.getParentTrans() != null ) {
      setParentTransformationName( job.getParentTrans().getName() );
      setParentBatchId( job.getParentTrans().getBatchId() );
      setParentLogChannelId( job.getParentTrans().getLogChannelId() );
    }

    if ( this.eventType == EventType.Job.FINISHED ) {

      if ( job.getResult() != null ) {
        setSuccess( EventType.Boolean.getValue( job.getResult().getResult() ) );
      }

      setEndTimeMillis( new Date().getTime() );

      if( getStartTimeMillis() != 0 ) {
        setRuntimeInMillis( getEndTimeMillis() - getStartTimeMillis() );
      }
    }

    setEventLogs( filterEventLogging( getLogChannelId() ) );
    setLog( getEventLogsAsString() );

    return build( job.getJobMeta() );
  }

  public JobEvent build( JobMeta meta ) throws KettleException {

    if ( meta == null ) {
      return this;
    }

    setName( meta.getName() );
    setFilename( meta.getFilename() );
    setDirectory( meta.getRepositoryDirectory() != null ? meta.getRepositoryDirectory().getPath() : null );
    setRepositoryId( meta.getRepository() != null ? meta.getRepository().getRepositoryMeta().getId() : null );
    setRepositoryName( meta.getRepository() != null ? meta.getRepository().getRepositoryMeta().getName() : null );
    setCreationUser( meta.getCreatedUser() );
    setCreationDate( meta.getCreatedDate() );
    setXml( meta.getXML() );

    return this;
  }

  @Override
  public Serializable getId() {
    return getName();
  }

  /**
   * Tranformation name
   *
   * @return Tranformation name
   */
  @SnmpVariable( oid = OID.JOB_NAME, type = SnmpVariable.TYPE.STRING )
  public String getName() {
    return name;
  }

  /**
   * Tranformation name
   *
   * @param name Tranformation name
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * Job Filename (when file system and not repository)
   *
   * @return Job filename
   */
  @SnmpVariable( oid = OID.JOB_FILENAME, type = SnmpVariable.TYPE.STRING )
  public String getFilename() {
    return filename;
  }

  /**
   * Job Filename (when file system and not repository)
   *
   * @param filename Job Filename
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * Job Directory (when repository)
   *
   * @return Job directory
   */
  @SnmpVariable( oid = OID.JOB_DIRECTORY, type = SnmpVariable.TYPE.STRING )
  public String getDirectory() {
    return directory;
  }

  /**
   * Job Directory (when repository)
   *
   * @param directory Job directory
   */
  public void setDirectory( String directory ) {
    this.directory = directory;
  }

  /**
   * Job XML string content
   *
   * @return XML string content
   */
  public String getXml() {
    return xml;
  }

  /**
   * Job XML string content
   *
   * @param xml XML string content
   */
  public void setXml( String xml ) {
    this.xml = xml;
  }

  /**
   * User who created this Job
   *
   * @return user who created this Job
   */
  public String getCreationUser() {
    return creationUser;
  }

  /**
   * User who created this Job
   *
   * @param creationUser user who created this Job
   */
  public void setCreationUser( String creationUser ) {
    this.creationUser = creationUser;
  }

  /**
   * Job creation date
   *
   * @return Job creation date
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Job creation date
   *
   * @param creationDate Job creation date
   */
  public void setCreationDate( Date creationDate ) {
    this.creationDate = creationDate;
  }

  /**
   * Executing Server (when executed on a server)
   *
   * @return executing Server (when executed on a server)
   */
  @SnmpVariable( oid = OID.JOB_EXEC_SERVER, type = SnmpVariable.TYPE.STRING )
  public String getExecutingServer() {
    return executingServer;
  }

  /**
   * Executing Server (when executed on a server)
   *
   * @param executingServer executing Server (when executed on a server)
   */
  public void setExecutingServer( String executingServer ) {
    this.executingServer = executingServer;
  }

  /**
   * Executing User
   *
   * @return executing User
   */
  @SnmpVariable( oid = OID.JOB_EXEC_USER, type = SnmpVariable.TYPE.STRING )
  public String getExecutingUser() {
    return executingUser;
  }

  /**
   * Executing User
   *
   * @param executingUser executing User
   */
  public void setExecutingUser( String executingUser ) {
    this.executingUser = executingUser;
  }

  /**
   * Job start time ( milliseconds )
   *
   * @return Job start time ( milliseconds )
   */
  public long getStartTimeMillis() {
    return startTimeMillis;
  }

  /**
   * Job start time ( milliseconds )
   *
   * @param startTimeMillis Job start time ( milliseconds )
   */
  public void setStartTimeMillis( long startTimeMillis ) {
    this.startTimeMillis = startTimeMillis;
  }

  /**
   * Job end time ( milliseconds )
   *
   * @return Job start time ( milliseconds )
   */
  public long getEndTimeMillis() {
    return endTimeMillis;
  }

  /**
   * Job end time ( milliseconds )
   *
   * @param endTimeMillis Job start time ( milliseconds )
   */
  public void setEndTimeMillis( long endTimeMillis ) {
    this.endTimeMillis = endTimeMillis;
  }

  /**
   * Triggered Event Type
   *
   * @return triggered Event Type
   */
  public EventType.Job getEventType() {
    return eventType;
  }

  /**
   * Triggered Event Type
   *
   * @param eventType triggered Event Type
   */
  public void setEventType( EventType.Job eventType ) {
    this.eventType = eventType;
  }

  /**
   * Repository ID (when repository)
   *
   * @return repository ID (when repository)
   */
  @SnmpVariable( oid = OID.JOB_REPO_ID, type = SnmpVariable.TYPE.STRING )
  public String getRepositoryId() {
    return repositoryId;
  }

  /**
   * Repository ID (when repository)
   *
   * @param repositoryId repository ID (when repository)
   */
  public void setRepositoryId( String repositoryId ) {
    this.repositoryId = repositoryId;
  }

  /**
   * Repository Name (when repository)
   *
   * @return repository Name (when repository)
   */
  @SnmpVariable( oid = OID.JOB_REPO_NAME, type = SnmpVariable.TYPE.STRING )
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * Repository Name (when repository)
   *
   * @param repositoryName repository Name (when repository)
   */
  public void setRepositoryName( String repositoryName ) {
    this.repositoryName = repositoryName;
  }

  /**
   * Parent Job Name (when part of a job)
   *
   * @return parent Job Name (when part of a job)
   */
  @SnmpVariable( oid = OID.JOB_PARENT_JOB, type = SnmpVariable.TYPE.STRING )
  public String getParentJobName() {
    return parentJobName;
  }

  /**
   * Parent Job Name (when part of a job)
   *
   * @param parentJobName parent Job Name (when part of a job)
   */
  public void setParentJobName( String parentJobName ) {
    this.parentJobName = parentJobName;
  }

  /**
   * Parent Transformation Name (when this is a sub-Job)
   *
   * @return parent Transformation Name (when this is a sub-Job)
   */
  @SnmpVariable( oid = OID.JOB_PARENT_TRANS, type = SnmpVariable.TYPE.STRING )
  public String getParentTransformationName() {
    return parentTransformationName;
  }

  /**
   * Parent Transformation Name (when this is a sub-Job)
   *
   * @param parentTransformationName parent Transformation Name (when this is a sub-Job)
   */
  public void setParentTransformationName( String parentTransformationName ) {
    this.parentTransformationName = parentTransformationName;
  }

  /**
   * Batch ID (when database logging is activated)
   *
   * @return batch ID (when database logging is activated)
   */
  @SnmpVariable( oid = OID.JOB_BATCH_ID, type = SnmpVariable.TYPE.INTEGER )
  public long getBatchId() {
    return batchId;
  }

  /**
   * Batch ID (when database logging is activated)
   *
   * @param batchId batch ID (when database logging is activated)
   */
  public void setBatchId( long batchId ) {
    this.batchId = batchId;
  }

  /**
   * Parent Batch ID (when database logging is activated and a sub-Job)
   *
   * @return parent Batch ID (when database logging is activated and a sub-Job)
   */
  @SnmpVariable( oid = OID.JOB_PARENT_BATCH_ID, type = SnmpVariable.TYPE.INTEGER )
  public long getParentBatchId() {
    return parentBatchId;
  }

  /**
   * Parent Batch ID (when database logging is activated and a sub-Job)
   *
   * @param parentBatchId parent Batch ID (when database logging is activated and a sub-Job)
   */
  public void setParentBatchId( long parentBatchId ) {
    this.parentBatchId = parentBatchId;
  }

  /**
   * Parent Channel ID
   *
   * @return parent Channel ID
   */
  @SnmpVariable( oid = OID.JOB_PARENT_LOG_CHANNEL_ID, type = SnmpVariable.TYPE.STRING )
  public String getParentLogChannelId() {
    return parentLogChannelId;
  }

  /**
   * Parent Channel ID
   *
   * @param parentLogChannelId parent Channel ID
   */
  public void setParentLogChannelId( String parentLogChannelId ) {
    this.parentLogChannelId = parentLogChannelId;
  }

  /**
   * Status
   *
   * @return status
   */
  @SnmpVariable( oid = OID.JOB_STATUS, type = SnmpVariable.TYPE.INTEGER )
  public int getStatus() {
    return status;
  }

  /**
   * Status
   *
   * @param status status
   */
  public void setStatus( int status ) {
    this.status = status;
  }

  /**
   * Error count
   *
   * @return error count
   */
  @SnmpVariable( oid = OID.JOB_ERROR_COUNT, type = SnmpVariable.TYPE.INTEGER )
  public int getErrors() {
    return errors;
  }

  /**
   * Error count
   *
   * @param errors error count
   */
  public void setErrors( int errors ) {
    this.errors = errors;
  }

  /**
   * Runtime
   *
   * @return runtime
   */
  @SnmpVariable( oid = OID.JOB_RUNTIME, type = SnmpVariable.TYPE.INTEGER )
  public long getRuntimeInMillis() {
    return runtimeInMillis;
  }

  /**
   * Runtime
   *
   * @param runtimeInMillis runtime
   */
  public void setRuntimeInMillis( long runtimeInMillis ) {
    this.runtimeInMillis = runtimeInMillis;
  }

  /**
   * Log channel ID
   *
   * @return log channel ID
   */
  @Override
  @SnmpVariable( oid = OID.JOB_LOG_CHANNEL_ID, type = SnmpVariable.TYPE.STRING )
  public String getLogChannelId() {
    return super.getLogChannelId();
  }

  /**
   * event log
   *
   * @return event log
   */
  @SnmpVariable( oid = OID.JOB_LOG, type = SnmpVariable.TYPE.STRING )
  public String getLog() {
    return log;
  }

  /**
   * the exit result.
   * <p/>
   * EventType.Boolean.TRUE if it has  finished successfully, EventType.Boolean.FALSE otherwise
   * <p/>
   *
   * @return success EventType.Boolean.TRUE if it has finished successfully, EventType.Boolean.FALSE otherwise
   */
  @SnmpVariable( oid = OID.JOB_SUCCESS, type = SnmpVariable.TYPE.INTEGER )
  public int getSuccess() {
    return success;
  }

  /**
   * the exit result.
   * <p/>
   * EventType.Boolean.TRUE if it has  finished successfully, EventType.Boolean.FALSE otherwise
   * <p/>
   *
   * @param success EventType.Boolean.TRUE if it has finished successfully, EventType.Boolean.FALSE otherwise
   */
  public void setSuccess( int success ) {
    this.success = success;
  }

  /**
   * event log
   *
   * @param log event log
   */
  public void setLog( String log ) {
    this.log = log;
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
