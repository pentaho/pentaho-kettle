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

package org.pentaho.di.trans.steps.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;

@RunWith( MockitoJUnitRunner.class )
public class SSHDataTest {

  @Mock
  private SshConnectionFactory mockFactory;
  @Mock
  private SshConnection sshConnection;
  @Mock
  private FileObject fileObject;
  @Mock
  private FileContent fileContent;
  @Mock
  private VariableSpace variableSpace;
  @Mock
  private IKettleVFS mockKettleVFS;

  private MockedStatic<SshConnectionFactory> factoryMockedStatic;
  private MockedStatic<KettleVFS> kettleVFSMockedStatic;

  // Test data
  private static final String server = "localhost";
  private static final int port = 22;
  private static final String username = "testuser";
  private static final String password = "testpass";
  private static final String keyFilePath = "/path/to/key.pem";
  private static final String passPhrase = "keypassphrase";
  private static final String proxyHost = "proxy.example.com";
  private static final int proxyPort = 8080;
  private static final String proxyUsername = "proxyuser";
  private static final String proxyPassword = "proxypass";

  @Before
  public void setUp() throws Exception {
    // Mock the static factory
    factoryMockedStatic = mockStatic( SshConnectionFactory.class );
    factoryMockedStatic.when( SshConnectionFactory::defaultFactory ).thenReturn( mockFactory );
    when( mockFactory.open( any( SshConfig.class ) ) ).thenReturn( sshConnection );

    // Mock KettleVFS for key file operations
    kettleVFSMockedStatic = mockStatic( KettleVFS.class );
    kettleVFSMockedStatic.when( () -> KettleVFS.getInstance( any() ) ).thenReturn( mockKettleVFS );
    when( mockKettleVFS.getFileObject( keyFilePath ) ).thenReturn( fileObject );
  }

  @After
  public void tearDown() {
    factoryMockedStatic.close();
    kettleVFSMockedStatic.close();
  }

  @Test
  public void testOpenSshConnection_Password() throws Exception {
    // Test successful password authentication
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .build();
    SshConnection result = SSHData.openSshConnection( params );
    assertNotNull( "Should return a valid connection adapter", result );
  }

  @Test( expected = KettleException.class )
  public void testOpenSshConnection_InvalidKey() throws Exception {
    // Test with key file that doesn't exist
    when( fileObject.exists() ).thenReturn( false );
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .useKey( true )
        .keyFilename( keyFilePath )
        .build();
    SSHData.openSshConnection( params );
  }

  @Test
  public void testOpenSshConnection_WithKey() throws Exception {
    // Test successful key authentication
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[] { 1, 2, 3, 4, 5 } ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( keyFilePath )
        .passPhrase( passPhrase )
        .space( variableSpace )
        .build();
    SshConnection result = SSHData.openSshConnection( params );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testOpenSshConnection_WithProxy() throws Exception {
    // Test connection with proxy settings
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .proxyhost( proxyHost )
        .proxyport( proxyPort )
        .proxyusername( proxyUsername )
        .proxypassword( proxyPassword )
        .build();
    SshConnection result = SSHData.openSshConnection( params );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testOpenSshConnection_WithTimeout() throws Exception {
    // Test connection with custom timeout
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .timeOut( 100 )
        .build();
    SshConnection result = SSHData.openSshConnection( params );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testPasswordAuthentication_ConfigurationVerification() throws Exception {
    // Test that password authentication sets correct SshConfig values
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Host should be set", server, config.getHost() );
    assertEquals( "Port should be set", port, config.getPort() );
    assertEquals( "Username should be set", username, config.getUsername() );
    assertEquals( "Password should be set", password, config.getPassword() );
    assertEquals( "Auth type should be PASSWORD", SshConfig.AuthType.PASSWORD, config.getAuthType() );
  }

  @Test
  public void testPasswordAuthentication_WithCustomTimeout() throws Exception {
    int customTimeout = 60; // seconds

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .timeOut( customTimeout )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Timeout should be set in milliseconds", customTimeout * 1000L, config.getConnectTimeoutMillis() );
  }

  @Test
  public void testPasswordAuthentication_DefaultTimeout() throws Exception {
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .timeOut( 0 )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Should use default timeout of 30 seconds", 30000L, config.getConnectTimeoutMillis() );
  }

  @Test
  public void testPublicKeyAuthentication_WithPassphrase() throws Exception {
    // Setup key file and passphrase
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( "fake-key-content".getBytes() ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( keyFilePath )
        .passPhrase( passPhrase )
        .space( variableSpace )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Auth type should be PUBLIC_KEY", SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertEquals( "Passphrase should be set", passPhrase, config.getPassphrase() );
    assertNotNull( "Key content should be set", config.getKeyContent() );
    assertNull( "Key path should be null (using secure in-memory approach)", config.getKeyPath() );
  }

  @Test
  public void testPublicKeyAuthentication_WithoutPassphrase() throws Exception {
    // Test key authentication without passphrase
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( "ssh-rsa AAAAB3...".getBytes() ) );

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( keyFilePath )
        .space( variableSpace )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Auth type should be PUBLIC_KEY", SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertNull( "Passphrase should be null", config.getPassphrase() );
    assertNotNull( "Key content should be set", config.getKeyContent() );
    assertNull( "Key path should be null (using secure in-memory approach)", config.getKeyPath() );
  }

  @Test( expected = KettleException.class )
  public void testPublicKeyAuthentication_MissingKeyFile() throws Exception {
    // Test with empty key file path
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( "" )
        .build();
    SSHData.openSshConnection( params );
  }

  @Test( expected = KettleException.class )
  public void testPublicKeyAuthentication_NullKeyFile() throws Exception {
    // Test with null key file path
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( null )
        .build();
    SSHData.openSshConnection( params );
  }

  @Test( expected = KettleException.class )
  public void testPublicKeyAuthentication_FileReadError() throws Exception {
    // Test error reading key file
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenThrow( new RuntimeException( "VFS error accessing file" ) );

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( keyFilePath )
        .build();
    SSHData.openSshConnection( params );
  }

  @Test
  public void testProxyConfiguration_HostAndPort() throws Exception {
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .proxyhost( proxyHost )
        .proxyport( proxyPort )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Proxy host should be set", proxyHost, config.getProxyHost() );
    assertEquals( "Proxy port should be set", proxyPort, config.getProxyPort() );
  }

  @Test
  public void testProxyConfiguration_WithAuthentication() throws Exception {
    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .proxyhost( proxyHost )
        .proxyport( proxyPort )
        .proxyusername( proxyUsername )
        .proxypassword( proxyPassword )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Proxy host should be set", proxyHost, config.getProxyHost() );
    assertEquals( "Proxy port should be set", proxyPort, config.getProxyPort() );
    assertEquals( "Proxy username should be set", proxyUsername, config.getProxyUser() );
    assertEquals( "Proxy password should be set", proxyPassword, config.getProxyPassword() );
  }

  @Test
  public void testCompleteConfiguration_KeyWithProxyAndPassphrase() throws Exception {
    // Test key authentication with proxy and passphrase
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( "ssh-rsa AAAAB3...".getBytes() ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .useKey( true )
        .keyFilename( keyFilePath )
        .passPhrase( passPhrase )
        .timeOut( 90 )
        .space( variableSpace )
        .proxyhost( proxyHost )
        .proxyport( proxyPort )
        .proxyusername( proxyUsername )
        .proxypassword( proxyPassword )
        .build();
    SSHData.openSshConnection( params );

    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );

    SshConfig config = configCaptor.getValue();
    assertEquals( "Auth type should be PUBLIC_KEY", SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertNotNull( "Key content should be set", config.getKeyContent() );
    assertNull( "Key path should be null (using secure in-memory approach)", config.getKeyPath() );
    assertEquals( "Passphrase should be set", passPhrase, config.getPassphrase() );
    assertEquals( "Timeout should be set", 90000L, config.getConnectTimeoutMillis() );
    assertEquals( "Proxy host should be set", proxyHost, config.getProxyHost() );
    assertEquals( "Proxy port should be set", proxyPort, config.getProxyPort() );
    assertEquals( "Proxy username should be set", proxyUsername, config.getProxyUser() );
    assertEquals( "Proxy password should be set", proxyPassword, config.getProxyPassword() );
  }

  @Test( expected = KettleException.class )
  public void testConnectionFailure_FactoryThrowsException() throws Exception {
    // Test when SSH factory throws exception
    when( mockFactory.open( any() ) ).thenThrow( new RuntimeException( "Connection failed" ) );

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .build();
    SSHData.openSshConnection( params );
  }

  @Test( expected = KettleException.class )
  public void testConnectionFailure_ConnectThrowsException() throws Exception {
    // Test when connection.connect() throws exception
    doThrow( new RuntimeException( "Connect failed" ) ).when( sshConnection ).connect();

    SshConnectionParameters params = SshConnectionParameters.builder()
        .bowl( DefaultBowl.getInstance() )
        .server( server )
        .port( port )
        .username( username )
        .password( password )
        .useKey( false )
        .build();
    SSHData.openSshConnection( params );
  }

  @Test
  public void testConnectionCleanup_OnError() throws Exception {
    // Test that connection is properly closed when an error occurs
    doThrow( new RuntimeException( "Connect failed" ) ).when( sshConnection ).connect();

    try {
      SshConnectionParameters params = SshConnectionParameters.builder()
          .bowl( DefaultBowl.getInstance() )
          .server( server )
          .port( port )
          .username( username )
          .password( password )
          .useKey( false )
          .build();
      SSHData.openSshConnection( params );
      fail( "Should have thrown KettleException" );
    } catch ( KettleException e ) {
      // Expected
    }

    verify( sshConnection ).close();
  }
}
