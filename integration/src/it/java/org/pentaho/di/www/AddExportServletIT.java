///*! ******************************************************************************
// *
// * Pentaho
// *
// * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
// *
// * Use of this software is governed by the Business Source License included
// * in the LICENSE.TXT file.
// *
// * Change Date: 2029-07-20
// ******************************************************************************/
//
//
//package org.pentaho.di.www;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringReader;
//import java.io.StringWriter;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.pentaho.di.core.exception.KettleXMLException;
//import static junit.framework.Assert.*;
//
//public class AddExportServletIT {
//  private AddExportServlet addExportServlet;
//  private JobMap mockJobMap;
//  private TransformationMap mockTransformationMap;
//
//  @Before
//  public void setup() {
//    mockJobMap = mock( JobMap.class );
//    mockTransformationMap = mock( TransformationMap.class );
//    addExportServlet = new AddExportServlet( mockJobMap, mockTransformationMap );
//  }
//
//  public boolean isEscaped( String string ) {
//    return !string.contains( "/" ) && !string.contains( ":" );
//  }
//
//  @Test
//  public void testDoGetEncodesMessageWithEmptyLoad() throws ServletException, IOException, KettleXMLException {
//    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
//    HttpServletResponse mockResponse = mock( HttpServletResponse.class );
//    StringWriter out = new StringWriter();
//    PrintWriter printWriter = new PrintWriter( out );
//
//    StringReader in = new StringReader( "" );
//    BufferedReader reader = new BufferedReader( in );
//
//    String requestURI = RegisterPackageServlet.CONTEXT_PATH;
//    when( mockRequest.getRequestURI() ).thenReturn( requestURI );
//    when( mockRequest.getReader() ).thenReturn( reader );
//    when( mockResponse.getWriter() ).thenReturn( printWriter );
//
//    addExportServlet.doGet( mockRequest, mockResponse );
//    String response = out.toString();
//    String message = ServletTestUtils.getInsideOfTag( "message", response );
//    assertTrue( message.length() > 0 );
//    assertTrue( isEscaped( message ) );
//  }
//}
