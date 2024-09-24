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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class GetStatusServletTest {
  private TransformationMap mockTransformationMap;
  private JobMap mockJobMap;
  private GetStatusServlet getStatusServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    mockJobMap = mock( JobMap.class );
    getStatusServlet = new GetStatusServlet( mockTransformationMap, mockJobMap );
  }

  @Test
  public void testGetStatusServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    getStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText(
      ServletTestUtils.getInsideOfTag( "TITLE", out.toString() ) ) ); // title will more reliably be plain text
  }

  @Test
  public void testGetStatusServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    getStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( ServletTestUtils.BAD_STRING_TO_TEST ) );
  }

  @Test
  public void testGetStatusServletAsXmlWhenJobDroppedFromList() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    setupForJobDroppedFromMap( mockHttpServletRequest, mockHttpServletResponse, out );

    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    getStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( "testJobId1" ) );
    assert ( out.toString().contains( "testJobId2" ) );
  }

  @Test
  public void testGetStatusServletAsHtmlWhenJobDroppedFromList() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    setupForJobDroppedFromMap( mockHttpServletRequest, mockHttpServletResponse, out );

    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "N" );
    getStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( "java.lang.NullPointerException" ) );
    assertFalse( out.toString().contains( "testJobId1" ) );
    assert ( out.toString().contains( "testJobId2" ) );
  }

  @Test
  public void testGetStatusServletAsXmlWhenTransDroppedFromList() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    setupForTransDroppedFromMap( mockHttpServletRequest, mockHttpServletResponse, out );

    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    getStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( "testTranId1" ) );
    assert ( out.toString().contains( "testTranId2" ) );
  }

  @Test
  public void testGetStatusServletAsHtmlWhenTransDroppedFromList() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    setupForTransDroppedFromMap( mockHttpServletRequest, mockHttpServletResponse, out );

    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "N" );
    getStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( "java.lang.NullPointerException" ) );
    assertFalse( out.toString().contains( "testTranId1" ) );
    assert ( out.toString().contains( "testTranId2" ) );
  }


  private void setupForJobDroppedFromMap( HttpServletRequest mockHttpServletRequest,
                                          HttpServletResponse mockHttpServletResponse, StringWriter out )
    throws IOException {
    KettleLogStore.init();

    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    PrintWriter printWriter = new PrintWriter( out );
    CarteObjectEntry carteObjectEntry1 = new CarteObjectEntry( "name1", "testJobId1" );
    CarteObjectEntry carteObjectEntry2 = new CarteObjectEntry( "name2", "testJobId2" );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetStatusServlet.CONTEXT_PATH );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    Job mockJob = mock( Job.class );
    when( mockJobMap.getJobObjects() ).thenReturn( Arrays.asList( carteObjectEntry1, carteObjectEntry2 ) );
    when( mockJobMap.getJob( carteObjectEntry1 ) )
      .thenReturn( null );  //Would have done this anyways but this line is to show it is required
    when( mockJobMap.getJob( carteObjectEntry2 ) ).thenReturn( mockJob );
  }

  private void setupForTransDroppedFromMap( HttpServletRequest mockHttpServletRequest,
                                          HttpServletResponse mockHttpServletResponse, StringWriter out )
    throws IOException {
    KettleLogStore.init();

    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    PrintWriter printWriter = new PrintWriter( out );
    CarteObjectEntry carteObjectEntry1 = new CarteObjectEntry( "name1", "testTranId1" );
    CarteObjectEntry carteObjectEntry2 = new CarteObjectEntry( "name2", "testTranId2" );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetStatusServlet.CONTEXT_PATH );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    Job mockJob = mock( Job.class );
    when( mockTransformationMap.getTransformationObjects() ).thenReturn( Arrays.asList( carteObjectEntry1, carteObjectEntry2 ) );
    when( mockTransformationMap.getTransformation( carteObjectEntry1 ) )
      .thenReturn( null );  //Would have done this anyways but this line is to show it is required
    when( mockTransformationMap.getTransformation( carteObjectEntry2 ) ).thenReturn( mockTrans );
  }
}
