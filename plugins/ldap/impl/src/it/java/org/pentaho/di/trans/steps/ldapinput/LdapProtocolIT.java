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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapProtocolIT {
  private LogChannelInterface mockLogChannelInterface;

  private VariableSpace mockVariableSpace;

  private LdapMeta mockLdapMeta;

  private class TestableLdapProtocol extends LdapProtocol {
    public Hashtable<String, String> contextEnv = null;

    public TestableLdapProtocol( LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
      Collection<String> binaryAttributes ) {
      super( log, variableSpace, meta, binaryAttributes );
    }

    @Override
    protected InitialLdapContext createLdapContext( Hashtable<String, String> env ) throws NamingException {
      contextEnv = env;
      return null;
    }
  }

  @Before
  public void setup() {
    mockLogChannelInterface = mock( LogChannelInterface.class );
    mockVariableSpace = mock( VariableSpace.class );
    mockLdapMeta = mock( LdapMeta.class );
  }

  @Test
  public void testLdapProtocolSubstitutesEnvironmentVariables() throws KettleException {
    String hostnameVar = "host_var";
    String hostConcrete = "host_concrete";
    String portVar = "port_var";
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostnameVar );
    when( mockLdapMeta.getPort() ).thenReturn( portVar );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostnameVar ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portVar ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals(
      testableLdapProtocol.getConnectionPrefix() + hostConcrete + ":" + portConcrete,
      testableLdapProtocol.contextEnv.get( Context.PROVIDER_URL ) );
  }

  @Test
  public void testLdapProtocolSetsInitialContextFactory() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals( "com.sun.jndi.ldap.LdapCtxFactory", testableLdapProtocol.contextEnv
      .get( Context.INITIAL_CONTEXT_FACTORY ) );
  }

  @Test
  public void testLdapProtocolSetsDerefAliases() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String deref = "TEST_DEREF";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( deref );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals( deref, testableLdapProtocol.contextEnv.get( "java.naming.ldap.derefAliases" ) );
  }

  @Test
  public void testLdapProtocolSetsReferral() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String referral = "TEST_REFERRAL";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( referral );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals( referral, testableLdapProtocol.contextEnv.get( Context.REFERRAL ) );
  }

  @Test
  public void testLdapProtocolAddsLdapPrefixIfNecessary() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
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

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals(
      testableLdapProtocol.getConnectionPrefix() + hostnameConcrete + ":" + portConcrete,
      testableLdapProtocol.contextEnv.get( Context.PROVIDER_URL ) );
  }

  @Test
  public void testLdapProtocolSetsCredentialsIfNecessary() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String username = "TEST_USERNAME";
    String password = "TEST_PASSWORD";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( username, password );

    assertEquals( username, testableLdapProtocol.contextEnv.get( Context.SECURITY_PRINCIPAL ) );
    assertEquals( password, testableLdapProtocol.contextEnv.get( Context.SECURITY_CREDENTIALS ) );
    assertEquals( "simple", testableLdapProtocol.contextEnv.get( Context.SECURITY_AUTHENTICATION ) );
  }

  @Test
  public void testLdapProtocolSkipsCredentialsIfNecessary() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals( "none", testableLdapProtocol.contextEnv.get( Context.SECURITY_AUTHENTICATION ) );
  }

  @Test
  public void testLdapProtocolAddsBinaryAttributesIfNecessary() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    Set<String> binaryAttributes = new HashSet<>();
    String attr1 = "TEST_ATTR_1";
    String attr2 = "TEST_ATTR_2";
    String attr3 = "TEST_ATTR_3";

    binaryAttributes.add( attr1 );
    binaryAttributes.add( attr2 );
    binaryAttributes.add( attr3 );

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, binaryAttributes );
    testableLdapProtocol.connect( null, null );

    String attributesString = testableLdapProtocol.contextEnv.get( "java.naming.ldap.attributes.binary" );
    String[] splitString = attributesString.split( " " );
    Set<String> boundAttributes = new HashSet<>( Arrays.asList( splitString ) );
    assertEquals( binaryAttributes, boundAttributes );
  }
}
