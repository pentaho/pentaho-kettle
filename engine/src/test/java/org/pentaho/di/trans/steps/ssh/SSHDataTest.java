/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Hitachi Vantara : http://www.pentaho.com
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


import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.ServerHostKeyVerifier;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.trilead.ssh2.Connection;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { SSHData.class, KettleVFS.class } )
public class SSHDataTest {

  @Mock
  Connection connection;
  @Mock
  FileObject fileObject;
  @Mock
  FileContent fileContent;
  @Mock
  VariableSpace variableSpace;

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
    PowerMockito.mockStatic( SSHData.class );
    PowerMockito.mockStatic( KettleVFS.class );
    when( SSHData.createConnection( server, port ) ).thenReturn( connection );
    when( SSHData.OpenConnection( any(), anyInt(), any(), any(), anyBoolean(),
      any(), any(), anyInt(), anyObject(), any(), anyInt(), any(), any() ) ).thenCallRealMethod();
    when( KettleVFS.getFileObject( keyFilePath ) ).thenReturn( fileObject );
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
    when( connection.authenticateWithPublicKey( eq( username ), Matchers.<char[]>any(), eq( passPhrase ) ) ).thenReturn( true );
    SSHData.OpenConnection( server, port, username, null, true, keyFilePath,
      passPhrase, 0, variableSpace, null, 0, null, null );
    verify( connection ).connect();
    verify( connection ).authenticateWithPublicKey( eq( username ), Matchers.<char[]>any(), eq( passPhrase ) );
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
    verify( connection ).connect( isNull( ServerHostKeyVerifier.class ), eq( 0 ), eq( 100 * 1000 ) );
  }

}
