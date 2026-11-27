/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.www;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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
  public void setup() throws KettleException {
    KettleClientEnvironment.init();
    KettleEnvironment.init();
    mockHttpServletRequest = mock( HttpServletRequest.class );
    spyHttpServletResponse = spy( HttpServletResponse.class );
    jobMap = new JobMap();
    spyExecuteJobServlet = spy( new ExecuteJobServlet( jobMap ) );

    repository = mock( Repository.class );
  }

  @After
  public void tearDown() {
    KettleEnvironment.reset();
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
      doNothing().when( spyExecuteJobServlet ).clearBowlCache( same( repository ) );

      JobMeta jobMeta = buildJobMeta();

      RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
      doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
      doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repositoryDirectoryInterface )
        .findDirectory( anyString() );
      doReturn( mock( ObjectId.class ) ).when( repository )
        .getJobId( anyString(), any( RepositoryDirectoryInterface.class ) );
      doReturn( jobMeta ).when( repository ).loadJob( any( ObjectId.class ), nullable( String.class ), nullable( VariableSpace.class ) );
      doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      assertTrue( out.toString().contains( WebResult.STRING_OK ) );
      assertTrue( out.toString().contains( "Job started" ) );
      verify( spyExecuteJobServlet ).clearBowlCache( same( repository ) );
    }
  }

  @Test
  public void testOpenRepositoryUsesRepositoryLocatorWhenNameEmpty() throws Exception {
    Repository activeRepository = mock( Repository.class );
    doReturn( activeRepository ).when( spyExecuteJobServlet ).getRepositoryFromLocator();

    Repository resolvedRepository = spyExecuteJobServlet.openRepository( "", AUTHORIZED_USER, PASSWORD );
    assertSame( activeRepository, resolvedRepository );
  }

  @Test
  public void loadJobFromFileUrlUsesFilesystemWhenRepositoryIsPresent() throws Exception {
    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJob", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    Repository jobRepository = mock( Repository.class );
    when( jobRepository.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    when( jobRepository.getName() ).thenReturn( "repo" );

    SlaveServerConfig serverConfig = new SlaveServerConfig();
    serverConfig.setAllowedExecuteFileSchemes( Collections.singletonList( "file" ) );
    JobMap localJobMap = new JobMap();
    localJobMap.setSlaveServerConfig( serverConfig );
    spyExecuteJobServlet.jobMap = localJobMap;

    java.io.File tempFile = new java.io.File( "src/test/resources/org/pentaho/di/job/one-step-job.kjb" );

    JobMeta jobMeta = (JobMeta) method.invoke( spyExecuteJobServlet, jobRepository, tempFile.toURI().toString(), new Variables() );

    assertNotNull( jobMeta );
    verify( jobRepository, never() ).loadRepositoryDirectoryTree();
    verify( jobRepository, never() ).getJobId( anyString(), any( RepositoryDirectoryInterface.class ) );
  }

  @Test
  public void loadJobFromRawPathUsesRepositoryWhenRepositoryIsPresent() throws Exception {
    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJob", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    Repository rawPathRepository = mock( Repository.class );
    RepositoryDirectoryInterface directoryTree = mock( RepositoryDirectoryInterface.class );
    ObjectId jobId = mock( ObjectId.class );
    JobMeta expectedMeta = mock( JobMeta.class );

    when( rawPathRepository.loadRepositoryDirectoryTree() ).thenReturn( directoryTree );
    when( directoryTree.findDirectory( "/foo" ) ).thenReturn( directoryTree );
    when( rawPathRepository.getJobId( "bar", directoryTree ) ).thenReturn( jobId );
    when( rawPathRepository.loadJob( same( jobId ), nullable( String.class ), any( VariableSpace.class ) ) ).thenReturn( expectedMeta );

    JobMeta actualMeta = (JobMeta) method.invoke( spyExecuteJobServlet, rawPathRepository, "/foo/bar", new Variables() );

    assertSame( expectedMeta, actualMeta );
    verify( rawPathRepository ).loadRepositoryDirectoryTree();
    verify( rawPathRepository ).getJobId( "bar", directoryTree );
  }

  @Test
  public void loadJobFromRawPathUsesFilesystemWhenRepositoryIsAbsent() throws Exception {
    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJob", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    SlaveServerConfig serverConfig = new SlaveServerConfig();
    serverConfig.setAllowedExecuteFileSchemes( Collections.singletonList( "file" ) );
    JobMap localJobMap = new JobMap();
    localJobMap.setSlaveServerConfig( serverConfig );
    spyExecuteJobServlet.jobMap = localJobMap;

    java.io.File tempFile = new java.io.File( "src/test/resources/org/pentaho/di/job/one-step-job.kjb" );

    JobMeta jobMeta = (JobMeta) method.invoke( spyExecuteJobServlet, null, tempFile.getAbsolutePath(), new Variables() );

    assertNotNull( jobMeta );
  }

  @Test
  public void openRepositoryThrowsWhenNamedRepositoryIsMissing() throws Exception {
    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "openRepository", String.class, String.class, String.class );
    method.setAccessible( true );

    try {
      method.invoke( spyExecuteJobServlet, "missing-repository", AUTHORIZED_USER, PASSWORD );
      assertTrue( false );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof KettleException );
      assertTrue( e.getCause().getMessage().contains( "Unable to find repository" ) );
    }
  }

  @Test
  public void loadJobFromRepositorySuccess() throws Exception {
    Repository mockRepository = mock( Repository.class );
    RepositoryDirectoryInterface directoryTree = mock( RepositoryDirectoryInterface.class );
    ObjectId jobId = mock( ObjectId.class );
    JobMeta expectedMeta = mock( JobMeta.class );

    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( directoryTree );
    when( directoryTree.findDirectory( "folder" ) ).thenReturn( directoryTree );
    when( mockRepository.getJobId( "sample-job", directoryTree ) ).thenReturn( jobId );
    when( mockRepository.loadJob( jobId, null, null ) ).thenReturn( expectedMeta );

    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJobFromRepository", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    JobMeta actualMeta = (JobMeta) method.invoke( spyExecuteJobServlet, mockRepository, "folder/sample-job", null );
    assertSame( expectedMeta, actualMeta );
  }

  @Test
  public void loadJobFromFilePathAllowed() throws Exception {
    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJob", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    SlaveServerConfig serverConfig = new SlaveServerConfig();
    serverConfig.setAllowedExecuteFileSchemes( Collections.singletonList( "file" ) );
    JobMap localJobMap = new JobMap();
    localJobMap.setSlaveServerConfig( serverConfig );
    spyExecuteJobServlet.jobMap = localJobMap;

    java.io.File tempFile = java.io.File.createTempFile( "allowed-job", ".kjb" );
    java.nio.file.Files.writeString(
      tempFile.toPath(),
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<job>\n"
        + "  <name>allowed-job</name>\n"
        + "  <directory>/</directory>\n"
        + "</job>\n" );

    JobMeta jobMeta = (JobMeta) method.invoke( spyExecuteJobServlet, null, tempFile.toURI().toString(), new Variables() );
    assertNotNull( jobMeta );
  }

  @Test
  public void loadJobFromUrlRejected() throws Exception {
    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJob", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    try {
      method.invoke( spyExecuteJobServlet, null, "http://example.com/test.kjb", new Variables() );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof org.pentaho.di.www.exception.ExecutionNotAllowedException );
      return;
    }
    assertTrue( false );
  }

  @Test
  public void loadJobFromRepositoryThrowsWhenDirectoryMissing() throws Exception {
    Repository mockRepository = mock( Repository.class );
    RepositoryDirectoryInterface directoryTree = mock( RepositoryDirectoryInterface.class );
    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( directoryTree );
    when( directoryTree.findDirectory( "/missing" ) ).thenReturn( null );

    java.lang.reflect.Method method = ExecuteJobServlet.class.getDeclaredMethod(
      "loadJobFromRepository", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    try {
      method.invoke( spyExecuteJobServlet, mockRepository, "/missing/test.kjb", new Variables() );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof KettleException );
      assertTrue( e.getCause().getMessage().contains( "Unable to find directory path" ) );
      return;
    }
    assertTrue( false );
  }

  @Test
  public void testExecuteJobServletReturnsBadRequestWhenExecutionNotAllowed() throws Exception {
    try ( MockedStatic<Encr> encrMockedStatic = mockStatic( Encr.class ) ) {
      encrMockedStatic.when( () -> Encr.decryptPasswordOptionallyEncrypted( eq( PASSWORD ) ) ).thenReturn( PASSWORD );
      doReturn( ExecuteJobServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
      doReturn( null ).when( mockHttpServletRequest ).getParameter( "rep" );
      doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( "user" );
      doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( "pass" );
      doReturn( "https://example.com/job.kjb" ).when( mockHttpServletRequest ).getParameter( "job" );
      doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( "level" );
      doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();
      Repository activeRepository = mock( Repository.class );
      doReturn( activeRepository ).when( spyExecuteJobServlet ).getRepositoryFromLocator();
      doReturn( activeRepository ).when( spyExecuteJobServlet ).openRepository( "", AUTHORIZED_USER, PASSWORD );

      StringWriter out = mockWriter();
      spyExecuteJobServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

      verify( activeRepository, never() ).loadRepositoryDirectoryTree();
      assertTrue( out.toString().contains( "Execution is not allowed" ) );
      verify( spyHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
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
      String output = out.toString();
      assertTrue( output.contains( message ), "Missing message in output: " + output );
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
