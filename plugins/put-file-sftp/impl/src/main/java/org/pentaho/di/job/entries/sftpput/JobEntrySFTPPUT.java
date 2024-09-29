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

package org.pentaho.di.job.entries.sftpput;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sftp.SFTPClient;
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This defines an SFTP put job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */

@JobEntry( id = "SFTPPUT", name = "JobEntry.SFTPPut.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.sftpput",
        description = "JobEntry.SFTPPut.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.FileTransfer",
        image = "PSFTP.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Put+a+file+with+SFTP" )
public class JobEntrySFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySFTPPUT.class; // for i18n purposes, needed by Translator2!!

  private int afterFTPS;

  public static final String[] afterFTPSDesc = new String[] {
    BaseMessages.getString( PKG, "JobSFTPPUT.AfterSFTP.DoNothing.Label" ),
    BaseMessages.getString( PKG, "JobSFTPPUT.AfterSFTP.Delete.Label" ),
    BaseMessages.getString( PKG, "JobSFTPPUT.AfterSFTP.Move.Label" ), };
  public static final String[] afterFTPSCode = new String[] { "nothing", "delete", "move" };

  public static final int AFTER_FTPSPUT_NOTHING = 0;
  public static final int AFTER_FTPSPUT_DELETE = 1;
  public static final int AFTER_FTPSPUT_MOVE = 2;

  private String serverName;
  private String serverPort;
  private String userName;
  private String password;
  private String sftpDirectory;
  private String localDirectory;
  private String wildcard;
  private boolean copyprevious;
  private boolean copypreviousfiles;
  private boolean addFilenameResut;
  private boolean usekeyfilename;
  private String keyfilename;
  private String keyfilepass;
  private String compression;
  private boolean createRemoteFolder;
  // proxy
  private String proxyType;
  private String proxyHost;
  private String proxyPort;
  private String proxyUsername;
  private String proxyPassword;

  private String destinationfolder;
  private boolean createDestinationFolder;

  private boolean successWhenNoFile;

  public JobEntrySFTPPUT(String n ) {
    super( n, "" );
    serverName = null;
    serverPort = "22";
    copyprevious = false;
    copypreviousfiles = false;
    addFilenameResut = false;
    usekeyfilename = false;
    keyfilename = null;
    keyfilepass = null;
    compression = "none";
    proxyType = null;
    proxyHost = null;
    proxyPort = null;
    proxyUsername = null;
    proxyPassword = null;
    createRemoteFolder = false;
    afterFTPS = AFTER_FTPSPUT_NOTHING;
    destinationfolder = null;
    createDestinationFolder = false;
    successWhenNoFile = false;
  }

  public JobEntrySFTPPUT() {
    this( "" );
  }

  public Object clone() {
    JobEntrySFTPPUT je = (JobEntrySFTPPUT) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "serverport", serverPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", userName ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sftpdirectory", sftpDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "localdirectory", localDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "copyprevious", copyprevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "copypreviousfiles", copypreviousfiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addFilenameResut", addFilenameResut ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usekeyfilename", usekeyfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "keyfilename", keyfilename ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "keyfilepass", Encr.encryptPasswordIfNotUsingVariables( keyfilepass ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compression", compression ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyType", proxyType ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyHost", proxyHost ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyPort", proxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyUsername", proxyUsername ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "proxyPassword", Encr.encryptPasswordIfNotUsingVariables( proxyPassword ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "createRemoteFolder", createRemoteFolder ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "aftersftpput", getAfterSFTPPutCode( getAfterFTPS() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "destinationfolder", destinationfolder ) );
    retval
      .append( "      " ).append( XMLHandler.addTagValue( "createdestinationfolder", createDestinationFolder ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "successWhenNoFile", successWhenNoFile ) );
    if ( parentJobMeta != null ) {
      parentJobMeta.getNamedClusterEmbedManager().registerUrl( localDirectory );
    }

    return retval.toString();
  }

  public static String getAfterSFTPPutCode( int i ) {
    if ( i < 0 || i >= afterFTPSCode.length ) {
      return afterFTPSCode[0];
    }
    return afterFTPSCode[i];
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      serverName = XMLHandler.getTagValue( entrynode, "servername" );
      serverPort = XMLHandler.getTagValue( entrynode, "serverport" );
      userName = XMLHandler.getTagValue( entrynode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );
      sftpDirectory = XMLHandler.getTagValue( entrynode, "sftpdirectory" );
      localDirectory = XMLHandler.getTagValue( entrynode, "localdirectory" );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      copyprevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "copyprevious" ) );
      copypreviousfiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "copypreviousfiles" ) );
      addFilenameResut = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addFilenameResut" ) );

      usekeyfilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "usekeyfilename" ) );
      keyfilename = XMLHandler.getTagValue( entrynode, "keyfilename" );
      keyfilepass = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "keyfilepass" ) );
      compression = XMLHandler.getTagValue( entrynode, "compression" );
      proxyType = XMLHandler.getTagValue( entrynode, "proxyType" );
      proxyHost = XMLHandler.getTagValue( entrynode, "proxyHost" );
      proxyPort = XMLHandler.getTagValue( entrynode, "proxyPort" );
      proxyUsername = XMLHandler.getTagValue( entrynode, "proxyUsername" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "proxyPassword" ) );

      createRemoteFolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createRemoteFolder" ) );

      boolean remove = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "remove" ) );
      setAfterFTPS( getAfterSFTPPutByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "aftersftpput" ), "" ) ) );
      if ( remove && getAfterFTPS() == AFTER_FTPSPUT_NOTHING ) {
        setAfterFTPS( AFTER_FTPSPUT_DELETE );
      }
      destinationfolder = XMLHandler.getTagValue( entrynode, "destinationfolder" );
      createDestinationFolder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createdestinationfolder" ) );
      successWhenNoFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "successWhenNoFile" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'SFTPPUT' from XML node", xe );
    }
  }

  public static int getAfterSFTPPutByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < afterFTPSCode.length; i++ ) {
      if ( afterFTPSCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getAfterSFTPPutDesc( int i ) {
    if ( i < 0 || i >= afterFTPSDesc.length ) {
      return afterFTPSDesc[0];
    }
    return afterFTPSDesc[i];
  }

  public static int getAfterSFTPPutByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < afterFTPSDesc.length; i++ ) {
      if ( afterFTPSDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getAfterSFTPPutByCode( tt );
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      serverName = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      serverPort = rep.getJobEntryAttributeString( id_jobentry, "serverport" );

      userName = rep.getJobEntryAttributeString( id_jobentry, "username" );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );
      sftpDirectory = rep.getJobEntryAttributeString( id_jobentry, "sftpdirectory" );
      localDirectory = rep.getJobEntryAttributeString( id_jobentry, "localdirectory" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      copyprevious = rep.getJobEntryAttributeBoolean( id_jobentry, "copyprevious" );
      copypreviousfiles = rep.getJobEntryAttributeBoolean( id_jobentry, "copypreviousfiles" );
      addFilenameResut = rep.getJobEntryAttributeBoolean( id_jobentry, "addFilenameResut" );

      usekeyfilename = rep.getJobEntryAttributeBoolean( id_jobentry, "usekeyfilename" );
      keyfilename = rep.getJobEntryAttributeString( id_jobentry, "keyfilename" );
      keyfilepass =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "keyfilepass" ) );
      compression = rep.getJobEntryAttributeString( id_jobentry, "compression" );
      proxyType = rep.getJobEntryAttributeString( id_jobentry, "proxyType" );
      proxyHost = rep.getJobEntryAttributeString( id_jobentry, "proxyHost" );
      proxyPort = rep.getJobEntryAttributeString( id_jobentry, "proxyPort" );
      proxyUsername = rep.getJobEntryAttributeString( id_jobentry, "proxyUsername" );
      proxyPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "proxyPassword" ) );

      createRemoteFolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createRemoteFolder" );

      boolean remove = rep.getJobEntryAttributeBoolean( id_jobentry, "remove" );
      setAfterFTPS( getAfterSFTPPutByCode( Const.NVL(
        rep.getJobEntryAttributeString( id_jobentry, "aftersftpput" ), "" ) ) );
      if ( remove && getAfterFTPS() == AFTER_FTPSPUT_NOTHING ) {
        setAfterFTPS( AFTER_FTPSPUT_DELETE );
      }
      destinationfolder = rep.getJobEntryAttributeString( id_jobentry, "destinationfolder" );
      createDestinationFolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createdestinationfolder" );
      successWhenNoFile = rep.getJobEntryAttributeBoolean( id_jobentry, "successWhenNoFile" );

    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'SFTPPUT' from the repository for id_jobentry="
        + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "serverport", serverPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", userName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sftpdirectory", sftpDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "localdirectory", localDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "copyprevious", copyprevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "copypreviousfiles", copypreviousfiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addFilenameResut", addFilenameResut );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "usekeyfilename", usekeyfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "keyfilename", keyfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "keyfilepass", Encr
        .encryptPasswordIfNotUsingVariables( keyfilepass ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "compression", compression );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxyType", proxyType );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxyHost", proxyHost );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxyPort", proxyPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxyUsername", proxyUsername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxyPassword", Encr
        .encryptPasswordIfNotUsingVariables( proxyPassword ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "aftersftpput", getAfterSFTPPutCode( getAfterFTPS() ) );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "createRemoteFolder", createRemoteFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "destinationfolder", destinationfolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "createdestinationfolder", createDestinationFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "successWhenNoFile", successWhenNoFile );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'SFTPPUT' to the repository for id_job="
        + id_job, dbe );
    }
  }

  /**
   * @param createDestinationFolder
   *          The create destination folder flag to set.
   */
  public void setCreateDestinationFolder( boolean createDestinationFolder ) {
    this.createDestinationFolder = createDestinationFolder;
  }

  /**
   * @return Returns the create destination folder flag
   */
  public boolean isCreateDestinationFolder() {
    return createDestinationFolder;
  }

  /**
   * @param successWhenNoFile
   *          The successWhenNoFile flag to set.
   */
  public void setSuccessWhenNoFile( boolean successWhenNoFile ) {
    this.successWhenNoFile = successWhenNoFile;
  }

  /**
   * @return Returns the create successWhenNoFile folder flag
   */
  public boolean isSuccessWhenNoFile() {
    return successWhenNoFile;
  }

  public void setDestinationFolder( String destinationfolderin ) {
    this.destinationfolder = destinationfolderin;
  }

  public String getDestinationFolder() {
    return destinationfolder;
  }

  /**
   * @return Returns the afterFTPS.
   */
  public int getAfterFTPS() {
    return afterFTPS;
  }

  /**
   * @param value
   *          The afterFTPS to set.
   */
  public void setAfterFTPS( int value ) {
    this.afterFTPS = value;
  }

  /**
   * @return Returns the directory.
   */
  public String getScpDirectory() {
    return sftpDirectory;
  }

  /**
   * @param directory
   *          The directory to set.
   */
  public void setScpDirectory( String directory ) {
    this.sftpDirectory = directory;
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
   * @return Returns the localdirectory.
   */
  public String getLocalDirectory() {
    return localDirectory;
  }

  /**
   * @param localDirectory
   *          The localDirectory to set.
   */
  public void setLocalDirectory( String localDirectory ) {
    this.localDirectory = localDirectory;
  }

  public boolean isCopyPrevious() {
    return copyprevious;
  }

  public void setCopyPrevious( boolean copyprevious ) {
    this.copyprevious = copyprevious;
  }

  public boolean isCopyPreviousFiles() {
    return copypreviousfiles;
  }

  public void setCopyPreviousFiles( boolean copypreviousfiles ) {
    this.copypreviousfiles = copypreviousfiles;
  }

  public boolean isAddFilenameResut() {
    return addFilenameResut;
  }

  public boolean isUseKeyFile() {
    return usekeyfilename;
  }

  public void setUseKeyFile( boolean value ) {
    this.usekeyfilename = value;
  }

  public String getKeyFilename() {
    return keyfilename;
  }

  public void setKeyFilename( String value ) {
    this.keyfilename = value;
  }

  public String getKeyPassPhrase() {
    return keyfilepass;
  }

  public void setKeyPassPhrase( String value ) {
    this.keyfilepass = value;
  }

  public void setAddFilenameResut( boolean addFilenameResut ) {
    this.addFilenameResut = addFilenameResut;
  }

  /**
   * @return Returns the compression.
   */
  public String getCompression() {
    return compression;
  }

  /**
   * @param compression
   *          The compression to set.
   */
  public void setCompression( String compression ) {
    this.compression = compression;
  }

  public String getServerPort() {
    return serverPort;
  }

  public void setServerPort( String serverPort ) {
    this.serverPort = serverPort;
  }

  public String getProxyType() {
    return proxyType;
  }

  public void setProxyType( String value ) {
    this.proxyType = value;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost( String value ) {
    this.proxyHost = value;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort( String value ) {
    this.proxyPort = value;
  }

  public String getProxyUsername() {
    return proxyUsername;
  }

  public void setProxyUsername( String value ) {
    this.proxyUsername = value;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword( String value ) {
    this.proxyPassword = value;
  }

  public boolean isCreateRemoteFolder() {
    return this.createRemoteFolder;
  }

  public void setCreateRemoteFolder( boolean value ) {
    this.createRemoteFolder = value;
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    List<RowMetaAndData> rows = result.getRows();
    result.setResult( false );

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.StartJobEntry" ) );
    }
    ArrayList<FileObject> myFileList = new ArrayList<FileObject>();

    if ( copyprevious ) {
      if ( rows.size() == 0 ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.ArgsFromPreviousNothing" ) );
        }
        result.setResult( true );
        return result;
      }

      try {
        RowMetaAndData resultRow = null;
        // Copy the input row to the (command line) arguments
        for ( int iteration = 0; iteration < rows.size(); iteration++ ) {
          resultRow = rows.get( iteration );

          // Get file names
          String file_previous = resultRow.getString( 0, null );
          if ( !Utils.isEmpty( file_previous ) ) {
            FileObject file = KettleVFS.getFileObject( file_previous, this );
            if ( !file.exists() ) {
              logError( BaseMessages.getString( PKG, "JobSFTPPUT.Log.FilefromPreviousNotFound", file_previous ) );
            } else {
              myFileList.add( file );
              if ( log.isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "JobSFTPPUT.Log.FilenameFromResult", file_previous ) );
              }
            }
          }
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "JobSFTPPUT.Error.ArgFromPrevious" ) );
        result.setNrErrors( 1 );
        // free resource
        myFileList = null;
        return result;
      }
    }

    if ( copypreviousfiles ) {
      List<ResultFile> resultFiles = result.getResultFilesList();
      if ( resultFiles == null || resultFiles.size() == 0 ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.ArgsFromPreviousNothingFiles" ) );
        }
        result.setResult( true );
        return result;
      }

      try {
        for ( Iterator<ResultFile> it = resultFiles.iterator(); it.hasNext() && !parentJob.isStopped(); ) {
          ResultFile resultFile = it.next();
          FileObject file = resultFile.getFile();
          if ( file != null ) {
            if ( !file.exists() ) {
              logError( BaseMessages.getString( PKG, "JobSFTPPUT.Log.FilefromPreviousNotFound", file.toString() ) );
            } else {
              myFileList.add( file );
              if ( log.isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "JobSFTPPUT.Log.FilenameFromResult", file.toString() ) );
              }
            }
          }
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "JobSFTPPUT.Error.ArgFromPrevious" ) );
        result.setNrErrors( 1 );
        // free resource
        myFileList = null;
        return result;
      }
    }

    SFTPClient sftpclient = null;

    // String substitution..
    String realServerName = environmentSubstitute( serverName );
    String realServerPort = environmentSubstitute( serverPort );
    String realUsername = environmentSubstitute( userName );
    String realPassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
    String realSftpDirString = environmentSubstitute( sftpDirectory );
    String realWildcard = environmentSubstitute( wildcard );
    String realLocalDirectory = environmentSubstitute( localDirectory );
    String realKeyFilename = null;
    String realPassPhrase = null;
    // Destination folder (Move to)
    String realDestinationFolder = environmentSubstitute( getDestinationFolder() );

    try {
      // Let's perform some checks before starting

      if ( getAfterFTPS() == AFTER_FTPSPUT_MOVE ) {
        if ( Utils.isEmpty( realDestinationFolder ) ) {
          logError( BaseMessages.getString( PKG, "JobSSH2PUT.Log.DestinatFolderMissing" ) );
          result.setNrErrors( 1 );
          return result;
        } else {
          FileObject folder = null;
          try {
            folder = KettleVFS.getFileObject( realDestinationFolder, this );
            // Let's check if folder exists...
            if ( !folder.exists() ) {
              // Do we need to create it?
              if ( createDestinationFolder ) {
                folder.createFolder();
              } else {
                logError( BaseMessages.getString(
                  PKG, "JobSSH2PUT.Log.DestinatFolderNotExist", realDestinationFolder ) );
                result.setNrErrors( 1 );
                return result;
              }
            }
            realDestinationFolder = KettleVFS.getFilename( folder );
          } catch ( Exception e ) {
            throw new KettleException( e );
          } finally {
            if ( folder != null ) {
              try {
                folder.close();
              } catch ( Exception e ) { /* Ignore */
              }
            }
          }
        }
      }

      if ( isUseKeyFile() ) {
        // We must have here a private keyfilename
        realKeyFilename = environmentSubstitute( getKeyFilename() );
        if ( Utils.isEmpty( realKeyFilename ) ) {
          // Error..Missing keyfile
          logError( BaseMessages.getString( PKG, "JobSFTP.Error.KeyFileMissing" ) );
          result.setNrErrors( 1 );
          return result;
        }
        if ( !KettleVFS.fileExists( realKeyFilename ) ) {
          // Error.. can not reach keyfile
          logError( BaseMessages.getString( PKG, "JobSFTP.Error.KeyFileNotFound" ) );
          result.setNrErrors( 1 );
          return result;
        }
        realPassPhrase = environmentSubstitute( getKeyPassPhrase() );
      }

      // Create sftp client to host ...
      sftpclient =
        new SFTPClient(
          InetAddress.getByName( realServerName ), Const.toInt( realServerPort, 22 ), realUsername,
          realKeyFilename, realPassPhrase );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.OpenedConnection", realServerName, ""
          + realServerPort, realUsername ) );
      }

      // Set compression
      sftpclient.setCompression( getCompression() );

      // Set proxy?
      String realProxyHost = environmentSubstitute( getProxyHost() );
      if ( !Utils.isEmpty( realProxyHost ) ) {
        // Set proxy
        sftpclient.setProxy(
          realProxyHost, environmentSubstitute( getProxyPort() ), environmentSubstitute( getProxyUsername() ),
          environmentSubstitute( getProxyPassword() ), getProxyType() );
      }

      // login to ftp host ...
      sftpclient.login( realPassword );
      // Don't show the password in the logs, it's not good for security audits
      // logDetailed("logged in using password "+realPassword); // Logging this seems a bad idea! Oh well.

      // move to spool dir ...
      if ( !Utils.isEmpty( realSftpDirString ) ) {
        boolean existfolder = sftpclient.folderExists( realSftpDirString );
        if ( !existfolder ) {
          if ( !isCreateRemoteFolder() ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobSFTPPUT.Error.CanNotFindRemoteFolder", realSftpDirString ) );
          }
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages
              .getString( PKG, "JobSFTPPUT.Error.CanNotFindRemoteFolder", realSftpDirString ) );
          }

          // Let's create folder
          sftpclient.createFolder( realSftpDirString );
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.RemoteFolderCreated", realSftpDirString ) );
          }
        }
        sftpclient.chdir( realSftpDirString );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.ChangedDirectory", realSftpDirString ) );
        }
      } // end if

      if ( !copyprevious && !copypreviousfiles ) {
        // Get all the files in the local directory...
        myFileList = new ArrayList<FileObject>();

        FileObject localFiles = KettleVFS.getFileObject( realLocalDirectory, this );
        FileObject[] children = localFiles.getChildren();
        if ( children != null ) {
          for ( int i = 0; i < children.length; i++ ) {
            // Get filename of file or directory
            if ( children[i].getType().equals( FileType.FILE ) ) {
              // myFileList.add(children[i].getAbsolutePath());
              myFileList.add( children[i] );
            }
          } // end for
        }
      }

      if ( myFileList == null || myFileList.size() == 0 ) {
        if ( isSuccessWhenNoFile() ) {
          // Just warn user
          if ( isBasic() ) {
            logBasic( BaseMessages.getString( PKG, "JobSFTPPUT.Error.NoFileToSend" ) );
          }
        } else {
          // Fail
          logError( BaseMessages.getString( PKG, "JobSFTPPUT.Error.NoFileToSend" ) );
          result.setNrErrors( 1 );
          return result;
        }
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.RowsFromPreviousResult", myFileList.size() ) );
      }

      Pattern pattern = null;
      if ( !copyprevious && !copypreviousfiles ) {
        if ( !Utils.isEmpty( realWildcard ) ) {
          pattern = Pattern.compile( realWildcard );
        }
      }

      // Get the files in the list and execute sftp.put() for each file
      Iterator<FileObject> it = myFileList.iterator();
      while ( it.hasNext() && !parentJob.isStopped() ) {
        FileObject myFile = it.next();
        try {
          String localFilename = myFile.toString();
          String destinationFilename = myFile.getName().getBaseName();
          boolean getIt = true;

          // First see if the file matches the regular expression!
          if ( pattern != null ) {
            Matcher matcher = pattern.matcher( destinationFilename );
            getIt = matcher.matches();
          }

          if ( getIt ) {
            if ( log.isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSFTPPUT.Log.PuttingFile", localFilename, realSftpDirString ) );
            }

            sftpclient.put( myFile, destinationFilename );

            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.TransferedFile", localFilename ) );
            }

            // We successfully uploaded the file
            // what's next ...
            switch ( getAfterFTPS() ) {
              case AFTER_FTPSPUT_DELETE:
                myFile.delete();
                if ( log.isDetailed() ) {
                  logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.DeletedFile", localFilename ) );
                }
                break;
              case AFTER_FTPSPUT_MOVE:
                FileObject destination = null;
                try {
                  destination =
                    KettleVFS.getFileObject( realDestinationFolder
                      + Const.FILE_SEPARATOR + myFile.getName().getBaseName(), this );
                  myFile.moveTo( destination );
                  if ( log.isDetailed() ) {
                    logDetailed( BaseMessages.getString( PKG, "JobSFTPPUT.Log.FileMoved", myFile, destination ) );
                  }
                } finally {
                  if ( destination != null ) {
                    destination.close();
                  }
                }
                break;
              default:
                if ( addFilenameResut ) {
                  // Add to the result files...
                  ResultFile resultFile =
                    new ResultFile( ResultFile.FILE_TYPE_GENERAL, myFile, parentJob.getJobname(), toString() );
                  result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
                  if ( log.isDetailed() ) {
                    logDetailed( BaseMessages.getString(
                      PKG, "JobSFTPPUT.Log.FilenameAddedToResultFilenames", localFilename ) );
                  }
                }
                break;
            }

          }
        } finally {
          if ( myFile != null ) {
            myFile.close();
          }
        }
      } // end for

      result.setResult( true );
      // JKU: no idea if this is needed...!
      // result.setNrFilesRetrieved(filesRetrieved);
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobSFTPPUT.Exception", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
    } finally {
      // close connection, if possible
      try {
        if ( sftpclient != null ) {
          sftpclient.disconnect();
        }
      } catch ( Exception e ) {
        // just ignore this, makes no big difference
      } // end catch
      myFileList = null;
    } // end finally

    return result;
  } // JKU: end function execute()

  public boolean evaluates() {
    return true;
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
      this, "localDirectory", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator(),
          JobEntryValidatorUtils.fileExistsValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "userName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "password", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "serverPort", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

}
