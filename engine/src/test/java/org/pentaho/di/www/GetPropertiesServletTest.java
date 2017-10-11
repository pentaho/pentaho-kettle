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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.pentaho.di.www.GetPropertiesServlet}
 */
public class GetPropertiesServletTest {

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void getContextPath() throws Exception {

    GetPropertiesServlet servlet = new GetPropertiesServlet();
    servlet.setJettyMode( true );
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetPropertiesServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    when( mockHttpServletResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      private ByteArrayOutputStream baos = new ByteArrayOutputStream();

      @Override public void write( int b ) throws IOException {
        baos.write( b );
      }

      public String toString() {
        return baos.toString();
      }
    } );

    servlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    //check that response is not contains sample text
    Assert.assertFalse( mockHttpServletResponse.getOutputStream().toString()
      .startsWith( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ) );
  }
}
