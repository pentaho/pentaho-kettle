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

package org.pentaho.di.trans.steps.ldapinput.store;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.ldapinput.LDAPInputMeta;

/**
 * This is a wrapper around a standard X509TrustManager. It's just initialized in a specific way for Kettle purposes.
 *
 */
public class KettleTrustManager implements X509TrustManager {

  private static Class<?> PKG = LDAPInputMeta.class; // i18n purposes

  /**
   * The trust manager around which we wrap ourselves in this class.
   */
  private X509TrustManager tm;

  /**
   *
   * @param certStorePath
   * @param certPassword
   * @throws KettleException
   */
  public KettleTrustManager( KeyStore keyStore, String certFilename, String certPassword ) throws KettleException {
    try {
      // Load the CERT key from the file into the store using the provided
      // password if needed.
      //
      InputStream inputStream = null;
      try {
        inputStream = KettleVFS.getInputStream( certFilename );
        keyStore.load( inputStream, Const.NVL( certPassword, "" ).toCharArray() );
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "KettleTrustManager.Exception.CouldNotOpenCertStore" ), e );
      } finally {
        if ( inputStream != null ) {
          try {
            inputStream.close();
          } catch ( Exception e ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "KettleTrustManager.Exception.CouldNotOpenCertStore" ), e );
          }
        }
      }

      // Now initialize the trust manager...
      //
      try {
        TrustManagerFactory tmf = null;
        tmf = TrustManagerFactory.getInstance( "SunX509" );
        tmf.init( keyStore );
        TrustManager[] tms = tmf.getTrustManagers();
        tm = (X509TrustManager) tms[0];
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "KettleTrustManager.Exception.CouldNotInitializeTrustManager" ), e );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "KettleTrustManager.Exception.CouldNotInitializeKettleTrustManager" ), e );
    }
  }

  /**
   * Pass method from x509TrustManager to this class...
   *
   * @return an array of certificate authority certificates which are trusted for authenticating peers
   */
  public X509Certificate[] getAcceptedIssuers() {
    if ( tm == null ) {
      return null;
    }
    return tm.getAcceptedIssuers();
  }

  /**
   * Pass method from x509TrustManager to this class...
   *
   * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root
   * and return if it can be validated and is trusted for client SSL authentication based on the authentication type
   */
  public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
    if ( tm == null ) {
      return;
    }
    tm.checkClientTrusted( chain, authType );
  }

  /**
   * Pass method from x509TrustManager to this class...
   *
   * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root
   * and return if it can be validated and is trusted for server SSL authentication based on the authentication type
   */
  public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
    if ( tm == null ) {
      return;
    }
    tm.checkServerTrusted( chain, authType );
  }
}
