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


package org.pentaho.di.trans.steps.ldapinput.store;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.ldapinput.LDAPInputMeta;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomSocketFactory extends SSLSocketFactory {

  private static Class<?> PKG = LDAPInputMeta.class; // for i18n purposes, needed by Translator2!!

  // private static boolean configured;
  private static AtomicBoolean configured = new AtomicBoolean( false );

  private static TrustManager[] trustManagers = null;

  private static final TrustManager[] ALWAYS_TRUST_MANAGER = new TrustManager[] { new TrustAlwaysManager() };

  private SSLSocketFactory factory;

  /**
   * Required for reflection.
   */
  public CustomSocketFactory() {
    super();
  }

  /**
   * For internal use only.
   */
  protected CustomSocketFactory( SSLSocketFactory factory ) {
    this.factory = factory;
  }

  public static CustomSocketFactory getDefault() {
    if ( !configured.get() ) {
      throw new IllegalStateException();
    }

    SSLContext ctx;
    try {
      ctx = SSLContext.getInstance( "TLS" );
      ctx.init( null, trustManagers, null );
    } catch ( KeyManagementException e ) {
      throw new RuntimeException( e );
    } catch ( NoSuchAlgorithmException e ) {
      throw new RuntimeException( e );
    }
    return new CustomSocketFactory( ctx.getSocketFactory() );
  }

  /**
   * Configures this SSLSocketFactory so that it uses the given keystore as its truststore.
   */
  public static synchronized void configure( Bowl bowl, String path, String password ) throws KettleException {

    // Get the appropriate key-store based on the file path...
    //
    KeyStore keyStore;

    try {
      if ( !Utils.isEmpty( path ) && path.endsWith( ".p12" ) ) {
        keyStore = KeyStore.getInstance( "PKCS12" );
      } else {
        keyStore = KeyStore.getInstance( "JKS" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "KettleTrustManager.Exception.CouldNotCreateCertStore" ), e );
    }

    trustManagers = new KettleTrustManager[] { new KettleTrustManager( bowl, keyStore, path, password ) };
    configured.set( true );
  }

  /**
   * Configures this SSLSocketFactory so that it trusts any signer.
   */
  public static synchronized void configure() {
    trustManagers = ALWAYS_TRUST_MANAGER;
    configured.set( true );
  }

  @Override
  public Socket createSocket( String host, int port ) throws IOException, UnknownHostException {
    return factory.createSocket( host, port );
  }

  @Override
  public Socket createSocket( String host, int port, InetAddress client_host, int client_port ) throws IOException, UnknownHostException {
    return factory.createSocket( host, port, client_host, client_port );
  }

  @Override
  public Socket createSocket( InetAddress host, int port ) throws IOException, UnknownHostException {
    return factory.createSocket( host, port );
  }

  @Override
  public Socket createSocket( InetAddress host, int port, InetAddress client_host, int client_port ) throws IOException, UnknownHostException {
    return factory.createSocket( host, port, client_host, client_port );
  }

  @Override
  public Socket createSocket( Socket socket, String host, int port, boolean autoclose ) throws IOException,
    UnknownHostException {
    return factory.createSocket( socket, host, port, autoclose );
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return factory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
  }

}
