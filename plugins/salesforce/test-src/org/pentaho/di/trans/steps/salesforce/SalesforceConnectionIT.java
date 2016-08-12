/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.salesforce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;

import com.sforce.soap.partner.GetServerTimestampResult;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceConnectionIT {

  private PartnerConnection bindingStub;

  private String samplePassword = "MySamplePassword";

  @BeforeClass
  public static void setUpClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void setUp() throws RemoteException, ConnectionException {
    GetServerTimestampResult serverTime = new GetServerTimestampResult();
    serverTime.setTimestamp(  Calendar.getInstance()  );
    LoginResult result = mock( LoginResult.class );

    bindingStub = mock( PartnerConnection.class );
    when( bindingStub.login( anyString(), anyString() ) ).thenReturn( result );
    when( bindingStub.getServerTimestamp() ).thenReturn( serverTime );
  }

  @Test
  public void testConnect() throws Exception {
    testConnect( samplePassword );
  }

  @Test
  public void testConnect_Encrypted() throws Exception {
    String password = Encr.encryptPasswordIfNotUsingVariables( samplePassword );
    testConnect( password );
  }

  private void testConnect( String password ) throws Exception {
    LogChannelInterface logInterface = mock( LogChannelInterface.class );
    String url = "http://localhost/services/Soap/u/37.0";
    String username = "MySampleUsername";
    SalesforceConnection connection;
    connection = spy( new SalesforceConnection( logInterface, url, username, password ) );
    ArgumentCaptor<ConnectorConfig> captorConfig = ArgumentCaptor.forClass( ConnectorConfig.class );

    ConnectorConfig mockConfig = mock( ConnectorConfig.class );
    doReturn( UUID.randomUUID().toString() ).when( mockConfig ).getUsername();
    doReturn( UUID.randomUUID().toString() ).when( mockConfig ).getPassword();
    doReturn( mockConfig ).when( bindingStub ).getConfig();
    doReturn( mock( LoginResult.class ) ).when( bindingStub ).login( anyString(), anyString() );

    try {
      connection.connect();
    } catch ( KettleException e ) {
      // The connection should fail
      // We just want to see the generated ConnectorConfig
    }

    verify( connection ).createBinding( captorConfig.capture() );
    assertEquals( username, captorConfig.getValue().getUsername() );
    // Password is unchanged (not decrypted) when setting in ConnectorConfig
    assertEquals( password, captorConfig.getValue().getPassword() );
  }

  @Test
  public void testConnectOptions() {
    LogChannelInterface logInterface = mock( LogChannelInterface.class );
    String url = SalesforceConnectionUtils.TARGET_DEFAULT_URL;
    String username = "username";
    String password = "password";
    Integer timeout = 30;
    try {
      SalesforceConnection connection =
        spy( new SalesforceConnection( logInterface, url, username, password ) );
      connection.setTimeOut( timeout );
      LoginResult loginResult = mock( LoginResult.class );
      GetUserInfoResult userInfo = mock( GetUserInfoResult.class );
      GetServerTimestampResult serverTime = new GetServerTimestampResult();
      serverTime.setTimestamp( Calendar.getInstance() );

      ArgumentCaptor<ConnectorConfig> captorConfig = ArgumentCaptor.forClass( ConnectorConfig.class );

      doReturn( loginResult ).when( bindingStub ).login( anyString(), anyString() );
      doReturn( userInfo ).when( bindingStub ).getUserInfo();
      when( loginResult.getServerUrl() ).thenReturn( "http://localhost/services/Soap/u/37.0" );
      when( loginResult.getSessionId() ).thenReturn( UUID.randomUUID().toString() );
      when( userInfo.getUserFullName() ).thenReturn( UUID.randomUUID().toString() );
      when( userInfo.getUserEmail() ).thenReturn( UUID.randomUUID().toString() );
      when( userInfo.getUserLanguage() ).thenReturn( UUID.randomUUID().toString() );
      when( userInfo.getOrganizationName() ).thenReturn( UUID.randomUUID().toString() );
      doReturn( serverTime ).when( bindingStub ).getServerTimestamp();

      connection.setTimeOut( timeout );
      connection.setUsingCompression( true );
      connection.setRollbackAllChangesOnError( true );
      try {
        connection.connect();
      } catch ( KettleException e ) {
        // The connection should fail
        // We just want to see the generated ConnectorConfig
      }

      verify( connection ).createBinding( captorConfig.capture() );
      ConnectorConfig config = captorConfig.getValue();
      assertNotNull( config );
      assertEquals( url, config.getAuthEndpoint() );
      assertTrue( config.isCompression() );
      assertTrue( config.isManualLogin() );
      assertEquals( timeout, Integer.valueOf( config.getConnectionTimeout() ) );
      assertEquals( timeout, Integer.valueOf( config.getReadTimeout() ) );
    } catch ( Exception e ) {
      fail( "Connection fail: " + e.getMessage() );
    }
  }
}
