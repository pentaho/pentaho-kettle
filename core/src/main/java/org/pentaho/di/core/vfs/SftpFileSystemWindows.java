/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.sftp.SftpClientFactory;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystem;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SftpFileSystemWindows extends SftpFileSystem {

  private static final LogChannelInterface log = new LogChannel( "SftpFileSystemWindows" );
  private static final String WHO_AMI_GROUPS_FO_LIST = "Whoami /GROUPS /FO LIST"; //windows command for getting croups for current user
  private static final String WHO_AMI = "Whoami "; ////windows command for getting current user
  private static final String ICACLS = "icacls "; //windows command for getting permissions for file
  private static final String VER = "ver"; //windows command for getting version OS
  private static final String GROUP_NAME = "Group Name:";
  private static final String WINDOWS = "WINDOWS";
  private static final String N_DELIMITER = "\\n";
  private static final String RN_DELIMITER = "\r\n";
  private static final String WINDOWS_PATH_DELIMITER = "/";

  private Session session;
  private List<String> userGroups;
  private Boolean windows;

  SftpFileSystemWindows( GenericFileName rootName, Session session, FileSystemOptions fileSystemOptions ) {
    super( rootName, session, fileSystemOptions );
    this.session = session;
  }

  @Override
  protected FileObject createFile( AbstractFileName name ) throws FileSystemException {
    return new SftpFileObjectWithWindowsSupport( name, this );
  }

  @Override
  protected void doCloseCommunicationLink() {
    if ( this.session != null ) {
      this.session.disconnect();
      this.session = null;
    }
    super.doCloseCommunicationLink();
  }

  @Override
  public boolean isReleaseable() {
    return !isOpen();
  }

  /**
   * get user group on remote windows host
   * @return  list of groups + user person
   * @throws JSchException
   * @throws IOException
   */
  List<String> getUserGroups() throws JSchException, IOException {
    if ( userGroups == null ) {
      StringBuilder output = new StringBuilder();
      int code = this.executeCommand( WHO_AMI_GROUPS_FO_LIST, output );
      if ( code != 0 ) {
        throw new JSchException( "Could not get the groups  of the current user (error code: " + code + ")" );
      }

      this.userGroups = getUserGroups( output.toString() );
      userGroups.add( getUser() );
    }
    return this.userGroups;
  }

  /**
   * cut user groups from output whoami
   *
   * @param commandOutput output from whoami
   * @return list of user groups
   */
  private List<String> getUserGroups( String commandOutput ) {
    List<String> result = new ArrayList<>();
    int startIndex = 0;
    int endIndex;
    while ( true ) {

      startIndex = StringUtils.indexOfIgnoreCase( commandOutput, GROUP_NAME, startIndex );
      if ( startIndex < 0 ) {
        return result;
      }
      startIndex += GROUP_NAME.length();
      endIndex = StringUtils.indexOfIgnoreCase( commandOutput, RN_DELIMITER, startIndex );
      if ( endIndex < 0 ) {
        return result;
      }
      result.add( commandOutput.substring( startIndex, endIndex ).toUpperCase().trim() );
    }
  }

  /**
   * Get current user on remote host
   *
   * @return name of user on remote host
   * @throws JSchException
   * @throws IOException
   */
  String getUser() throws JSchException, IOException {
    StringBuilder output = new StringBuilder();
    int code = this.executeCommand( WHO_AMI, output );
    if ( code != 0 ) {
      throw new JSchException( "Could not get user name on remote host (error code: " + code + ")" );
    }

    return output.toString().trim().toUpperCase();
  }

  /**
   *
   * @param path path to file or directory
   *
   * @return Map Windows Group - permissions
   * @throws JSchException
   * @throws IOException
   */
  Map<String, String> getFilePermission( String path ) throws JSchException, IOException {
    String windowsAbsPath;
    if ( path.startsWith( WINDOWS_PATH_DELIMITER ) ) {
      //cut first "/" windows does not have it
      //it mean first letter is name of disk
      path = path.substring( WINDOWS_PATH_DELIMITER.length() );
      windowsAbsPath = path.substring( 0, 1 ) + ":" + path.substring( 1 );
    } else {
      windowsAbsPath = path;
    }
    Map<String, String> result = new HashMap<>();
    StringBuilder output = new StringBuilder();
    int code = this.executeCommand( ICACLS + windowsAbsPath, output );
    if ( code != 0 ) {
      return result;
    }
    String outputString = output.toString();

    int indexOf = outputString.indexOf( windowsAbsPath );
    if ( indexOf > -1 ) {
      outputString = outputString.substring( indexOf + windowsAbsPath.length() );
    }

    String[] strings = outputString.toUpperCase().split( N_DELIMITER );
    for ( String string : strings ) {
      int index = string.indexOf( ":" );
      if ( index > -1 ) {
        result.put( string.substring( 0, index ).trim(), string.substring( index + 1 ).trim() );
      }
    }
    return result;
  }

  /**
   * check is remote host is windows
   *
   * @return true if host windows
   * @throws JSchException
   * @throws IOException
   */
  boolean isRemoteHostWindows() throws JSchException, IOException {
    if ( this.windows == null ) {
      StringBuilder output = new StringBuilder();
      int code = this.executeCommand( VER, output );
      this.windows = code == 0 && output.toString().toUpperCase().contains( WINDOWS );
    }
    return this.windows;
  }


  /**
   * {@link  org.apache.commons.vfs2.provider.sftp.SftpFileSystem#getChannel() }
   *
   */
  private void ensureSession() throws FileSystemException {
    if ( this.session == null || !this.session.isConnected() ) {
      this.doCloseCommunicationLink();
      UserAuthenticationData authData = null;

      Session session;
      try {
        GenericFileName e = (GenericFileName) this.getRootName();
        authData = UserAuthenticatorUtils.authenticate( this.getFileSystemOptions(), SftpFileProvider.AUTHENTICATOR_TYPES );
        session = SftpClientFactory.createConnection( e.getHostName(), e.getPort(), UserAuthenticatorUtils.getData( authData, UserAuthenticationData.USERNAME,
                UserAuthenticatorUtils.toChar( e.getUserName() ) ), UserAuthenticatorUtils.getData( authData, UserAuthenticationData.PASSWORD,
                UserAuthenticatorUtils.toChar( e.getPassword() ) ), this.getFileSystemOptions() );
      } catch ( Exception var7 ) {
        throw new FileSystemException( "vfs.provider.sftp/connect.error", this.getRootName(), var7 );
      } finally {
        UserAuthenticatorUtils.cleanup( authData );
      }

      this.session = session;
    }

  }

  /**
   *
   * {@link  org.apache.commons.vfs2.provider.sftp.SftpFileSystem#executeCommand(java.lang.String, java.lang.StringBuilder) }
   */
  private int executeCommand( String command, StringBuilder output ) throws JSchException, IOException {
    this.ensureSession();
    ChannelExec channel = (ChannelExec) this.session.openChannel( "exec" );
    channel.setCommand( command );
    channel.setInputStream( (InputStream) null );
    InputStreamReader stream = new InputStreamReader( channel.getInputStream() );
    channel.setErrStream( System.err, true );
    channel.connect();
    char[] buffer = new char[128];

    int read;
    while ( ( read = stream.read( buffer, 0, buffer.length ) ) >= 0 ) {
      output.append( buffer, 0, read );
    }

    stream.close();

    while ( !channel.isClosed() ) {
      try {
        Thread.sleep( 100L );
      } catch ( Exception exc ) {
        log.logMinimal( "Warning: Error session closing. " + exc.getMessage() );
      }
    }

    channel.disconnect();
    return channel.getExitStatus();
  }
}
