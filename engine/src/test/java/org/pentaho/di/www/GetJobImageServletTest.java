/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.pentaho.di.core.util.Assert.assertTrue;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class GetJobImageServletTest {
  private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!!

  private HttpServletRequest mockHttpServletRequest;
  private HttpServletResponse spyHttpServletResponse;
  private JobMap jobMap;
  private GetJobImageServlet spyGetJobImageServlet;

  private static String JOB_ID = "123";
  private static String JOB_NAME = "test";
  private static String USE_XML = "Y";

  @Before
  public void setup() {
    mockHttpServletRequest = mock( HttpServletRequest.class );
    spyHttpServletResponse = spy( HttpServletResponse.class );
    jobMap = new JobMap();
    spyGetJobImageServlet = spy( new GetJobImageServlet( jobMap ) );
  }

  @Test
  public void testGetJobImageServletByCarteObjectId() throws Exception {
    Job job = buildJob();

    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( JOB_ID ).when( mockHttpServletRequest ).getParameter( "id" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    jobMap.addJob( JOB_NAME, JOB_ID, job, null );

    // mock job image generation.
    BufferedImage image = new BufferedImage( 200, 200, BufferedImage.TYPE_INT_ARGB );
    doReturn( image ).when( spyGetJobImageServlet ).generateJobImage( any() );

    mockOutputStream();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );
    assertTrue( spyHttpServletResponse.getOutputStream().toString().contains( "PNG" ) );
  }

  @Test
  public void testGetJobImageServletByCarteObjectIdNotFound() throws Exception {
    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( JOB_ID ).when( mockHttpServletRequest ).getParameter( "id" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    StringWriter out = mockWriter();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.CoundNotFindJob", "null", JOB_ID );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testGetJobImageServletByWrongCarteObjectId() throws Exception {
    Job job = buildJob();

    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( "456" ).when( mockHttpServletRequest ).getParameter( "id" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    jobMap.addJob( JOB_NAME, JOB_ID, job, null );

    StringWriter out = mockWriter();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.CoundNotFindJob", "null", "456" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testGetJobImageServletByJobName() throws Exception {
    Job job = buildJob();

    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "name" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    jobMap.addJob( JOB_NAME, JOB_ID, job, null );

    // mock job image generation.
    BufferedImage image = new BufferedImage( 200, 200, BufferedImage.TYPE_INT_ARGB );
    doReturn( image ).when( spyGetJobImageServlet ).generateJobImage( any() );

    mockOutputStream();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );
    assertTrue( spyHttpServletResponse.getOutputStream().toString().contains( "PNG" ) );
  }

  @Test
  public void testGetJobImageServletByJobNameNotFound() throws Exception {
    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "name" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    StringWriter out = mockWriter();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.CoundNotFindJob", JOB_NAME, "null" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testGetJobImageServletByWrongJobName() throws Exception {
    Job job = buildJob();

    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( "wrong" ).when( mockHttpServletRequest ).getParameter( "name" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    jobMap.addJob( JOB_NAME, JOB_ID, job, null );

    StringWriter out = mockWriter();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.CoundNotFindJob", "wrong", "null" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testGetJobImageServletByJobNameDuplicate() throws Exception {
    // Second jobId with the same JOB_NAME.
    String secondJobId = "456";

    Job job = buildJob();

    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( JOB_NAME ).when( mockHttpServletRequest ).getParameter( "name" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    jobMap.addJob( JOB_NAME, JOB_ID, job, null );
    jobMap.addJob( JOB_NAME, secondJobId, job, null );

    StringWriter out = mockWriter();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );
    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.DuplicateJobName", JOB_NAME );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  @Test
  public void testGetJobImageServletWithoutParameters() throws Exception {
    doReturn( GetJobImageServlet.CONTEXT_PATH ).when( mockHttpServletRequest ).getContextPath( );
    doReturn( null ).when( mockHttpServletRequest ).getParameter( "id" );
    doReturn( null ).when( mockHttpServletRequest ).getParameter( "name" );
    doReturn( USE_XML ).when( mockHttpServletRequest ).getParameter( "xml" );

    StringWriter out = mockWriter();

    spyGetJobImageServlet.doGet( mockHttpServletRequest, spyHttpServletResponse );

    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.CoundNotFindJob", "null", "null" );
    assertTrue( out.toString().contains( Encode.forHtml( message ) ) );
  }

  private Job buildJob() {
    JobMeta jobMeta = new JobMeta();
    jobMeta.setCarteObjectId( JOB_ID );
    jobMeta.setName( JOB_NAME );
    JobEntryCopy jobEntryCopy = new JobEntryCopy( );
    jobEntryCopy.setEntry( new JobEntryEmpty() );
    jobEntryCopy.setLocation( 150, 50 );
    jobMeta.addJobEntry( jobEntryCopy );

    Job job = new Job( null, jobMeta );
    job.setName( JOB_NAME );
    return job;
  }

  private StringWriter mockWriter() throws IOException {
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    doReturn( printWriter ).when( spyHttpServletResponse ).getWriter( );
    return out;
  }

  private void mockOutputStream() throws IOException {
    doReturn( new ServletOutputStream() {
      private ByteArrayOutputStream baos = new ByteArrayOutputStream();

      @Override public void write( int b ) {
        baos.write( b );
      }

      public String toString() {
        return baos.toString();
      }
    } ).when( spyHttpServletResponse ).getOutputStream();
  }
}
