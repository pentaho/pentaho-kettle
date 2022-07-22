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
