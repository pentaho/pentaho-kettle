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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.pentaho.di.core.util.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;

public class ExecuteTransServletTest {
  private ExecuteTransServlet executeTransServlet;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void setup() throws KettleException {
    KettleClientEnvironment.init();
    KettleEnvironment.init();
    executeTransServlet = spy( ExecuteTransServlet.class );
  }

  @After
  public void tearDown() {
    KettleEnvironment.reset();
  }

  @Test
  public void doGetMissingMandatoryParamRepoTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( null );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( null );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetMissingMandatoryParamTransTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( "Repo" );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( null );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetWrongRepositoryTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    RepositoriesMeta repositoriesMeta = mock( RepositoriesMeta.class );
    spy( RepositoriesMeta.class );
    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( "Repo" );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( "Trans" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
//    whenNew( RepositoriesMeta.class ).withNoArguments().thenReturn( repositoriesMeta );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
  }

  @Ignore("Unable to run this test without PowerMock") @Test
  public void doGetRepositoryAuthenticationFailTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    RepositoriesMeta repositoriesMeta = mock( RepositoriesMeta.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    Repository repository = mock( Repository.class );
    KettleException kettleException = mock( KettleException.class );
    ExecutionException executionException = mock( ExecutionException.class );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( "Repo" );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( "Trans" );
    when( mockHttpServletRequest.getParameter( "user" ) ).thenReturn( "wrongUser" );
    when( mockHttpServletRequest.getParameter( "pass" ) ).thenReturn( "wrongPass" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
//    whenNew( RepositoriesMeta.class ).withNoArguments().thenReturn( repositoriesMeta );
    when( repositoriesMeta.findRepository( "Repo" ) ).thenReturn( repositoryMeta );

    doThrow( kettleException ).when( repository ).connect( "wrongUser", "wrongPass" );
    when( kettleException.getCause() ).thenReturn( executionException );
    when( executionException.getCause() ).thenReturn( new KettleAuthenticationException() );
    when( repository.isConnected() ).thenReturn( false );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_UNAUTHORIZED );
  }

  @Test
  public void clearBowlCacheHandlesNullRepository() throws Exception {
    executeTransServlet.clearBowlCache( (Repository) null );
    assertNotNull( DefaultBowl.getInstance() );
    executeTransServlet.clearBowlCache( DefaultBowl.getInstance() );
  }

  @Test
  public void openRepositoryUsesRepositoryLocatorWhenNameEmpty() throws Exception {
    Repository activeRepository = mock( Repository.class );
    doReturn( activeRepository ).when( executeTransServlet ).getRepositoryFromLocator();

    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod( "openRepository",
      String.class, String.class, String.class );
    method.setAccessible( true );
    Repository resolvedRepository = (Repository) method.invoke( executeTransServlet, "", "user", "pass" );
    assertSame( activeRepository, resolvedRepository );
  }

  @Test
  public void openRepositoryThrowsWhenNamedRepositoryIsMissing() throws Exception {
    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "openRepository", String.class, String.class, String.class );
    method.setAccessible( true );

    try {
      method.invoke( executeTransServlet, "missing-repository", "user", "pass" );
      assertTrue( false );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof KettleException );
      assertTrue( e.getCause().getMessage().contains( "Unable to find repository" ) );
    }
  }

  @Test
  public void loadTransformationFromRepositorySuccess() throws Exception {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface directoryTree = mock( RepositoryDirectoryInterface.class );
    ObjectId objectId = mock( ObjectId.class );
    TransMeta expectedMeta = mock( TransMeta.class );

    when( repository.loadRepositoryDirectoryTree() ).thenReturn( directoryTree );
    when( directoryTree.findDirectory( "folder" ) ).thenReturn( directoryTree );
    when( repository.getTransformationID( "sample-trans", directoryTree ) ).thenReturn( objectId );
    when( repository.loadTransformation( objectId, null, null ) ).thenReturn( expectedMeta );

    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformationFromRepository", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    TransMeta actualMeta = (TransMeta) method.invoke( executeTransServlet, repository, "folder/sample-trans", null );
    assertSame( expectedMeta, actualMeta );
  }

  @Test
  public void loadTransformationFromFilePathAllowed() throws Exception {
    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformation", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    SlaveServerConfig serverConfig = new SlaveServerConfig();
    serverConfig.setAllowedExecuteFileSchemes( Collections.singletonList( "file" ) );
    TransformationMap transformationMap = new TransformationMap();
    transformationMap.setSlaveServerConfig( serverConfig );
    executeTransServlet.transformationMap = transformationMap;

    java.io.File tempFile = java.io.File.createTempFile( "allowed-trans", ".ktr" );
    java.nio.file.Files.writeString(
      tempFile.toPath(),
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<transformation>\n"
        + "  <info>\n"
        + "    <name>allowed-trans</name>\n"
        + "    <directory>/</directory>\n"
        + "  </info>\n"
        + "</transformation>\n" );

    TransMeta transMeta = (TransMeta) method.invoke( executeTransServlet, null, tempFile.toURI().toString(), new Variables() );
    assertNotNull( transMeta );
  }

  @Test
  public void loadTransformationFromFileUrlUsesFilesystemWhenRepositoryIsPresent() throws Exception {
    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformation", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    Repository repository = mock( Repository.class );
    when( repository.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    when( repository.getName() ).thenReturn( "repo" );

    SlaveServerConfig serverConfig = new SlaveServerConfig();
    serverConfig.setAllowedExecuteFileSchemes( Collections.singletonList( "file" ) );
    TransformationMap transformationMap = new TransformationMap();
    transformationMap.setSlaveServerConfig( serverConfig );
    executeTransServlet.transformationMap = transformationMap;

    java.io.File tempFile = new java.io.File( "src/test/resources/org/pentaho/di/trans/one-step-trans.ktr" );
    TransMeta transMeta = (TransMeta) method.invoke( executeTransServlet, repository, tempFile.toURI().toString(), new Variables() );

    assertNotNull( transMeta );
    verify( repository, never() ).loadRepositoryDirectoryTree();
    verify( repository, never() ).getTransformationID( anyString(), any( RepositoryDirectoryInterface.class ) );
  }

  @Test
  public void loadTransformationFromRawPathUsesRepositoryWhenRepositoryIsPresent() throws Exception {
    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformation", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface directoryTree = mock( RepositoryDirectoryInterface.class );
    ObjectId objectId = mock( ObjectId.class );
    TransMeta expectedMeta = mock( TransMeta.class );

    when( repository.loadRepositoryDirectoryTree() ).thenReturn( directoryTree );
    when( directoryTree.findDirectory( "/foo" ) ).thenReturn( directoryTree );
    when( repository.getTransformationID( "bar", directoryTree ) ).thenReturn( objectId );
    when( repository.loadTransformation( same( objectId ), nullable( String.class ), any( VariableSpace.class ) ) ).thenReturn( expectedMeta );

    TransMeta actualMeta = (TransMeta) method.invoke( executeTransServlet, repository, "/foo/bar", new Variables() );

    assertSame( expectedMeta, actualMeta );
    verify( repository ).loadRepositoryDirectoryTree();
    verify( repository ).getTransformationID( "bar", directoryTree );
  }

  @Test
  public void loadTransformationFromRawPathUsesFilesystemWhenRepositoryIsAbsent() throws Exception {
    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformation", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    SlaveServerConfig serverConfig = new SlaveServerConfig();
    serverConfig.setAllowedExecuteFileSchemes( Collections.singletonList( "file" ) );
    TransformationMap transformationMap = new TransformationMap();
    transformationMap.setSlaveServerConfig( serverConfig );
    executeTransServlet.transformationMap = transformationMap;

    java.io.File tempFile = new java.io.File( "src/test/resources/org/pentaho/di/trans/one-step-trans.ktr" );

    TransMeta transMeta = (TransMeta) method.invoke( executeTransServlet, null, tempFile.getAbsolutePath(), new Variables() );

    assertNotNull( transMeta );
  }

  @Test
  public void loadTransformationFromUrlRejected() throws Exception {
    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformation", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    try {
      method.invoke( executeTransServlet, null, "http://example.com/test.ktr", new Variables() );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof org.pentaho.di.www.exception.ExecutionNotAllowedException );
      return;
    }
    assertTrue( false );
  }

  @Test
  public void loadTransformationFromRepositoryThrowsWhenDirectoryMissing() throws Exception {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface directoryTree = mock( RepositoryDirectoryInterface.class );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( directoryTree );
    when( directoryTree.findDirectory( "/missing" ) ).thenReturn( null );

    java.lang.reflect.Method method = ExecuteTransServlet.class.getDeclaredMethod(
      "loadTransformationFromRepository", Repository.class, String.class, VariableSpace.class );
    method.setAccessible( true );

    try {
      method.invoke( executeTransServlet, repository, "/missing/test.ktr", new Variables() );
    } catch ( java.lang.reflect.InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof KettleException );
      assertTrue( e.getCause().getMessage().contains( "Unable to find directory path" ) );
      return;
    }
    assertTrue( false );
  }

  @Test
  public void doGetExecutionNotAllowedTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    Repository activeRepository = mock( Repository.class );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( null );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( "https://example.com/test.ktr" );
    when( mockHttpServletRequest.getParameter( "user" ) ).thenReturn( "user" );
    when( mockHttpServletRequest.getParameter( "pass" ) ).thenReturn( "pass" );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.emptyEnumeration() );
    doReturn( activeRepository ).when( executeTransServlet ).getRepositoryFromLocator();
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( activeRepository, never() ).loadRepositoryDirectoryTree();
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
    assertTrue( out.toString().contains( "Execution is not allowed" ) );
  }

  @Test
  public void doGetTransformationNotFoundTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans trans = initMocksForTransExecution( mockHttpServletRequest, mockHttpServletResponse );
    doThrow( new KettleException( "Unable to find transformation" ) ).when( executeTransServlet ).executeTrans( any() );
    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
  }

  @Test
  public void doGetTransformationFailsTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans trans = initMocksForTransExecution( mockHttpServletRequest, mockHttpServletResponse );
    when( trans.isFinishedOrStopped() ).thenReturn( true );
    when( trans.getErrors() ).thenReturn( 1 );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
  }

  @Test
  public void doGetTransformationSucceedsTest() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans trans = initMocksForTransExecution( mockHttpServletRequest, mockHttpServletResponse );
    when( trans.isFinishedOrStopped() ).thenReturn( true );
    when( trans.getErrors() ).thenReturn( 0 );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
//    verify( mockHttpServletResponse, Mockito.times( 0 ) ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    verify( mockHttpServletResponse, Mockito.times( 0 ) ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
    verify( mockHttpServletResponse, Mockito.times( 0 ) ).setStatus( HttpServletResponse.SC_NOT_FOUND );
    verify( mockHttpServletResponse, Mockito.times( 0 ) ).setStatus( HttpServletResponse.SC_UNAUTHORIZED );
  }

  private Trans initMocksForTransExecution( HttpServletRequest mockHttpServletRequest,
                                            HttpServletResponse mockHttpServletResponse ) throws Exception {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objectId = mock( ObjectId.class );
    TransMeta transMeta = mock( TransMeta.class );
    Trans trans = mock( Trans.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( "Repo" );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( "Trans" );
    when( mockHttpServletRequest.getParameter( "user" ) ).thenReturn( "user" );
    when( mockHttpServletRequest.getParameter( "pass" ) ).thenReturn( "pass" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transMeta.listParameters() ).thenReturn( new String[0] );
    when( transMeta.getName() ).thenReturn( "mock-trans" );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repositoryDirectoryInterface );
    when( repositoryDirectoryInterface.findDirectory( "/" ) ).thenReturn( repositoryDirectoryInterface );
    when( repository.getTransformationID( "Trans", repositoryDirectoryInterface ) ).thenReturn( objectId );
    when( repository.loadTransformation( objectId, null ) ).thenReturn( transMeta );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( new ArrayList<>() ) );
    doReturn( repository ).when( executeTransServlet ).openRepository( "Repo", "user", "pass" );
    doNothing().when( executeTransServlet ).clearBowlCache( repository );
    setInternalState( executeTransServlet, "transformationMap", mock( TransformationMap.class ) );
    return trans;
  }

}
