/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.www.cache.CarteStatusCache;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;


@RunWith( PowerMockRunner.class )
public class GetJobStatusServletTest {
  private JobMap mockJobMap;

  private GetJobStatusServlet getJobStatusServlet;

  @Before
  public void setup() {
    mockJobMap = mock( JobMap.class );
    getJobStatusServlet = new GetJobStatusServlet( mockJobMap );
  }

  @Test
  @PrepareForTest( { Encode.class } )
  public void testGetJobStatusServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    PowerMockito.spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
    PowerMockito.verifyStatic( atLeastOnce() );
    Encode.forHtml( anyString() );

  }

  @Test
  @PrepareForTest( { Encode.class, Job.class } )
  public void testGetJobStatusServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Job mockJob = PowerMockito.mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    PowerMockito.spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getJob( any( CarteObjectEntry.class ) ) ).thenReturn( mockJob );
    PowerMockito.when( mockJob.getJobname() ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    PowerMockito.when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
    PowerMockito.when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    PowerMockito.when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( ServletTestUtils.BAD_STRING_TO_TEST ) );

    PowerMockito.verifyStatic( atLeastOnce() );
    Encode.forHtml( anyString() );
  }

  @Test
  @PrepareForTest( { Job.class } )
  public void testGetJobStatus() throws ServletException, IOException {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Job mockJob = PowerMockito.mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
    ServletOutputStream outMock = mock( ServletOutputStream.class );

    String id = "123";
    String logId = "logId";
    String useXml = "Y";

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( id );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( useXml );
    when( mockHttpServletResponse.getOutputStream() ).thenReturn( outMock );
    when( mockJobMap.findJob( id ) ).thenReturn( mockJob );
    PowerMockito.when( mockJob.getJobname() ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    PowerMockito.when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
    PowerMockito.when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    PowerMockito.when( mockJob.isFinished() ).thenReturn( true );
    PowerMockito.when( mockJob.getLogChannelId() ).thenReturn( logId );
    PowerMockito.when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    when( cacheMock.get( logId, 0 ) ).thenReturn( new byte[] { 0, 1, 2 } );
    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( cacheMock, times( 2 ) ).get( logId, 0 );
    verify( cacheMock, times( 1 ) ).put( eq( logId ), anyString(), eq( 0 ) );
    verify( mockJob.getLogChannel(), times( 1 ) );

  }
}
