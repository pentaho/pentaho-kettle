/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ftpsget;

import org.apache.commons.vfs2.FileObject;
import org.ftp4che.util.ftpfile.FTPFile;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This defines an FTPS job entry.
 *
 * @author Samatar
 * @since 08-03-2010
 *
 */
@JobEntry( id = "FTPS_GET", name = "JobEntry.FTPSGet.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.ftpsget",
        description = "JobEntry.FTPSGet.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.FileTransfer",
        image = "GFTPS.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+a+file+with+FTPS" )
public class JobEntryFTPSGet extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryFTPSGet.class; // for i18n purposes, needed by Translator2!!

  private String serverName;
  private String userName;
  private String password;
  private String FTPSDirectory;
  private String targetDirectory;
  private String wildcard;
  private boolean binaryMode;
  private int timeout;
  private boolean remove;
  private boolean onlyGettingNewFiles; /* Don't overwrite files */
  private boolean activeConnection;

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

  private int connectionType;

  public static final String[] FILE_EXISTS_ACTIONS =
    new String[] { "ifFileExistsSkip", "ifFileExistsCreateUniq", "ifFileExistsFail" };
  public static final int ifFileExistsSkip = 0;
  public static final int ifFileExistsCreateUniq = 1;
  public static final int ifFileExistsFail = 2;

  private int ifFileExists;

  public String SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED = "success_when_at_least";
  public String SUCCESS_IF_ERRORS_LESS = "success_if_errors_less";
  public String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  private String nr_limit;
  private String success_condition;

  long NrErrors = 0;
  long NrfilesRetrieved = 0;
  boolean successConditionBroken = false;
  int limitFiles = 0;

  String localFolder = null;
  String realMoveToFolder = null;

  static String FILE_SEPARATOR = "/";

  public JobEntryFTPSGet( String n ) {
    super( n, "" );
    nr_limit = "10";
    port = "21";
    success_condition = SUCCESS_IF_NO_ERRORS;
    ifFileExists = 0;

    serverName = null;
    movefiles = false;
    movetodirectory = null;
    adddate = false;
    addtime = false;
    SpecifyFormat = false;
    AddDateBeforeExtension = false;
    isaddresult = true;
    createmovefolder = false;
    connectionType = FTPSConnection.CONNECTION_TYPE_FTP;
  }

  public JobEntryFTPSGet() {
    this( "" );
  }

  public Object clone() {
    JobEntryFTPSGet je = (JobEntryFTPSGet) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 550 ); // 490 chars in spaces and tag names alone

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "FTPSdirectory", FTPSDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "targetdirectory", targetDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "binary", binaryMode ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "remove", remove ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "only_new", onlyGettingNewFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "active", activeConnection ) );
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

    retval.append( "      " ).append( XMLHandler.addTagValue( "ifFileExists", getFileExistsAction( ifFileExists ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_limit", nr_limit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection_type", FTPSConnection.getConnectionTypeCode( connectionType ) ) );
    if ( parentJobMeta != null ) {
      parentJobMeta.getNamedClusterEmbedManager().registerUrl( targetDirectory );
    }
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
      FTPSDirectory = XMLHandler.getTagValue( entrynode, "FTPSdirectory" );
      targetDirectory = XMLHandler.getTagValue( entrynode, "targetdirectory" );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      binaryMode = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "binary" ) );
      timeout = Const.toInt( XMLHandler.getTagValue( entrynode, "timeout" ), 10000 );
      remove = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "remove" ) );
      onlyGettingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "only_new" ) );
      activeConnection = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "active" ) );

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

      ifFileExists = getFileExistsIndex( XMLHandler.getTagValue( entrynode, "ifFileExists" ) );
      nr_limit = XMLHandler.getTagValue( entrynode, "nr_limit" );
      success_condition =
        Const.NVL( XMLHandler.getTagValue( entrynode, "success_condition" ), SUCCESS_IF_NO_ERRORS );
      connectionType =
        FTPSConnection.getConnectionTypeByCode( Const.NVL(
          XMLHandler.getTagValue( entrynode, "connection_type" ), "" ) );
    } catch ( Exception xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'FTPS' from XML node", xe );
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
      FTPSDirectory = rep.getJobEntryAttributeString( id_jobentry, "FTPSdirectory" );
      targetDirectory = rep.getJobEntryAttributeString( id_jobentry, "targetdirectory" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      binaryMode = rep.getJobEntryAttributeBoolean( id_jobentry, "binary" );
      timeout = (int) rep.getJobEntryAttributeInteger( id_jobentry, "timeout" );
      remove = rep.getJobEntryAttributeBoolean( id_jobentry, "remove" );
      onlyGettingNewFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "only_new" );
      activeConnection = rep.getJobEntryAttributeBoolean( id_jobentry, "active" );

      movefiles = rep.getJobEntryAttributeBoolean( id_jobentry, "movefiles" );
      movetodirectory = rep.getJobEntryAttributeString( id_jobentry, "movetodirectory" );

      adddate = rep.getJobEntryAttributeBoolean( id_jobentry, "adddate" );
      addtime = rep.getJobEntryAttributeBoolean( id_jobentry, "adddate" );
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

      ifFileExists = getFileExistsIndex( rep.getJobEntryAttributeString( id_jobentry, "ifFileExists" ) );
      nr_limit = rep.getJobEntryAttributeString( id_jobentry, "nr_limit" );
      success_condition =
        Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "success_condition" ), SUCCESS_IF_NO_ERRORS );
      connectionType =
        FTPSConnection.getConnectionTypeByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "connection_type" ), "" ) );
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'FTPS' from the repository for id_jobentry="
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
      rep.saveJobEntryAttribute( id_job, getObjectId(), "FTPSdirectory", FTPSDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targetdirectory", targetDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "binary", binaryMode );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "remove", remove );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "only_new", onlyGettingNewFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "active", activeConnection );

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
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ifFileExists", getFileExistsAction( ifFileExists ) );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "nr_limit", nr_limit );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "connection_type", FTPSConnection
        .getConnectionType( connectionType ) );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'FTPS' to the repository for id_job=" + id_job, dbe );
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
  public String getFTPSDirectory() {
    return FTPSDirectory;
  }

  /**
   * @param directory
   *          The directory to set.
   */
  public void setFTPSDirectory( String directory ) {
    this.FTPSDirectory = directory;
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
   * @return Returns the hostname of the FTPS-proxy.
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
   * @return Returns the password which is used to authenticate at the proxy.
   */
  public String getProxyPassword() {
    return proxyPassword;
  }

  /**
   * @param proxyPassword
   *          The password which is used to authenticate at the proxy.
   */
  public void setProxyPassword( String proxyPassword ) {
    this.proxyPassword = proxyPassword;
  }

  /**
   * @return Returns the port of the FTPS-proxy.
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * @param proxyPort
   *          The port of the FTPS-proxy.
   */
  public void setProxyPort( String proxyPort ) {
    this.proxyPort = proxyPort;
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

  public int getIfFileExists() {
    return ifFileExists;
  }

  public void setIfFileExists( int ifFileExists ) {
    this.ifFileExists = ifFileExists;
  }

  public static String getFileExistsAction( int actionId ) {
    if ( actionId < 0 || actionId >= FILE_EXISTS_ACTIONS.length ) {
      return FILE_EXISTS_ACTIONS[0];
    }
    return FILE_EXISTS_ACTIONS[actionId];
  }

  public static int getFileExistsIndex( String desc ) {
    int result = 0;
    if ( Utils.isEmpty( desc ) ) {
      return result;
    }
    for ( int i = 0; i < FILE_EXISTS_ACTIONS.length; i++ ) {
      if ( desc.equalsIgnoreCase( FILE_EXISTS_ACTIONS[i] ) ) {
        result = i;
        break;
      }
    }
    return result;
  }
  public Result execute( Result previousResult, int nr ) throws KettleException {
    // LogWriter log = LogWriter.getInstance();
    logBasic( BaseMessages.getString( PKG, "JobEntryFTPS.Started", serverName ) );

    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );
    NrErrors = 0;
    NrfilesRetrieved = 0;
    successConditionBroken = false;
    boolean exitjobentry = false;
    limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    // Here let's put some controls before stating the job
    if ( movefiles ) {
      if ( Utils.isEmpty( movetodirectory ) ) {
        logError( BaseMessages.getString( PKG, "JobEntryFTPS.MoveToFolderEmpty" ) );
        return result;
      }
    }

    localFolder = environmentSubstitute( targetDirectory );

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.Start" ) );
    }

    FTPSConnection connection = null;

    try {
      // Create FTPS client to host:port ...

      String realServername = environmentSubstitute( serverName );
      String realUsername = environmentSubstitute( userName );
      String realPassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
      int realPort = Const.toInt( environmentSubstitute( this.port ), 0 );

      connection = new FTPSConnection( getConnectionType(), realServername, realPort, realUsername, realPassword, this );

      this.buildFTPSConnection( connection );

      // Create move to folder if necessary
      if ( movefiles && !Utils.isEmpty( movetodirectory ) ) {
        realMoveToFolder = normalizePath( environmentSubstitute( movetodirectory ) );
        // Folder exists?
        boolean folderExist = connection.isDirectoryExists( realMoveToFolder );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.CheckMoveToFolder", realMoveToFolder ) );
        }

        if ( !folderExist ) {
          if ( createmovefolder ) {
            connection.createDirectory( realMoveToFolder );
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.MoveToFolderCreated", realMoveToFolder ) );
            }
          } else {
            logError( BaseMessages.getString( PKG, "JobEntryFTPS.MoveToFolderNotExist" ) );
            exitjobentry = true;
            NrErrors++;
          }
        }
      }
      if ( !exitjobentry ) {
        Pattern pattern = null;
        if ( !Utils.isEmpty( wildcard ) ) {
          String realWildcard = environmentSubstitute( wildcard );
          pattern = Pattern.compile( realWildcard );
        }

        if ( !getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) ) {
          limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );
        }

        // Get all the files in the current directory...
        downloadFiles( connection, connection.getWorkingDirectory(), pattern, result );
      }

    } catch ( Exception e ) {
      if ( !successConditionBroken && !exitjobentry ) {
        updateErrors();
      }
      logError( BaseMessages.getString( PKG, "JobEntryFTPS.ErrorGetting", e.getMessage() ) );
    } finally {
      if ( connection != null ) {
        try {
          connection.disconnect();
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntryFTPS.ErrorQuitting", e.getMessage() ) );
        }
      }
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

  private void downloadFiles( FTPSConnection connection, String folder, Pattern pattern, Result result ) throws KettleException {

    List<FTPFile> fileList = connection.getFileList( folder );
    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.FoundNFiles", fileList.size() ) );
    }

    for ( int i = 0; i < fileList.size(); i++ ) {

      if ( parentJob.isStopped() ) {
        throw new KettleException( BaseMessages.getString( PKG, "JobEntryFTPS.JobStopped" ) );
      }

      if ( successConditionBroken ) {
        throw new KettleException( BaseMessages.getString( PKG, "JobEntryFTPS.SuccesConditionBroken", NrErrors ) );
      }

      FTPFile file = fileList.get( i );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString(
          PKG, "JobEntryFTPS.AnalysingFile", file.getPath(), file.getName(), file.getMode(), file
            .getDate().toString(), file.getFileType() == 0 ? "File" : "Folder", String
            .valueOf( file.getSize() ) ) );
      }

      if ( !file.isDirectory() && !file.isLink() ) {
        // download file
        boolean getIt = true;
        if ( getIt ) {
          try {
            // See if the file matches the regular expression!
            if ( pattern != null ) {
              Matcher matcher = pattern.matcher( file.getName() );
              getIt = matcher.matches();
            }

            if ( getIt ) {
              // return local filename
              String localFilename = returnTargetFilename( file.getName() );

              if ( ( !onlyGettingNewFiles ) || ( onlyGettingNewFiles && needsDownload( localFilename ) ) ) {

                if ( isDetailed() ) {
                  logDetailed( BaseMessages.getString(
                    PKG, "JobEntryFTPS.GettingFile", file.getName(), targetDirectory ) );
                }

                // download file
                connection.downloadFile( file, returnTargetFilename( file.getName() ) );

                // Update retrieved files
                updateRetrievedFiles();

                if ( isDetailed() ) {
                  logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.GotFile", file.getName() ) );
                }

                // Add filename to result filenames
                addFilenameToResultFilenames( result, localFilename );

                // Delete the file if this is needed!
                if ( remove ) {
                  connection.deleteFile( file );

                  if ( isDetailed() ) {
                    if ( isDetailed() ) {
                      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.DeletedFile", file.getName() ) );
                    }
                  }
                } else {
                  if ( movefiles ) {
                    // Try to move file to destination folder ...
                    connection.moveToFolder( file, realMoveToFolder );

                    if ( isDetailed() ) {
                      logDetailed( BaseMessages.getString(
                        PKG, "JobEntryFTPS.MovedFile", file.getName(), realMoveToFolder ) );
                    }
                  }
                }
              }
            }
          } catch ( Exception e ) {
            // Update errors number
            updateErrors();
            logError( BaseMessages.getString( PKG, "JobFTPS.UnexpectedError", e.toString() ) );
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

  private void addFilenameToResultFilenames( Result result, String filename ) throws KettleException {
    if ( isaddresult ) {
      FileObject targetFile = null;
      try {
        targetFile = KettleVFS.getFileObject( filename, this );

        // Add to the result files...
        ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(), toString() );
        resultFile.setComment( BaseMessages.getString( PKG, "JobEntryFTPS.Downloaded", serverName ) );
        result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.FileAddedToResult", filename ) );
        }
      } catch ( Exception e ) {
        throw new KettleException( e );
      } finally {
        try {
          targetFile.close();
          targetFile = null;
        } catch ( Exception e ) {
          // Ignore errors
        }
      }
    }
  }

  private void displayResults() {
    if ( isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.Log.Info.FilesInError", "" + NrErrors ) );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.Log.Info.FilesRetrieved", "" + NrfilesRetrieved ) );
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
   *          the filename from the FTPS server
   *
   * @return the calculated target filename
   */
  private String returnTargetFilename( String filename ) {
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
      retval += retval.substring( lastindexOfDot, lenstring );
    }

    // Add foldername to filename
    retval = localFolder + Const.FILE_SEPARATOR + retval;
    return retval;
  }

  public boolean evaluates() {
    return true;
  }

  /**
   * See if the filename on the FTPS server needs downloading. The default is to check the presence of the file in the
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
        logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTPS.LocalFileNotExists" ), filename );
      }
      return true;
    } else {
      // Local file exists!
      if ( ifFileExists == ifFileExistsCreateUniq ) {
        if ( isDebug() ) {
          logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTPS.LocalFileExists" ), filename );
          // Create file with unique name
        }

        int lenstring = filename.length();
        int lastindexOfDot = filename.lastIndexOf( '.' );
        if ( lastindexOfDot == -1 ) {
          lastindexOfDot = lenstring;
        }

        filename =
          filename.substring( 0, lastindexOfDot )
            + StringUtil.getFormattedDateTimeNow( true ) + filename.substring( lastindexOfDot, lenstring );

        return true;
      } else if ( ifFileExists == ifFileExistsFail ) {
        logError( toString(), BaseMessages.getString( PKG, "JobEntryFTPS.LocalFileExists" ), filename );
        updateErrors();
      } else {
        if ( isDebug() ) {
          logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTPS.LocalFileExists" ), filename );
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
   * @return the conenction type
   */
  public int getConnectionType() {
    return connectionType;
  }

  /**
   * @param connectionType
   *          the connectionType to set
   */
  public void setConnectionType( int type ) {
    connectionType = type;
  }

  /**
   * @param activeConnection
   *          the activeConnection to set
   */
  public void setActiveConnection( boolean passive ) {
    this.activeConnection = passive;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( serverName ) ) {
      String realServerName = jobMeta.environmentSubstitute( serverName );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServerName, ResourceType.SERVER ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "serverName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate(
      this, "localDirectory", remarks, AndValidator.putValidators(
          JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.fileExistsValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "userName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "password", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "serverPort", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

  void buildFTPSConnection( FTPSConnection connection ) throws Exception {
    if ( !Utils.isEmpty( proxyHost ) ) {
      String realProxy_host = environmentSubstitute( proxyHost );
      String realProxy_username = environmentSubstitute( proxyUsername );
      String realProxy_password =
        Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( proxyPassword ) );

      connection.setProxyHost( realProxy_host );
      if ( !Utils.isEmpty( realProxy_username ) ) {
        connection.setProxyUser( realProxy_username );
      }
      if ( !Utils.isEmpty( realProxy_password ) ) {
        connection.setProxyPassword( realProxy_password );
      }
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.OpenedProxyConnectionOn", realProxy_host ) );
      }

      int proxyport = Const.toInt( environmentSubstitute( proxyPort ), 21 );
      if ( proxyport != 0 ) {
        connection.setProxyPort( proxyport );
      }
    } else {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.OpenedConnectionTo", connection.getHostName() ) );
      }
    }

    // set activeConnection connectmode ...
    if ( activeConnection ) {
      connection.setPassiveMode( false );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.SetActive" ) );
      }
    } else {
      connection.setPassiveMode( true );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.SetPassive" ) );
      }
    }

    // Set the timeout
    connection.setTimeOut( timeout );
    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.SetTimeout", String.valueOf( timeout ) ) );
    }

    // login to FTPS host ...
    connection.connect();

    // Set binary mode
    if ( isBinaryMode() ) {
      connection.setBinaryMode( true );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.SetBinary" ) );
      }
    }

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.LoggedIn", connection.getUserName() ) );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.WorkingDirectory", connection
        .getWorkingDirectory() ) );
    }

    // move to spool dir ...
    if ( !Utils.isEmpty( FTPSDirectory ) ) {
      String realFTPSDirectory = environmentSubstitute( FTPSDirectory );
      realFTPSDirectory = normalizePath( realFTPSDirectory );
      connection.changeDirectory( realFTPSDirectory );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPS.ChangedDir", realFTPSDirectory ) );
      }
    }
  }

}
