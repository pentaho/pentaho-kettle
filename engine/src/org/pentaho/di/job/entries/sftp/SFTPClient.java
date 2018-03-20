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

package org.pentaho.di.job.entries.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.vfs.KettleVFS;

import com.google.common.annotations.VisibleForTesting;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SFTPClient {

  private static final String COMPRESSION_S2C = "compression.s2c";
  private static final String COMPRESSION_C2S = "compression.c2s";

  public static final String PROXY_TYPE_SOCKS5 = "SOCKS5";
  public static final String PROXY_TYPE_HTTP = "HTTP";
  public static final String HTTP_DEFAULT_PORT = "80";
  public static final String SOCKS5_DEFAULT_PORT = "1080";
  public static final int SSH_DEFAULT_PORT = 22;

  // -D parameter telling whether we should use GSSAPI authentication or not
  static final String ENV_PARAM_USERAUTH_GSSAPI = "userauth.gssapi.enabled";

  private static final String PREFERRED_AUTH_CONFIG_NAME = "PreferredAuthentications";
  private static final String PREFERRED_AUTH_DEFAULT = "publickey,keyboard-interactive,password";
  // adding GSSAPI to be the last one
  private static final String PREFERRED_AUTH_WITH_GSSAPI = PREFERRED_AUTH_DEFAULT + ",gssapi-with-mic";

  private InetAddress serverIP;
  private int serverPort;
  private String userName;
  private String password;
  private String prvkey = null; // Private key
  private String passphrase = null; // Empty passphrase for now
  private String compression = null;

  private Session s;
  private ChannelSftp c;

  /**
   * Init Helper Class with connection settings
   *
   * @param serverIP
   *          IP address of remote server
   * @param serverPort
   *          port of remote server
   * @param userName
   *          username of remote server
   * @throws KettleJobException
   */
  public SFTPClient( InetAddress serverIP, int serverPort, String userName ) throws KettleJobException {
    this( serverIP, serverPort, userName, null, null );
  }

  /**
   * Init Helper Class with connection settings
   *
   * @param serverIP
   *          IP address of remote server
   * @param serverPort
   *          port of remote server
   * @param userName
   *          username of remote server
   * @param privateKeyFilename
   *          filename of private key
   * @throws KettleJobException
   */
  public SFTPClient( InetAddress serverIP, int serverPort, String userName, String privateKeyFilename ) throws KettleJobException {
    this( serverIP, serverPort, userName, privateKeyFilename, null );
  }

  /**
   * Init Helper Class with connection settings
   *
   * @param serverIP
   *          IP address of remote server
   * @param serverPort
   *          port of remote server
   * @param userName
   *          username of remote server
   * @param privateKeyFilename
   *          filename of private key
   * @param passPhrase
   *          passphrase
   * @throws KettleJobException
   */
  public SFTPClient( InetAddress serverIP, int serverPort, String userName, String privateKeyFilename,
    String passPhrase ) throws KettleJobException {

    if ( serverIP == null || serverPort < 0 || userName == null || userName.equals( "" ) ) {
      throw new KettleJobException(
        "For a SFTP connection server name and username must be set and server port must be greater than zero." );
    }

    this.serverIP = serverIP;
    this.serverPort = serverPort;
    this.userName = userName;

    JSch jsch = createJSch();
    try {
      if ( !Utils.isEmpty( privateKeyFilename ) ) {
        // We need to use private key authentication
        this.prvkey = privateKeyFilename;
        byte[] passphrasebytes = new byte[0];
        if ( !Utils.isEmpty( passPhrase ) ) {
          // Set passphrase
          this.passphrase = passPhrase;
          passphrasebytes = GetPrivateKeyPassPhrase().getBytes();
        }
        jsch.addIdentity( getUserName(), FileUtil.getContent( KettleVFS.getFileObject( prvkey ) ), // byte[] privateKey
          null, // byte[] publicKey
          passphrasebytes ); // byte[] passPhrase
      }
      s = jsch.getSession( userName, serverIP.getHostAddress(), serverPort );
      s.setConfig( PREFERRED_AUTH_CONFIG_NAME, getPreferredAuthentications() );
    } catch ( IOException e ) {
      throw new KettleJobException( e );
    } catch ( KettleFileException e ) {
      throw new KettleJobException( e );
    } catch ( JSchException e ) {
      throw new KettleJobException( e );
    }
  }

  public void login( String password ) throws KettleJobException {
    this.password = password;

    s.setPassword( this.getPassword() );
    try {
      java.util.Properties config = new java.util.Properties();
      config.put( "StrictHostKeyChecking", "no" );
      // set compression property
      // zlib, none
      String compress = getCompression();
      if ( compress != null ) {
        config.put( COMPRESSION_S2C, compress );
        config.put( COMPRESSION_C2S, compress );
      }
      s.setConfig( config );
      s.connect();
      Channel channel = s.openChannel( "sftp" );
      channel.connect();
      c = (ChannelSftp) channel;
    } catch ( JSchException e ) {
      throw new KettleJobException( e );
    }
  }

  public void chdir( String dirToChangeTo ) throws KettleJobException {
    try {
      c.cd( dirToChangeTo );
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }
  }

  public String[] dir() throws KettleJobException {
    String[] fileList = null;

    try {
      java.util.Vector<?> v = c.ls( "." );
      java.util.Vector<String> o = new java.util.Vector<String>();
      if ( v != null ) {
        for ( int i = 0; i < v.size(); i++ ) {
          Object obj = v.elementAt( i );
          if ( obj != null && obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry ) {
            LsEntry lse = (com.jcraft.jsch.ChannelSftp.LsEntry) obj;
            if ( !lse.getAttrs().isDir() ) {
              o.add( lse.getFilename() );
            }
          }
        }
      }
      if ( o.size() > 0 ) {
        fileList = new String[o.size()];
        o.copyInto( fileList );
      }
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }

    return fileList;
  }

  public void get( FileObject localFile, String remoteFile ) throws KettleJobException {
    OutputStream localStream = null;
    try {
      localStream = KettleVFS.getOutputStream( localFile, false );
      c.get( remoteFile, localStream );
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    } catch ( IOException e ) {
      throw new KettleJobException( e );
    } finally {
      if ( localStream != null ) {
        try {
          localStream.close();
        } catch ( IOException ignore ) {
          // Ignore any IOException, as we're trying to close the stream anyways
        }
      }
    }
  }

  /**
   * @deprecated use {@link #get(FileObject, String)}
   * @param localFilePath
   * @param remoteFile
   * @throws KettleJobException
   */
  @Deprecated
  public void get( String localFilePath, String remoteFile ) throws KettleJobException {
    int mode = ChannelSftp.OVERWRITE;
    try {
      c.get( remoteFile, localFilePath, null, mode );
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }
  }

  public String pwd() throws KettleJobException {
    try {
      return c.pwd();
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }
  }

  public void put( FileObject fileObject, String remoteFile ) throws KettleJobException {
    int mode = ChannelSftp.OVERWRITE;
    InputStream inputStream = null;
    try {
      inputStream = KettleVFS.getInputStream( fileObject );
      c.put( inputStream, remoteFile, null, mode );
    } catch ( Exception e ) {
      throw new KettleJobException( e );
    } finally {
      if ( inputStream != null ) {
        try {
          inputStream.close();
        } catch ( IOException e ) {
          throw new KettleJobException( e );
        }
      }
    }
  }

  public void put( InputStream inputStream, String remoteFile ) throws KettleJobException {
    int mode = ChannelSftp.OVERWRITE;

    try {
      c.put( inputStream, remoteFile, null, mode );
    } catch ( Exception e ) {
      throw new KettleJobException( e );
    } finally {
      if ( inputStream != null ) {
        try {
          inputStream.close();
        } catch ( IOException e ) {
          throw new KettleJobException( e );
        }
      }
    }
  }

  public void delete( String file ) throws KettleJobException {
    try {
      c.rm( file );
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }
  }

  /**
   * Creates this file as a folder.
   */
  public void createFolder( String foldername ) throws KettleJobException {
    try {
      c.mkdir( foldername );
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }
  }

  /**
   * Rename the file.
   */
  public void renameFile( String sourcefilename, String destinationfilename ) throws KettleJobException {
    try {
      c.rename( sourcefilename, destinationfilename );
    } catch ( SftpException e ) {
      throw new KettleJobException( e );
    }
  }

  public FileType getFileType( String filename ) throws KettleJobException {
    try {
      SftpATTRS attrs = c.stat( filename );
      if ( attrs == null ) {
        return FileType.IMAGINARY;
      }

      if ( ( attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS ) == 0 ) {
        throw new KettleJobException( "Unknown permissions error" );
      }

      if ( attrs.isDir() ) {
        return FileType.FOLDER;
      } else {
        return FileType.FILE;
      }
    } catch ( Exception e ) {
      throw new KettleJobException( e );
    }
  }

  public boolean folderExists( String foldername ) {
    boolean retval = false;
    try {
      SftpATTRS attrs = c.stat( foldername );
      if ( attrs == null ) {
        return false;
      }

      if ( ( attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS ) == 0 ) {
        throw new KettleJobException( "Unknown permissions error" );
      }

      retval = attrs.isDir();
    } catch ( Exception e ) {
      // Folder can not be found!
    }
    return retval;
  }

  public void setProxy( String host, String port, String user, String pass, String proxyType ) throws KettleJobException {

    if ( Utils.isEmpty( host ) || Const.toInt( port, 0 ) == 0 ) {
      throw new KettleJobException( "Proxy server name must be set and server port must be greater than zero." );
    }
    Proxy proxy = null;
    String proxyhost = host + ":" + port;

    if ( proxyType.equals( PROXY_TYPE_HTTP ) ) {
      proxy = new ProxyHTTP( proxyhost );
      if ( !Utils.isEmpty( user ) ) {
        ( (ProxyHTTP) proxy ).setUserPasswd( user, pass );
      }
    } else if ( proxyType.equals( PROXY_TYPE_SOCKS5 ) ) {
      proxy = new ProxySOCKS5( proxyhost );
      if ( !Utils.isEmpty( user ) ) {
        ( (ProxySOCKS5) proxy ).setUserPasswd( user, pass );
      }
    }
    s.setProxy( proxy );
  }

  public void disconnect() {
    if ( c != null ) {
      c.disconnect();
    }
    if ( s != null ) {
      s.disconnect();
    }
  }

  public String GetPrivateKeyFileName() {
    return this.prvkey;
  }

  public String GetPrivateKeyPassPhrase() {
    return this.passphrase;
  }

  public String getPassword() {
    return password;
  }

  public int getServerPort() {
    return serverPort;
  }

  public String getUserName() {
    return userName;
  }

  public InetAddress getServerIP() {
    return serverIP;
  }

  public void setCompression( String compression ) {
    this.compression = compression;
  }

  public String getCompression() {
    if ( this.compression == null ) {
      return null;
    }
    if ( this.compression.equals( "zlib" ) ) {
      // compatibility with OpenSSH implementation of delayed compression
      // https://www.openssh.com/txt/draft-miller-secsh-compression-delayed-00.txt
      return "zlib@openssh.com,zlib";
    }
    if ( this.compression.equals( "none" ) ) {
      return null;
    }
    return this.compression;
  }

  @VisibleForTesting
  JSch createJSch() {
    return new JSch();
  }

  /**
   * Whether we should use GSSAPI when authenticating or not.
   */
  private String getPreferredAuthentications() {
    String param = Const.getEnvironmentVariable( ENV_PARAM_USERAUTH_GSSAPI, null );
    return Boolean.valueOf( param ) ? PREFERRED_AUTH_WITH_GSSAPI : PREFERRED_AUTH_DEFAULT;
  }
}
