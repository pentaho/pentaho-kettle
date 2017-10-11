/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
public class SessionConfiguratorTest {
  private SessionConfigurator sessionConfigurator;
  @Mock private Map<String, List<String>> headers;
  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testNoAuthentication() throws URISyntaxException {
    sessionConfigurator = new SessionConfigurator( new URI( "ws://test:8080/execute" ), null, null );

    sessionConfigurator.beforeRequest( headers );
    verify( headers, times( 0 ) ).put( any(), any() );
  }

  @Test
  public void testPrincipal() throws URISyntaxException, RuntimeException {
    sessionConfigurator = new SessionConfigurator( new URI( "ws://test:8080/execute" ), null, "testPrincipal" );

    expectedException.expect( RuntimeException.class );
    sessionConfigurator.beforeRequest( headers );
  }

  @Test
  public void testPrincipalKeytab() throws URISyntaxException, RuntimeException {
    sessionConfigurator = new SessionConfigurator( new URI( "ws://test:8080/execute" ), "filepath.keytab", "testPrincipal" );

    expectedException.expect( RuntimeException.class );
    sessionConfigurator.beforeRequest( headers );
  }

  @Test
  public void testDaemonNoAuthentication( ) throws KettleException {
    expectedException.expect( KettleException.class );
    new DaemonMessagesClientEndpoint( "host", "8080", true, null );
  }

  @Test
  public void testDaemonWithAuthentication( ) throws KettleException {
    System.getProperties().setProperty( "KETTLE_AEL_PDI_DAEMON_PRINCIPAL", "principal" );
    System.getProperties().setProperty( "KETTLE_AEL_PDI_DAEMON_KEYTAB", "filepath.keytab" );

    expectedException.expect( KettleException.class );
    new DaemonMessagesClientEndpoint( "host", "8080", true, null );
  }
}
