/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.ldapinput;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import com.google.common.base.Joiner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Class encapsulating Ldap protocol configuration
 */
public class LdapProtocol {
  private static Class<?> PKG = LdapProtocol.class; // for i18n purposes, needed by Translator2!!

  private final String hostname;

  private final int port;

  private final String derefAliases;

  private final String referral;

  private final LogChannelInterface log;

  private InitialLdapContext ctx;

  private final Set<String> binaryAttributes;

  public InitialLdapContext getCtx() {
    return ctx;
  }

  public LdapProtocol( LogChannelInterface log, VariableSpace variableSpace, LdapMeta meta,
                       Collection<String> binaryAttributes ) {
    this.log = log;
    hostname = variableSpace.environmentSubstitute( meta.getHost() );
    port = Const.toInt( variableSpace.environmentSubstitute( meta.getPort() ), LDAPConnection.DEFAULT_PORT );
    derefAliases = meta.getDerefAliases();
    referral = meta.getReferrals();

    if ( binaryAttributes == null ) {
      this.binaryAttributes = new HashSet<String>();
    } else {
      this.binaryAttributes = new HashSet<String>( binaryAttributes );
    }
  }

  protected String getConnectionPrefix() {
    return "ldap://";
  }

  /**
   * Method signature used by factory to get display name, method should exist in every ldap protocol
   *
   * @return the display name
   */
  public static String getName() {
    return "LDAP";
  }

  protected void setupEnvironment( Map<String, String> env, String username, String password ) throws KettleException {
    env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
    env.put( "java.naming.ldap.derefAliases", derefAliases );
    env.put( Context.REFERRAL, referral );

    if ( hostname.startsWith( getConnectionPrefix() ) ) {
      env.put( Context.PROVIDER_URL, hostname + ":" + port );
    } else {
      env.put( Context.PROVIDER_URL, getConnectionPrefix() + hostname + ":" + port );
    }

    if ( !Utils.isEmpty( username ) ) {
      env.put( Context.SECURITY_PRINCIPAL, username );
      env.put( Context.SECURITY_CREDENTIALS, password );
      env.put( Context.SECURITY_AUTHENTICATION, "simple" );
    } else {
      env.put( Context.SECURITY_AUTHENTICATION, "none" );
    }

    if ( binaryAttributes.size() > 0 ) {
      env.put( "java.naming.ldap.attributes.binary", Joiner.on( " " ).join( binaryAttributes ) );
    }
  }

  protected InitialLdapContext createLdapContext( Hashtable<String, String> env ) throws NamingException {
    // PDI-19783 : The classes needed to create ldap were loaded inside of plugin classloader, so we need to
    // override the current thread classloader to be able to create the ldap context.
    ClassLoader currentThreadClassloader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
      InitialLdapContext initialLdapContext = new InitialLdapContext( env, null );
      return initialLdapContext;
    } finally {
      Thread.currentThread().setContextClassLoader( currentThreadClassloader );
    }
  }

  protected void doConnect( String username, String password ) throws KettleException {
    Hashtable<String, String> env = new Hashtable<String, String>();
    setupEnvironment( env, username, password );
    try {
      ctx = createLdapContext( env );
    } catch ( NamingException e ) {
      throw new KettleException( e );
    }
  }

  public final void connect( String username, String password ) throws KettleException {
    Hashtable<String, String> env = new Hashtable<String, String>();
    setupEnvironment( env, username, password );
    try {
      /* Establish LDAP association */
      doConnect( username, password );

      if ( log.isBasic() ) {
        log.logBasic( BaseMessages.getString( PKG, "LDAPInput.Log.ConnectedToServer", hostname, Const.NVL(
          username, "" ) ) );
      }
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "LDAPInput.ClassUsed.Message", ctx.getClass().getName() ) );
      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "LDAPinput.Exception.ErrorConnecting", e
        .getMessage() ), e );
    }
  }

  public void close() throws KettleException {
    if ( ctx != null ) {
      try {
        ctx.close();
        if ( log.isBasic() ) {
          log.logBasic( BaseMessages.getString( PKG, "LDAPInput.log.Disconnection.Done" ) );
        }
      } catch ( Exception e ) {
        log.logError( BaseMessages.getString( PKG, "LDAPInput.Exception.ErrorDisconecting", e.toString() ) );
        log.logError( Const.getStackTracker( e ) );
      } finally {
        ctx = null;
      }
    }
  }
}
