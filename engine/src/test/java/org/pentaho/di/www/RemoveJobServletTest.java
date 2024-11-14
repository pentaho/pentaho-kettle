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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class RemoveJobServletTest {
  private JobMap mockJobMap;

  private RemoveJobServlet removeJobServlet;

  @Before
  public void setup() {
    mockJobMap = mock( JobMap.class );
    removeJobServlet = new RemoveJobServlet( mockJobMap );
  }

  @Test
  public void testRemoveJobServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( RemoveJobServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      removeJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );
      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
    }
  }

  @Test
  public void testRemoveJobServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Job mockJob = mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
    mockJob.setName( ServletTestUtils.BAD_STRING_TO_TEST );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( RemoveJobServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockJobMap.getJob( any( CarteObjectEntry.class ) ) ).thenReturn( mockJob );
    when( mockJob.getLogChannelId() ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      removeJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );
      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H3", out.toString() ) ) );
    }
  }
}
