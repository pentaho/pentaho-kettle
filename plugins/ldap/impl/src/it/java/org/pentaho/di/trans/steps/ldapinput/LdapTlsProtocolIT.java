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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.ldapinput.store.CustomSocketFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LdapTlsProtocolIT {
  private LogChannelInterface mockLogChannelInterface;

  private VariableSpace mockVariableSpace;

  private LdapMeta mockLdapMeta;

  private InitialLdapContext mockInitialLdapContext;

  private StartTlsResponse mockStartTlsResponse;

  private class TestableLdapTlsProtocol extends LdapTlsProtocol {
    public Hashtable<String, String> contextEnv = null;

    public TestableLdapTlsProtocol( LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
      Collection<String> binaryAttributes ) {
      super( DefaultBowl.getInstance(), log, variableSpace, meta, binaryAttributes );
    }

    @Override
    protected InitialLdapContext createLdapContext( Hashtable<String, String> env ) throws NamingException {
      contextEnv = env;
      return mockInitialLdapContext;
    }

    @Override
    protected void configureSocketFactory( boolean trustAllCertificates, String trustStorePath,
      String trustStorePassword ) throws KettleException {
      CustomSocketFactory.configure();
    }
  }

  @Before
  public void setup() throws NamingException {
    mockLogChannelInterface = mock( LogChannelInterface.class );
    mockVariableSpace = mock( VariableSpace.class );
    mockLdapMeta = mock( LdapMeta.class );
    mockInitialLdapContext = mock( InitialLdapContext.class );
    mockStartTlsResponse = mock( StartTlsResponse.class );
    when( mockInitialLdapContext.extendedOperation( any( StartTlsRequest.class ) ) ).thenReturn(
      mockStartTlsResponse );
  }

  @Test
  public void testLdapProtocolAddsLdapPrefixIfNecessary() throws KettleException, NamingException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapTlsProtocol testableLdapProtocol =
      new TestableLdapTlsProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals(
      testableLdapProtocol.getConnectionPrefix() + hostConcrete + ":" + portConcrete,
      testableLdapProtocol.contextEnv.get( Context.PROVIDER_URL ) );
  }

  @Test
  public void testLdapProtocolSkipsAddingLdapPrefixIfNecessary() throws KettleException {
    String hostnameConcrete = "host_concrete";
    String hostConcrete = "ldap://" + hostnameConcrete;
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapTlsProtocol testableLdapProtocol =
      new TestableLdapTlsProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals(
      testableLdapProtocol.getConnectionPrefix() + hostnameConcrete + ":" + portConcrete,
      testableLdapProtocol.contextEnv.get( Context.PROVIDER_URL ) );
  }

  @Test
  public void testLdapProtocolSetsSsl() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapTlsProtocol testableLdapProtocol =
      new TestableLdapTlsProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertNull( testableLdapProtocol.contextEnv.get( Context.SECURITY_PROTOCOL ) );
  }

  @Test
  public void testLdapProtocolSetsSocketFactory() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapTlsProtocol testableLdapProtocol =
      new TestableLdapTlsProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals( null, testableLdapProtocol.contextEnv.get( "java.naming.ldap.factory.socket" ) );
  }

  @Test
  public void testLdapProtocolNegotiatesTls() throws KettleException, IOException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapTlsProtocol testableLdapProtocol =
      new TestableLdapTlsProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    verify( mockStartTlsResponse ).negotiate( any( CustomSocketFactory.class ) );
  }
}
