/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SFTPClientTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private final int port = 22;
  private final String username = "admin";
  private final String password = "password";
  private final Session session = mock( Session.class );
  private final ChannelSftp channel = mock( ChannelSftp.class );
  private final InetAddress server = mock( InetAddress.class );
  private final JSch jSch = mock( JSch.class );

  @Before
  public void setUp() throws JSchException {
    System.clearProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI );

    when( server.getHostAddress() ).thenReturn( "localhost" );
    when( jSch.getSession( username, "localhost", port ) ).thenReturn( session );
    when( session.openChannel( "sftp" ) ).thenReturn( channel );
  }

  @After
  public void tearDown() {
    System.clearProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI );
  }

  /**
   * Given SFTP connection configuration, and -Duserauth.gssapi.enabled param was NOT passed on application start.
   * <br/>
   * When SFTP Client is instantiated, then preferred authentications list should not contain
   * GSS API Authentication.
   */
  @Test
  public void shouldExcludeGssapiFromPreferredAuthenticationsByDefault() throws Exception {
    new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password" );
  }

  /**
   * Given SFTP connection configuration, and -Duserauth.gssapi.enabled param
   * was passed on application start with correct value.
   * <br/>
   * When SFTP Client is instantiated, then preferred authentications list should contain
   * GSS API Authentication as the last one.
   */
  @Test
  public void shouldIncludeGssapiToPreferredAuthenticationsIfSpecified() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "true" );

    new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password,gssapi-with-mic" );
  }

  /**
   * Given SFTP connection configuration, and -Duserauth.gssapi.enabled param
   * was passed on application start with incorrect value.
   * <br/>
   * When SFTP Client is instantiated, then preferred authentications list should not contain
   * GSS API Authentication.
   */
  @Test
  public void shouldIncludeGssapiToPreferredAuthenticationsIfOnlySpecifiedCorrectly() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );

    new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password" );
  }

  /**
   * Can't create root folder. An exception is expected.
   */
  @Test( expected = KettleJobException.class )
  public void folderCreationEmptyTest() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );
    SFTPClient client = new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    client.login( password );
    client.createFolder( "//" );
  }

  /**
   * Create a folder under the current user's home.
   */
  @Test
  public void folderCreation_Relative_Simple() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );
    SFTPClient client = spy( new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    } );

    doReturn( "/home/admin" ).when( client ).pwd();

    client.login( password );
    client.createFolder( "myfolder" );

    verify( channel, times( 1 ) ).mkdir( anyString() );
    verify( channel, times( 1 ) ).mkdir( "/home/admin/myfolder" );
  }

  /**
   * Create a folder with nested folders under the current user's home.
   */
  @Test
  public void folderCreation_Relative_Nested() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );
    SFTPClient client = spy( new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    } );

    doReturn( "/home/admin" ).when( client ).pwd();

    client.login( password );
    client.createFolder( "myfolder/subfolder/finalfolder" );

    verify( channel, times( 3 ) ).mkdir( anyString() );
    verify( channel, times( 1 ) ).mkdir( "/home/admin/myfolder" );
    verify( channel, times( 1 ) ).mkdir( "/home/admin/myfolder/subfolder" );
    verify( channel, times( 1 ) ).mkdir( "/home/admin/myfolder/subfolder/finalfolder" );
  }

  /**
   * Create a folder under an existing folder given an absolute path.
   */
  @Test
  public void folderCreation_Absolute_Simple() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );
    SFTPClient client = spy( new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    } );

    doReturn( true ).when( client ).folderExists( "/var" );
    doReturn( true ).when( client ).folderExists( "/var/ftproot" );

    client.login( password );
    client.createFolder( "/var/ftproot/myfolder" );

    verify( channel, times( 1 ) ).mkdir( anyString() );
    verify( channel, times( 1 ) ).mkdir( "/var/ftproot/myfolder" );
  }

  /**
   * Create a folder under an existing folder given an absolute path.
   * The specified folder ends with a slash.
   */
  @Test
  public void folderCreation_Absolute_TrailingSlash() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );
    SFTPClient client = spy( new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    } );

    doReturn( true ).when( client ).folderExists( "/var" );
    doReturn( true ).when( client ).folderExists( "/var/ftproot" );

    client.login( password );
    client.createFolder( "/var/ftproot/myfolder/" );

    verify( channel, times( 1 ) ).mkdir( anyString() );
    verify( channel, times( 1 ) ).mkdir( "/var/ftproot/myfolder" );
  }

  /**
   * Create a folder with nested folders under an existing folder given an absolute path.
   */
  @Test
  public void folderCreation_Absolute_Nested() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );
    SFTPClient client = spy( new SFTPClient( server, port, username ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    } );

    doReturn( true ).when( client ).folderExists( "/var" );
    doReturn( true ).when( client ).folderExists( "/var/ftproot" );

    client.login( password );
    client.createFolder( "/var/ftproot/myfolder/subfolder/finalfolder" );

    verify( channel, times( 3 ) ).mkdir( anyString() );
    verify( channel, times( 1 ) ).mkdir( "/var/ftproot/myfolder" );
    verify( channel, times( 1 ) ).mkdir( "/var/ftproot/myfolder/subfolder" );
    verify( channel, times( 1 ) ).mkdir( "/var/ftproot/myfolder/subfolder/finalfolder" );
  }

  @Test
  public void testDir() throws Exception {
    Vector<ChannelSftp.LsEntry> files = new Vector<>();
    files.add( mockLsEntry("file_1", false ) );
    files.add( mockLsEntry("a_dir", true ) );

    when( channel.ls( anyString() ) ).thenReturn( files );

    SFTPClient client = spyClient();
    client.login( password );

    String[] dirs = client.dir();

    assertEquals( 1, dirs.length );
    assertEquals( "file_1", dirs[0] );
  }

  @Test
  public void testDirNoFiles() throws Exception {
    Vector<ChannelSftp.LsEntry> files = new Vector<>();
    files.add( mockLsEntry("a_dir", true ) );

    when( channel.ls( anyString() ) ).thenReturn( files );

    SFTPClient client = spyClient();
    client.login( password );

    assertNull( client.dir() );
  }


  @Test
  public void testReadKeyFile() throws Exception {
    FileObject pk = KettleVFS.createTempFile( "pk", KettleVFS.Suffix.TMP );
    try {
      byte[] pk_bytes = new byte[] { 1, 3, 1 };
      try ( OutputStream out = pk.getContent().getOutputStream() ) {
        out.write( pk_bytes );
      }

      // just call ctor
      spyClient( pk.getName().getPath() );
      verify( jSch ).addIdentity( username, pk_bytes, null, new byte[0] );
    } finally {
      pk.delete();
    }
  }

  private SFTPClient spyClient() throws KettleException {
    return spyClient( null );
  }

  private SFTPClient spyClient( String privateKeyFilename ) throws KettleException {
    return spy( new SFTPClient( server, port, username, privateKeyFilename ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    } );
  }

  private static ChannelSftp.LsEntry mockLsEntry( String fileName, boolean isDir ) {
    SftpATTRS attr =  mock( SftpATTRS.class );
    when( attr.isDir() ).thenReturn( isDir );
    ChannelSftp.LsEntry entry = mock( ChannelSftp.LsEntry.class );
    when( entry.getAttrs() ).thenReturn( attr );
    when( entry.getFilename() ).thenReturn( fileName );
    return entry;
  }
}
