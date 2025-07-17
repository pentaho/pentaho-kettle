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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * Tests for GetRootServlet class
 *
 * @author Pavel Sakun
 * @see GetRootServlet
 */
public class GetRootServletTest {
  @Test
  public void testDoGetReturn404StatusCode() throws ServletException, IOException {
    GetRootServlet servlet = new GetRootServlet();
    servlet.setJettyMode( true );
    HttpServletRequest request =
        when( mock( HttpServletRequest.class ).getRequestURI() ).thenReturn( "/wrong_path" ).getMock();
    HttpServletResponse response = mock( HttpServletResponse.class );
    servlet.doGet( request, response );
    verify( response ).sendError( HttpServletResponse.SC_NOT_FOUND );
  }
}
