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
