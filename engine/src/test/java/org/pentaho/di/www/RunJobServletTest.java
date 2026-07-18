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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;

public class RunJobServletTest {

  private RunJobServlet runJobServlet;

  @Before
  public void setup() {
    runJobServlet = new RunJobServlet();
  }

  @Test
  public void doGetCannotConnectToRepositoryTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "BASIC" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( false );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_UNAUTHORIZED );
  }

  @Test
  public void doGetMissingMandatoryParamJobTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( null );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "BASIC" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetInvalidLogLevelTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repDirInterface = mock( RepositoryDirectoryInterface.class );
    JobMeta jobMeta = mock( JobMeta.class );
    ObjectId objId = mock( ObjectId.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( "dummyJob", repDirInterface ) ).thenReturn( objId );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( Collections.emptyList() ) );
    when( repository.loadJob( same( objId ), nullable( String.class ), nullable( VariableSpace.class )  ) ).thenReturn( jobMeta );
    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetClearsBowlCacheBeforeLoadingJobTest() throws Exception {

    runJobServlet = spy( new RunJobServlet() );

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repDirInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objId = mock( ObjectId.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    doNothing().when( runJobServlet ).clearBowlCache( same( repository ) );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( "dummyJob", repDirInterface ) ).thenReturn( objId );
    JobMeta jobMeta = mock( JobMeta.class );
    when( repository.loadJob( same( objId ), nullable( String.class ), any( VariableSpace.class ) ) ).thenReturn( jobMeta );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( Collections.emptyList() ) );
    when( jobMeta.listParameters() ).thenReturn( new String[0] );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );

    InOrder inOrder = inOrder( runJobServlet, repository );
    inOrder.verify( runJobServlet ).clearBowlCache( same( repository ) );
    inOrder.verify( repository )
      .loadJob( same( objId ), nullable( String.class ), any( VariableSpace.class ) );
  }

  @Test
  public void doGetJobWithNoPermissionsTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repDirInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objId = mock( ObjectId.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( "dummyJob", repDirInterface ) ).thenReturn( objId );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( Collections.emptyList() ) );
    when( repository.loadJob( same( objId ), nullable( String.class ), nullable( VariableSpace.class) ) ).thenThrow( new IdNotFoundException( "", "", RepositoryObjectType.DATABASE ) );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_UNAUTHORIZED );
  }

  @Test
  public void doGetWrongCredentialsRepoTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repDirInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objId = mock( ObjectId.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( "dummyJob", repDirInterface ) ).thenReturn( objId );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( Collections.emptyList() ) );
    when( repository.loadJob( same( objId ), nullable( String.class ), nullable( VariableSpace.class )  ) ).thenThrow( new KettleException( "The server sent HTTP status code 401" ) );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_UNAUTHORIZED );
  }

  @Test
  public void doGetNonExistingJobTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repDirInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objId = mock( ObjectId.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( "dummyJob", repDirInterface ) ).thenReturn( objId );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( Collections.emptyList() ) );
    when( repository.loadJob( same( objId ), nullable( String.class ), nullable( VariableSpace.class ) ) ).thenThrow( new KettleException( "Unable to load job" ) );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_NOT_FOUND );
  }

  @Test
  public void doGetUnexpectedErrorTest() throws Exception {

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repDirInterface = mock( RepositoryDirectoryInterface.class );
    ObjectId objId = mock( ObjectId.class );
    setInternalState( runJobServlet, "transformationMap", transformationMap );

    KettleLogStore.init();
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getParameter( "job" ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( "level" ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( "dummyJob", repDirInterface ) ).thenReturn( objId );
    when( repository.loadJob( objId, null ) ).thenThrow( new KettleException( "" ) );

    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
  }
}
