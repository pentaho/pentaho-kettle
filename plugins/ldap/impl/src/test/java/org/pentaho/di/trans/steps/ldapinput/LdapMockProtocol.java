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

import java.util.Collection;

import javax.naming.ldap.InitialLdapContext;

import org.mockito.Mockito;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * Mock LDAP connection protocol, for testing
 *
 * @author nhudak
 */
public class LdapMockProtocol extends LdapProtocol {
  public static InitialLdapContext mockContext;

  public LdapMockProtocol( Bowl bowl, LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
    Collection<String> binaryAttributes ) {
    super( bowl, log, variableSpace, meta, binaryAttributes );
  }

  public static String getName() {
    return "LDAP MOCK";
  }

  public static InitialLdapContext setup() {
    LdapProtocolFactory.protocols.add( LdapMockProtocol.class );
    return mockContext = Mockito.mock( InitialLdapContext.class );
  }

  public static void cleanup() {
    LdapProtocolFactory.protocols.remove( LdapMockProtocol.class );
    mockContext = null;
  }

  @Override
  protected void doConnect( String username, String password ) throws KettleException {
    if ( mockContext == null ) {
      throw new RuntimeException( "LDAP Mock Connection was not setup" );
    }
  }

  @Override
  public InitialLdapContext getCtx() {
    if ( mockContext == null ) {
      throw new RuntimeException( "LDAP Mock Connection was not setup" );
    } else {
      return mockContext;
    }
  }

  @Override
  public void close() throws KettleException {
    mockContext = null;
  }
}
