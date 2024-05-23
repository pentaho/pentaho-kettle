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
