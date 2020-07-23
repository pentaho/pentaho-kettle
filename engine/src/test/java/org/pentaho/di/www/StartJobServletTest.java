/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.Const;
import org.pentaho.di.job.Job;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( PowerMockRunner.class )
public class StartJobServletTest {
  private JobMap mockJobMap;

  private StartJobServlet startJobServlet;

  @Before
  public void setup() {
    mockJobMap = mock( JobMap.class );
    startJobServlet = spy( new StartJobServlet( mockJobMap ) );
  }

  @Test
  public void testSetResponseEncoding_EmptyDefaultEncoding() {
    System.setProperty( StartJobServlet.KETTLE_DEFAULT_SERVLET_ENCODING, "" );
    testSetResponseEncodingBase( Const.XML_ENCODING );
  }

  @Test
  public void testSetResponseEncoding_CustomEncoding() {
    String dummyEncoding = "dummyEncoding";
    System.setProperty( StartJobServlet.KETTLE_DEFAULT_SERVLET_ENCODING, dummyEncoding );
    testSetResponseEncodingBase( dummyEncoding );
  }

  @Test
  public void testSetResponseEncoding_SpaceDefaultEncoding() {
    System.setProperty( StartJobServlet.KETTLE_DEFAULT_SERVLET_ENCODING, "   " );
    testSetResponseEncodingBase( Const.XML_ENCODING );
  }

  private void testSetResponseEncodingBase( String expectedEncoding ) {
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    doAnswer( invocationOnMock -> {
      String arg = (String) invocationOnMock.getArguments()[0];

      assertNotNull( arg );
      if ( !arg.contains( expectedEncoding ) ) {
        fail( "Unexpected encoding found!" );
      }
      return null;
    } ).when( mockHttpServletResponse ).setContentType( anyString() );

    doAnswer( invocationOnMock -> {
      String arg = (String) invocationOnMock.getArguments()[0];

      assertNotNull( arg );
      if ( !arg.contains( expectedEncoding ) ) {
        fail( "Unexpected encoding found!" );
      }
      return null;
    } ).when( mockHttpServletResponse ).setCharacterEncoding( anyString() );

    startJobServlet.setResponseEncoding( mockHttpServletResponse );

    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );
  }

  @Test
  public void testGetParameter_ParameterDoesNotExist() {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );

    String parmValue = startJobServlet.getParameter( mockHttpServletRequest, "dummy");

    assertNull( parmValue );
  }

  @Test
  public void testGetParameter_ParameterValueWithTrailingSpaces() {
    String theValue = "\tValue   ";
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    doReturn( theValue ).when( mockHttpServletRequest ).getParameter( "parm" );

    String parmValue = startJobServlet.getParameter( mockHttpServletRequest, "parm");

    assertNotNull( parmValue );
    assertEquals( theValue.trim(), parmValue );
  }

  @Test
  public void testGetParameter_ParameterValueJustSpaces() {
    String theValue = "\t  ";
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    doReturn( theValue ).when( mockHttpServletRequest ).getParameter( "parm" );

    String parmValue = startJobServlet.getParameter( mockHttpServletRequest, "parm");

    assertNull( parmValue );
  }

  @Test
  public void testGetCarteObjectEntry_WithNameWithoutId() {
    String jobName = "jobName";

    JobMap jobMap = mock( JobMap.class );
    doReturn( jobMap ).when( startJobServlet ).getJobMap();
    CarteObjectEntry carteObjectEntry = mock( CarteObjectEntry.class );
    doReturn( carteObjectEntry ).when( jobMap ).getFirstCarteObjectEntry( jobName );

    CarteObjectEntry cOE = startJobServlet.getCarteObjectEntry( jobName, null );

    assertNotNull( cOE );
    assertEquals( carteObjectEntry, cOE );
    verify( jobMap ).getFirstCarteObjectEntry( anyString() );
  }

  @Test
  public void testGetCarteObjectEntry_WithNameWithId() {
    String jobName = "jobName";

    JobMap jobMap = mock( JobMap.class );
    doReturn( jobMap ).when( startJobServlet ).getJobMap();
    CarteObjectEntry carteObjectEntry = mock( CarteObjectEntry.class );
    doReturn( carteObjectEntry ).when( jobMap ).getFirstCarteObjectEntry( jobName );

    CarteObjectEntry cOE = startJobServlet.getCarteObjectEntry( jobName, null );

    assertNotNull( cOE );
    assertEquals( carteObjectEntry, cOE );
    verify( jobMap ).getFirstCarteObjectEntry( anyString() );
  }

  @Test
  public void testGetCarteObjectEntry_WithoutNameWithId_JobFound() {
    String jobName = "jobName";
    String jobId = "jobId";

    JobMap jobMapMock = mock( JobMap.class );
    doReturn( jobMapMock ).when( startJobServlet ).getJobMap();
    Job job = mock( Job.class );
    doReturn( jobName ).when( job ).getJobname();
    doAnswer( invocationOnMock -> {
      CarteObjectEntry arg = (CarteObjectEntry) invocationOnMock.getArguments()[0];

      assertNull( arg.getName() );
      assertEquals( jobId, arg.getId() );

      return job;
    } ).when( jobMapMock ).getJob( any( CarteObjectEntry.class ) );

    CarteObjectEntry carteObjectEntry = startJobServlet.getCarteObjectEntry( null, jobId );

    assertNotNull( carteObjectEntry );
    assertEquals( jobName, carteObjectEntry.getName() );
    assertEquals( jobId, carteObjectEntry.getId() );
    verify( jobMapMock, times( 0 ) ).getFirstCarteObjectEntry( anyString() );
  }

  @Test
  public void testGetCarteObjectEntry_WithoutNameWithId_JobNotFound() {
    String jobName = "jobName";
    String jobId = "jobId";

    JobMap jobMapMock = mock( JobMap.class );
    doReturn( jobMapMock ).when( startJobServlet ).getJobMap();
    doAnswer( invocationOnMock -> {
      CarteObjectEntry arg = (CarteObjectEntry) invocationOnMock.getArguments()[0];

      assertNull( arg.getName() );
      assertEquals( jobId, arg.getId() );

      return null;
    } ).when( jobMapMock ).getJob( any( CarteObjectEntry.class ) );

    CarteObjectEntry carteObjectEntry = startJobServlet.getCarteObjectEntry( null, jobId );

    assertNull( carteObjectEntry );
    verify( jobMapMock, times( 0 ) ).getFirstCarteObjectEntry( anyString() );
  }

  @Test
  public void testGetService() {
    String service = startJobServlet.getService();

    assertNotNull( service );
    assertTrue( service.contains( StartJobServlet.CONTEXT_PATH ) );
  }

  // ////////// ////////// ////////// ////////// //////////
  // Test the scenario were no Job Name is given.
  //
  // The actual test is done on testDoGet_NoName_Base.
  // The following tests exist to test the various output variants.

  @Test
  public void testDoGet_NoName_XmlOutput1() throws Exception {
    testDoGet_NoName_Base( "Y" );
  }

  @Test
  public void testDoGet_NoName_XmlOutput2() throws Exception {
    testDoGet_NoName_Base( "Y " );
  }

  @Test
  public void testDoGet_NoName_HtmlOutput1() throws Exception {
    testDoGet_NoName_Base( "NN" );
  }

  @Test
  public void testDoGet_NoName_HtmlOutput2() throws Exception {
    testDoGet_NoName_Base( "" );
  }

  @Test
  public void testDoGet_NoName_HtmlOutput3() throws Exception {
    testDoGet_NoName_Base( null );
  }

  /**
   * <p>Base method for the testDoGet_NoName_* tests.</p>
   *
   * @param useXmlParm the value for the {@link StartJobServlet#PARM_USE_XML_OUTPUT}
   * @throws Exception
   */
  public void testDoGet_NoName_Base( String useXmlParm ) throws Exception {
    String jobId = "jobId";

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( StartJobServlet.CONTEXT_PATH );

    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    doReturn( null ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_NAME );
    doReturn( jobId ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_ID );
    doReturn( useXmlParm ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_USE_XML_OUTPUT );

    PrintWriter printWriterMock = mock( PrintWriter.class );
    doReturn( printWriterMock ).when( mockHttpServletResponse ).getWriter();

    // The actual call
    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // The encoding was set
    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );

    // It must report Bad Request
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_BAD_REQUEST );
  }

  // ////////// ////////// ////////// ////////// //////////
  // Test the scenario were no CarteObjectEntry is found.
  //
  // The actual test is done on testDoGet_NoCarteObject_Base.
  // The following tests exist to test the various output variants.

  @Test
  public void testDoGet_NoCarteObject_XmlOutput1() throws Exception {
    testDoGet_NoCarteObject_Base( "Y" );
  }

  @Test
  public void testDoGet_NoCarteObject_XmlOutput2() throws Exception {
    testDoGet_NoCarteObject_Base( "Y " );
  }

  @Test
  public void testDoGet_NoCarteObject_HtmlOutput1() throws Exception {
    testDoGet_NoCarteObject_Base( "NN" );
  }

  @Test
  public void testDoGet_NoCarteObject_HtmlOutput2() throws Exception {
    testDoGet_NoCarteObject_Base( "" );
  }

  @Test
  public void testDoGet_NoCarteObject_HtmlOutput3() throws Exception {
    testDoGet_NoCarteObject_Base( null );
  }

  /**
   * <p>Base method for the testDoGet_NoCarteObject_* tests.</p>
   *
   * @param useXmlParm the value for the {@link StartJobServlet#PARM_USE_XML_OUTPUT}
   * @throws Exception
   */
  public void testDoGet_NoCarteObject_Base( String useXmlParm ) throws Exception {
    String jobName = "jobName";
    String jobId = "jobId";

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( StartJobServlet.CONTEXT_PATH );

    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    doReturn( jobName ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_NAME );
    doReturn( jobId ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_ID );
    doReturn( useXmlParm ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_USE_XML_OUTPUT );

    PrintWriter printWriterMock = mock( PrintWriter.class );
    doReturn( printWriterMock ).when( mockHttpServletResponse ).getWriter();

    doReturn( null ).when( startJobServlet ).getCarteObjectEntry( jobName, jobId );

    // The actual call
    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // The encoding was set
    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );

    verify( startJobServlet ).getCarteObjectEntry( anyString(), anyString() );

    // It must report File Not Found
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_NOT_FOUND );
  }

  // ////////// ////////// ////////// ////////// //////////
  // Test the scenario were no Job is found.
  //
  // The actual test is done on testDoGet_WithCarteObjectButNoJob_Base.
  // The following tests exist to test the various output variants.

  @Test
  public void testDoGet_WithCarteObjectButNoJob_XmlOutput1() throws Exception {
    testDoGet_WithCarteObjectButNoJob_Base( "Y" );
  }

  @Test
  public void testDoGet_WithCarteObjectButNoJob_XmlOutput2() throws Exception {
    testDoGet_WithCarteObjectButNoJob_Base( "Y " );
  }

  @Test
  public void testDoGet_WithCarteObjectButNoJob_HtmlOutput1() throws Exception {
    testDoGet_WithCarteObjectButNoJob_Base( "NN" );
  }

  @Test
  public void testDoGet_WithCarteObjectButNoJob_HtmlOutput2() throws Exception {
    testDoGet_WithCarteObjectButNoJob_Base( "" );
  }

  @Test
  public void testDoGet_WithCarteObjectButNoJob_HtmlOutput3() throws Exception {
    testDoGet_WithCarteObjectButNoJob_Base( null );
  }

  /**
   * <p>Base method for the testDoGet_WithCarteObjectButNoJob_* tests.</p>
   *
   * @param useXmlParm the value for the {@link StartJobServlet#PARM_USE_XML_OUTPUT}
   * @throws Exception
   */
  public void testDoGet_WithCarteObjectButNoJob_Base( String useXmlParm ) throws Exception {
    String jobName = "jobName";
    String jobId = "jobId";

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( StartJobServlet.CONTEXT_PATH );

    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    doReturn( jobName ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_NAME );
    doReturn( jobId ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_ID );
    doReturn( useXmlParm ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_USE_XML_OUTPUT );

    PrintWriter printWriterMock = mock( PrintWriter.class );
    doReturn( printWriterMock ).when( mockHttpServletResponse ).getWriter();

    CarteObjectEntry carteObjectEntryMock = mock( CarteObjectEntry.class );
    doReturn( jobName ).when( carteObjectEntryMock ).getName();
    doReturn( jobId ).when( carteObjectEntryMock ).getId();
    doReturn( carteObjectEntryMock ).when( startJobServlet ).getCarteObjectEntry( jobName, jobId );

    JobMap jobMapMock = mock( JobMap.class );
    doReturn( jobMapMock ).when( startJobServlet ).getJobMap();
    doAnswer( invocationOnMock -> {
      CarteObjectEntry arg = (CarteObjectEntry) invocationOnMock.getArguments()[0];

      // The correct values should be used to query
      assertEquals( jobName, arg.getName() );
      assertEquals( jobId, arg.getId() );

      return null;
    } ).when( jobMapMock ).getJob( any( CarteObjectEntry.class ) );

    // The actual call
    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // The encoding was set
    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );

    verify( startJobServlet ).getCarteObjectEntry( anyString(), anyString() );

    // It must report File Not Found
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_NOT_FOUND );
  }

  // ////////// ////////// ////////// ////////// //////////
  // Test the scenario were the Job has already completed.
  //
  // The actual test is done on testDoGet_JobCompleted_Base.
  // The following tests exist to test the various output variants.

  @Test
  public void testDoGet_JobCompleted_XmlOutput1() throws Exception {
    testDoGet_JobCompleted_Base( "Y" );
  }

  @Test
  public void testDoGet_JobCompleted_XmlOutput2() throws Exception {
    testDoGet_JobCompleted_Base( "Y " );
  }

  @Test
  public void testDoGet_JobCompleted_HtmlOutput1() throws Exception {
    testDoGet_JobCompleted_Base( "NN" );
  }

  @Test
  public void testDoGet_JobCompleted_HtmlOutput2() throws Exception {
    testDoGet_JobCompleted_Base( "" );
  }

  @Test
  public void testDoGet_JobCompleted_HtmlOutput3() throws Exception {
    testDoGet_JobCompleted_Base( null );
  }

  /**
   * <p>Base method for the testDoGet_JobCompleted_* tests.</p>
   *
   * @param useXmlParm the value for the {@link StartJobServlet#PARM_USE_XML_OUTPUT}
   * @throws Exception
   */
  public void testDoGet_JobCompleted_Base( String useXmlParm ) throws Exception {
    String jobName = "jobName";
    String jobId = "jobId";

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( StartJobServlet.CONTEXT_PATH );

    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    doReturn( jobName ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_NAME );
    doReturn( jobId ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_ID );
    doReturn( useXmlParm ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_USE_XML_OUTPUT );

    PrintWriter printWriterMock = mock( PrintWriter.class );
    doReturn( printWriterMock ).when( mockHttpServletResponse ).getWriter();

    CarteObjectEntry carteObjectEntryMock = mock( CarteObjectEntry.class );
    doReturn( jobName ).when( carteObjectEntryMock ).getName();
    doReturn( jobId ).when( carteObjectEntryMock ).getId();
    doReturn( carteObjectEntryMock ).when( startJobServlet ).getCarteObjectEntry( jobName, jobId );

    JobMap jobMapMock = mock( JobMap.class );
    doReturn( jobMapMock ).when( startJobServlet ).getJobMap();
    Job jobMock1 = mock( Job.class );
    doReturn( true ).when( jobMock1 ).isInitialized();
    doReturn( false ).when( jobMock1 ).isActive();
    doAnswer( invocationOnMock -> {
      CarteObjectEntry arg = (CarteObjectEntry) invocationOnMock.getArguments()[0];

      // The correct values should be used to query
      assertEquals( jobName, arg.getName() );
      assertEquals( jobId, arg.getId() );

      return jobMock1;
    } ).when( jobMapMock ).getJob( any( CarteObjectEntry.class ) );

    Job jobMock2 = mock( Job.class );
    doReturn( jobMock2 ).when( startJobServlet ).recreateJob( any(), any() );

    // The actual call
    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // The encoding was set
    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );

    verify( startJobServlet ).getCarteObjectEntry( anyString(), anyString() );

    // The job was recreated
    verify( startJobServlet ).recreateJob( any(), any() );

    // The new job was executed
    verify( startJobServlet, times( 0 ) ).runJob( jobMock1 );
    verify( startJobServlet ).runJob( jobMock2 );
    verify( startJobServlet ).runJob( any( Job.class ) );

    // It must report OK
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
  }

  // ////////// ////////// ////////// ////////// //////////
  // Test the scenario were the Job is not initialized not active.
  //
  // The actual test is done on testDoGet_JobNotInitializedAndNotActive_Base.
  // The following tests exist to test the various output variants.

  @Test
  public void testDoGet_JobNotInitializedAndNotActive_XmlOutput1() throws Exception {
    testDoGet_JobNotInitializedAndNotActive_Base( "Y" );
  }

  @Test
  public void testDoGet_JobNotInitializedAndNotActive_XmlOutput2() throws Exception {
    testDoGet_JobNotInitializedAndNotActive_Base( "Y " );
  }

  @Test
  public void testDoGet_JobNotInitializedAndNotActive_HtmlOutput1() throws Exception {
    testDoGet_JobNotInitializedAndNotActive_Base( "NN" );
  }

  @Test
  public void testDoGet_JobNotInitializedAndNotActive_HtmlOutput2() throws Exception {
    testDoGet_JobNotInitializedAndNotActive_Base( "" );
  }

  @Test
  public void testDoGet_JobNotInitializedAndNotActive_HtmlOutput3() throws Exception {
    testDoGet_JobNotInitializedAndNotActive_Base( null );
  }

  /**
   * <p>Base method for the testDoGet_JobNotInitializedAndNotActive_* tests.</p>
   *
   * @param useXmlParm the value for the {@link StartJobServlet#PARM_USE_XML_OUTPUT}
   * @throws Exception
   */
  public void testDoGet_JobNotInitializedAndNotActive_Base( String useXmlParm ) throws Exception {
    String jobName = "jobName";
    String jobId = "jobId";

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( StartJobServlet.CONTEXT_PATH );

    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    doReturn( jobName ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_NAME );
    doReturn( jobId ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_ID );
    doReturn( useXmlParm ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_USE_XML_OUTPUT );

    PrintWriter printWriterMock = mock( PrintWriter.class );
    doReturn( printWriterMock ).when( mockHttpServletResponse ).getWriter();

    CarteObjectEntry carteObjectEntryMock = mock( CarteObjectEntry.class );
    doReturn( jobName ).when( carteObjectEntryMock ).getName();
    doReturn( jobId ).when( carteObjectEntryMock ).getId();
    doReturn( carteObjectEntryMock ).when( startJobServlet ).getCarteObjectEntry( jobName, jobId );

    JobMap jobMapMock = mock( JobMap.class );
    doReturn( jobMapMock ).when( startJobServlet ).getJobMap();
    Job jobMock = mock( Job.class );
    doReturn( false ).when( jobMock ).isInitialized();
    doReturn( false ).when( jobMock ).isActive();
    doAnswer( invocationOnMock -> {
      CarteObjectEntry arg = (CarteObjectEntry) invocationOnMock.getArguments()[0];

      // The correct values should be used to query
      assertEquals( jobName, arg.getName() );
      assertEquals( jobId, arg.getId() );

      return jobMock;
    } ).when( jobMapMock ).getJob( any( CarteObjectEntry.class ) );

    // Run Ok
    doNothing().when( startJobServlet ).runJob( jobMock );

    // The actual call
    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // The encoding was set
    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );

    verify( startJobServlet ).getCarteObjectEntry( anyString(), anyString() );

    // No recreation is needed
    verify( startJobServlet, times( 0 ) ).recreateJob( any(), any() );

    // The job was executed
    verify( startJobServlet ).runJob( jobMock );
    verify( startJobServlet ).runJob( any( Job.class ) );

    // It must report OK
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
  }

  // ////////// ////////// ////////// ////////// //////////
  // Test the scenario were an exception is thrown while executing the Job.
  //
  // The actual test is done on testDoGet_JobException_Base.
  // The following tests exist to test the various output variants.

  @Test
  public void testDoGet_JobException_XmlOutput1() throws Exception {
    testDoGet_JobException_Base( "Y" );
  }

  @Test
  public void testDoGet_JobException_XmlOutput2() throws Exception {
    testDoGet_JobException_Base( "Y " );
  }

  @Test
  public void testDoGet_JobException_HtmlOutput1() throws Exception {
    testDoGet_JobException_Base( "NN" );
  }

  @Test
  public void testDoGet_JobException_HtmlOutput2() throws Exception {
    testDoGet_JobException_Base( "" );
  }

  @Test
  public void testDoGet_JobException_HtmlOutput3() throws Exception {
    testDoGet_JobException_Base( null );
  }

  /**
   * <p>Base method for the testDoGet_JobException_* tests.</p>
   *
   * @param useXmlParm the value for the {@link StartJobServlet#PARM_USE_XML_OUTPUT}
   * @throws Exception
   */
  public void testDoGet_JobException_Base( String useXmlParm ) throws Exception {
    String jobName = "jobName";
    String jobId = "jobId";

    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( StartJobServlet.CONTEXT_PATH );

    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    doReturn( jobName ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_NAME );
    doReturn( jobId ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_JOB_ID );
    doReturn( useXmlParm ).when( mockHttpServletRequest ).getParameter( StartJobServlet.PARM_USE_XML_OUTPUT );

    PrintWriter printWriterMock = mock( PrintWriter.class );
    doReturn( printWriterMock ).when( mockHttpServletResponse ).getWriter();

    CarteObjectEntry carteObjectEntryMock = mock( CarteObjectEntry.class );
    doReturn( jobName ).when( carteObjectEntryMock ).getName();
    doReturn( jobId ).when( carteObjectEntryMock ).getId();
    doReturn( carteObjectEntryMock ).when( startJobServlet ).getCarteObjectEntry( jobName, jobId );

    JobMap jobMapMock = mock( JobMap.class );
    doReturn( jobMapMock ).when( startJobServlet ).getJobMap();
    Job jobMock = mock( Job.class );
    doReturn( false ).when( jobMock ).isInitialized();
    doReturn( false ).when( jobMock ).isActive();
    doAnswer( invocationOnMock -> {
      CarteObjectEntry arg = (CarteObjectEntry) invocationOnMock.getArguments()[0];

      // The correct values should be used to query
      assertEquals( jobName, arg.getName() );
      assertEquals( jobId, arg.getId() );

      return jobMock;
    } ).when( jobMapMock ).getJob( any( CarteObjectEntry.class ) );

    // Throw an exception on run
    doThrow( new RuntimeException() ).when( startJobServlet ).runJob( jobMock );

    // The actual call
    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // The encoding was set
    verify( mockHttpServletResponse ).setCharacterEncoding( anyString() );
    verify( mockHttpServletResponse ).setContentType( anyString() );

    verify( startJobServlet ).getCarteObjectEntry( anyString(), anyString() );

    // No recreation is needed
    verify( startJobServlet, times( 0 ) ).recreateJob( any(), any() );

    // The job was executed
    verify( startJobServlet ).runJob( jobMock );
    verify( startJobServlet ).runJob( any( Job.class ) );

    // It must report OK
    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
  }

  @Test
  public void testDoGet_JettyMode() throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    doReturn( true ).when( startJobServlet ).isJettyMode();
    doReturn( "WrongContextPath" ).when( mockHttpServletRequest ).getContextPath();

    startJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    // It should not have reached this point
    verify( startJobServlet, times( 0 ) )
      .setResponseEncoding( any( HttpServletResponse.class ) );
  }

  @Test
  public void testJobMap() {
    // Create a StartJobServlet instance without providing a job map
    StartJobServlet startJobServlet = new StartJobServlet();

    // Get the underlying job map
    JobMap jobMap = startJobServlet.getJobMap();

    // It must not be null
    assertNotNull( jobMap );
    // The same object must be returned
    assertEquals( jobMap, startJobServlet.getJobMap() );
  }
}
