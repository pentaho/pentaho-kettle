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
      public void setReadListener( ReadListener readListener ) {

      }

      @Override public int read() throws IOException {
        return is.read();
      }
    } );
    registerTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    verify( mockHttpServletResponse, times( expectedResponseCode == HttpServletResponse.SC_OK ? 0 : 1 ) ).setStatus( expectedResponseCode );
  }
}
