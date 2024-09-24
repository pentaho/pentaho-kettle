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

package org.pentaho.di.trans.steps.ldapinput.store;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

@SuppressWarnings( "squid:S4424" )
public class TrustAlwaysManager implements X509TrustManager {
  public void checkClientTrusted( X509Certificate[] cert, String authType ) {
    //no work to do since our purpose here is to always trust
  }

  public void checkServerTrusted( X509Certificate[] cert, String authType ) {
    //no work to do since our purpose here is to always trust
  }

  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }
}
