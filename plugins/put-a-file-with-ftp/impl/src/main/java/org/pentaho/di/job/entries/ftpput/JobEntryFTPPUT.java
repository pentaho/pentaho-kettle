/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.ftpput;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftp.MVSFileParser;
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
import com.enterprisedt.net.ftp.FTPFileFactory;
import com.enterprisedt.net.ftp.FTPFileParser;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This defines an FTP put job entry.
 *
 * @author Samatar
 * @since 15-09-2007
 */

@JobEntry( id = "FTP_PUT", name = "JobEntry.FTPPUT.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.ftpput",
        description = "JobEntry.FTPPUT.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.FileTransfer",
        image = "ui/images/PFTP.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Put+a+file+with+FTP" )
public class JobEntryFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface {
  public static final String STRING_Y = "Y";
  private static Class<?> PKG = JobEntryFTPPUT.class; // for i18n purposes, needed by Translator2!!

  private static final String XML_TAG_WILDCARD = "wildcard";
  private static final String XML_TAG_BINARY = "binary";
  private static final String XML_TAG_TIMEOUT = "timeout";
  private static final String XML_TAG_REMOVE = "remove";
  private static final String XML_TAG_ONLY_NEW = "only_new";
  private static final String XML_TAG_ACTIVE = "active";
  private static final String CONTROL_ENCODING = "control_encoding";
  private static final String XML_TAG_PROXY_HOST = "proxy_host";
  private static final String XML_TAG_PROXY_PORT = "proxy_port";
  private static final String XML_TAG_PROXY_USERNAME = "proxy_username";
  private static final String XML_TAG_PROXY_PASSWORD = "proxy_password";
  private static final String XML_TAG_SOCKSPROXY_HOST = "socksproxy_host";
  private static final String XML_TAG_SOCKSPROXY_PORT = "socksproxy_port";
  private static final String XML_TAG_SOCKSPROXY_USERNAME = "socksproxy_username";
  private static final String XML_TAG_SOCKSPROXY_PASSWORD = "socksproxy_password";
  private static final String XML_TAG_REMOTE_DIRECTORY = "remoteDirectory";
  private static final String XML_TAG_LOCAL_DIRECTORY = "localDirectory";
  private static final String XML_TAG_SERVERNAME = "servername";
  private static final String XML_TAG_SERVERPORT = "serverport";
  private static final String XML_TAG_USERNAME = "username";
  private static final String XML_TAG_PASSWORD = "password";
  private static final String XML_TAG_INDENT2 = "      ";

  public static final int FTP_DEFAULT_PORT = 21;

  private String serverName;
  private String serverPort;
  private String userName;
  private String password;
  private String remoteDirectory;
  private String localDirectory;
  private String wildcard;
  private boolean binaryMode;
  private int timeout;
  private boolean remove;
  private boolean onlyPuttingNewFiles; /* Don't overwrite files */
  private boolean activeConnection;
  private String controlEncoding; /* how to convert list of filenames e.g. */
  private String proxyHost;

  private String proxyPort; /* string to allow variable substitution */

  private String proxyUsername;

  private String proxyPassword;

  private String socksProxyHost;
  private String socksProxyPort;
  private String socksProxyUsername;
  private String socksProxyPassword;

  /**
   * Implicit encoding used before PDI v2.4.1
   */
  private static final String LEGACY_CONTROL_ENCODING = "US-ASCII";

  /**
   * Default encoding when making a new ftp job entry instance.
   */
  private static final String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

  public JobEntryFTPPUT( String n ) {
    super( n, "" );
    serverName = null;
    serverPort = "21";
    socksProxyPort = "1080";
    remoteDirectory = null;
    localDirectory = null;
    setControlEncoding( DEFAULT_CONTROL_ENCODING );
  }

  public JobEntryFTPPUT() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntryFTPPUT je = (JobEntryFTPPUT) super.clone();
    return je;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 450 ); // 365 characters in spaces and tag names alone

    retval.append( super.getXML() );

    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_SERVERNAME, serverName ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_SERVERPORT, serverPort ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_USERNAME, userName ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_PASSWORD,
      Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_REMOTE_DIRECTORY, remoteDirectory ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_LOCAL_DIRECTORY, localDirectory ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_WILDCARD, wildcard ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_BINARY, binaryMode ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_TIMEOUT, timeout ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_REMOVE, remove ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_ONLY_NEW, onlyPuttingNewFiles ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_ACTIVE, activeConnection ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( CONTROL_ENCODING, controlEncoding ) );

    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_PROXY_HOST, proxyHost ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_PROXY_PORT, proxyPort ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_PROXY_USERNAME, proxyUsername ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_PROXY_PASSWORD,
      Encr.encryptPasswordIfNotUsingVariables( proxyPassword ) ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_SOCKSPROXY_HOST, socksProxyHost ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_SOCKSPROXY_PORT, socksProxyPort ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_SOCKSPROXY_USERNAME, socksProxyUsername ) );
    retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( XML_TAG_SOCKSPROXY_PASSWORD,
      Encr.encryptPasswordIfNotUsingVariables( socksProxyPassword ) ) );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
                       Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      serverName = XMLHandler.getTagValue( entrynode, XML_TAG_SERVERNAME );
      serverPort = XMLHandler.getTagValue( entrynode, XML_TAG_SERVERPORT );
      userName = XMLHandler.getTagValue( entrynode, XML_TAG_USERNAME );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, XML_TAG_PASSWORD ) );
      remoteDirectory = XMLHandler.getTagValue( entrynode, XML_TAG_REMOTE_DIRECTORY );
      localDirectory = XMLHandler.getTagValue( entrynode, XML_TAG_LOCAL_DIRECTORY );
      wildcard = XMLHandler.getTagValue( entrynode, XML_TAG_WILDCARD );
      binaryMode = STRING_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, XML_TAG_BINARY ) );
      timeout = Const.toInt( XMLHandler.getTagValue( entrynode, XML_TAG_TIMEOUT ), 10000 );
      remove = STRING_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, XML_TAG_REMOVE ) );
      onlyPuttingNewFiles = STRING_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, XML_TAG_ONLY_NEW ) );
      activeConnection = STRING_Y.equalsIgnoreCase( XMLHandler.getTagValue( entrynode, XML_TAG_ACTIVE ) );
      controlEncoding = XMLHandler.getTagValue( entrynode, CONTROL_ENCODING );

      proxyHost = XMLHandler.getTagValue( entrynode, XML_TAG_PROXY_HOST );
      proxyPort = XMLHandler.getTagValue( entrynode, XML_TAG_PROXY_PORT );
      proxyUsername = XMLHandler.getTagValue( entrynode, XML_TAG_PROXY_USERNAME );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, XML_TAG_PROXY_PASSWORD ) );
      socksProxyHost = XMLHandler.getTagValue( entrynode, XML_TAG_SOCKSPROXY_HOST );
      socksProxyPort = XMLHandler.getTagValue( entrynode, XML_TAG_SOCKSPROXY_PORT );
      socksProxyUsername = XMLHandler.getTagValue( entrynode, XML_TAG_SOCKSPROXY_USERNAME );
      socksProxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, XML_TAG_SOCKSPROXY_PASSWORD ) );

      if ( controlEncoding == null ) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the the encoding used before v 2.4.0
        controlEncoding = LEGACY_CONTROL_ENCODING;
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobFTPPUT.Log.UnableToLoadFromXml" ), xe );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId jobEntryId, List<DatabaseMeta> databases,
                       List<SlaveServer> slaveServers ) throws KettleException {
    try {
      serverName = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_SERVERNAME );
      serverPort = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_SERVERPORT );
      userName = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_USERNAME );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( jobEntryId, XML_TAG_PASSWORD ) );
      remoteDirectory = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_REMOTE_DIRECTORY );
      localDirectory = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_LOCAL_DIRECTORY );
      wildcard = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_WILDCARD );
      binaryMode = rep.getJobEntryAttributeBoolean( jobEntryId, XML_TAG_BINARY );
      timeout = (int) rep.getJobEntryAttributeInteger( jobEntryId, XML_TAG_TIMEOUT );
      remove = rep.getJobEntryAttributeBoolean( jobEntryId, XML_TAG_REMOVE );
      onlyPuttingNewFiles = rep.getJobEntryAttributeBoolean( jobEntryId, XML_TAG_ONLY_NEW );
      activeConnection = rep.getJobEntryAttributeBoolean( jobEntryId, XML_TAG_ACTIVE );
      controlEncoding = rep.getJobEntryAttributeString( jobEntryId, CONTROL_ENCODING );
      if ( controlEncoding == null ) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the encoding used before v 2.4.0
        controlEncoding = LEGACY_CONTROL_ENCODING;
      }

      proxyHost = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_PROXY_HOST );
      proxyPort = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_PROXY_PORT );
      proxyUsername = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_PROXY_USERNAME );
      proxyPassword =
        Encr
          .decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( jobEntryId, XML_TAG_PROXY_PASSWORD ) );
      socksProxyHost = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_SOCKSPROXY_HOST );
      socksProxyPort = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_SOCKSPROXY_PORT );
      socksProxyUsername = rep.getJobEntryAttributeString( jobEntryId, XML_TAG_SOCKSPROXY_USERNAME );
      socksProxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(
          jobEntryId, XML_TAG_SOCKSPROXY_PASSWORD ) );

    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPPUT.UnableToLoadFromRepo", String
        .valueOf( jobEntryId ) ), dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idJob ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_SERVERNAME, serverName );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_SERVERPORT, serverPort );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_USERNAME, userName );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_REMOTE_DIRECTORY, remoteDirectory );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_LOCAL_DIRECTORY, localDirectory );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_WILDCARD, wildcard );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_BINARY, binaryMode );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_TIMEOUT, timeout );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_REMOVE, remove );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_ONLY_NEW, onlyPuttingNewFiles );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_ACTIVE, activeConnection );
      rep.saveJobEntryAttribute( idJob, getObjectId(), CONTROL_ENCODING, controlEncoding );

      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_PROXY_HOST, proxyHost );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_PROXY_PORT, proxyPort );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_PROXY_USERNAME, proxyUsername );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_PROXY_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( proxyPassword ) );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_SOCKSPROXY_HOST, socksProxyHost );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_SOCKSPROXY_PORT, socksProxyPort );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_SOCKSPROXY_USERNAME, socksProxyUsername );
      rep.saveJobEntryAttribute( idJob, getObjectId(), XML_TAG_SOCKSPROXY_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( socksProxyPassword ) );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPPUT.UnableToSaveToRepo", String
        .valueOf( idJob ) ), dbe );
    }
  }

  /**
   * @return Returns the binaryMode.
   */
  public boolean isBinaryMode() {
    return binaryMode;
  }

  /**
   * @param binaryMode The binaryMode to set.
   */
  public void setBinaryMode( boolean binaryMode ) {
    this.binaryMode = binaryMode;
  }

  /**
   * @param timeout The timeout to set.
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
   * @return Returns the onlyGettingNewFiles.
   */
  public boolean isOnlyPuttingNewFiles() {
    return onlyPuttingNewFiles;
  }

  /**
   * @param onlyPuttingNewFiles Only transfer new files to the remote host
   */
  public void setOnlyPuttingNewFiles( boolean onlyPuttingNewFiles ) {
    this.onlyPuttingNewFiles = onlyPuttingNewFiles;
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
   * @param encoding The encoding to be used.
   */
  public void setControlEncoding( String encoding ) {
    this.controlEncoding = encoding;
  }

  /**
   * @return Returns the remoteDirectory.
   */
  public String getRemoteDirectory() {
    return remoteDirectory;
  }

  /**
   * @param directory The remoteDirectory to set.
   */
  public void setRemoteDirectory( String directory ) {
    this.remoteDirectory = directory;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password The password to set.
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
   * @param serverName The serverName to set.
   */
  public void setServerName( String serverName ) {
    this.serverName = serverName;
  }

  /**
   * @return Returns the userName.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName The userName to set.
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
   * @param wildcard The wildcard to set.
   */
  public void setWildcard( String wildcard ) {
    this.wildcard = wildcard;
  }

  /**
   * @return Returns the localDirectory.
   */
  public String getLocalDirectory() {
    return localDirectory;
  }

  /**
   * @param directory The localDirectory to set.
   */
  public void setLocalDirectory( String directory ) {
    this.localDirectory = directory;
  }

  /**
   * @param remove The remove to set.
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

  public String getServerPort() {
    return serverPort;
  }

  public void setServerPort( String serverPort ) {
    this.serverPort = serverPort;
  }

  /**
   * @return the activeConnection
   */
  public boolean isActiveConnection() {
    return activeConnection;
  }

  /**
   * @param activeConnection set to true to get an active FTP connection
   */
  public void setActiveConnection( boolean activeConnection ) {
    this.activeConnection = activeConnection;
  }

  /**
   * @return Returns the hostname of the ftp-proxy.
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * @param proxyHost The hostname of the proxy.
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
   * @param proxyPassword The password which is used to authenticate at the proxy.
   */
  public void setProxyPassword( String proxyPassword ) {
    this.proxyPassword = proxyPassword;
  }

  /**
   * @return Returns the port of the ftp-proxy.
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * @param proxyPort The port of the ftp-proxy.
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
   * @param socksProxyHost The socks proxy host to set
   */
  public void setSocksProxyHost( String socksProxyHost ) {
    this.socksProxyHost = socksProxyHost;
  }

  /**
   * @param socksProxyPort The socks proxy port to set
   */
  public void setSocksProxyPort( String socksProxyPort ) {
    this.socksProxyPort = socksProxyPort;
  }

  /**
   * @param socksProxyUsername The socks proxy username to set
   */
  public void setSocksProxyUsername( String socksProxyUsername ) {
    this.socksProxyUsername = socksProxyUsername;
  }

  /**
   * @param socksProxyPassword The socks proxy password to set
   */
  public void setSocksProxyPassword( String socksProxyPassword ) {
    this.socksProxyPassword = socksProxyPassword;
  }

  /**
   * @return The sox proxy host name
   */
  public String getSocksProxyHost() {
    return this.socksProxyHost;
  }

  /**
   * @return The socks proxy port
   */
  public String getSocksProxyPort() {
    return this.socksProxyPort;
  }

  /**
   * @return The socks proxy username
   */
  public String getSocksProxyUsername() {
    return this.socksProxyUsername;
  }

  /**
   * @return The socks proxy password
   */
  public String getSocksProxyPassword() {
    return this.socksProxyPassword;
  }

  /**
   * @param proxyUsername The username which is used to authenticate at the proxy.
   */
  public void setProxyUsername( String proxyUsername ) {
    this.proxyUsername = proxyUsername;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );
    long filesput = 0;

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.Starting" ) );
    }

    FTPClient ftpclient = null;
    try {
      // Create ftp client to host:port ...
      ftpclient = createAndSetUpFtpClient();

      // login to ftp host ...
      String realUsername = environmentSubstitute( userName );
      String realPassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
      ftpclient.connect();
      ftpclient.login( realUsername, realPassword );

      // set BINARY
      if ( binaryMode ) {
        ftpclient.setType( FTPTransferType.BINARY );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.BinaryMode" ) );
        }
      }

      // Remove password from logging, you don't know where it ends up.
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.Logged", realUsername ) );
      }

      // Fix for PDI-2534 - add auxiliary FTP File List parsers to the ftpclient object.
      this.hookInOtherParsers( ftpclient );

      // move to spool dir ...
      String realRemoteDirectory = environmentSubstitute( remoteDirectory );
      if ( !Utils.isEmpty( realRemoteDirectory ) ) {
        ftpclient.chdir( realRemoteDirectory );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.ChangedDirectory", realRemoteDirectory ) );
        }
      }

      String realLocalDirectory = environmentSubstitute( localDirectory );
      if ( realLocalDirectory == null ) {
        throw new FTPException( BaseMessages.getString( PKG, "JobFTPPUT.LocalDir.NotSpecified" ) );
      } else {
        // handle file:/// prefix
        if ( realLocalDirectory.startsWith( "file:" ) ) {
          realLocalDirectory = new URI( realLocalDirectory ).getPath();
        }
      }

      final List<String> files;
      File localFiles = new File( realLocalDirectory );
      File[] children = localFiles.listFiles();
      if ( children == null ) {
        // Unable to read local directory for some reason...
        if ( log.isDebug() ) {
          logDebug(
            BaseMessages.getString( PKG, "JobFTPPUT.Log.UnableToReadLocalDirectoryExtended", realLocalDirectory,
              Files.isReadable( localFiles.toPath() ) ) );
        } else if ( log.isDetailed() ) {
          logDetailed(
            BaseMessages.getString( PKG, "JobFTPPUT.Log.UnableToReadLocalDirectory", realLocalDirectory ) );
        }
        files = Collections.emptyList();
      } else {
        files = new ArrayList<>( children.length );
        for ( File child : children ) {
          // Get filename of file or directory
          if ( !child.isDirectory() ) {
            files.add( child.getName() );
          }
        }
      }
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString(
          PKG, "JobFTPPUT.Log.FoundFileLocalDirectory", "" + files.size(), realLocalDirectory ) );
      }

      String realWildcard = environmentSubstitute( wildcard );
      Pattern pattern;
      if ( !Utils.isEmpty( realWildcard ) ) {
        pattern = Pattern.compile( realWildcard );
      } else {
        pattern = null;
      }

      for ( String file : files ) {
        if ( parentJob.isStopped() ) {
          break;
        }

        boolean toBeProcessed = true;

        // First see if the file matches the regular expression!
        if ( pattern != null ) {
          Matcher matcher = pattern.matcher( file );
          toBeProcessed = matcher.matches();
        }

        if ( toBeProcessed ) {
          // File exists?
          boolean fileExist = false;
          try {
            fileExist = ftpclient.exists( file );
          } catch ( Exception e ) {
            // Assume file does not exist !!
          }

          if ( log.isDebug() ) {
            if ( fileExist ) {
              logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.FileExists", file ) );
            } else {
              logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.FileDoesNotExists", file ) );
            }
          }

          if ( !fileExist || !onlyPuttingNewFiles ) {
            if ( log.isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobFTPPUT.Log.PuttingFileToRemoteDirectory", file, realRemoteDirectory ) );
            }

            String localFilename = realLocalDirectory + Const.FILE_SEPARATOR + file;
            ftpclient.put( localFilename, file );

            filesput++;

            // Delete the file if this is needed!
            if ( remove ) {
              new File( localFilename ).delete();
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.DeletedFile", localFilename ) );
              }
            }
          }
        }
      }

      result.setResult( true );
      if ( log.isDetailed() ) {
        logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.WeHavePut", "" + filesput ) );
      }
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobFTPPUT.Log.ErrorPuttingFiles", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
    } finally {
      if ( ftpclient != null && ftpclient.connected() ) {
        try {
          ftpclient.quit();
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobFTPPUT.Log.ErrorQuitingFTP", e.getMessage() ) );
        }
      }

      FTPClient.clearSOCKS();
    }

    return result;
  }

  // package-local visibility for testing purposes
  FTPClient createAndSetUpFtpClient() throws IOException, FTPException {
    String realServerName = environmentSubstitute( serverName );
    String realServerPort = environmentSubstitute( serverPort );

    FTPClient ftpClient = createFtpClient();
    ftpClient.setRemoteAddr( InetAddress.getByName( realServerName ) );
    if ( !Utils.isEmpty( realServerPort ) ) {
      ftpClient.setRemotePort( Const.toInt( realServerPort, FTP_DEFAULT_PORT ) );
    }

    if ( !Utils.isEmpty( proxyHost ) ) {
      String realProxyHost = environmentSubstitute( proxyHost );
      ftpClient.setRemoteAddr( InetAddress.getByName( realProxyHost ) );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPPUT.OpenedProxyConnectionOn", realProxyHost ) );
      }

      // FIXME: Proper default port for proxy
      int port = Const.toInt( environmentSubstitute( proxyPort ), FTP_DEFAULT_PORT );
      if ( port != 0 ) {
        ftpClient.setRemotePort( port );
      }
    } else {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTPPUT.OpenConnection", realServerName ) );
      }
    }

    // set activeConnection connectmode ...
    if ( activeConnection ) {
      ftpClient.setConnectMode( FTPConnectMode.ACTIVE );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.SetActiveConnection" ) );
      }
    } else {
      ftpClient.setConnectMode( FTPConnectMode.PASV );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.SetPassiveConnection" ) );
      }
    }

    // Set the timeout
    if ( timeout > 0 ) {
      ftpClient.setTimeout( timeout );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.SetTimeout", "" + timeout ) );
      }
    }

    ftpClient.setControlEncoding( controlEncoding );
    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.SetEncoding", controlEncoding ) );
    }

    // If socks proxy server was provided
    if ( !Utils.isEmpty( socksProxyHost ) ) {
      // if a port was provided
      if ( !Utils.isEmpty( socksProxyPort ) ) {
        FTPClient.initSOCKS( environmentSubstitute( socksProxyPort ), environmentSubstitute( socksProxyHost ) );
      } else { // looks like we have a host and no port
        throw new FTPException( BaseMessages.getString(
          PKG, "JobFTPPUT.SocksProxy.PortMissingException", environmentSubstitute( socksProxyHost ) ) );
      }
      // now if we have authentication information
      if ( !Utils.isEmpty( socksProxyUsername )
        && Utils.isEmpty( socksProxyPassword ) || Utils.isEmpty( socksProxyUsername )
        && !Utils.isEmpty( socksProxyPassword ) ) {
        // we have a username without a password or vice versa
        throw new FTPException( BaseMessages.getString(
          PKG, "JobFTPPUT.SocksProxy.IncompleteCredentials", environmentSubstitute( socksProxyHost ),
          getName() ) );
      }
    }

    return ftpClient;
  }

  // package-local visibility for testing purposes
  FTPClient createFtpClient() {
    return new PDIFTPClient( log );
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
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
      this, XML_TAG_LOCAL_DIRECTORY, remarks, AndValidator.putValidators(
          JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.fileExistsValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "userName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, XML_TAG_PASSWORD, remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "serverPort", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
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
          cName = parserClasses[ i ].trim();
          if ( !cName.isEmpty() ) {
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
