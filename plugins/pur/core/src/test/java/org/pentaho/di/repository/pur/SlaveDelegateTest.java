/*!
 * Copyright 2020 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.repository.pur;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlaveDelegateTest {

  @ClassRule
  public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  private PurRepository mockPurRepository;
  private SlaveDelegate slaveDelegate;
  private DataNode mockDataNode;
  private  SlaveServer mockSlaveServer;

  private static String PROP_HOST_NAME_VALUE = "hostname";
  private static String PROP_USERNAME_VALUE = "username";
  private static String PROP_PASSWORD_VALUE = "password";
  private static String PROP_PORT_VALUE = "1234";
  private static String PROP_PROXY_HOST_NAME_VALUE = "proxyhostname";
  private static String PROP_PROXY_PORT_VALUE = "4321";
  private static String PROP_WEBAPP_NAME_VALUE = "webappname";
  private static String PROP_NON_PROXY_HOSTS_VALUE = "nonproxyhosts";
  private static Boolean PROP_MASTER_VALUE = true;
  private static Boolean PROP_USE_HTTPS_PROTOCOL_VALUE = true;


  @Before
  public void setup() throws KettleException {
    KettleClientEnvironment.init();
    mockPurRepository = mock( PurRepository.class );
    slaveDelegate = new SlaveDelegate( mockPurRepository );

    mockSlaveServer = mock( SlaveServer.class );
    when( mockSlaveServer.getHostname() ).thenReturn( PROP_HOST_NAME_VALUE );
    when( mockSlaveServer.getUsername() ).thenReturn( PROP_USERNAME_VALUE );
    when( mockSlaveServer.getPassword() ).thenReturn( PROP_PASSWORD_VALUE );
    when( mockSlaveServer.getPort() ).thenReturn( PROP_PORT_VALUE );
    when( mockSlaveServer.getProxyHostname() ).thenReturn( PROP_PROXY_HOST_NAME_VALUE );
    when( mockSlaveServer.getProxyPort() ).thenReturn( PROP_PROXY_PORT_VALUE );
    when( mockSlaveServer.getWebAppName() ).thenReturn( PROP_WEBAPP_NAME_VALUE );
    when( mockSlaveServer.getNonProxyHosts() ).thenReturn( PROP_NON_PROXY_HOSTS_VALUE );
    when( mockSlaveServer.isMaster() ).thenReturn( PROP_MASTER_VALUE );
    when( mockSlaveServer.isSslMode() ).thenReturn( PROP_USE_HTTPS_PROTOCOL_VALUE );

    mockDataNode = mock( DataNode.class, RETURNS_DEEP_STUBS );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_HOST_NAME ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_HOST_NAME ).getString() ).thenReturn( PROP_HOST_NAME_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_USERNAME ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_USERNAME ).getString() ).thenReturn( PROP_USERNAME_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_PASSWORD ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_PASSWORD ).getString() ).thenReturn( PROP_PASSWORD_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_PORT ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_PORT ).getString() ).thenReturn( PROP_PORT_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_PROXY_HOST_NAME ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_PROXY_HOST_NAME ).getString() ).thenReturn( PROP_PROXY_HOST_NAME_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_PROXY_PORT ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_PROXY_PORT ).getString() ).thenReturn( PROP_PROXY_PORT_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_WEBAPP_NAME ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_WEBAPP_NAME ).getString() ).thenReturn( PROP_WEBAPP_NAME_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_NON_PROXY_HOSTS ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_NON_PROXY_HOSTS ).getString() ).thenReturn( PROP_NON_PROXY_HOSTS_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_MASTER ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_MASTER ).getBoolean() ).thenReturn( PROP_MASTER_VALUE );
    when( mockDataNode.hasProperty( SlaveDelegate.PROP_USE_HTTPS_PROTOCOL ) ).thenReturn( true );
    when( mockDataNode.getProperty( SlaveDelegate.PROP_USE_HTTPS_PROTOCOL ).getBoolean() ).thenReturn( PROP_USE_HTTPS_PROTOCOL_VALUE );
  }

  @Test
  public void testElementToDataNode() throws KettleException {
    DataNode dataNode = slaveDelegate.elementToDataNode( mockSlaveServer );

    Assert.assertEquals( PROP_HOST_NAME_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_HOST_NAME ) );
    Assert.assertEquals( PROP_USERNAME_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_USERNAME ) );
    Assert.assertEquals( Encr.encryptPasswordIfNotUsingVariables( PROP_PASSWORD_VALUE ), slaveDelegate.getString( dataNode, SlaveDelegate.PROP_PASSWORD ) );
    Assert.assertEquals( PROP_PORT_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_PORT ) );
    Assert.assertEquals( PROP_PROXY_HOST_NAME_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_PROXY_HOST_NAME ) );
    Assert.assertEquals( PROP_PROXY_PORT_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_PROXY_PORT ) );
    Assert.assertEquals( PROP_WEBAPP_NAME_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_WEBAPP_NAME ) );
    Assert.assertEquals( PROP_NON_PROXY_HOSTS_VALUE, slaveDelegate.getString( dataNode, SlaveDelegate.PROP_NON_PROXY_HOSTS ) );
    Assert.assertEquals( PROP_MASTER_VALUE, slaveDelegate.getBoolean( dataNode, SlaveDelegate.PROP_MASTER ) );
    Assert.assertEquals( PROP_USE_HTTPS_PROTOCOL_VALUE, slaveDelegate.getBoolean( dataNode, SlaveDelegate.PROP_USE_HTTPS_PROTOCOL ) );
  }

  @Test
  public void testDataNodeToElement() throws KettleException {
    SlaveServer slaveServer = new SlaveServer();
    slaveDelegate.dataNodeToElement( mockDataNode, slaveServer );
    Assert.assertEquals( PROP_HOST_NAME_VALUE, slaveServer.getHostname() );
    Assert.assertEquals( PROP_USERNAME_VALUE, slaveServer.getUsername() );
    Assert.assertEquals( PROP_PASSWORD_VALUE, slaveServer.getPassword() );
    Assert.assertEquals( PROP_PORT_VALUE, slaveServer.getPort() );
    Assert.assertEquals( PROP_PROXY_HOST_NAME_VALUE, slaveServer.getProxyHostname() );
    Assert.assertEquals( PROP_PROXY_PORT_VALUE, slaveServer.getProxyPort() );
    Assert.assertEquals( PROP_WEBAPP_NAME_VALUE, slaveServer.getWebAppName() );
    Assert.assertEquals( PROP_NON_PROXY_HOSTS_VALUE, slaveServer.getNonProxyHosts() );
    Assert.assertEquals( PROP_MASTER_VALUE, slaveServer.isMaster() );
    Assert.assertEquals( PROP_USE_HTTPS_PROTOCOL_VALUE, slaveServer.isSslMode() );
  }
}
