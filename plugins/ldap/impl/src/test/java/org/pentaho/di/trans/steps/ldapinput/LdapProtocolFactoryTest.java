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
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.Collections;

public class LdapProtocolFactoryTest {

  @Test
  public void createLdapProtocol() throws Exception {
    String ldapVariable = "${ldap_protocol_variable}";
    String ldap = "LDAP";
    String host = "localhost";

    LdapProtocolFactory ldapProtocolFactory = new LdapProtocolFactory( Mockito.mock( LogChannelInterface.class ) );
    VariableSpace variableSpace = Mockito.mock( VariableSpace.class );
    LdapMeta meta = Mockito.mock( LdapMeta.class );
    Mockito.doReturn( ldapVariable ).when( meta ).getProtocol();
    Mockito.doReturn( ldap ).when( variableSpace ).environmentSubstitute( ldapVariable );
    Mockito.doReturn( host ).when( meta ).getHost();
    Mockito.doReturn( host ).when( variableSpace ).environmentSubstitute( host );

    ldapProtocolFactory.createLdapProtocol( variableSpace, meta, Collections.emptyList() );
    Mockito.verify( variableSpace, Mockito.times( 1 ) ).environmentSubstitute( ldapVariable );

  }
}
