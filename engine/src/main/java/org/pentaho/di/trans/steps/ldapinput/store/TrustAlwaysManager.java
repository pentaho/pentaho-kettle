/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
