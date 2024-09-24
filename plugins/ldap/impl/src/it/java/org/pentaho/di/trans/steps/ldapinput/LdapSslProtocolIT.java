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
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.ldapinput.store.CustomSocketFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import java.util.Collection;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapSslProtocolIT {
  private LogChannelInterface mockLogChannelInterface;

  private VariableSpace mockVariableSpace;

  private LdapMeta mockLdapMeta;

  private class TestableLdapProtocol extends LdapSslProtocol {
    public Hashtable<String, String> contextEnv = null;

    public boolean trustAllCertificates = false;

    public String trustStorePath = null;

    public String trustStorePassword = null;

    public TestableLdapProtocol( LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
      Collection<String> binaryAttributes ) {
      super( log, variableSpace, meta, binaryAttributes );
    }

    @Override
    protected InitialLdapContext createLdapContext( Hashtable<String, String> env ) throws NamingException {
      contextEnv = env;
      return null;
    }

    @Override
    protected void configureSocketFactory( boolean trustAllCertificates, String trustStorePath,
      String trustStorePassword ) throws KettleException {
      this.trustAllCertificates = trustAllCertificates;
      this.trustStorePath = trustStorePath;
      this.trustStorePassword = trustStorePassword;
    }
  }

  @Before
  public void setup() {
    mockLogChannelInterface = mock( LogChannelInterface.class );
    mockVariableSpace = mock( VariableSpace.class );
    mockLdapMeta = mock( LdapMeta.class );
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
    String hostConcrete = "ldaps://" + hostnameConcrete;
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
  public void testLdapProtocolSetsSsl() throws KettleException {
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

    assertEquals( "ssl", testableLdapProtocol.contextEnv.get( Context.SECURITY_PROTOCOL ) );
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

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertEquals( CustomSocketFactory.class.getCanonicalName(), testableLdapProtocol.contextEnv
      .get( "java.naming.ldap.factory.socket" ) );
  }

  @Test
  public void testLdapProtocolSkipsConfiguresSocketFactoryIfNecessary() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String trustStorePath = "TEST_PATH";
    String trustStorePassword = "TEST_PASSWORD";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );
    when( mockLdapMeta.isUseCertificate() ).thenReturn( false );
    when( mockLdapMeta.isTrustAllCertificates() ).thenReturn( true );
    when( mockLdapMeta.getTrustStorePath() ).thenReturn( trustStorePath );
    when( mockLdapMeta.getTrustStorePassword() ).thenReturn( trustStorePassword );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertFalse( testableLdapProtocol.trustAllCertificates );
    assertNull( testableLdapProtocol.trustStorePath );
    assertNull( testableLdapProtocol.trustStorePassword );
  }

  @Test
  public void testLdapProtocolConfiguresSocketFactoryIfNecessary() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String trustStorePath = "TEST_PATH";
    String trustStorePassword = "TEST_PASSWORD";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );
    when( mockLdapMeta.isUseCertificate() ).thenReturn( true );
    when( mockLdapMeta.isTrustAllCertificates() ).thenReturn( true );
    when( mockLdapMeta.getTrustStorePath() ).thenReturn( trustStorePath );
    when( mockLdapMeta.getTrustStorePassword() ).thenReturn( trustStorePassword );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );
    when( mockVariableSpace.environmentSubstitute( trustStorePath ) ).thenReturn( trustStorePath );
    when( mockVariableSpace.environmentSubstitute( trustStorePassword ) ).thenReturn( trustStorePassword );

    TestableLdapProtocol testableLdapProtocol =
      new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );

    assertTrue( testableLdapProtocol.trustAllCertificates );
    assertEquals( trustStorePath, testableLdapProtocol.trustStorePath );
    assertEquals( trustStorePassword, testableLdapProtocol.trustStorePassword );
  }

  @Test
  public void testResolvingPathVariables() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String trustStorePath = "${KETTLE_SSL_PATH}";
    String trustStorePathResolved = "/home/test_path";
    String trustStorePassword = "TEST_PASSWORD";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );
    when( mockLdapMeta.isUseCertificate() ).thenReturn( true );
    when( mockLdapMeta.isTrustAllCertificates() ).thenReturn( true );
    when( mockLdapMeta.getTrustStorePath() ).thenReturn( trustStorePath );
    when( mockLdapMeta.getTrustStorePassword() ).thenReturn( trustStorePassword );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );
    when( mockVariableSpace.environmentSubstitute( trustStorePath ) ).thenReturn( trustStorePathResolved );
    when( mockVariableSpace.environmentSubstitute( trustStorePassword ) ).thenReturn( trustStorePassword );

    KettleClientEnvironment.init();
    TestableLdapProtocol testableLdapProtocol =
            new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );
    assertEquals( trustStorePathResolved, testableLdapProtocol.trustStorePath );
  }


  @Test
  public void testResolvingPasswordVariables() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String trustStorePath = "/home/test_path";
    String trustStorePassword = "${PASSWORD_VARIABLE}";
    String trustStorePasswordResolved = "TEST_PASSWORD_VALUE";

    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );
    when( mockLdapMeta.isUseCertificate() ).thenReturn( true );
    when( mockLdapMeta.isTrustAllCertificates() ).thenReturn( true );
    when( mockLdapMeta.getTrustStorePath() ).thenReturn( trustStorePath );
    when( mockLdapMeta.getTrustStorePassword() ).thenReturn( trustStorePassword );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );
    when( mockVariableSpace.environmentSubstitute( trustStorePath ) ).thenReturn( trustStorePath );
    when( mockVariableSpace.environmentSubstitute( trustStorePassword ) ).thenReturn( trustStorePasswordResolved );

    KettleClientEnvironment.init();
    TestableLdapProtocol testableLdapProtocol =
            new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );
    assertEquals( trustStorePasswordResolved, testableLdapProtocol.trustStorePassword );
  }

  @Test
  public void testResolvingPasswordAndDecryptVariables() throws KettleException {
    String hostConcrete = "host_concrete";
    String portConcrete = "12345";
    String trustStorePath = "/home/test_path";
    String trustStorePassword = "${PASSWORD_VARIABLE}";
    String trustStorePasswordResolved = "Encrypted 2be98afc86aa7f2e4cb79ff228dc6fa8c"; //original value 123456


    when( mockLdapMeta.getHost() ).thenReturn( hostConcrete );
    when( mockLdapMeta.getPort() ).thenReturn( portConcrete );
    when( mockLdapMeta.getDerefAliases() ).thenReturn( "always" );
    when( mockLdapMeta.getReferrals() ).thenReturn( "follow" );
    when( mockLdapMeta.isUseCertificate() ).thenReturn( true );
    when( mockLdapMeta.isTrustAllCertificates() ).thenReturn( true );
    when( mockLdapMeta.getTrustStorePath() ).thenReturn( trustStorePath );
    when( mockLdapMeta.getTrustStorePassword() ).thenReturn( trustStorePassword );

    when( mockVariableSpace.environmentSubstitute( hostConcrete ) ).thenReturn( hostConcrete );
    when( mockVariableSpace.environmentSubstitute( portConcrete ) ).thenReturn( portConcrete );
    when( mockVariableSpace.environmentSubstitute( trustStorePath ) ).thenReturn( trustStorePath );
    when( mockVariableSpace.environmentSubstitute( trustStorePassword ) ).thenReturn( trustStorePasswordResolved );

    KettleClientEnvironment.init();
    TestableLdapProtocol testableLdapProtocol =
            new TestableLdapProtocol( mockLogChannelInterface, mockVariableSpace, mockLdapMeta, null );
    testableLdapProtocol.connect( null, null );
    assertEquals( "123456", testableLdapProtocol.trustStorePassword );
  }

}
