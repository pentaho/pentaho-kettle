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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.pentaho.di.core.exception.KettleException;

public class CustomdSocketFactory extends SSLSocketFactory {
	
	private SSLSocketFactory factory;
	
    private static TrustManager trustManagers[] = null;

    private static CustomdSocketFactory default_factory = null;

    private static String certStorePath = null;

    private static String certStorePwd = null;
	
	private static final TrustManager[] ALWAYS_TRUST_MANAGER = new TrustManager[] { new TrustAlwaysManager() };
	
	protected CustomdSocketFactory() {
        factory = null;
        init(null, null);
    }

    protected CustomdSocketFactory(InputStream in, String keyStore, String password) throws Exception {
        factory = null;
        KeyStore ks = null;
        if (keyStore.endsWith(".p12"))
            ks = KeyStore.getInstance("PKCS12");
        else
            ks = KeyStore.getInstance("JKS");
        char pwd[] = password.toCharArray();
        ks.load(in, pwd);
        init(ks, pwd);
    }

    protected CustomdSocketFactory(String keyStore, String passphrase) {
        factory = null;
        init(null, null);
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress client_host, int client_port) throws IOException,
            UnknownHostException {
        return factory.createSocket(host, port, client_host, client_port);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(InetAddress host, int port, InetAddress client_host, int client_port) throws IOException,
            UnknownHostException {
        return factory.createSocket(host, port, client_host, client_port);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoclose) throws IOException, UnknownHostException {
        return factory.createSocket(socket, host, port, autoclose);
    }

    public static synchronized SocketFactory getDefault() {
        return getDefaultFactory();
    }

    public static void setCertStorePath(String path) {
    	CustomdSocketFactory.certStorePath = path;
    }

    public static void setCertStorePassword(String password) {
    	CustomdSocketFactory.certStorePwd = password;
    }

    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    private static SocketFactory getDefaultFactory() {
        if (default_factory == null)
            default_factory = new CustomdSocketFactory();
        return default_factory;
    }

    private TrustManager[] getDefaultTrustManager() throws KettleException {
        if (trustManagers == null)
            if (CustomdSocketFactory.certStorePwd != null) {
                trustManagers = (new Truster[] { new Truster(CustomdSocketFactory.certStorePath,
                		CustomdSocketFactory.certStorePwd) });
            } else {
                trustManagers = (new Truster[] { new Truster(CustomdSocketFactory.certStorePath) });
            }
        return trustManagers;
    }

    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    private void init(KeyStore ks, char password[]) {
        SSLContext ctx = null;
        KeyManager keyManagers[] = null;
        TrustManager trustManagers[] = null;
        try {
            if (ks != null) {
                KeyManagerFactory kmf = null;
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);
                keyManagers = kmf.getKeyManagers();
            }
            ctx = SSLContext.getInstance("TLS");
            trustManagers = getDefaultTrustManager();
            ctx.init(keyManagers, trustManagers, null);
            factory = ctx.getSocketFactory();
        } catch (Exception e) {
            System.err.println("Error: failed to initialize : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void alwaysTrust() {
        trustManagers = ALWAYS_TRUST_MANAGER;
    }
}