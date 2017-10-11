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

package org.pentaho.di.job.entries.sendnagiospassivecheck;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

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
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.googlecode.jsendnsca.Level;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;

/**
 * This defines an SendNagiosPassiveCheck job entry.
 *
 * @author Samatar
 * @since 01-10-2011
 *
 */

public class JobEntrySendNagiosPassiveCheck extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySendNagiosPassiveCheck.class; // for i18n purposes, needed by Translator2!!

  private String serverName;
  private String port;
  private String responseTimeOut;
  private String connectionTimeOut;

  private String message;
  private String senderServerName;
  private String senderServiceName;
  private int encryptionMode;
  private int level;

  private String password;

  /**
   * Default responseTimeOut to 1000 milliseconds
   */
  private static int DEFAULT_RESPONSE_TIME_OUT = 10000; // ms

  /**
   * Default connection responseTimeOut to 5000 milliseconds
   */
  public static int DEFAULT_CONNECTION_TIME_OUT = 5000; // ms

  /**
   * Default port
   */
  public static int DEFAULT_PORT = 5667;

  public static final String[] encryption_mode_Desc = new String[] {
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.EncryptionMode.None" ),
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.EncryptionMode.TripleDES" ),
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.EncryptionMode.XOR" ) };
  public static final String[] encryption_mode_Code = new String[] { "none", "tripledes", "xor" };

  public static final int ENCRYPTION_MODE_NONE = 0;
  public static final int ENCRYPTION_MODE_TRIPLEDES = 1;
  public static final int ENCRYPTION_MODE_XOR = 2;

  public static final String[] level_type_Desc = new String[] {
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.LevelType.Unknown" ),
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.EncryptionMode.OK" ),
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.EncryptionMode.Warning" ),
    BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.EncryptionMode.Critical" ) };
  public static final String[] level_type_Code = new String[] { "unknown", "ok", "warning", "critical" };

  public static final int LEVEL_TYPE_UNKNOWN = 0;
  public static final int LEVEL_TYPE_OK = 1;
  public static final int LEVEL_TYPE_WARNING = 2;
  public static final int LEVEL_TYPE_CRITICAL = 3;

  public JobEntrySendNagiosPassiveCheck( String n ) {
    super( n, "" );
    port = "" + DEFAULT_PORT;
    serverName = null;
    connectionTimeOut = String.valueOf( DEFAULT_CONNECTION_TIME_OUT );
    responseTimeOut = String.valueOf( DEFAULT_RESPONSE_TIME_OUT );
    message = null;
    senderServerName = null;
    senderServiceName = null;
    encryptionMode = ENCRYPTION_MODE_NONE;
    level = LEVEL_TYPE_UNKNOWN;
    password = null;
  }

  public JobEntrySendNagiosPassiveCheck() {
    this( "" );
  }

  public Object clone() {
    JobEntrySendNagiosPassiveCheck je = (JobEntrySendNagiosPassiveCheck) super.clone();
    return je;
  }

  public static int getEncryptionModeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < encryption_mode_Desc.length; i++ ) {
      if ( encryption_mode_Desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getEncryptionModeByCode( tt );
  }

  public static String getEncryptionModeDesc( int i ) {
    if ( i < 0 || i >= encryption_mode_Desc.length ) {
      return encryption_mode_Desc[0];
    }
    return encryption_mode_Desc[i];
  }

  public static String getLevelDesc( int i ) {
    if ( i < 0 || i >= level_type_Desc.length ) {
      return level_type_Desc[0];
    }
    return level_type_Desc[i];
  }

  public static int getLevelByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < level_type_Desc.length; i++ ) {
      if ( level_type_Desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getEncryptionModeByCode( tt );
  }

  private static String getEncryptionModeCode( int i ) {
    if ( i < 0 || i >= encryption_mode_Code.length ) {
      return encryption_mode_Code[0];
    }
    return encryption_mode_Code[i];
  }

  private String getLevelCode( int i ) {
    if ( i < 0 || i >= level_type_Code.length ) {
      return level_type_Code[0];
    }
    return level_type_Code[i];
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "password", password ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "responseTimeOut", responseTimeOut ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "connectionTimeOut", connectionTimeOut ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "senderServerName", senderServerName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "senderServiceName", senderServiceName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "message", message ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "encryptionMode", getEncryptionModeCode( encryptionMode ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "level", getLevelCode( level ) ) );

    return retval.toString();
  }

  private static int getEncryptionModeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < encryption_mode_Code.length; i++ ) {
      if ( encryption_mode_Code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getLevelByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < level_type_Code.length; i++ ) {
      if ( level_type_Code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      port = XMLHandler.getTagValue( entrynode, "port" );
      serverName = XMLHandler.getTagValue( entrynode, "servername" );
      responseTimeOut = XMLHandler.getTagValue( entrynode, "responseTimeOut" );
      connectionTimeOut = XMLHandler.getTagValue( entrynode, "connectionTimeOut" );
      password = XMLHandler.getTagValue( entrynode, "password" );

      senderServerName = XMLHandler.getTagValue( entrynode, "senderServerName" );
      senderServiceName = XMLHandler.getTagValue( entrynode, "senderServiceName" );
      message = XMLHandler.getTagValue( entrynode, "message" );

      encryptionMode = getEncryptionModeByCode( XMLHandler.getTagValue( entrynode, "encryptionMode" ) );
      level = getLevelByCode( XMLHandler.getTagValue( entrynode, "level" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'SendNagiosPassiveCheck' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      serverName = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      password = rep.getJobEntryAttributeString( id_jobentry, "password" );
      responseTimeOut = rep.getJobEntryAttributeString( id_jobentry, "responseTimeOut" );
      connectionTimeOut = rep.getJobEntryAttributeString( id_jobentry, "connectionTimeOut" );

      senderServerName = rep.getJobEntryAttributeString( id_jobentry, "senderServerName" );
      senderServiceName = rep.getJobEntryAttributeString( id_jobentry, "senderServiceName" );

      message = rep.getJobEntryAttributeString( id_jobentry, "message" );

      encryptionMode = getEncryptionModeByCode( rep.getJobEntryAttributeString( id_jobentry, "encryptionMode" ) );
      level = getLevelByCode( rep.getJobEntryAttributeString( id_jobentry, "level" ) );

    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'SendNagiosPassiveCheck' from the repository for id_jobentry="
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", password );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "responseTimeOut", responseTimeOut );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "connectionTimeOut", connectionTimeOut );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "senderServerName", senderServerName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "senderServiceName", senderServiceName );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "message", message );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "encryptionMode", getEncryptionModeCode( encryptionMode ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "level", getLevelCode( level ) );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'SendNagiosPassiveCheck' to the repository for id_job=" + id_job, dbe );
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
   * @return Returns the senderServerName.
   */
  public String getSenderServerName() {
    return senderServerName;
  }

  /**
   * @param senderServerName
   *          The senderServerName to set.
   */
  public void setSenderServerName( String senderServerName ) {
    this.senderServerName = senderServerName;
  }

  /**
   * @return Returns the senderServiceName.
   */
  public String getSenderServiceName() {
    return senderServiceName;
  }

  /**
   * @param senderServiceName
   *          The senderServiceName to set.
   */
  public void setSenderServiceName( String senderServiceName ) {
    this.senderServiceName = senderServiceName;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  public int getEncryptionMode() {
    return encryptionMode;
  }

  public void setEncryptionMode( int encryptionModein ) {
    this.encryptionMode = encryptionModein;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel( int levelMode ) {
    this.level = levelMode;
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

  /**
   * @param responseTimeOut
   *          The responseTimeOut to set.
   */
  public void setResponseTimeOut( String responseTimeOut ) {
    this.responseTimeOut = responseTimeOut;
  }

  /**
   * @return Returns the responseTimeOut.
   */
  public String getResponseTimeOut() {
    return responseTimeOut;
  }

  /**
   * @param connectionTimeOut
   *          The connectionTimeOut to set.
   */
  public void setConnectionTimeOut( String connectionTimeOut ) {
    this.connectionTimeOut = connectionTimeOut;
  }

  /**
   * @return Returns the connectionTimeOut.
   */
  public String getConnectionTimeOut() {
    return connectionTimeOut;
  }

  public Result execute( Result previousResult, int nr ) {
    log.logBasic( BaseMessages.getString( PKG, "JobEntrySendNagiosPassiveCheck.Started", serverName ) );

    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    // Target
    String realServername = environmentSubstitute( serverName );
    String realPassword = Utils.resolvePassword( variables, password );
    int realPort = Const.toInt( environmentSubstitute( port ), DEFAULT_PORT );
    int realResponseTimeOut = Const.toInt( environmentSubstitute( responseTimeOut ), DEFAULT_RESPONSE_TIME_OUT );
    int realConnectionTimeOut =
      Const.toInt( environmentSubstitute( connectionTimeOut ), DEFAULT_CONNECTION_TIME_OUT );

    // Sender
    String realSenderServerName = environmentSubstitute( senderServerName );
    String realSenderServiceName = environmentSubstitute( senderServiceName );

    try {
      if ( Utils.isEmpty( realServername ) ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "JobSendNagiosPassiveCheck.Error.TargetServerMissing" ) );
      }

      String realMessageString = environmentSubstitute( message );

      if ( Utils.isEmpty( realMessageString ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "JobSendNagiosPassiveCheck.Error.MessageMissing" ) );
      }

      Level level = Level.UNKNOWN;
      switch ( getLevel() ) {
        case LEVEL_TYPE_OK:
          level = Level.OK;
          break;
        case LEVEL_TYPE_CRITICAL:
          level = Level.CRITICAL;
          break;
        case LEVEL_TYPE_WARNING:
          level = Level.WARNING;
          break;
        default:
          break;
      }
      Encryption encr = Encryption.NONE;
      switch ( getEncryptionMode() ) {
        case ENCRYPTION_MODE_TRIPLEDES:
          encr = Encryption.TRIPLE_DES;
          break;
        case ENCRYPTION_MODE_XOR:
          encr = Encryption.XOR;
          break;
        default:
          break;
      }

      // settings
      NagiosSettingsBuilder ns = new NagiosSettingsBuilder();
      ns.withNagiosHost( realServername );
      ns.withPort( realPort );
      ns.withConnectionTimeout( realConnectionTimeOut );
      ns.withResponseTimeout( realResponseTimeOut );
      ns.withEncryption( encr );
      if ( !Utils.isEmpty( realPassword ) ) {
        ns.withPassword( realPassword );
      } else {
        ns.withNoPassword();
      }

      // target nagios host
      NagiosSettings settings = ns.create();

      // sender
      MessagePayloadBuilder pb = new MessagePayloadBuilder();
      if ( !Utils.isEmpty( realSenderServerName ) ) {
        pb.withHostname( realSenderServerName );
      }
      pb.withLevel( level );
      if ( !Utils.isEmpty( realSenderServiceName ) ) {
        pb.withServiceName( realSenderServiceName );
      }
      pb.withMessage( realMessageString );
      MessagePayload payload = pb.create();

      NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender( settings );

      sender.send( payload );

      result.setNrErrors( 0 );
      result.setResult( true );

    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "JobEntrySendNagiosPassiveCheck.ErrorGetting", e.toString() ) );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( serverName ) ) {
      String realServername = jobMeta.environmentSubstitute( serverName );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "serverName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

}
