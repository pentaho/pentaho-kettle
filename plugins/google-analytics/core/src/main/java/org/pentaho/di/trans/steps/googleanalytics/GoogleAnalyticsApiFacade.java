/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2017 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

/* Copyright (c) 2008 Google Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pentaho.di.trans.steps.googleanalytics;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleAnalyticsApiFacade {
  private Analytics analytics;
  private final HttpTransport httpTransport;

  public static GoogleAnalyticsApiFacade createFor(
    String application, String oauthServiceAccount, String oauthKeyFile )
    throws GeneralSecurityException, IOException, KettleFileException {

    return new GoogleAnalyticsApiFacade(
      GoogleNetHttpTransport.newTrustedTransport(),
      JacksonFactory.getDefaultInstance(),
      application,
      oauthServiceAccount,
      new File( KettleVFS.getFileObject( oauthKeyFile ).getURL().getPath() )
    );
  }

  public GoogleAnalyticsApiFacade( HttpTransport httpTransport, JsonFactory jsonFactory, String application,
                                   String oathServiceEmail, File keyFile )
    throws IOException, GeneralSecurityException {

    Assert.assertNotNull( httpTransport, "HttpTransport cannot be null" );
    Assert.assertNotNull( jsonFactory, "JsonFactory cannot be null" );
    Assert.assertNotBlank( application, "Application name cannot be empty" );
    Assert.assertNotBlank( oathServiceEmail, "OAuth Service Email name cannot be empty" );
    Assert.assertNotNull( keyFile, "OAuth secret key file cannot be null" );

    this.httpTransport = httpTransport;

    Credential credential = new GoogleCredential.Builder()
      .setTransport( httpTransport )
      .setJsonFactory( jsonFactory )
      .setServiceAccountScopes( AnalyticsScopes.all() )
      .setServiceAccountId( oathServiceEmail )
      .setServiceAccountPrivateKeyFromP12File( keyFile )
      .build();

    analytics = new Analytics.Builder( httpTransport, jsonFactory, credential )
      .setApplicationName( application )
      .build();
  }

  public void close() throws IOException {
    httpTransport.shutdown();
  }

  public Analytics getAnalytics() {
    return analytics;
  }
}
