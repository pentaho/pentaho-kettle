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
import org.owasp.encoder.Encode;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
  private static final String PASSWORD = "password";

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
  public void testExecuteTransServletTest()
    throws ServletException, IOException, KettleException {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission();
    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

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
  public void testExecuteTransServletTestCantFindDirectory()
    throws ServletException, IOException, KettleException {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission();
    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

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
  public void testExecuteTransServletTestTransNotFoundInDirectory()
    throws ServletException, IOException, KettleException {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( REPOSITORY_NAME ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission();
    doReturn( repository ).when( spyExecuteTransServlet ).openRepository( REPOSITORY_NAME, AUTHORIZED_USER, PASSWORD );

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
  public void testExecuteTransServletTestCantFindRepository() throws ServletException, IOException {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();
    doReturn( "Unknown" ).when( mockHttpServletRequest ).getParameter( REPOSITORY_PARAMETER );
    doReturn( AUTHORIZED_USER ).when( mockHttpServletRequest ).getParameter( USER_PARAMETER );
    doReturn( PASSWORD ).when( mockHttpServletRequest ).getParameter( PASSWORD_PARAMETER );
    doReturn( TRANS_NAME ).when( mockHttpServletRequest ).getParameter( TRANS_PARAMETER );
    doReturn( LEVEL ).when( mockHttpServletRequest ).getParameter( LEVEL_PARAMETER );

    PowerMockito.mockStatic( Encr.class );
    when( Encr.decryptPasswordOptionallyEncrypted( PASSWORD ) ).thenReturn( PASSWORD );

    doReturn( true ).when( spyExecuteTransServlet ).checkExecutePermission();

    KettleLogStore.init();

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.UnableToFindRepository", "Unknown" );
    assertTrue( out.toString().contains( message ) );
  }

  @Test
  public void testNoPermission() throws ServletException, IOException {
    doReturn( ExecuteTransServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath();

    doReturn( false ).when( spyExecuteTransServlet ).checkExecutePermission();

    StringWriter out = mockWriter();
    spyExecuteTransServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    assertTrue( out.toString().contains( WebResult.STRING_ERROR ) );
    String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.ExecutePermissionRequired" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testOpenRepository_NullOrEmptyRepositoryName() throws KettleException {
    ExecuteTransServlet executeTransServlet = new ExecuteTransServlet();

    assertNull( executeTransServlet.openRepository( null, AUTHORIZED_USER, PASSWORD ) );
    assertNull( executeTransServlet.openRepository( StringUtil.EMPTY_STRING, AUTHORIZED_USER, PASSWORD ) );
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
