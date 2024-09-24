/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class ExecuteJobServletTest {
  private static Class<?> PKG = ExecuteJobServlet.class; // for i18n purposes, needed by Translator2!!

  private HttpServletRequest mockHttpServletRequest;
  private HttpServletResponse spyHttpServletResponse;
  private JobMap jobMap;
  private ExecuteJobServlet spyExecuteJobServlet;
  private Repository repository;
  MockedStatic<Encr> staticEncrMock;

  private static String JOB_ID = "123";
  private static String JOB_NAME = "test";

  private static String REPOSITORY_NAME = "repository";

  private static String AUTHORIZED_USER = "authorized";
  private static String UNAUTHORIZED_USER = "unauthorized";
  private static String PASSWORD = "password";

  private static String LEVEL = LogLevel.DEBUG.getCode();

  @Before
  public void setup() {
    mockHttpServletRequest = mock( HttpServletRequest.class );
    spyHttpServletResponse = spy( HttpServletResponse.class );
    jobMap = new JobMap();
    spyExecuteJobServlet = spy( new ExecuteJobServlet( jobMap ) );

    repository = mock( Repository.class );
  }

  @Test
  public void testExecuteJobServletTest()
    throws ServletException, IOException, KettleException {
    try ( MockedStatic<Encr> encrMockedStatic = mockStatic( Encr.class ) ) {
      encrMockedStatic.when( () -> Encr.decryptPasswordOptionallyEncrypted( eq( PASSWORD ) ) ).thenReturn( PASSWORD );
      doReturn( ExecuteJobServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
      doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( "rep" );
      doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( "user" );
      doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( "pass" );
      doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "job" );
      doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( "level" );

      doReturn( repository ).when( spyExecuteJobServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

      JobMeta jobMeta = buildJobMeta();

      RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
      doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
      doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repositoryDirectoryInterface )
        .findDirectory( anyString() );
      doReturn( mock( ObjectId.class ) ).when( repository )
        .getJobId( anyString(), any( RepositoryDirectoryInterface.class ) );
      doReturn( jobMeta ).when( repository ).loadJob( any( ObjectId.class ), nullable( String.class ) );
      doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      assertTrue( out.toString().contains( WebResult.STRING_OK ) );
      assertTrue( out.toString().contains( "Job started" ) );
    }
  }

  @Test
  public void testExecuteJobServletTestCantFindDirectory()
    throws ServletException, IOException, KettleException {
    try ( MockedStatic<Encr> encrMockedStatic = mockStatic( Encr.class ) ) {
      encrMockedStatic.when( () -> Encr.decryptPasswordOptionallyEncrypted( eq( PASSWORD ) ) ).thenReturn( PASSWORD );
      doReturn( ExecuteJobServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
      doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( "rep" );
      doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( "user" );
      doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( "pass" );
      doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "job" );
      doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( "level" );

      doReturn( repository ).when( spyExecuteJobServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

      RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
      doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
      doReturn( null ).when( repositoryDirectoryInterface ).findDirectory( anyString() );
      doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.DirectoryPathNotFoundInRepository", "/" );
      assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
    }
  }

  @Test
  public void testExecuteJobServletTestJobNotFoundInDirectory()
    throws ServletException, IOException, KettleException {
    try ( MockedStatic<Encr> encrMockedStatic = mockStatic( Encr.class ) ) {
      encrMockedStatic.when( () -> Encr.decryptPasswordOptionallyEncrypted( eq( PASSWORD ) ) ).thenReturn( PASSWORD );
      doReturn( ExecuteJobServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
      doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( "rep" );
      doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( "user" );
      doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( "pass" );
      doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "job" );
      doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( "level" );

      doReturn( repository ).when( spyExecuteJobServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

      RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
      doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
      doReturn( repositoryDirectoryInterface ).when( repositoryDirectoryInterface ).findDirectory( anyString() );
      doReturn( null ).when( repository ).getJobId( anyString(), any( RepositoryDirectoryInterface.class ) );
      doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.JobNotFoundInDirectory", JOB_NAME, "/" );
      assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
    }
  }

  @Test
  public void testExecuteJobServletTestCantFindRepository() throws ServletException, IOException {
    try ( MockedStatic<Encr> encrMockedStatic = mockStatic( Encr.class ) ) {
      encrMockedStatic.when( () -> Encr.decryptPasswordOptionallyEncrypted( eq( PASSWORD ) ) ).thenReturn( PASSWORD );
      doReturn( ExecuteJobServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
      doReturn( "Unknown" ).when( mockHttpServletRequest ).getParameter( "rep" );
      doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( "user" );
      doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( "pass" );
      doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "job" );
      doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( "level" );

      KettleLogStore.init();

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.UnableToFindRepository", "Unknown" );
      assertTrue( out.toString().contains( message ) );
    }
  }

  @Test
  public void testExecuteJobServletTestWithUnauthorizedUser()
    throws KettleException, IOException, ServletException {
    try ( MockedStatic<Encr> encrMockedStatic = mockStatic( Encr.class ) ) {
      encrMockedStatic.when( () -> Encr.decryptPasswordOptionallyEncrypted( eq( PASSWORD ) ) ).thenReturn( PASSWORD );
      doReturn( ExecuteJobServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
      doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( "rep" );
      doReturn( UNAUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( "user" );
      doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( "pass" );
      doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "job" );
      doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( "level" );
      KettleAuthenticationException kae = new KettleAuthenticationException();
      ExecutionException ee = new ExecutionException( kae );
      KettleException ke = new KettleException( ee );
      doThrow( ke ).when( spyExecuteJobServlet ).openRepository( REPOSITORY_NAME, UNAUTHORIZED_USER, PASSWORD );

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      String message =
        BaseMessages.getString( PKG, "ExecuteJobServlet.Error.Authentication", ExecuteJobServlet.CONTEXT_PATH );
      assertTrue( out.toString().contains( message ) );
    }
  }

  private JobMeta buildJobMeta() {
    JobMeta jobMeta = new JobMeta();
    jobMeta.setCarteObjectId( JOB_ID );
    jobMeta.setName( JOB_NAME );
    JobEntryCopy jobEntryCopy = new JobEntryCopy( );
    jobEntryCopy.setEntry( new JobEntryEmpty() );
    jobEntryCopy.setLocation( 150, 50 );
    jobMeta.addJobEntry( jobEntryCopy );
    return jobMeta;
  }

  private StringWriter mockWriter() throws IOException {
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    doReturn( printWriter ).when( spyHttpServletResponse ).getWriter( );
    return out;
  }
}
