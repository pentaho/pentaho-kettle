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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
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

    when( mockHttpServletRequest.getParameter( eq( "job" ) ) ).thenReturn( "dummyJob" );
    when( mockHttpServletRequest.getParameter( eq ("level" ) ) ).thenReturn( "SomethingInvalid" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getRepository() ).thenReturn( repository );
    when( repository.isConnected() ).thenReturn( true );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( repDirInterface );
    when( repDirInterface.findDirectory( anyString() ) ).thenReturn( repDirInterface );
    when( repository.getJobId( eq( "dummyJob" ), eq( repDirInterface ) ) ).thenReturn( objId );
    when( repository.loadJob( eq( objId ), nullable( String.class ) ) ).thenReturn( jobMeta );
    when( mockHttpServletRequest.getParameterNames() ).thenReturn( Collections.enumeration( Collections.emptyList() ) );
    runJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
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
    when( repository.loadJob( objId, null ) ).thenThrow( new IdNotFoundException( "", "", RepositoryObjectType.DATABASE ) );

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
    when( repository.loadJob( objId, null ) ).thenThrow( new KettleException( "The server sent HTTP status code 401" ) );

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
    when( repository.loadJob( objId, null ) ).thenThrow( new KettleException( "Unable to load job" ) );

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
