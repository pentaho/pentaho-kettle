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

import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class PauseTransServletIT {
  private TransformationMap mockTransformationMap;

  private PauseTransServlet pauseTransServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    pauseTransServlet = new PauseTransServlet( mockTransformationMap );
  }

  @Test
  public void testPauseTransServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( PauseTransServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    pauseTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
  }

  @Test
  public void testPauseTransServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    KettleLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( PauseTransServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    pauseTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( ServletTestUtils.BAD_STRING ) );
  }
}
