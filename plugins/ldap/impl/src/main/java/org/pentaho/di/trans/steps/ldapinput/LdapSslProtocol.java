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
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.ldapinput.store.CustomSocketFactory;

public class LdapSslProtocol extends LdapProtocol {

  private final boolean trustAllCertificates;

  private final String trustStorePath;

  private final String trustStorePassword;

  public LdapSslProtocol( LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
    Collection<String> binaryAttributes ) {
    super( log, variableSpace, meta, binaryAttributes );
    String trustStorePath = null;
    String trustStorePassword = null;
    boolean trustAllCertificates = false;

    if ( meta.isUseCertificate() ) {
      trustStorePath = variableSpace.environmentSubstitute( meta.getTrustStorePath() );
      trustStorePassword =  Utils.resolvePassword( variableSpace,
              meta.getTrustStorePassword() );
      trustAllCertificates = meta.isTrustAllCertificates();
    }

    this.trustAllCertificates = trustAllCertificates;
    this.trustStorePath = trustStorePath;
    this.trustStorePassword = trustStorePassword;
  }

  @Override
  protected String getConnectionPrefix() {
    return "ldaps://";
  }

  public static String getName() {
    return "LDAP SSL";
  }

  protected void configureSslEnvironment( Map<String, String> env ) {
    env.put( javax.naming.Context.SECURITY_PROTOCOL, "ssl" );
    env.put( "java.naming.ldap.factory.socket", CustomSocketFactory.class.getCanonicalName() );
  }

  @Override
  protected void setupEnvironment( Map<String, String> env, String username, String password ) throws KettleException {
    super.setupEnvironment( env, username, password );
    configureSslEnvironment( env );
    configureSocketFactory( trustAllCertificates, trustStorePath, trustStorePassword );
  }

  protected void configureSocketFactory( boolean trustAllCertificates, String trustStorePath,
    String trustStorePassword ) throws KettleException {
    if ( trustAllCertificates ) {
      CustomSocketFactory.configure();
    } else {
      CustomSocketFactory.configure( trustStorePath, trustStorePassword );
    }
  }
}
