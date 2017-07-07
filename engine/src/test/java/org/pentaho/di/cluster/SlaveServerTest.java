/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.utils.TestUtils;
import org.pentaho.di.www.GetPropertiesServlet;

/**
 * Tests for SlaveServer class
 *
 * @author Pavel Sakun
 * @see SlaveServer
 */
public class SlaveServerTest {
  SlaveServer slaveServer;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void init() throws IOException {
    SlaveConnectionManager connectionManager = SlaveConnectionManager.getInstance();
    HttpClient httpClient = spy( connectionManager.createHttpClient() );

    //mock response
    CloseableHttpResponse closeableHttpResponseMock = mock( CloseableHttpResponse.class );

    //mock status line
    StatusLine statusLineMock = mock( StatusLine.class );
    doReturn( HttpStatus.SC_NOT_FOUND ).when( statusLineMock ).getStatusCode();
    doReturn( statusLineMock ).when( closeableHttpResponseMock ).getStatusLine();

    //mock entity
    HttpEntity httpEntityMock = mock( HttpEntity.class );
    doReturn( httpEntityMock ).when( closeableHttpResponseMock ).getEntity();

    doReturn( closeableHttpResponseMock ).when( httpClient ).execute( any( HttpGet.class ) );
    doReturn( closeableHttpResponseMock ).when( httpClient ).execute( any( HttpPost.class ) );

    slaveServer = spy( new SlaveServer() );
    doReturn( httpClient ).when( slaveServer ).getHttpClient();
    doReturn( "response_body" ).when( slaveServer ).getResponseBodyAsString( any( InputStream.class ) );
  }

  @Test( expected = KettleException.class )
  public void testExecService() throws Exception {
    HttpGet httpGetMock = mock( HttpGet.class );
    URI uriMock = new URI( "fake" );
    doReturn( uriMock ).when( httpGetMock ).getURI();
    doReturn( httpGetMock ).when( slaveServer ).buildExecuteServiceMethod( anyString(),
        anyMapOf( String.class, String.class ) );
    slaveServer.execService( "wrong_app_name" );
    fail( "Incorrect connection details had been used, but no exception was thrown" );
  }

  @Test( expected = KettleException.class )
  public void testSendXML() throws Exception {
    HttpPost httpPostMock = mock( HttpPost.class );
    URI uriMock = new URI( "fake" );
    doReturn( uriMock ).when( httpPostMock ).getURI();
    doReturn( httpPostMock ).when( slaveServer ).buildSendXMLMethod( any( byte[].class ), anyString() );
    slaveServer.sendXML( "", "" );
    fail( "Incorrect connection details had been used, but no exception was thrown" );
  }

  @Test( expected = KettleException.class )
  public void testSendExport() throws Exception {
    HttpPost httpPostMock = mock( HttpPost.class );
    URI uriMock = new URI( "fake" );
    doReturn( uriMock ).when( httpPostMock ).getURI();
    doReturn( httpPostMock ).when( slaveServer ).buildSendExportMethod( anyString(), anyString(),
        any( InputStream.class ) );
    File tempFile;
    tempFile = File.createTempFile( "PDI-", "tmp" );
    tempFile.deleteOnExit();
    slaveServer.sendExport( tempFile.getAbsolutePath(), "", "" );
    fail( "Incorrect connection details had been used, but no exception was thrown" );
  }

  @Test
  public void testAddCredentials() {
    slaveServer.setUsername( "test_username" );
    slaveServer.setPassword( "test_password" );
    slaveServer.setHostname( "test_host" );
    slaveServer.setPort( "8081" );

    HttpClientContext localContext = HttpClientContext.create();
    slaveServer.addCredentials( localContext );

    AuthScope scope = new AuthScope( slaveServer.getHostname(), Const.toInt( slaveServer.getPort(), 80 ) );
    Credentials credentials = localContext.getCredentialsProvider().getCredentials( scope );
    assertNotNull( credentials );
    assertTrue( credentials instanceof UsernamePasswordCredentials );
    UsernamePasswordCredentials baseCredentials = (UsernamePasswordCredentials) credentials;
    assertEquals( slaveServer.getUsername(), baseCredentials.getUserName() );
    assertEquals( slaveServer.getPassword(), baseCredentials.getPassword() );
  }

  @Test
  public void testModifyingName() {
    slaveServer.setName( "test" );
    List<SlaveServer> list = new ArrayList<SlaveServer>();
    list.add( slaveServer );

    SlaveServer slaveServer2 = spy( new SlaveServer() );
    slaveServer2.setName( "test" );

    slaveServer2.verifyAndModifySlaveServerName( list, null );

    assertTrue( !slaveServer.getName().equals( slaveServer2.getName() ) );
  }

  @Test
  public void testEqualsHashCodeConsistency() throws Exception {
    SlaveServer slave = new SlaveServer();
    slave.setName( "slave" );
    TestUtils.checkEqualsHashCodeConsistency( slave, slave );

    SlaveServer slaveSame = new SlaveServer();
    slaveSame.setName( "slave" );
    assertTrue( slave.equals( slaveSame ) );
    TestUtils.checkEqualsHashCodeConsistency( slave, slaveSame );

    SlaveServer slaveCaps = new SlaveServer();
    slaveCaps.setName( "SLAVE" );
    TestUtils.checkEqualsHashCodeConsistency( slave, slaveCaps );

    SlaveServer slaveOther = new SlaveServer();
    slaveOther.setName( "something else" );
    TestUtils.checkEqualsHashCodeConsistency( slave, slaveOther );
  }

  @Test
  public void testGetKettleProperties() throws Exception {
    String encryptedResponse = "3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e6"
      + "73d225554462d38223f3e0a3c21444f43545950452070726f706572"
      + "746965730a202053595354454d2022687474703a2f2f6a6176612e737"
      + "56e2e636f6d2f6474642f70726f706572746965732e647464223e0a3c"
      + "70726f706572746965733e0a2020203c636f6d6d656e743e3c2f636f6d6d6"
      + "56e743e0a2020203c656e747279206b65793d224167696c6542494461746162"
      + "617365223e4167696c6542493c2f656e7470c7a6a5f445d7808bbb1cbc64d797bc84";
    doReturn(
      encryptedResponse )
      .when( slaveServer ).execService( GetPropertiesServlet.CONTEXT_PATH + "/?xml=Y" );
    slaveServer.getKettleProperties().getProperty( "AgileBIDatabase" );
    assertEquals( "AgileBI", slaveServer.getKettleProperties().getProperty( "AgileBIDatabase" ) );

  }

}
