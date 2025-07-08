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
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class PrepareExecutionTransServletTest {
  private TransformationMap mockTransformationMap;

  private PrepareExecutionTransServlet prepareExecutionTransServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    prepareExecutionTransServlet = new PrepareExecutionTransServlet( mockTransformationMap );
  }

  @Test
  public void testPauseTransServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    spy( Encode.class );
    when( mockHttpServletRequest.getContextPath() ).thenReturn( PrepareExecutionTransServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    prepareExecutionTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );

    Encode.forHtml( "");
  }

  @Test
  public void testPauseTransServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransConfiguration mockTransConf = mock( TransConfiguration.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    TransExecutionConfiguration mockTransExecutionConf = mock( TransExecutionConfiguration.class );

    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( PrepareExecutionTransServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTransformationMap.getConfiguration( any( CarteObjectEntry.class ) ) ).thenReturn( mockTransConf );
    when( mockTransConf.getTransExecutionConfiguration() ).thenReturn( mockTransExecutionConf );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    prepareExecutionTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );

    Encode.forHtml( "" );
  }
}
