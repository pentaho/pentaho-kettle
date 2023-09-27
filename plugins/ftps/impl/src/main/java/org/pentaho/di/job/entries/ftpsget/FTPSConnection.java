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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.ftp4che.FTPConnection;
import org.ftp4che.FTPConnectionFactory;
import org.ftp4che.event.FTPEvent;
import org.ftp4che.event.FTPListener;
import org.ftp4che.exception.ConfigurationException;
import org.ftp4che.util.ftpfile.FTPFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.ftpsget.ftp4che.SecureDataFTPConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class FTPSConnection implements FTPListener {

  private static Class<?> PKG = JobEntryFTPSGet.class; // for i18n purposes, needed by Translator2!!
  private LogChannelInterface logger;

  public static final String HOME_FOLDER = "/";
  public static final String COMMAND_SUCCESSUL = "COMMAND SUCCESSFUL";

  public static final int CONNECTION_TYPE_FTP = 0;
  public static final int CONNECTION_TYPE_FTP_IMPLICIT_SSL = 1;
  public static final int CONNECTION_TYPE_FTP_AUTH_SSL = 2;
  public static final int CONNECTION_TYPE_FTP_IMPLICIT_SSL_WITH_CRYPTED = 3;
  public static final int CONNECTION_TYPE_FTP_AUTH_TLS = 4;
  public static final int CONNECTION_TYPE_FTP_IMPLICIT_TLS = 5;
  public static final int CONNECTION_TYPE_FTP_IMPLICIT_TLS_WITH_CRYPTED = 6;

  public static final String[] connection_type_Desc = new String[] {
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.FTP" ),
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.ImplicitSSL" ),
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.AuthSSL" ),
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.ImplicitSSLCrypted" ),
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.AuthTLS" ),
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.ImplicitTLS" ),
    BaseMessages.getString( PKG, "JobFTPS.ConnectionType.ImplicitTLSCrypted" ) };

  public static final String[] connection_type_Code = new String[] {
    "FTP_CONNECTION", "IMPLICIT_SSL_FTP_CONNECTION", "AUTH_SSL_FTP_CONNECTION",
    "IMPLICIT_SSL_WITH_CRYPTED_DATA_FTP_CONNECTION", "AUTH_TLS_FTP_CONNECTION", "IMPLICIT_TLS_FTP_CONNECTION",
    "IMPLICIT_TLS_WITH_CRYPTED_DATA_FTP_CONNECTION" };

  private FTPConnection connection = null;
  private ArrayList<String> replies = new ArrayList<String>();

  private String hostName;
  private int portNumber;
  private String userName;
  private String passWord;
  private int connectionType;
  private int timeOut;
  private boolean passiveMode;

  private String proxyHost;
  private String proxyUser;
  private String proxyPassword;
  private int proxyPort;
  private VariableSpace nameSpace;

  /**
   * Please supply real namespace as it is required for proper VFS operation
   */
  @Deprecated
  public FTPSConnection( int connectionType, String hostname, int port, String username, String password ) {
    this( connectionType, hostname, port, username, password, new Variables() );
  }

  public FTPSConnection( int connectionType, String hostname, int port, String username, String password,
                        VariableSpace nameSpace ) {
    this.hostName = hostname;
    this.portNumber = port;
    this.userName = username;
    this.passWord = password;
    this.connectionType = connectionType;
    this.passiveMode = false;
    this.nameSpace = nameSpace;
    this.logger = new LogChannel( this );
  }

  /**
   *
   * this method is used to set the proxy host
   *
   * @param type
   *          true: proxy host
   */
  public void setProxyHost( String proxyhost ) {
    this.proxyHost = proxyhost;
  }

  /**
   *
   * this method is used to set the proxy port
   *
   * @param type
   *          true: proxy port
   */
  public void setProxyPort( int proxyport ) {
    this.proxyPort = proxyport;
  }

  /**
   *
   * this method is used to set the proxy username
   *
   * @param type
   *          true: proxy username
   */
  public void setProxyUser( String username ) {
    this.proxyUser = username;
  }

  /**
   *
   * this method is used to set the proxy password
   *
   * @param type
   *          true: proxy password
   */
  public void setProxyPassword( String password ) {
    this.proxyPassword = password;
  }

  /**
   *
   * this method is used to connect to a remote host
   *
   * @throws KettleException
   */
  public void connect() throws KettleException {
    try {
      connection =
        FTPConnectionFactory.getInstance( getProperties(
          hostName, portNumber, userName, passWord, connectionType, timeOut, passiveMode ) );
      if ( connection.getConnectionType() == FTPConnection.IMPLICIT_SSL_WITH_CRYPTED_DATA_FTP_CONNECTION
          || connection.getConnectionType() == FTPConnection.IMPLICIT_TLS_WITH_CRYPTED_DATA_FTP_CONNECTION ) {
        // need to upgrade to our custom connection to force crypted data channel
        connection = getSecureDataFTPConnection( connection, passWord, timeOut );
      }
      connection.addFTPStatusListener( this );
      connection.connect();
    } catch ( Exception e ) {
      connection = null;
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.Connecting", hostName ), e );
    }
  }

  @VisibleForTesting
  protected FTPConnection getSecureDataFTPConnection( FTPConnection connection, String password, int timeout )
    throws ConfigurationException {
    return new SecureDataFTPConnection( connection, password, timeout );
  }

  private Properties getProperties( String hostname, int port, String username, String password,
    int connectionType, int timeout, boolean passiveMode ) {
    Properties pt = new Properties();
    pt.setProperty( "connection.host", hostname );
    pt.setProperty( "connection.port", String.valueOf( port ) );
    pt.setProperty( "user.login", username );
    pt.setProperty( "user.password", password );
    pt.setProperty( "connection.type", getConnectionType( connectionType ) );
    pt.setProperty( "connection.timeout", String.valueOf( timeout ) );
    pt.setProperty( "connection.passive", String.valueOf( passiveMode ) );
    // Set proxy
    if ( this.proxyHost != null ) {
      pt.setProperty( "proxy.host", this.proxyHost );
    }
    if ( this.proxyPort != 0 ) {
      pt.setProperty( "proxy.port", String.valueOf( this.proxyPort ) );
    }
    if ( this.proxyUser != null ) {
      pt.setProperty( "proxy.user", this.proxyUser );
    }
    if ( this.proxyPassword != null ) {
      pt.setProperty( "proxy.pass", this.proxyPassword );
    }

    return pt;
  }

  public static String getConnectionTypeDesc( String tt ) {
    if ( Utils.isEmpty( tt ) ) {
      return connection_type_Desc[0];
    }
    if ( tt.equalsIgnoreCase( connection_type_Code[ 1 ] ) ) {
      return connection_type_Desc[1];
    } else {
      return connection_type_Desc[0];
    }
  }

  public static String getConnectionTypeCode( String tt ) {
    if ( tt == null ) {
      return connection_type_Code[0];
    }
    if ( tt.equals( connection_type_Desc[1] ) ) {
      return connection_type_Code[1];
    } else {
      return connection_type_Code[0];
    }
  }

  public static String getConnectionTypeDesc( int i ) {
    if ( i < 0 || i >= connection_type_Desc.length ) {
      return connection_type_Desc[0];
    }
    return connection_type_Desc[i];
  }

  public static String getConnectionType( int i ) {
    return connection_type_Code[i];
  }

  public static int getConnectionTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < connection_type_Desc.length; i++ ) {
      if ( connection_type_Desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails,return the first value
    return 0;
  }

  public static int getConnectionTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < connection_type_Code.length; i++ ) {
      if ( connection_type_Code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getConnectionTypeCode( int i ) {
    if ( i < 0 || i >= connection_type_Code.length ) {
      return connection_type_Code[0];
    }
    return connection_type_Code[i];
  }

  /**
   * public void setBinaryMode(boolean type)
   *
   * this method is used to set the transfer type to binary
   *
   * @param type
   *          true: Binary
   * @throws KettleException
   */
  public void setBinaryMode( boolean type ) throws KettleException {
    try {
      connection.setTransferType( true );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   *
   * this method is used to set the mode to passive
   *
   * @param type
   *          true: passive mode
   */
  public void setPassiveMode( boolean passivemode ) {
    this.passiveMode = passivemode;
  }

  /**
   *
   * this method is used to return the passive mode
   *
   * @return TRUE if we use passive mode
   *
   */
  public boolean isPassiveMode() {
    return this.passiveMode;
  }

  /**
   *
   * this method is used to set the timeout
   *
   * @param timeout
   *
   */
  public void setTimeOut( int timeout ) {
    this.timeOut = timeout;
  }

  /**
   *
   * this method is used to return the timeout
   *
   * @return timeout
   *
   */
  public int getTimeOut() {
    return this.timeOut;
  }

  public String getUserName() {
    return userName;
  }

  public String getHostName() {
    return hostName;
  }

  public ArrayList<String> getReplies() {
    return replies;
  }

  /**
   *
   * this method is used to set the connection type
   *
   * @param type
   *          true: connection type
   */
  public void setConnectionType( int connectiontype ) {
    this.connectionType = connectiontype;
  }

  public int getConnectionType() {
    return this.connectionType;
  }

  public void connectionStatusChanged( FTPEvent arg0 ) {
  }

  public void replyMessageArrived( FTPEvent event ) {
    this.replies = new ArrayList<String>();
    for ( String e : event.getReply().getLines() ) {
      if ( !e.trim().equals( "" ) ) {
        e = e.substring( 3 ).trim().replace( "\n", "" );
        if ( !e.toUpperCase().contains( COMMAND_SUCCESSUL ) ) {
          e = e.substring( 1 ).trim();
          replies.add( e );
        }
      }
    }
  }

  /**
   *
   * this method change FTP working directory
   *
   * @param directory
   *          change the working directory
   * @throws KettleException
   */
  public void changeDirectory( String directory ) throws KettleException {
    try {
      this.connection.changeDirectory( directory );
    } catch ( Exception f ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.ChangingFolder", directory ), f );
    }
  }

  /**
   *
   * this method is used to create a directory in remote host
   *
   * @param directory
   *          directory name on remote host
   * @throws KettleException
   */
  public void createDirectory( String directory ) throws KettleException {
    try {
      this.connection.makeDirectory( directory );
    } catch ( Exception f ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.CreationFolder", directory ), f );
    }
  }

  public List<FTPFile> getFileList( String folder ) throws KettleException {
    try {
      if ( connection != null ) {
        List<FTPFile> response = connection.getDirectoryListing( folder );
        return response;
      } else {
        return null;
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * this method is used to download a file from a remote host
   *
   * @param file          remote file to download
   * @param localFilename target filename
   * @throws KettleException
   */
  public void downloadFile( FTPFile file, String localFilename ) throws KettleException {
    try {
      FileObject localFile = KettleVFS.getFileObject( localFilename, nameSpace );
      writeToFile( connection.downloadStream( file ), localFile.getContent().getOutputStream(), localFilename );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private void writeToFile( InputStream is, OutputStream os, String filename ) throws KettleException {
    try {
      IOUtils.copy( is, os );
    } catch ( IOException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.WritingToFile", filename ), e );
    } finally {
      IOUtils.closeQuietly( is );
      IOUtils.closeQuietly( os );
    }
  }

  /**
   *
   * this method is used to upload a file to a remote host
   *
   * @param localFileName
   *          Local full filename
   * @param shortFileName
   *          Filename in remote host
   * @throws KettleException
   */
  public void uploadFile( String localFileName, String shortFileName ) throws KettleException {
    FileObject file = null;

    try {
      file = KettleVFS.getFileObject( localFileName, nameSpace );
      this.connection.uploadStream( file.getContent().getInputStream(), new FTPFile( new File( shortFileName ) ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.UuploadingFile", localFileName ), e );
    } finally {
      if ( file != null ) {
        try {
          file.close();
        } catch ( Exception e ) {
           //we do not able to close file will log it
          logger.logDetailed( "Unable to close file file", e );
        }
      }
    }
  }

  /**
   *
   * this method is used to return filenames in working directory
   *
   * @return filenames
   * @throws KettleException
   */
  public String[] getFileNames() throws KettleException {
    ArrayList<String> list = null;
    try {
      List<FTPFile> fileList = getFileList( getWorkingDirectory() );
      list = new ArrayList<String>();
      Iterator<FTPFile> it = fileList.iterator();
      while ( it.hasNext() ) {
        FTPFile file = it.next();
        if ( !file.isDirectory() ) {
          list.add( file.getName() );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.RetrievingFilenames" ), e );
    }
    return list == null ? null : list.toArray( new String[list.size()] );
  }

  /**
   *
   * this method is used to delete a file in remote host
   *
   * @param file
   *          File on remote host to delete
   * @throws KettleException
   */
  public void deleteFile( FTPFile file ) throws KettleException {
    try {
      this.connection.deleteFile( file );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.DeletingFile", file.getName() ), e );
    }
  }

  /**
   *
   * this method is used to delete a file in remote host
   *
   * @param filename
   *          Name of file on remote host to delete
   * @throws KettleException
   */
  public void deleteFile( String filename ) throws KettleException {
    try {
      this.connection.deleteFile( new FTPFile( getWorkingDirectory(), filename ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.DeletingFile", filename ), e );
    }
  }

  /**
   *
   * this method is used to move a file to remote directory
   *
   * @param fromFile
   *          File on remote host to move
   * @param targetFoldername
   *          Target remote folder
   * @throws KettleException
   */
  public void moveToFolder( FTPFile fromFile, String targetFoldername ) throws KettleException {
    try {
      this.connection.renameFile( fromFile, new FTPFile( targetFoldername, fromFile.getName() ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPS.Error.MovingFileToFolder", fromFile
        .getName(), targetFoldername ), e );
    }
  }

  /**
   *
   * Checks if a directory exists
   *
   * @return true if the directory exists
   *
   */
  public boolean isDirectoryExists( String directory ) {
    String currectDirectory = null;
    boolean retval = false;
    try {
      // Before save current directory
      currectDirectory = this.connection.getWorkDirectory();
      // Change directory
      this.connection.changeDirectory( directory );
      retval = true;
    } catch ( Exception e ) {
      // Ignore directory change errors
    } finally {
      // switch back to the current directory
      if ( currectDirectory != null ) {
        try {
          this.connection.changeDirectory( currectDirectory );
        } catch ( Exception e ) {
          // Ignore directory change errors
        }
      }
    }
    return retval;
  }

  /**
   *
   * Checks if a file exists on remote host
   *
   * @param filename
   *          the name of the file to check
   * @return true if the file exists
   *
   */
  public boolean isFileExists( String filename ) {
    boolean retval = false;
    try {
      FTPFile file = new FTPFile( new File( filename ) );
      // Get modification time just to check if file exists
      connection.getModificationTime( file );
      retval = true;
    } catch ( Exception e ) {
      // Ignore errors
    }
    return retval;
  }

  /**
   *
   * Returns the working directory
   *
   * @return working directory
   * @throws Exception
   */
  public String getWorkingDirectory() throws Exception {
    return this.connection.getWorkDirectory();
  }

  /**
   *
   * this method is used to disconnect the connection
   *
   */
  public void disconnect() {
    if ( this.connection != null ) {
      this.connection.disconnect();
    }
    if ( this.replies != null ) {
      this.replies.clear();
    }
  }

}
