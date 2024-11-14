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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.ldapinput.store.CustomSocketFactory;

public class LdapTlsProtocol extends LdapSslProtocol {
  private StartTlsResponse startTlsResponse;

  public LdapTlsProtocol( LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
    Collection<String> binaryAttributes ) {
    super( log, variableSpace, meta, binaryAttributes );
  }

  @Override
  protected String getConnectionPrefix() {
    return "ldap://";
  }

  public static String getName() {
    return "LDAP TLS";
  }

  @Override
  protected void doConnect( String username, String password ) throws KettleException {
    super.doConnect( username, password );
    StartTlsRequest tlsRequest = new StartTlsRequest();
    try {
      this.startTlsResponse = (StartTlsResponse) getCtx().extendedOperation( tlsRequest );
      /* Starting TLS */
      this.startTlsResponse.negotiate( CustomSocketFactory.getDefault() );
    } catch ( NamingException e ) {
      throw new KettleException( e );
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }

  @Override
  protected void configureSslEnvironment( Map<String, String> env ) {
    // noop
  }

  @Override
  public void close() throws KettleException {
    if ( startTlsResponse != null ) {
      try {
        startTlsResponse.close();
      } catch ( IOException e ) {
        throw new KettleException( e );
      } finally {
        startTlsResponse = null;
      }
    }
    super.close();
  }
}
