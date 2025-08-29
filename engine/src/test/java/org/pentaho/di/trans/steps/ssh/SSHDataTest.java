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

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.SshConnectionFactory;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


public class SSHDataTest {

  SshConnection sshConnection;

  FileObject fileObject;

  FileContent fileContent;

  VariableSpace variableSpace;
  LogChannelInterface logChannel;

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
    logChannel = mock( LogChannelInterface.class );
    sshConnectionFactoryMockedStatic = mockStatic( SshConnectionFactory.class );
    kettleVFSMockedStatic = mockStatic( KettleVFS.class );

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
    SshConnection result = SSHData.OpenSshConnection( DefaultBowl.getInstance(), server, port, username, password, false, null,
      null, 0, null, null, 0, null, null, logChannel );
    assertNotNull( "Should return a valid connection adapter", result );
  }

  @Test( expected = KettleException.class )
  public void testOpenSshConnection_InvalidKey() throws Exception {
    // Test with key file that doesn't exist
    when( fileObject.exists() ).thenReturn( false );
    SSHData.OpenSshConnection( DefaultBowl.getInstance(), server, port, null, null, true, keyFilePath,
      null, 0, null, null, 0, null, null, logChannel );
  }

  @Test
  public void testOpenSshConnection_WithKey() throws Exception {
    // Test successful key authentication
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getSize() ).thenReturn( 1000L );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[] { 1, 2, 3, 4, 5 } ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn( passPhrase );
    
    SshConnection result = SSHData.OpenSshConnection( DefaultBowl.getInstance(), server, port, username, null, true, keyFilePath,
      passPhrase, 0, variableSpace, null, 0, null, null, logChannel );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testOpenSshConnection_WithProxy() throws Exception {
    // Test connection with proxy settings
    SshConnection result = SSHData.OpenSshConnection( DefaultBowl.getInstance(), server, port, username, password, false, null,
      null, 0, null, proxyHost, proxyPort, proxyUsername, proxyPassword, logChannel );
    assertNotNull( "Should return a valid SSH connection", result );
  }

  @Test
  public void testOpenSshConnection_WithTimeout() throws Exception {
    // Test connection with custom timeout
    SshConnection result = SSHData.OpenSshConnection( DefaultBowl.getInstance(), server, port, username, password, false, null,
      null, 100, null, null, 0, null, null, logChannel );
    assertNotNull( "Should return a valid SSH connection", result );
  }

}
