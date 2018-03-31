/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleXMLException;
import static junit.framework.Assert.*;

public class AddExportServletIT {
  private AddExportServlet addExportServlet;
  private JobMap mockJobMap;
  private TransformationMap mockTransformationMap;

  @Before
  public void setup() {
    mockJobMap = mock( JobMap.class );
    mockTransformationMap = mock( TransformationMap.class );
    addExportServlet = new AddExportServlet( mockJobMap, mockTransformationMap );
  }

  public boolean isEscaped( String string ) {
    return !string.contains( "/" ) && !string.contains( ":" );
  }

  @Test
  public void testDoGetEncodesMessageWithEmptyLoad() throws ServletException, IOException, KettleXMLException {
    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    StringReader in = new StringReader( "" );
    BufferedReader reader = new BufferedReader( in );

    String requestURI = RegisterPackageServlet.CONTEXT_PATH;
    when( mockRequest.getRequestURI() ).thenReturn( requestURI );
    when( mockRequest.getReader() ).thenReturn( reader );
    when( mockResponse.getWriter() ).thenReturn( printWriter );

    addExportServlet.doGet( mockRequest, mockResponse );
    String response = out.toString();
    String message = ServletTestUtils.getInsideOfTag( "message", response );
    assertTrue( message.length() > 0 );
    assertTrue( isEscaped( message ) );
  }
}
