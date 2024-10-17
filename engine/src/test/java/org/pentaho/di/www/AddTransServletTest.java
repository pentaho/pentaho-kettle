/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddTransServletTest {

  private AddTransServlet addTransServlet;

  @Before
  public void setup() {
    addTransServlet = new AddTransServlet();
  }

  @Test
  public void doGetInvalidXmlTest() throws Exception {
    String xml = "<somexml></xml>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetInvalidTransformationFileTest() throws Exception {
    String xml = "<somexml></somexml>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetMissingTransConfigTagTest() throws Exception {
    String xml = "<transformation>"
      +             "<info>"
      +               "<name>transName</name>"
      +             "</info>"
      +          "</transformation>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetInvalidTransformationNameTest() throws Exception {
    String xml = "<transformation_configuration>"
      +             "<transformation>"
      +               "<info>"
      +                 "<name><img src=\"someurl\"></img></name>"
      +               "</info>"
      +             "</transformation>"
      +          "</transformation_configuration>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetMissingTransformationNameTest() throws Exception {
    String xml = "<transformation_configuration>"
      +             "<transformation>"
      +               "<info>"
      +                 "<name></name>"
      +               "</info>"
      +             "</transformation>"
      +          "</transformation_configuration>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetTransformationOkTest() throws Exception {
    String xml = "<transformation_configuration>"
      +             "<transformation>"
      +               "<info>"
      +                 "<name>test</name>"
      +               "</info>"
      +             "</transformation>"
      +          "</transformation_configuration>";
    doGetTest( xml, HttpServletResponse.SC_OK );
  }

  @Test
  public void doGetValidNameWithInvalidElementTest() throws Exception {
    String xml = "<transformation_configuration>"
      +             "<transformation>"
      +               "<info>"
      +                 "<name>MyTrans<img src=\"someurl\"></img></name>"
      +               "</info>"
      +             "</transformation>"
      +          "</transformation_configuration>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  private void doGetTest( String xmlTrans, int expectedResponseCode ) throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );
    BufferedReader br = new BufferedReader( new StringReader( xmlTrans ) );

    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockHttpServletRequest.getReader() ).thenReturn( br );
    addTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );

    verify( mockHttpServletResponse ).setStatus( HttpServletResponse.SC_OK );
    verify( mockHttpServletResponse ).setStatus( expectedResponseCode );
  }
}
