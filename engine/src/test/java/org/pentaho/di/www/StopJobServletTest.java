/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.www;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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


public class StopJobServletTest {
  private JobMap mockJobMap;

  private StopJobServlet stopJobServlet;

  @Before
  public void setup() {
    mockJobMap = mock( JobMap.class );
    stopJobServlet = new StopJobServlet( mockJobMap );
  }

  @Test
  public void testStopJobServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
      HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

      StringWriter out = new StringWriter();
      PrintWriter printWriter = new PrintWriter( out );

      when( mockHttpServletRequest.getContextPath() ).thenReturn( StopJobServlet.CONTEXT_PATH );
      when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
      when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

      stopJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );
    }
  }

  @Test
  public void testStopJobServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      KettleLogStore.init();
      HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
      HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
      Job mockJob = mock( Job.class );
      JobMeta mockJobMeta = mock( JobMeta.class );
      LogChannelInterface mockLogChannelInterface = mock( LogChannelInterface.class );
      mockJob.setName( ServletTestUtils.BAD_STRING_TO_TEST );
      StringWriter out = new StringWriter();
      PrintWriter printWriter = new PrintWriter( out );

      when( mockHttpServletRequest.getContextPath() ).thenReturn( StopJobServlet.CONTEXT_PATH );
      when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
      when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
      when( mockJobMap.getJob( any( CarteObjectEntry.class ) ) ).thenReturn( mockJob );
      when( mockJob.getLogChannelId() ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
      when( mockJob.getLogChannel() ).thenReturn( mockLogChannelInterface );
      when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
      when( mockJobMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

      stopJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );

      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );
    }
  }
}
