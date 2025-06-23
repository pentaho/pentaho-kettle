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


package org.pentaho.di.trans.steps.ldapinput;

import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
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

    return new LdapProtocolFactory( null ).createLdapProtocol( DefaultBowl.getInstance(), variableSpace, mockLdapMeta,
      null );
  }
}
