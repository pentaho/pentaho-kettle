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
import org.pentaho.di.trans.Trans;

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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class CleanupTransServletTest {
  private TransformationMap mockTransformationMap;

  private CleanupTransServlet cleanupTransServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    cleanupTransServlet = new CleanupTransServlet( mockTransformationMap );
  }

  @Test
  public void testCleanupTransServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
      HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
      StringWriter out = new StringWriter();
      PrintWriter printWriter = new PrintWriter( out );
      spy( Encode.class );
      when( mockHttpServletRequest.getContextPath() ).thenReturn( CleanupTransServlet.CONTEXT_PATH );
      when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
      when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

      cleanupTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );
    }
  }

  @Test
  public void testCleanupTransServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
      HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
      Trans mockTrans = mock( Trans.class );
      StringWriter out = new StringWriter();
      PrintWriter printWriter = new PrintWriter( out );
      spy( Encode.class );
      when( mockHttpServletRequest.getContextPath() ).thenReturn( CleanupTransServlet.CONTEXT_PATH );
      when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
      when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
      when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );

      cleanupTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ) );
    }
  }
}
