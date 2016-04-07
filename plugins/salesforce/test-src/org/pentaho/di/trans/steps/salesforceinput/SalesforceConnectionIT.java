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
package org.pentaho.di.trans.steps.salesforceinput;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.rmi.RemoteException;
import java.util.Calendar;

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
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.SoapBindingStub;
import com.sforce.soap.partner.fault.InvalidIdFault;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;

public class SalesforceConnectionIT {

  private SoapBindingStub bindingStub;

  private String samplePassword = "password";

  @BeforeClass
  public static void setUpClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID = Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void setUp() throws InvalidIdFault, UnexpectedErrorFault, LoginFault, RemoteException {
    GetServerTimestampResult serverTime = new GetServerTimestampResult( Calendar.getInstance() );
    LoginResult result = mock( LoginResult.class );

    bindingStub = mock( SoapBindingStub.class );
    when( bindingStub.login( anyString(), anyString() ) ).thenReturn( result );
    when( bindingStub.getServerTimestamp() ).thenReturn( serverTime );
  }

  @Test
  public void testConnect() {
    testConnect( samplePassword );
  }

  @Test
  public void testConnect_Encrypted() {
    String password = Encr.encryptPasswordIfNotUsingVariables( samplePassword );
    testConnect( password );
  }

  public void testConnect( String password ) {
    LogChannelInterface logInterface = mock( LogChannelInterface.class );
    String url = "url";
    String username = "username";
    SalesforceConnection connection;
    try {
      connection = spy( new SalesforceConnection( logInterface, url, username, password ) );
      doReturn( bindingStub ).when( connection ).getSoapBinding();
      ArgumentCaptor<String> captorUser = ArgumentCaptor.forClass( String.class );
      ArgumentCaptor<String> captorPassword = ArgumentCaptor.forClass( String.class );

      connection.connect();
      verify( bindingStub ).login( captorUser.capture(), captorPassword.capture() );
      assertTrue( username.equals( captorUser.getValue() ) );
      assertTrue( samplePassword.equals( captorPassword.getValue() ) );
    } catch ( Exception e ) {
      fail( "Connection fail" );
    }
  }

}
