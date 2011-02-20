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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.pentaho.di.core.exception.KettleException;

public class Truster implements X509TrustManager {

    private String certStore;

    private char certStorePwd[];

    private X509TrustManager trustManager;

    private KeyStore ks;

    public Truster(String certStorePath) throws KettleException {
        certStore = null;
        certStorePwd = null;
        trustManager = null;
        ks = null;
        if (certStorePath == null) {
            certStore = "certs";
        } else {
            certStore = certStorePath;
        }
        certStorePwd = "".toCharArray();
        init();
    }

    public Truster(String certStorePath, String password)  throws KettleException {
        certStore = null;
        certStorePwd = null;
        trustManager = null;
        ks = null;
        if (certStorePath == null) {
            certStore = "certs";
        } else {
            certStore = certStorePath;
        }
        if (password != null && password.length() > 0) {
        	certStorePwd=password.toCharArray();
        }
        init();
    }

    public X509Certificate[] getAcceptedIssuers() {
        if (trustManager == null)
            return null;
        else
            return trustManager.getAcceptedIssuers();
    }

    private void init() throws KettleException {
        try {
            if (certStore.endsWith(".p12"))
                ks = KeyStore.getInstance("PKCS12");
            else
                ks = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
        	 throw new KettleException("Failed to create cert store : ", e);
        }
        InputStream in = null;
        if (certStore.indexOf("://") == -1)
            try {
                in = new FileInputStream(certStore);
            } catch (FileNotFoundException _ex) {
            }
        else
            try {
                URL url = new URL(certStore);
                URLConnection con = url.openConnection();
                in = con.getInputStream();
            } catch (MalformedURLException e) {
            	 throw new KettleException("The location of the cert store file is invalid: ", e);
            } catch (IOException _ex) {
            }
        try {
            ks.load(in, certStorePwd);
        } catch (Exception e) {
            throw new KettleException("Failed to load the cert store : " , e);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception _ex) {
                }
        }
        try {
            trustManager = initTrustManager(ks);
        } catch (Exception e) {
        	 throw new KettleException("Failed to create initial trust manager : ", e);
        }
    }

    private X509TrustManager initTrustManager(KeyStore ks) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = null;
        trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(ks);
        TrustManager trusts[] = trustManagerFactory.getTrustManagers();
        return (X509TrustManager) trusts[0];
    }

    /*  
     * Delegate to the default trust manager.  
     */  
    public void checkClientTrusted(X509Certificate[] chain, String authType)   
                throws CertificateException {   
        try {   
        	trustManager.checkClientTrusted(chain, authType);   
        } catch (CertificateException excep) {    
        }   
    }   
    
    /*  
     * Delegate to the default trust manager.  
     */  
    public void checkServerTrusted(X509Certificate[] chain, String authType)   
                throws CertificateException {   
        try {   
        	trustManager.checkServerTrusted(chain, authType);   
        } catch (CertificateException excep) {   
        }   
    }   
}
