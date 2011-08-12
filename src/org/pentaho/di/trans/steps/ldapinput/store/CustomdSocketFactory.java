/* * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.ldapinput.store;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.pentaho.di.core.exception.KettleException;

public class CustomdSocketFactory extends SSLSocketFactory {

  private static boolean configured;
  
  private static TrustManager trustManagers[] = null;

  private static final TrustManager[] ALWAYS_TRUST_MANAGER = new TrustManager[] { new TrustAlwaysManager() };

  private SSLSocketFactory factory;
  
  /**
   * Required for reflection.
   */
  public CustomdSocketFactory() {
    super();
  }
  
  /**
   * For internal use only.
   */
  protected CustomdSocketFactory(SSLSocketFactory factory) {
    this.factory = factory;
  }
  
  public static synchronized SocketFactory getDefault() {
    if (!configured) {
      throw new IllegalStateException();
    }

    SSLContext ctx;
    try {
      ctx = SSLContext.getInstance("TLS");
      ctx.init(null, trustManagers, null);
    } catch (KeyManagementException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return new CustomdSocketFactory(ctx.getSocketFactory());
  }

  /**
   * Configures this SSLSocketFactory so that it uses the given keystore as its truststore.
   */
  public static synchronized void configure(String path, String password) throws KettleException {
    if (password != null) {
      trustManagers = new Truster[] { new Truster(path, password) };
    } else {
      trustManagers = new Truster[] { new Truster(path) };
    }
    configured = true;
  }
  
  /**
   * Configures this SSLSocketFactory so that it trusts any signer.
   */
  public static synchronized void configure() {
    trustManagers = ALWAYS_TRUST_MANAGER;
    configured = true;
  }
  
  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return factory.createSocket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress client_host, int client_port) throws IOException,
      UnknownHostException {
    return factory.createSocket(host, port, client_host, client_port);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException, UnknownHostException {
    return factory.createSocket(host, port);
  }

  @Override
  public Socket createSocket(InetAddress host, int port, InetAddress client_host, int client_port) throws IOException,
      UnknownHostException {
    return factory.createSocket(host, port, client_host, client_port);
  }

  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean autoclose) throws IOException,
      UnknownHostException {
    return factory.createSocket(socket, host, port, autoclose);
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