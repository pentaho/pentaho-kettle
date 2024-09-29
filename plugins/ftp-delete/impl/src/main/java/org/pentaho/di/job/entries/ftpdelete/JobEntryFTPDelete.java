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

package org.pentaho.di.job.entries.ftpdelete;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sftp.SFTPClient;
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
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;

/**
 * This defines an FTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
@JobEntry( id = "FTP_DELETE", name = "JobEntry.FTPDELETE.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.ftpdelete",
        description = "JobEntry.FTPDELETE.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.FileTransfer",
        image = "ui/images/FTPD.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/FTP+Delete" )
public class JobEntryFTPDelete extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryFTPDelete.class; // for i18n purposes, needed by Translator2!!

  private String serverName;

  private String port;

  private String userName;

  private String password;

  private String ftpDirectory;

  private String wildcard;

  private int timeout;

  private boolean activeConnection;

  private boolean publicpublickey;

  private String keyFilename;

  private String keyFilePass;

  private boolean useproxy;

  private String proxyHost;

  private String proxyPort; /* string to allow variable substitution */

  private String proxyUsername;

  private String proxyPassword;

  private String socksProxyHost;

  private String socksProxyPort;

  private String socksProxyUsername;

  private String socksProxyPassword;

  private String protocol;

  public static final String PROTOCOL_FTP = "FTP";

  public static final String PROTOCOL_FTPS = "FTPS";

  public static final String PROTOCOL_SFTP = "SFTP";

  public static final String PROTOCOL_SSH = "SSH";

  public String SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED = "success_when_at_least";

  public String SUCCESS_IF_ERRORS_LESS = "success_if_errors_less";

  public String SUCCESS_IF_ALL_FILES_DOWNLOADED = "success_is_all_files_downloaded";

  private String nr_limit_success;

  private String success_condition;

  private boolean copyprevious;

  private int FTPSConnectionType;

  long NrErrors = 0;

  long NrfilesDeleted = 0;

  boolean successConditionBroken = false;

  String targetFilename = null;

  int limitFiles = 0;

  FTPClient ftpclient = null;

  FTPSConnection ftpsclient = null;

  SFTPClient sftpclient = null;

  SFTPv3Client sshclient = null;

  public JobEntryFTPDelete( String n ) {
    super( n, "" );
    copyprevious = false;
    protocol = PROTOCOL_FTP;
    port = "21";
    socksProxyPort = "1080";
    nr_limit_success = "10";
    success_condition = SUCCESS_IF_ALL_FILES_DOWNLOADED;
    publicpublickey = false;
    keyFilename = null;
    keyFilePass = null;
    serverName = null;
    FTPSConnectionType = FTPSConnection.CONNECTION_TYPE_FTP;
  }

  public JobEntryFTPDelete() {
    this( "" );
  }

  public Object clone() {
    JobEntryFTPDelete je = (JobEntryFTPDelete) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 550 ); // 448 characters in spaces and tag names alone

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protocol", protocol ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( getPassword() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "ftpdirectory", ftpDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "active", activeConnection ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "useproxy", useproxy ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_host", proxyHost ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_port", proxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_username", proxyUsername ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "proxy_password", Encr.encryptPasswordIfNotUsingVariables( proxyPassword ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "publicpublickey", publicpublickey ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "keyfilename", keyFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "keyfilepass", keyFilePass ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_limit_success", nr_limit_success ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "copyprevious", copyprevious ) );
    retval.append( "      " ).append(
      XMLHandler
        .addTagValue( "ftps_connection_type", FTPSConnection.getConnectionTypeCode( FTPSConnectionType ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_host", socksProxyHost ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_port", socksProxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_username", socksProxyUsername ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "socksproxy_password", Encr
        .encryptPasswordIfNotUsingVariables( getSocksProxyPassword() ) ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      protocol = XMLHandler.getTagValue( entrynode, "protocol" );
      port = XMLHandler.getTagValue( entrynode, "port" );
      serverName = XMLHandler.getTagValue( entrynode, "servername" );
      userName = XMLHandler.getTagValue( entrynode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );
      ftpDirectory = XMLHandler.getTagValue( entrynode, "ftpdirectory" );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      timeout = Const.toInt( XMLHandler.getTagValue( entrynode, "timeout" ), 10000 );
      activeConnection = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "active" ) );

      useproxy = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "useproxy" ) );
      proxyHost = XMLHandler.getTagValue( entrynode, "proxy_host" );
      proxyPort = XMLHandler.getTagValue( entrynode, "proxy_port" );
      proxyUsername = XMLHandler.getTagValue( entrynode, "proxy_username" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "proxy_password" ) );

      publicpublickey = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "publicpublickey" ) );
      keyFilename = XMLHandler.getTagValue( entrynode, "keyfilename" );
      keyFilePass = XMLHandler.getTagValue( entrynode, "keyfilepass" );

      nr_limit_success = XMLHandler.getTagValue( entrynode, "nr_limit_success" );
      success_condition = XMLHandler.getTagValue( entrynode, "success_condition" );
      copyprevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "copyprevious" ) );
      FTPSConnectionType =
        FTPSConnection.getConnectionTypeByCode( Const.NVL( XMLHandler.getTagValue(
          entrynode, "ftps_connection_type" ), "" ) );
      socksProxyHost = XMLHandler.getTagValue( entrynode, "socksproxy_host" );
      socksProxyPort = XMLHandler.getTagValue( entrynode, "socksproxy_port" );
      socksProxyUsername = XMLHandler.getTagValue( entrynode, "socksproxy_username" );
      socksProxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "socksproxy_password" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'ftp' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      protocol = rep.getJobEntryAttributeString( id_jobentry, "protocol" );
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      serverName = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      userName = rep.getJobEntryAttributeString( id_jobentry, "username" );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );
      ftpDirectory = rep.getJobEntryAttributeString( id_jobentry, "ftpdirectory" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      timeout = (int) rep.getJobEntryAttributeInteger( id_jobentry, "timeout" );
      activeConnection = rep.getJobEntryAttributeBoolean( id_jobentry, "active" );

      copyprevious = rep.getJobEntryAttributeBoolean( id_jobentry, "copyprevious" );

      useproxy = rep.getJobEntryAttributeBoolean( id_jobentry, "useproxy" );
      proxyHost = rep.getJobEntryAttributeString( id_jobentry, "proxy_host" );
      proxyPort = rep.getJobEntryAttributeString( id_jobentry, "proxy_port" );
      proxyUsername = rep.getJobEntryAttributeString( id_jobentry, "proxy_username" );
      proxyPassword =
        Encr
          .decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "proxy_password" ) );

      publicpublickey = rep.getJobEntryAttributeBoolean( id_jobentry, "publicpublickey" );
      keyFilename = rep.getJobEntryAttributeString( id_jobentry, "keyfilename" );
      keyFilePass = rep.getJobEntryAttributeString( id_jobentry, "keyfilepass" );

      nr_limit_success = rep.getJobEntryAttributeString( id_jobentry, "nr_limit_success" );
      success_condition = rep.getJobEntryAttributeString( id_jobentry, "success_condition" );
      FTPSConnectionType =
        FTPSConnection.getConnectionTypeByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "ftps_connection_type" ), "" ) );

      socksProxyHost = rep.getJobEntryAttributeString( id_jobentry, "socksproxy_host" );
      socksProxyPort = rep.getJobEntryAttributeString( id_jobentry, "socksproxy_port" );
      socksProxyUsername = rep.getJobEntryAttributeString( id_jobentry, "socksproxy_username" );
      socksProxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(
          id_jobentry, "socksproxy_password" ) );
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'ftp' from the repository for id_jobentry="
        + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "protocol", protocol );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", userName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ftpdirectory", ftpDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "active", activeConnection );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "copyprevious", copyprevious );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "useproxy", useproxy );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "publicpublickey", publicpublickey );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "keyfilename", keyFilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "keyfilepass", keyFilePass );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_host", proxyHost );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_port", proxyPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_username", proxyUsername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_password", Encr
        .encryptPasswordIfNotUsingVariables( proxyPassword ) );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "nr_limit_success", nr_limit_success );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ftps_connection_type", FTPSConnection
        .getConnectionType( FTPSConnectionType ) );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_host", socksProxyHost );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_port", socksProxyPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_username", socksProxyUsername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_password", Encr
        .encryptPasswordIfNotUsingVariables( socksProxyPassword ) );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'ftp' to the repository for id_job=" + id_job, dbe );
    }
  }

  private boolean getStatus() {
    boolean retval = false;

    if ( ( NrErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_ALL_FILES_DOWNLOADED ) )
      || ( NrfilesDeleted >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED ) )
      || ( NrErrors <= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  public boolean isCopyPrevious() {
    return copyprevious;
  }

  public void setCopyPrevious( boolean copyprevious ) {
    this.copyprevious = copyprevious;
  }

  /**
   * @param publickey
   *          The publicpublickey to set.
   */
  public void setUsePublicKey( boolean publickey ) {
    this.publicpublickey = publickey;
  }

  /**
   * @return Returns the use public key.
   */
  public boolean isUsePublicKey() {
    return publicpublickey;
  }

  /**
   * @param keyfilename
   *          The key filename to set.
   */
  public void setKeyFilename( String keyfilename ) {
    this.keyFilename = keyfilename;
  }

  /**
   * @return Returns the key filename.
   */
  public String getKeyFilename() {
    return keyFilename;
  }

  /**
   * @param keyFilePass
   *          The key file pass to set.
   */
  public void setKeyFilePass( String keyFilePass ) {
    this.keyFilePass = keyFilePass;
  }

  /**
   * @return Returns the key file pass.
   */
  public String getKeyFilePass() {
    return keyFilePass;
  }

  /**
   * @return the connection type
   */
  public int getFTPSConnectionType() {
    return FTPSConnectionType;
  }

  /**
   * @param connectionType
   *          the connectionType to set
   */
  public void setFTPSConnectionType( int type ) {
    FTPSConnectionType = type;
  }

  public void setLimitSuccess( String nr_limit_successin ) {
    this.nr_limit_success = nr_limit_successin;
  }

  public String getLimitSuccess() {
    return nr_limit_success;
  }

  public void setSuccessCondition( String success_condition ) {
    this.success_condition = success_condition;
  }

  public String getSuccessCondition() {
    return success_condition;
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

  public void setProtocol( String protocol ) {
    this.protocol = protocol;
  }

  public String getProtocol() {
    return protocol;
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

  public boolean isUseProxy() {
    return useproxy;
  }

  public void setUseProxy( boolean useproxy ) {
    this.useproxy = useproxy;
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
   * @return Returns the port of the ftp.
   */
  public String getPort() {
    return port;
  }

  /**
   * @param proxyPort
   *          The port of the ftp.
   */
  public void setPort( String port ) {
    this.port = port;
  }

  /**
   * @return Returns the port of the ftp-proxy.
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * @param proxyPort
   *          The port of the ftp-proxy.
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

  /** Needed for the Vector coming from sshclient.ls() *
   */
  @SuppressWarnings( "unchecked" )
  public Result execute( Result previousResult, int nr ) {
    log.logBasic( BaseMessages.getString( PKG, "JobEntryFTPDelete.Started", serverName ) );
    RowMetaAndData resultRow = null;
    Result result = previousResult;
    List<RowMetaAndData> rows = result.getRows();

    result.setResult( false );
    NrErrors = 0;
    NrfilesDeleted = 0;
    successConditionBroken = false;
    HashSet<String> list_previous_files = new HashSet<>();

    // Here let's put some controls before stating the job

    String realservername = environmentSubstitute( serverName );
    String realserverpassword = Utils.resolvePassword( this, password );
    String realFtpDirectory = environmentSubstitute( ftpDirectory );

    int realserverport = Const.toInt( environmentSubstitute( port ), 0 );
    String realUsername = environmentSubstitute( userName );
    String realPassword = Utils.resolvePassword( this, password );
    String realproxyhost = environmentSubstitute( proxyHost );
    String realproxyusername = environmentSubstitute( proxyUsername );
    String realproxypassword = Utils.resolvePassword( this, proxyPassword );
    int realproxyport = Const.toInt( environmentSubstitute( proxyPort ), 0 );
    String realkeyFilename = environmentSubstitute( keyFilename );
    String realkeyPass = environmentSubstitute( keyFilePass );

    // PDI The following is used to apply a path for SSH because the SFTPv3Client doesn't let us specify/change dirs
    String sourceFolder = "";

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.Start" ) );
    }

    if ( copyprevious && rows.size() == 0 ) {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.ArgsFromPreviousNothing" ) );
      }
      result.setResult( true );
      return result;
    }

    try {

      // Get all the files in the current directory...
      String[] filelist = null;
      if ( copyprevious ) {
        realFtpDirectory = "";
      }
      if ( protocol.equals( PROTOCOL_FTP ) ) {
        // If socks proxy server was provided
        if ( !Utils.isEmpty( socksProxyHost ) ) {
          if ( !Utils.isEmpty( socksProxyPort ) ) {
            FTPClient.initSOCKS( environmentSubstitute( socksProxyPort ), environmentSubstitute( socksProxyHost ) );
          } else {
            throw new FTPException( BaseMessages.getString(
              PKG, "JobEntryFTPDelete.SocksProxy.PortMissingException", environmentSubstitute( socksProxyHost ),
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
              PKG, "JobEntryFTPDelete.SocksProxy.IncompleteCredentials",
              environmentSubstitute( socksProxyHost ), getName() ) );
          }
        }

        if ( copyprevious ) {
          realFtpDirectory = "";
        }

        // establish the connection
        FTPConnect(
          realservername, realUsername, realPassword, realserverport, realFtpDirectory, realproxyhost,
          realproxyusername, realproxypassword, realproxyport, timeout );

        filelist = ftpclient.dir();
      } else if ( protocol.equals( PROTOCOL_FTPS ) ) {
        // establish the secure connection
        FTPSConnect( realservername, realUsername, realserverport, realPassword, realFtpDirectory, timeout );
        // Get all the files in the current directory...
        filelist = ftpsclient.getFileNames();
      } else if ( protocol.equals( PROTOCOL_SFTP ) ) {
        // establish the secure connection
        SFTPConnect( realservername, realUsername, realserverport, realPassword, realFtpDirectory );

        // Get all the files in the current directory...
        filelist = sftpclient.dir();
      } else if ( protocol.equals( PROTOCOL_SSH ) ) {
        // establish the secure connection
        SSHConnect(
          realservername, realserverpassword, realserverport, realUsername, realPassword, realproxyhost,
          realproxyusername, realproxypassword, realproxyport, realkeyFilename, realkeyPass );

        sourceFolder = ".";
        if ( realFtpDirectory != null ) {
          sourceFolder = realFtpDirectory + "/";
        } else {
          sourceFolder = "./";
        }

        // NOTE: Source of the unchecked warning suppression for the declaration of this method.
        Vector<SFTPv3DirectoryEntry> vfilelist = sshclient.ls( sourceFolder );
        if ( vfilelist != null ) {
          // Make one pass through the vfilelist to get an accurate count
          // Using the two-pass method with arrays is faster than using ArrayList
          int fileCount = 0;
          Iterator<SFTPv3DirectoryEntry> iterator = vfilelist.iterator();
          while ( iterator.hasNext() ) {
            SFTPv3DirectoryEntry dirEntry = iterator.next();

            if ( dirEntry != null
              && !dirEntry.filename.equals( "." ) && !dirEntry.filename.equals( ".." )
              && !isDirectory( sshclient, sourceFolder + dirEntry.filename ) ) {
              fileCount++;
            }
          }

          // Now that we have the correct count, create and fill in the array
          filelist = new String[fileCount];
          iterator = vfilelist.iterator();
          int i = 0;
          while ( iterator.hasNext() ) {
            SFTPv3DirectoryEntry dirEntry = iterator.next();

            if ( dirEntry != null
              && !dirEntry.filename.equals( "." ) && !dirEntry.filename.equals( ".." )
              && !isDirectory( sshclient, sourceFolder + dirEntry.filename ) ) {
              filelist[i] = dirEntry.filename;
              i++;
            }
          }
        }
      }

      if ( isDetailed() ) {
        logDetailed( "JobEntryFTPDelete.FoundNFiles", String.valueOf( filelist.length ) );
      }
      int found = filelist == null ? 0 : filelist.length;
      if ( found == 0 ) {
        result.setResult( true );
        return result;
      }

      Pattern pattern = null;
      if ( copyprevious ) {
        // Copy the input row to the (command line) arguments
        for ( int iteration = 0; iteration < rows.size(); iteration++ ) {
          resultRow = rows.get( iteration );

          // Get file names
          String file_previous = resultRow.getString( 0, null );
          if ( !Utils.isEmpty( file_previous ) ) {
            list_previous_files.add( file_previous );
          }
        }
      } else {
        if ( !Utils.isEmpty( wildcard ) ) {
          String realWildcard = environmentSubstitute( wildcard );
          pattern = Pattern.compile( realWildcard );

        }
      }

      if ( !getSuccessCondition().equals( SUCCESS_IF_ALL_FILES_DOWNLOADED ) ) {
        limitFiles = Const.toInt( environmentSubstitute( getLimitSuccess() ), 10 );
      }

      // Get the files in the list...
      for ( int i = 0; i < filelist.length && !parentJob.isStopped(); i++ ) {
        if ( successConditionBroken ) {
          throw new Exception( BaseMessages.getString( PKG, "JobEntryFTPDelete.SuccesConditionBroken" ) );
        }

        boolean getIt = false;

        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEntryFTPDelete.AnalysingFile", filelist[i] ) );
        }

        try {
          // First see if the file matches the regular expression!
          if ( copyprevious ) {
            if ( list_previous_files.contains( filelist[i] ) ) {
              getIt = true;
            }
          } else {
            if ( pattern != null ) {
              Matcher matcher = pattern.matcher( filelist[i] );
              getIt = matcher.matches();
            }
          }

          if ( getIt ) {
            // Delete file
            if ( protocol.equals( PROTOCOL_FTP ) ) {
              ftpclient.delete( filelist[i] );
            }
            if ( protocol.equals( PROTOCOL_FTPS ) ) {
              // System.out.println( "---------------" + filelist[i] );
              ftpsclient.deleteFile( filelist[i] );
            } else if ( protocol.equals( PROTOCOL_SFTP ) ) {
              sftpclient.delete( filelist[i] );
            } else if ( protocol.equals( PROTOCOL_SSH ) ) {
              sshclient.rm( sourceFolder + filelist[i] );
            }
            if ( isDetailed() ) {
              logDetailed( "JobEntryFTPDelete.RemotefileDeleted", filelist[i] );
            }
            updateDeletedFiles();
          }
        } catch ( Exception e ) {
          // Update errors number
          updateErrors();
          logError( BaseMessages.getString( PKG, "JobFTP.UnexpectedError", e.getMessage() ) );

          if ( successConditionBroken ) {
            throw new Exception( BaseMessages.getString( PKG, "JobEntryFTPDelete.SuccesConditionBroken" ) );
          }
        }
      } // end for
    } catch ( Exception e ) {
      updateErrors();
      logError( BaseMessages.getString( PKG, "JobEntryFTPDelete.ErrorGetting", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
    } finally {
      if ( ftpclient != null && ftpclient.connected() ) {
        try {
          ftpclient.quit();
          ftpclient = null;
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntryFTPDelete.ErrorQuitting", e.getMessage() ) );
        }
      }
      if ( ftpsclient != null ) {
        try {
          ftpsclient.disconnect();
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntryFTPDelete.ErrorQuitting", e.getMessage() ) );
        }
      }
      if ( sftpclient != null ) {
        try {
          sftpclient.disconnect();
          sftpclient = null;
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntryFTPDelete.ErrorQuitting", e.getMessage() ) );
        }
      }
      if ( sshclient != null ) {
        try {
          sshclient.close();
          sshclient = null;
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntryFTPDelete.ErrorQuitting", e.getMessage() ) );
        }
      }

      FTPClient.clearSOCKS();
    }

    result.setResult( !successConditionBroken );
    result.setNrFilesRetrieved( NrfilesDeleted );
    result.setNrErrors( NrErrors );

    return result;
  }

  /**
   * Checks if file is a directory
   *
   * @param sftpClient
   * @param filename
   * @return true, if filename is a directory
   */
  public boolean isDirectory( SFTPv3Client sftpClient, String filename ) {
    try {
      return sftpClient.stat( filename ).isDirectory();
    } catch ( Exception e ) {
      // Ignore FTP errors
    }
    return false;
  }

  private void SSHConnect( String realservername, String realserverpassword, int realserverport,
    String realUsername, String realPassword, String realproxyhost, String realproxyusername,
    String realproxypassword, int realproxyport, String realkeyFilename, String realkeyPass ) throws Exception {

    /* Create a connection instance */

    Connection conn = new Connection( realservername, realserverport );

    /* We want to connect through a HTTP proxy */
    if ( useproxy ) {
      conn.setProxyData( new HTTPProxyData( realproxyhost, realproxyport ) );

      /* Now connect */
      // if the proxy requires basic authentication:
      if ( !Utils.isEmpty( realproxyusername ) || !Utils.isEmpty( realproxypassword ) ) {
        conn
          .setProxyData( new HTTPProxyData( realproxyhost, realproxyport, realproxyusername, realproxypassword ) );
      }
    }

    if ( timeout > 0 ) {
      // Use timeout
      conn.connect( null, 0, timeout * 1000 );

    } else {
      // Cache Host Key
      conn.connect();
    }

    // Authenticate

    boolean isAuthenticated = false;
    if ( publicpublickey ) {
      isAuthenticated = conn.authenticateWithPublicKey( realUsername, new File( realkeyFilename ), realkeyPass );
    } else {
      isAuthenticated = conn.authenticateWithPassword( realUsername, realserverpassword );
    }

    if ( !isAuthenticated ) {
      throw new Exception( "Can not connect to " );
    }

    sshclient = new SFTPv3Client( conn );

  }

  private void SFTPConnect( String realservername, String realusername, int realport, String realpassword,
    String realFTPDirectory ) throws Exception {
    // Create sftp client to host ...
    sftpclient = new SFTPClient( InetAddress.getByName( realservername ), realport, realusername );

    // login to ftp host ...
    sftpclient.login( realpassword );

    // move to spool dir ...
    if ( !Utils.isEmpty( realFTPDirectory ) ) {
      sftpclient.chdir( realFTPDirectory );
      if ( isDetailed() ) {
        logDetailed( "Changed to directory [" + realFTPDirectory + "]" );
      }
    }

  }

  private void FTPSConnect( String realservername, String realusername, int realport, String realpassword,
    String realFTPDirectory, int realtimeout ) throws Exception {
    // Create ftps client to host ...
    ftpsclient =
      new FTPSConnection( getFTPSConnectionType(), realservername, realport, realusername, realpassword );

    if ( !Utils.isEmpty( proxyHost ) ) {
      String realProxy_host = environmentSubstitute( proxyHost );
      String realProxy_username = environmentSubstitute( proxyUsername );
      String realProxy_password = Utils.resolvePassword( this, proxyPassword );

      ftpsclient.setProxyHost( realProxy_host );
      if ( !Utils.isEmpty( realProxy_username ) ) {
        ftpsclient.setProxyUser( realProxy_username );
      }
      if ( !Utils.isEmpty( realProxy_password ) ) {
        ftpsclient.setProxyPassword( realProxy_password );
      }
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.OpenedProxyConnectionOn", realProxy_host ) );
      }

      int proxyport = Const.toInt( environmentSubstitute( proxyPort ), 21 );
      if ( proxyport != 0 ) {
        ftpsclient.setProxyPort( proxyport );
      }
    } else {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.OpenedConnectionTo", realservername ) );
      }
    }

    // set activeConnection connectmode ...
    if ( activeConnection ) {
      ftpsclient.setPassiveMode( false );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.SetActive" ) );
      }
    } else {
      ftpsclient.setPassiveMode( true );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.SetPassive" ) );
      }
    }

    // Set the timeout
    ftpsclient.setTimeOut( realtimeout );
    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.SetTimeout", String.valueOf( realtimeout ) ) );
    }

    // now connect
    ftpsclient.connect();

    // move to spool dir ...
    if ( !Utils.isEmpty( realFTPDirectory ) ) {
      ftpsclient.changeDirectory( realFTPDirectory );
      if ( isDetailed() ) {
        logDetailed( "Changed to directory [" + realFTPDirectory + "]" );
      }
    }
  }

  private void FTPConnect( String realServername, String realusername, String realpassword, int realport,
    String realFtpDirectory, String realProxyhost, String realproxyusername, String realproxypassword,
    int realproxyport, int realtimeout ) throws Exception {

    // Create ftp client to host:port ...
    ftpclient = new FTPClient();
    ftpclient.setControlEncoding("UTF-8");
    ftpclient.setRemoteAddr( InetAddress.getByName( realServername ) );
    if ( realport != 0 ) {
      ftpclient.setRemotePort( realport );
    }

    if ( !Utils.isEmpty( realProxyhost ) ) {
      ftpclient.setRemoteAddr( InetAddress.getByName( realProxyhost ) );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.OpenedProxyConnectionOn", realProxyhost ) );
      }

      // FIXME: Proper default port for proxy
      if ( realproxyport != 0 ) {
        ftpclient.setRemotePort( realproxyport );
      }
    } else {
      ftpclient.setRemoteAddr( InetAddress.getByName( realServername ) );

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.OpenedConnectionTo", realServername ) );
      }
    }

    // set activeConnection connectmode ...
    if ( activeConnection ) {
      ftpclient.setConnectMode( FTPConnectMode.ACTIVE );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.SetActive" ) );
      }
    } else {
      ftpclient.setConnectMode( FTPConnectMode.PASV );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.SetPassive" ) );
      }
    }

    // Set the timeout
    ftpclient.setTimeout( realtimeout );
    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.SetTimeout", String.valueOf( realtimeout ) ) );
    }

    // login to ftp host ...
    ftpclient.connect();

    String realUsername =
      realusername
        + ( !Utils.isEmpty( realProxyhost ) ? "@" + realServername : "" )
        + ( !Utils.isEmpty( realproxyusername ) ? " " + realproxyusername : "" );

    String realPassword = realpassword + ( !Utils.isEmpty( realproxypassword ) ? " " + realproxypassword : "" );

    ftpclient.login( realUsername, realPassword );
    // Remove password from logging, you don't know where it ends up.
    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.LoggedIn", realUsername ) );
    }

    // move to spool dir ...
    if ( !Utils.isEmpty( realFtpDirectory ) ) {
      ftpclient.chdir( realFtpDirectory );
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPDelete.ChangedDir", realFtpDirectory ) );
      }
    }

  }

  private void updateErrors() {
    NrErrors++;
    if ( !getStatus() ) {
      // Success condition was broken
      successConditionBroken = true;
    }
  }

  private void updateDeletedFiles() {
    NrfilesDeleted++;
  }

  public boolean evaluates() {
    return true;
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
    JobEntryValidatorUtils.andValidator().validate( this, "serverName", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate(
      this, "targetDirectory", remarks, AndValidator.putValidators(
          JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.fileExistsValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "userName", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "password", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
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
   * @return Socks proxy host
   */
  public String getSocksProxyHost() {
    return this.socksProxyHost;
  }

  /**
   * @return Socks proxy port
   */
  public String getSocksProxyPort() {
    return this.socksProxyPort;
  }

  /**
   * @return Socks proxy username
   */
  public String getSocksProxyUsername() {
    return this.socksProxyUsername;
  }

  /**
   * @return Socks proxy username
   */
  public String getSocksProxyPassword() {
    return this.socksProxyPassword;
  }

  /**
   * @return Sets socks proxy host
   */
  public void setSocksProxyHost( String socksProxyHost ) {
    this.socksProxyHost = socksProxyHost;
  }

  /**
   * @return Sets socks proxy port
   */
  public void setSocksProxyPort( String socksProxyPort ) {
    this.socksProxyPort = socksProxyPort;
  }

  /**
   * @return Sets socks proxy username
   */
  public void setSocksProxyUsername( String socksProxyUsername ) {
    this.socksProxyUsername = socksProxyUsername;
  }

  /**
   * @return Sets socks proxy username
   */
  public void setSocksProxyPassword( String socksProxyPassword ) {
    this.socksProxyPassword = socksProxyPassword;
  }
}
