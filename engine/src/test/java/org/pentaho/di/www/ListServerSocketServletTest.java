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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ListServerSocketServletTest {
  private TransformationMap mockTransformationMap;

  private ListServerSocketServlet listServerSocketServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    listServerSocketServlet = new ListServerSocketServlet( mockTransformationMap );
  }

  @Test
  public void testListServerSocketServletEncodesParametersForHmtlResponse() throws ServletException, IOException {
    try ( MockedStatic<Encode> encodeMockedStatic = mockStatic( Encode.class ) ) {
      HttpServletRequest mockRequest = mock( HttpServletRequest.class );
      HttpServletResponse mockResponse = mock( HttpServletResponse.class );
      SocketPortAllocation mockSocketPortAllocation = mock( SocketPortAllocation.class );
      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ServletOutputStream servletOutputStream = new ServletOutputStream() {

        @Override
        public void write( int b ) {
          byteArrayOutputStream.write( b );
        }
      };

      when( mockRequest.getContextPath() ).thenReturn( ListServerSocketServlet.CONTEXT_PATH );
      when( mockRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING_TO_TEST );
      when( mockResponse.getOutputStream() ).thenReturn( servletOutputStream );
      when(
        mockTransformationMap.allocateServerSocketPort(
          anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
          anyString(), anyString() ) ).thenReturn( mockSocketPortAllocation );

      listServerSocketServlet.doGet( mockRequest, mockResponse );

      String response = byteArrayOutputStream.toString();
      assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", response ) ) );
      encodeMockedStatic.verify( () -> Encode.forHtml( anyString() ), atLeastOnce() );
    }
  }
}
