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
import org.owasp.encoder.Encode;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.cache.CarteStatusCache;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetTransStatusServletTest {
  private TransformationMap mockTransformationMap;

  private GetTransStatusServlet getTransStatusServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    getTransStatusServlet = new GetTransStatusServlet( mockTransformationMap );
  }

  @Test
  public void testGetTransStatusServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetTransStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    getTransStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );

    Encode.forHtml( "" );
  }

  @Test
  public void testGetTransStatusServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetTransStatusServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    getTransStatusServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "TITLE", out.toString() ) ) );

    Encode.forHtml( "" );
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
