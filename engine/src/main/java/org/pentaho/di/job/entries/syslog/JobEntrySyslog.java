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

package org.pentaho.di.job.entries.syslog;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;
import org.w3c.dom.Node;

/**
 * This defines a Syslog job entry.
 *
 * @author Samatar
 * @since 05-01-2010
 *
 */
public class JobEntrySyslog extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySyslog.class; // for i18n purposes, needed by Translator2!!

  private String serverName;
  private String port;
  private String message;
  private String facility;
  private String priority;
  private String datePattern;
  private boolean addTimestamp;
  private boolean addHostname;

  public JobEntrySyslog( String n ) {
    super( n, "" );
    port = String.valueOf( SyslogDefs.DEFAULT_PORT );
    serverName = null;
    message = null;
    facility = SyslogDefs.FACILITYS[0];
    priority = SyslogDefs.PRIORITYS[0];
    datePattern = SyslogDefs.DEFAULT_DATE_FORMAT;
    addTimestamp = true;
    addHostname = true;
  }

  public JobEntrySyslog() {
    this( "" );
  }

  public Object clone() {
    JobEntrySyslog je = (JobEntrySyslog) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 128 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "facility", facility ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "priority", priority ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "message", message ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "datePattern", datePattern ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addTimestamp", addTimestamp ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addHostname", addHostname ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      port = XMLHandler.getTagValue( entrynode, "port" );
      serverName = XMLHandler.getTagValue( entrynode, "servername" );
      facility = XMLHandler.getTagValue( entrynode, "facility" );
      priority = XMLHandler.getTagValue( entrynode, "priority" );
      message = XMLHandler.getTagValue( entrynode, "message" );
      datePattern = XMLHandler.getTagValue( entrynode, "datePattern" );
      addTimestamp = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addTimestamp" ) );
      addHostname = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addHostname" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'Syslog' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      serverName = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      facility = rep.getJobEntryAttributeString( id_jobentry, "facility" );
      priority = rep.getJobEntryAttributeString( id_jobentry, "priority" );
      message = rep.getJobEntryAttributeString( id_jobentry, "message" );
      datePattern = rep.getJobEntryAttributeString( id_jobentry, "datePattern" );
      addTimestamp = rep.getJobEntryAttributeBoolean( id_jobentry, "addTimestamp" );
      addHostname = rep.getJobEntryAttributeBoolean( id_jobentry, "addHostname" );

    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'Syslog' from the repository for id_jobentry="
        + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "facility", facility );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "priority", priority );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "message", message );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "datePattern", datePattern );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addTimestamp", addTimestamp );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addHostname", addHostname );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'Syslog' to the repository for id_job="
        + id_job, dbe );
    }
  }

  /**
   * @return Returns the serverName.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName
   *          The serverName to set.
   */
  public void setServerName( String serverName ) {
    this.serverName = serverName;
  }

  /**
   * @return Returns the Facility.
   */
  public String getFacility() {
    return facility;
  }

  /**
   * @param facility
   *          The facility to set.
   */
  public void setFacility( String facility ) {
    this.facility = facility;
  }

  /**
   * @param priority
   *          The priority to set.
   */
  public void setPriority( String priority ) {
    this.priority = priority;
  }

  /**
   * @return Returns the priority.
   */
  public String getPriority() {
    return priority;
  }

  /**
   * @param message
   *          The message to set.
   */
  public void setMessage( String message ) {
    this.message = message;
  }

  /**
   * @return Returns the comString.
   */
  public String getMessage() {
    return message;
  }

  public void addTimestamp( boolean value ) {
    this.addTimestamp = value;
  }

  /**
   * @return Returns the addHostname.
   */
  public boolean isAddHostName() {
    return addHostname;
  }

  public void addHostName( boolean value ) {
    this.addHostname = value;
  }

  /**
   * @return Returns the addTimestamp.
   */
  public boolean isAddTimestamp() {
    return addTimestamp;
  }

  /**
   * @param pattern
   *          The datePattern to set.
   */
  public void setDatePattern( String pattern ) {
    this.datePattern = pattern;
  }

  /**
   * @return Returns the datePattern.
   */
  public String getDatePattern() {
    return datePattern;
  }

  /**
   * @return Returns the port.
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          The port to set.
   */
  public void setPort( String port ) {
    this.port = port;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    String servername = environmentSubstitute( getServerName() );

    if ( Utils.isEmpty( servername ) ) {
      logError( BaseMessages.getString( PKG, "JobEntrySyslog.MissingServerName" ) );
    }

    String messageString = environmentSubstitute( getMessage() );

    if ( Utils.isEmpty( messageString ) ) {
      logError( BaseMessages.getString( PKG, "JobEntrySyslog.MissingMessage" ) );
    }

    int nrPort = Const.toInt( environmentSubstitute( getPort() ), SyslogDefs.DEFAULT_PORT );

    SyslogIF syslog = null;
    try {
      String pattern = null;

      if ( isAddTimestamp() ) {
        // add timestamp to message
        pattern = environmentSubstitute( getDatePattern() );
        if ( Utils.isEmpty( pattern ) ) {
          logError( BaseMessages.getString( PKG, "JobEntrySyslog.DatePatternEmpty" ) );
          throw new KettleException( BaseMessages.getString( PKG, "JobEntrySyslog.DatePatternEmpty" ) );
        }

      }

      // Open syslog connection
      // Set a Specific Host, then Log to It
      syslog = Syslog.getInstance( "udp" );
      syslog.getConfig().setHost( servername );
      syslog.getConfig().setPort( nrPort );
      syslog.getConfig().setFacility( getFacility() );
      syslog.getConfig().setSendLocalName( false );
      syslog.getConfig().setSendLocalTimestamp( false );
      SyslogDefs.sendMessage(
        syslog, SyslogDefs.getPriority( getPriority() ), messageString, isAddTimestamp(), pattern,
        isAddHostName() );

      // message was sent
      result.setNrErrors( 0 );
      result.setResult( true );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntrySyslog.ErrorSendingMessage", e.toString() ) );
    } finally {
      if ( syslog != null ) {
        syslog.shutdown();
      }
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

  }

}
