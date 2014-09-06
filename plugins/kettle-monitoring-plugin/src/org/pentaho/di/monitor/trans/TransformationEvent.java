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
package org.pentaho.di.monitor.trans;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.monitor.OID;
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;
import org.pentaho.platform.api.monitoring.snmp.SnmpVariable;

import java.io.Serializable;
import java.util.Date;

@SnmpTrapEvent( oid=OID.TRANSFORMATION )
public class TransformationEvent extends BaseEvent {

  private static final long serialVersionUID = -7141225896990220465L;

  // Triggered Event Type
  private EventType.Transformation eventType;

  // Tranformation name
  private String name;

  // Transformation Filename (when file system and not repository)
  private String filename;

  // Transformation Directory (when repository)
  private String directory;

  // Repository ID (when repository)
  private String repositoryId;

  // Repository name (when repository)
  private String repositoryName;

  // Parent Job Name (when part of a job)
  private String parentJobName;

  // Parent Transformation Name (when this is a sub-transformation)
  private String parentTransformationName;

  // Executing Server (when executed on a server)
  private String executingServer;

  // Executing User
  private String executingUser;

  // Batch ID (when database logging is activated)
  private long batchId;

  // Parent Batch ID (when database logging is activated and a sub-transformation)
  private long parentBatchId;

  //Parent Channel ID
  private String parentLogChannelId;

  // status
  private int status;

  // error count
  private int errors;

  // runtime
  private long runtimeInMillis;

  // Transformation end result: Lines Read
  private long linesRead;

  // Transformation end result: Lines Written
  private long linesWritten;

  // Transformation end result: Lines Updated
  private long linesUpdated;

  // Transformation end result: Lines Rejected
  private long linesRejected;

  // Transformation end result: Lines Input
  private long linesInput;

  // Transformation end result: Lines Output
  private long linesOutput;

  // event log
  private String log;

  // Transformation XML string content
  private String xml;

  // User who created this transformation
  private String creationUser;

  // Transformation creation date
  private Date creationDate;

  // Transformation start time ( milliseconds )
  private long startTimeMillis;

  // Transformation end time ( milliseconds )
  private long endTimeMillis;

  public TransformationEvent( EventType.Transformation eventType ) {
    this.eventType = eventType;
  }

  /**
   * Populates this TransformationEvent instance with information contained in Trans object; also calls this.build(
   * TransMeta meta )
   *
   * @param trans transformation object
   * @return this TransformationEvent instance
   * @throws org.pentaho.di.core.exception.KettleException should something go horribly wrong
   */
  public TransformationEvent build( Trans trans ) throws KettleException {

    if ( trans == null ) {
      return this;
    }

    setLogChannelId( trans.getLogChannelId() );
    setBatchId( trans.getBatchId() );
    setExecutingServer( trans.getExecutingServer() );
    setExecutingUser( trans.getExecutingUser() );
    setStartTimeMillis( trans.getCurrentDate() != null ? trans.getCurrentDate().getTime() : 0 );
    setStatus( eventType.getSnmpId() );
    setErrors( trans.getErrors() );

    if ( trans.getParentJob() != null ) {
      setParentJobName( trans.getParentJob().getJobMeta() != null ?
        trans.getParentJob().getJobMeta().getName() : null );
      setParentBatchId( trans.getParentJob().getBatchId() );
      setParentLogChannelId( trans.getParentJob().getLogChannelId() );
    }

    if ( trans.getParentTrans() != null ) {
      setParentJobName( trans.getParentTrans().getTransMeta() != null ?
        trans.getParentTrans().getTransMeta().getName() : null );
      setParentBatchId( trans.getParentTrans().getBatchId() );
      setParentLogChannelId( trans.getParentTrans().getLogChannelId() );
    }

    if ( this.eventType == EventType.Transformation.FINISHED ) {
      setEndTimeMillis( new Date().getTime() );
      setRuntimeInMillis( getEndTimeMillis() - getStartTimeMillis() );

      if ( trans.getResult() != null ) {
        setLinesInput( trans.getResult().getNrLinesInput() );
        setLinesOutput( trans.getResult().getNrLinesOutput() );
        setLinesRead( trans.getResult().getNrLinesRead() );
        setLinesWritten( trans.getResult().getNrLinesWritten() );
        setLinesUpdated( trans.getResult().getNrLinesUpdated() );
        setLinesRejected( trans.getResult().getNrLinesRejected() );
      }
    }

    setEventLogs( filterEventLogging( getLogChannelId() ) );
    setLog( getEventLogsAsString() );

    return build( trans.getTransMeta() );
  }

  /**
   * Populates this TransformationEvent instance with information contained in TransMeta object
   *
   * @param meta transformationMeta object
   * @return this TransformationEvent instance
   * @throws KettleException should something go horribly wrong
   */
  public TransformationEvent build( TransMeta meta ) throws KettleException {

    if ( meta == null ) {
      return this;
    }

    setName( meta.getName() );
    setFilename( meta.getFilename() );
    setDirectory( meta.getRepositoryDirectory() != null ? meta.getRepositoryDirectory().getPath() : null );
    setCreationUser( meta.getCreatedUser() );
    setCreationDate( meta.getCreatedDate() );
    setXml( meta.getXML() );

    if ( meta.getRepository() != null && meta.getRepository().getRepositoryMeta() != null ) {
      setRepositoryId( meta.getRepository().getRepositoryMeta().getId() );
      setRepositoryName( meta.getRepository().getRepositoryMeta().getName() );
    }

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
  @SnmpVariable( oid=OID.TRANSFORMATION_NAME, type = SnmpVariable.TYPE.STRING )
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
   * Transformation Filename (when file system and not repository)
   *
   * @return transformation filename
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_FILENAME, type = SnmpVariable.TYPE.STRING )
  public String getFilename() {
    return filename;
  }

  /**
   * Transformation Filename (when file system and not repository)
   *
   * @param filename Transformation Filename
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * Transformation Directory (when repository)
   *
   * @return transformation directory
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_DIRECTORY, type = SnmpVariable.TYPE.STRING )
  public String getDirectory() {
    return directory;
  }

  /**
   * Transformation Directory (when repository)
   *
   * @param directory transformation directory
   */
  public void setDirectory( String directory ) {
    this.directory = directory;
  }

  /**
   * Transformation XML string content
   *
   * @return XML string content
   */
  public String getXml() {
    return xml;
  }

  /**
   * Transformation XML string content
   *
   * @param xml XML string content
   */
  public void setXml( String xml ) {
    this.xml = xml;
  }

  /**
   * User who created this transformation
   *
   * @return user who created this transformation
   */
  public String getCreationUser() {
    return creationUser;
  }

  /**
   * User who created this transformation
   *
   * @param creationUser user who created this transformation
   */
  public void setCreationUser( String creationUser ) {
    this.creationUser = creationUser;
  }

  /**
   * Transformation creation date
   *
   * @return transformation creation date
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Transformation creation date
   *
   * @param creationDate transformation creation date
   */
  public void setCreationDate( Date creationDate ) {
    this.creationDate = creationDate;
  }

  /**
   * Executing Server (when executed on a server)
   *
   * @return executing Server (when executed on a server)
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_EXEC_SERVER, type = SnmpVariable.TYPE.STRING )
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
  @SnmpVariable( oid=OID.TRANSFORMATION_EXEC_USER, type = SnmpVariable.TYPE.STRING )
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
   * Transformation start time ( milliseconds )
   *
   * @return transformation start time ( milliseconds )
   */
  public long getStartTimeMillis() {
    return startTimeMillis;
  }

  /**
   * Transformation start time ( milliseconds )
   *
   * @param startTimeMillis transformation start time ( milliseconds )
   */
  public void setStartTimeMillis( long startTimeMillis ) {
    this.startTimeMillis = startTimeMillis;
  }

  /**
   * Transformation end time ( milliseconds )
   *
   * @return transformation start time ( milliseconds )
   */
  public long getEndTimeMillis() {
    return endTimeMillis;
  }

  /**
   * Transformation end time ( milliseconds )
   *
   * @param endTimeMillis transformation start time ( milliseconds )
   */
  public void setEndTimeMillis( long endTimeMillis ) {
    this.endTimeMillis = endTimeMillis;
  }

  /**
   * Triggered Event Type
   *
   * @return triggered Event Type
   */
  public EventType.Transformation getEventType() {
    return eventType;
  }

  /**
   * Triggered Event Type
   *
   * @param eventType triggered Event Type
   */
  public void setEventType( EventType.Transformation eventType ) {
    this.eventType = eventType;
  }

  /**
   * Repository ID (when repository)
   *
   * @return repository ID (when repository)
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_REPO_ID, type = SnmpVariable.TYPE.STRING )
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
  @SnmpVariable( oid=OID.TRANSFORMATION_REPO_NAME, type = SnmpVariable.TYPE.STRING )
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
  @SnmpVariable( oid=OID.TRANSFORMATION_PARENT_JOB, type = SnmpVariable.TYPE.STRING )
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
   * Parent Transformation Name (when this is a sub-transformation)
   *
   * @return parent Transformation Name (when this is a sub-transformation)
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_PARENT_TRANS, type = SnmpVariable.TYPE.STRING )
  public String getParentTransformationName() {
    return parentTransformationName;
  }

  /**
   * Parent Transformation Name (when this is a sub-transformation)
   *
   * @param parentTransformationName parent Transformation Name (when this is a sub-transformation)
   */
  public void setParentTransformationName( String parentTransformationName ) {
    this.parentTransformationName = parentTransformationName;
  }

  /**
   * Batch ID (when database logging is activated)
   *
   * @return batch ID (when database logging is activated)
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_BATCH_ID, type = SnmpVariable.TYPE.INTEGER )
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
   * Parent Batch ID (when database logging is activated and a sub-transformation)
   *
   * @return parent Batch ID (when database logging is activated and a sub-transformation)
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_PARENT_BATCH_ID, type = SnmpVariable.TYPE.INTEGER )
  public long getParentBatchId() {
    return parentBatchId;
  }

  /**
   * Parent Batch ID (when database logging is activated and a sub-transformation)
   *
   * @param parentBatchId parent Batch ID (when database logging is activated and a sub-transformation)
   */
  public void setParentBatchId( long parentBatchId ) {
    this.parentBatchId = parentBatchId;
  }

  /**
   * Parent Channel ID
   *
   * @return parent Channel ID
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_PARENT_LOG_CHANNEL_ID, type = SnmpVariable.TYPE.STRING )
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
  @SnmpVariable( oid=OID.TRANSFORMATION_STATUS, type = SnmpVariable.TYPE.INTEGER )
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
  @SnmpVariable( oid=OID.TRANSFORMATION_ERROR_COUNT, type = SnmpVariable.TYPE.INTEGER )
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
  @SnmpVariable( oid=OID.TRANSFORMATION_RUNTIME, type = SnmpVariable.TYPE.INTEGER )
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
   * Transformation end result: Lines read
   *
   * @return lines read
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LINES_READ, type = SnmpVariable.TYPE.INTEGER )
  public long getLinesRead() {
    return linesRead;
  }

  /**
   * Transformation end result: Lines read
   *
   * @param linesRead lines read
   */
  public void setLinesRead( long linesRead ) {
    this.linesRead = linesRead;
  }

  /**
   * Transformation end result: Lines Written
   *
   * @return lines written
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LINES_WRITTEN, type = SnmpVariable.TYPE.INTEGER )
  public long getLinesWritten() {
    return linesWritten;
  }

  /**
   * Transformation end result: Lines Written
   *
   * @param linesWritten lines written
   */
  public void setLinesWritten( long linesWritten ) {
    this.linesWritten = linesWritten;
  }

  /**
   * Transformation end result: Lines Updated
   *
   * @return lines updated
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LINES_UPDATED, type = SnmpVariable.TYPE.INTEGER )
  public long getLinesUpdated() {
    return linesUpdated;
  }

  /**
   * Transformation end result: Lines Updated
   *
   * @param linesUpdated lines updated
   */
  public void setLinesUpdated( long linesUpdated ) {
    this.linesUpdated = linesUpdated;
  }

  /**
   * Transformation end result: Lines read
   *
   * @return lines rejected
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LINES_REJECTED, type = SnmpVariable.TYPE.INTEGER )
  public long getLinesRejected() {
    return linesRejected;
  }

  /**
   * Transformation end result: Lines rejected
   *
   * @param linesRejected lines rejected
   */
  public void setLinesRejected( long linesRejected ) {
    this.linesRejected = linesRejected;
  }

  /**
   * Transformation end result: Lines Input
   *
   * @return lines Input
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LINES_INPUT, type = SnmpVariable.TYPE.INTEGER )
  public long getLinesInput() {
    return linesInput;
  }

  /**
   * Transformation end result: Lines Input
   *
   * @param linesInput lines Input
   */
  public void setLinesInput( long linesInput ) {
    this.linesInput = linesInput;
  }

  /**
   * Transformation end result: Lines output
   *
   * @return lines output
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LINES_OUTPUT, type = SnmpVariable.TYPE.INTEGER )
  public long getLinesOutput() {
    return linesOutput;
  }

  /**
   * Transformation end result: Lines output
   *
   * @param linesOutput lines output
   */
  public void setLinesOutput( long linesOutput ) {
    this.linesOutput = linesOutput;
  }

  /**
   * Log channel ID
   *
   * @return log channel ID
   */
  @Override
  @SnmpVariable( oid=OID.TRANSFORMATION_LOG_CHANNEL_ID, type = SnmpVariable.TYPE.STRING )
  public String getLogChannelId() {
    return super.getLogChannelId();
  }

  /**
   * event log
   *
   * @return event log
   */
  @SnmpVariable( oid=OID.TRANSFORMATION_LOG, type = SnmpVariable.TYPE.STRING )
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

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer( "[" + getClass().getSimpleName() + "]" );
    sb.append( "[" + this.eventType.toString() + "]" );
    sb.append( " Name: '" + getName() + "' " );
    sb.append( ", Created by: '" + getCreationUser() + "' " );

    if ( !StringUtils.isEmpty( getExecutingServer() ) ) {
      sb.append( ", executed in server: '" + getExecutingServer() + "' " );
    }

    if ( !StringUtils.isEmpty( getExecutingUser() ) ) {
      sb.append( ", by user: '" + getExecutingUser() + "' " );
    }

    sb.append( " ( " );
    sb.append( " I= " ).append( getLinesInput() ).append( ", " );
    sb.append( " O= " ).append( getLinesOutput() ).append( ", " );
    sb.append( " R= " ).append( getLinesRead() ).append( ", " );
    sb.append( " W= " ).append( getLinesWritten() ).append( ", " );
    sb.append( " U= " ).append( getLinesUpdated() ).append( ", " );
    sb.append( " R= " ).append( getLinesRejected() ).append( ", " );
    sb.append( " ) " );

    long runtimeSecs = ( getRuntimeInMillis() > 0 ? ( ( getRuntimeInMillis() / 1000 ) % 60 ) : 0 );

    if ( this.eventType == EventType.Transformation.FINISHED && runtimeSecs > 0 ) {
      sb.append( ", executed in: " + runtimeSecs + " seconds " );
    }

    return sb.toString();
  }
}
