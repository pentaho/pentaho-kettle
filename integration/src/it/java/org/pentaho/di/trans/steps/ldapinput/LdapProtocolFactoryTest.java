/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.ldapinput;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

public class LdapProtocolFactoryTest {
  private LdapMeta mockLdapMeta;

  @Before
  public void setup() {
    mockLdapMeta = mock( LdapMeta.class );
  }

  @Test
  public void testLdapProtocolFactoryGetConnectionTypesReturnsAllProtocolNames() {
    List<String> types = LdapProtocolFactory.getConnectionTypes( null );
    assertEquals( 3, types.size() );
    assertTrue( types.contains( LdapProtocol.getName() ) );
    assertTrue( types.contains( LdapSslProtocol.getName() ) );
    assertTrue( types.contains( LdapTlsProtocol.getName() ) );
  }

  @Test
  public void testLdapProtocolFactoryReturnsLdapProtocolForName() throws KettleException {
    when( mockLdapMeta.getProtocol() ).thenReturn( LdapProtocol.getName() );
    LdapProtocol protocol =
      new LdapProtocolFactory( null ).createLdapProtocol( mock( VariableSpace.class ), mockLdapMeta, null );
    assertTrue( protocol.getClass().equals( LdapProtocol.class ) );
  }

  @Test
  public void testLdapProtocolFactoryReturnsLdapSslProtocolForName() throws KettleException {
    when( mockLdapMeta.getProtocol() ).thenReturn( LdapSslProtocol.getName() );
    LdapProtocol protocol =
      new LdapProtocolFactory( null ).createLdapProtocol( mock( VariableSpace.class ), mockLdapMeta, null );
    assertTrue( protocol.getClass().equals( LdapSslProtocol.class ) );
  }

  @Test
  public void testLdapProtocolFactoryReturnsLdapTlsProtocolForName() throws KettleException {
    when( mockLdapMeta.getProtocol() ).thenReturn( LdapTlsProtocol.getName() );
    LdapProtocol protocol =
      new LdapProtocolFactory( null ).createLdapProtocol( mock( VariableSpace.class ), mockLdapMeta, null );
    assertTrue( protocol.getClass().equals( LdapTlsProtocol.class ) );
  }
}
