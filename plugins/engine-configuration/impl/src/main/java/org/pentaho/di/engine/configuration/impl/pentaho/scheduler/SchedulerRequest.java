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

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by bmorrise on 8/22/17.
 */
public class SchedulerRequest {

  public static final String API_SCHEDULER_JOB = "/plugin/scheduler-plugin/api/scheduler/job";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String APPLICATION_XML = "application/xml";
  public static final String UTF_8 = "UTF-8";
  public static final String AUTHORIZATION = "Authorization";

  public static final String STRING_TYPE = "string";

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
    try {
      httpPost.setEntity( buildSchedulerRequestEntity( meta ) );
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

  StringEntity buildSchedulerRequestEntity( AbstractMeta meta )
          throws UnsupportedEncodingException, UnknownParamException {

    String filename = getFullPath( meta );

    JobScheduleRequest jobScheduleRequest = new JobScheduleRequest();
    jobScheduleRequest.setInputFile( filename );

    // Set the log level
    if ( meta.getLogLevel() != null ) {
      JobScheduleParam jobScheduleParam = new JobScheduleParam();
      jobScheduleParam.setName( "logLevel" );
      jobScheduleParam.setType( STRING_TYPE );
      jobScheduleParam.setStringValue( Arrays.asList( meta.getLogLevel().getCode() ) );
      jobScheduleRequest.getJobParameters().add( jobScheduleParam );
    }

    // Set the clearing log param
    JobScheduleParam jobScheduleParamClearingLog = new JobScheduleParam();
    jobScheduleParamClearingLog.setName( "clearLog" );
    jobScheduleParamClearingLog.setType( STRING_TYPE );
    jobScheduleParamClearingLog
      .setStringValue( Arrays.asList( String.valueOf( meta.isClearingLog() ) ) );
    jobScheduleRequest.getJobParameters().add( jobScheduleParamClearingLog );

    // Set the safe mode enabled param
    JobScheduleParam jobScheduleParamSafeModeEnable = new JobScheduleParam();
    jobScheduleParamSafeModeEnable.setName( "runSafeMode" );
    jobScheduleParamSafeModeEnable.setType( STRING_TYPE );
    jobScheduleParamSafeModeEnable
      .setStringValue( Arrays.asList( String.valueOf( meta.isSafeModeEnabled() ) ) );
    jobScheduleRequest.getJobParameters().add( jobScheduleParamSafeModeEnable );


    // Set the gathering metrics param
    JobScheduleParam jobScheduleParamGatheringMetricsParam = new JobScheduleParam();
    jobScheduleParamGatheringMetricsParam.setName( "gatheringMetrics" );
    jobScheduleParamGatheringMetricsParam.setType( STRING_TYPE );
    jobScheduleParamGatheringMetricsParam
      .setStringValue( Arrays.asList( String.valueOf( meta.isGatheringMetrics() ) ) );
    jobScheduleRequest.getJobParameters().add( jobScheduleParamGatheringMetricsParam );

    if ( meta instanceof JobMeta ) {
      JobMeta jobMeta = (JobMeta) meta;

      if ( jobMeta.getStartCopyName() != null ) {
        // Set the start step name
        JobScheduleParam jobScheduleParamStartStepName = new JobScheduleParam();
        jobScheduleParamStartStepName.setName( "startCopyName" );
        jobScheduleParamStartStepName.setType( STRING_TYPE );
        jobScheduleParamStartStepName.setStringValue( Arrays.asList( jobMeta.getStartCopyName() ) );
        jobScheduleRequest.getJobParameters().add( jobScheduleParamStartStepName );
      }

      // Set the expanding remote job param
      JobScheduleParam jobScheduleParamExpandingRemoteJob = new JobScheduleParam();
      jobScheduleParamExpandingRemoteJob.setName( "expandingRemoteJob" );
      jobScheduleParamExpandingRemoteJob.setType( STRING_TYPE );
      jobScheduleParamExpandingRemoteJob
        .setStringValue( Arrays.asList( String.valueOf( jobMeta.isExpandingRemoteJob() ) ) );
      jobScheduleRequest.getJobParameters().add( jobScheduleParamExpandingRemoteJob );
    }

    // Set the PDI parameters
    if ( meta.listParameters() != null ) {
      for ( String param : meta.listParameters() ) {
        jobScheduleRequest.getPdiParameters().put( param, meta.getParameterValue( param ) );
      }
    }

    // Marshal object to string
    StringWriter sw = new StringWriter();
    String jobScheduleRequestString = null;
    try {
      JAXBContext jc = JAXBContext.newInstance( JobScheduleRequest.class );
      Marshaller marshaller = jc.createMarshaller();
      marshaller.marshal( jobScheduleRequest, sw );
      jobScheduleRequestString = sw.toString();
    } catch ( JAXBException e ) {
      repository.getLog().logError( BaseMessages.getString( PKG, "SchedulerRequest.error.marshal" ), e.getMessage() );
    }

    return new StringEntity( jobScheduleRequestString );
  }

}
