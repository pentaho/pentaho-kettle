/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { Encr.class, Trans.class } )
public class ExecuteTransServletTest {
  private static Class<?> PKG = ExecuteTransServlet.class; // for i18n purposes, needed by Translator2!!

  private HttpServletRequest mockHttpServletRequest;
  private HttpServletResponse spyHttpServletResponse;
  private ExecuteTransServlet spyExecuteTransServlet;
  private Repository repository;

  private static final String REPOSITORY_PARAMETER = "rep";
  private static final String USER_PARAMETER = "user";
  private static final String PASSWORD_PARAMETER = "pass";
  private static final String TRANS_PARAMETER = "trans";
  private static final String LEVEL_PARAMETER = "level";

  private static final String TRANS_ID = "321";
  private static final String TRANS_NAME = "test";

  private static final String REPOSITORY_NAME = "repository";

  private static final String AUTHORIZED_USER = "authorized";
  private static final String UNAUTHORIZED_USER = "unauthorized";
  private static final String PASSWORD = "password";

  private static final String ENCODING = "UTF-8";

  private static final String LEVEL = LogLevel.DEBUG.getCode();

  @Before
  public void setup() throws Exception {
    mockHttpServletRequest = mock( HttpServletRequest.class );
    spyHttpServletResponse = spy( HttpServletResponse.class );
    TransformationMap transMap = new TransformationMap();
    spyExecuteTransServlet = spy( new ExecuteTransServlet( transMap ) );

    repository = mock( Repository.class );

    LogChannel mockLogChannel = mock( LogChannel.class );
    when( mockLogChannel.isDebug() ).thenReturn( true );
    when( mockLogChannel.isDetailed() ).thenReturn( true );
    doNothing().when( mockLogChannel ).logDebug( anyString() );
    doNothing().when( mockLogChannel ).logDetailed( anyString() );
    PowerMockito.whenNew( LogChannel.class ).withAnyArguments().thenReturn( mockLogChannel );
  }

  @Test
  public void testExecuteTransServlet_Success() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    TransMeta transMeta = buildTransMeta();

    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repositoryDirectoryInterface )
      .findDirectory( anyString() );
    doReturn( mock( ObjectId.class ) ).when( repository )
      .getTransformationID( anyString(), any( RepositoryDirectoryInterface.class ) );
    doReturn( transMeta ).when( repository ).loadTransformation( any( ObjectId.class ), anyString() );
    doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertEquals( StringUtil.EMPTY_STRING, out.toString() );
  }

  @Test
  public void testExecuteTransServlet_DirectoryNotFound() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( null ).when( repositoryDirectoryInterface ).findDirectory( anyString() );
    doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.DirectoryPathNotFoundInRepository", "/" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testExecuteTransServlet_TransNotFound() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( repositoryDirectoryInterface ).when( repositoryDirectoryInterface ).findDirectory( anyString() );
    doReturn( null ).when( repository ).getTransformationID( anyString(), any( RepositoryDirectoryInterface.class ) );
    doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message =
      BaseMessages.getString( PKG, "ExecuteTransServlet.Error.TransformationNotFoundInDirectory", TRANS_NAME, "/" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testExecuteTransServlet_RepositoryNotFound() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( "Unknown" ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    KettleLogStore.init();

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.UnableToFindRepository", "Unknown" );
    assertTrue( out.toString().contains( message ) );
  }

  @Test
  public void testExecuteTransServlet_UnauthorizedUser() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( UNAUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    KettleAuthenticationException kae = new KettleAuthenticationException();
    ExecutionException ee = new ExecutionException( kae );
    KettleException ke = new KettleException( ee );
    Mockito.doThrow( ke ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, UNAUTHORIZED_USER, PASSWORD );

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message =
      BaseMessages.getString( PKG, "ExecuteTransServlet.Error.Authentication", REPOSITORY_NAME );
    assertTrue( out.toString().contains( message ) );
  }

  @Test
  public void testExecuteTransServlet_ConnectError() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    Exception npe = new NullPointerException();
    KettleException ke = new KettleException( npe );
    Mockito.doThrow( ke ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR );
    assertTrue( out.toString().contains( message ) );
  }

  @Test
  public void testExecuteTransServlet_NoPermission() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
    doReturn( false ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.ExecutePermissionRequired" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testExecuteTransServlet_UnexpectedError() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    TransMeta transMeta = buildTransMeta();

    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repositoryDirectoryInterface )
      .findDirectory( anyString() );
    doReturn( mock( ObjectId.class ) ).when( repository )
      .getTransformationID( anyString(), any( RepositoryDirectoryInterface.class ) );
    doReturn( transMeta ).when( repository ).loadTransformation( any( ObjectId.class ), anyString() );
    doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

    doThrow( new UnknownParamException() ).when( spyExecuteTransServlet )
      .setServletParametersAsVariables( any(), any(), any() );

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR );
    assertTrue( out.toString().contains( message ) );
  }

  @Test
  public void testExecuteTransServlet_ExecutionError() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission( repository );

    TransMeta transMeta = buildTransMeta();

    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( repositoryDirectoryInterface ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repositoryDirectoryInterface )
      .findDirectory( anyString() );
    doReturn( mock( ObjectId.class ) ).when( repository )
      .getTransformationID( anyString(), any( RepositoryDirectoryInterface.class ) );
    doReturn( transMeta ).when( repository ).loadTransformation( any( ObjectId.class ), anyString() );
    doReturn( Collections.emptyEnumeration() ).when( mockHttpServletRequest ).getParameterNames();

    doThrow( new KettleException() ).when( spyExecuteTransServlet ).executeTrans( any( Trans.class ) );

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.WhileExecutingTrans", TRANS_NAME, "" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testDefaultServletEncoding() throws Exception {
    testDefaultServletEncodingCommon();
    System.setProperty( ExecuteTransServlet.KETTLE_DEFAULT_SERVLET_ENCODING, ENCODING );

    mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    verify( spyHttpServletResponse, times( 1 ) ).setCharacterEncoding( ENCODING );
    verify( spyHttpServletResponse, times( 1 ) ).setContentType( anyString() );
  }

  @Test
  public void testDefaultServletEncoding_EmptyString() throws Exception {
    testDefaultServletEncodingCommon();
    System.setProperty( ExecuteTransServlet.KETTLE_DEFAULT_SERVLET_ENCODING, "" );

    mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    verify( spyHttpServletResponse, times( 0 ) ).setCharacterEncoding( anyString() );
    verify( spyHttpServletResponse, times( 0 ) ).setContentType( anyString() );
  }

  private void testDefaultServletEncodingCommon() throws Exception {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    Exception npe = new NullPointerException();
    KettleException ke = new KettleException( npe );
    doThrow( ke ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );
  }

  @Test
  public void testSetServletParametersAsVariables() throws Exception {
    HttpServletRequest[] request = new HttpServletRequest[ 1 ];
    String[][] knownOptions = new String[ 1 ][];
    TransMeta[] transMeta = new TransMeta[ 1 ];


    // These must be added as a new variable
    List<String> newVariables = Arrays.asList( "val-1", "val-4" );
    // These are trans parameters; the value must be updated
    List<String> transParameters = Arrays.asList( "parm-2", "parm-4" );
    // These must be ignored as they're already known
    List<String> knownVariables = Arrays.asList( "known-1", "known-4" );

    // Put everything on a single List
    List<String> requestParameters = new ArrayList<>();
    requestParameters.addAll( newVariables );
    requestParameters.addAll( transParameters );
    requestParameters.addAll( knownVariables );
    // Shuffle just because...
    Collections.shuffle( requestParameters );

    testSetServletParametersAsVariablesCommon( request, requestParameters, knownOptions, transMeta );

    int nVarsBefore = transMeta[ 0 ].listVariables().length;

    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();
    executeTransServlet.setServletParametersAsVariables( request[ 0 ], knownOptions[ 0 ], transMeta[ 0 ] );

    int nVarsAfter = transMeta[ 0 ].listVariables().length;

    verify( transMeta[ 0 ], times( newVariables.size() ) ).setVariable( anyString(), anyString() );
    assertEquals( newVariables.size(), nVarsAfter - nVarsBefore );

    verify( transMeta[ 0 ], times( transParameters.size() ) ).setParameterValue( anyString(), anyString() );
    for ( String parameter : transMeta[ 0 ].listParameters() ) {
      if ( transParameters.contains( parameter ) ) {
        // The value should have changed
        assertNotEquals( parameter, transMeta[ 0 ].getParameterValue( parameter ) );
      } else {
        // The value should be the original
        assertEquals( StringUtil.EMPTY_STRING, transMeta[ 0 ].getParameterValue( parameter ) );
      }
    }
  }

  private void testSetServletParametersAsVariablesCommon( HttpServletRequest[] request, List<String> requestParameters,
                                                          String[][] knownOptions,
                                                          TransMeta[] transMeta ) throws Exception {
    request[ 0 ] = spy( HttpServletRequest.class );

    Enumeration parmNames = Collections.enumeration( requestParameters );
    doReturn( parmNames ).when( request[ 0 ] ).getParameterNames();

    doAnswer( (Answer<String[]>) call -> {
      Object arg = call.getArguments()[ 0 ];
      return new String[] { 'X' + (String) arg };
    } ).when( request[ 0 ] ).getParameterValues( anyString() );

    knownOptions[ 0 ] = new String[] { "known-1", "known-2", "known-3", "known-4" };

    transMeta[ 0 ] = spy( TransMeta.class );
    transMeta[ 0 ].addParameterDefinition( "parm-1", "parm-1-def", "parm-1-desc" );
    transMeta[ 0 ].addParameterDefinition( "parm-2", "parm-2-def", "parm-2-desc" );
    transMeta[ 0 ].addParameterDefinition( "parm-3", "parm-3-def", "parm-3-desc" );
    transMeta[ 0 ].addParameterDefinition( "parm-4", "parm-4-def", "parm-4-desc" );
  }

  @Test
  public void testOpenRepository_NullOrEmptyRepositoryName() throws Exception {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();

    assertNull( executeTransServlet.openRepository( null, AUTHORIZED_USER, PASSWORD ) );
    assertNull( executeTransServlet.openRepository( StringUtil.EMPTY_STRING, AUTHORIZED_USER, PASSWORD ) );
  }

  @Test
  public void testGetService() {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();

    assertNotNull( executeTransServlet.getService() );
  }

  @Test
  public void testGetContextPath() {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();

    assertNotNull( executeTransServlet.getContextPath() );
  }

  @Test
  public void testCheckExecutePermission_NoService() throws Exception {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();
    Repository repository = mock( Repository.class );
    doReturn( null ).when( repository ).getService( any() );

    assertFalse( executeTransServlet.checkExecutePermission( repository ) );
  }

  @Test
  public void testCheckExecutePermission_Success() throws Exception {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();
    Repository repository = mock( Repository.class );
    RepositorySecurityProvider repositorySecurityProvider = mock( RepositorySecurityProvider.class );
    doNothing().when( repositorySecurityProvider ).validateAction( RepositoryOperation.EXECUTE_TRANSFORMATION );
    doReturn( repositorySecurityProvider ).when( repository ).getService( any() );

    assertTrue( executeTransServlet.checkExecutePermission( repository ) );
  }

  @Test
  public void testCheckExecutePermission_NoPermission() throws Exception {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();
    Repository repository = mock( Repository.class );
    RepositorySecurityProvider repositorySecurityProvider = mock( RepositorySecurityProvider.class );
    doThrow( new KettleException() ).when( repositorySecurityProvider )
      .validateAction( RepositoryOperation.EXECUTE_TRANSFORMATION );
    doReturn( repositorySecurityProvider ).when( repository ).getService( any() );

    assertFalse( executeTransServlet.checkExecutePermission( repository ) );
  }

  @Test
  public void testCheckExecutePermission_NoRepository() {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();

    assertTrue( executeTransServlet.checkExecutePermission( null ) );
  }

  private TransMeta buildTransMeta() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( TRANS_NAME );
    transMeta.setObjectId( new StringObjectId( TRANS_ID ) );
    transMeta.clearChanged();
    return transMeta;
  }

  private StringWriter mockWriter() throws IOException {
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    doReturn( printWriter ).when( spyHttpServletResponse ).getWriter();
    return out;
  }
}
