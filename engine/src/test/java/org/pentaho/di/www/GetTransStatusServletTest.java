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
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.cache.CarteStatusCache;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class GetTransStatusServletTest {
  private TransformationMap mockTransformationMap;

  private GetTransStatusServlet getTransStatusServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    getTransStatusServlet = new GetTransStatusServlet( mockTransformationMap );
  }

  @Test
  @PrepareForTest( { Encode.class } )
  public void testGetTransStatusServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    PowerMockito.spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetTransStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    getTransStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );

    PowerMockito.verifyStatic( atLeastOnce() );
    Encode.forHtml( anyString() );
  }

  @Test
  @PrepareForTest( { Encode.class } )
  public void testGetTransStatusServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    PowerMockito.spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetTransStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    getTransStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "TITLE", out.toString() ) ) );

    PowerMockito.verifyStatic( atLeastOnce() );
    Encode.forHtml( anyString() );
  }

  @Test
  public void testGetTransStatus() throws ServletException, IOException {
    KettleLogStore.init();
    CarteStatusCache cacheMock = mock( CarteStatusCache.class );
    getTransStatusServlet.cache = cacheMock;
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    ServletOutputStream outMock = mock( ServletOutputStream.class );

    String id = "123";
    String logId = "logId";
    String useXml = "Y";

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetTransStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "id" ) ).thenReturn( id );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( useXml );
    when( mockHttpServletResponse.getOutputStream() ).thenReturn( outMock );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTrans.getLogChannelId() ).thenReturn( logId );
    when( mockTrans.isFinishedOrStopped() ).thenReturn( true );
    when( mockTrans.getStatus() ).thenReturn( "Finished" );

    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    getTransStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    when( cacheMock.get( logId, 0 ) ).thenReturn( new byte[] { 0, 1, 2 } );
    getTransStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( cacheMock, times( 2 ) ).get( logId, 0 );
    verify( cacheMock, times( 1 ) ).put( eq( logId ), anyString(), eq( 0 ) );
    verify( mockTrans, times( 1 ) ).getLogChannel();

  }

}
