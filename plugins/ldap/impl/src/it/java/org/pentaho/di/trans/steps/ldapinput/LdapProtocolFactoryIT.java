/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapProtocolFactoryIT {

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
    LdapProtocol protocol = getProtocolInstance( LdapProtocol.getName() );
    assertEquals( protocol.getClass(), LdapProtocol.class );
  }

  @Test
  public void testLdapProtocolFactoryReturnsLdapSslProtocolForName() throws KettleException {
    LdapProtocol protocol = getProtocolInstance( LdapSslProtocol.getName() );
    assertEquals( protocol.getClass(), LdapSslProtocol.class );
  }

  @Test
  public void testLdapProtocolFactoryReturnsLdapTlsProtocolForName() throws KettleException {
    LdapProtocol protocol = getProtocolInstance( LdapTlsProtocol.getName() );
    assertEquals( protocol.getClass(), LdapTlsProtocol.class );
  }

  private LdapProtocol getProtocolInstance( String protocolName ) throws KettleException {
    LdapMeta mockLdapMeta = mock( LdapMeta.class );
    when( mockLdapMeta.getProtocol() ).thenReturn( protocolName );

    VariableSpace variableSpace = mock( VariableSpace.class );
    // In this particular case, no variable is used, so no substitution is needed
    when( variableSpace.environmentSubstitute( protocolName ) ).thenReturn( protocolName );

    return new LdapProtocolFactory( null ).createLdapProtocol( variableSpace, mockLdapMeta, null );
  }
}
