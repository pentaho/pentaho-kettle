/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerRequestTest {
  // input file
  private static final String TEST_REPOSITORY_DIRECTORY = "/home/admin";
  private static final String TEST_JOB_NAME = "jobName and special characters & < >";
  private static final String JOB_EXTENSION = "kjb";

  // job parameters
  private static final String STRING_PARAM_TYPE = "string";
  private static final String LOG_LEVEL_PARAM_NAME = "logLevel";
  private static final String TEST_LOG_LEVEL_PARAM_VALUE = "Rowlevel";
  private static final String CLEAR_LOG_PARAM_NAME = "clearLog";
  private static final String TEST_CLEAR_LOG_PARAM_VALUE = "true";
  private static final String RUN_SAFE_MODE_PARAM_NAME = "runSafeMode";
  private static final String TEST_RUN_SAFE_MODE_PARAM_VALUE = "false";
  private static final String GATHERING_METRICS_PARAM_NAME = "gatheringMetrics";
  private static final String TEST_GATHERING_METRICS_PARAM_VALUE = "false";
  private static final String START_COPY_NAME_PARAM_NAME = "startCopyName";
  private static final String TEST_START_COPY_NAME_PARAM_VALUE = "stepName";
  private static final String EXPANDING_REMOTE_JOB_PARAM_NAME = "expandingRemoteJob";
  private static final String TEST_EXPANDING_REMOTE_JOB_PARAM_VALUE = "false";

  // pdi parameters
  private static final String TEST_PDI_PARAM_NAME = "paramName";
  private static final String TEST_PDI_PARAM_VALUE = "paramValue";
  private static final String[] ARRAY_WITH_TEST_PDI_PARAM_NAME = new String[]{TEST_PDI_PARAM_NAME};

  private static final String REFERENCE_TEST_REQUEST = String.format(
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<jobScheduleRequest>"
      + "<inputFile>%s/%s.%s</inputFile>"
      + "<outputFile>%s</outputFile>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<pdiParameters>"
      + "<entry>"
      + "<key>%s</key>" + "<value>%s</value>"
      + "</entry>"
      + "</pdiParameters>"
      + "</jobScheduleRequest>", TEST_REPOSITORY_DIRECTORY,
    TEST_JOB_NAME.replace( "&", "&amp;" ).replace( "<", "&lt;" )
      .replace( ">", "&gt;" ),
    JOB_EXTENSION,TEST_REPOSITORY_DIRECTORY,LOG_LEVEL_PARAM_NAME, STRING_PARAM_TYPE, TEST_LOG_LEVEL_PARAM_VALUE,
        CLEAR_LOG_PARAM_NAME, STRING_PARAM_TYPE, TEST_CLEAR_LOG_PARAM_VALUE,
        RUN_SAFE_MODE_PARAM_NAME, STRING_PARAM_TYPE, TEST_RUN_SAFE_MODE_PARAM_VALUE,
        GATHERING_METRICS_PARAM_NAME, STRING_PARAM_TYPE, TEST_GATHERING_METRICS_PARAM_VALUE,
        START_COPY_NAME_PARAM_NAME, STRING_PARAM_TYPE, TEST_START_COPY_NAME_PARAM_VALUE,
        EXPANDING_REMOTE_JOB_PARAM_NAME, STRING_PARAM_TYPE, TEST_EXPANDING_REMOTE_JOB_PARAM_VALUE,
        TEST_PDI_PARAM_NAME, TEST_PDI_PARAM_VALUE );

  private SchedulerRequest schedulerRequest;

  @Before
  public void before() {
    KettleLogStore.init();
    schedulerRequest = mock( SchedulerRequest.class );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testBuildSchedulerRequestEntity() throws KettleException, UnsupportedEncodingException {
    AbstractMeta meta = mock( JobMeta.class );
    RepositoryDirectoryInterface repositoryDirectory = mock( RepositoryDirectoryInterface.class );
    Repository repository = mock( Repository.class );

    doReturn( repositoryDirectory ).when( meta ).getRepositoryDirectory();
    doReturn( TEST_REPOSITORY_DIRECTORY ).when( repositoryDirectory ).getPath();
    doReturn( repository ).when( meta ).getRepository();
    doReturn( repositoryDirectory ).when( repository ).getUserHomeDirectory();
    doReturn( TEST_REPOSITORY_DIRECTORY ).when( repositoryDirectory ).getPath();
    doReturn( TEST_JOB_NAME ).when( meta ).getName();
    doReturn( JOB_EXTENSION ).when( meta ).getDefaultExtension();

    doReturn( LogLevel.getLogLevelForCode( TEST_LOG_LEVEL_PARAM_VALUE ) ).when( meta ).getLogLevel();
    doReturn( Boolean.valueOf( TEST_CLEAR_LOG_PARAM_VALUE ) ).when( meta ).isClearingLog();
    doReturn( Boolean.valueOf( TEST_RUN_SAFE_MODE_PARAM_VALUE ) ).when( meta ).isSafeModeEnabled();
    doReturn( Boolean.valueOf( TEST_GATHERING_METRICS_PARAM_VALUE ) ).when( meta ).isGatheringMetrics();
    doReturn( TEST_START_COPY_NAME_PARAM_VALUE ).when( (JobMeta) meta ).getStartCopyName();
    doReturn( Boolean.valueOf( TEST_EXPANDING_REMOTE_JOB_PARAM_VALUE ) ).when( (JobMeta) meta ).isExpandingRemoteJob();

    doReturn( ARRAY_WITH_TEST_PDI_PARAM_NAME ).when( meta ).listParameters();
    doReturn( TEST_PDI_PARAM_VALUE ).when( meta ).getParameterValue( TEST_PDI_PARAM_NAME );

    doCallRealMethod().when( schedulerRequest ).buildSchedulerRequestEntity( meta );

    assertTrue( compareContentOfStringEntities( schedulerRequest.buildSchedulerRequestEntity( meta ),
            new StringEntity( REFERENCE_TEST_REQUEST ) ) );
  }

  private boolean compareContentOfStringEntities( StringEntity entity1, StringEntity entity2 ) {
    if ( entity1.getContentLength() == entity2.getContentLength() ) {
      try ( InputStream stream1 = entity1.getContent();
            InputStream stream2 = entity2.getContent() ) {
        while ( stream1.available() > 0 ) {
          if ( stream1.read() != stream2.read() ) {
            return false;
          }
        }
        return true;
      } catch ( IOException e ) {
        return false;
      }
    } else {
      return false;
    }
  }

  @Test
  public void testBuild_SessionAuthToken_ValidJSessionId_SetsCookieHeader() {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( AuthenticationContext.SESSION_AUTH_TOKEN );

    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext authContext = mock( AuthenticationContext.class );
    when( authContext.getJSessionId() ).thenReturn( "testSessionId" );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) ).thenReturn( authContext );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      verify( httpPost ).setHeader( "Cookie", "JSESSIONID=testSessionId" );
    }
  }

  @Test
  public void testBuild_NullPassword_FallsBackToBasicAuth() {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( null );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      String expectedEncoded = "Basic " + new String(
        Base64.getEncoder().encode( "admin:null".getBytes( java.nio.charset.StandardCharsets.UTF_8 ) ) );
      verify( httpPost ).setHeader( SchedulerRequest.AUTHORIZATION, expectedEncoded );
      verify( httpPost, never() ).setHeader( eq( "Cookie" ), anyString() );
    }
  }

  @Test
  public void testBuild_EmptyPassword_FallsBackToBasicAuth() throws Exception {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( "" );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      String expectedEncoded = "Basic " + new String(
        Base64.getEncoder().encode( "admin:".getBytes( SchedulerRequest.UTF_8 ) ) );
      verify( httpPost ).setHeader( SchedulerRequest.AUTHORIZATION, expectedEncoded );
      verify( httpPost, never() ).setHeader( eq( "Cookie" ), anyString() );
    }
  }

  @Test
  public void testBuild_SessionAuth_NullJSessionId_NoCookieHeader() {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( AuthenticationContext.SESSION_AUTH_TOKEN );

    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext authContext = mock( AuthenticationContext.class );
    when( authContext.getJSessionId() ).thenReturn( null );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) ).thenReturn( authContext );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      verify( httpPost, never() ).setHeader( eq( "Cookie" ), anyString() );
    }
  }

  @Test
  public void testBuild_SessionAuth_BlankJSessionId_NoCookieHeader() {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( AuthenticationContext.SESSION_AUTH_TOKEN );

    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext authContext = mock( AuthenticationContext.class );
    when( authContext.getJSessionId() ).thenReturn( "   " );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) ).thenReturn( authContext );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      verify( httpPost, never() ).setHeader( eq( "Cookie" ), anyString() );
    }
  }

  @Test
  public void testBuild_SessionAuth_SpoonSessionManagerThrows_NoCookieHeaderAndNoException() {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( AuthenticationContext.SESSION_AUTH_TOKEN );

    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    when( sessionManager.getAuthenticationContext( anyString() ) )
      .thenThrow( new RuntimeException( "Connection failed" ) );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      // Exception must be swallowed; build() must complete normally
      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      verify( httpPost, never() ).setHeader( eq( "Cookie" ), anyString() );
    }
  }

  @Test
  public void testBuild_BasicAuth_UsernameAndPassword_SetsAuthorizationHeader() throws Exception {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( "secret" );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      builder.build();

      String expectedEncoded = "Basic " + new String(
        Base64.getEncoder().encode( "admin:secret".getBytes( SchedulerRequest.UTF_8 ) ) );
      HttpPost httpPost = mockedPost.constructed().get( 0 );
      verify( httpPost ).setHeader( SchedulerRequest.AUTHORIZATION, expectedEncoded );
    }
  }

  @Test
  public void testBuild_BasicAuth_NullUsername_NoAuthorizationHeader() {
    Repository repo = mock( Repository.class );
    IUser userInfo = mock( IUser.class );
    when( repo.getUserInfo() ).thenReturn( userInfo );
    when( userInfo.getName() ).thenReturn( null );
    when( userInfo.getPassword() ).thenReturn( "secret" );

    SchedulerRequest.Builder builder = spy( new SchedulerRequest.Builder() );
    builder.repository( repo );
    doReturn( "http://localhost:8080/pentaho" ).when( builder ).getRepositoryServiceBaseUrl();

    try ( MockedConstruction<HttpPost> mockedPost = mockConstruction( HttpPost.class ) ) {
      builder.build();

      HttpPost httpPost = mockedPost.constructed().get( 0 );
      verify( httpPost, never() ).setHeader( eq( SchedulerRequest.AUTHORIZATION ), anyString() );
    }
  }
}
