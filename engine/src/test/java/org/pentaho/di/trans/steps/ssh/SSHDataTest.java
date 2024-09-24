/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.ssh;


import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.ServerHostKeyVerifier;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SSHDataTest {


  Connection connection;

  FileObject fileObject;

  FileContent fileContent;

  VariableSpace variableSpace;

  MockedStatic<SSHData> sshDataMockedStatic;
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
    connection = mock( Connection.class );
    fileObject = mock( FileObject.class );
    fileContent = mock( FileContent.class );
    variableSpace = mock( VariableSpace.class );
    sshDataMockedStatic = mockStatic( SSHData.class );
    kettleVFSMockedStatic = mockStatic( KettleVFS.class );
    when( SSHData.createConnection( server, port ) ).thenReturn( connection );
    when( SSHData.OpenConnection( any(), anyInt(), any(), any(), anyBoolean(),
      any(), any(), anyInt(), any(), any(), anyInt(), any(), any() ) ).thenCallRealMethod();
    when( KettleVFS.getFileObject( keyFilePath ) ).thenReturn( fileObject );
  }

  @After
  public void tearDown() {
    sshDataMockedStatic.close();
    kettleVFSMockedStatic.close();
  }

  @Test
  public void testOpenConnection_1() throws Exception {
    when( connection.authenticateWithPassword( username, password ) ).thenReturn( true );
    assertNotNull( SSHData.OpenConnection( server, port, username, password, false, null,
      null, 0, null, null, 0, null, null ) );
    verify( connection ).connect();
    verify( connection ).authenticateWithPassword( username, password );
  }

  @Test( expected = KettleException.class )
  public void testOpenConnection_2() throws Exception {
    when( connection.authenticateWithPassword( username, password ) ).thenReturn( false );
    SSHData.OpenConnection( server, port, username, password, false, null,
      null, 0, null, null, 0, null, null );
    verify( connection ).connect();
    verify( connection ).authenticateWithPassword( username, password );
  }

  @Test( expected = KettleException.class )
  public void testOpenConnectionUseKey_1() throws Exception {
    when( fileObject.exists() ).thenReturn( false );
    SSHData.OpenConnection( server, port, null, null, true, null,
      null, 0, null, null, 0, null, null );
    verify( fileObject ).exists();
  }

  @Test
  public void testOpenConnectionUseKey_2() throws Exception {
    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getContent() ).thenReturn( fileContent );
    when( fileContent.getSize() ).thenReturn( 1000L );
    when( fileContent.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[] { 1, 2, 3, 4, 5 } ) );
    when( variableSpace.environmentSubstitute( passPhrase ) ).thenReturn(  passPhrase );
    when( connection.authenticateWithPublicKey( eq( username ), any( char[].class ), eq( passPhrase ) ) ).thenReturn( true );
    SSHData.OpenConnection( server, port, username, null, true, keyFilePath,
      passPhrase, 0, variableSpace, null, 0, null, null );
    verify( connection ).connect();
    verify( connection ).authenticateWithPublicKey( eq( username ), any( char[].class ), eq( passPhrase ) );
  }

  @Test
  public void testOpenConnectionProxy() throws Exception {
    when( connection.authenticateWithPassword( username, password ) ).thenReturn( true );
    assertNotNull( SSHData.OpenConnection( server, port, username, password, false, null,
      null, 0, null, proxyHost, proxyPort, proxyUsername, proxyPassword ) );
    verify( connection ).connect();
    verify( connection ).authenticateWithPassword( username, password );
    verify( connection ).setProxyData( any( HTTPProxyData.class ) );
  }

  @Test
  public void testOpenConnectionTimeOut() throws Exception {
    when( connection.authenticateWithPassword( username, password ) ).thenReturn( true );
    assertNotNull( SSHData.OpenConnection( server, port, username, password, false, null,
      null, 100, null, null, proxyPort, proxyUsername, proxyPassword ) );
    verify( connection ).connect( isNull(), eq( 0 ), eq( 100 * 1000 ) );
  }

}
