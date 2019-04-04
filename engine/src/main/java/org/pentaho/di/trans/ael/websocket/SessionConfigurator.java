/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket;

import com.google.common.base.Strings;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.auth.SPNegoScheme;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.protocol.HttpContext;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;
import java.net.URI;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SessionConfigurator extends ClientEndpointConfig.Configurator {
  private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
  private static final String NEGOTIATE = "NEGOTIATE";
  private static final String LOGIN_MODULE_NAME = "com.sun.security.auth.module.Krb5LoginModule";
  private static final String ERROR_MSG = "Error starting websocket session";

  private static final String USE_KEY_TAB_OPT = "useKeyTab";
  private static final String KEY_TAB_OPT = "keyTab";
  private static final String PRINCIPAL_OPT = "principal";
  private static final String STORE_KEY_OPT = "storeKey";
  private static final String USE_TICKET_CACHE_OPT = "useTicketCache";
  private static final String DO_NOT_PROMPT_OPT = "doNotPrompt";
  private static final String IS_INITIATOR_OPT = "isInitiator";

  private boolean withAuth = false;
  private String principal = null;
  private String keytab = null;
  private URI url;

  private LoginContext loginContext;

  private static final Credentials credentials = new NullCredentials();
  private static final Header AUTHENTICATE_HEADER = new Header() {
    @Override public String getName() {
      return WWW_AUTHENTICATE;
    }

    @Override public String getValue() {
      return NEGOTIATE;
    }

    @Override public HeaderElement[] getElements() throws ParseException {
      return new HeaderElement[ 0 ];
    }
  };

  public SessionConfigurator( URI url, String keytab, String principal ) {
    this.url = url;
    this.withAuth = !Strings.isNullOrEmpty( principal );
    this.principal = principal;
    this.keytab = keytab;
  }

  @Override
  public void beforeRequest( Map<String, List<String>> headers ) {
    if ( withAuth ) {
      Header authenticationHeader = getAuthenticationHeader( url );
      if ( authenticationHeader != null ) {
        headers
          .put( authenticationHeader.getName(), Collections.singletonList( authenticationHeader.getValue() ) );
      }
    }
  }

  @Override
  public void afterResponse( HandshakeResponse hr ) {
    try {
      if ( loginContext != null ) {
        loginContext.logout();
      }
    } catch ( LoginException e ) {
      e.printStackTrace();
      //work is done just ignore
    }
  }

  private Header getAuthenticationHeader( URI uri ) throws RuntimeException {
    try {
      ClientLoginConfig loginConfig =
        new ClientLoginConfig( this.keytab, this.principal );

      Subject serviceSubject = getServiceSubject( loginConfig );
      return Subject.doAs( serviceSubject, new PrivilegedAction<Header>() {
        public Header run() {
          // First try without stripping the port
          RuntimeException saveFirstException;
          try {
            return spnegoAuthenticate( false, uri );
          } catch ( Exception e ) {
            saveFirstException = new RuntimeException( e );
          }
          // if fails let's try stripping the port
          try {
            return spnegoAuthenticate( true, uri );
          } catch ( Exception e ) {
            //let's send the first exception
            throw saveFirstException;
          }
        }
      } );
    } catch ( RuntimeException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new RuntimeException( ERROR_MSG, e );
    }
  }

  private Header spnegoAuthenticate( boolean stripPort, URI uri ) throws Exception {
    SPNegoSchemeFactory spNegoSchemeFactory = new SPNegoSchemeFactory( stripPort );
    // using newInstance method instead of create method to be compatible httpclient library from 4.2 to 4.5
    // the create method was introduced at version 4.3
    SPNegoScheme spNegoScheme = (SPNegoScheme) spNegoSchemeFactory.newInstance( null );
    spNegoScheme.processChallenge( AUTHENTICATE_HEADER );
    return spNegoScheme.authenticate( credentials, new HttpGet( "" ), getContext( uri ) );
  }

  private HttpContext getContext( URI uri ) {
    HttpClientContext httpClientContext = HttpClientContext.create();
    //used by httpclient version >= 4.3
    httpClientContext
      .setAttribute( HttpClientContext.HTTP_ROUTE, new HttpRoute( new HttpHost( uri.getHost(), uri.getPort() ) ) );
    //used by httpclient version 4.2
    httpClientContext
      .setAttribute( HttpClientContext.HTTP_TARGET_HOST, new HttpHost( uri.getHost(), uri.getPort() ) );
    return httpClientContext;
  }

  private Subject getServiceSubject( ClientLoginConfig loginConfig ) throws Exception {
    Set<Principal> princ = new HashSet<>( 1 );
    princ.add( new KerberosPrincipal( this.principal ) );
    Subject sub = new Subject( false, princ, new HashSet(), new HashSet() );
    loginContext = new LoginContext( "", sub, null, loginConfig );
    loginContext.login();
    return loginContext.getSubject();
  }

  private static class NullCredentials implements Credentials {
    private NullCredentials() {
    }

    public Principal getUserPrincipal() {
      return null;
    }

    public String getPassword() {
      return null;
    }
  }

  private static class ClientLoginConfig extends Configuration {
    private final String keyTabLocation;
    private final String userPrincipal;

    private ClientLoginConfig( String keyTabLocation, String userPrincipal ) {
      this.keyTabLocation = keyTabLocation;
      this.userPrincipal = userPrincipal;
    }

    public AppConfigurationEntry[] getAppConfigurationEntry( String name ) {
      Map<String, Object> options = new HashMap<>();
      if ( !Strings.isNullOrEmpty( this.keyTabLocation ) && !Strings.isNullOrEmpty( this.userPrincipal ) ) {
        options.put( USE_KEY_TAB_OPT, Boolean.TRUE.toString() );
        options.put( KEY_TAB_OPT, this.keyTabLocation );
        options.put( PRINCIPAL_OPT, this.userPrincipal );
        options.put( STORE_KEY_OPT, Boolean.TRUE.toString() );
      } else {
        options.put( USE_TICKET_CACHE_OPT, Boolean.TRUE.toString() );
      }

      options.put( DO_NOT_PROMPT_OPT, Boolean.TRUE.toString() );
      options.put( IS_INITIATOR_OPT, Boolean.TRUE.toString() );

      return new AppConfigurationEntry[] { new AppConfigurationEntry( LOGIN_MODULE_NAME,
        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options ) };
    }
  }
}
