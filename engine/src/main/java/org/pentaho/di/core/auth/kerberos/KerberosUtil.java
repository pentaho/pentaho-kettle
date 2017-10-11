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

package org.pentaho.di.core.auth.kerberos;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.sun.security.auth.module.Krb5LoginModule;

public class KerberosUtil {
  /**
   * A Login Configuration that is pre-configured based on our static configuration.
   */
  public static class PentahoLoginConfiguration extends Configuration {
    private AppConfigurationEntry[] entries;

    public PentahoLoginConfiguration( AppConfigurationEntry[] entries ) {
      if ( entries == null ) {
        throw new NullPointerException( "AppConfigurationEntry[] is required" );
      }
      this.entries = entries;
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry( String ignored ) {
      return entries;
    }
  }

  /**
   * The application name to use when creating login contexts.
   */
  public static final String KERBEROS_APP_NAME = "pentaho";

  /**
   * The environment property to set to enable JAAS debugging for the LoginConfiguration created by this utility.
   */
  public static final String PENTAHO_JAAS_DEBUG = "PENTAHO_JAAS_DEBUG";

  /**
   * Base properties to be inherited by all other LOGIN_CONFIG* configuration maps.
   */
  public static final Map<String, String> LOGIN_CONFIG_BASE = createLoginConfigBaseMap();

  /**
   * Login Configuration options for KERBEROS_USER mode.
   */
  private static final Map<String, String> LOGIN_CONFIG_OPTS_KERBEROS_USER = getLoginConfigOptsKerberosUser();

  private static final Map<String, String> LOGIN_CONFIG_OPTS_KERBEROS_USER_NOPASS =
      getLoginConfigOptsKerberosNoPassword();

  private static Map<String, String> createLoginConfigBaseMap() {
    Map<String, String> result = new HashMap<String, String>();
    // Enable JAAS debug if PENTAHO_JAAS_DEBUG is set
    if ( Boolean.parseBoolean( System.getenv( PENTAHO_JAAS_DEBUG ) ) ) {
      result.put( "debug", Boolean.TRUE.toString() );
    }

    return Collections.unmodifiableMap( result );
  }

  private static Map<String, String> getLoginConfigOptsKerberosUser() {
    Map<String, String> result = new HashMap<String, String>( LOGIN_CONFIG_BASE );
    result.put( "useTicketCache", Boolean.FALSE.toString() );
    // Attempt to renew tickets
    result.put( "renewTGT", Boolean.FALSE.toString() );
    return Collections.unmodifiableMap( result );
  }

  private static Map<String, String> getLoginConfigOptsKerberosNoPassword() {
    Map<String, String> result = new HashMap<String, String>( LOGIN_CONFIG_OPTS_KERBEROS_USER );
    result.put( "useTicketCache", Boolean.TRUE.toString() );
    result.put( "renewTGT", Boolean.TRUE.toString() );
    // Never prompt for passwords
    result.put( "doNotPrompt", Boolean.TRUE.toString() );
    return result;
  }

  /**
   * Login Configuration options for KERBEROS_KEYTAB mode.
   */
  public static final Map<String, String> LOGIN_CONFIG_OPTS_KERBEROS_KEYTAB = createLoginConfigOptsKerberosKeytabMap();

  private static Map<String, String> createLoginConfigOptsKerberosKeytabMap() {
    Map<String, String> result = new HashMap<String, String>( LOGIN_CONFIG_BASE );
    // Never prompt for passwords
    result.put( "doNotPrompt", Boolean.TRUE.toString() );
    // Use a keytab file
    result.put( "useKeyTab", Boolean.TRUE.toString() );
    result.put( "storeKey", Boolean.TRUE.toString() );
    // Refresh KRB5 config before logging in
    result.put( "refreshKrb5Config", Boolean.TRUE.toString() );

    return Collections.unmodifiableMap( result );
  }

  public LoginContext getLoginContextFromKeytab( String principal, String keytab ) throws LoginException {
    Map<String, String> keytabConfig = new HashMap<String, String>( LOGIN_CONFIG_OPTS_KERBEROS_KEYTAB );
    keytabConfig.put( "keyTab", keytab );
    keytabConfig.put( "principal", principal );

    // Create the configuration and from them, a new login context
    AppConfigurationEntry config =
        new AppConfigurationEntry( Krb5LoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, keytabConfig );
    AppConfigurationEntry[] configEntries = new AppConfigurationEntry[] { config };
    Subject subject = new Subject();
    return new LoginContext( KERBEROS_APP_NAME, subject, null, new PentahoLoginConfiguration( configEntries ) );
  }

  public LoginContext getLoginContextFromUsernamePassword( final String principal, final String password ) throws LoginException {
    Map<String, String> opts = new HashMap<String, String>( LOGIN_CONFIG_OPTS_KERBEROS_USER );
    opts.put( "principal", principal );
    AppConfigurationEntry[] appConfigurationEntries =
        new AppConfigurationEntry[] { new AppConfigurationEntry( Krb5LoginModule.class.getName(),
            LoginModuleControlFlag.REQUIRED, opts ) };
    return new LoginContext( KERBEROS_APP_NAME, new Subject(), new CallbackHandler() {

      @Override
      public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
        for ( Callback callback : callbacks ) {
          if ( callback instanceof NameCallback ) {
            ( (NameCallback) callback ).setName( principal );
          } else if ( callback instanceof PasswordCallback ) {
            ( (PasswordCallback) callback ).setPassword( password.toCharArray() );
          } else {
            throw new UnsupportedCallbackException( callback );
          }
        }
      }
    }, new PentahoLoginConfiguration( appConfigurationEntries ) );
  }

  public LoginContext getLoginContextFromKerberosCache( String principal ) throws LoginException {
    Map<String, String> opts = new HashMap<String, String>( LOGIN_CONFIG_OPTS_KERBEROS_USER_NOPASS );
    opts.put( "principal", principal );
    AppConfigurationEntry[] appConfigurationEntries =
        new AppConfigurationEntry[] { new AppConfigurationEntry( Krb5LoginModule.class.getName(),
            LoginModuleControlFlag.REQUIRED, opts ) };
    return new LoginContext( KERBEROS_APP_NAME, new Subject(), null, new PentahoLoginConfiguration(
        appConfigurationEntries ) );
  }
}
