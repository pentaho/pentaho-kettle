/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.writetolog;

import java.util.Date;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Job entry type to output message to the job log.
 *
 * @author Samatar
 * @since 08-08-2007
 */

public class JobEntryWriteToLog extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryWriteToLog.class; // for i18n purposes, needed by Translator2!!

  /**
   * The log level with which the message should be logged.
   *
   * @deprecated Use {@link JobEntryWriteToLog#getEntryLogLevel()} and
   * {@link JobEntryWriteToLog#setEntryLogLevel(LogLevel)} instead.
   */
  @Deprecated
  public LogLevel entryLogLevel;
  private String logsubject;
  private String logmessage;

  public JobEntryWriteToLog( String n ) {
    super( n, "" );
    logmessage = null;
    logsubject = null;
  }

  public JobEntryWriteToLog() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntryWriteToLog je = (JobEntryWriteToLog) super.clone();
    return je;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logmessage", logmessage ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "loglevel", ( getEntryLogLevel() == null ) ? null : getEntryLogLevel().getCode() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logsubject", logsubject ) );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      logmessage = XMLHandler.getTagValue( entrynode, "logmessage" );
      entryLogLevel = LogLevel.getLogLevelForCode( XMLHandler.getTagValue( entrynode, "loglevel" ) );
      logsubject = XMLHandler.getTagValue( entrynode, "logsubject" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "WriteToLog.Error.UnableToLoadFromXML.Label" ), e );

    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      logmessage = rep.getJobEntryAttributeString( id_jobentry, "logmessage" );
      entryLogLevel = LogLevel.getLogLevelForCode( rep.getJobEntryAttributeString( id_jobentry, "loglevel" ) );
      logsubject = rep.getJobEntryAttributeString( id_jobentry, "logsubject" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "WriteToLog.Error.UnableToLoadFromRepository.Label" )
        + id_jobentry, dbe );

    }
  }

  // Save the attributes of this job entry
  //
  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logmessage", logmessage );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "loglevel", ( entryLogLevel != null ? entryLogLevel
        .getCode() : "" ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logsubject", logsubject );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "WriteToLog.Error.UnableToSaveToRepository.Label" )
        + id_job, dbe );
    }
  }

  private class LogWriterObject implements LoggingObjectInterface {

    private LogChannelInterface writerLog;

    private LogLevel logLevel;
    private LoggingObjectInterface parent;
    private String subject;
    private String containerObjectId;

    public LogWriterObject( String subject, LoggingObjectInterface parent, LogLevel logLevel ) {
      this.subject = subject;
      this.parent = parent;
      this.logLevel = logLevel;
      this.writerLog = new LogChannel( this, parent );
      this.containerObjectId = writerLog.getContainerObjectId();
    }

    @Override
    public String getFilename() {
      return null;
    }

    @Override
    public String getLogChannelId() {
      return writerLog.getLogChannelId();
    }

    @Override
    public String getObjectCopy() {
      return null;
    }

    @Override
    public ObjectId getObjectId() {
      return null;
    }

    @Override
    public String getObjectName() {
      return subject;
    }

    @Override
    public ObjectRevision getObjectRevision() {
      return null;
    }

    @Override
    public LoggingObjectType getObjectType() {
      return LoggingObjectType.STEP;
    }

    @Override
    public LoggingObjectInterface getParent() {
      return parent;
    }

    @Override
    public RepositoryDirectory getRepositoryDirectory() {
      return null;
    }

    public LogChannelInterface getLogChannel() {
      return writerLog;
    }

    @Override
    public LogLevel getLogLevel() {
      return logLevel;
    }

    /**
     * @return the execution container object id
     */
    @Override
    public String getContainerObjectId() {
      return containerObjectId;
    }

    /**
     * Stub
     */
    @Override
    public Date getRegistrationDate() {
      return null;
    }

    @Override
    public boolean isGatheringMetrics() {
      return log.isGatheringMetrics();
    }

    @Override
    public void setGatheringMetrics( boolean gatheringMetrics ) {
      log.setGatheringMetrics( gatheringMetrics );
    }

    @Override
    public boolean isForcingSeparateLogging() {
      return log.isForcingSeparateLogging();
    }

    @Override
    public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
      log.setForcingSeparateLogging( forcingSeparateLogging );
    }
  }

  LogChannelInterface createLogChannel() {
    LogWriterObject logWriterObject = new LogWriterObject( getRealLogSubject(), this, parentJob.getLogLevel() );
    return logWriterObject.getLogChannel();
  }

  /**
   * Output message to job log.
   */
  public boolean evaluate( Result result ) {
    LogChannelInterface logChannel = createLogChannel();
    String message = getRealLogMessage();

    // Filter out empty messages and those that are not visible with the job's log level
    if ( Utils.isEmpty( message ) || !getEntryLogLevel().isVisible( logChannel.getLogLevel() ) ) {
      return true;
    }

    try {
      switch ( getEntryLogLevel() ) {
        case ERROR:
          logChannel.logError( message + Const.CR );
          break;
        case MINIMAL:
          logChannel.logMinimal( message + Const.CR );
          break;
        case BASIC:
          logChannel.logBasic( message + Const.CR );
          break;
        case DETAILED:
          logChannel.logDetailed( message + Const.CR );
          break;
        case DEBUG:
          logChannel.logDebug( message + Const.CR );
          break;
        case ROWLEVEL:
          logChannel.logRowlevel( message + Const.CR );
          break;
        default: // NOTHING
          break;
      }

      return true;
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      log.logError( BaseMessages.getString( PKG, "WriteToLog.Error.Label" ), BaseMessages.getString(
        PKG, "WriteToLog.Error.Description" )
        + " : " + e.toString() );
      return false;
    }

  }

  /**
   * Execute this job entry and return the result. In this case it means, just set the result boolean in the Result
   * class.
   *
   * @param prev_result
   *          The result of the previous execution
   * @return The Result of the execution.
   */
  @Override
  public Result execute( Result prev_result, int nr ) {
    prev_result.setResult( evaluate( prev_result ) );
    return prev_result;
  }

  @Override
  public boolean resetErrorsBeforeExecution() {
    // we should be able to evaluate the errors in
    // the previous jobentry.
    return false;
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return false;
  }

  public String getRealLogMessage() {
    return Const.NVL( environmentSubstitute( getLogMessage() ), "" );

  }

  public String getRealLogSubject() {
    return Const.NVL( environmentSubstitute( getLogSubject() ), "" );
  }

  public String getLogMessage() {
    if ( logmessage == null ) {
      logmessage = "";
    }
    return logmessage;

  }

  public String getLogSubject() {
    if ( logsubject == null ) {
      logsubject = "";
    }
    return logsubject;

  }

  public void setLogMessage( String s ) {
    logmessage = s;
  }

  public void setLogSubject( String logsubjectin ) {
    logsubject = logsubjectin;
  }

  public LogLevel getEntryLogLevel() {
    return entryLogLevel;
  }

  public void setEntryLogLevel( LogLevel in ) {
    this.entryLogLevel = in;
  }
}
