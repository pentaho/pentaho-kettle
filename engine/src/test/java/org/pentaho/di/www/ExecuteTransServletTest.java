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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;

public class ExecuteTransServletTest {
  private ExecuteTransServlet executeTransServlet;
  private MockedStatic<PluginRegistry> pluginRegistryMockedStatic;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Before
  public void setup() {
    executeTransServlet = spy( ExecuteTransServlet.class );
    pluginRegistryMockedStatic = mockStatic( PluginRegistry.class );
  }

  @After
  public void tearDown() {
    pluginRegistryMockedStatic.close();
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
    PluginRegistry pluginRegistry = mock( PluginRegistry.class );
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
    when( PluginRegistry.getInstance() ).thenReturn( pluginRegistry );
    when( pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class ) ).thenReturn( repository );

    doThrow( kettleException ).when( repository ).connect( "wrongUser", "wrongPass" );
    when( kettleException.getCause() ).thenReturn( executionException );
    when( executionException.getCause() ).thenReturn( new KettleAuthenticationException() );
    when( repository.isConnected() ).thenReturn( false );

    executeTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_UNAUTHORIZED );
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
    RepositoriesMeta repositoriesMeta = mock( RepositoriesMeta.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    Repository repository = mock( Repository.class );
    PluginRegistry pluginRegistry = mock( PluginRegistry.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objectId = mock( ObjectId.class );
    TransMeta transMeta = mock( TransMeta.class );
    Trans trans = mock( Trans.class );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "rep" ) ).thenReturn( "Repo" );
    when( mockHttpServletRequest.getParameter( "trans" ) ).thenReturn( "Trans" );
    when( mockHttpServletRequest.getParameter( "user" ) ).thenReturn( "user" );
    when( mockHttpServletRequest.getParameter( "pass" ) ).thenReturn( "pass" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
//    whenNew( RepositoriesMeta.class ).withNoArguments().thenReturn( repositoriesMeta );
    when( repositoriesMeta.findRepository( "Repo" ) ).thenReturn( repositoryMeta );
    when( PluginRegistry.getInstance() ).thenReturn( pluginRegistry );
    when( pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class ) )
      .thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repositoryDirectoryInterface );
    when( repositoryDirectoryInterface.findDirectory( "/" ) ).thenReturn( repositoryDirectoryInterface );
    when( repository.getTransformationID( "Trans", repositoryDirectoryInterface ) ).thenReturn( objectId );
    when( repository.loadTransformation( objectId, null ) ).thenReturn( transMeta );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( new ArrayList<>() ) );
//    whenNew( Trans.class ).withAnyArguments().thenReturn( trans );
    setInternalState( executeTransServlet, "socketRepository", mock( SocketRepository.class ) );
    setInternalState( executeTransServlet, "transformationMap", mock( TransformationMap.class ) );
    return trans;
  }
}
