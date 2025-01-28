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


package org.pentaho.di.www;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.www.cache.CarteStatusCache;
import org.pentaho.di.www.exception.DuplicateKeyException;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetJobStatusServletTest {
  private JobMap mockJobMap;

  private GetJobStatusServlet getJobStatusServlet;

  @Before
  public void setup() {
    mockJobMap = mock( JobMap.class );
    getJobStatusServlet = new GetJobStatusServlet( mockJobMap );
  }

  @Test
  public void testGetJobStatusServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );

      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
    }

  }

  @Test
  public void testGetJobStatusServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Job mockJob = mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getJob( any( CarteObjectEntry.class ) ) ).thenReturn( mockJob );
    when( mockJob.getJobname() ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ), atLeastOnce() );
      assertFalse( out.toString().contains( ServletTestUtils.BAD_STRING_TO_TEST ) );
    }
  }

  @Test
  public void testGetJobStatus() throws ServletException, IOException {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Job mockJob = mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
    ServletOutputStream outMock = mock( ServletOutputStream.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    String id = "123";
    String logId = "logId";
    String useXml = "Y";
    String name = "dummyName";

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( id );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( useXml );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getOutputStream() ).thenReturn( outMock );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getJob( (CarteObjectEntry) any() ) ).thenReturn( mockJob );
    when( mockJob.getJobname() ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    when( mockJob.isFinished() ).thenReturn( true );
    when( mockJob.getLogChannelId() ).thenReturn( logId );
    when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );
    when( mockJob.getStatus() ).thenReturn( "Finished" );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    when( cacheMock.get( logId, 0 ) ).thenReturn( new byte[] { 0, 1, 2 } );
    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( cacheMock, times( 2 ) ).get( logId, 0 );
    verify( cacheMock, times( 1 ) ).put( eq( logId ), anyString(), eq( 0 ) );
    verify( mockJob, times( 1 ) ).getLogChannel();

  }

  @Test
  public void doGetMissingMandatoryParameterNameUseXMLTest() throws Exception {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( "123" );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( null );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetMissingMandatoryParameterNameUseHTMLTest() throws Exception {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( "123" );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( null );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetConflictingJobNamesUseXMLTest() throws Exception {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( null );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( "dummy_job" );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getUniqueCarteObjectEntry( any() ) ).thenThrow( new DuplicateKeyException() );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_CONFLICT );
  }

  @Test
  public void doGetConflictingJobNamesUseHTMLTest() throws Exception {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( null );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( "dummy_job" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getUniqueCarteObjectEntry( any() ) ).thenThrow( new DuplicateKeyException() );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_CONFLICT );
  }

  @Test
  public void doGetJobNotFoundUseXMLTest() throws Exception {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    mock( Job.class );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( "123" );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( "dummy_job" );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getJob( "dummy_job" ) ).thenReturn( null );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_NOT_FOUND );
  }

  @Test
  public void doGetJobNotFoundUseHTMLTest() throws Exception {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getJobStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    mock( Job.class );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( "123" );
    when( mockHttpServletRequest.getParameter( "name" ) ).thenReturn( "dummy_job" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getJob( "dummy_job" ) ).thenReturn( null );

    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_NOT_FOUND );
  }
}
