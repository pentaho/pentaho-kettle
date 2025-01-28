///*! ******************************************************************************
// *
// * Pentaho
// *
// * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
// *
// * Use of this software is governed by the Business Source License included
// * in the LICENSE.TXT file.
// *
// * Change Date: 2029-07-20
// ******************************************************************************/
//
//
//package org.pentaho.di.www;
//
//import static junit.framework.Assert.assertFalse;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.pentaho.di.core.gui.Point;
//import org.pentaho.di.core.logging.KettleLogStore;
//import org.pentaho.di.core.logging.LogChannelInterface;
//import org.pentaho.di.job.Job;
//import org.pentaho.di.job.JobMeta;
//
//public class GetJobStatusServletIT {
//  private JobMap mockJobMap;
//
//  private GetJobStatusServlet getJobStatusServlet;
//
//  @Before
//  public void setup() {
//    mockJobMap = mock( JobMap.class );
//    getJobStatusServlet = new GetJobStatusServlet( mockJobMap );
//  }
//
//  @Test
//  public void testGetJobStatusServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
//    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
//    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
//
//    StringWriter out = new StringWriter();
//    PrintWriter printWriter = new PrintWriter( out );
//
//    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
//    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
//    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
//
//    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
//
//    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
//  }
//
//  @Test
//  public void testGetJobStatusServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
//    KettleLogStore.init();
//    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
//    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
//    Job mockJob = mock( Job.class );
//    JobMeta mockJobMeta = mock( JobMeta.class );
//    LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
//    mockJob.setName( ServletTestUtils.BAD_STRING );
//    StringWriter out = new StringWriter();
//    PrintWriter printWriter = new PrintWriter( out );
//
//    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetJobStatusServlet.CONTEXT_PATH );
//    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
//    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
//    when( mockJobMap.getJob( any( CarteObjectEntry.class ) ) ).thenReturn( mockJob );
//    when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
//    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
//    when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );
//
//    getJobStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
//
//    assertFalse( out.toString().contains( ServletTestUtils.BAD_STRING ) );
//  }
//}
