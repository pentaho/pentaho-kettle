/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Base64;

/**
 * Created by bmorrise on 8/22/17.
 */
public class SchedulerRequest {

  public static final String API_SCHEDULER_JOB = "/api/scheduler/job";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String APPLICATION_XML = "application/xml";
  public static final String UTF_8 = "UTF-8";
  public static final String AUTHORIZATION = "Authorization";
  private HttpClient httpclient = HttpClients.createDefault();
  private HttpPost httpPost;
  private Repository repository;
  private String baseUrl;

  public SchedulerRequest( Builder builder ) {
    this.httpPost = builder.httpPost;
    this.repository = builder.repository;
    this.baseUrl = builder.baseUrl;
  }

  private static Class<?> PKG = SchedulerRequest.class;

  public static class Builder {

    private HttpPost httpPost;
    private Repository repository;
    private String baseUrl;

    public SchedulerRequest build() {
      baseUrl = getRepositoryServiceBaseUrl();
      httpPost = new HttpPost( baseUrl + API_SCHEDULER_JOB );
      httpPost.setHeader( CONTENT_TYPE, APPLICATION_XML );

      String username = repository.getUserInfo().getName();
      String password = repository.getUserInfo().getPassword();

      if ( username != null && password != null ) {

        byte[] encoding;
        try {
          String userPass = username + ":" + password;
          encoding = Base64.getEncoder().encode( userPass.getBytes( UTF_8 ) );
          httpPost.setHeader( AUTHORIZATION, "Basic " + new String( encoding ) );
        } catch ( UnsupportedEncodingException e ) {
          e.printStackTrace();
        }
      }
      return new SchedulerRequest( this );
    }

    public Builder repository( Repository repository ) {
      this.repository = repository;
      return this;
    }

    public String getRepositoryServiceBaseUrl() {
      String repoLocation = "http://localhost:8080/pentaho"; //$NON-NLS-1$

      try {
        Method m = repository.getRepositoryMeta().getClass().getMethod( "getRepositoryLocation" );
        Object loc = m.invoke( repository.getRepositoryMeta() );
        m = loc.getClass().getMethod( "getUrl" );
        repoLocation = (String) m.invoke( loc );
      } catch ( Exception ex ) {
        // Ignore
      }
      return repoLocation;
    }
  }

  public void submit( AbstractMeta meta ) {
    String filename = getFullPath( meta );
    try {
      httpPost.setEntity( new StringEntity( "<jobScheduleRequest>\n"
        + "<inputFile>" + filename + "</inputFile>\n"
        + "</jobScheduleRequest>" ) );
      httpclient.execute( httpPost );
      logMessage();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private void logMessage() {
    String url = baseUrl + "/kettle/status";
    String message = "[" + url + "](" + url + ")";
    repository.getLog().logBasic( BaseMessages.getString( PKG, "SchedulerRequest.submit.message" ), message );
  }

  private String getFullPath( AbstractMeta meta ) {
    return meta.getRepositoryDirectory().getPath() + "/" + meta.getName() + "." + meta.getDefaultExtension();
  }

}
