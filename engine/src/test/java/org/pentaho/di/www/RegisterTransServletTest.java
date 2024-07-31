/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import jakarta.servlet.ReadListener;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterTransServletTest {

  private RegisterTransServlet registerTransServlet;

  @Before
  public void setup() {
    registerTransServlet = new RegisterTransServlet();
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    DelegatingMetaStore delegatingMetaStore = mock( DelegatingMetaStore.class );
    registerTransServlet.transformationMap = transformationMap;
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getMetaStore() ).thenReturn( delegatingMetaStore );
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

  private void doGetTest( String xmlTrans, int expectedResponseCode ) throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    ServletOutputStream outputStream = mock( ServletOutputStream.class );
    when( mockHttpServletResponse.getOutputStream() ).thenReturn( outputStream );
    final ByteArrayInputStream is = new ByteArrayInputStream( xmlTrans.getBytes() );
    when( mockHttpServletRequest.getInputStream() ).thenReturn( new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {

      }

      @Override public int read() throws IOException {
        return is.read();
      }
    } );
    registerTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    verify( mockHttpServletResponse, times( expectedResponseCode == HttpServletResponse.SC_OK ? 0 : 1 ) ).setStatus( expectedResponseCode );
  }
}
