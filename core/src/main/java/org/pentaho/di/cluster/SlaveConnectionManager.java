/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.cluster;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Encapsulates the Apache commons HTTP connection manager with a singleton. We can use this to limit the number of open
 * connections to slave servers.
 *
 * @author matt
 *
 */
public class SlaveConnectionManager {

  private static final String SSL = "SSL";
  private static final String KEYSTORE_SYSTEM_PROPERTY = "javax.net.ssl.keyStore";

  private static SlaveConnectionManager slaveConnectionManager;

  private MultiThreadedHttpConnectionManager manager;

  private SlaveConnectionManager() {
    if ( needToInitializeSSLContext() ) {
      try {
        SSLContext context = SSLContext.getInstance( SSL );
        context.init( new KeyManager[0], new X509TrustManager[] { getDefaultTrustManager() }, new SecureRandom() );
        SSLContext.setDefault( context );
      } catch ( Exception e ) {
        //log.logError( "Default SSL context hasn't been initialized", e );
      }
    }
    manager = new MultiThreadedHttpConnectionManager();
    manager.getParams().setDefaultMaxConnectionsPerHost( 100 );
    manager.getParams().setMaxTotalConnections( 200 );
  }

  private static boolean needToInitializeSSLContext() {
    return System.getProperty( KEYSTORE_SYSTEM_PROPERTY ) == null;
  }

  public static SlaveConnectionManager getInstance() {
    if ( slaveConnectionManager == null ) {
      slaveConnectionManager = new SlaveConnectionManager();
    }
    return slaveConnectionManager;
  }

  public HttpClient createHttpClient() {
    return new HttpClient( manager );
  }

  public void shutdown() {
    manager.shutdown();
  }

  private static X509TrustManager getDefaultTrustManager() {
    return new X509TrustManager() {
      @Override
      public void checkClientTrusted( X509Certificate[] certs, String param ) throws CertificateException {
      }

      @Override
      public void checkServerTrusted( X509Certificate[] certs, String param ) throws CertificateException {
        for ( X509Certificate cert : certs ) {
          cert.checkValidity(); // validate date
          // cert.verify( key ); // check by Public key
          // cert.getBasicConstraints()!=-1 // check by CA
        }
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };
  }

  static void reset() {
    slaveConnectionManager = null;
  }
}
