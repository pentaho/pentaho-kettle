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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ssh.SshConfig;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;


public class SSHDataTest {

  SshConnection sshConnection;
  SshConnectionFactory mockFactory;

  FileObject fileObject;

  FileContent fileContent;

  VariableSpace variableSpace;

  MockedStatic<SshConnectionFactory> sshConnectionFactoryMockedStatic;
  MockedStatic<KettleVFS> kettleVFSMockedStatic;

  String server = "testServerUrl";
  String keyFilePath = "keyFilePath";
  String passPhrase = "passPhrase";
  String username = "username";
  String password = "password";
  String proxyUsername = "proxyUsername";
  String proxyPassword = "proxyPassword";
  String proxyHost = "proxyHost";
  int port = 22;
  int proxyPort = 23;

  @Before
  public void setup() throws Exception {
    sshConnection = mock( SshConnection.class );
    fileObject = mock( FileObject.class );
    fileContent = mock( FileContent.class );
    variableSpace = mock( VariableSpace.class );
    mockFactory = mock( SshConnectionFactory.class );

    sshConnectionFactoryMockedStatic = mockStatic( SshConnectionFactory.class );
    kettleVFSMockedStatic = mockStatic( KettleVFS.class );

    // Mock the static defaultFactory method to return our mock factory
    sshConnectionFactoryMockedStatic.when( SshConnectionFactory::defaultFactory ).thenReturn( mockFactory );

    // Mock the factory to return our mock SSH connection
    when( mockFactory.open( any() ) ).thenReturn( sshConnection );

    IKettleVFS ikettleVFS = mock( IKettleVFS.class );
    when( ikettleVFS.getFileObject( keyFilePath ) ).thenReturn( fileObject );
    when( KettleVFS.getInstance( any() ) ).thenReturn( ikettleVFS );
  }

  @After
  public void tearDown() {
    sshConnectionFactoryMockedStatic.close();
    kettleVFSMockedStatic.close();
  }

  @Test
  public void testOpenSshConnection_Password() throws Exception {
    // Test successful password authentication
    SshConnection result = SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, false, null,
      null, 0, null, null, 0, null, null );
    assertNotNull( "Should return a valid connection adapter", result );
  }

  @Test( expected = KettleException.class )
  public void testOpenSshConnection_InvalidKey() throws Exception {
    // Test with key file that doesn't exist
    when( fileObject.exists() ).thenReturn( false );
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, null, null, true, keyFilePath,
      null, 0, null, null, 0, null, null );
  }

  @Test
  public void testOpenSshConnection_WithKey() throws Exception {
    // Test successful key authentication
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getSize() ).thenReturn( 1000L );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[] { 1, 2, 3, 4, 5 } ) );
    when( variableSpace.environmentSubstitute( keyFilePath ) ).thenReturn( keyFilePath );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );

    SshConnection result = SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, true, keyFilePath,
      passPhrase, 0, variableSpace, null, 0, null, null );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testOpenSshConnection_WithProxy() throws Exception {
    // Test connection with proxy settings
    SshConnection result = SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, false, null,
      null, 0, null, proxyHost, proxyPort, proxyUsername, proxyPassword );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testOpenSshConnection_WithTimeout() throws Exception {
    // Test connection with custom timeout
    SshConnection result = SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, false, null,
      null, 100, null, null, 0, null, null );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  // ================================
  // ADDITIONAL COMPREHENSIVE TESTS
  // ================================

  @Test
  public void testPasswordAuthentication_ConfigurationVerification() throws Exception {
    // Test that password authentication sets correct SshConfig values
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, null, 0, null, null );
    
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
    
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, customTimeout, null, null, 0, null, null );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Timeout should be set in milliseconds", customTimeout * 1000L, config.getConnectTimeoutMillis() );
  }

  @Test
  public void testPasswordAuthentication_DefaultTimeout() throws Exception {
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, null, 0, null, null );
    
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
    when( fileContent.getSize() ).thenReturn( 1000L );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( "fake-key-content".getBytes() ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );

    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, 
        true, keyFilePath, passPhrase, 0, variableSpace, null, 0, null, null );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Auth type should be PUBLIC_KEY", SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertEquals( "Passphrase should be set", passPhrase, config.getPassphrase() );
    assertNotNull( "Key path should be set", config.getKeyPath() );
  }

  @Test
  public void testPublicKeyAuthentication_WithoutPassphrase() throws Exception {
    // Test key authentication without passphrase
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getSize() ).thenReturn( 2048L );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( "ssh-rsa AAAAB3...".getBytes() ) );

    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, 
        true, keyFilePath, null, 0, variableSpace, null, 0, null, null );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Auth type should be PUBLIC_KEY", SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertNull( "Passphrase should be null", config.getPassphrase() );
    assertNotNull( "Key path should be set", config.getKeyPath() );
  }

  @Test( expected = KettleException.class )
  public void testPublicKeyAuthentication_MissingKeyFile() throws Exception {
    // Test with empty key file path
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, 
        true, "", null, 0, null, null, 0, null, null );
  }

  @Test( expected = KettleException.class )
  public void testPublicKeyAuthentication_NullKeyFile() throws Exception {
    // Test with null key file path
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, 
        true, null, null, 0, null, null, 0, null, null );
  }

  @Test( expected = KettleException.class )
  public void testPublicKeyAuthentication_FileReadError() throws Exception {
    // Test error reading key file
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenThrow( new RuntimeException( "VFS error accessing file" ) );

    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, 
        true, keyFilePath, null, 0, null, null, 0, null, null );
  }

  @Test
  public void testProxyConfiguration_HostAndPort() throws Exception {
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, proxyHost, proxyPort, null, null );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Proxy host should be set", proxyHost, config.getProxyHost() );
    assertEquals( "Proxy port should be set", proxyPort, config.getProxyPort() );
  }

  @Test
  public void testProxyConfiguration_WithAuthentication() throws Exception {
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, proxyHost, proxyPort, proxyUsername, proxyPassword );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Proxy host should be set", proxyHost, config.getProxyHost() );
    assertEquals( "Proxy port should be set", proxyPort, config.getProxyPort() );
    assertEquals( "Proxy username should be set", proxyUsername, config.getProxyUser() );
    assertEquals( "Proxy password should be set", proxyPassword, config.getProxyPassword() );
  }

  @Test
  public void testProxyConfiguration_PartialCredentials() throws Exception {
    // Test with proxy username but no password
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, proxyHost, proxyPort, proxyUsername, null );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Proxy username should be set", proxyUsername, config.getProxyUser() );
    assertNull( "Proxy password should be null", config.getProxyPassword() );
  }

  @Test
  public void testProxyConfiguration_EmptyHost() throws Exception {
    // Test with empty proxy host (should not configure proxy)
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, "", proxyPort, proxyUsername, proxyPassword );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertNull( "Proxy host should not be set for empty host", config.getProxyHost() );
  }

  @Test( expected = KettleException.class )
  public void testConnectionFailure_FactoryThrowsException() throws Exception {
    // Test when SSH factory throws exception
    when( mockFactory.open( any() ) ).thenThrow( new RuntimeException( "Connection failed" ) );
    
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, null, 0, null, null );
  }

  @Test( expected = KettleException.class )
  public void testConnectionFailure_ConnectThrowsException() throws Exception {
    // Test when connection.connect() throws exception
    doThrow( new RuntimeException( "Connect failed" ) ).when( sshConnection ).connect();
    
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 0, null, null, 0, null, null );
  }

  @Test
  public void testConnectionCleanup_OnError() throws Exception {
    // Test that connection is properly closed when an error occurs
    doThrow( new RuntimeException( "Connect failed" ) ).when( sshConnection ).connect();
    
    try {
      SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
          false, null, null, 0, null, null, 0, null, null );
      fail( "Should have thrown KettleException" );
    } catch ( KettleException e ) {
      // Expected
    }
    
    verify( sshConnection ).close();
  }

  @Test
  public void testCompleteConfiguration_PasswordWithProxy() throws Exception {
    // Test password authentication with full proxy configuration
    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, password, 
        false, null, null, 120, null, proxyHost, proxyPort, proxyUsername, proxyPassword );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Host should be set", server, config.getHost() );
    assertEquals( "Port should be set", port, config.getPort() );
    assertEquals( "Username should be set", username, config.getUsername() );
    assertEquals( "Password should be set", password, config.getPassword() );
    assertEquals( "Auth type should be PASSWORD", SshConfig.AuthType.PASSWORD, config.getAuthType() );
    assertEquals( "Timeout should be set", 120000L, config.getConnectTimeoutMillis() );
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
    when( fileContent.getSize() ).thenReturn( 2048L );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( "ssh-rsa AAAAB3...".getBytes() ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );

    SSHData.openSshConnection( DefaultBowl.getInstance(), server, port, username, null, 
        true, keyFilePath, passPhrase, 90, variableSpace, proxyHost, proxyPort, proxyUsername, proxyPassword );
    
    ArgumentCaptor<SshConfig> configCaptor = ArgumentCaptor.forClass( SshConfig.class );
    verify( mockFactory ).open( configCaptor.capture() );
    
    SshConfig config = configCaptor.getValue();
    assertEquals( "Auth type should be PUBLIC_KEY", SshConfig.AuthType.PUBLIC_KEY, config.getAuthType() );
    assertNotNull( "Key path should be set", config.getKeyPath() );
    assertEquals( "Passphrase should be set", passPhrase, config.getPassphrase() );
    assertEquals( "Timeout should be set", 90000L, config.getConnectTimeoutMillis() );
    assertEquals( "Proxy host should be set", proxyHost, config.getProxyHost() );
    assertEquals( "Proxy port should be set", proxyPort, config.getProxyPort() );
    assertEquals( "Proxy username should be set", proxyUsername, config.getProxyUser() );
    assertEquals( "Proxy password should be set", proxyPassword, config.getProxyPassword() );
  }

}
