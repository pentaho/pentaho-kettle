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

import java.io.UnsupportedEncodingException;
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

  public SchedulerRequest( Builder builder ) {
    this.httpPost = builder.httpPost;
  }

  public static class Builder {

    private HttpPost httpPost;
    private String username;
    private String password;

    public SchedulerRequest build() {
      httpPost = new HttpPost( SpoonUtil.getRepositoryServiceBaseUrl() + API_SCHEDULER_JOB );
      httpPost.setHeader( CONTENT_TYPE, APPLICATION_XML );
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

    public Builder authentication( String username, String password ) {
      this.username = username;
      this.password = password;
      return this;
    }
  }

  public void submit( String filename ) {
    try {
      httpPost.setEntity( new StringEntity( "<jobScheduleRequest>\n"
        + "<inputFile>" + filename + "</inputFile>\n"
        + "</jobScheduleRequest>" ) );
      httpclient.execute( httpPost );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
}
