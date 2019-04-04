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

package org.pentaho.di.job.entries.ftp;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
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

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPFileFactory;
import com.enterprisedt.net.ftp.FTPFileParser;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This defines an FTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntryFTP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryFTP.class; // for i18n purposes, needed by Translator2!!

  private String serverName;

  private String userName;

  private String password;

  private String ftpDirectory;

  private String targetDirectory;

  private String wildcard;

  private boolean binaryMode;

  private int timeout;

  private boolean remove;

  private boolean onlyGettingNewFiles; /* Don't overwrite files */

  private boolean activeConnection;

  private String controlEncoding; /* how to convert list of filenames e.g. */

  /**
   * Implicit encoding used before PDI v2.4.1
   */
  private static String LEGACY_CONTROL_ENCODING = "US-ASCII";

  /**
   * Default encoding when making a new ftp job entry instance.
   */
  private static String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

  private boolean movefiles;

  private String movetodirectory;

  private boolean adddate;

  private boolean addtime;

  private boolean SpecifyFormat;

  private String date_time_format;

  private boolean AddDateBeforeExtension;

  private boolean isaddresult;

  private boolean createmovefolder;

  private String port;

  private String proxyHost;

  private String proxyPort; /* string to allow variable substitution */

  private String proxyUsername;

  private String proxyPassword;

  private String socksProxyHost;

  private String socksProxyPort;

  private String socksProxyUsername;

  private String socksProxyPassword;

  public int ifFileExistsSkip = 0;

  public String SifFileExistsSkip = "ifFileExistsSkip";

  public int ifFileExistsCreateUniq = 1;

  public String SifFileExistsCreateUniq = "ifFileExistsCreateUniq";

  public int ifFileExistsFail = 2;

  public String SifFileExistsFail = "ifFileExistsFail";

  public int ifFileExists;

  public String SifFileExists;

  public String SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED = "success_when_at_least";

  public String SUCCESS_IF_ERRORS_LESS = "success_if_errors_less";

  public String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  private String nr_limit;

  private String success_condition;

  long NrErrors = 0;

  long NrfilesRetrieved = 0;

  boolean successConditionBroken = false;

  int limitFiles = 0;

  String targetFilename = null;

  static String FILE_SEPARATOR = "/";

  public JobEntryFTP( String n ) {
    super( n, "" );
    nr_limit = "10";
    port = "21";
    socksProxyPort = "1080";
    success_condition = SUCCESS_IF_NO_ERRORS;
    ifFileExists = ifFileExistsSkip;
    SifFileExists = SifFileExistsSkip;

    serverName = null;
    movefiles = false;
    movetodirectory = null;
    adddate = false;
    addtime = false;
    SpecifyFormat = false;
    AddDateBeforeExtension = false;
    isaddresult = true;
    createmovefolder = false;

    setControlEncoding( DEFAULT_CONTROL_ENCODING );
  }

  public JobEntryFTP() {
    this( "" );
  }

  public Object clone() {
    JobEntryFTP je = (JobEntryFTP) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 650 ); // 528 chars in spaces and tags alone

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "ftpdirectory", ftpDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "targetdirectory", targetDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "binary", binaryMode ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "remove", remove ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "only_new", onlyGettingNewFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "active", activeConnection ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "control_encoding", controlEncoding ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "movefiles", movefiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "movetodirectory", movetodirectory ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "adddate", adddate ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addtime", addtime ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "SpecifyFormat", SpecifyFormat ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "date_time_format", date_time_format ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "AddDateBeforeExtension", AddDateBeforeExtension ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "isaddresult", isaddresult ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "createmovefolder", createmovefolder ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_host", proxyHost ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_port", proxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_username", proxyUsername ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "proxy_password", Encr.encryptPasswordIfNotUsingVariables( proxyPassword ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_host", socksProxyHost ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_port", socksProxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_username", socksProxyUsername ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "socksproxy_password", Encr
        .encryptPasswordIfNotUsingVariables( socksProxyPassword ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "ifFileExists", SifFileExists ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_limit", nr_limit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      port = XMLHandler.getTagValue( entrynode, "port" );
      serverName = XMLHandler.getTagValue( entrynode, "servername" );
      userName = XMLHandler.getTagValue( entrynode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );
      ftpDirectory = XMLHandler.getTagValue( entrynode, "ftpdirectory" );
      targetDirectory = XMLHandler.getTagValue( entrynode, "targetdirectory" );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      binaryMode = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "binary" ) );
      timeout = Const.toInt( XMLHandler.getTagValue( entrynode, "timeout" ), 10000 );
      remove = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "remove" ) );
      onlyGettingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "only_new" ) );
      activeConnection = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "active" ) );
      controlEncoding = XMLHandler.getTagValue( entrynode, "control_encoding" );
      if ( controlEncoding == null ) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the the encoding used before v 2.4.0
        controlEncoding = LEGACY_CONTROL_ENCODING;
      }
      movefiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "movefiles" ) );
      movetodirectory = XMLHandler.getTagValue( entrynode, "movetodirectory" );

      adddate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "adddate" ) );
      addtime = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addtime" ) );
      SpecifyFormat = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "SpecifyFormat" ) );
      date_time_format = XMLHandler.getTagValue( entrynode, "date_time_format" );
      AddDateBeforeExtension =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "AddDateBeforeExtension" ) );

      String addresult = XMLHandler.getTagValue( entrynode, "isaddresult" );

      if ( Utils.isEmpty( addresult ) ) {
        isaddresult = true;
      } else {
        isaddresult = "Y".equalsIgnoreCase( addresult );
      }

      createmovefolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createmovefolder" ) );

      proxyHost = XMLHandler.getTagValue( entrynode, "proxy_host" );
      proxyPort = XMLHandler.getTagValue( entrynode, "proxy_port" );
      proxyUsername = XMLHandler.getTagValue( entrynode, "proxy_username" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "proxy_password" ) );
      socksProxyHost = XMLHandler.getTagValue( entrynode, "socksproxy_host" );
      socksProxyPort = XMLHandler.getTagValue( entrynode, "socksproxy_port" );
      socksProxyUsername = XMLHandler.getTagValue( entrynode, "socksproxy_username" );
      socksProxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "socksproxy_password" ) );
      SifFileExists = XMLHandler.getTagValue( entrynode, "ifFileExists" );
      if ( Utils.isEmpty( SifFileExists ) ) {
        ifFileExists = ifFileExistsSkip;
      } else {
        if ( SifFileExists.equals( SifFileExistsCreateUniq ) ) {
          ifFileExists = ifFileExistsCreateUniq;
        } else if ( SifFileExists.equals( SifFileExistsFail ) ) {
          ifFileExists = ifFileExistsFail;
        } else {
          ifFileExists = ifFileExistsSkip;
        }

      }
      nr_limit = XMLHandler.getTagValue( entrynode, "nr_limit" );
      success_condition =
        Const.NVL( XMLHandler.getTagValue( entrynode, "success_condition" ), SUCCESS_IF_NO_ERRORS );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'ftp' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      serverName = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      userName = rep.getJobEntryAttributeString( id_jobentry, "username" );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );
      ftpDirectory = rep.getJobEntryAttributeString( id_jobentry, "ftpdirectory" );
      targetDirectory = rep.getJobEntryAttributeString( id_jobentry, "targetdirectory" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      binaryMode = rep.getJobEntryAttributeBoolean( id_jobentry, "binary" );
      timeout = (int) rep.getJobEntryAttributeInteger( id_jobentry, "timeout" );
      remove = rep.getJobEntryAttributeBoolean( id_jobentry, "remove" );
      onlyGettingNewFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "only_new" );
      activeConnection = rep.getJobEntryAttributeBoolean( id_jobentry, "active" );
      controlEncoding = rep.getJobEntryAttributeString( id_jobentry, "control_encoding" );
      if ( controlEncoding == null ) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the the encoding used before v 2.4.0
        controlEncoding = LEGACY_CONTROL_ENCODING;
      }

      movefiles = rep.getJobEntryAttributeBoolean( id_jobentry, "movefiles" );
      movetodirectory = rep.getJobEntryAttributeString( id_jobentry, "movetodirectory" );

      adddate = rep.getJobEntryAttributeBoolean( id_jobentry, "adddate" );
      addtime = rep.getJobEntryAttributeBoolean( id_jobentry, "addtime" );
      SpecifyFormat = rep.getJobEntryAttributeBoolean( id_jobentry, "SpecifyFormat" );
      date_time_format = rep.getJobEntryAttributeString( id_jobentry, "date_time_format" );
      AddDateBeforeExtension = rep.getJobEntryAttributeBoolean( id_jobentry, "AddDateBeforeExtension" );

      String addToResult = rep.getJobEntryAttributeString( id_jobentry, "isaddresult" );
      if ( Utils.isEmpty( addToResult ) ) {
        isaddresult = true;
      } else {
        isaddresult = rep.getJobEntryAttributeBoolean( id_jobentry, "isaddresult" );
      }

      createmovefolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createmovefolder" );

      proxyHost = rep.getJobEntryAttributeString( id_jobentry, "proxy_host" );
      proxyPort = rep.getJobEntryAttributeString( id_jobentry, "proxy_port" );
      proxyUsername = rep.getJobEntryAttributeString( id_jobentry, "proxy_username" );
      proxyPassword =
        Encr
          .decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "proxy_password" ) );
      socksProxyHost = rep.getJobEntryAttributeString( id_jobentry, "socksproxy_host" );
      socksProxyPort = rep.getJobEntryAttributeString( id_jobentry, "socksproxy_port" );
      socksProxyUsername = rep.getJobEntryAttributeString( id_jobentry, "socksproxy_username" );
      socksProxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(
          id_jobentry, "socksproxy_password" ) );
      SifFileExists = rep.getJobEntryAttributeString( id_jobentry, "ifFileExists" );
      if ( Utils.isEmpty( SifFileExists ) ) {
        ifFileExists = ifFileExistsSkip;
      } else {
        if ( SifFileExists.equals( SifFileExistsCreateUniq ) ) {
          ifFileExists = ifFileExistsCreateUniq;
        } else if ( SifFileExists.equals( SifFileExistsFail ) ) {
          ifFileExists = ifFileExistsFail;
        } else {
          ifFileExists = ifFileExistsSkip;
        }
      }
      nr_limit = rep.getJobEntryAttributeString( id_jobentry, "nr_limit" );
      success_condition =
        Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "success_condition" ), SUCCESS_IF_NO_ERRORS );

    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'ftp' from the repository for id_jobentry="
        + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", userName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ftpdirectory", ftpDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targetdirectory", targetDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "binary", binaryMode );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "remove", remove );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "only_new", onlyGettingNewFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "active", activeConnection );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "control_encoding", controlEncoding );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "movefiles", movefiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "movetodirectory", movetodirectory );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "addtime", addtime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "adddate", adddate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "SpecifyFormat", SpecifyFormat );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "date_time_format", date_time_format );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "AddDateBeforeExtension", AddDateBeforeExtension );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "isaddresult", isaddresult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "createmovefolder", createmovefolder );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_host", proxyHost );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_port", proxyPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_username", proxyUsername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_password", Encr
        .encryptPasswordIfNotUsingVariables( proxyPassword ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_host", socksProxyHost );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_port", socksProxyPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_username", socksProxyUsername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_password", Encr
        .encryptPasswordIfNotUsingVariables( socksProxyPassword ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ifFileExists", SifFileExists );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "nr_limit", nr_limit );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'ftp' to the repository for id_job=" + id_job, dbe );
    }
  }

  public void setLimit( String nr_limitin ) {
    this.nr_limit = nr_limitin;
  }

  public String getLimit() {
    return nr_limit;
  }

  public void setSuccessCondition( String success_condition ) {
    this.success_condition = success_condition;
  }

  public String getSuccessCondition() {
    return success_condition;
  }

  public void setCreateMoveFolder( boolean createmovefolderin ) {
    this.createmovefolder = createmovefolderin;
  }

  public boolean isCreateMoveFolder() {
    return createmovefolder;
  }

  public void setAddDateBeforeExtension( boolean AddDateBeforeExtension ) {
    this.AddDateBeforeExtension = AddDateBeforeExtension;
  }

  public boolean isAddDateBeforeExtension() {
    return AddDateBeforeExtension;
  }

  public void setAddToResult( boolean isaddresultin ) {
    this.isaddresult = isaddresultin;
  }

  public boolean isAddToResult() {
    return isaddresult;
  }

  public void setDateInFilename( boolean adddate ) {
    this.adddate = adddate;
  }

  public boolean isDateInFilename() {
    return adddate;
  }

  public void setTimeInFilename( boolean addtime ) {
    this.addtime = addtime;
  }

  public boolean isTimeInFilename() {
    return addtime;
  }

  public boolean isSpecifyFormat() {
    return SpecifyFormat;
  }

  public void setSpecifyFormat( boolean SpecifyFormat ) {
    this.SpecifyFormat = SpecifyFormat;
  }

  public String getDateTimeFormat() {
    return date_time_format;
  }

  public void setDateTimeFormat( String date_time_format ) {
    this.date_time_format = date_time_format;
  }

  /**
   * @return Returns the movefiles.
   */
  public boolean isMoveFiles() {
    return movefiles;
  }

  /**
   * @param movefilesin
   *          The movefiles to set.
   */
  public void setMoveFiles( boolean movefilesin ) {
    this.movefiles = movefilesin;
  }

  /**
   * @return Returns the movetodirectory.
   */
  public String getMoveToDirectory() {
    return movetodirectory;
  }

  /**
   * @param movetoin
   *          The movetodirectory to set.
   */
  public void setMoveToDirectory( String movetoin ) {
    this.movetodirectory = movetoin;
  }

  /**
   * @return Returns the binaryMode.
   */
  public boolean isBinaryMode() {
    return binaryMode;
  }

  /**
   * @param binaryMode
   *          The binaryMode to set.
   */
  public void setBinaryMode( boolean binaryMode ) {
    this.binaryMode = binaryMode;
  }

  /**
   * @return Returns the directory.
   */
  public String getFtpDirectory() {
    return ftpDirectory;
  }

  /**
   * @param directory
   *          The directory to set.
   */
  public void setFtpDirectory( String directory ) {
    this.ftpDirectory = directory;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
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
   * @return Returns the userName.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName
   *          The userName to set.
   */
  public void setUserName( String userName ) {
    this.userName = userName;
  }

  /**
   * @return Returns the wildcard.
   */
  public String getWildcard() {
    return wildcard;
  }

  /**
   * @param wildcard
   *          The wildcard to set.
   */
  public void setWildcard( String wildcard ) {
    this.wildcard = wildcard;
  }

  /**
   * @return Returns the targetDirectory.
   */
  public String getTargetDirectory() {
    return targetDirectory;
  }

  /**
   * @param targetDirectory
   *          The targetDirectory to set.
   */
  public void setTargetDirectory( String targetDirectory ) {
    this.targetDirectory = targetDirectory;
  }

  /**
   * @param timeout
   *          The timeout to set.
   */
  public void setTimeout( int timeout ) {
    this.timeout = timeout;
  }

  /**
   * @return Returns the timeout.
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * @param remove
   *          The remove to set.
   */
  public void setRemove( boolean remove ) {
    this.remove = remove;
  }

  /**
   * @return Returns the remove.
   */
  public boolean getRemove() {
    return remove;
  }

  /**
   * @return Returns the onlyGettingNewFiles.
   */
  public boolean isOnlyGettingNewFiles() {
    return onlyGettingNewFiles;
  }

  /**
   * @param onlyGettingNewFiles
   *          The onlyGettingNewFiles to set.
   */
  public void setOnlyGettingNewFiles( boolean onlyGettingNewFilesin ) {
    this.onlyGettingNewFiles = onlyGettingNewFilesin;
  }

  /**
   * Get the control encoding to be used for ftp'ing
   *
   * @return the used encoding
   */
  public String getControlEncoding() {
    return controlEncoding;
  }

  /**
   * Set the encoding to be used for ftp'ing. This determines how names are translated in dir e.g. It does impact the
   * contents of the files being ftp'ed.
   *
   * @param encoding
   *          The encoding to be used.
   */
  public void setControlEncoding( String encoding ) {
    this.controlEncoding = encoding;
  }

  /**
   * @return Returns the hostname of the ftp-proxy.
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * @param proxyHost
   *          The hostname of the proxy.
   */
  public void setProxyHost( String proxyHost ) {
    this.proxyHost = proxyHost;
  }

  /**
   * @param proxyPassword
   *          The password which is used to authenticate at the socks proxy.
   */
  public void setProxyPassword( String proxyPassword ) {
    this.proxyPassword = proxyPassword;
  }

  /**
   * @return Returns the password which is used to authenticate at the proxy.
   */
  public String getProxyPassword() {
    return proxyPassword;
  }

  /**
   * @param proxyPassword
   *          The password which is used to authenticate at the proxy.
   */
  public void setSocksProxyPassword( String socksProxyPassword ) {
    this.socksProxyPassword = socksProxyPassword;
  }

  /**
   * @return Returns the password which is used to authenticate at the socks proxy.
   */
  public String getSocksProxyPassword() {
    return socksProxyPassword;
  }

  /**
   * @param proxyPort
   *          The port of the ftp-proxy.
   */
  public void setProxyPort( String proxyPort ) {
    this.proxyPort = proxyPort;
  }

  /**
   * @return Returns the port of the ftp-proxy.
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * @return Returns the username which is used to authenticate at the proxy.
   */
  public String getProxyUsername() {
    return proxyUsername;
  }

  /**
   * @param proxyUsername
   *          The username which is used to authenticate at the proxy.
   */
  public void setProxyUsername( String proxyUsername ) {
    this.proxyUsername = proxyUsername;
  }

  /**
   * @return Returns the username which is used to authenticate at the socks proxy.
   */
  public String getSocksProxyUsername() {
    return socksProxyUsername;
  }

  /**
   * @param proxyUsername
   *          The username which is used to authenticate at the socks proxy.
   */
  public void setSocksProxyUsername( String socksPoxyUsername ) {
    this.socksProxyUsername = socksPoxyUsername;
  }

  /**
   *
   * @param socksProxyHost
   *          The host name of the socks proxy host
   */
  public void setSocksProxyHost( String socksProxyHost ) {
    this.socksProxyHost = socksProxyHost;
  }

  /**
   * @return The host name of the socks proxy host
   */
  public String getSocksProxyHost() {
    return this.socksProxyHost;
  }

  /**
   * @param socksProxyPort
   *          The port number the socks proxy host is using
   */
  public void setSocksProxyPort( String socksProxyPort ) {
    this.socksProxyPort = socksProxyPort;
  }

  /**
   * @return The port number the socks proxy host is using
   */
  public String getSocksProxyPort() {
    return this.socksProxyPort;
  }

  protected FTPClient initFTPClient() {
    return new FTPClient();
  }

  protected InetAddress getInetAddress( String realServername ) throws UnknownHostException {
    return InetAddress.getByName( realServername );
  }

  public Result execute( Result previousResult, int nr ) {
    log.logBasic( BaseMessages.getString( PKG, "JobEntryFTP.Started", serverName ) );

    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );
    NrErrors = 0;
    NrfilesRetrieved = 0;
    successConditionBroken = false;
    boolean exitjobentry = false;
    limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );

    // Here let's put some controls before stating the job
    if ( movefiles ) {
      if ( Utils.isEmpty( movetodirectory ) ) {
        logError( BaseMessages.getString( PKG, "JobEntryFTP.MoveToFolderEmpty" ) );
        return result;
      }

    }

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.Start" ) );
    }

    FTPClient ftpclient = null;
    String realMoveToFolder = null;

    try {
      // Create ftp client to host:port ...
      ftpclient = initFTPClient();
      String realServername = environmentSubstitute( serverName );
      String realServerPort = environmentSubstitute( port );
      ftpclient.setRemoteAddr( getInetAddress( realServername ) );
      if ( !Utils.isEmpty( realServerPort ) ) {
        ftpclient.setRemotePort( Const.toInt( realServerPort, 21 ) );
      }

      if ( !Utils.isEmpty( proxyHost ) ) {
        String realProxy_host = environmentSubstitute( proxyHost );
        ftpclient.setRemoteAddr( InetAddress.getByName( realProxy_host ) );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.OpenedProxyConnectionOn", realProxy_host ) );
        }

        // FIXME: Proper default port for proxy
        int port = Const.toInt( environmentSubstitute( proxyPort ), 21 );
        if ( port != 0 ) {
          ftpclient.setRemotePort( port );
        }
      } else {
        ftpclient.setRemoteAddr( getInetAddress( realServername ) );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.OpenedConnectionTo", realServername ) );
        }
      }

      // set activeConnection connectmode ...
      if ( activeConnection ) {
        ftpclient.setConnectMode( FTPConnectMode.ACTIVE );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.SetActive" ) );
        }
      } else {
        ftpclient.setConnectMode( FTPConnectMode.PASV );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.SetPassive" ) );
        }
      }

      // Set the timeout
      ftpclient.setTimeout( timeout );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.SetTimeout", String.valueOf( timeout ) ) );
      }

      ftpclient.setControlEncoding( controlEncoding );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.SetEncoding", controlEncoding ) );
      }

      // If socks proxy server was provided
      if ( !Utils.isEmpty( socksProxyHost ) ) {
        if ( !Utils.isEmpty( socksProxyPort ) ) {
          FTPClient.initSOCKS( environmentSubstitute( socksProxyPort ), environmentSubstitute( socksProxyHost ) );
        } else {
          throw new FTPException( BaseMessages.getString(
            PKG, "JobEntryFTP.SocksProxy.PortMissingException", environmentSubstitute( socksProxyHost ),
            getName() ) );
        }
        // then if we have authentication information
        if ( !Utils.isEmpty( socksProxyUsername ) && !Utils.isEmpty( socksProxyPassword ) ) {
          FTPClient.initSOCKSAuthentication(
            environmentSubstitute( socksProxyUsername ), Utils.resolvePassword( this, socksProxyPassword ) );
        } else if ( !Utils.isEmpty( socksProxyUsername )
          && Utils.isEmpty( socksProxyPassword ) || Utils.isEmpty( socksProxyUsername )
          && !Utils.isEmpty( socksProxyPassword ) ) {
          // we have a username without a password or vica versa
          throw new FTPException( BaseMessages.getString(
            PKG, "JobEntryFTP.SocksProxy.IncompleteCredentials", environmentSubstitute( socksProxyHost ),
            getName() ) );
        }
      }

      // login to ftp host ...
      ftpclient.connect();

      String realUsername =
        environmentSubstitute( userName )
          + ( !Utils.isEmpty( proxyHost ) ? "@" + realServername : "" )
          + ( !Utils.isEmpty( proxyUsername ) ? " " + environmentSubstitute( proxyUsername ) : "" );

      String realPassword =
              Utils.resolvePassword( this, password )
          + ( !Utils.isEmpty( proxyPassword ) ? " "
            + Utils.resolvePassword( this, proxyPassword ) : "" );

      ftpclient.login( realUsername, realPassword );
      // Remove password from logging, you don't know where it ends up.
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.LoggedIn", realUsername ) );
      }

      // Fix for PDI-2534 - add auxilliary FTP File List parsers to the ftpclient object.
      this.hookInOtherParsers( ftpclient );

      // move to spool dir ...
      if ( !Utils.isEmpty( ftpDirectory ) ) {
        String realFtpDirectory = environmentSubstitute( ftpDirectory );
        realFtpDirectory = normalizePath( realFtpDirectory );
        ftpclient.chdir( realFtpDirectory );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.ChangedDir", realFtpDirectory ) );
        }
      }

      // Create move to folder if necessary
      if ( movefiles && !Utils.isEmpty( movetodirectory ) ) {
        realMoveToFolder = environmentSubstitute( movetodirectory );
        realMoveToFolder = normalizePath( realMoveToFolder );
        // Folder exists?
        boolean folderExist = true;
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.CheckMoveToFolder", realMoveToFolder ) );
        }
        String originalLocation = ftpclient.pwd();
        try {
          // does not work for folders, see PDI-2567: folderExist=ftpclient.exists(realMoveToFolder);
          // try switching to the 'move to' folder.
          ftpclient.chdir( realMoveToFolder );
          // Switch back to the previous location.
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.CheckMoveToFolderSwitchBack", originalLocation ) );
          }
          ftpclient.chdir( originalLocation );
        } catch ( Exception e ) {
          folderExist = false;
          // Assume folder does not exist !!
        }

        if ( !folderExist ) {
          if ( createmovefolder ) {
            ftpclient.mkdir( realMoveToFolder );
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.MoveToFolderCreated", realMoveToFolder ) );
            }
          } else {
            logError( BaseMessages.getString( PKG, "JobEntryFTP.MoveToFolderNotExist" ) );
            exitjobentry = true;
            NrErrors++;
          }
        }
      }

      if ( !exitjobentry ) {
        // Get all the files in the current directory...
        FTPFile[] ftpFiles = ftpclient.dirDetails( null );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.FoundNFiles", String.valueOf( ftpFiles.length ) ) );
        }

        // set transfertype ...
        if ( binaryMode ) {
          ftpclient.setType( FTPTransferType.BINARY );
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.SetBinary" ) );
          }
        } else {
          ftpclient.setType( FTPTransferType.ASCII );
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.SetAscii" ) );
          }
        }

        // Some FTP servers return a message saying no files found as a string in the filenlist
        // e.g. Solaris 8
        // CHECK THIS !!!

        if ( ftpFiles.length == 1 ) {
          String translatedWildcard = environmentSubstitute( wildcard );
          if ( !Utils.isEmpty( translatedWildcard ) ) {
            if ( ftpFiles[0].getName().startsWith( translatedWildcard ) ) {
              throw new FTPException( ftpFiles[0].getName() );
            }
          }
        }

        Pattern pattern = null;
        if ( !Utils.isEmpty( wildcard ) ) {
          String realWildcard = environmentSubstitute( wildcard );
          pattern = Pattern.compile( realWildcard );
        }

        if ( !getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) ) {
          limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );
        }

        // Get the files in the list...
        for ( FTPFile ftpFile : ftpFiles ) {

          if ( parentJob.isStopped() ) {
            exitjobentry = true;
            throw new Exception( BaseMessages.getString( PKG, "JobEntryFTP.JobStopped" ) );
          }

          if ( successConditionBroken ) {
            throw new Exception( BaseMessages.getString( PKG, "JobEntryFTP.SuccesConditionBroken", "" + NrErrors ) );
          }

          boolean getIt = true;

          String filename = ftpFile.getName();
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobEntryFTP.AnalysingFile", filename ) );
          }

          // We get only files
          if ( ftpFile.isDir() ) {
            // not a file..so let's skip it!
            getIt = false;
            if ( isDebug() ) {
              logDebug( BaseMessages.getString( PKG, "JobEntryFTP.SkippingNotAFile", filename ) );
            }
          }
          if ( getIt ) {
            try {
              // See if the file matches the regular expression!
              if ( pattern != null ) {
                Matcher matcher = pattern.matcher( filename );
                getIt = matcher.matches();
              }
              if ( getIt ) {
                downloadFile( ftpclient, filename, realMoveToFolder, parentJob, result );
              }
            } catch ( Exception e ) {
              // Update errors number
              updateErrors();
              logError( BaseMessages.getString( PKG, "JobFTP.UnexpectedError", e.toString() ) );
            }
          }
        } // end for
      }
    } catch ( Exception e ) {
      if ( !successConditionBroken && !exitjobentry ) {
        updateErrors();
      }
      logError( BaseMessages.getString( PKG, "JobEntryFTP.ErrorGetting", e.getMessage() ) );
    } finally {
      if ( ftpclient != null ) {
        try {
          ftpclient.quit();
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntryFTP.ErrorQuitting", e.getMessage() ) );
        }
      }
      FTPClient.clearSOCKS();
    }

    result.setNrErrors( NrErrors );
    result.setNrFilesRetrieved( NrfilesRetrieved );
    if ( getSuccessStatus() ) {
      result.setResult( true );
    }
    if ( exitjobentry ) {
      result.setResult( false );
    }
    displayResults();
    return result;
  }

  private void downloadFile( FTPClient ftpclient, String filename, String realMoveToFolder, Job parentJob,
    Result result ) throws Exception {
    String localFilename = filename;
    targetFilename = KettleVFS.getFilename( KettleVFS.getFileObject( returnTargetFilename( localFilename ) ) );

    if ( ( !onlyGettingNewFiles ) || ( onlyGettingNewFiles && needsDownload( targetFilename ) ) ) {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString(
          PKG, "JobEntryFTP.GettingFile", filename, environmentSubstitute( targetDirectory ) ) );
      }
      ftpclient.get( targetFilename, filename );

      // Update retrieved files
      updateRetrievedFiles();
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.GotFile", filename ) );
      }

      // Add filename to result filenames
      addFilenameToResultFilenames( result, parentJob, targetFilename );

      // Delete the file if this is needed!
      if ( remove ) {
        ftpclient.delete( filename );
        if ( isDetailed() ) {
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.DeletedFile", filename ) );
          }
        }
      } else {
        if ( movefiles ) {
          // Try to move file to destination folder ...
          ftpclient.rename( filename, realMoveToFolder + FILE_SEPARATOR + filename );

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.MovedFile", filename, realMoveToFolder ) );
          }
        }
      }
    }
  }

  /**
   * normalize / to \ and remove trailing slashes from a path
   *
   * @param path
   * @return normalized path
   * @throws Exception
   */
  public String normalizePath( String path ) throws Exception {

    String normalizedPath = path.replaceAll( "\\\\", FILE_SEPARATOR );
    while ( normalizedPath.endsWith( "\\" ) || normalizedPath.endsWith( FILE_SEPARATOR ) ) {
      normalizedPath = normalizedPath.substring( 0, normalizedPath.length() - 1 );
    }

    return normalizedPath;
  }

  private void addFilenameToResultFilenames( Result result, Job parentJob, String filename ) throws KettleException {
    if ( isaddresult ) {
      FileObject targetFile = null;
      try {
        targetFile = KettleVFS.getFileObject( filename, this );

        // Add to the result files...
        ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(), toString() );
        resultFile.setComment( BaseMessages.getString( PKG, "JobEntryFTP.Downloaded", serverName ) );
        result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.FileAddedToResult", filename ) );
        }
      } catch ( Exception e ) {
        throw new KettleException( e );
      } finally {
        try {
          targetFile.close();
          targetFile = null;
        } catch ( Exception e ) {
          // Ignore close errors
        }
      }
    }
  }

  private void displayResults() {
    if ( isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.Log.Info.FilesInError", "" + NrErrors ) );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.Log.Info.FilesRetrieved", "" + NrfilesRetrieved ) );
      logDetailed( "=======================================" );
    }
  }

  private boolean getSuccessStatus() {
    boolean retval = false;

    if ( ( NrErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( NrfilesRetrieved >= limitFiles && getSuccessCondition().equals(
        SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED ) )
      || ( NrErrors <= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  private void updateErrors() {
    NrErrors++;
    if ( checkIfSuccessConditionBroken() ) {
      // Success condition was broken
      successConditionBroken = true;
    }
  }

  private boolean checkIfSuccessConditionBroken() {
    boolean retval = false;
    if ( ( NrErrors > 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( NrErrors >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }
    return retval;
  }

  private void updateRetrievedFiles() {
    NrfilesRetrieved++;
  }

  /**
   * @param string
   *          the filename from the FTP server
   *
   * @return the calculated target filename
   */
  @VisibleForTesting
  String returnTargetFilename( String filename ) {
    String retval = null;
    // Replace possible environment variables...
    if ( filename != null ) {
      retval = filename;
    } else {
      return null;
    }

    int lenstring = retval.length();
    int lastindexOfDot = retval.lastIndexOf( "." );
    if ( lastindexOfDot == -1 ) {
      lastindexOfDot = lenstring;
    }

    String fileExtension = retval.substring( lastindexOfDot, lenstring );

    if ( isAddDateBeforeExtension() ) {
      retval = retval.substring( 0, lastindexOfDot );
    }

    SimpleDateFormat daf = new SimpleDateFormat();
    Date now = new Date();

    if ( SpecifyFormat && !Utils.isEmpty( date_time_format ) ) {
      daf.applyPattern( date_time_format );
      String dt = daf.format( now );
      retval += dt;
    } else {
      if ( adddate ) {
        daf.applyPattern( "yyyyMMdd" );
        String d = daf.format( now );
        retval += "_" + d;
      }
      if ( addtime ) {
        daf.applyPattern( "HHmmssSSS" );
        String t = daf.format( now );
        retval += "_" + t;
      }
    }

    if ( isAddDateBeforeExtension() ) {
      retval += fileExtension;
    }

    // Add foldername to filename
    retval = environmentSubstitute( targetDirectory ) + Const.FILE_SEPARATOR + retval;
    return retval;
  }

  public boolean evaluates() {
    return true;
  }

  /**
   * See if the filename on the FTP server needs downloading. The default is to check the presence of the file in the
   * target directory. If you need other functionality, extend this class and build it into a plugin.
   *
   * @param filename
   *          The local filename to check
   * @param remoteFileSize
   *          The size of the remote file
   * @return true if the file needs downloading
   */
  protected boolean needsDownload( String filename ) {
    boolean retval = false;

    File file = new File( filename );

    if ( !file.exists() ) {
      // Local file not exists!
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobEntryFTP.LocalFileNotExists" ), filename );
      }
      return true;
    } else {

      // Local file exists!
      if ( ifFileExists == ifFileExistsCreateUniq ) {
        if ( isDebug() ) {
          logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTP.LocalFileExists" ), filename );
          // Create file with unique name
        }

        int lenstring = targetFilename.length();
        int lastindexOfDot = targetFilename.lastIndexOf( '.' );
        if ( lastindexOfDot == -1 ) {
          lastindexOfDot = lenstring;
        }

        targetFilename =
          targetFilename.substring( 0, lastindexOfDot )
            + StringUtil.getFormattedDateTimeNow( true )
            + targetFilename.substring( lastindexOfDot, lenstring );

        return true;
      } else if ( ifFileExists == ifFileExistsFail ) {
        log.logError( BaseMessages.getString( PKG, "JobEntryFTP.LocalFileExists" ), filename );
        updateErrors();
      } else {
        if ( isDebug() ) {
          logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTP.LocalFileExists" ), filename );
        }
      }
    }

    return retval;
  }

  /**
   * @return the activeConnection
   */
  public boolean isActiveConnection() {
    return activeConnection;
  }

  /**
   * @param activeConnection
   *          the activeConnection to set
   */
  public void setActiveConnection( boolean passive ) {
    this.activeConnection = passive;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "serverName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate(
      this, "targetDirectory", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator(),
          JobEntryValidatorUtils.fileExistsValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "userName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "password", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
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

  /**
   * Hook in known parsers, and then those that have been specified in the variable ftp.file.parser.class.names
   *
   * @param ftpClient
   * @throws FTPException
   * @throws IOException
   */
  protected void hookInOtherParsers( FTPClient ftpClient ) throws FTPException, IOException {
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Hooking.Parsers" ) );
    }
    String system = ftpClient.system();
    MVSFileParser parser = new MVSFileParser( log );
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Created.MVS.Parser" ) );
    }
    FTPFileFactory factory = new FTPFileFactory( system );
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Created.Factory" ) );
    }
    factory.addParser( parser );
    ftpClient.setFTPFileFactory( factory );
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Get.Variable.Space" ) );
    }
    VariableSpace vs = this.getVariables();
    if ( vs != null ) {
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Getting.Other.Parsers" ) );
      }
      String otherParserNames = vs.getVariable( "ftp.file.parser.class.names" );
      if ( otherParserNames != null ) {
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Creating.Parsers" ) );
        }
        String[] parserClasses = otherParserNames.split( "|" );
        String cName = null;
        Class<?> clazz = null;
        Object parserInstance = null;
        for ( int i = 0; i < parserClasses.length; i++ ) {
          cName = parserClasses[i].trim();
          if ( cName.length() > 0 ) {
            try {
              clazz = Class.forName( cName );
              parserInstance = clazz.newInstance();
              if ( parserInstance instanceof FTPFileParser ) {
                if ( log.isDetailed() ) {
                  logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Created.Other.Parser", cName ) );
                }
                factory.addParser( (FTPFileParser) parserInstance );
              }
            } catch ( Exception ignored ) {
              if ( log.isDebug() ) {
                ignored.printStackTrace();
                logError( BaseMessages.getString( PKG, "JobEntryFTP.ERROR.Creating.Parser", cName ) );
              }
            }
          }
        }
      }
    }
  }
}
